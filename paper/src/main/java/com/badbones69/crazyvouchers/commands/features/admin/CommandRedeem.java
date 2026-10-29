package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.Methods;
import com.ryderbelserion.crazyvouchers.utils.ConfigUtils;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import com.badbones69.crazyvouchers.api.enums.PermissionKeys;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemCodeEvent;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.ryderbelserion.fusion.paper.builders.items.ItemBuilder;
import dev.triumphteam.cmd.core.annotations.ArgName;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import dev.triumphteam.cmd.core.annotations.Syntax;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandRedeem extends BaseCommand {

    @Command(value = "redeem")
    @Permission(value = "crazyvouchers.redeem", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers redeem [code]")
    public void redeem(final Player player, @ArgName("code") @Suggestion("codes") String name) {
        final Location location = player.getLocation();

        final Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{arg}", name);
        placeholders.put("{player}", player.getName());
        placeholders.put("{world}", location.getWorld().getName());
        placeholders.put("{x}", String.valueOf(location.x()));
        placeholders.put("{y}", String.valueOf(location.y()));
        placeholders.put("{z}", String.valueOf(location.z()));
        placeholders.put("{prefix}", ConfigUtils.getPrefix());

        if (!this.platform.isVoucherCode(name)) {
            Message.code_unavailable.sendMessage(player, placeholders);

            return;
        }

        final VoucherCode code = this.platform.getVoucherCode(name);

        if (code == null) {
            return;
        }

        final Server server = this.plugin.getServer();

        // Checking the permissions of the code.
        if (!player.isOp() && !PermissionKeys.crazyvouchers_bypass.hasPermission(player)) {
            if (code.useWhiteListPermissions()) {
                final List<String> commands = code.getWhitelistCommands();

                for (final String permission : code.getWhitelistPermissions()) {
                    if (!player.hasPermission(permission)) {
                        Message.no_permission_to_use_voucher.sendMessage(player, placeholders);

                        Methods.dispatch(player, commands, placeholders, true);

                        return;
                    }
                }
            }

            if (code.useWhitelistWorlds()) {
                if (code.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                    player.sendMessage(this.fusion.asComponent(player, code.getWhitelistWorldMessage(), placeholders));

                    Methods.dispatch(player, code.getWhitelistWorldCommands(), placeholders, true);

                    return;
                }
            }

            if (code.useBlacklistPermissions()) {
                final List<String> commands = code.getBlacklistCommands();

                for (final String permission : code.getBlacklistPermissions()) {
                    if (player.hasPermission(permission.toLowerCase())) {
                        player.sendMessage(this.fusion.asComponent(player, code.getBlacklistMessage(), placeholders));

                        Methods.dispatch(player, commands, placeholders, true);

                        return;
                    }
                }
            }
        }

        // Has permission to continue.
        final UUID uuid = player.getUniqueId();
        // Checking if the player has used the code before.

        final String id = code.getStrippedName();

        final boolean isLimiterEnabled = code.useLimiter();

        if (!isLimiterEnabled && this.holder.isCodeRedeemed(id)) {
            Message.code_used.sendMessage(player, placeholders);

            return;
        }

        if (isLimiterEnabled) {
            final int limit = this.holder.getCodeLimit(id, uuid);

            if (limit >= code.getLimit()) {
                Message.code_unavailable.sendMessage(player, placeholders);

                return;
            }

            if (this.holder.isCodeRedeemed(id)) { // only increment if code is redeemed.
                this.holder.incrementCodeLimit(id, uuid);
            }
        }

        // Gives the reward to the player.
        final VoucherRedeemCodeEvent event = new VoucherRedeemCodeEvent(player, code);

        server.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            if (!this.holder.isCodeRedeemed(id)) { // only add if code is not redeemed.
                this.holder.addRedeemedCode(id, uuid);
            }

            code.dispatchCommands(player, placeholders);

            for (final ItemBuilder itemBuilder : code.getItems()) {
                Methods.addItem(player, itemBuilder.asItemStack(player));
            }

            if (code.useSounds()) {
                for (final Sound sound : code.getSounds()) {
                    player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, code.getVolume(), code.getPitch());
                }
            }

            if (code.useFireworks()) Methods.firework(player.getLocation(), code.getFireworkColors());

            final String message = code.getMessage();

            if (!message.isEmpty()) {
                player.sendMessage(this.fusion.asComponent(player, message, placeholders));
            }
        }
    }
}