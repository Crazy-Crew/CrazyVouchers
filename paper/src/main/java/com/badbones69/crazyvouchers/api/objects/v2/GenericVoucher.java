package com.badbones69.crazyvouchers.api.objects.v2;

import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import com.badbones69.crazyvouchers.platform.config.types.ConfigKeys;
import com.badbones69.crazyvouchers.platform.util.MiscUtil;
import com.badbones69.crazyvouchers.platform.util.MsgUtil;
import com.ryderbelserion.vital.enums.Support;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GenericVoucher extends AbstractVoucher {

    private final Map<String, String> placeholders = new HashMap<>();

    public GenericVoucher(ConfigurationSection section, String file) {
        super(section, file);
    }

    @Override
    public boolean execute(Player player) {
        // Return if in creative mode.
        if (player.getGameMode() == GameMode.CREATIVE && ConfigManager.getConfig().getProperty(ConfigKeys.must_be_in_survival)) {
            Messages.survival_mode.sendMessage(player);

            return true;
        }

        this.placeholders.put("{player}", player.getName());
        this.placeholders.put("{world}", player.getWorld().getName());

        Location location = player.getLocation();
        this.placeholders.put("{x}", String.valueOf(location.getBlockX()));
        this.placeholders.put("{y}", String.valueOf(location.getBlockY()));
        this.placeholders.put("{z}", String.valueOf(location.getBlockZ()));

        //this.placeholders.put("{prefix}", MsgUtil.getPrefix());

        if (this.whitelistWorldToggle && !this.whitelistWorlds.contains(player.getWorld().getName().toLowerCase())) {
            player.sendRichMessage(MiscUtil.replacePlaceholders(this.placeholders, this.whitelistWorldMessage, false));

            this.whitelistWorldCommands.forEach(command -> this.server.dispatchCommand(this.server.getConsoleSender(), MiscUtil.replacePlaceholders(this.placeholders, command, true)));

            setCancelled(true);
        }

        if (this.whitelistPermissionToggle && !isCancelled()) {
            this.whitelistPermissions.forEach(permission -> {
                // If they have one of the permissions, return in loop.
                if (player.hasPermission(permission)) {
                    return;
                }

                this.placeholders.put("{permission}", permission);

                player.sendRichMessage(MiscUtil.replacePlaceholders(this.placeholders, this.whitelistPermissionMessage, false));

                this.whitelistCommands.forEach(command -> this.server.dispatchCommand(this.server.getConsoleSender(), MiscUtil.replacePlaceholders(this.placeholders, command, true)));

                setCancelled(true);
            });
        }

        if (this.blacklistPermissionToggle && !isCancelled()) {
            this.blacklistPermissions.forEach(permission -> {
                // If they don't have the permission, return in loop.
                if (!player.hasPermission(permission)) {
                    return;
                }

                player.sendRichMessage(MiscUtil.replacePlaceholders(this.placeholders, this.blacklistPermissionMessage, false));

                this.placeholders.put("{permission}", permission);

                this.blacklistCommands.forEach(command -> this.server.dispatchCommand(this.server.getConsoleSender(), MiscUtil.replacePlaceholders(this.placeholders, command, true)));

                setCancelled(true);
            });
        }

        if (Support.placeholder_api.isEnabled() && !isCancelled()) {
            this.requiredPlaceholders.forEach((placeholder, value) -> {
                String newValue = PlaceholderAPI.setPlaceholders(player, placeholder);

                if (!newValue.equals(value)) {
                    String msg = replacePlaceholders(this.requiredPlaceholderMessage, player);

                    player.sendRichMessage(MiscUtil.replacePlaceholders(this.placeholders, msg, false));

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

    public ItemStack getItem(Player player) {
        return this.builder.setTarget(player).build();
    }

    private String replacePlaceholders(String string, Player player) {
        if (Support.placeholder_api.isEnabled()) return PlaceholderAPI.setPlaceholders(player, string);

        return MsgUtil.color(string);
    }
}