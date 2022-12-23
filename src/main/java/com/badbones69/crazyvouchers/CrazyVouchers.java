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
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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

        if (metricsPath == null) {
            config.set("Settings.Toggle-Metrics", true);

            Files.CONFIG.saveFile();
        }

        String updater = config.getString("Settings.Update-Checker");

        if (updater == null) {
            config.set("Settings.Update-Checker", true);

            Files.CONFIG.saveFile();
        }

        if (metricsEnabled) new Metrics(this, 4536);

        checkUpdate(null, true);

        crazyManager.load();
    }

    private void registerCommand(PluginCommand pluginCommand, TabCompleter tabCompleter, CommandExecutor commandExecutor) {
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);

            if (tabCompleter != null) pluginCommand.setTabCompleter(tabCompleter);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        checkUpdate(e.getPlayer(), false);
    }

    private void checkUpdate(Player player, boolean consolePrint) {
        FileConfiguration config = Files.CONFIG.getFile();

        boolean updaterEnabled = config.getBoolean("Settings.Update-Checker");

        if (!updaterEnabled) return;

        getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            UpdateChecker updateChecker = new UpdateChecker(13654);

            try {
                if (updateChecker.hasUpdate() && !getDescription().getVersion().contains("SNAPSHOT")) {
                    if (consolePrint) {
                        getLogger().warning("CrazyVouchers has a new update available! New version: " + updateChecker.getNewVersion());
                        getLogger().warning("Current Version: v" + getDescription().getVersion());
                        getLogger().warning("Download: " + updateChecker.getResourcePage());

                        return;
                    } else {
                        if (!player.isOp() || !player.hasPermission("voucher-admin")) return;

                        player.sendMessage(methods.color("&8> &cCrazyVouchers has a new update available! New version: &e&n" + updateChecker.getNewVersion()));
                        player.sendMessage(methods.color("&8> &cCurrent Version: &e&n" + getDescription().getVersion()));
                        player.sendMessage(methods.color("&8> &cDownload: &e&n" + updateChecker.getResourcePage()));
                    }

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