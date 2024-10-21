package com.badbones69.crazyvouchers.listeners;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.config.ConfigManager;
import de.tr7zw.changeme.nbtapi.NBTItem;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;

public class VoucherCraftListener implements Listener {

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NotNull final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final SettingsManager config = ConfigManager.getConfig();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void prepareItemCraft(PrepareItemCraftEvent event) {
        if (!this.config.getProperty(ConfigKeys.prevent_using_vouchers_in_recipes_toggle)) return;

        for (ItemStack itemStack : event.getInventory().getMatrix()) {
            if (itemStack == null) return;

            Voucher voucher = crazyManager.getVoucherFromItem(itemStack);

            if (voucher == null) return;

            final PersistentDataContainerView container = itemStack.getPersistentDataContainer();

            if (container.has(PersistentKeys.voucher_item.getNamespacedKey())) {
                event.getInventory().setResult(new ItemStack(Material.AIR));

                if (this.config.getProperty(ConfigKeys.prevent_using_vouchers_in_recipes_alert)) {
                    Messages.cannot_put_items_in_crafting_table.sendMessage(event.getView().getPlayer());
                }

                break;
            } else {
                NBTItem nbt = new NBTItem(itemStack); // this section related to nbt items is deprecated, and marked for removal

                if (nbt.hasTag("voucher")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));

                    if (this.config.getProperty(ConfigKeys.prevent_using_vouchers_in_recipes_alert)) {
                        Messages.cannot_put_items_in_crafting_table.sendMessage(event.getView().getPlayer());
                    }

                    break;
                }
            }
        }
    }
}