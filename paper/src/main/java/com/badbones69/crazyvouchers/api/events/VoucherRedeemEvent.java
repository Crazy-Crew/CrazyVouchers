package com.badbones69.crazyvouchers.api.events;

import com.badbones69.crazyvouchers.api.objects.Voucher;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class VoucherRedeemEvent extends Event implements Cancellable {
    
    private final Player player;
    private final Voucher voucher;
    private final String argument;
    private boolean cancelled;
    private static final HandlerList handlers = new HandlerList();
    
    /**
     *
     * @param player The player using the voucher.
     * @param voucher The voucher being used.
     * @param argument The argument that is used. If no argument is used leave it as a blank string.
     */
    public VoucherRedeemEvent(Player player, Voucher voucher, String argument) {
        this.player = player;
        this.voucher = voucher;
        this.argument = argument;
        this.cancelled = false;
    }
    
    /**
     * @return The player redeeming the voucher.
     */
    public Player getPlayer() {
        return this.player;
    }
    
    /**
     * @return Voucher object used in the event.
     */
    public Voucher getVoucher() {
        return this.voucher;
    }
    
    /**
     * @return The argument used by the voucher. If not used it will be a blank string.
     */
    public String getArgument() {
        return this.argument;
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