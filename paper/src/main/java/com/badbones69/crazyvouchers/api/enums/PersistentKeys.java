package com.badbones69.crazyvouchers.api.enums;

import com.badbones69.crazyvouchers.CrazyVouchers;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public enum PersistentKeys {

    voucher_item_admin("voucher_item_admin", PersistentDataType.STRING),
    back_button("back_button", PersistentDataType.STRING),
    next_button("next_button", PersistentDataType.STRING),
    no_firework_damage("firework", PersistentDataType.BOOLEAN);

    private final @NotNull CrazyVouchers plugin = CrazyVouchers.get();

    private final String NamespacedKey;
    private final PersistentDataType type;

    PersistentKeys(String NamespacedKey, PersistentDataType type) {
        this.NamespacedKey = NamespacedKey;
        this.type = type;
    }

    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(this.plugin, this.plugin.getName().toLowerCase() + "_" + this.NamespacedKey);
    }

    public PersistentDataType getType() {
        return this.type;
    }
}