package com.badbones69.crazyvouchers.commands.features.relations;

import com.badbones69.crazyvouchers.api.enums.config.MessageKeys;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.extention.meta.MetaKey;
import dev.triumphteam.cmd.core.message.MessageKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ArgumentRelations {

    private final BukkitCommandManager<CommandSender> commandManager;

    public ArgumentRelations(@NotNull final BukkitCommandManager<CommandSender> commandManager) {
        this.commandManager = commandManager;
    }

    public void build() {
        this.commandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> context.getMeta().get(MetaKey.SYNTAX).ifPresent(key -> MessageKeys.correct_usage.sendMessage(sender, "{usage}", key)));

        this.commandManager.registerMessage(MessageKey.TOO_MANY_ARGUMENTS, (sender, context) -> context.getMeta().get(MetaKey.SYNTAX).ifPresent(key -> MessageKeys.correct_usage.sendMessage(sender, "{usage}", key)));

        this.commandManager.registerMessage(MessageKey.INVALID_ARGUMENT, (sender, context) -> MessageKeys.correct_usage.sendMessage(sender, "{usage}", context.getSyntax()));

        this.commandManager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, context) -> MessageKeys.no_permission.sendMessage(sender, "{permission}", context.getPermission().toString()));

        this.commandManager.registerMessage(BukkitMessageKey.UNKNOWN_COMMAND, (sender, context) -> MessageKeys.unknown_command.sendMessage(sender, "{command}", context.getInvalidInput()));

        this.commandManager.registerMessage(BukkitMessageKey.CONSOLE_ONLY, (sender, context) -> MessageKeys.must_be_console_sender.sendMessage(sender));

        this.commandManager.registerMessage(BukkitMessageKey.PLAYER_ONLY, (sender, context) -> MessageKeys.player_only.sendMessage(sender));
    }
}