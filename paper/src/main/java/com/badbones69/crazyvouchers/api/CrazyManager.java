package com.badbones69.crazyvouchers.api;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.ryderbelserion.fusion.core.api.enums.FileAction;
import com.ryderbelserion.fusion.core.api.utils.FileUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
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

    private @NotNull final FusionPaper fusion = this.plugin.getFusion();

    private @NotNull final Path dataPath = this.plugin.getDataPath();

    private @NotNull final SettingsManager config = ConfigManager.getConfig();

    private @NotNull final FileManager fileManager = this.plugin.getFileManager();

    private @NotNull final ComponentLogger logger = this.plugin.getComponentLogger();

    private @NotNull final List<Voucher> vouchers = new ArrayList<>();
    private @NotNull final List<VoucherCode> voucherCodes = new ArrayList<>();

    private @NotNull final List<String> brokenVouchers = new ArrayList<>();
    private @NotNull final List<String> brokenVoucherCodes = new ArrayList<>();

    public void load(final boolean isMigrator) {
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
                final PaperCustomFile config = FileKeys.codes.getCustomFile();

                if (config == null) {
                    this.logger.warn("The voucher file named {} could not be found in the cache", FileKeys.codes.getName());

                    return;
                }

                if (!config.isLoaded()) {
                    this.logger.warn("The {} was not loaded into memory.", FileKeys.codes.getName());

                    return;
                }

                final FileConfiguration configuration = config.getConfiguration();
                final ConfigurationSection section = configuration.getConfigurationSection("voucher-codes");

                if (section == null) {
                    this.logger.warn("The configuration section we need for {} could not be found.", FileKeys.codes.getName());

                    return;
                }

                for (final String code : section.getKeys(false)) {
                    try {
                        final ConfigurationSection codeSection = section.getConfigurationSection(code);

                        if (codeSection == null) {
                            this.fusion.log("warn", "The section for {} could not be found in voucher-codes.yml", code);

                            continue;
                        }

                        this.voucherCodes.add(new VoucherCode(codeSection, code));
                    } catch (final Exception exception) {
                        this.brokenVoucherCodes.add(code);
                    }
                }
            }

            case MULTIPLE -> {
                for (final Path code : getCodesList()) {
                    final PaperCustomFile file = this.fileManager.getPaperCustomFile(code);

                    if (file == null) {
                        this.logger.warn("The code file named {} could not be found in the cache", code);

                        this.brokenVoucherCodes.add(code.getFileName().toString());

                        continue;
                    }

                    if (!file.isLoaded()) {
                        this.logger.warn("Could not load code configuration for {}", code);

                        this.brokenVoucherCodes.add(file.getFileName());

                        continue;
                    }

                    final ConfigurationSection section = file.getConfiguration().getConfigurationSection("voucher-code");

                    if (section == null) {
                        this.logger.warn("Could not find voucher code configuration section for {}", code);

                        this.brokenVoucherCodes.add(file.getFileName());

                        continue;
                    }

                    this.voucherCodes.add(new VoucherCode(file.getConfiguration(), file.getPrettyName()));
                }
            }
        }
    }

    public void loadVouchers() {
        final FileSystem type = this.config.getProperty(ConfigKeys.file_system);

        switch (type) {
            case SINGLE -> {
                final PaperCustomFile config = FileKeys.vouchers.getCustomFile();

                if (config == null) {
                    this.logger.warn("The voucher file named {} could not be found in the cache", FileKeys.vouchers.getName());

                    return;
                }

                if (!config.isLoaded()) {
                    this.logger.warn("The {} was not loaded into memory.", FileKeys.vouchers.getName());

                    return;
                }

                final YamlConfiguration configuration = config.getConfiguration();
                final ConfigurationSection section = configuration.getConfigurationSection("vouchers");

                if (section == null) {
                    this.logger.warn("The configuration section we need for {} could not be found.", FileKeys.vouchers.getName());

                    return;
                }

                for (final String voucher : section.getKeys(false)) {
                    try {
                        final ConfigurationSection voucherSection = section.getConfigurationSection(voucher);

                        if (voucherSection == null) {
                            this.fusion.log("warn", "The section for {} could not be found in vouchers.yml", voucher);

                            continue;
                        }

                        this.vouchers.add(new Voucher(voucherSection, voucher));
                    } catch (final Exception exception) {
                        this.brokenVouchers.add(voucher);
                    }
                }
            }

            case MULTIPLE -> {
                for (final Path voucher : getVouchersList()) {
                    final PaperCustomFile file = this.fileManager.getPaperCustomFile(voucher);

                    if (file == null) {
                        this.logger.warn("The voucher file named {} could not be found in the cache", voucher);

                        this.brokenVouchers.add(voucher.getFileName().toString());

                        continue;
                    }

                    if (!file.isLoaded()) {
                        this.logger.warn("Could not load voucher configuration for {}", voucher);

                        this.brokenVouchers.add(file.getFileName());

                        continue;
                    }

                    final ConfigurationSection section = file.getConfiguration().getConfigurationSection("voucher");

                    if (section == null) {
                        this.logger.warn("Could not find voucher configuration section for {}", voucher);

                        this.brokenVouchers.add(file.getFileName());

                        continue;
                    }

                    this.vouchers.add(new Voucher(section, file.getPrettyName()));
                }
            }
        }
    }

    public void reload() {
        this.voucherCodes.clear();
        this.vouchers.clear();

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
        return Collections.unmodifiableList(this.brokenVouchers);
    }

    public @NotNull final List<VoucherCode> getVoucherCodes() {
        return Collections.unmodifiableList(this.voucherCodes);
    }

    public @NotNull final List<String> getBrokenVoucherCodes() {
        return Collections.unmodifiableList(this.brokenVoucherCodes);
    }

    public @Nullable VoucherCode getVoucherCode(@NotNull final String voucherName) {
        VoucherCode voucherCode = null;

        if (voucherName.isEmpty()) return voucherCode;

        for (final VoucherCode value : getVoucherCodes()) {
            if (!value.getCode().equalsIgnoreCase(voucherName)) continue;

            voucherCode = value;

            break;
        }

        return voucherCode;
    }

    public boolean isVoucherCode(@NotNull final String voucherCode) {
        if (voucherCode.isEmpty()) return false;

        boolean isVoucherCode = false;

        for (final VoucherCode voucher : getVoucherCodes()) {
            if (!voucher.isEnabled()) continue;

            if (voucher.isCaseSensitive()) {
                isVoucherCode = voucher.getCode().equals(voucherCode);

                if (isVoucherCode) {
                    break;
                }

                continue;
            }

            isVoucherCode = voucher.getCode().equalsIgnoreCase(voucherCode);

            if (isVoucherCode) {
                break;
            }
        }

        return isVoucherCode;
    }

    public @Nullable Voucher getVoucher(@NotNull final String voucherName) {
        Voucher safeVoucher = null;

        if (voucherName.isEmpty()) return safeVoucher;

        for (final Voucher voucher : getVouchers()) {
            if (!voucher.getStrippedName().equalsIgnoreCase(voucherName)) continue;

            safeVoucher = voucher;

            break;
        }

        return safeVoucher;
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
        if (item.getType() == Material.AIR || !voucher.hasArguments()) return "";

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
}