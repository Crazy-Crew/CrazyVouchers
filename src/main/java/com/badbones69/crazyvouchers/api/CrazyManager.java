package com.badbones69.crazyvouchers.api;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.other.ItemBuilder;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.ryderbelserion.vital.core.util.FileUtil;
import com.ryderbelserion.vital.paper.files.config.CustomFile;
import de.tr7zw.changeme.nbtapi.NBTItem;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class CrazyManager {

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();
    
    private final List<Voucher> vouchers = new ArrayList<>();
    private final List<VoucherCode> voucherCodes = new ArrayList<>();
    
    public void load() {
        // Used for when wanting to put in fake vouchers.
        // for(int i = 1; i <= 400; i++) vouchers.add(new Voucher(i));

        loadVouchers();
    }

    private void loadVouchers() {
        boolean loadOldWay = ConfigManager.getConfig().getProperty(ConfigKeys.mono_file);

        if (loadOldWay) {
            FileConfiguration vouchers = Files.vouchers.getConfiguration();

            for (String voucherName : vouchers.getConfigurationSection("vouchers").getKeys(false)) {
                this.vouchers.add(new Voucher(vouchers, voucherName));
            }

            FileConfiguration voucherCodes = Files.voucher_codes.getConfiguration();

            if (voucherCodes.contains("voucher-codes")) {
                for (String voucherName : voucherCodes.getConfigurationSection("voucher-codes").getKeys(false)) {
                    this.voucherCodes.add(new VoucherCode(voucherCodes, voucherName));
                }
            }

            return;
        }

        for (String voucherName : getVouchersList()) {
            try {
                @Nullable CustomFile file = this.plugin.getFileManager().getCustomFile(voucherName);

                if (file != null) {
                    this.vouchers.add(new Voucher(file.getConfiguration(), voucherName));
                }
            } catch (Exception exception) {
                this.plugin.getLogger().log(Level.SEVERE, "There was an error while loading the " + voucherName + ".yml file.", exception);
            }
        }

        for (String voucherCode : getCodesList()) {
            try {
                @Nullable CustomFile file = this.plugin.getFileManager().getCustomFile(voucherCode);

                if (file != null) {
                    this.voucherCodes.add(new VoucherCode(file.getConfiguration(), voucherCode));
                }
            } catch (Exception exception) {
                this.plugin.getLogger().log(Level.SEVERE,"There was an error while loading the " + voucherCode + ".yml file.", exception);
            }
        }
    }

    public void reload() {
        this.vouchers.clear();
        this.voucherCodes.clear();

        loadVouchers();
    }

    /**
     * @return A list of crate names.
     */
    public final List<String> getVouchersList() {
        return FileUtil.getFiles(new File(this.plugin.getDataFolder(), "vouchers"), ".yml");
    }

    /**
     * @return A list of crate names.
     */
    public final List<String> getCodesList() {
        return FileUtil.getFiles(new File(this.plugin.getDataFolder(), "codes"), ".yml");
    }
    
    public final List<Voucher> getVouchers() {
        return Collections.unmodifiableList(this.vouchers);
    }
    
    public final List<VoucherCode> getVoucherCodes() {
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

    public Voucher getVoucherFromItem(final ItemStack item) {
        final PersistentDataContainerView container = item.getPersistentDataContainer();

        Voucher voucher = null;

        if (container.has(PersistentKeys.voucher_item.getNamespacedKey())) {
            final String voucherName = container.get(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING);

            voucher = getVoucher(voucherName);
        } else {
            NBTItem nbt = new NBTItem(item); // this section related to nbt items is deprecated, and marked for removal

            if (nbt.hasTag("voucher")) {
                voucher = getVoucher(nbt.getString("voucher"));
            }
        }

        return voucher;
    }

    public String getArgument(final ItemStack item, final Voucher voucher) {
        final PersistentDataContainerView container = item.getPersistentDataContainer();

        if (voucher.usesArguments()) {
            if (container.has(PersistentKeys.voucher_item.getNamespacedKey()) && container.has(PersistentKeys.voucher_arg.getNamespacedKey())) {
                final String arg = container.get(PersistentKeys.voucher_arg.getNamespacedKey(), PersistentDataType.STRING);
                final String voucherName = container.get(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING);

                if (voucherName != null) {
                    if (voucherName.equalsIgnoreCase(voucher.getName())) {
                        return arg;
                    }
                }
            } else {
                NBTItem nbt = new NBTItem(item); // this section related to nbt items is deprecated, and marked for removal

                if (nbt.hasTag("voucher") && nbt.hasTag("argument")) {
                    if (nbt.getString("voucher").equalsIgnoreCase(voucher.getName())) return nbt.getString("argument");
                }
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
            return min + ThreadLocalRandom.current().nextLong(max - min);
        } catch (IllegalArgumentException e) {
            return min;
        }
    }
}