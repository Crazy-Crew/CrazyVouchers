package com.badbones69.crazyvouchers.api.enums;

import com.badbones69.crazyvouchers.CrazyVouchers;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public enum PersistentKeys {

    voucher_item_admin("voucher_item_admin"), // string
    voucher_item("voucher_item"), // string
    voucher_argument("voucher_argument"), // string
    back_button("back_button"), // string
    next_button("next_button"), // string
    no_firework_damage("firework"); // boolean

    private final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final String NamespacedKey;

    PersistentKeys(String NamespacedKey) {
        this.NamespacedKey = NamespacedKey;
    }

    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(this.plugin, this.plugin.getName().toLowerCase() + "_" + this.NamespacedKey);
    }
}