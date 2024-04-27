package com.badbones69.crazyvouchers.platform.util;

import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import com.badbones69.crazyvouchers.platform.config.types.ConfigKeys;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public class MiscUtil {

    public static boolean isLogging() {
        return ConfigManager.getConfig().getProperty(ConfigKeys.verbose_logging);
    }

    public static void registerCommand(PluginCommand pluginCommand, TabCompleter tabCompleter, CommandExecutor commandExecutor) {
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);

            if (tabCompleter != null) pluginCommand.setTabCompleter(tabCompleter);
        }
    }
}