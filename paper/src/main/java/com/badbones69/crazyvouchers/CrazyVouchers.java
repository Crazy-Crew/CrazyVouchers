package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.FileManager;
import com.badbones69.crazyvouchers.listeners.FireworkDamageListener;
import com.badbones69.crazyvouchers.listeners.VoucherMenuListener;
import com.badbones69.crazyvouchers.api.FileManager.Files;
import com.badbones69.crazyvouchers.commands.VoucherCommands;
import com.badbones69.crazyvouchers.commands.VoucherTab;
import com.badbones69.crazyvouchers.listeners.VoucherClickListener;
import com.badbones69.crazyvouchers.listeners.VoucherCraftListener;
import com.badbones69.crazyvouchers.support.SkullCreator;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyvouchers.common.config.types.Config;
import us.crazycrew.crazyvouchers.api.plugin.CrazyHandler;

public class CrazyVouchers extends JavaPlugin {

    public static CrazyVouchers get() {
        return JavaPlugin.getPlugin(CrazyVouchers.class);
    }

    private CrazyHandler crazyHandler;

    private CrazyManager crazyManager;

    private Methods methods;

    private SkullCreator skullCreator;

    private VoucherMenuListener voucherMenuListener;

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

        pluginManager.registerEvents(new VoucherClickListener(), this);
        pluginManager.registerEvents(new VoucherCraftListener(), this);
        pluginManager.registerEvents(this.voucherMenuListener = new VoucherMenuListener(), this);
        pluginManager.registerEvents(new FireworkDamageListener(), this);

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

    public VoucherMenuListener getGui() {
        return this.voucherMenuListener;
    }
}