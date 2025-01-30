package com.badbones69.crazyvouchers;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import com.ryderbelserion.vital.paper.util.scheduler.FoliaRunnable;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Methods {

    private @NotNull static final CrazyVouchers plugin = CrazyVouchers.get();
    private @NotNull static final SettingsManager config = ConfigManager.getConfig();
    
    public static void removeItem(final ItemStack item, final Player player) {
        if (item.getAmount() <= 1) {
            player.getInventory().removeItem(item);
        } else if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        }
    }
    
    public static String getPrefix(final String message) {
        return MsgUtils.color(config.getProperty(ConfigKeys.command_prefix) + message);
    }

    public static void addItem(final Player player, final ItemStack... items) {
        final Inventory inventory = player.getInventory();

        inventory.setMaxStackSize(64);
        inventory.addItem(items);
    }
    
    public static boolean isInt(final String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }
    
    public static boolean isInt(final CommandSender sender, final String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            Map<String, String> placeholders = new HashMap<>();

            placeholders.put("{arg}", value);

            Messages.not_a_number.sendMessage(sender, placeholders);

            return false;
        }

        return true;
    }

    public static String replacePlaceholders(final Map<String, String> placeholders, String message, final boolean isCommand) {
        for (String placeholder : placeholders.keySet()) {
            message = message.replace(placeholder, placeholders.get(placeholder)).replace(placeholder.toLowerCase(), placeholders.get(placeholder));
        }

        if (isCommand) return message; else return MsgUtils.color(message);
    }
    
    public static boolean isOnline(final CommandSender sender, final String name) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) return true;
        }

        Messages.not_online.sendMessage(sender);

        return false;
    }
    
    public static boolean hasPermission(final Player player, final String perm) {
        if (!player.hasPermission("crazyvouchers." + perm) || !player.hasPermission("voucher." + perm)) {
            Messages.no_permission.sendMessage(player);

            return false;
        }

        return true;
    }
    
    public static boolean hasPermission(final CommandSender sender, final String perm) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("crazyvouchers." + perm) || !player.hasPermission("voucher." + perm)) {
                Messages.no_permission.sendMessage(player);

                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    
    public static boolean isInventoryFull(final Player player) {
        return player.getInventory().firstEmpty() == -1;
    }
    
    public static void firework(final Location location, final List<Color> list) {
        if (location.getWorld() == null) return;

        Firework firework = location.getWorld().spawn(location, Firework.class);

        FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffects(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(list).trail(false).flicker(false).build());
        meta.setPower(0);

        firework.setFireworkMeta(meta);

        PersistentDataContainer container = firework.getPersistentDataContainer();

        container.set(PersistentKeys.no_firework_damage.getNamespacedKey(), PersistentDataType.BOOLEAN, true);

        new FoliaRunnable(plugin.getServer().getRegionScheduler(), firework.getLocation()) {
            @Override
            public void run() {
                firework.detonate();
            }
        }.runDelayed(plugin, 2);
    }
}