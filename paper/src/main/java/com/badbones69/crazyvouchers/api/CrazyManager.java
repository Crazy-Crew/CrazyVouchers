package com.badbones69.crazyvouchers.api;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.utils.ItemUtils;
import com.ryderbelserion.fusion.core.managers.files.FileType;
import com.ryderbelserion.fusion.core.utils.FileUtils;
import com.ryderbelserion.fusion.paper.api.builder.items.modern.ItemBuilder;
import com.ryderbelserion.fusion.paper.files.LegacyCustomFile;
import com.ryderbelserion.fusion.paper.files.LegacyFileManager;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CrazyManager {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final SettingsManager config = ConfigManager.getConfig();

    private final LegacyFileManager fileManager = this.plugin.getFileManager();

    private final ComponentLogger logger = this.plugin.getComponentLogger();

    private final List<Voucher> vouchers = new ArrayList<>();
    private final List<VoucherCode> voucherCodes = new ArrayList<>();

    private final List<String> brokenVouchers = new ArrayList<>();
    private final List<String> brokenVoucherCodes = new ArrayList<>();

    public void load() {
        // Used for when wanting to put in fake vouchers.
        // for(int i = 1; i <= 400; i++) vouchers.add(new Voucher(i));

        loadVouchers();
        loadCodes();
    }

    public void loadCodes() {
        final FileSystem type = this.config.getProperty(ConfigKeys.file_system);

        switch (type) {
            case SINGLE -> {
                final FileConfiguration configuration = FileKeys.codes.getConfiguration();

                if (configuration == null) return;

                final ConfigurationSection section = configuration.getConfigurationSection("voucher-codes");

                if (section == null) return;

                for (final String code : section.getKeys(false)) {
                    try {
                        this.voucherCodes.add(new VoucherCode(configuration, code));
                    } catch (Exception exception) {
                        this.brokenVouchers.add(code);
                    }
                }
            }

            case MULTIPLE -> {
                for (final String code : getCodesList()) {
                    @Nullable final LegacyCustomFile file = this.fileManager.getFile(code, FileType.YAML);

                    if (file != null) {
                        final YamlConfiguration configuration = file.getConfiguration();

                        if (configuration != null) {
                            this.voucherCodes.add(new VoucherCode(configuration, code));
                        } else {
                            this.logger.warn("Could not load code configuration for {}", code);
                            this.brokenVoucherCodes.add(code);
                        }
                    } else {
                        this.logger.warn("The code file named {} could not be found in the cache", code);
                        this.brokenVoucherCodes.add(code);
                    }
                }
            }
        }
    }

    public void loadVouchers() {
        final FileSystem type = this.config.getProperty(ConfigKeys.file_system);

        switch (type) {
            case SINGLE -> {
                final FileConfiguration configuration = FileKeys.vouchers.getConfiguration();

                if (configuration == null) return;

                final ConfigurationSection section = configuration.getConfigurationSection("vouchers");

                if (section == null) return;

                for (final String voucher : section.getKeys(false)) {
                    try {
                        this.vouchers.add(new Voucher(configuration, voucher));
                    } catch (final Exception exception) {
                        this.brokenVouchers.add(voucher);
                    }
                }
            }

            case MULTIPLE -> {
                for (final String voucher : getVouchersList()) {
                    @Nullable final LegacyCustomFile file = this.fileManager.getFile(voucher, FileType.YAML);

                    if (file != null) {
                        final YamlConfiguration configuration = file.getConfiguration();

                        if (configuration != null) {
                            this.vouchers.add(new Voucher(configuration, voucher));
                        } else {
                            this.logger.warn("Could not load voucher configuration for {}", voucher);
                            this.brokenVouchers.add(voucher);
                        }
                    } else {
                        this.logger.warn("The voucher file named {} could not be found in the cache", voucher);
                        this.brokenVouchers.add(voucher);
                    }
                }
            }
        }
    }

    public void reload() {
        this.vouchers.clear();
        this.voucherCodes.clear();

        if (this.config.getProperty(ConfigKeys.update_examples_folder)) {
            final Path path = this.plugin.getDataFolder().toPath();

            List.of(
                    "codes.yml",
                    "data.yml",
                    "users.yml",
                    "vouchers.yml"
            ).forEach(file -> FileUtils.extract(file, path.resolve("examples"), true, false));

            FileUtils.extract("vouchers", path.resolve("examples"), true, false);
            FileUtils.extract("codes", path.resolve("examples"), true, false);
            FileUtils.extract("locale", path.resolve("examples"), true, false);
        }

        load();
    }

    /**
     * @return A list of crate names.
     */
    public final List<String> getVouchersList() {
        return FileUtils.getNamesWithoutExtension("vouchers", this.plugin.getDataPath(), ".yml");
    }

    /**
     * @return A list of crate names.
     */
    public final List<String> getCodesList() {
        return FileUtils.getNamesWithoutExtension("codes", this.plugin.getDataPath(), ".yml");
    }
    
    public final List<Voucher> getVouchers() {
        return Collections.unmodifiableList(this.vouchers);
    }

    public final List<String> getBrokenVouchers() {
        return this.brokenVouchers;
    }

    public final List<VoucherCode> getVoucherCodes() {
        return Collections.unmodifiableList(this.voucherCodes);
    }

    public final List<String> getBrokenVoucherCodes() {
        return this.brokenVoucherCodes;
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
        Voucher voucher = null;

        if (item.getType() != Material.AIR) {
            final PersistentDataContainerView container = item.getPersistentDataContainer();

            if (container.has(PersistentKeys.voucher_item.getNamespacedKey())) {
                final String voucherName = container.get(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING);

                voucher = getVoucher(voucherName);
            }
        }

        return voucher;
    }

    public String getArgument(final ItemStack item, final Voucher voucher) {
        if (item.getType() != Material.AIR) {
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
        return ItemUtils.convertStringList(file.getStringList("voucher.items"), voucher);
    }
    
    private boolean usesRandom(String string) {
        return string.toLowerCase().contains("{random}:");
    }
    
    private long pickNumber(long min, long max) {
        try {
            return min + Methods.getRandom().nextLong(max - min);
        } catch (IllegalArgumentException e) {
            return min;
        }
    }
}