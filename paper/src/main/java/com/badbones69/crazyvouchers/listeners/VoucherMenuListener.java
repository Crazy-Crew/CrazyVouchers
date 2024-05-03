package com.badbones69.crazyvouchers.listeners;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyHandler;
import com.badbones69.crazyvouchers.api.builders.types.VoucherGuiMenu;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.v2.GenericVoucher;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class VoucherMenuListener implements Listener {

    private final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final @NotNull CrazyHandler crazyHandler = this.plugin.getCrazyHandler();

    @EventHandler
    public void invClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        if (inventory == null) return;

        if (!(inventory.getHolder() instanceof VoucherGuiMenu menu)) return;

        event.setCancelled(true);

        if (event.getRawSlot() > 54) return;

        if (event.getCurrentItem() == null) return;

        if (item == null) return;

        if (!item.hasItemMeta()) return;

        ItemMeta itemMeta = item.getItemMeta();

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

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

        ItemStack stack = inventory.getItem(event.getRawSlot());

        if (stack != null) {
            if (stack.hasItemMeta()) {
                ItemMeta meta = stack.getItemMeta();

                PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();

                if (persistentDataContainer.has(PersistentKeys.voucher_item_admin.getNamespacedKey())) {
                    GenericVoucher voucher = this.crazyHandler.getVoucherFromItem(meta);

                    PlayerInventory playerInventory = player.getInventory();

                    if (voucher != null) {
                        playerInventory.setItem(playerInventory.firstEmpty(), voucher.getItem(player));
                    }
                }
            }
        }
    }
}