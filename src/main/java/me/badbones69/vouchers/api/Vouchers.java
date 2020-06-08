package me.badbones69.vouchers.api;

import me.badbones69.vouchers.api.FileManager.Files;
import me.badbones69.vouchers.api.itemnbtapi.NBTItem;
import me.badbones69.vouchers.api.objects.Voucher;
import me.badbones69.vouchers.api.objects.VoucherCode;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Vouchers {
    
    private static ArrayList<Voucher> vouchers = new ArrayList<>();
    private static ArrayList<VoucherCode> voucherCodes = new ArrayList<>();
    
    public static void load() {
        vouchers.clear();
        voucherCodes.clear();
        //Used for when wanting to put in fake vouchers.
        //for(int i = 1; i <= 400; i++) vouchers.add(new Voucher(i));
        for (String voucherName : Files.CONFIG.getFile().getConfigurationSection("Vouchers").getKeys(false)) {
            vouchers.add(new Voucher(voucherName));
        }
        if (Files.VOUCHER_CODES.getFile().contains("Voucher-Codes")) {
            for (String voucherName : Files.VOUCHER_CODES.getFile().getConfigurationSection("Voucher-Codes").getKeys(false)) {
                voucherCodes.add(new VoucherCode(voucherName));
            }
        }
    }
    
    public static ArrayList<Voucher> getVouchers() {
        return vouchers;
    }
    
    public static ArrayList<VoucherCode> getVoucherCodes() {
        return voucherCodes;
    }
    
    public static Voucher getVoucher(String voucherName) {
        for (Voucher voucher : getVouchers()) {
            if (voucher.getName().equalsIgnoreCase(voucherName)) {
                return voucher;
            }
        }
        return null;
    }
    
    public static Boolean isVoucherName(String voucherName) {
        for (Voucher voucher : getVouchers()) {
            if (voucher.getName().equalsIgnoreCase(voucherName)) {
                return true;
            }
        }
        return false;
    }
    
    public static VoucherCode getVoucherCode(String voucherName) {
        for (VoucherCode voucher : getVoucherCodes()) {
            if (voucher.getCode().equalsIgnoreCase(voucherName)) {
                return voucher;
            }
        }
        return null;
    }
    
    public static Boolean isVoucherCode(String voucherCode) {
        for (VoucherCode voucher : getVoucherCodes()) {
            if (voucher.isEnabled()) {
                if (voucher.isCaseSensitive()) {
                    if (voucher.getCode().equals(voucherCode)) {
                        return true;
                    }
                } else {
                    if (voucher.getCode().equalsIgnoreCase(voucherCode)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static Voucher getVoucherFromItem(ItemStack item) {
        try {
            NBTItem nbt = new NBTItem(item);
            if (nbt.hasKey("voucher")) {
                return getVoucher(nbt.getString("voucher"));
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    public static String getArgument(ItemStack item, Voucher voucher) {
        if (voucher.usesArguments()) {
            //Checks to see if the voucher uses nbt tags.
            NBTItem nbt = new NBTItem(item);
            if (nbt.hasKey("voucher") && nbt.hasKey("argument")) {
                if (nbt.getString("voucher").equalsIgnoreCase(voucher.getName())) {
                    return nbt.getString("argument");
                }
            }
        }
        return null;
    }
    
    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("Vouchers");
    }
    
    public static String replaceRandom(String string) {
        String newString = string;
        if (usesRandom(string)) {
            string = "";
            for (String word : newString.split(" ")) {
                if (word.toLowerCase().startsWith("%random%:")) {
                    word = word.toLowerCase().replace("%random%:", "");
                    try {
                        long min = Long.parseLong(word.split("-")[0]);
                        long max = Long.parseLong(word.split("-")[1]);
                        string += pickNumber(min, max) + " ";
                    } catch (Exception e) {
                        string += "1 ";
                    }
                } else {
                    string += word + " ";
                }
            }
            newString = string.substring(0, string.length() - 1);
        }
        return newString;
    }
    
    private static boolean usesRandom(String string) {
        return string.toLowerCase().contains("%random%:");
    }
    
    private static long pickNumber(long min, long max) {
        try {
            // new Random() does not have a nextLong(long bound) method.
            return min + ThreadLocalRandom.current().nextLong(max - min);
        } catch (IllegalArgumentException e) {
            return min;
        }
    }
    
}