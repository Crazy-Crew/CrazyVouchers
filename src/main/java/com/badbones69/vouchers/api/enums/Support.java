package com.badbones69.vouchers.api.enums;

import com.badbones69.vouchers.Vouchers;

public enum Support {
    
    PLACEHOLDERAPI("PlaceholderAPI");
    
    private final String name;

    private final Vouchers plugin = Vouchers.getPlugin();
    
    Support(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isPluginLoaded() {
        return plugin.getServer().getPluginManager().getPlugin(name) != null;
    }
}