package com.badbones69.vouchers.api;

import com.badbones69.vouchers.Vouchers;
import com.badbones69.vouchers.api.objects.Voucher;
import de.tr7zw.changeme.nbtapi.NBTItem;
import com.badbones69.vouchers.api.objects.VoucherCode;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class CrazyManager {
    
    private final static ArrayList<Voucher> vouchers = new ArrayList<>();
    private final static ArrayList<VoucherCode> voucherCodes = new ArrayList<>();

    /**
     * The Vouchers plugin.
     */
    private Vouchers plugin;

    /**
     * Get the Vouchers Plugin.
     * @return The Vouchers Plugin object.
     */
    public Vouchers getPlugin() {
        return plugin;
    }

    /**
     * FileManager object.
     */
    private static final FileManager fileManager = FileManager.getInstance();

    /**
     * The instance of this class.
     */
    private static final CrazyManager instance = new CrazyManager();

    /**
     * Gets the instance of the CrazyCrates class.
     *
     * @return Instance of this class.
     */
    public static CrazyManager getInstance() {
        return instance;
    }

    /**
     * Get the file manager that controls all yml files.
     *
     * @return The FileManager that controls all yml files.
     */
    public static FileManager getFileManager() {
        return fileManager;
    }

    public void loadPlugin(Vouchers plugin) {
        this.plugin = plugin;
    }
    
    public CrazyManager load() {
        vouchers.clear();
        voucherCodes.clear();

        // Used for when wanting to put in fake vouchers.
        // for(int i = 1; i <= 400; i++) vouchers.add(new Voucher(i));

        for (String voucherName : FileManager.Files.CONFIG.getFile().getConfigurationSection("Vouchers").getKeys(false)) {
            vouchers.add(new Voucher(voucherName));
        }

        if (FileManager.Files.VOUCHER_CODES.getFile().contains("Voucher-Codes")) {
            for (String voucherName : FileManager.Files.VOUCHER_CODES.getFile().getConfigurationSection("Voucher-Codes").getKeys(false)) {
                voucherCodes.add(new VoucherCode(voucherName));
            }
        }

        return this;
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

        } catch (Exception ignored) {}
        return null;
    }
    
    public static String getArgument(ItemStack item, Voucher voucher) {
        if (voucher.usesArguments()) {
            // Checks to see if the voucher uses nbt tags.
            NBTItem nbt = new NBTItem(item);

            if (nbt.hasKey("voucher") && nbt.hasKey("argument")) {
                if (nbt.getString("voucher").equalsIgnoreCase(voucher.getName())) {
                    return nbt.getString("argument");
                }
            }
        }

        return null;
    }
    
    public static String replaceRandom(String string) {
        String newString = string;

        if (usesRandom(string)) {
            StringBuilder stringBuilder = new StringBuilder();

            for (String word : newString.split(" ")) {
                if (word.toLowerCase().startsWith("%random%:")) {
                    word = word.toLowerCase().replace("%random%:", "");

                    try {
                        long min = Long.parseLong(word.split("-")[0]);
                        long max = Long.parseLong(word.split("-")[1]);
                        stringBuilder.append(pickNumber(min, max)).append(" ");
                    } catch (Exception e) {
                        stringBuilder.append("1 ");
                    }
                } else {
                    stringBuilder.append(word).append(" ");
                }
            }

            string = stringBuilder.toString();

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