package com.badbones69.crazyvouchers.commands.features.base;

import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Permission;
import dev.triumphteam.cmd.core.annotations.Syntax;
import dev.triumphteam.cmd.core.enums.Mode;
import org.bukkit.command.CommandSender;

public class CommandHelp extends BaseCommand {

    @Command
    @Permission(value = "crazyvouchers.access", def = Mode.OP)
    @Syntax("/crazyvouchers")
    public void base(CommandSender sender) {
        Messages.help.sendMessage(sender);
    }

    @Command("help")
    @Permission(value = "crazyvouchers.help", def = Mode.OP)
    @Syntax("/crazyvouchers help")
    public void help(final CommandSender sender) {
        Messages.help.sendMessage(sender);
    }
}