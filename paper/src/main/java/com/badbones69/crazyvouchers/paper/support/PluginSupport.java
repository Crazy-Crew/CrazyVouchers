package com.badbones69.crazyvouchers.paper.support;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;

public enum PluginSupport {

    ORAXEN("Oraxen"),
    ITEMS_ADDER("ItemsAdder");

    private final String name;

    private static final CrazyVouchers plugin = CrazyVouchers.getPlugin();

    PluginSupport(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isPluginEnabled() {
        return plugin.getServer().getPluginManager().isPluginEnabled(name);
    }
}