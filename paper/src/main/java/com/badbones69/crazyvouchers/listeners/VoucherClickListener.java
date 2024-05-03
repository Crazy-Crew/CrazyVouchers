package com.badbones69.crazyvouchers.listeners;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyHandler;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemEvent;
import com.badbones69.crazyvouchers.api.objects.v2.GenericVoucher;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class VoucherClickListener implements Listener {

    private final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final @NotNull CrazyHandler crazyHandler = this.plugin.getCrazyHandler();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoucherClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();

        if (event.getHand() == EquipmentSlot.OFF_HAND && event.getHand() != null) {
            ItemStack itemStack = inventory.getItemInOffHand();

            if (itemStack.hasItemMeta()) {
                GenericVoucher voucher = this.crazyHandler.getVoucherFromItem(itemStack.getItemMeta());

                if (voucher != null && !voucher.isCancelled()) {
                    Messages.no_permission_to_use_voucher_offhand.sendMessage(player);

                    event.setCancelled(true);
                }

                return;
            }

            return;
        }

        ItemStack itemStack = inventory.getItemInMainHand();

        if (itemStack.getType() == Material.AIR) return;

        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            if (itemStack.hasItemMeta()) {
                ItemMeta itemMeta = itemStack.getItemMeta();

                GenericVoucher voucher = this.crazyHandler.getVoucherFromItem(itemMeta);

                if (voucher != null) {
                    String argument = "";

                    if (this.crazyHandler.hasArgument(itemMeta)) {
                        argument = this.crazyHandler.getArgumentFromItem(itemMeta);
                    }

                    VoucherRedeemEvent redeemEvent = new VoucherRedeemEvent(player, voucher, argument, itemStack);
                    this.plugin.getServer().getPluginManager().callEvent(redeemEvent);
                }
            }
        }
    }
}