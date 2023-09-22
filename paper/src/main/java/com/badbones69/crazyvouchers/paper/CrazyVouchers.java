package com.badbones69.crazyvouchers.paper;

import com.badbones69.crazyvouchers.paper.api.FileManager;
import com.badbones69.crazyvouchers.paper.controllers.GUI;
import com.badbones69.crazyvouchers.paper.api.FileManager.Files;
import com.badbones69.crazyvouchers.paper.api.CrazyManager;
import com.badbones69.crazyvouchers.paper.commands.VoucherCommands;
import com.badbones69.crazyvouchers.paper.commands.VoucherTab;
import com.badbones69.crazyvouchers.paper.controllers.FireworkDamageAPI;
import com.badbones69.crazyvouchers.paper.controllers.VoucherClick;
import com.badbones69.crazyvouchers.paper.listeners.VoucherCraftListener;
import com.badbones69.crazyvouchers.paper.support.SkullCreator;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyenvoys.common.config.types.Config;
import us.crazycrew.crazyvouchers.paper.api.plugin.CrazyHandler;

public class CrazyVouchers extends JavaPlugin {

    private CrazyHandler crazyHandler;

    private CrazyManager crazyManager;

    private Methods methods;

    private FireworkDamageAPI fireworkDamageAPI;

    private SkullCreator skullCreator;

    private GUI gui;

    @Override
    public void onEnable() {
        this.crazyHandler = new CrazyHandler(getDataFolder());
        this.crazyHandler.install();

        enable();
    }

    @Override
    public void onDisable() {
        this.crazyHandler.uninstall();
    }

    public @NotNull CrazyHandler getCrazyHandler() {
        return this.crazyHandler;
    }

    public @NotNull FileManager getFileManager() {
        return this.crazyHandler.getFileManager();
    }

    public boolean isLogging() {
        return this.crazyHandler.getConfigManager().getConfig().getProperty(Config.verbose_logging);
    }

    private void enable() {
        this.crazyManager = new CrazyManager();

        this.methods = new Methods();

        this.skullCreator = new SkullCreator();

        if (!Files.users.getFile().contains("Players")) {
            Files.users.getFile().set("Players.Clear", null);
            Files.users.saveFile();
        }

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new VoucherClick(), this);
        pluginManager.registerEvents(new VoucherCraftListener(), this);
        pluginManager.registerEvents(this.gui = new GUI(), this);
        pluginManager.registerEvents(this.fireworkDamageAPI = new FireworkDamageAPI(), this);

        registerCommand(getCommand("vouchers"), new VoucherTab(), new VoucherCommands());

        this.crazyManager.load(true);
    }

    private void registerCommand(PluginCommand pluginCommand, TabCompleter tabCompleter, CommandExecutor commandExecutor) {
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);

            if (tabCompleter != null) pluginCommand.setTabCompleter(tabCompleter);
        }
    }

    public CrazyManager getCrazyManager() {
        return this.crazyManager;
    }

    public Methods getMethods() {
        return this.methods;
    }

    public SkullCreator getSkullCreator() {
        return this.skullCreator;
    }

    public FireworkDamageAPI getFireworkDamageAPI() {
        return this.fireworkDamageAPI;
    }

    public GUI getGui() {
        return this.gui;
    }
}