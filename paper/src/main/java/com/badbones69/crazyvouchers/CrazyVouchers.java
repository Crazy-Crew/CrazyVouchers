package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.badbones69.crazyvouchers.api.builders.types.VoucherMenu;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.commands.features.CommandHandler;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.listeners.FireworkDamageListener;
import com.badbones69.crazyvouchers.listeners.VoucherClickListener;
import com.badbones69.crazyvouchers.listeners.VoucherCraftListener;
import com.badbones69.crazyvouchers.listeners.VoucherMiscListener;
import com.badbones69.crazyvouchers.support.MetricsWrapper;
import com.ryderbelserion.fusion.core.managers.files.FileType;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.files.LegacyFileManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import java.util.List;
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

    private FusionPaper api;
    private LegacyFileManager fileManager;

    @Override
    public void onEnable() {
        this.api = new FusionPaper(getComponentLogger(), getDataPath());
        this.api.enable(this);

        this.fileManager = this.api.getLegacyFileManager();

        ConfigManager.load(getDataFolder());

        final FileSystem system = ConfigManager.getConfig().getProperty(ConfigKeys.file_system);

        this.fileManager.addFile("users.yml", FileType.YAML).addFile("data.yml", FileType.YAML);

        switch (system) {
            case MULTIPLE -> this.fileManager.addFolder("codes", FileType.YAML).addFolder("vouchers", FileType.YAML);
            case SINGLE -> this.fileManager.addFile("codes.yml", FileType.YAML).addFile("vouchers.yml", FileType.YAML);
        }

        new MetricsWrapper(4536).start();

        this.crazyManager = new CrazyManager();
        this.crazyManager.load();

        this.inventoryManager = new InventoryManager();

        Methods.janitor();

        PluginManager pluginManager = getServer().getPluginManager();

        new CommandHandler();

        List.of(
                new FireworkDamageListener(),
                new VoucherClickListener(),
                new VoucherCraftListener(),
                new VoucherMiscListener(),
                new VoucherMenu()
        ).forEach(event -> pluginManager.registerEvents(event, this));

        if (getFusion().isVerbose()) {
            getComponentLogger().info("Done ({})!", String.format(Locale.ROOT, "%.3fs", (double) (System.nanoTime() - this.startTime) / 1.0E9D));
        }
    }

    public final InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public final CrazyManager getCrazyManager() {
        return this.crazyManager;
    }

    public LegacyFileManager getFileManager() {
        return this.fileManager;
    }

    public final FusionPaper getFusion() {
        return this.api;
    }
}