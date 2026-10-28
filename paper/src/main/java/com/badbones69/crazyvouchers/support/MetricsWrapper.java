package com.badbones69.crazyvouchers.support;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyPlatform;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.other.FileSystem;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.Nullable;

public class MetricsWrapper {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final CrazyPlatform platform = this.plugin.getPlatform();

    private final ConfigManager configManager = this.platform.getConfigManager();

    private final PropertyManager config = this.configManager.getConfig();

    private @Nullable final Metrics metrics;

    public MetricsWrapper(final int serviceId) {
        this.metrics = new Metrics(this.plugin, serviceId);
    }

    public void start() {
        // If it's not enabled, we do nothing!
        if (this.metrics == null || !this.config.getProperty(RootKeys.toggle_metrics)) return;

        this.metrics.addCustomChart(new SimplePie("use_dupe_protection", () -> String.valueOf(this.config.getProperty(RootKeys.dupe_protection).booleanValue())));
        this.metrics.addCustomChart(new SimplePie("old_file_system", () -> {
            final FileSystem system = this.config.getProperty(RootKeys.file_system);

            switch (system) {
                case SINGLE -> {
                    return "true";
                }

                case MULTIPLE -> {
                    return "false";
                }

                default -> {
                    return "unknown";
                }
            }
        }));
    }
}