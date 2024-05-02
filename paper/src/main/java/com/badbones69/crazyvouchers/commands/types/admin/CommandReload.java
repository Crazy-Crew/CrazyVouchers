package com.badbones69.crazyvouchers.commands.types.admin;

import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.commands.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.PermissionDefault;

public class CommandReload extends BaseCommand {

    @Command("reload")
    @Permission(value = "voucher.admin", def = PermissionDefault.OP)
    public void reload(CommandSender sender) {
        this.fileManager.create();

        FileConfiguration configuration = Files.users.getFile();

        if (!configuration.contains("Players")) {
            configuration.set("Players.Clear", null);

            Files.users.save();
        }

        this.crazyManager.reload();

        Messages.config_reload.sendMessage(sender);
    }
}