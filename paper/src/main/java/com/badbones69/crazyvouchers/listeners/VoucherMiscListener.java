package com.badbones69.crazyvouchers.listeners;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.badbones69.crazyvouchers.api.builders.types.VoucherMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class VoucherMiscListener implements Listener {

    private final InventoryManager inventoryManager = CrazyVouchers.get().getInventoryManager();

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof VoucherMenu)) return;

        if (event.getPlayer() instanceof Player player) {
            this.inventoryManager.remove(player);
        }
    }
}