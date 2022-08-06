package com.badbones69.vouchers.api.enums;

import com.badbones69.vouchers.api.CrazyManager;

public enum Support {
    
    PLACEHOLDERAPI("PlaceholderAPI");
    
    private final String name;

    private final CrazyManager crazyManager = CrazyManager.getInstance();
    
    Support(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isPluginLoaded() {
        return crazyManager.getPlugin().getServer().getPluginManager().getPlugin(name) != null;
    }
}