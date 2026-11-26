package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;
import us.crazycrew.crazyvouchers.api.objects.other.PluginVersion;
import java.util.List;
import java.util.Map;

public class CommandVersion extends BaseCommand {

    @Command("version")
    @Permission(value = "crazyvouchers.version", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers version")
    public void version(final CommandSender sender) {
        final PluginVersion.VersionInfo versionInfo = this.platform.getVersionInfo();

        final PluginVersion.GitInfo git = versionInfo.git();

        final Map<String, String> placeholders = Map.of(
                "{version}", versionInfo.version(),
                "{apiVersion}", versionInfo.apiVersion(),
                "{current_commit}", git.current(),
                "{previous_commit}", git.previous()
        );

        sender.sendMessage(
                this.fusion.asComponent(StringUtils.toString(List.of(
                        "<bold><gold>━━━━━━━━━━━━━━━━━━━ Plugin Version ━━━━━━━━━━━━━━━━━━━</gold></bold>",
                        " <red>⤷ <gold>Version: <red>{version}",
                        " <red>⤷ <gold>Api Version: <red>{apiVersion}",
                        "",
                        " ⤷ <gold>Current Commit: <red>{current_commit}",
                        " ⤷ <gold>Previous Commit: <red>{previous_commit}",
                        "<bold><gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold></bold>"
                )), placeholders)
        );
    }
}