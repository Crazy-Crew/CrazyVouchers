package com.badbones69.crazyvouchers.platform.util;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.platform.config.types.ConfigKeys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiscUtil {

    public static final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    public static final @NotNull SettingsManager config = ConfigManager.getConfig();
    
    public static void removeItem(ItemStack item, Player player) {
        if (item.getAmount() <= 1) {
            player.getInventory().removeItem(item);
        } else if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        }
    }
    
    public static String getPrefix(String message) {
        return MsgUtil.color(config.getProperty(ConfigKeys.command_prefix) + message);
    }
    
    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }
    
    public static boolean isInt(CommandSender sender, String value) {
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

    public static boolean isLogging() {
        return ConfigManager.getConfig().getProperty(ConfigKeys.verbose_logging);
    }

    public static void registerCommand(PluginCommand pluginCommand, TabCompleter tabCompleter, CommandExecutor commandExecutor) {
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);

            if (tabCompleter != null) pluginCommand.setTabCompleter(tabCompleter);
        }
    }

    public static String replacePlaceholders(Map<String, String> placeholders, String message, boolean isCommand) {
        for (String placeholder : placeholders.keySet()) {
            message = message.replace(placeholder, placeholders.get(placeholder)).replace(placeholder.toLowerCase(), placeholders.get(placeholder));
        }

        if (isCommand) return message; else return MsgUtil.color(message);
    }
    
    public static boolean isOnline(CommandSender sender, String name) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) return true;
        }

        Messages.not_online.sendMessage(sender);

        return false;
    }
    
    public static boolean hasPermission(Player player, String perm) {
        if (!player.hasPermission("voucher." + perm)) {
            Messages.no_permission.sendMessage(player);

            return false;
        }

        return true;
    }
    
    public static boolean hasPermission(CommandSender sender, String perm) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("voucher." + perm)) {
                Messages.no_permission.sendMessage(player);

                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    
    public static boolean isInventoryFull(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }
    
    public static void firework(Location loc, List<Color> list) {
        if (loc.getWorld() == null) return;

        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffects(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(list).trail(false).flicker(false).build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);

        PersistentDataContainer container = firework.getPersistentDataContainer();

        container.set(PersistentKeys.no_firework_damage.getNamespacedKey(), PersistentDataType.BOOLEAN, true);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, firework::detonate, 2);
    }
}