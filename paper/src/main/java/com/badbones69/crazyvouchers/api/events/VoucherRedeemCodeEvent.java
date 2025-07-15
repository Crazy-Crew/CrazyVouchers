package com.badbones69.crazyvouchers.api.events;

import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class VoucherRedeemCodeEvent extends Event implements Cancellable {
    
    private final Player player;
    private final VoucherCode voucherCode;
    private boolean cancelled;
    private static final HandlerList handlers = new HandlerList();
    
    /**
     * @param player The player using the voucherCode.
     * @param voucherCode The voucherCode being used.
     */
    public VoucherRedeemCodeEvent(@NotNull final Player player, @NotNull final VoucherCode voucherCode) {
        this.player = player;
        this.voucherCode = voucherCode;
        this.cancelled = false;
    }
    
    /**
     * @return The player redeeming the voucherCode.
     */
    public @NotNull Player getPlayer() {
        return this.player;
    }
    
    /**
     * @return Voucher object used in the event.
     */
    public @NotNull VoucherCode getVoucherCode() {
        return this.voucherCode;
    }
    
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}