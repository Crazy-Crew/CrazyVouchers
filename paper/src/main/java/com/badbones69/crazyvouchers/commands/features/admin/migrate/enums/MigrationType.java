package com.badbones69.crazyvouchers.commands.features.admin.migrate.enums;

import org.jetbrains.annotations.NotNull;

public enum MigrationType {

    VOUCHERS_DEPRECATED("VouchersDeprecated"),
    VOUCHERS_NBT_API("VouchersNbtApi"),
    VOUCHERS_SWITCH("VouchersSwitch"),
    NEW_ITEM_FORMAT("NewItemFormat"),
    VOUCHERS_COLOR("VouchersColor"),

    VOUCHERS_RENAME("VouchersRename");

    private final String name;

    MigrationType(@NotNull final String name) {
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }

    public static MigrationType fromName(@NotNull final String name) {
        MigrationType type = null;

        for (final MigrationType key : MigrationType.values()) {
            if (key.getName().equalsIgnoreCase(name)) {
                type = key;

                break;
            }
        }

        return type;
    }
}