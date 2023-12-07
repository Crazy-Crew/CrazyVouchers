package com.badbones69.crazyvouchers.listeners;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.Translation;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import us.crazycrew.crazyvouchers.common.config.types.Config;

public class VoucherCraftListener implements Listener {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void prepareItemCraft(PrepareItemCraftEvent event) {
        if (!this.plugin.getCrazyHandler().getConfigManager().getConfig().getProperty(Config.prevent_using_vouchers_in_recipes_toggle)) return;

        for (ItemStack itemStack : event.getInventory().getMatrix()) {
            if (itemStack != null) {
                Voucher voucher = crazyManager.getVoucherFromItem(itemStack);

                NBTItem nbt = new NBTItem(itemStack);

                if (voucher != null && nbt.hasTag("voucher")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));

                    boolean sendMsg = this.plugin.getCrazyHandler().getConfigManager().getConfig().getProperty(Config.prevent_using_vouchers_in_recipes_alert);

                    if (sendMsg) Translation.cannot_put_items_in_crafting_table.sendMessage(event.getView().getPlayer());

                    break;
                }
            }
        }
    }
}