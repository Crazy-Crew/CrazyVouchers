package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.enums.keys.PermissionKeys;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemCodeEvent;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.api.objects.VoucherCommand;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import com.ryderbelserion.fusion.paper.builder.items.modern.ItemBuilder;
import dev.triumphteam.cmd.core.annotations.ArgName;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Permission;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import dev.triumphteam.cmd.core.annotations.Syntax;
import dev.triumphteam.cmd.core.enums.Mode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CommandRedeem extends BaseCommand {

    @Command(value = "redeem")
    @Permission(value = "crazyvouchers.redeem", def = Mode.OP)
    @Syntax("/crazyvouchers redeem [code]")
    public void redeem(final Player player, @ArgName("code") @Suggestion("codes") String name) {
        final Location location = player.getLocation();

        final Map<String, String> placeholders = new HashMap<>() {{
            put("{arg}", name);
            put("{player}", player.getName());
            put("{world}", location.getWorld().getName());
            put("{x}", String.valueOf(location.x()));
            put("{y}", String.valueOf(location.y()));
            put("{z}", String.valueOf(location.z()));
            put("{prefix}", ConfigManager.getConfig().getProperty(ConfigKeys.command_prefix));
        }};

        final VoucherCode code = this.crazyManager.getVoucherCode(name);

        if (code == null) {
            Messages.code_unavailable.sendMessage(player, placeholders);

            return;
        }

        final Server server = this.plugin.getServer();

        // Checking the permissions of the code.
        if (!player.isOp() && !PermissionKeys.crazyvouchers_bypass.hasPermission(player)) {
            if (code.useWhiteListPermissions()) {
                final List<String> commands = code.getWhitelistCommands();

                for (final String permission : code.getWhitelistPermissions()) {
                    if (!player.hasPermission(permission)) {
                        Messages.no_permission_to_use_voucher.sendMessage(player, placeholders);

                        for (final String command : commands) {
                            server.dispatchCommand(server.getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                        }

                        return;
                    }
                }
            }

            if (code.useWhitelistWorlds()) {
                if (code.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                    final List<String> commands = code.getWhitelistWorldCommands();

                    player.sendMessage(Methods.replacePlaceholders(placeholders, code.getWhitelistWorldMessage(), true));

                    for (final String command : commands) {
                        server.dispatchCommand(server.getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                    }

                    return;
                }
            }

            if (code.useBlacklistPermissions()) {
                final List<String> commands = code.getBlacklistCommands();

                for (final String permission : code.getBlacklistPermissions()) {
                    if (player.hasPermission(permission.toLowerCase())) {
                        player.sendMessage(Methods.replacePlaceholders(placeholders, code.getBlacklistMessage(), true));

                        for (final String command : commands) {
                            server.dispatchCommand(server.getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                        }

                        return;
                    }
                }
            }
        }

        // Has permission to continue.
        final FileConfiguration data = Files.users.getConfiguration();
        final String uuid = player.getUniqueId().toString();
        // Checking if the player has used the code before.

        if (data.contains("Players." + uuid)) {
            if (data.contains("Players." + uuid + ".Codes." + code.getName())) {
                if (data.getString("Players." + uuid + ".Codes." + code.getName()).equalsIgnoreCase("used")) {
                    Messages.code_used.sendMessage(player, placeholders);

                    return;
                }
            }
        }

        //Checking the limit of the code.
        if (code.useLimiter()) {
            if (data.contains("Voucher-Limit." + code.getName())) {
                if (data.getInt("Voucher-Limit." + code.getName()) < 1) {
                    Messages.code_unavailable.sendMessage(player, placeholders);

                    return;
                }

                data.set("Voucher-Limit." + code.getName(), (data.getInt("Voucher-Limit." + code.getName()) - 1));
            } else {
                data.set("Voucher-Limit." + code.getName(), (code.getLimit() - 1));
            }

            Files.users.save();
        }

        // Gives the reward to the player.
        final VoucherRedeemCodeEvent event = new VoucherRedeemCodeEvent(player, code);

        server.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            data.set("Players." + uuid + ".Codes." + code.getName(), "used");

            Files.users.save();

            for (final String command : code.getCommands()) {
                server.dispatchCommand(server.getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
            }

            final List<VoucherCommand> random = code.getRandomCommands();

            if (!random.isEmpty()) { // Picks a random command from the Random-Commands list.
                for (final String command : random.get(Methods.getRandom(random.size())).getCommands()) {
                    server.dispatchCommand(server.getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                }
            }

            final List<VoucherCommand> chance = code.getChanceCommands();

            if (!chance.isEmpty()) { // Picks a command based on the chance system of the Chance-Commands list.
                for (String command : chance.get(Methods.getRandom(chance.size())).getCommands()) {
                    server.dispatchCommand(server.getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                }
            }

            for (final ItemBuilder itemBuilder : code.getItems()) {
                Methods.addItem(player, itemBuilder.asItemStack(true));
            }

            if (code.useSounds()) {
                for (final Sound sound : code.getSounds()) {
                    player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, code.getVolume(), code.getPitch());
                }
            }

            if (code.useFireworks()) Methods.firework(player.getLocation(), code.getFireworkColors());

            if (!code.getMessage().isEmpty()) player.sendMessage(MsgUtils.color(Methods.replacePlaceholders(placeholders, code.getMessage(), true)));
        }
    }
}