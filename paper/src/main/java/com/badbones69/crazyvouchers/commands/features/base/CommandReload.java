package com.badbones69.crazyvouchers.commands.features.base;

import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.api.enums.FileType;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Permission;
import dev.triumphteam.cmd.core.annotations.Syntax;
import dev.triumphteam.cmd.core.enums.Mode;
import org.bukkit.command.CommandSender;

public class CommandReload extends BaseCommand {

    @Command("reload")
    @Permission(value = "crazyvouchers.reload", def = Mode.OP)
    @Syntax("/crazyvouchers reload")
    public void reload(final CommandSender sender) {
        this.plugin.getFusion().reload();

        ConfigManager.refresh();

        final FileSystem system = this.config.getProperty(ConfigKeys.file_system);

        this.fileManager.purge();

        this.fileManager.addFile("users.yml").addFile("data.yml");

        switch (system) {
            case MULTIPLE -> {
                this.fileManager.removeFile("codes.yml", FileType.YAML, false);
                this.fileManager.removeFile("vouchers.yml", FileType.YAML, false);

                this.fileManager.addFolder("codes", FileType.YAML).addFolder("vouchers", FileType.YAML);
            }

            case SINGLE -> this.fileManager.addFile("codes.yml", FileType.YAML).addFile("vouchers.yml", FileType.YAML);
        }

        Methods.janitor();

        this.crazyManager.reload();

        Messages.config_reload.sendMessage(sender);
    }
}