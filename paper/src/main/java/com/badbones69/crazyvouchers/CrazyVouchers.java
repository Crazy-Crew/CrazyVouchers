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
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.files.enums.FileType;
import com.ryderbelserion.fusion.paper.FusionPaper;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class CrazyVouchers extends JavaPlugin {

    public @NonNull static CrazyVouchers get() {
        return JavaPlugin.getPlugin(CrazyVouchers.class);
    }

    private final long startTime;

    public CrazyVouchers() {
        this.startTime = System.nanoTime();
    }

    private InventoryManager inventoryManager;

    private CrazyManager crazyManager;
    private FileManager fileManager;
    private FusionPaper fusion;

    @Override
    public void onEnable() {
        this.fusion = new FusionPaper(this);
        this.fusion.init();

        final Path path = getDataPath();

        ConfigManager.load(getDataFolder());

        final FileSystem system = ConfigManager.getConfig().getProperty(ConfigKeys.file_system);
        
        this.fileManager.addFile(path.resolve("users.yml"), FileType.YAML)
                .addFile(path.resolve("data.yml"), FileType.YAML)
                .addFile(path.resolve("version.json"), FileType.JSON);

        switch (system) {
            case SINGLE -> this.fileManager.addFile(path.resolve("codes.yml"), FileType.YAML)
                .addFile(path.resolve("vouchers.yml"), FileType.YAML);
            case MULTIPLE -> this.fileManager.addFolder(path.resolve("codes"), FileType.YAML)
                    .addFolder(path.resolve("vouchers"), FileType.YAML);
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

        this.fusion.log(Level.info, "Done (%s)", String.format(Locale.ROOT, "%.3fs", (double) (System.nanoTime() - this.startTime) / 1.0E9D));
    }

    public @NonNull final InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public @NonNull final CrazyManager getCrazyManager() {
        return this.crazyManager;
    }

    public @NonNull final FileManager getFileManager() {
        return this.fileManager;
    }

    public @NonNull final FusionPaper getFusion() {
        return this.fusion;
    }
}