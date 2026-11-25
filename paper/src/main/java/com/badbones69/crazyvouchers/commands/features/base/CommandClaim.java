package com.badbones69.crazyvouchers.commands.features.base;

import com.badbones69.crazyvouchers.commands.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Syntax;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionDefault;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;

public class CommandClaim extends BaseCommand {

    @Command(value = "claim")
    @Permission(value = "crazyvouchers.claim", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers claim")
    public void redeem(final Player player) {
        if (!this.config.getProperty(RootKeys.offline_player_support)) {
            Message.feature_disabled.sendMessage(player);

            return;
        }

        final PlayerInventory inventory = player.getInventory();

        if (!inventory.isEmpty()) {
            Message.inventory_not_empty.sendMessage(player);

            return;
        }

        this.userRegistry.getUser(player.getUniqueId()).ifPresentOrElse(target -> {
            this.userRegistry.loadVouchers(target);

            Message.claimed_vouchers.sendMessage(player);
        }, () -> Message.cannot_find_in_cache.sendMessage(player));
    }
}