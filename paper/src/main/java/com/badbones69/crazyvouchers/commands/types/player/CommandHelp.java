package com.badbones69.crazyvouchers.commands.types.player;

import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.commands.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class CommandHelp extends BaseCommand {

    @Command()
    public void base(CommandSender sender) {
        help(sender);
    }

    @Command("help")
    @Permission(value = "voucher.access", def = PermissionDefault.TRUE)
    public void help(CommandSender sender) {
        Messages.help.sendMessage(sender);
    }
}