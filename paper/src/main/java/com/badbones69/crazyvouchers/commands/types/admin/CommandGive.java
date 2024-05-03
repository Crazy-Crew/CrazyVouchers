package com.badbones69.crazyvouchers.commands.types.admin;

import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.v2.GenericVoucher;
import com.badbones69.crazyvouchers.commands.types.BaseCommand;
import com.badbones69.crazyvouchers.platform.util.MiscUtil;
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
    public void give(Player player, @Suggestion("vouchers") String voucherName, @Suggestion("numbers") int amount, @Optional String argument) {
        GenericVoucher voucher = this.crazyHandler.getVoucher(voucherName);

        if (voucher == null) {
            Messages.not_a_voucher.sendMessage(player);

            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack item = argument != null ? voucher.getItem(player, argument.replace("%random%", "{random}"), amount).setString(PersistentKeys.voucher_item.getNamespacedKey(), voucher.getFileName()).build() : voucher.getItem(player, amount);

        if (inventory.firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), item);
        } else {
            inventory.setItem(inventory.firstEmpty(), item);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{player}", player.getName());
        placeholders.put("{voucher}", voucher.getFileName());

        Messages.sent_voucher.sendMessage(player, placeholders);
    }

    @Command(value = "giveall")
    @Permission(value = "voucher.admin", def = PermissionDefault.OP)
    public void all(CommandSender sender, @Suggestion("vouchers") String voucherName, @Suggestion("numbers") int amount, @Optional String argument) {
        GenericVoucher voucher = this.crazyHandler.getVoucher(voucherName);

        if (voucher == null) {
            Messages.not_a_voucher.sendMessage(sender);

            return;
        }

        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            ItemStack item = argument != null ? voucher.getItem(player, argument.replace("%random%", "{random}"), amount).setString(PersistentKeys.voucher_item.getNamespacedKey(), voucher.getFileName()).build() : voucher.getItem(player, amount);

            if (!MiscUtil.isInventoryFull(player)) {
                PlayerInventory inventory = player.getInventory();

                inventory.setItem(inventory.firstEmpty(), item);
                player.updateInventory();
            } else {
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{voucher}", voucher.getFileName());

        Messages.sent_everyone_voucher.sendMessage(sender, placeholders);
    }
}