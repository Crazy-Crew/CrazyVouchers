package com.badbones69.crazyvouchers.commands.types.player;

import com.badbones69.crazyvouchers.api.builders.ItemBuilder;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemCodeEvent;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.commands.types.BaseCommand;
import com.badbones69.crazyvouchers.platform.util.MiscUtil;
import com.badbones69.crazyvouchers.platform.util.MsgUtil;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CommandRedeem extends BaseCommand {

    @Command("redeem")
    @Permission(value = "voucher.redeem", def = PermissionDefault.TRUE)
    public void redeem(Player player, String code) {
        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{arg}", code);
        placeholders.put("{player}", player.getName());
        placeholders.put("{world}", player.getWorld().getName());
        placeholders.put("{x}", String.valueOf(player.getLocation().getBlockX()));
        placeholders.put("{y}", String.valueOf(player.getLocation().getBlockY()));
        placeholders.put("{z}", String.valueOf(player.getLocation().getBlockZ()));
        placeholders.put("{prefix}", MsgUtil.getPrefix());

        if (this.crazyManager.isVoucherCode(code)) {
            VoucherCode voucherCode = this.crazyManager.getVoucherCode(code);

            // Checking the permissions of the code.
            if (!player.isOp() && !player.hasPermission("voucher.bypass")) {
                if (voucherCode.useWhiteListPermissions()) {
                    for (String permission : voucherCode.getWhitelistPermissions()) {
                        if (!player.hasPermission(permission)) {
                            Messages.no_permission_to_use_voucher.sendMessage(player, placeholders);

                            for (String command : voucherCode.getWhitelistCommands()) {
                                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                            }

                            return;
                        }
                    }
                }

                if (voucherCode.useWhitelistWorlds()) {
                    if (voucherCode.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                        player.sendMessage(MiscUtil.replacePlaceholders(placeholders, voucherCode.getWhitelistWorldMessage(), true));

                        for (String command : voucherCode.getWhitelistWorldCommands()) {
                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                        }

                        return;
                    }
                }

                if (voucherCode.useBlacklistPermissions()) {
                    for (String permission : voucherCode.getBlacklistPermissions()) {
                        if (player.hasPermission(permission.toLowerCase())) {
                            player.sendMessage(MiscUtil.replacePlaceholders(placeholders, voucherCode.getBlacklistMessage(), true));

                            for (String command : voucherCode.getBlacklistCommands()) {
                                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                            }

                            return;
                        }
                    }
                }
            }

            // Has permission to continue.
            FileConfiguration data = Files.users.getFile();
            String uuid = player.getUniqueId().toString();
            // Checking if the player has used the code before.

            if (data.contains("Players." + uuid)) {
                if (data.contains("Players." + uuid + ".Codes." + voucherCode.getName())) {
                    if (data.getString("Players." + uuid + ".Codes." + voucherCode.getName()).equalsIgnoreCase("used")) {
                        Messages.code_used.sendMessage(player, placeholders);

                        return;
                    }
                }
            }

            // Checking the limit of the code.
            if (voucherCode.useLimiter()) {
                if (data.contains("Voucher-Limit." + voucherCode.getName())) {
                    if (data.getInt("Voucher-Limit." + voucherCode.getName()) < 1) {
                        Messages.code_unavailable.sendMessage(player, placeholders);

                        return;
                    }

                    data.set("Voucher-Limit." + voucherCode.getName(), (data.getInt("Voucher-Limit." + voucherCode.getName()) - 1));
                } else {
                    data.set("Voucher-Limit." + voucherCode.getName(), (voucherCode.getLimit() - 1));
                }

                Files.users.save();
            }

            // Gives the reward to the player.
            VoucherRedeemCodeEvent event = new VoucherRedeemCodeEvent(player, voucherCode);
            this.plugin.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                data.set("Players." + uuid + ".Codes." + voucherCode.getName(), "used");
                Files.users.save();

                for (String command : voucherCode.getCommands()) {
                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, crazyManager.replaceRandom(command), true));
                }

                if (!voucherCode.getRandomCommands().isEmpty()) { // Picks a random command from the Random-Commands list.
                    for (String command : voucherCode.getRandomCommands().get(ThreadLocalRandom.current().nextInt(voucherCode.getRandomCommands().size())).getCommands()) {
                        this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                    }
                }

                if (!voucherCode.getChanceCommands().isEmpty()) { // Picks a command based on the chance system of the Chance-Commands list.
                    for (String command : voucherCode.getChanceCommands().get(ThreadLocalRandom.current().nextInt(voucherCode.getChanceCommands().size())).getCommands()) {
                        this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                    }
                }

                for (ItemBuilder itemBuilder : voucherCode.getItems()) {
                    if (!MiscUtil.isInventoryFull(player)) {
                        player.getInventory().addItem(itemBuilder.build());
                    } else {
                        player.getWorld().dropItem(player.getLocation(), itemBuilder.build());
                    }
                }

                if (voucherCode.useSounds()) {
                    for (Sound sound : voucherCode.getSounds()) {
                        player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, voucherCode.getVolume(), voucherCode.getPitch());
                    }
                }

                if (voucherCode.useFireworks()) MiscUtil.firework(player.getLocation(), voucherCode.getFireworkColors());

                if (!voucherCode.getMessage().isEmpty()) player.sendMessage(MsgUtil.color(MiscUtil.replacePlaceholders(placeholders, voucherCode.getMessage(), true)));
            }

            return;
        }

        Messages.code_unavailable.sendMessage(player, placeholders);
    }
}