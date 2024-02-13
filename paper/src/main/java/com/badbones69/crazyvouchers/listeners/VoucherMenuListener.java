package com.badbones69.crazyvouchers.listeners;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.builders.types.VoucherGuiMenu;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.CrazyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

public class VoucherMenuListener implements Listener {
    
    private final CrazyVouchers plugin = CrazyVouchers.get();
    
    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    @EventHandler
    public void invClick(InventoryClickEvent e) {
        Inventory inventory = e.getClickedInventory();
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();

        if (inventory == null) return;

        if (!(inventory.getHolder() instanceof VoucherGuiMenu menu)) return;

        e.setCancelled(true);

        if (e.getRawSlot() > 54) return;

        if (e.getCurrentItem() == null) return;

        if (item == null) return;

        if (item.getItemMeta() == null || !item.hasItemMeta()) return;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        if (container.has(PersistentKeys.back_button.getNamespacedKey())) {
            menu.backPage(player);
            player.openInventory(menu.build().getInventory());
            return;
        }

        if (container.has(PersistentKeys.next_button.getNamespacedKey())) {
            menu.nextPage(player);
            player.openInventory(menu.build().getInventory());
            return;
        }

        ItemStack stack = inventory.getItem(e.getRawSlot());

        if (stack != null) {
            PersistentDataContainer persistentDataContainer = stack.getItemMeta().getPersistentDataContainer();

            if (persistentDataContainer.has(PersistentKeys.voucher_item_admin.getNamespacedKey())) {
                if (this.crazyManager.getVoucherFromItem(stack) != null) player.getInventory().addItem(this.crazyManager.getVoucherFromItem(stack).buildItem());
            }
        }
    }
}