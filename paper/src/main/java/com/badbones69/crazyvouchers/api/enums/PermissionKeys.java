package com.badbones69.crazyvouchers.api.enums;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jspecify.annotations.NonNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum PermissionKeys {

    crazyvouchers_admin("admin", "Allows access to miscellaneous admin privileges that might not be specific to need their own permission.", PermissionDefault.OP),
    crazyvouchers_redeem("redeem", "Allows you to use the /crazyvouchers redeem command", PermissionDefault.OP),
    crazyvouchers_notify("notify.duped", "Notifies you if a duped voucher is used", PermissionDefault.OP),
    crazyvouchers_migrate("migrate", "Gives access to /crazyvouchers migrate", PermissionDefault.OP),
    crazyvouchers_bypass("bypass", "Allows you to bypass restrictions", PermissionDefault.OP),

    crazyvouchers_bypass_2fa("bypass.2fa", "Allows you to bypass 2fa when clicking a voucher", PermissionDefault.OP),

    crazyvouchers_give("give", "Access to /crazyvouchers give", PermissionDefault.OP),
    crazyvouchers_all("giveall", "Access to /crazyvouchers giveall", PermissionDefault.OP),
    crazyvouchers_open("open", "Access to /crazyvouchers open", PermissionDefault.OP),
    crazyvouchers_types("types", "Access to /crazyvouchers types", PermissionDefault.OP),
    crazyvouchers_access("access", "Access to /crazyvouchers", PermissionDefault.OP),
    crazyvouchers_help("help", "Access to /crazyvouchers help", PermissionDefault.OP),
    crazyvouchers_reload("reload", "Access to /crazyvouchers reload", PermissionDefault.OP);

    private final String node;
    private final String description;
    private final PermissionDefault isDefault;
    private final Map<String, Boolean> children;

    PermissionKeys(@NonNull final String node, @NonNull final String description, @NonNull final PermissionDefault isDefault, @NonNull final HashMap<String, Boolean> children) {
        this.node = node;
        this.description = description;

        this.isDefault = isDefault;

        this.children = children;
    }

    PermissionKeys(@NonNull final String node, @NonNull final String description, @NonNull final PermissionDefault isDefault) {
        this.node = node;
        this.description = description;

        this.isDefault = isDefault;
        this.children = new HashMap<>();
    }

    public @NonNull final String getPermission() {
        return "crazyvouchers." + this.node;
    }

    public @NonNull final String getDescription() {
        return this.description;
    }

    public @NonNull final PermissionDefault isDefault() {
        return this.isDefault;
    }

    public @NonNull final Map<String, Boolean> getChildren() {
        return Collections.unmodifiableMap(this.children);
    }

    public final boolean hasPermission(@NonNull final Player player) {
        return player.hasPermission(getPermission());
    }
}