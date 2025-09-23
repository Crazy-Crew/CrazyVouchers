package com.badbones69.crazyvouchers.listeners;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class VoucherClickListener implements Listener {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final SettingsManager config = ConfigManager.getConfig();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoucherClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Action action = event.getAction();

        final PlayerInventory inventory = player.getInventory();

        final EquipmentSlot slot = event.getHand();

        if (slot == null) return;

        if (slot == EquipmentSlot.OFF_HAND) {
            final Voucher voucher = this.crazyManager.getVoucherFromItem(inventory.getItemInOffHand());

            if (voucher == null) return;
            if (voucher.isEdible()) return;

            Messages.no_permission_to_use_voucher_offhand.sendMessage(player);

            this.fusion.log("warn", "{} tried to use the voucher in off-hand.", player.getName());

            event.setCancelled(true);

            return;
        }

        if (slot != EquipmentSlot.HAND) return;
        if (!action.isRightClick()) return;

        final ItemStack item = inventory.getItemInMainHand();
        final Voucher voucher = this.crazyManager.getVoucherFromItem(item);

        if (voucher == null) return;
        if (voucher.isEdible()) return;

        useVoucher(player, voucher, item);

        event.setCancelled(true);
    }

    @EventHandler
    public void onVoucherEntity(PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();

        if (!(entity instanceof ItemFrame itemFrame)) return;

        if (!itemFrame.getItem().isEmpty()) return;

        final EquipmentSlot equipmentSlot = event.getHand();

        final Player player = event.getPlayer();

        if (!player.canUseEquipmentSlot(equipmentSlot)) return;

        final PlayerInventory inventory = player.getInventory();

        final ItemStack itemStack = inventory.getItem(equipmentSlot);

        final Voucher voucher = this.crazyManager.getVoucherFromItem(itemStack);

        if (voucher == null) return;
        if (voucher.isEdible()) return;
        if (!voucher.isItemFramePlacementToggled()) return;

        itemFrame.setItem(itemStack, false);

        final int amount = itemStack.getAmount();

        if (amount >= 1) {
            final ItemStack cloned = itemStack.clone();

            cloned.setAmount(itemStack.getAmount() - 1);

            inventory.setItem(equipmentSlot, cloned);

            return;
        }

        inventory.setItem(equipmentSlot, null);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        final EquipmentSlot slot = event.getHand();

        if (slot == EquipmentSlot.HAND) return;

        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();

        if (slot == EquipmentSlot.OFF_HAND) {
            final Voucher voucher = this.crazyManager.getVoucherFromItem(inventory.getItemInOffHand());

            if (voucher == null) return;
            if (!voucher.isEdible()) return;

            Messages.no_permission_to_use_voucher_offhand.sendMessage(player);

            this.fusion.log("warn", "{} tried to use the voucher in off-hand like it's a piece of food.", player.getName());

            event.setCancelled(true);

            return;
        }

        final ItemStack item = event.getItem();

        final Voucher voucher = this.crazyManager.getVoucherFromItem(item);

        if (voucher == null) return;
        if (!voucher.isEdible()) return;

        event.setCancelled(true);

        if (item.getAmount() > 1) {
            Messages.unstack_item.sendMessage(player);

            return;
        }

        useVoucher(player, voucher, item);
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onArmorStandClick(PlayerInteractEntityEvent event) {
        final EquipmentSlot slot = event.getHand();

        final Player player = event.getPlayer();

        final PlayerInventory inventory = player.getInventory();

        final ItemStack itemStack = inventory.getItem(slot);

        if (itemStack.isEmpty()) return;

        final Voucher voucher = this.crazyManager.getVoucherFromItem(itemStack);

        if (voucher != null) event.setCancelled(true);
    }
}