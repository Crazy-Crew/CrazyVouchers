package com.badbones69.crazyvouchers.commands.features.base;

import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.core.api.enums.FileType;
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
        ConfigManager.refresh();

        boolean loadOldWay = ConfigManager.getConfig().getProperty(ConfigKeys.mono_file);

        this.fileManager.purge();

        this.fileManager.addFile("users.yml").addFile("data.yml");

        if (loadOldWay) {
            this.fileManager.addFile("voucher-codes.yml").addFile("vouchers.yml");
        } else {
            this.fileManager.removeFile("voucher-codes.yml", FileType.YAML, false);
            this.fileManager.removeFile("vouchers.yml", FileType.YAML, false);

            this.fileManager.addFolder("codes", FileType.YAML).addFolder("vouchers", FileType.YAML);
        }

        this.fileManager.init();

        Methods.janitor();

        this.crazyManager.reload();

        Messages.config_reload.sendMessage(sender);
    }
}