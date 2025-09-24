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
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.files.PaperFileManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import java.nio.file.Path;
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
    private PaperFileManager fileManager;

    @Override
    public void onEnable() {
        this.fusion = new FusionPaper(this);
        this.fusion.init();

        this.fileManager = this.fusion.getFileManager();

        final Path path = getDataPath();

        ConfigManager.load(getDataFolder());

        final FileSystem system = ConfigManager.getConfig().getProperty(ConfigKeys.file_system);

        this.fileManager.addPaperFile(path.resolve("users.yml"))
                .addPaperFile(path.resolve("data.yml"));

        switch (system) {
            case SINGLE -> this.fileManager.addPaperFile(path.resolve("codes.yml"))
                    .addPaperFile(path.resolve("vouchers.yml"));
            case MULTIPLE -> this.fileManager.addPaperFolder(path.resolve("codes"))
                    .addPaperFolder(path.resolve("vouchers"));
        }

        new MetricsWrapper(4536).start();

        this.crazyManager = new CrazyManager();
        this.crazyManager.load(false);

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

        this.fusion.log("info", "Done ({})", String.format(Locale.ROOT, "%.3fs", (double) (System.nanoTime() - this.startTime) / 1.0E9D));
    }

    public @NotNull final InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public @NotNull final CrazyManager getCrazyManager() {
        return this.crazyManager;
    }

    public @NotNull final PaperFileManager getFileManager() {
        return this.fileManager;
    }

    public @NotNull final FusionPaper getFusion() {
        return this.fusion;
    }
}