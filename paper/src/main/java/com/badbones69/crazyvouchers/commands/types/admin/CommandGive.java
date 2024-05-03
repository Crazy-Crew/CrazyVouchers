package com.badbones69.crazyvouchers.commands.types.admin;

import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.objects.v2.GenericVoucher;
import com.badbones69.crazyvouchers.commands.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Optional;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionDefault;
import java.util.HashMap;
import java.util.Map;

public class CommandGive extends BaseCommand {

    @Command(value = "give")
    @Permission(value = "voucher.admin", def = PermissionDefault.OP)
    public void give(Player player, @Suggestion("vouchers") String voucherName, @Suggestion("numbers") int amount) {
        GenericVoucher voucher = this.crazyHandler.getVoucher(voucherName);

        if (voucher == null) {
            Messages.not_a_voucher.sendMessage(player);

            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack item = voucher.getItem(player);

        if (inventory.firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), item);
        } else {
            inventory.setItem(inventory.firstEmpty(), item);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{player}", player.getName());
        placeholders.put("{voucher}", voucher.getFileName());

        Messages.sent_voucher.sendMessage(player, placeholders);

        /*
        Voucher voucher = this.crazyManager.getVoucher(type);

        Player player = this.plugin.getServer().getPlayer(name);

        ItemStack item = argument != null ? voucher.buildItem(argument.replace("%random%", "{random}"), amount) : voucher.buildItem(amount);*/
    }

    @Command(value = "givealll")
    @Permission(value = "voucher.admin", def = PermissionDefault.OP)
    public void all(CommandSender sender, @Suggestion("vouchers") String type, @Suggestion("numbers") int amount, @Optional String argument) {
        /*if (this.crazyManager.isVoucherName(type)) {
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

        Messages.sent_everyone_voucher.sendMessage(sender, placeholders);*/
    }
}