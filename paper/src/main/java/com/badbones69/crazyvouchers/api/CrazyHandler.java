package com.badbones69.crazyvouchers.api;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.v2.GenericVoucher;
import com.ryderbelserion.vital.common.util.FileUtil;
import com.ryderbelserion.vital.files.FileManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrazyHandler {

    private final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final @NotNull FileManager fileManager = this.plugin.getFileManager();

    private final Set<GenericVoucher> vouchers = new HashSet<>();

    private List<String> files = new ArrayList<>();

    public void load() {
        this.files = FileUtil.getFiles(this.plugin.getDataFolder().toPath().resolve("vouchers"), "vouchers", ".yml", true);

        // Clear only if not empty.
        if (!this.vouchers.isEmpty()) {
            this.vouchers.clear();
        }

        for (String voucherName : this.files) {
            FileConfiguration configuration = this.fileManager.getCustomFile(voucherName.replace(".yml", "")).getConfiguration();

            ConfigurationSection section = configuration.getConfigurationSection("voucher");

            if (section == null) {
                this.plugin.getLogger().warning("Failed to load " + voucherName);

                continue;
            }

            this.vouchers.add(new GenericVoucher(section, voucherName));
        }
    }

    /**
     * Gets a voucher from the set.
     *
     * @param voucherName the name of the voucher.
     * @return the voucher object.
     */
    public GenericVoucher getVoucher(String voucherName) {
        GenericVoucher voucher = null;

        for (GenericVoucher key : this.vouchers) {
            if (key.getFileName().equalsIgnoreCase(voucherName)) {
                voucher = key;

                break;
            }
        }

        return voucher;
    }

    /**
     * Get a voucher from item.
     *
     * @param itemMeta the item meta of the item.
     * @return the voucher
     */
    public GenericVoucher getVoucherFromItem(ItemMeta itemMeta) {
        return getVoucher(itemMeta.getPersistentDataContainer().get(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING));
    }

    /**
     * Checks if item is a voucher.
     *
     * @param itemMeta the item meta of the item.
     * @return true or false.
     */
    public boolean isVoucher(ItemMeta itemMeta) {
        return itemMeta.getPersistentDataContainer().has(PersistentKeys.voucher_item.getNamespacedKey());
    }

    /**
     * @return a list of files from the vouchers directory.
     */
    public List<String> getVoucherFiles() {
        return Collections.unmodifiableList(this.files);
    }

    /**
     * @return an unmodifiable set of vouchers.
     */
    public Set<GenericVoucher> getVouchers() {
        return Collections.unmodifiableSet(this.vouchers);
    }
}