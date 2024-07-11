package com.badbones69.crazyvouchers.support;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.vital.paper.bStats;

public class MetricsWrapper extends bStats {

    /**
     * Creates a new Metrics instance.
     *
     * @param serviceId The id of the service. It can be found at <a href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
     */
    public MetricsWrapper(CrazyVouchers plugin, int serviceId) {
        super(plugin, serviceId);
    }

    public void start() {
        // If it's not enabled, we do nothing!
        if (!isEnabled()) return;

        addCustomChart(new SimplePie("old_file_system", () -> String.valueOf(ConfigManager.getConfig().getProperty(ConfigKeys.mono_file).booleanValue())));
    }
}