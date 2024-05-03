package com.badbones69.crazyvouchers.listeners;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyHandler;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.platform.config.types.ConfigKeys;

public class VoucherCraftListener implements Listener {

    private final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final @NotNull CrazyHandler crazyHandler = this.plugin.getCrazyHandler();

    private final @NotNull SettingsManager config = ConfigManager.getConfig();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void prepareItemCraft(PrepareItemCraftEvent event) {
        if (!config.getProperty(ConfigKeys.prevent_using_vouchers_in_recipes_toggle)) return;

        for (ItemStack itemStack : event.getInventory().getMatrix()) {
            if (itemStack == null) return;

            if (itemStack.hasItemMeta()) {
                if (!this.crazyHandler.isVoucher(itemStack.getItemMeta())) return;

                event.getInventory().setResult(new ItemStack(Material.AIR));

                if (config.getProperty(ConfigKeys.prevent_using_vouchers_in_recipes_alert)) Messages.cannot_put_items_in_crafting_table.sendMessage(event.getView().getPlayer());

                break;
            }
        }
    }
}