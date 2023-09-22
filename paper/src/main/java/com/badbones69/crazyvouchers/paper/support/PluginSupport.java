package com.badbones69.crazyvouchers.paper.support;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import org.bukkit.plugin.java.JavaPlugin;

public enum PluginSupport {

    ORAXEN("Oraxen"),
    ITEMS_ADDER("ItemsAdder"),
    PLACEHOLDERAPI("PlaceholderAPI");

    private final String name;

    private final CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

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