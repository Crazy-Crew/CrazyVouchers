package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class VoucherNbtMigrator extends IVoucherMigrator {

    public VoucherNbtMigrator(@NotNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_NBT_API);
    }

    @Override
    public void run() {
        final Player player = (Player) this.sender;

        final Inventory inventory = player.getInventory();

        final ItemStack[] contents = inventory.getContents();

        AtomicInteger count = new AtomicInteger();

        for (final ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) continue;

            NBT.get(item, nbt -> {
                final boolean hasVoucherTag = nbt.hasTag("voucher");
                final boolean hasVoucherArg = nbt.hasTag("argument");

                if (hasVoucherTag || hasVoucherArg) {
                    count.getAndIncrement();
                }

                if (hasVoucherTag) {
                    item.editPersistentDataContainer(container -> {
                        if (ConfigManager.getConfig().getProperty(ConfigKeys.dupe_protection)) {
                            container.set(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING, UUID.randomUUID().toString());
                        }

                        container.set(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, nbt.getString("voucher"));
                    });
                }

                if (hasVoucherArg) {
                    item.editPersistentDataContainer(container -> container.set(PersistentKeys.voucher_arg.getNamespacedKey(), PersistentDataType.STRING, nbt.getString("argument")));
                }
            });
        }

        this.fusion.log("warn", "Successfully migrated <green>{}</green> items that were using legacy api!", count);

        Messages.migrated_old_vouchers.sendMessage(player);
    }
}