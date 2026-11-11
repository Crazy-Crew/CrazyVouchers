package com.badbones69.crazyvouchers.api;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.builders.types.VoucherMenu;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.ryderbelserion.fusion.paper.builders.items.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryManager {

    private @NonNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NonNull final CrazyPlatform platform = this.plugin.getPlatform();

    private final Map<UUID, Integer> pages = new HashMap<>();

    private final ItemBuilder nextButton;
    private final ItemBuilder backButton;

    private final int amount = 36;

    public InventoryManager() {
        this.nextButton = ItemBuilder.from(ItemType.ARROW).withDisplayName("<gold>Next >>");

        this.backButton = ItemBuilder.from(ItemType.ARROW).withDisplayName("<gold><< Back");
    }

    public final ItemStack getVoucher(@NonNull final Player player, @NonNull final Voucher voucher) {
        final ItemStack itemStack = voucher.buildItem(player);

        itemStack.editPersistentDataContainer(container -> container.set(PersistentKeys.voucher_item_admin.getNamespacedKey(), PersistentDataType.STRING, voucher.getStrippedName()));

        return itemStack;
    }

    public final List<ItemStack> getPreviewItems(@NonNull final Player player, int page) {
        final List<ItemStack> items = new ArrayList<>();

        if (page <= 0) page = 1;

        final List<Voucher> vouchers = this.platform.getVouchers();
        final int count = vouchers.size();

        final int max = this.amount;

        int startIndex = page * max - max;
        int endIndex = Math.min(startIndex + max, count);

        for (;startIndex < endIndex; startIndex++) {
            if (startIndex < count) {
                items.add(getVoucher(player, vouchers.get(startIndex)));
            }
        }

        return items;
    }

    public final ItemStack getBackButton(@NonNull final Player player) {
        int page = getPage(player) - 1;

        ItemStack itemStack = this.backButton.addDisplayLore("<gray>Page: <blue>" + page).asItemStack(player);

        itemStack.editPersistentDataContainer(container -> container.set(PersistentKeys.back_button.getNamespacedKey(), PersistentDataType.INTEGER, page));

        return itemStack;
    }

    public final ItemStack getNextButton(@NonNull final Player player) {
        int page = getPage(player) + 1;

        ItemStack itemStack = this.nextButton.addDisplayLore("<gray>Page: <blue>" + page).asItemStack(player);

        itemStack.editPersistentDataContainer(container -> container.set(PersistentKeys.next_button.getNamespacedKey(), PersistentDataType.INTEGER, page));

        return itemStack;
    }

    public void remove(@NonNull final Player player) {
        this.pages.remove(player.getUniqueId());
    }

    public void nextPage(@NonNull final Player player, final int page) {
        setPage(player, page);

        buildInventory(player, 0);
    }

    public void backPage(@NonNull final Player player, final int page) {
        setPage(player, page);

        buildInventory(player, 0);
    }

    public void buildInventory(@NonNull final Player player, final int page) {
        final VoucherMenu menu = new VoucherMenu(player, 54, page <= 0 ? getPage(player) : page, "<red>CrazyVouchers");

        player.openInventory(menu.build().getInventory());
    }

    public int getPage(@NonNull final Player player) {
        return this.pages.getOrDefault(player.getUniqueId(), 1);
    }

    public void setPage(@NonNull final Player player, int page) {
        int max = getMaxPages();

        if (page > max) {
            page = max;
        }

        this.pages.put(player.getUniqueId(), page);
    }

    public int getMaxPages() {
        int size = this.platform.getVouchers().size();

        return Math.max(1, size % this.amount > 0 ? (size / this.amount) + 1 : size / this.amount);
    }
}