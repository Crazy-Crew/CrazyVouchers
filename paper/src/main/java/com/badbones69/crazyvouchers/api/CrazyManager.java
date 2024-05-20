package com.badbones69.crazyvouchers.api;

import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyvouchers.common.config.ConfigManager;
import us.crazycrew.crazyvouchers.common.config.types.ConfigKeys;
import us.crazycrew.crazyvouchers.api.MetricsHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class CrazyManager {

    @NotNull
    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final ConfigManager configManager = this.plugin.getCrazyHandler().getConfigManager();

    private final ArrayList<Voucher> vouchers = new ArrayList<>();
    private final ArrayList<VoucherCode> voucherCodes = new ArrayList<>();
    
    public void load() {
        // Used for when wanting to put in fake vouchers.
        // for(int i = 1; i <= 400; i++) vouchers.add(new Voucher(i));

        loadVouchers();
    }

    private void loadVouchers() {
        boolean loadOldWay = this.configManager.getConfig().getProperty(ConfigKeys.mono_file);

        if (loadOldWay) {
            FileConfiguration vouchers = FileManager.Files.vouchers.getFile();

            for (String voucherName : vouchers.getConfigurationSection("vouchers").getKeys(false)) {
                this.vouchers.add(new Voucher(vouchers, voucherName));
            }

            FileConfiguration voucherCodes = FileManager.Files.voucher_codes.getFile();

            if (voucherCodes.contains("voucher-codes")) {
                for (String voucherName : voucherCodes.getConfigurationSection("voucher-codes").getKeys(false)) {
                    this.voucherCodes.add(new VoucherCode(voucherCodes, voucherName));
                }
            }

            return;
        }

        for (String voucherName : this.plugin.getFileManager().getVouchers()) {
            try {
                FileConfiguration file = this.plugin.getFileManager().getFile(voucherName).getFile();
                this.vouchers.add(new Voucher(file, voucherName));
            } catch (Exception exception) {
                this.plugin.getLogger().log(Level.SEVERE, "There was an error while loading the " + voucherName + ".yml file.", exception);
            }
        }

        for (String voucherCode : this.plugin.getFileManager().getCodes()) {
    public void loadVouchers() {
        /*
        for (String voucherCode : getVoucherCodeFiles()) {
            try {
                FileConfiguration file = this.fileManager.getCustomFile(voucherCode).getConfiguration();

                this.voucherCodes.add(new VoucherCode(file, voucherCode));
            } catch (Exception exception) {
                this.plugin.getLogger().log(Level.SEVERE,"There was an error while loading the " + voucherCode + ".yml file.", exception);
            }
        }*/
    }

    public void reload(boolean serverStop) {
        MetricsHandler metricsHandler = this.plugin.getCrazyHandler().getMetrics();

        if (serverStop) {
            metricsHandler.stop();
        }

        this.configManager.reload();

        boolean metrics = this.configManager.getConfig().getProperty(ConfigKeys.toggle_metrics);

        if (metrics) {
            metricsHandler.start();
        } else {
            metricsHandler.stop();
        }

    public void reload() {
        ConfigManager.reload();

        loadVouchers();
    }

    public List<VoucherCode> getVoucherCodes() {
        return Collections.unmodifiableList(this.voucherCodes);
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
}