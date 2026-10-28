package com.badbones69.crazyvouchers.api;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.other.FileSystem;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.files.types.configurate.YamlCustomFile;
import com.ryderbelserion.fusion.paper.FusionPaper;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import java.nio.file.Path;
import java.util.*;

public class CrazyManager {

    private @NonNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NonNull final CrazyPlatform platform = this.plugin.getPlatform();

    private @NonNull final FusionPaper fusion = this.plugin.getFusion();

    private @NonNull final Path dataPath = this.plugin.getDataPath();

    private @NonNull final ConfigManager configManager = this.platform.getConfigManager();

    private @NonNull final PropertyManager config = this.configManager.getConfig();

    private @NonNull final FileManager fileManager = this.fusion.getFileManager();

    private @NonNull final List<Voucher> vouchers = new ArrayList<>();
    private @NonNull final List<VoucherCode> voucherCodes = new ArrayList<>();

    private @NonNull final List<String> brokenVouchers = new ArrayList<>();
    private @NonNull final List<String> brokenVoucherCodes = new ArrayList<>();

    private @NonNull final Map<UUID, String> voucher_auth = new HashMap<>();

    public void load() {
        loadVouchers();
        loadCodes();
    }

    public void reload() {
        reloadVouchers();
        reloadCodes();
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
        final FileSystem type = this.config.getProperty(RootKeys.file_system);

        switch (type) {
            case SINGLE -> {
                this.fileManager.getYamlFile(this.dataPath.resolve("codes.yml")).ifPresent(customFile -> {
                    if (!customFile.isLoaded()) {
                        this.fusion.log(Level.warn, "The %s was not loaded into memory.", customFile.getFileName());

                        return;
                    }

                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    if (!configuration.hasChild("voucher-codes")) {
                        this.fusion.log(Level.warn, "The configuration section we need for %s could not be found.", customFile.getFileName());

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
        final FileSystem type = this.config.getProperty(RootKeys.file_system);

        switch (type) {
            case SINGLE -> {
                this.fileManager.getYamlFile(this.dataPath.resolve("vouchers.yml")).ifPresent(customFile -> {
                    if (!customFile.isLoaded()) {
                        this.fusion.log(Level.warn, "The %s was not loaded into memory.", customFile.getFileName());

                        return;
                    }

                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    if (configuration.hasChild("vouchers")) {
                        this.fusion.log(Level.warn, "The configuration section we need for %s could not be found.", customFile.getFileName());

                        return;
                    }

                    configuration.node("vouchers").childrenMap().forEach((id, node) -> {
                        try {
                            this.vouchers.add(new Voucher(node, id.toString()));
                        } catch (final Exception exception) {
                            this.brokenVoucherCodes.add(id.toString());
                        }
                    });
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