package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.CrazyPlatform;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.badbones69.crazyvouchers.api.builders.types.VoucherMenu;
import com.badbones69.crazyvouchers.commands.features.CommandHandler;
import com.badbones69.crazyvouchers.listeners.FireworkDamageListener;
import com.badbones69.crazyvouchers.listeners.VoucherClickListener;
import com.badbones69.crazyvouchers.listeners.VoucherCraftListener;
import com.badbones69.crazyvouchers.listeners.VoucherMiscListener;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.paper.FusionPaper;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
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
    private CrazyPlatform platform;
    private FusionPaper fusion;

    @Override
    public void onEnable() {
        this.fusion = new FusionPaper(this);
        this.fusion.init();

        this.platform = new CrazyPlatform(this.fusion, getDataPath());
        this.platform.init();

        this.inventoryManager = new InventoryManager();

        Methods.janitor();

        final PluginManager pluginManager = getServer().getPluginManager();

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

    public @NonNull final CrazyPlatform getPlatform() {
        return this.platform;
    }

    public @NonNull final FusionPaper getFusion() {
        return this.fusion;
    }
}