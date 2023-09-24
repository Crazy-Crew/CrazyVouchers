package com.badbones69.crazyvouchers.paper.api.enums;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public enum DataKeys {

    NO_FIREWORK_DAMAGE("voucher_firework", Boolean.class);

    private final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final String nameSpaceKey;

    DataKeys(String nameSpaceKey, Object dataType) {
        this.nameSpaceKey = nameSpaceKey;
    }

    public NamespacedKey getKey() {
        return new NamespacedKey(this.plugin, this.nameSpaceKey);
    }

    public String getStringKey() {
        return this.nameSpaceKey;
    }
}