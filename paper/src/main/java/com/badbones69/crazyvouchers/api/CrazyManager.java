package com.badbones69.crazyvouchers.api;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.builders.OldBuilder;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import com.badbones69.crazyvouchers.platform.util.ItemUtil;
import com.ryderbelserion.vital.files.FileManager;
import de.tr7zw.changeme.nbtapi.NBTItem;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class CrazyManager {

    private final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final @NotNull FileManager fileManager = this.plugin.getFileManager();
    
    private final List<Voucher> vouchers = new ArrayList<>();
    private final List<VoucherCode> voucherCodes = new ArrayList<>();
    
    public void load() {
        // Used for when wanting to put in fake vouchers.
        // for(int i = 1; i <= 400; i++) vouchers.add(new Voucher(i));

        loadvouchers();
    }

    public void loadvouchers() {
        for (String voucherName : getVouchersFiles()) {
            try {
                FileConfiguration file = this.fileManager.getCustomFile(voucherName).getConfiguration();

                this.vouchers.add(new Voucher(file, voucherName));
            } catch (Exception exception) {
                this.plugin.getLogger().log(Level.SEVERE, "There was an error while loading the " + voucherName + ".yml file.", exception);
            }
        }

        for (String voucherCode : getVoucherCodeFiles()) {
            try {
                FileConfiguration file = this.fileManager.getCustomFile(voucherCode).getConfiguration();

                this.voucherCodes.add(new VoucherCode(file, voucherCode));
            } catch (Exception exception) {
                this.plugin.getLogger().log(Level.SEVERE,"There was an error while loading the " + voucherCode + ".yml file.", exception);
            }
        }
    }

    public void reload() {
        ConfigManager.reload();

        loadvouchers();
    }
    
    public List<Voucher> getVouchers() {
        return Collections.unmodifiableList(this.vouchers);
    }
    
    public List<VoucherCode> getVoucherCodes() {
        return Collections.unmodifiableList(this.voucherCodes);
    }
    
    public Voucher getVoucher(String voucherName) {
        for (Voucher voucher : getVouchers()) {
            if (voucher.getName().equalsIgnoreCase(voucherName)) {
                return voucher;
            }
        }

        return null;
    }
    
    public boolean isVoucherName(String voucherName) {
        for (Voucher voucher : getVouchers()) {
            if (voucher.getName().equalsIgnoreCase(voucherName)) return false;
        }

        return true;
    }
    
    public VoucherCode getVoucherCode(String voucherName) {
        for (VoucherCode voucher : getVoucherCodes()) {
            if (voucher.getCode().equalsIgnoreCase(voucherName)) return voucher;
        }

        return null;
    }
    
    public boolean isVoucherCode(String voucherCode) {
        for (VoucherCode voucher : getVoucherCodes()) {
            if (voucher.isEnabled()) {
                if (voucher.isCaseSensitive()) {
                    if (voucher.getCode().equals(voucherCode)) return true;
                } else {
                    if (voucher.getCode().equalsIgnoreCase(voucherCode)) return true;
                }
            }
        }

        return false;
    }

    public Voucher getVoucherFromItem(ItemStack item) {
        try {
            Voucher name = ItemUtil.getVoucherNameFromOldKey(item.getItemMeta());

            if (ItemUtil.isSimilar(item, name)) {
                if (name != null) {
                    return getVoucher(name.getName());
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    public String getArgument(ItemStack item, Voucher voucher) {
        if (voucher.usesArguments()) {
            // Checks to see if the voucher uses nbt tags.
            NBTItem nbt = new NBTItem(item);

            if (nbt.hasTag("voucher") && nbt.hasTag("argument")) {
                if (nbt.getString("voucher").equalsIgnoreCase(voucher.getName())) return nbt.getString("argument");
            }
        }

        return null;
    }
    
    public String replaceRandom(String string) {
        String newString = string;

        if (usesRandom(string)) {
            StringBuilder stringBuilder = new StringBuilder();

            for (String word : newString.split(" ")) {
                if (word.toLowerCase().startsWith("{random}:")) {
                    word = word.toLowerCase().replace("{random}:", "");

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

    public List<OldBuilder> getItems(FileConfiguration file, String voucher) {
        return OldBuilder.convertStringList(file.getStringList("voucher.items"), voucher);
    }
    
    private boolean usesRandom(String string) {
        return string.toLowerCase().contains("{random}:");
    }
    
    private long pickNumber(long min, long max) {
        try {
            // new Random() does not have a nextLong(long bound) method.
            return min + new Random().nextLong(max - min);
        } catch (IllegalArgumentException e) {
            return min;
        }
    }

    /**
     * @return A list of voucher names.
     */
    private List<String> getVouchersFiles() {
        return getFiles(new File(this.plugin.getDataFolder(), "/vouchers"));
    }

    /**
     * @return A list of voucher codes.
     */
    private List<String> getVoucherCodeFiles() {
        return getFiles(new File(this.plugin.getDataFolder(), "/codes"));
    }

    /**
     * @param dir the directory to look.
     *
     * @return the files
     */
    private List<String> getFiles(File dir) {
        List<String> files = new ArrayList<>();

        String[] file = dir.list();

        if (file != null) {
            File[] filesList = dir.listFiles();

            if (filesList != null) {
                for (File directory : filesList) {
                    if (directory.isDirectory()) {
                        String[] folder = directory.list();

                        if (folder != null) {
                            for (String name : folder) {
                                if (!name.endsWith(".yml")) continue;

                                files.add(name.replaceAll(".yml", ""));
                            }
                        }
                    }
                }
            }

            for (String name : file) {
                if (!name.endsWith(".yml")) continue;

                files.add(name.replaceAll(".yml", ""));
            }
        }

        return Collections.unmodifiableList(files);
    }
}