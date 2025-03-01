package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import de.tr7zw.changeme.nbtapi.NBT;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Permission;
import dev.triumphteam.cmd.core.annotations.Syntax;
import dev.triumphteam.cmd.core.enums.Mode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.UUID;

public class CommandMigrate extends BaseCommand {

    @Command(value = "migrate")
    @Permission(value = "crazyvouchers.migrate", def = Mode.OP)
    @Syntax("/crazyvouchers migrate")
    public void base(final Player player) {
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