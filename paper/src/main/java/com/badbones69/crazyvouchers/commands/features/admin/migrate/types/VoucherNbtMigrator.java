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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class VoucherNbtMigrator extends IVoucherMigrator {

    public VoucherNbtMigrator(@NotNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_NBT_API);
    }

    @Override
    public void run() {
        final Player player = (Player) this.sender;

        final Inventory inventory = player.getInventory();

        final ItemStack[] contents = inventory.getContents();

        for (final ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) continue;

            NBT.get(item, nbt -> {
                if (nbt.hasTag("voucher")) {
                    item.editMeta(itemMeta -> {
                        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                        if (ConfigManager.getConfig().getProperty(ConfigKeys.dupe_protection)) {
                            container.set(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING, UUID.randomUUID().toString());
                        }

                        container.set(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, nbt.getString("voucher"));
                    });
                }

                if (nbt.hasTag("argument")) {
                    item.editMeta(itemMeta -> {
                        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                        container.set(PersistentKeys.voucher_arg.getNamespacedKey(), PersistentDataType.STRING, nbt.getString("argument"));
                    });
                }
            });
        }

        Messages.migrated_old_vouchers.sendMessage(player);
    }
}