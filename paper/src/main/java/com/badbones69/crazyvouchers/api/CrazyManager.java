package com.badbones69.crazyvouchers.api;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import us.crazycrew.crazyvouchers.api.enums.other.FileSystem;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.files.enums.FileType;
import com.ryderbelserion.fusion.files.types.configurate.YamlCustomFile;
import com.ryderbelserion.fusion.paper.FusionPaper;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.badbones69.crazyvouchers.config.ConfigManager;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class CrazyManager {

    private @NonNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NonNull final FusionPaper fusion = this.plugin.getFusion();

    private @NonNull final Path dataPath = this.plugin.getDataPath();

    private @NonNull final SettingsManager config = ConfigManager.getConfig();

    private @NonNull final FileManager fileManager = this.plugin.getFileManager();

    private @NonNull final List<Voucher> vouchers = new ArrayList<>();
    private @NonNull final List<VoucherCode> voucherCodes = new ArrayList<>();

    private @NonNull final List<String> brokenVouchers = new ArrayList<>();
    private @NonNull final List<String> brokenVoucherCodes = new ArrayList<>();

    private @NonNull final Map<UUID, String> voucher_auth = new HashMap<>();

    public void load() {
        loadExamples();

        update();

        loadVouchers();
        loadCodes();
    }

    public void reload() {
        loadExamples();

        update();

        reloadVouchers();
        reloadCodes();
    }

    public void update() {
        final Path version = this.dataPath.resolve("version.json");

        try {
            Files.deleteIfExists(version);
        } catch (final IOException exception) {
            this.fusion.log(Level.warn, "Failed to delete version.json!", exception);
        }
    }

    public void reloadCodes() {
        this.voucherCodes.clear();

        loadCodes();
    }

    public void reloadVouchers() {
        this.vouchers.clear();

        loadVouchers();
    }

    public void loadCodes() {
        final FileSystem type = this.config.getProperty(ConfigKeys.file_system);

        switch (type) {
            case SINGLE -> {
                final YamlCustomFile config = FileKeys.codes.getPaperCustomFile();

                if (!config.isLoaded()) {
                    this.fusion.log(Level.warn, "The %s was not loaded into memory.", FileKeys.codes.getName());

                    return;
                }

                final CommentedConfigurationNode configuration = config.getConfiguration();

                if (!configuration.hasChild("voucher-codes")) {
                    this.fusion.log(Level.warn, "The configuration section we need for %s could not be found.", FileKeys.codes.getName());

                    return;
                }

                final CommentedConfigurationNode section = configuration.node("voucher-codes");

                section.childrenMap().forEach((id, node) -> {
                    try {
                        this.voucherCodes.add(new VoucherCode(node, id.toString()));
                    } catch (final Exception exception) {
                        this.brokenVoucherCodes.add(id.toString());
                    }
                });
            }

            case MULTIPLE -> {
                for (final Path code : getCodesList()) {
                    final Optional<YamlCustomFile> optional = this.fileManager.getYamlFile(code);

                    if (optional.isEmpty()) {
                        this.fusion.log(Level.warn, "The code file named %s could not be found in the cache", code);

                        this.brokenVoucherCodes.add(code.getFileName().toString());

                        continue;
                    }

                    final YamlCustomFile file = optional.get();

                    if (!file.isLoaded()) {
                        this.fusion.log(Level.warn, "Could not load code configuration for %s", code);

                        this.brokenVoucherCodes.add(file.getFileName());

                        continue;
                    }

                    final CommentedConfigurationNode configuration = file.getConfiguration();

                    if (!configuration.hasChild("voucher-code")) {
                        this.fusion.log(Level.warn, "Could not find voucher code configuration section for %s", code);

                        this.brokenVoucherCodes.add(file.getFileName());

                        continue;
                    }

                    this.voucherCodes.add(new VoucherCode(configuration.node("voucher-code"), file.getFileName()));
                }
            }
        }
    }

    public void loadVouchers() {
        final FileSystem type = this.config.getProperty(ConfigKeys.file_system);

        switch (type) {
            case SINGLE -> {
                final YamlCustomFile config = FileKeys.vouchers.getPaperCustomFile();

                if (!config.isLoaded()) {
                    this.fusion.log(Level.warn, "The %s was not loaded into memory.", FileKeys.vouchers.getName());

                    return;
                }

                final CommentedConfigurationNode configuration = config.getConfiguration();

                if (configuration.hasChild("vouchers")) {
                    this.fusion.log(Level.warn, "The configuration section we need for %s could not be found.", FileKeys.vouchers.getName());

                    return;
                }

                configuration.node("vouchers").childrenMap().forEach((id, node) -> {
                    try {
                        this.vouchers.add(new Voucher(node, id.toString()));
                    } catch (final Exception exception) {
                        this.brokenVoucherCodes.add(id.toString());
                    }
                });
            }

            case MULTIPLE -> {
                for (final Path voucher : getVouchersList()) {
                    final Optional<YamlCustomFile> optional = this.fileManager.getYamlFile(voucher);

                    if (optional.isEmpty()) {
                        this.fusion.log(Level.warn, "The voucher file named %s could not be found in the cache", voucher);

                        this.brokenVouchers.add(voucher.getFileName().toString());

                        continue;
                    }

                    final YamlCustomFile file = optional.get();

                    if (!file.isLoaded()) {
                        this.fusion.log(Level.warn, "Could not load voucher configuration for %s", voucher);

                        this.brokenVouchers.add(file.getFileName());

                        continue;
                    }

                    final CommentedConfigurationNode configuration = file.getConfiguration();

                    if (configuration.hasChild("voucher")) {
                        this.fusion.log(Level.warn, "Could not find voucher configuration section for %s", voucher);

                        this.brokenVouchers.add(file.getFileName());

                        continue;
                    }

                    this.vouchers.add(new Voucher(configuration.node("voucher"), file.getFileName()));
                }
            }
        }
    }

    public void loadExamples() {
        if (this.config.getProperty(ConfigKeys.update_examples_folder)) {
            final Path examples = this.dataPath.resolve("examples");

            if (Files.exists(examples)) {
                try (final Stream<Path> values = Files.walk(examples)) {
                    values.sorted(Comparator.reverseOrder()).forEach(path -> { // sorted in reverse order, to ensure the directories are empty first.
                        try {
                            this.fusion.log(Level.warn, "Successfully deleted path %s, re-generating the examples later.", path);

                            Files.delete(path);
                        } catch (final IOException exception) {
                            this.fusion.log(Level.warn, "Failed to delete %s in loop.", exception, path);
                        }
                    });
                } catch (final Exception exception) {
                    this.fusion.log(Level.warn, "Failed to delete %s.", exception, exception);
                }
            }

            this.fileManager.extractFolder("vouchers", FileType.YAML, examples);
            this.fileManager.extractFolder("codes", FileType.YAML, examples);
            this.fileManager.extractFolder("locale", FileType.YAML, examples);

            List.of(
                    "config.yml",
                    "codes.yml",
                    "data.yml",
                    "users.yml",
                    "vouchers.yml"
            ).forEach(file -> this.fileManager.extractFile(file, examples.resolve(file)));
        }
    }

    public @NonNull final List<Path> getVouchersList() {
        return this.fusion.getFilesByPath(this.dataPath.resolve("vouchers"), ".yml");
    }

    public @NonNull final List<Path> getCodesList() {
        return this.fusion.getFilesByPath(this.dataPath.resolve("codes"), ".yml");
    }
    
    public @NonNull final List<Voucher> getVouchers() {
        return Collections.unmodifiableList(this.vouchers);
    }

    public @NonNull final List<String> getBrokenVouchers() {
        return Collections.unmodifiableList(this.brokenVouchers);
    }

    public @NonNull final List<VoucherCode> getVoucherCodes() {
        return Collections.unmodifiableList(this.voucherCodes);
    }

    public @NonNull final List<String> getBrokenVoucherCodes() {
        return Collections.unmodifiableList(this.brokenVoucherCodes);
    }

    public @Nullable VoucherCode getVoucherCode(@NonNull final String voucherName) {
        VoucherCode voucherCode = null;

        if (voucherName.isEmpty()) return voucherCode;

        for (final VoucherCode value : getVoucherCodes()) {
            if (!value.getCode().equalsIgnoreCase(voucherName)) continue;

            voucherCode = value;

            break;
        }

        return voucherCode;
    }

    public boolean isVoucherCode(@NonNull final String voucherCode) {
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

    public @Nullable Voucher getVoucher(@NonNull final String voucherName) {
        Voucher safeVoucher = null;

        if (voucherName.isEmpty()) return safeVoucher;

        for (final Voucher voucher : getVouchers()) {
            if (!voucher.getStrippedName().equalsIgnoreCase(voucherName)) continue;

            safeVoucher = voucher;

            break;
        }

        return safeVoucher;
    }

    public @Nullable Voucher getVoucherFromItem(@NonNull final ItemStack item) {
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

    public void addVoucherAuth(@NonNull final UUID uuid, @NonNull final String voucher) {
        this.voucher_auth.put(uuid, voucher);
    }

    public void removeVoucherAuth(@NonNull final UUID uuid) {
        if (!isVoucherAuthActive(uuid)) return;

        this.voucher_auth.remove(uuid);
    }

    public boolean isVoucherAuthActive(@NonNull final UUID uuid) {
        return this.voucher_auth.containsKey(uuid);
    }

    public String getActiveVoucherAuth(@NonNull final UUID uuid) {
        return this.voucher_auth.getOrDefault(uuid, "");
    }
}