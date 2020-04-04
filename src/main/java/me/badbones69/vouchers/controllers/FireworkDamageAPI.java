package me.badbones69.vouchers.controllers;

import me.badbones69.vouchers.api.enums.Version;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class FireworkDamageAPI implements Listener {
    
    private Plugin plugin;
    private static ArrayList<Entity> fireworks = new ArrayList<>();
    
    public FireworkDamageAPI(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     *
     * @return All the active fireworks.
     */
    public static ArrayList<Entity> getFireworks() {
        return fireworks;
    }
    
    /**
     *
     * @param firework The firework you want to add.
     */
    public static void addFirework(Entity firework) {
        if (Version.isNewer(Version.v1_10_R1)) {
            fireworks.add(firework);
        }
    }
    
    /**
     *
     * @param firework The firework you are removing.
     */
    public static void removeFirework(Entity firework) {
        fireworks.remove(firework);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        for (Entity en : e.getEntity().getNearbyEntities(5, 5, 5)) {
            if (en.getType() == EntityType.FIREWORK) {
                if (getFireworks().contains(en)) {
                    e.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onFireworkExplode(FireworkExplodeEvent e) {
        final Entity firework = e.getEntity();
        if (getFireworks().contains(firework)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    removeFirework(firework);
                }
            }.runTaskLater(plugin, 5);
        }
    }
    
}