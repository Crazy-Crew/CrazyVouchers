package com.badbones69.crazyvouchers.commands.features.base;

import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.core.api.enums.FileType;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;
import java.util.ArrayList;

public class CommandReload extends BaseCommand {

    @Command("reload")
    @Permission(value = "crazyvouchers.reload", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers reload")
    public void reload(final CommandSender sender) {
        this.plugin.getFusion().reload();

        ConfigManager.refresh();

        final FileSystem system = this.config.getProperty(ConfigKeys.file_system);

        this.fileManager.purge();

        this.fileManager.addFile(this.dataPath.resolve("users.yml"), FileType.PAPER, new ArrayList<>(), null)
                .addFile(this.dataPath.resolve("data.yml"), FileType.PAPER, new ArrayList<>(), null);

        switch (system) {
            case MULTIPLE -> {
                this.fileManager.removeFile(this.dataPath.resolve("codes.yml"), null)
                        .removeFile(this.dataPath.resolve("vouchers.yml"), null);

                this.fileManager.addFolder(this.dataPath.resolve("codes"), FileType.PAPER, new ArrayList<>(), null)
                        .addFolder(this.dataPath.resolve("vouchers"), FileType.PAPER, new ArrayList<>(), null);
            }

            case SINGLE -> this.fileManager.addFile(this.dataPath.resolve("codes.yml"), FileType.PAPER, new ArrayList<>(), null)
                    .addFile(this.dataPath.resolve("vouchers.yml"), FileType.PAPER, new ArrayList<>(), null);
        }

        Methods.janitor();

        this.crazyManager.reload();

        Messages.config_reload.sendMessage(sender);
    }
}