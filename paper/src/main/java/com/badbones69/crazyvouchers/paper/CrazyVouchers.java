package com.badbones69.crazyvouchers.paper;

import com.badbones69.crazyvouchers.paper.api.enums.Messages;
import com.badbones69.crazyvouchers.paper.controllers.GUI;
import com.badbones69.crazyvouchers.paper.api.FileManager;
import com.badbones69.crazyvouchers.paper.api.FileManager.Files;
import com.badbones69.crazyvouchers.paper.api.CrazyManager;
import com.badbones69.crazyvouchers.paper.commands.VoucherCommands;
import com.badbones69.crazyvouchers.paper.commands.VoucherTab;
import com.badbones69.crazyvouchers.paper.controllers.FireworkDamageAPI;
import com.badbones69.crazyvouchers.paper.controllers.VoucherClick;
import com.badbones69.crazyvouchers.paper.listeners.VoucherCraftListener;
import com.badbones69.crazyvouchers.paper.support.MetricsHandler;
import com.badbones69.crazyvouchers.paper.support.SkullCreator;
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

    private SkullCreator skullCreator;

    private GUI gui;

    @Override
    public void onEnable() {
        plugin = this;

        fileManager = new FileManager();

        crazyManager = new CrazyManager();

        methods = new Methods();

        skullCreator = new SkullCreator();

        fileManager.logInfo(true).setup();

        if (!Files.DATA.getFile().contains("Players")) {
            Files.DATA.getFile().set("Players.Clear", null);
            Files.DATA.saveFile();
        }

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new VoucherClick(), this);
        pluginManager.registerEvents(new VoucherCraftListener(), this);
        pluginManager.registerEvents(gui = new GUI(), this);
        pluginManager.registerEvents(fireworkDamageAPI = new FireworkDamageAPI(), this);

        registerCommand(getCommand("vouchers"), new VoucherTab(), new VoucherCommands());

        Messages.addMissingMessages();

        FileConfiguration config = Files.CONFIG.getFile();

        boolean metricsEnabled = Files.CONFIG.getFile().getBoolean("Settings.Toggle-Metrics");
        String metricsPath = Files.CONFIG.getFile().getString("Settings.Toggle-Metrics");

        String useVouchers = Files.CONFIG.getFile().getString("Settings.Prevent-Using-Vouchers-In-Recipes");

        String path = Files.CONFIG.getFile().getString("Settings.Must-Be-In-Survival");

        if (useVouchers == null) {
            config.set("Settings.Prevent-Using-Vouchers-In-Recipes.Toggle", true);
            config.set("Settings.Prevent-Using-Vouchers-In-Recipes.Alert", false);

            Files.CONFIG.saveFile();
        }

        if (path == null) {
            config.set("Settings.Must-Be-In-Survival", true);

            Files.CONFIG.saveFile();
        }

        if (metricsPath == null) {
            config.set("Settings.Toggle-Metrics", false);

            Files.CONFIG.saveFile();
        }

        if (metricsEnabled) {
            MetricsHandler metricsHandler = new MetricsHandler();

            metricsHandler.start();
        }

        crazyManager.load();
    }

    private void registerCommand(PluginCommand pluginCommand, TabCompleter tabCompleter, CommandExecutor commandExecutor) {
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);

            if (tabCompleter != null) pluginCommand.setTabCompleter(tabCompleter);
        }
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

    public SkullCreator getSkullCreator() {
        return skullCreator;
    }

    public FireworkDamageAPI getFireworkDamageAPI() {
        return fireworkDamageAPI;
    }

    public GUI getGui() {
        return gui;
    }
}