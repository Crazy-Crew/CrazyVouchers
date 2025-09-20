package com.badbones69.crazyvouchers.api.builders;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.paper.FusionPaper;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ALL")
public abstract class InventoryBuilder implements InventoryHolder, Listener {

    protected @NotNull final CrazyVouchers plugin = CrazyVouchers.get();
    protected @NotNull final Server server = this.plugin.getServer();
    protected @NotNull final FusionPaper fusion = this.plugin.getFusion();

    private Inventory inventory;
    private Player player;
    private String title;
    private int size;
    private int page;

    public InventoryBuilder(@NotNull final Player player, final int size, @NotNull final String title) {
        this.title = title;
        this.player = player;
        this.size = size;

        this.inventory = this.server.createInventory(this, this.size, this.fusion.parse(this.title));
    }

    public InventoryBuilder(@NotNull final Player player, final int size, final int page, @NotNull final String title) {
        this.title = title;
        this.player = player;
        this.size = size;
        this.page = page;

        this.inventory = this.server.createInventory(this, this.size, this.fusion.parse(this.title));
    }

    public InventoryBuilder() {}

    public abstract InventoryBuilder build();

    public abstract void run(InventoryClickEvent event);

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        run(event);
    }

    public void size(final int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public void setPage(final int page) {
        this.page = page;
    }

    public int getPage() {
        return this.page;
    }

    public void title(@NotNull final String title) {
        this.title = title;
    }

    public boolean contains(@NotNull final String message) {
        return this.title.contains(message);
    }

    public @NotNull final Player getPlayer() {
        return this.player;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}