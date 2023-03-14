package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.controllers.GUI;
import com.badbones69.crazyvouchers.api.FileManager;
import com.badbones69.crazyvouchers.api.FileManager.Files;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.commands.VoucherCommands;
import com.badbones69.crazyvouchers.commands.VoucherTab;
import com.badbones69.crazyvouchers.controllers.FireworkDamageAPI;
import com.badbones69.crazyvouchers.controllers.VoucherClick;
import com.badbones69.crazyvouchers.support.MetricsHandler;
import com.badbones69.crazyvouchers.support.libraries.UpdateChecker;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CrazyVouchers extends JavaPlugin implements Listener {

    private static CrazyVouchers plugin;

    private FileManager fileManager;

    private CrazyManager crazyManager;

    private Methods methods;

    private FireworkDamageAPI fireworkDamageAPI;

    private GUI gui;

    @Override
    public void onEnable() {
        plugin = this;

        fileManager = new FileManager();

        crazyManager = new CrazyManager();

        methods = new Methods();

        fileManager.logInfo(true).setup();

        if (!Files.DATA.getFile().contains("Players")) {
            Files.DATA.getFile().set("Players.Clear", null);
            Files.DATA.saveFile();
        }

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new VoucherClick(), this);
        pluginManager.registerEvents(gui = new GUI(), this);
        pluginManager.registerEvents(fireworkDamageAPI = new FireworkDamageAPI(), this);

        registerCommand(getCommand("vouchers"), new VoucherTab(), new VoucherCommands());

        Messages.addMissingMessages();

        FileConfiguration config = Files.CONFIG.getFile();

        boolean metricsEnabled = Files.CONFIG.getFile().getBoolean("Settings.Toggle-Metrics");
        String metricsPath = Files.CONFIG.getFile().getString("Settings.Toggle-Metrics");

        String version = config.getString("Settings.Config-Version");

        if (metricsPath == null) {
            config.set("Settings.Toggle-Metrics", true);

            Files.CONFIG.saveFile();
        }

        if (version == null) {
            config.set("Settings.Config-Version", 1);

            Files.CONFIG.saveFile();
        }

        String updater = config.getString("Settings.Update-Checker");

        if (updater == null) {
            config.set("Settings.Update-Checker", true);

            Files.CONFIG.saveFile();
        }

        int configVersion = 1;
        if (configVersion != config.getInt("Settings.Config-Version") && version != null) {
            plugin.getLogger().warning("========================================================================");
            plugin.getLogger().warning("You have an outdated config, Please run the command /vouchers update!");
            plugin.getLogger().warning("This will take a backup of your entire folder & update your configs.");
            plugin.getLogger().warning("Default values will be used in place of missing options!");
            plugin.getLogger().warning("If you have any issues, Please contact Discord Support.");
            plugin.getLogger().warning("https://discord.gg/crazycrew");
            plugin.getLogger().warning("========================================================================");
        }

        if (metricsEnabled) {
            MetricsHandler metricsHandler = new MetricsHandler();

            metricsHandler.start();
        }

        checkUpdate();

        crazyManager.load();
    }

    private void registerCommand(PluginCommand pluginCommand, TabCompleter tabCompleter, CommandExecutor commandExecutor) {
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);

            if (tabCompleter != null) pluginCommand.setTabCompleter(tabCompleter);
        }
    }

    private void checkUpdate() {
        FileConfiguration config = Files.CONFIG.getFile();

        boolean updaterEnabled = config.getBoolean("Settings.Update-Checker");

        if (!updaterEnabled) return;

        getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            UpdateChecker updateChecker = new UpdateChecker(13654);

            try {
                if (updateChecker.hasUpdate() && !getDescription().getVersion().contains("SNAPSHOT")) {
                    getLogger().warning("CrazyVouchers has a new update available! New version: " + updateChecker.getNewVersion());
                    getLogger().warning("Current Version: v" + getDescription().getVersion());
                    getLogger().warning("Download: " + updateChecker.getResourcePage());

                    return;
                }

                getLogger().info("Plugin is up to date! - " + updateChecker.getNewVersion());
            } catch (Exception exception) {
                getLogger().warning("Could not check for updates! Perhaps the call failed or you are using a snapshot build:");
                getLogger().warning("You can turn off the update checker in config.yml if on a snapshot build.");
            }
        });
    }

    public static CrazyVouchers getPlugin() {
        return plugin;
    }

    public CrazyManager getCrazyManager() {
        return crazyManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public Methods getMethods() {
        return methods;
    }

    public FireworkDamageAPI getFireworkDamageAPI() {
        return fireworkDamageAPI;
    }

    public GUI getGui() {
        return gui;
    }
}