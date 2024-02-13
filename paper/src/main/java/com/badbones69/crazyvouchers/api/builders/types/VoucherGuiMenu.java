package com.badbones69.crazyvouchers.api.builders.types;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.builders.InventoryBuilder;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.ItemBuilder;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;

public class VoucherGuiMenu extends InventoryBuilder {

    private final HashMap<UUID, Integer> playerPage = new HashMap<>();

    public VoucherGuiMenu(Player player, int size, String title) {
        super(player, size, title);
    }

    @Override
    public InventoryBuilder build() {
        setDefaultItems();

        for (Voucher voucher : getPageVouchers(getPage())) {
            build(voucher);
        }

        return this;
    }

    private void build(Voucher voucher) {
        ItemStack itemStack = voucher.buildItem();

        ItemMeta itemMeta = itemStack.getItemMeta();

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        container.set(PersistentKeys.voucher_item_admin.getNamespacedKey(), PersistentDataType.STRING, voucher.getName());

        itemStack.setItemMeta(itemMeta);

        getInventory().setItem(getInventory().firstEmpty(), itemStack);
    }

    @Override
    public InventoryBuilder build(int pageNumber) {
        setDefaultItems();

        for (Voucher voucher : getPageVouchers(pageNumber)) {
            build(voucher);
        }

        return this;
    }

    private void setDefaultItems() {
        for (int i : Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 49, 51, 52, 53)) {
            getInventory().setItem(i, new ItemBuilder()
                    .setMaterial(Material.BLUE_STAINED_GLASS_PANE)
                    .setDamage(0)
                    .setName(" ")
                    .build());
        }

        int page = getPage(getPlayer());
        int maxPage = getMaxPage();

        if (page == 1) {
            getInventory().setItem(48, new ItemBuilder()
                    .setMaterial(Material.GRAY_STAINED_GLASS_PANE)
                    .setDamage(0)
                    .setName(" ")
                    .build());
        } else {
            ItemStack backButton = new ItemBuilder().setMaterial(Material.FEATHER).setName("&6&l<< Back").addLore("&7&lPage: &b" + (getPage(getPlayer()) - 1)).build();

            ItemMeta itemMeta = backButton.getItemMeta();

            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            container.set(PersistentKeys.back_button.getNamespacedKey(), PersistentDataType.STRING, "none");

            backButton.setItemMeta(itemMeta);

            getInventory().setItem(48, backButton);
        }

        if (page == maxPage) {
            getInventory().setItem(50, new ItemBuilder()
                    .setMaterial(Material.GRAY_STAINED_GLASS_PANE)
                    .setDamage(0)
                    .setName(" ")
                    .build());
        } else {
            ItemStack nextButton = new ItemBuilder().setMaterial(Material.FEATHER).setName("&6&lNext >>").addLore("&7&lPage: &b" + (getPage(getPlayer()) + 1)).build();

            ItemMeta itemMeta = nextButton.getItemMeta();

            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            container.set(PersistentKeys.next_button.getNamespacedKey(), PersistentDataType.STRING, "none");

            nextButton.setItemMeta(itemMeta);

            getInventory().setItem(50, nextButton);
        }
    }

    private List<Voucher> getPageVouchers(Integer page) {
        List<Voucher> list = this.plugin.getCrazyManager().getVouchers();
        List<Voucher> vouchers = new ArrayList<>();
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
        int amount = CrazyVouchers.get().getCrazyManager().getVouchers().size();
        for (; amount > 36; amount -= 36, maxPage++) ;
        return maxPage;
    }
}