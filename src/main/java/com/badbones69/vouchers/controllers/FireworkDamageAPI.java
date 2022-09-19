package com.badbones69.vouchers.controllers;

import com.badbones69.vouchers.Vouchers;
import com.badbones69.vouchers.api.CrazyManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class FireworkDamageAPI implements Listener {

    public final Vouchers plugin = Vouchers.getPlugin();
    
    /**
     * @param firework The firework you want to add.
     */
    public void addFirework(Entity firework) {
        firework.setMetadata("nodamage", new FixedMetadataValue(plugin, true));
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Firework fw) {
            if (fw.hasMetadata("nodamage")) e.setCancelled(true);
        }
    }
}