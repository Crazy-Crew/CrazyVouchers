package com.badbones69.crazyvouchers.support;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CustomMetrics;

public class MetricsWrapper extends CustomMetrics {

    /**
     * Creates a new Metrics instance.
     *
     * @param serviceId The id of the service. It can be found at <a href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
     */
    public MetricsWrapper(CrazyVouchers plugin, int serviceId) {
        super(plugin, serviceId);
    }
}