package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.badbones69.crazyvouchers.api.builders.types.VoucherMenu;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.listeners.FireworkDamageListener;
import com.badbones69.crazyvouchers.commands.VoucherCommands;
import com.badbones69.crazyvouchers.commands.VoucherTab;
import com.badbones69.crazyvouchers.listeners.VoucherClickListener;
import com.badbones69.crazyvouchers.listeners.VoucherCraftListener;
import com.badbones69.crazyvouchers.listeners.VoucherMiscListener;
import com.badbones69.crazyvouchers.support.MetricsWrapper;
import com.ryderbelserion.fusion.core.api.enums.FileType;
import com.ryderbelserion.fusion.paper.FusionApi;
import com.ryderbelserion.fusion.paper.files.FileManager;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import java.util.Locale;

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

    private HeadDatabaseAPI api;

    private final FusionApi fusionApi = FusionApi.get();

    private FileManager fileManager;

    @Override
    public void onEnable() {
        this.fusionApi.enable(this);

        this.api = this.fusionApi.getDatabaseAPI();

        this.fileManager = this.fusionApi.getFileManager();

        ConfigManager.load(getDataFolder());

        boolean loadOldWay = ConfigManager.getConfig().getProperty(ConfigKeys.mono_file);

        this.fileManager.addFile("users.yml").addFile("data.yml");

        if (loadOldWay) {
            this.fileManager.addFile("voucher-codes.yml").addFile("vouchers.yml");
        } else {
            this.fileManager.addFolder("codes", FileType.YAML).addFolder("vouchers", FileType.YAML);
        }

        this.fileManager.init();

        new MetricsWrapper(4536).start();

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

        final VoucherTab voucherTab = new VoucherTab();
        final VoucherCommands voucherCommands = new VoucherCommands();

        registerCommand(getCommand("vouchers"), voucherTab, voucherCommands);

        registerCommand(getCommand("crazyvouchers"), voucherTab, voucherCommands);

        if (this.fusionApi.getFusion().isVerbose()) {
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

    public @NotNull FileManager getFileManager() {
        return this.fileManager;
    }
}