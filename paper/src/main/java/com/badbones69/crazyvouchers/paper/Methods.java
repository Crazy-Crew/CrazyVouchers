package com.badbones69.crazyvouchers.paper;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.paper.api.enums.Translation;
import com.ryderbelserion.cluster.bukkit.utils.LegacyUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import us.crazycrew.crazyenvoys.common.config.ConfigManager;
import us.crazycrew.crazyenvoys.common.config.types.Config;
import us.crazycrew.crazyvouchers.paper.api.plugin.CrazyHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Methods {

    private final CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);
    private final CrazyHandler crazyHandler = this.plugin.getCrazyHandler();
    private final ConfigManager configManager = this.crazyHandler.getConfigManager();
    private final SettingsManager config = this.configManager.getConfig();
    
    public void removeItem(ItemStack item, Player player) {
        if (item.getAmount() <= 1) {
            player.getInventory().removeItem(item);
        } else if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        }
    }
    
    public String getPrefix(String message) {
        return LegacyUtils.color(this.config.getProperty(Config.command_prefix) + message);
    }
    
    public boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }
    
    public boolean isInt(CommandSender sender, String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("%Arg%", s);
            placeholders.put("%arg%", s);
            Translation.not_a_number.sendMessage(sender, placeholders);
            return false;
        }

        return true;
    }

    public String replacePlaceholders(HashMap<String, String> placeholders, String message) {
        for (String placeholder : placeholders.keySet()) {
            message = message.replaceAll(placeholder, placeholders.get(placeholder))
                    .replaceAll(placeholder.toLowerCase(), placeholders.get(placeholder));
        }

        return message;
    }

    public List<String> replacePlaceholders(HashMap<String, String> placeholders, List<String> messageList) {
        List<String> newMessageList = new ArrayList<>();

        for (String message : messageList) {
            for (String placeholder : placeholders.keySet()) {
                newMessageList.add(message.replaceAll(placeholder, placeholders.get(placeholder))
                        .replaceAll(placeholder.toLowerCase(), placeholders.get(placeholder)));
            }
        }

        return newMessageList;
    }
    
    public boolean isOnline(CommandSender sender, String name) {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) return true;
        }

        Translation.not_online.sendMessage(sender);
        return false;
    }
    
    public boolean hasPermission(Player player, String perm) {
        if (!player.hasPermission("voucher." + perm)) {
            Translation.no_permission.sendMessage(player);
            return false;
        }

        return true;
    }
    
    public boolean hasPermission(CommandSender sender, String perm) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("voucher." + perm)) {
                Translation.no_permission.sendMessage(player);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    
    public boolean isInventoryFull(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }
    
    public void fireWork(Location loc, List<Color> list) {
        if (loc.getWorld() == null) return;

        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffects(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(list).trail(false).flicker(false).build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);
        this.plugin.getFireworkDamageAPI().addFirework(firework);

        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, firework::detonate, 2);
    }
    
    public boolean isSimilar(ItemStack one, ItemStack two) {
        if (one.getType() == two.getType()) {
            if (one.hasItemMeta()) {
                if (one.getItemMeta().hasDisplayName()) {
                    if (one.getItemMeta().getDisplayName().equalsIgnoreCase(two.getItemMeta().getDisplayName())) {
                        if (one.getItemMeta().hasLore()) {
                            if (one.getItemMeta().getLore().size() == two.getItemMeta().getLore().size()) {
                                int i = 0;

                                for (String lore : one.getItemMeta().getLore()) {
                                    if (!lore.equals(two.getItemMeta().getLore().get(i))) {
                                        return false;
                                    }

                                    i++;
                                }

                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}