package com.badbones69.crazyvouchers.api.builders.types;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.builders.InventoryBuilder;
import com.badbones69.crazyvouchers.api.builders.ItemBuilder;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.v2.GenericVoucher;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VoucherGuiMenu extends InventoryBuilder {

    private final Map<UUID, Integer> playerPage = new HashMap<>();

    public VoucherGuiMenu(Player player, int size, String title) {
        super(player, size, title);
    }

    @Override
    public InventoryBuilder build() {
        setDefaultItems();

        for (GenericVoucher voucher : getPageVouchers(getPage())) {
            build(voucher);
        }

        return this;
    }

    private void build(GenericVoucher voucher) {
        ItemStack itemStack = voucher.getItem(getPlayer(), null, 1).setString(PersistentKeys.voucher_item_admin.getNamespacedKey(), voucher.getFileName()).build();
        Inventory inventory = getInventory();

        inventory.setItem(inventory.firstEmpty(), itemStack);
    }

    @Override
    public InventoryBuilder build(int pageNumber) {
        setDefaultItems();

        for (GenericVoucher voucher : getPageVouchers(pageNumber)) {
            build(voucher);
        }

        return this;
    }

    private void setDefaultItems() {
        for (int i : Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 49, 51, 52, 53)) {
            getInventory().setItem(i, new ItemBuilder().setDisplayName(" ").setMaterial(Material.BLUE_STAINED_GLASS_PANE).build());
        }

        int page = getPage(getPlayer());
        int maxPage = getMaxPage();

        if (page == 1) {
            getInventory().setItem(48, new ItemBuilder().setDisplayName(" ").setMaterial(Material.GRAY_STAINED_GLASS_PANE).build());
        } else {
            ItemStack backButton = new ItemBuilder().setMaterial(Material.FEATHER).setDisplayName("<gold><bold><< Back</bold>").addDisplayLore("<gray><bold>Page:</bold> <blue>" + (getPage(getPlayer()) - 1)).setString(PersistentKeys.back_button.getNamespacedKey(), "none").build();

            getInventory().setItem(48, backButton);
        }

        if (page == maxPage) {
            getInventory().setItem(50, new ItemBuilder().setDisplayName(" ").setMaterial(Material.GRAY_STAINED_GLASS_PANE).build());
        } else {
            ItemStack nextButton = new ItemBuilder().setMaterial(Material.FEATHER).setDisplayName("<gold><bold>Next >></bold>").addDisplayLore("<gray><bold>Page:</bold> <blue>" + (getPage(getPlayer()) + 1)).setString(PersistentKeys.next_button.getNamespacedKey(), "none").build();

            getInventory().setItem(50, nextButton);
        }
    }

    private List<GenericVoucher> getPageVouchers(int page) {
        List<GenericVoucher> list = plugin.getCrazyHandler().getVouchers();
        List<GenericVoucher> vouchers = new ArrayList<>();

        if (page <= 0) page = 1;
        int max = 36;
        int index = page * max - max;
        int endIndex = index >= list.size() ? list.size() - 1 : index + max;

        for (; index < endIndex; index++) {
            if (index < list.size()) vouchers.add(list.get(index));
        }

        for (; vouchers.isEmpty(); page--) {
            if (page <= 0) break;
            index = page * max - max;
            endIndex = index >= list.size() ? list.size() - 1 : index + max;

            for (; index < endIndex; index++) {
                if (index < list.size()) vouchers.add(list.get(index));
            }
        }

        return vouchers;
    }

    private void setPage(Player player, int pageNumber) {
        int max = getMaxPage();

        if (pageNumber < 1) {
            pageNumber = 1;
        } else if (pageNumber >= max) {
            pageNumber = max;
        }

        this.playerPage.put(player.getUniqueId(), pageNumber);
    }

    private int getPage(Player player) {
        if (this.playerPage.containsKey(player.getUniqueId())) return this.playerPage.get(player.getUniqueId());

        return 1;
    }

    public void nextPage(Player player) {
        setPage(player, getPage(player) + 1);
    }

    public void backPage(Player player) {
        setPage(player, getPage(player) - 1);
    }

    public static int getMaxPage() {
        int maxPage = 1;
        int amount = JavaPlugin.getPlugin(CrazyVouchers.class).getCrazyHandler().getVouchers().size();
        for (; amount > 36; amount -= 36, maxPage++);
        return maxPage;
    }
}