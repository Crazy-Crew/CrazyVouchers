package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.FileManager;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.badbones69.crazyvouchers.api.builders.types.VoucherMenu;
import com.badbones69.crazyvouchers.listeners.FireworkDamageListener;
import com.badbones69.crazyvouchers.api.FileManager.Files;
import com.badbones69.crazyvouchers.commands.VoucherCommands;
import com.badbones69.crazyvouchers.commands.VoucherTab;
import com.badbones69.crazyvouchers.listeners.VoucherClickListener;
import com.badbones69.crazyvouchers.listeners.VoucherCraftListener;
import com.badbones69.crazyvouchers.listeners.VoucherMiscListener;
import com.badbones69.crazyvouchers.support.MetricsWrapper;
import com.ryderbelserion.vital.paper.VitalPaper;
import com.ryderbelserion.vital.paper.enums.Support;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.crazycrew.crazyvouchers.common.config.types.ConfigKeys;
import com.badbones69.crazyvouchers.api.plugin.CrazyHandler;

public class CrazyVouchers extends JavaPlugin {

    @NotNull
    public static CrazyVouchers get() {
        return JavaPlugin.getPlugin(CrazyVouchers.class);
    }

    private InventoryManager inventoryManager;

    private CrazyHandler crazyHandler;

    private CrazyManager crazyManager;

    private HeadDatabaseAPI api;

    private Methods methods;

    @Override
    public void onEnable() {
        new VitalPaper(this).setLogging(false);

        new MetricsWrapper(this, 4536);

        if (Support.head_database.isEnabled()) {
            this.api = new HeadDatabaseAPI();
        }

        this.crazyHandler = new CrazyHandler(getDataFolder());
        this.crazyHandler.install();

        this.crazyManager = new CrazyManager();
        this.crazyManager.load();

        this.inventoryManager = new InventoryManager();

        this.methods = new Methods();

        final FileConfiguration configuration = Files.users.getFile();

        if (!configuration.contains("Players")) {
            configuration.set("Players.Clear", null);

            Files.users.saveFile();
        }

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new FireworkDamageListener(), this);
        pluginManager.registerEvents(new VoucherClickListener(), this);
        pluginManager.registerEvents(new VoucherCraftListener(), this);
        pluginManager.registerEvents(new VoucherMiscListener(), this);

        pluginManager.registerEvents(new VoucherMenu(), this);

        registerCommand(getCommand("vouchers"), new VoucherTab(), new VoucherCommands());
    }

    @Override
    public void onDisable() {
        this.crazyHandler.uninstall();
    }

    private void registerCommand(PluginCommand pluginCommand, TabCompleter tabCompleter, CommandExecutor commandExecutor) {
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);

            if (tabCompleter != null) pluginCommand.setTabCompleter(tabCompleter);
        }
    }

    public @Nullable final HeadDatabaseAPI getApi() {
        if (this.api == null) {
            return null;
        }

        return this.api;
    }

    public final CrazyHandler getCrazyHandler() {
        return this.crazyHandler;
    }

    public final FileManager getFileManager() {
        return this.crazyHandler.getFileManager();
    }

    public final boolean isLogging() {
        return this.crazyHandler.getConfigManager().getConfig().getProperty(ConfigKeys.verbose_logging);
    }

    public final InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public final CrazyManager getCrazyManager() {
        return this.crazyManager;
    }

    public final Methods getMethods() {
        return this.methods;
    }
}