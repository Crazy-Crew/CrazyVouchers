package com.badbones69.crazyvouchers.api.builders;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyPlatform;
import com.ryderbelserion.fusion.paper.FusionPaper;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("ALL")
public abstract class InventoryBuilder implements InventoryHolder, Listener {

    protected @NonNull final CrazyVouchers plugin = CrazyVouchers.get();
    protected @NonNull final CrazyPlatform platform = this.plugin.getPlatform();
    protected @NonNull final Server server = this.plugin.getServer();
    protected @NonNull final FusionPaper fusion = this.plugin.getFusion();

    private Inventory inventory;
    private Player player;
    private String title;
    private int size;
    private int page;

    public InventoryBuilder(@NonNull final Player player, final int size, @NonNull final String title) {
        this.title = title;
        this.player = player;
        this.size = size;

        this.inventory = this.server.createInventory(this, this.size, this.fusion.asComponent(player, this.title));
    }

    public InventoryBuilder(@NonNull final Player player, final int size, final int page, @NonNull final String title) {
        this.title = title;
        this.player = player;
        this.size = size;
        this.page = page;

        this.inventory = this.server.createInventory(this, this.size, this.fusion.asComponent(player, this.title));
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

    public void title(@NonNull final String title) {
        this.title = title;
    }

    public boolean contains(@NonNull final String message) {
        return this.title.contains(message);
    }

    public @NonNull final Player getPlayer() {
        return this.player;
    }

    @Override
    public @NonNull Inventory getInventory() {
        return this.inventory;
    }
}