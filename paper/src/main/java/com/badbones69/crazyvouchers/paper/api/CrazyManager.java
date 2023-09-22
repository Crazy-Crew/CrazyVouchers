package com.badbones69.crazyvouchers.paper.api;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import com.badbones69.crazyvouchers.paper.api.objects.ItemBuilder;
import com.badbones69.crazyvouchers.paper.api.objects.Voucher;
import com.ryderbelserion.cluster.bukkit.utils.LegacyLogger;
import de.tr7zw.changeme.nbtapi.NBTItem;
import com.badbones69.crazyvouchers.paper.api.objects.VoucherCode;
import org.bukkit.configuration.file.FileConfiguration;
import com.badbones69.crazyvouchers.paper.api.FileManager.Files;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import us.crazycrew.crazyenvoys.common.config.types.Config;
import us.crazycrew.crazyvouchers.paper.api.MetricsHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrazyManager {

    private final CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);
    
    private final ArrayList<Voucher> vouchers = new ArrayList<>();
    private final ArrayList<VoucherCode> voucherCodes = new ArrayList<>();
    
    public void load(boolean serverStart) {
        if (serverStart) {

        }

        // Used for when wanting to put in fake vouchers.
        // for(int i = 1; i <= 400; i++) vouchers.add(new Voucher(i));

        loadVouchers();
    }

    private void loadVouchers() {
        for (String voucherName : this.plugin.getFileManager().getAllVoucherItems()) {
            try {
                FileConfiguration file = this.plugin.getFileManager().getFile(voucherName).getFile();
                this.vouchers.add(new Voucher(file, voucherName));
            } catch (Exception exception) {
                LegacyLogger.error("There was an error while loading the " + voucherName + ".yml file.", exception);
            }
        }

        if (Files.voucher_codes.getFile().contains("Voucher-Codes")) {
            for (String voucherName : Files.voucher_codes.getFile().getConfigurationSection("Voucher-Codes").getKeys(false)) {
                this.voucherCodes.add(new VoucherCode(voucherName));
            }
        }
    }

    public void reload(boolean serverStop) {
        MetricsHandler metricsHandler = this.plugin.getCrazyHandler().getMetrics();

        if (serverStop) {
            metricsHandler.stop();
        }

        this.plugin.getCrazyHandler().getConfigManager().reload();

        boolean metrics = this.plugin.getCrazyHandler().getConfigManager().getConfig().getProperty(Config.toggle_metrics);

        if (metrics) {
            metricsHandler.start();
        } else {
            metricsHandler.stop();
        }

        this.vouchers.clear();
        this.voucherCodes.clear();

        loadVouchers();
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
            NBTItem nbt = new NBTItem(item);

            if (nbt.hasTag("voucher")) return getVoucher(nbt.getString("voucher"));
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

    public List<ItemBuilder> getItems(FileConfiguration file, String voucher) {
        return ItemBuilder.convertStringList(file.getStringList("voucher.items"), voucher);
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
}