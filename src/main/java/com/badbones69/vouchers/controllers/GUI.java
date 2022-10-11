package com.badbones69.vouchers.controllers;

import com.badbones69.vouchers.Vouchers;
import com.badbones69.vouchers.api.objects.Voucher;
import com.badbones69.vouchers.Methods;
import com.badbones69.vouchers.api.CrazyManager;
import com.badbones69.vouchers.api.objects.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class GUI implements Listener {
    
    private final Vouchers plugin = Vouchers.getPlugin();
    
    private final CrazyManager crazyManager = plugin.getCrazyManager();

    private final Methods methods = plugin.getMethods();
    
    private final String inventoryName = methods.color("&8&l&nVouchers");
    private final HashMap<UUID, Integer> playerPage = new HashMap<>();
    
    public void openGUI(Player player) {
        int page = getPage(player);
        Inventory inv = Bukkit.createInventory(null, 54, inventoryName);
        setDefaultItems(player, inv);

        for (Voucher i : getPageVouchers(page)) {
            inv.setItem(inv.firstEmpty(), i.buildItem());
        }

        player.openInventory(inv);
    }
    
    public void openGUI(Player player, int pageNumber) {
        setPage(player, pageNumber);
        pageNumber = getPage(player);
        Inventory inv = Bukkit.createInventory(null, 54, inventoryName);
        setDefaultItems(player, inv);

        for (Voucher i : getPageVouchers(pageNumber)) {
            inv.setItem(inv.firstEmpty(), i.buildItem());
        }

        player.openInventory(inv);
    }
    
    @EventHandler
    public void invClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();

        if (inv != null) {
            if (e.getView().getTitle().equals(inventoryName)) {
                e.setCancelled(true);

                if (e.getRawSlot() < 54) {
                    if (e.getCurrentItem() != null) {
                        if (item.hasItemMeta()) {
                            if (item.getItemMeta().hasDisplayName()) {
                                if (item.getItemMeta().getDisplayName().equals(methods.color("&6&l<< Back"))) {
                                    backPage(player);
                                    openGUI(player);
                                    return;
                                } else if (item.getItemMeta().getDisplayName().equals(methods.color("&6&lNext >>"))) {
                                    nextPage(player);
                                    openGUI(player);
                                    return;
                                }
                            }
                        }

                        for (Voucher voucher : crazyManager.getVouchers()) {
                            if (methods.isSimilar(item, voucher.buildItem())) {
                                player.getInventory().addItem(item);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private int getPage(Player player) {
        if (playerPage.containsKey(player.getUniqueId())) return playerPage.get(player.getUniqueId());

        return 1;
    }
    
    private void setPage(Player player, int pageNumber) {
        int max = getMaxPage();

        if (pageNumber < 1) {
            pageNumber = 1;
        } else if (pageNumber >= max) {
            pageNumber = max;
        }

        playerPage.put(player.getUniqueId(), pageNumber);
    }
    
    private void nextPage(Player player) {
        setPage(player, getPage(player) + 1);
    }
    
    private void backPage(Player player) {
        setPage(player, getPage(player) - 1);
    }
    
    public int getMaxPage() {
        int maxPage = 1;
        int amount = crazyManager.getVouchers().size();
        for (; amount > 36; amount -= 36, maxPage++) ;
        return maxPage;
    }
    
    private List<Voucher> getPageVouchers(Integer page) {
        List<Voucher> list = crazyManager.getVouchers();
        List<Voucher> vouchers = new ArrayList<>();
        if (page <= 0) page = 1;
        int max = 36;
        int index = page * max - max;
        int endIndex = index >= list.size() ? list.size() - 1 : index + max;

        for (; index < endIndex; index++) {
            if (index < list.size()) vouchers.add(list.get(index));
        }

        for (; vouchers.size() == 0; page--) {
            if (page <= 0) break;
            index = page * max - max;
            endIndex = index >= list.size() ? list.size() - 1 : index + max;

            for (; index < endIndex; index++) {
                if (index < list.size()) vouchers.add(list.get(index));
            }
        }

        return vouchers;
    }
    
    private void setDefaultItems(Player player, Inventory inv) {

        for (int i : Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 49, 51, 52, 53)) {
            inv.setItem(i, new ItemBuilder()
            .setMaterial(Material.BLUE_STAINED_GLASS_PANE)
            .setDamage(0)
            .setName(" ")
            .build());
        }

        int page = getPage(player);
        int maxPage = getMaxPage();

        if (page == 1) {
            inv.setItem(48, new ItemBuilder()
            .setMaterial(Material.GRAY_STAINED_GLASS_PANE)
            .setDamage(0)
            .setName(" ")
            .build());
        } else {
            inv.setItem(48, new ItemBuilder()
            .setMaterial(Material.FEATHER)
            .setName("&6&l<< Back")
            .addLore("&7&lPage: &b" + (getPage(player) - 1))
            .build());
        }

        if (page == maxPage) {
            inv.setItem(50, new ItemBuilder()
            .setMaterial(Material.GRAY_STAINED_GLASS_PANE)
            .setDamage(0)
            .setName(" ")
            .build());
        } else {
            inv.setItem(50, new ItemBuilder()
            .setMaterial(Material.FEATHER)
            .setName("&6&lNext >>")
            .addLore("&7&lPage: &b" + (getPage(player) + 1))
            .build());
        }
    }
}