package com.badbones69.crazyvouchers.listeners.v2;

import com.badbones69.crazyvouchers.api.events.VoucherRedeemEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VoucherRedeemListener implements Listener {

    @EventHandler
    public void onVoucherRedeem(VoucherRedeemEvent event) {
        event.setCancelled(event.getVoucher().execute(event.getPlayer()));
    }
}