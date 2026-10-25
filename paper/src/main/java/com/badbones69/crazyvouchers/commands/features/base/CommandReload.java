package com.badbones69.crazyvouchers.commands.features.base;

import com.badbones69.crazyvouchers.Methods;
import us.crazycrew.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.ryderbelserion.fusion.files.enums.FileType;
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
        this.fusion.reload();

        ConfigManager.refresh();

        final FileSystem system = this.config.getProperty(ConfigKeys.file_system);

        this.fileManager.purge();

        this.fileManager.addFile(this.dataPath.resolve("users.yml"), FileType.YAML)
                .addFile(this.dataPath.resolve("data.yml"), FileType.YAML)
                .addFile(this.dataPath.resolve("version.json"), FileType.JSON);

        switch (system) {
            case MULTIPLE -> {
                this.fileManager.removeFile(this.dataPath.resolve("codes.yml"))
                        .removeFile(this.dataPath.resolve("vouchers.yml"));

                this.fileManager.addFolder(this.dataPath.resolve("codes"), FileType.YAML)
                        .addFolder(this.dataPath.resolve("vouchers"), FileType.YAML);
            }

            case SINGLE -> this.fileManager.addFile(this.dataPath.resolve("codes.yml"), FileType.YAML)
                    .addFile(this.dataPath.resolve("vouchers.yml"), FileType.YAML);
        }

        Methods.janitor();

        this.crazyManager.reload();

        Messages.config_reload.sendMessage(sender);
    }
}