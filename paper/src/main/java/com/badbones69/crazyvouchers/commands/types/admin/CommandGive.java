package com.badbones69.crazyvouchers.commands.types.admin;

import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.commands.types.BaseCommand;
import com.badbones69.crazyvouchers.platform.util.MiscUtil;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Optional;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.Map;

public class CommandGive extends BaseCommand {

    //todo() add 2 separate flags, one for giving codes and one for giving vouchers

    @Command(value = "give")
    @Permission(value = "voucher.admin", def = PermissionDefault.OP)
    public void give(CommandSender sender, String type, int amount, @Suggestion("players") String name, @Optional String argument) {
        if (this.crazyManager.isVoucherName(type)) {
            Messages.not_a_voucher.sendMessage(sender);

            return;
        }

        Voucher voucher = this.crazyManager.getVoucher(type);

        Player player = this.plugin.getServer().getPlayer(name);

        ItemStack item = argument != null ? voucher.buildItem(argument.replace("%random%", "{random}"), amount) : voucher.buildItem(amount);

        if (player != null) {
            if (MiscUtil.isInventoryFull(player)) {
                player.getWorld().dropItem(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
                player.updateInventory();
            }

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("{player}", player.getName());
            placeholders.put("{voucher}", voucher.getName());

            if (!Messages.sent_voucher.isBlank()) Messages.sent_voucher.sendMessage(sender, placeholders);

            return;
        }

        Messages.not_online.sendMessage(sender);
    }

    @Command(value = "givealll")
    @Permission(value = "voucher.admin", def = PermissionDefault.OP)
    public void all(CommandSender sender, String type, int amount, @Optional String argument) {
        if (this.crazyManager.isVoucherName(type)) {
            Messages.not_a_voucher.sendMessage(sender);

            return;
        }

        Voucher voucher = this.crazyManager.getVoucher(type);

        ItemStack item = argument != null ? voucher.buildItem(argument.replace("%random%", "{random}"), amount) : voucher.buildItem(amount);

        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (MiscUtil.isInventoryFull(player)) {
                player.getWorld().dropItem(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
                player.updateInventory();
            }
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{voucher}", voucher.getName());

        Messages.sent_everyone_voucher.sendMessage(sender, placeholders);
    }
}