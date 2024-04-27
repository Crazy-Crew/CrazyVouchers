package com.badbones69.crazyvouchers.listeners;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import de.tr7zw.changeme.nbtapi.NBTItem;
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

    private final @NotNull CrazyManager crazyManager = this.plugin.getCrazyManager();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void prepareItemCraft(PrepareItemCraftEvent event) {
        if (!ConfigManager.getConfig().getProperty(ConfigKeys.prevent_using_vouchers_in_recipes_toggle)) return;

        for (ItemStack itemStack : event.getInventory().getMatrix()) {
            if (itemStack != null) {
                Voucher voucher = crazyManager.getVoucherFromItem(itemStack);

                NBTItem nbt = new NBTItem(itemStack);

                if (voucher != null && nbt.hasTag("voucher")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));

                    if (ConfigManager.getConfig().getProperty(ConfigKeys.prevent_using_vouchers_in_recipes_alert)) Messages.cannot_put_items_in_crafting_table.sendMessage(event.getView().getPlayer());

                    break;
                }
            }
        }
    }
}