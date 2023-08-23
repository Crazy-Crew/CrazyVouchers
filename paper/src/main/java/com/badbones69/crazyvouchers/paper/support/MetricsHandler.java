package com.badbones69.crazyvouchers.paper.support;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import org.bstats.bukkit.Metrics;

public class MetricsHandler {

    private final CrazyVouchers plugin = CrazyVouchers.getPlugin();

    public void start() {
        new Metrics(plugin, 4536);

        plugin.getLogger().info("Metrics has been enabled.");
    }
}