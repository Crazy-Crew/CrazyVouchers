package com.badbones69.crazyvouchers.api.builders.types;

import com.badbones69.crazyvouchers.api.InventoryManager;
import com.badbones69.crazyvouchers.api.builders.InventoryBuilder;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.other.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;

public class VoucherMenu extends InventoryBuilder {

    private @NotNull final InventoryManager inventoryManager = this.plugin.getInventoryManager();

    public VoucherMenu(final Player player, final int size, final int page, final String title) {
        super(player, size, page, title);
    }

    public VoucherMenu() {}

    @Override
    public InventoryBuilder build() {
        final Inventory inventory = getInventory();

        setDefaultItems(inventory);

        final List<ItemStack> items = this.inventoryManager.getPreviewItems(getPage());

        for (ItemStack item : items) {
            final int nextSlot = inventory.firstEmpty();

            if (nextSlot >= 0) { // If next slot is greater than 0
                inventory.setItem(nextSlot, item);
            } else {
                break;
            }
        }

        return this;
    }

    @Override
    public void run(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof VoucherMenu holder)) return;

        event.setCancelled(true);

        final Player player = holder.getPlayer();

        final ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        if (!item.hasItemMeta()) return;

        final PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        if (container.has(PersistentKeys.back_button.getNamespacedKey())) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, SoundCategory.PLAYERS, 1F, 1F);

            this.inventoryManager.backPage(player, container.getOrDefault(PersistentKeys.back_button.getNamespacedKey(), PersistentDataType.INTEGER, 1));

            return;
        }

        if (container.has(PersistentKeys.next_button.getNamespacedKey())) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, SoundCategory.PLAYERS, 1F, 1F);

            this.inventoryManager.nextPage(player, container.getOrDefault(PersistentKeys.next_button.getNamespacedKey(), PersistentDataType.INTEGER, 1));

            return;
        }

        if (container.has(PersistentKeys.voucher_item_admin.getNamespacedKey())) {
            final Voucher voucher = this.plugin.getCrazyManager().getVoucherFromItem(item);

            if (voucher != null) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 1F, 1F);

                player.getInventory().addItem(voucher.buildItem());
            }
        }
    }

    private void setDefaultItems(@NotNull final Inventory inventory) {
        final Player player = getPlayer();

        final List<Integer> numbers = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53);

        final ItemStack itemStack = new ItemBuilder().setMaterial(Material.BLUE_STAINED_GLASS_PANE).setName(" ").build();

        for (int number : numbers) {
            inventory.setItem(number, itemStack);
        }

        final int page = this.inventoryManager.getPage(player);

        if (page == 1) {
            inventory.setItem(48, itemStack.withType(Material.GRAY_STAINED_GLASS_PANE));
        } else {
            inventory.setItem(48, this.inventoryManager.getBackButton(player));
        }

        final int maxPage = this.inventoryManager.getMaxPages();

        if (page == maxPage) {
            inventory.setItem(50, itemStack.withType(Material.GRAY_STAINED_GLASS_PANE));
        } else {
            inventory.setItem(50, this.inventoryManager.getNextButton(player));
        }
    }
}