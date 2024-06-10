package com.badbones69.crazyvouchers.api.builders;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ALL")
public abstract class InventoryBuilder implements InventoryHolder, Listener {

    @NotNull
    protected final CrazyVouchers plugin = CrazyVouchers.get();

    private Inventory inventory;
    private Player player;
    private String title;
    private int size;
    private int page;

    public InventoryBuilder(final Player player, final int size, final String title) {
        this.title = title;
        this.player = player;
        this.size = size;

        this.inventory = this.plugin.getServer().createInventory(this, this.size, MsgUtils.color(this.title));
    }

    public InventoryBuilder(final Player player, final int size, final int page, final String title) {
        this.title = title;
        this.player = player;
        this.size = size;
        this.page = page;

        this.inventory = this.plugin.getServer().createInventory(this, this.size, MsgUtils.color(this.title));
    }

    public InventoryBuilder() {}

    public abstract InventoryBuilder build();

    public abstract void run(InventoryClickEvent event);

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        run(event);
    }

    public void size(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPage() {
        return this.page;
    }

    public void title(String title) {
        this.title = title;
    }

    public boolean contains(String message) {
        return this.title.contains(message);
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return this.inventory;
    }
}