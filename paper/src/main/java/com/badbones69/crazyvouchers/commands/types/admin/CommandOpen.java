package com.badbones69.crazyvouchers.commands.types.admin;

import com.badbones69.crazyvouchers.api.builders.types.VoucherGuiMenu;
import com.badbones69.crazyvouchers.commands.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class CommandOpen extends BaseCommand {

    @Command(value = "open", alias = "admin")
    @Permission(value = "voucher.admin", def = PermissionDefault.OP)
    public void open(CommandSender sender, @Suggestion("numbers") int page) {
        Player player = (Player) sender;

        VoucherGuiMenu menu = new VoucherGuiMenu(player, 54, "<dark_gray><bold><underlined>Vouchers");

        player.openInventory(menu.build(page).getInventory());
    }
}