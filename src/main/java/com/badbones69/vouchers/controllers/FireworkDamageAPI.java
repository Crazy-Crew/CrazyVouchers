package com.badbones69.vouchers.controllers;

import com.badbones69.vouchers.Vouchers;
import com.badbones69.vouchers.api.CrazyManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class FireworkDamageAPI implements Listener {

    public final Vouchers plugin = Vouchers.getPlugin();

    NamespacedKey noDamage = new NamespacedKey(plugin, "no-damage");

    /**
     * @param firework The firework you want to add.
     */
    public void addFirework(Entity firework) {
        PersistentDataContainer container = firework.getPersistentDataContainer();

        container.set(noDamage, PersistentDataType.STRING, "no-damage");
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Firework firework) {

            PersistentDataContainer container = firework.getPersistentDataContainer();

            if (container.has(noDamage, PersistentDataType.STRING)) e.setCancelled(true);
        }
    }
}