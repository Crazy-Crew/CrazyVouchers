package com.badbones69.crazyvouchers.support;

import com.badbones69.crazyvouchers.CrazyVouchers;
import org.bukkit.plugin.java.JavaPlugin;

public enum PluginSupport {

    ORAXEN("Oraxen"),
    ITEMS_ADDER("ItemsAdder"),
    PLACEHOLDERAPI("PlaceholderAPI");

    private final String name;

    private final CrazyVouchers plugin = CrazyVouchers.get();

    PluginSupport(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean isPluginEnabled() {
        return this.plugin.getServer().getPluginManager().isPluginEnabled(this.name);
    }
}