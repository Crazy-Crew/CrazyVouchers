package com.badbones69.crazyvouchers.api.enums;

import com.badbones69.crazyvouchers.CrazyVouchers;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
public enum DataKeys {

    NO_FIREWORK_DAMAGE("voucher_firework", PersistentDataType.BOOLEAN),
    VOUCHER_ITEM_ADMIN("voucher_item_admin", PersistentDataType.STRING),
    BACK_BUTTON("voucher_back_button", PersistentDataType.STRING),
    NEXT_BUTTON("voucher_next_button", PersistentDataType.STRING);

    private final @NotNull CrazyVouchers plugin = CrazyVouchers.get();

    private final String NamespacedKey;
    private final PersistentDataType type;

    DataKeys(String NamespacedKey, PersistentDataType type) {
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