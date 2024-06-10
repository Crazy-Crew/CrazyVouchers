package com.badbones69.crazyvouchers.api;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.builders.types.VoucherMenu;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.other.ItemBuilder;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryManager {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final Map<UUID, Integer> pages = new HashMap<>();

    private final ItemBuilder nextButton;
    private final ItemBuilder backButton;

    private final int amount = 36;

    public InventoryManager() {
        this.nextButton = new ItemBuilder()
                .setMaterial(Material.ARROW)
                .setName("&6&lNext >>");

        this.backButton = new ItemBuilder()
                .setMaterial(Material.ARROW)
                .setName("&6&l<< Back");
    }

    public final ItemStack getVoucher(final Voucher voucher) {
        final ItemStack itemStack = voucher.buildItem();

        itemStack.editMeta(itemMeta -> {
            final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            container.set(PersistentKeys.voucher_item_admin.getNamespacedKey(), PersistentDataType.STRING, voucher.getName());
        });

        return itemStack;
    }

    public final List<ItemStack> getPreviewItems(int page) {
        final List<ItemStack> items = new ArrayList<>();

        if (page <= 0) page = 1;

        final List<Voucher> vouchers = this.crazyManager.getVouchers();
        final int count = vouchers.size();

        final int max = this.amount;

        int startIndex = page * max - max;
        int endIndex = Math.min(startIndex + max, count);

        for (;startIndex < endIndex; startIndex++) {
            if (startIndex < count) {
                items.add(getVoucher(vouchers.get(startIndex)));
            }
        }

        return items;
    }

    public final ItemStack getBackButton(final Player player) {
        int page = getPage(player) - 1;

        ItemStack itemStack = this.backButton
                .setLore(List.of("&7&lPage: &b" + page))
                .build();

        itemStack.editMeta(itemMeta -> {
            final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            container.set(PersistentKeys.back_button.getNamespacedKey(), PersistentDataType.INTEGER, page);
        });

        return itemStack;
    }

    public final ItemStack getNextButton(final Player player) {
        int page = getPage(player) + 1;

        ItemStack itemStack = this.nextButton
                .setLore(List.of("&7&lPage: &b" + page))
                .build();

        itemStack.editMeta(itemMeta -> {
            final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            container.set(PersistentKeys.next_button.getNamespacedKey(), PersistentDataType.INTEGER, page);
        });

        return itemStack;
    }

    public void remove(@NotNull final Player player) {
        this.pages.remove(player.getUniqueId());
    }

    public void nextPage(@NotNull final Player player, final int page) {
        setPage(player, page);

        buildInventory(player, 0);
    }

    public void backPage(@NotNull final Player player, final int page) {
        setPage(player, page);

        buildInventory(player, 0);
    }

    public void buildInventory(@NotNull final Player player, final int page) {
        final VoucherMenu menu = new VoucherMenu(player, 54, page <= 0 ? getPage(player) : page, MsgUtils.color("&c&lCrazyVouchers"));

        player.openInventory(menu.build().getInventory());
    }

    public int getPage(@NotNull final Player player) {
        return this.pages.getOrDefault(player.getUniqueId(), 1);
    }

    public void setPage(@NotNull final Player player, int page) {
        int max = getMaxPages();

        if (page > max) {
            page = max;
        }

        this.pages.put(player.getUniqueId(), page);
    }

    public int getMaxPages() {
        int size = this.crazyManager.getVouchers().size();

        return Math.max(1, size % this.amount > 0 ? (size / this.amount) + 1 : size / this.amount);
    }
}