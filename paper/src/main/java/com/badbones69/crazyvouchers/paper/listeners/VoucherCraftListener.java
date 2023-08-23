package com.badbones69.crazyvouchers.paper.listeners;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import com.badbones69.crazyvouchers.paper.api.CrazyManager;
import com.badbones69.crazyvouchers.paper.api.FileManager.Files;
import com.badbones69.crazyvouchers.paper.api.enums.Messages;
import com.badbones69.crazyvouchers.paper.api.objects.Voucher;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

public class VoucherCraftListener implements Listener {

    private final CrazyVouchers plugin = CrazyVouchers.getPlugin();

    private final CrazyManager crazyManager = plugin.getCrazyManager();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void prepareItemCraft(PrepareItemCraftEvent event) {
        FileConfiguration config = Files.CONFIG.getFile();

        if (!config.getBoolean("Settings.Prevent-Using-Vouchers-In-Recipes.Toggle")) return;

        for (ItemStack itemStack : event.getInventory().getMatrix()) {
            if (itemStack != null) {
                Voucher voucher = crazyManager.getVoucherFromItem(itemStack);

                NBTItem nbt = new NBTItem(itemStack);

                if (voucher != null && nbt.hasTag("voucher")) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));

                    boolean sendMsg = config.getBoolean("Settings.Prevent-Using-Vouchers-In-Recipes.Alert");

                    if (sendMsg) event.getView().getPlayer().sendMessage(Messages.CANNOT_PUT_ITEMS_IN_CRAFTING_TABLE.getMessage());

                    break;
                }
            }
        }
    }
}