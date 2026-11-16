package com.badbones69.crazyvouchers.listeners;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyPlatform;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class VoucherCraftListener implements Listener {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final CrazyPlatform platform = this.plugin.getPlatform();

    private final ConfigManager configManager = this.platform.getConfigManager();

    private final PropertyManager config = this.configManager.getConfig();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void prepareItemCraft(PrepareItemCraftEvent event) {
        if (!this.config.getProperty(RootKeys.prevent_using_vouchers_in_recipes_toggle)) return;

        final CraftingInventory inventory = event.getInventory();

        for (final ItemStack itemStack : inventory.getMatrix()) {
            if (itemStack == null || itemStack.isEmpty()) continue;

            final PersistentDataContainerView container = itemStack.getPersistentDataContainer();

            if (container.has(PersistentKeys.voucher_item.getNamespacedKey())) {
                inventory.setResult(ItemStack.empty());

                if (this.config.getProperty(RootKeys.prevent_using_vouchers_in_recipes_alert)) {
                    Message.cannot_put_items_in_crafting_table.sendMessage(event.getView().getPlayer());
                }

                break;
            }
        }
    }
}