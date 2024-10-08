package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.badbones69.crazyvouchers.api.builders.types.VoucherMenu;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.objects.other.Server;
import com.badbones69.crazyvouchers.config.migrate.MigrationService;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.listeners.FireworkDamageListener;
import com.badbones69.crazyvouchers.commands.VoucherCommands;
import com.badbones69.crazyvouchers.commands.VoucherTab;
import com.badbones69.crazyvouchers.listeners.VoucherClickListener;
import com.badbones69.crazyvouchers.listeners.VoucherCraftListener;
import com.badbones69.crazyvouchers.listeners.VoucherMiscListener;
import com.badbones69.crazyvouchers.support.MetricsWrapper;
import com.ryderbelserion.vital.paper.enums.Support;
import com.ryderbelserion.vital.paper.files.config.FileManager;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class CrazyVouchers extends JavaPlugin {

    public @NotNull static CrazyVouchers get() {
        return JavaPlugin.getPlugin(CrazyVouchers.class);
    }

    private final long startTime;

    public CrazyVouchers() {
        this.startTime = System.nanoTime();
    }

    private InventoryManager inventoryManager;

    private CrazyManager crazyManager;
    private FileManager fileManager;

    private HeadDatabaseAPI api;

    @Override
    public void onEnable() {
        final File file = new File(getDataFolder(), "Config.yml");

        boolean isReadyToMigrate = false;

        if (file.exists()) {
            // Load configuration of input.
            YamlConfiguration config = CompletableFuture.supplyAsync(() -> YamlConfiguration.loadConfiguration(file)).join();

            // Get the configuration section.
            ConfigurationSection vouchers = config.getConfigurationSection("Vouchers");

            // If we can't see the section in the config.yml, we do nothing.
            if (vouchers != null) {
                File backupFile = new File(getDataFolder(), "Vouchers-Backup.yml");

                // Rename to back up file.
                file.renameTo(backupFile);

                isReadyToMigrate = true;
            }
        }

        Server server = new Server(this);

        boolean loadOldWay = ConfigManager.getConfig().getProperty(ConfigKeys.mono_file);

        new MigrationService().migrate(loadOldWay, isReadyToMigrate);

        this.fileManager = new FileManager();
        this.fileManager.addFile("users.yml").addFile("data.yml");

        if (loadOldWay) {
            this.fileManager.addFile("voucher-codes.yml").addFile("vouchers.yml");
        } else {
            this.fileManager.addFolder("codes").addFolder("vouchers");
        }

        this.fileManager.init();

        new MetricsWrapper(this, 4536).start();

        if (Support.head_database.isEnabled()) {
            this.api = new HeadDatabaseAPI();
        }

        this.crazyManager = new CrazyManager();
        this.crazyManager.load();

        this.inventoryManager = new InventoryManager();

        final FileConfiguration configuration = Files.users.getConfiguration();

        if (!configuration.contains("Players")) {
            configuration.set("Players.Clear", null);

            Files.users.save();
        }

        final FileConfiguration data = Files.data.getConfiguration();

        if (!data.contains("Used-Vouchers")) {
            data.set("Used-Vouchers.Clear", null);

            Files.data.save();
        }

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new FireworkDamageListener(), this);
        pluginManager.registerEvents(new VoucherClickListener(), this);
        pluginManager.registerEvents(new VoucherCraftListener(), this);
        pluginManager.registerEvents(new VoucherMiscListener(), this);

        pluginManager.registerEvents(new VoucherMenu(), this);

        registerCommand(getCommand("vouchers"), new VoucherTab(), new VoucherCommands());

        if (server.isLogging()) {
            getComponentLogger().info("Done ({})!", String.format(Locale.ROOT, "%.3fs", (double) (System.nanoTime() - this.startTime) / 1.0E9D));
        }
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

    public @NotNull final InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public @NotNull final CrazyManager getCrazyManager() {
        return this.crazyManager;
    }

    public @NotNull final FileManager getFileManager() {
        return this.fileManager;
    }
}