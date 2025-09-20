package com.badbones69.crazyvouchers.commands.features.base;

import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.core.files.enums.FileType;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class CommandReload extends BaseCommand {

    @Command("reload")
    @Permission(value = "crazyvouchers.reload", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers reload")
    public void reload(final CommandSender sender) {
        this.plugin.getFusion().reload();

        ConfigManager.refresh();

        final FileSystem system = this.config.getProperty(ConfigKeys.file_system);

        this.fileManager.purge();

        this.fileManager.addFile(this.dataPath.resolve("users.yml"), FileType.PAPER)
                .addFile(this.dataPath.resolve("data.yml"), FileType.PAPER);

        switch (system) {
            case MULTIPLE -> {
                this.fileManager.removeFile(this.dataPath.resolve("codes.yml"))
                        .removeFile(this.dataPath.resolve("vouchers.yml"));

                this.fileManager.addFolder(this.dataPath.resolve("codes"), FileType.PAPER)
                        .addFolder(this.dataPath.resolve("vouchers"), FileType.PAPER);
            }

            case SINGLE -> this.fileManager.addFile(this.dataPath.resolve("codes.yml"), FileType.PAPER)
                    .addFile(this.dataPath.resolve("vouchers.yml"), FileType.PAPER);
        }

        Methods.janitor();

        this.crazyManager.reload();

        Messages.config_reload.sendMessage(sender);
    }
}