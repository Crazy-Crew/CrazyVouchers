package com.badbones69.crazyvouchers.api;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.utils.ItemUtils;
import com.ryderbelserion.fusion.core.api.enums.FileAction;
import com.ryderbelserion.fusion.core.api.utils.FileUtils;
import com.ryderbelserion.fusion.paper.api.builders.items.ItemBuilder;
import com.ryderbelserion.fusion.paper.files.FileManager;
import com.ryderbelserion.fusion.paper.files.types.PaperCustomFile;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrazyManager {

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NotNull final Path dataPath = this.plugin.getDataPath();

    private @NotNull final SettingsManager config = ConfigManager.getConfig();

    private @NotNull final FileManager fileManager = this.plugin.getFileManager();

    private @NotNull final ComponentLogger logger = this.plugin.getComponentLogger();

    private @NotNull final List<Voucher> vouchers = new ArrayList<>();
    private @NotNull final List<VoucherCode> voucherCodes = new ArrayList<>();

    private @NotNull final List<String> brokenVouchers = new ArrayList<>();
    private @NotNull final List<String> brokenVoucherCodes = new ArrayList<>();

    public void load(final boolean isMigrator) {
        // Used for when wanting to put in fake vouchers.
        // for(int i = 1; i <= 400; i++) vouchers.add(new Voucher(i));

        if (!isMigrator) {
            loadExamples();
        }

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
                for (final Path code : getCodesList()) {
                    @Nullable final PaperCustomFile file = this.fileManager.getPaperCustomFile(code);

                    if (file != null) {
                        final YamlConfiguration configuration = file.getConfiguration();

                        if (configuration != null) {
                            this.voucherCodes.add(new VoucherCode(configuration, file.getPrettyName()));
                        } else {
                            this.logger.warn("Could not load code configuration for {}", code);

                            this.brokenVouchers.add(file.getFileName());
                        }
                    } else {
                        this.logger.warn("The code file named {} could not be found in the cache", code);

                        this.brokenVouchers.add(code.getFileName().toString());
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
                for (final Path voucher : getVouchersList()) {
                    @Nullable final PaperCustomFile file = this.fileManager.getPaperCustomFile(voucher);

                    if (file != null) {
                        final YamlConfiguration configuration = file.getConfiguration();

                        if (configuration != null) {
                            this.vouchers.add(new Voucher(configuration, file.getPrettyName()));
                        } else {
                            this.logger.warn("Could not load voucher configuration for {}", voucher);

                            this.brokenVouchers.add(file.getFileName());
                        }
                    } else {
                        this.logger.warn("The voucher file named {} could not be found in the cache", voucher);

                        this.brokenVouchers.add(voucher.getFileName().toString());
                    }
                }
            }
        }
    }

    public void reload() {
        this.vouchers.clear();
        this.voucherCodes.clear();

        load(false);
    }

    public void loadExamples() {
        if (this.config.getProperty(ConfigKeys.update_examples_folder)) {
            final List<FileAction> actions = new ArrayList<>();

            actions.add(FileAction.DELETE_FILE);
            actions.add(FileAction.EXTRACT_FOLDER);

            FileUtils.extract("vouchers", this.dataPath.resolve("examples"), actions);
            FileUtils.extract("codes", this.dataPath.resolve("examples"), actions);
            FileUtils.extract("locale", this.dataPath.resolve("examples"), actions);

            actions.remove(FileAction.EXTRACT_FOLDER);

            List.of(
                    "config.yml",
                    "codes.yml",
                    "data.yml",
                    "users.yml",
                    "vouchers.yml"
            ).forEach(file -> FileUtils.extract(file, this.dataPath.resolve("examples"), actions));
        }
    }

    public @NotNull final List<Path> getVouchersList() {
        return FileUtils.getFiles(this.dataPath.resolve("vouchers"), ".yml");
    }

    public @NotNull final List<Path> getCodesList() {
        return FileUtils.getFiles(this.dataPath.resolve("codes"), ".yml");
    }
    
    public @NotNull final List<Voucher> getVouchers() {
        return Collections.unmodifiableList(this.vouchers);
    }

    public @NotNull final List<String> getBrokenVouchers() {
        return this.brokenVouchers;
    }

    public @NotNull final List<VoucherCode> getVoucherCodes() {
        return Collections.unmodifiableList(this.voucherCodes);
    }

    public @NotNull final List<String> getBrokenVoucherCodes() {
        return this.brokenVoucherCodes;
    }

    public @Nullable Voucher getVoucher(@NotNull final String voucherName) {
        if (voucherName.isEmpty()) return null;

        for (final Voucher voucher : getVouchers()) {
            if (voucher.getStrippedName().equalsIgnoreCase(voucherName)) {
                return voucher;
            }
        }

        return null;
    }
    
    public boolean isVoucherName(@NotNull final String voucherName) {
        if (voucherName.isEmpty()) return false;

        for (final Voucher voucher : getVouchers()) {
            if (voucher.getStrippedName().equalsIgnoreCase(voucherName)) return false;
        }

        return true;
    }
    
    public @Nullable VoucherCode getVoucherCode(@NotNull final String voucherName) {
        if (voucherName.isEmpty()) return null;

        for (final VoucherCode voucher : getVoucherCodes()) {
            if (voucher.getCode().equalsIgnoreCase(voucherName)) return voucher;
        }

        return null;
    }
    
    public boolean isVoucherCode(@NotNull final String voucherCode) {
        if (voucherCode.isEmpty()) return false;

        for (final VoucherCode voucher : getVoucherCodes()) {
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

    public @Nullable Voucher getVoucherFromItem(@NotNull final ItemStack item) {
        if (item.getType() == Material.AIR) return null;

        final PersistentDataContainerView container = item.getPersistentDataContainer();

        Voucher voucher = null;

        if (container.has(PersistentKeys.voucher_item.getNamespacedKey())) {
            final String voucherName = container.getOrDefault(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, "");

            if (!voucherName.isEmpty()) {
                voucher = getVoucher(voucherName);
            }
        }

        return voucher;
    }

    public @NotNull String getArgument(@NotNull final ItemStack item, @NotNull final Voucher voucher) {
        if (item.getType() == Material.AIR || !voucher.usesArguments()) return "";

        final PersistentDataContainerView container = item.getPersistentDataContainer();

        if (container.has(PersistentKeys.voucher_item.getNamespacedKey()) && container.has(PersistentKeys.voucher_arg.getNamespacedKey())) {
            final String arg = container.getOrDefault(PersistentKeys.voucher_arg.getNamespacedKey(), PersistentDataType.STRING, "");
            final String voucherName = container.getOrDefault(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, "");

            if (!voucherName.isEmpty() && voucherName.equalsIgnoreCase(voucher.getName())) {
                return arg;
            }
        }

        return "";
    }

    public @NotNull List<ItemBuilder> getItems(@NotNull final FileConfiguration file, @NotNull final String voucher) {
        return ItemUtils.convertStringList(file.getStringList("voucher.items"), voucher);
    }
}