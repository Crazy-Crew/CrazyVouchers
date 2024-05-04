package com.badbones69.crazyvouchers.api.objects.v2;

import com.badbones69.crazyvouchers.api.builders.ItemBuilder;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.platform.config.types.ConfigKeys;
import com.badbones69.crazyvouchers.platform.util.MiscUtil;
import com.ryderbelserion.vital.enums.Support;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GenericVoucher extends AbstractVoucher {

    private final Map<String, String> placeholders = new HashMap<>();

    public Map<String, String> getPlaceholders() {
        return Collections.unmodifiableMap(this.placeholders);
    }

    public GenericVoucher(@NotNull final ConfigurationSection section, @NotNull final String file) {
        super(section, file);
    }

    public boolean isEdible() {
        return this.isEdible;
    }

    public boolean twoStep() {
        return this.twoStep;
    }

    @Override
    public final boolean execute(@NotNull final Player player, @Nullable final String argument) {
        // Return if in creative mode.
        if (player.getGameMode() == GameMode.CREATIVE && this.config.getProperty(ConfigKeys.must_be_in_survival)) {
            Messages.survival_mode.sendMessage(player);

            return true;
        }

        String world = player.getWorld().getName();

        this.placeholders.put("{arg}", argument != null ? argument : "{arg}");
        this.placeholders.put("{player}", player.getName());
        this.placeholders.put("{world}", world);

        Location location = player.getLocation();

        this.placeholders.put("{x}", String.valueOf(location.getBlockX()));
        this.placeholders.put("{y}", String.valueOf(location.getBlockY()));
        this.placeholders.put("{z}", String.valueOf(location.getBlockZ()));
        this.placeholders.put("{prefix}", this.config.getProperty(ConfigKeys.command_prefix));

        if (this.whitelistWorldToggle && !this.whitelistWorlds.contains(world.toLowerCase())) {
            player.sendRichMessage(MiscUtil.replacePlaceholders(this.placeholders, this.whitelistWorldMessage));

            this.whitelistWorldCommands.forEach(command -> this.server.dispatchCommand(this.server.getConsoleSender(), MiscUtil.replacePlaceholders(this.placeholders, command)));

            setCancelled(true);
        }

        if (this.whitelistPermissionToggle && !isCancelled()) {
            this.whitelistPermissions.forEach(permission -> {
                // If they have one of the permissions, return in loop.
                if (player.hasPermission(permission.toLowerCase().replace("{arg}", argument != null ? argument : "{arg}"))) {
                    return;
                }

                this.placeholders.put("{permission}", permission);

                player.sendRichMessage(MiscUtil.replacePlaceholders(this.placeholders, this.whitelistPermissionMessage));

                this.whitelistCommands.forEach(command -> this.server.dispatchCommand(this.server.getConsoleSender(), MiscUtil.replacePlaceholders(this.placeholders, command)));

                setCancelled(true);
            });
        }

        if (this.blacklistPermissionToggle && !isCancelled()) {
            this.blacklistPermissions.forEach(permission -> {
                // If they don't have the permission, return in loop.
                if (!player.hasPermission(permission.toLowerCase().replace("{arg}", argument != null ? argument : "{arg}"))) {
                    return;
                }

                player.sendRichMessage(MiscUtil.replacePlaceholders(this.placeholders, this.blacklistPermissionMessage));

                this.placeholders.put("{permission}", permission);

                this.blacklistCommands.forEach(command -> this.server.dispatchCommand(this.server.getConsoleSender(), MiscUtil.replacePlaceholders(this.placeholders, command)));

                setCancelled(true);
            });
        }

        if (Support.placeholder_api.isEnabled() && !isCancelled()) {
            this.requiredPlaceholders.forEach((placeholder, value) -> {
                String newValue = PlaceholderAPI.setPlaceholders(player, placeholder);

                if (!newValue.equals(value)) {
                    String msg = replacePlaceholders(this.requiredPlaceholderMessage, player);

                    player.sendRichMessage(MiscUtil.replacePlaceholders(this.placeholders, msg));

                    setCancelled(true);
                }
            });
        }

        if (!isCancelled()) {
            UUID uuid = player.getUniqueId();
            String asString = uuid.toString();

            FileConfiguration configuration = Files.users.getFile();

            if (!player.hasPermission("voucher.bypass") && this.limiterToggle && configuration.contains("Players." + asString + ".Vouchers." + getFileName()) && !isCancelled()) {
                int amount = configuration.getInt("Players." + asString + ".Vouchers." + getFileName());

                if (amount >= this.limiterAmount) {
                    Messages.hit_voucher_limit.sendMessage(player);

                    setCancelled(true);
                }
            }
        }

        // if it returns true, checks above have failed.
        return isCancelled();
    }

    public @NotNull final ItemBuilder getItem(@NotNull final Player player, @Nullable final String argument, final int amount) {
        if (argument != null) {
            this.builder.setString(PersistentKeys.voucher_argument.getNamespacedKey(), argument.replace("%random%", "{random}"));
        }

        return this.builder.setAmount(amount).setTarget(player);
    }

    public @NotNull final ItemBuilder getItem(@NotNull final Player player, final int amount) {
        return getItem(player, null, amount);
    }

    public @NotNull final ItemStack getItem(@NotNull final Player player) {
        return getItem(player, null, 1).build();
    }

    public @NotNull final String replacePlaceholders(@NotNull final String string, @NotNull final Player player) {
        if (Support.placeholder_api.isEnabled()) return PlaceholderAPI.setPlaceholders(player, string);

        return string;
    }

    // Not needed in this class
    @Override
    public boolean execute(@NotNull final Player player) { return false; }
}