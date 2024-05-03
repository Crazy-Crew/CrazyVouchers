package com.badbones69.crazyvouchers.api;

import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrazyManager {

    private final List<VoucherCode> voucherCodes = new ArrayList<>();
    
    public void load() {
        // Used for when wanting to put in fake vouchers.
        // for(int i = 1; i <= 400; i++) vouchers.add(new Voucher(i));

        loadVouchers();
    }

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