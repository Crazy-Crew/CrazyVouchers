package com.badbones69.crazyvouchers.api.builders;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.other.MsgUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ALL")
public abstract class InventoryBuilder implements InventoryHolder {

    @NotNull
    protected final CrazyVouchers plugin = CrazyVouchers.get();

    private final Inventory inventory;
    private final Player player;
    private String title;
    private int size;
    private int page;

    public InventoryBuilder(Player player, int size, String title) {
        this.title = title;
        this.player = player;
        this.size = size;

        this.inventory = this.plugin.getServer().createInventory(this, this.size, MsgUtils.color(this.title));
    }

    public abstract InventoryBuilder build();

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