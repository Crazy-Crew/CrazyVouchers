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
import com.ryderbelserion.fusion.core.api.enums.FileType;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.files.FileManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import java.nio.file.Path;
import java.util.ArrayList;
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

    private FusionPaper fusion;
    private FileManager fileManager;

    @Override
    public void onEnable() {
        this.fusion = new FusionPaper(getComponentLogger(), getDataPath());
        this.fusion.enable(this);

        this.fileManager = this.fusion.getFileManager();

        final Path path = getDataPath();

        ConfigManager.load(getDataFolder());

        final FileSystem system = ConfigManager.getConfig().getProperty(ConfigKeys.file_system);

        this.fileManager.addFile(path.resolve("users.yml"), FileType.PAPER, new ArrayList<>(), null)
                .addFile(path.resolve("data.yml"), FileType.PAPER, new ArrayList<>(), null);

        switch (system) {
            case SINGLE -> this.fileManager.addFile(path.resolve("codes.yml"), FileType.PAPER, new ArrayList<>(), null)
                    .addFile(path.resolve("vouchers.yml"), FileType.PAPER, new ArrayList<>(), null);
            case MULTIPLE -> this.fileManager.addFolder(path.resolve("codes"), FileType.PAPER, new ArrayList<>(), null)
                    .addFolder(path.resolve("vouchers"), FileType.PAPER, new ArrayList<>(), null);
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

    public FileManager getFileManager() {
        return this.fileManager;
    }

    public final FusionPaper getFusion() {
        return this.fusion;
    }
}