package com.badbones69.crazyvouchers.support;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetricsWrapper {

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();
    private @Nullable final Metrics metrics;

    public MetricsWrapper(final int serviceId) {
        this.metrics = new Metrics(this.plugin, serviceId);
    }

    private final SettingsManager config = ConfigManager.getConfig();

    public void start() {
        // If it's not enabled, we do nothing!
        if (this.metrics == null || !this.config.getProperty(ConfigKeys.toggle_metrics)) return;

        this.metrics.addCustomChart(new SimplePie("use_dupe_protection", () -> String.valueOf(this.config.getProperty(ConfigKeys.dupe_protection).booleanValue())));
        this.metrics.addCustomChart(new SimplePie("old_file_system", () -> {
            final FileSystem system = this.config.getProperty(ConfigKeys.file_system);

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