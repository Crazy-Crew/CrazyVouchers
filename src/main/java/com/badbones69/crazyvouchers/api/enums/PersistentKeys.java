package com.badbones69.crazyvouchers.api.enums;

import com.badbones69.crazyvouchers.CrazyVouchers;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public enum PersistentKeys {

    voucher_item_admin("voucher_item_admin"),
    back_button("back_button"),
    next_button("next_button"),
    no_firework_damage("firework"),
    dupe_protection("dupe_protection");

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private final String NamespacedKey;

    PersistentKeys(String NamespacedKey) {
        this.NamespacedKey = NamespacedKey;
    }

    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(this.plugin, this.plugin.getName().toLowerCase() + "_" + this.NamespacedKey);
    }
}