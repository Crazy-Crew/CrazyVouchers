package me.badbones69.vouchers.controllers;

import me.badbones69.vouchers.Methods;
import me.badbones69.vouchers.api.VouchersManager;
import me.badbones69.vouchers.api.enums.Version;
import me.badbones69.vouchers.api.objects.ItemBuilder;
import me.badbones69.vouchers.api.objects.Voucher;
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
    
    private static String inventoryName = Methods.color("&8&l&nVouchers");
    private static HashMap<UUID, Integer> playerPage = new HashMap<>();
    
    public static void openGUI(Player player) {
        int page = getPage(player);
        Inventory inv = Bukkit.createInventory(null, 54, inventoryName);
        setDefaultItems(player, inv);
        for (Voucher i : getPageVouchers(page)) {
            inv.setItem(inv.firstEmpty(), i.buildItem());
        }
        player.openInventory(inv);
    }
    
    public static void openGUI(Player player, int pageNumber) {
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
                                if (item.getItemMeta().getDisplayName().equals(Methods.color("&6&l<< Back"))) {
                                    backPage(player);
                                    openGUI(player);
                                    return;
                                } else if (item.getItemMeta().getDisplayName().equals(Methods.color("&6&lNext >>"))) {
                                    nextPage(player);
                                    openGUI(player);
                                    return;
                                }
                            }
                        }
                        for (Voucher voucher : VouchersManager.getVouchers()) {
                            if (Methods.isSimilar(item, voucher.buildItem())) {
                                player.getInventory().addItem(item);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static int getPage(Player player) {
        if (playerPage.containsKey(player.getUniqueId())) {
            return playerPage.get(player.getUniqueId());
        }
        return 1;
    }
    
    private static void setPage(Player player, int pageNumber) {
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
    
    public static int getMaxPage() {
        int maxPage = 1;
        int amount = VouchersManager.getVouchers().size();
        for (; amount > 36; amount -= 36, maxPage++) ;
        return maxPage;
    }
    
    private static List<Voucher> getPageVouchers(Integer page) {
        List<Voucher> list = VouchersManager.getVouchers();
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
    
    private static void setDefaultItems(Player player, Inventory inv) {
        boolean isNew = Version.isNewer(Version.v1_12_R1);
        for (int i : Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 49, 51, 52, 53)) {
            inv.setItem(i, new ItemBuilder()
            .setMaterial(isNew ? Material.BLUE_STAINED_GLASS_PANE : Material.matchMaterial("STAINED_GLASS_PANE"))
            .setDamage(isNew ? 0 : (short) 11)
            .setName(" ")
            .build());
        }
        int page = getPage(player);
        int maxPage = getMaxPage();
        if (page == 1) {
            inv.setItem(48, new ItemBuilder()
            .setMaterial(isNew ? Material.GRAY_STAINED_GLASS_PANE : Material.matchMaterial("STAINED_GLASS_PANE"))
            .setDamage(isNew ? 0 : (short) 7)
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
            .setMaterial(isNew ? Material.GRAY_STAINED_GLASS_PANE : Material.matchMaterial("STAINED_GLASS_PANE"))
            .setDamage(isNew ? 0 : (short) 7)
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