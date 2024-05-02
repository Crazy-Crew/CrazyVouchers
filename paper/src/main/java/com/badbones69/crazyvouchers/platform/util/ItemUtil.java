package com.badbones69.crazyvouchers.platform.util;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemUtil {

    private static final CrazyManager crazyManager = JavaPlugin.getPlugin(CrazyVouchers.class).getCrazyManager();

    public static boolean isSimilar(ItemStack itemStack, Voucher voucher) {
        if (!itemStack.hasItemMeta()) return false;

        ItemMeta itemMeta = itemStack.getItemMeta();

        boolean isNewVoucher = itemMeta.getPersistentDataContainer().has(PersistentKeys.voucher_item.getNamespacedKey());

        if (!isNewVoucher) {
            String value = itemMeta.getAsString();

            String[] sections = value.split(",");

            String pair = null;

            for (String key : sections) {
                if (key.contains("voucher")) {
                    pair = key.trim().replaceAll("\\{", "").replaceAll("\"", "");

                    break;
                }
            }

            if (pair == null) {
                return false;
            }

            Bukkit.getLogger().warning("Pair: " + pair.split(":")[1]);

            return voucher.getName().equals(pair.split(":")[1]);
        }

        return true;
    }

    public static boolean isVoucher(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return false;
        }

        return itemStack.getItemMeta().getPersistentDataContainer().has(PersistentKeys.voucher_item.getNamespacedKey());
    }

    public static Voucher getVoucherNameFromOldKey(ItemMeta itemMeta) {
        // Get the item meta as a string
        String value = itemMeta.getAsString();

        String[] sections = value.split(",");

        String pair = null;

        for (String key : sections) {
            if (key.contains("voucher")) {
                pair = key.trim().replaceAll("\\{", "").replaceAll("\"", "");

                break;
            }
        }

        if (pair == null) {
            return null;
        }

        return crazyManager.getVoucher(pair.split(":")[1]);
    }
}