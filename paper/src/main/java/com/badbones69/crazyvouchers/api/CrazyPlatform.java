package com.badbones69.crazyvouchers.api;

import com.badbones69.crazyvouchers.api.adapters.PaperSenderAdapter;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.support.MetricsWrapper;
import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.files.types.configurate.YamlCustomFile;
import com.ryderbelserion.fusion.paper.FusionPaper;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.other.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrazyPlatform extends CrazyVouchersPlugin<Audience, FusionPaper> {

    private @NonNull final List<String> brokenVouchers = new ArrayList<>();
    private @NonNull final List<Voucher> vouchers = new ArrayList<>();

    private @NonNull final List<String> brokenCodes = new ArrayList<>();
    private @NonNull final List<VoucherCode> codes = new ArrayList<>();

    private @NonNull final Map<UUID, String> two_step_authentication = new ConcurrentHashMap<>();

    private PaperSenderAdapter senderAdapter;

    public CrazyPlatform(@NonNull final FusionPaper fusion, @NonNull final Path path) {
        super(fusion, path);
    }

    @Override
    public void init() {
        super.init();

        this.senderAdapter = new PaperSenderAdapter();

        loadMessages();
        loadVouchers();
        loadCodes();

        new MetricsWrapper(4536).start();
    }

    @Override
    public void reload() {
        super.reload();

        updateVouchers();
        updateCodes();
    }

    @Override
    public @NonNull PaperSenderAdapter getSenderAdapter() {
        return this.senderAdapter;
    }

    @Override
    public void updateVouchers() {
        this.brokenVouchers.clear();
        this.vouchers.clear();

        loadVouchers();
    }

    @Override
    public void loadVouchers() {
        final FileSystem type = this.config.getProperty(RootKeys.file_system);

        switch (type) {
            case SINGLE -> {
                this.fileManager.getYamlFile(this.path.resolve("vouchers.yml")).ifPresent(customFile -> {
                    if (!customFile.isLoaded()) {
                        this.fusion.log(Level.warn, "The %s was not loaded into memory.", customFile.getFileName());

                        return;
                    }

                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    if (!configuration.hasChild("vouchers")) {
                        this.fusion.log(Level.warn, "The configuration section we need for %s could not be found.", customFile.getFileName());

                        return;
                    }

                    configuration.node("vouchers").childrenMap().forEach((id, node) -> {
                        try {
                            this.vouchers.add(new Voucher(node, id.toString()));
                        } catch (final Exception exception) {
                            this.brokenVouchers.add(id.toString());
                        }
                    });
                });
            }

            case MULTIPLE -> {
                for (final Path voucher : getVoucherFiles()) {
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

                    if (!configuration.hasChild("voucher")) {
                        this.fusion.log(Level.warn, "Could not find voucher configuration section for %s", voucher);

                        this.brokenVouchers.add(file.getFileName());

                        continue;
                    }

                    this.vouchers.add(new Voucher(configuration.node("voucher"), file.getFileName()));
                }
            }
        }
    }

    @Override
    public void updateCodes() {
        this.brokenCodes.clear();
        this.codes.clear();

        loadCodes();
    }

    @Override
    public void loadCodes() {
        final FileSystem type = this.config.getProperty(RootKeys.file_system);

        switch (type) {
            case SINGLE -> {
                this.fileManager.getYamlFile(this.path.resolve("codes.yml")).ifPresent(customFile -> {
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
                            this.codes.add(new VoucherCode(node, id.toString()));
                        } catch (final Exception exception) {
                            this.brokenCodes.add(id.toString());
                        }
                    });
                });
            }

            case MULTIPLE -> {
                for (final Path code : getCodeFiles()) {
                    final Optional<YamlCustomFile> optional = this.fileManager.getYamlFile(code);

                    if (optional.isEmpty()) {
                        this.fusion.log(Level.warn, "The code file named %s could not be found in the cache", code);

                        this.brokenCodes.add(code.getFileName().toString());

                        continue;
                    }

                    final YamlCustomFile file = optional.get();

                    if (!file.isLoaded()) {
                        this.fusion.log(Level.warn, "Could not load code configuration for %s", code);

                        this.brokenCodes.add(file.getFileName());

                        continue;
                    }

                    final CommentedConfigurationNode configuration = file.getConfiguration();

                    if (!configuration.hasChild("voucher-code")) {
                        this.fusion.log(Level.warn, "Could not find voucher code configuration section for %s", code);

                        this.brokenCodes.add(file.getFileName());

                        continue;
                    }

                    this.codes.add(new VoucherCode(configuration.node("voucher-code"), file.getFileName()));
                }
            }
        }
    }

    public @NonNull final List<Voucher> getVouchers() {
        return Collections.unmodifiableList(this.vouchers);
    }

    public @NonNull final List<String> getBrokenVouchers() {
        return Collections.unmodifiableList(this.brokenVouchers);
    }

    public @NonNull final List<VoucherCode> getVoucherCodes() {
        return Collections.unmodifiableList(this.codes);
    }

    public @NonNull final List<String> getBrokenVoucherCodes() {
        return Collections.unmodifiableList(this.brokenCodes);
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
        this.two_step_authentication.put(uuid, voucher);
    }

    public void removeVoucherAuth(@NonNull final UUID uuid) {
        if (!isVoucherAuthActive(uuid)) return;

        this.two_step_authentication.remove(uuid);
    }

    public boolean isVoucherAuthActive(@NonNull final UUID uuid) {
        return this.two_step_authentication.containsKey(uuid);
    }

    public String getActiveVoucherAuth(@NonNull final UUID uuid) {
        return this.two_step_authentication.getOrDefault(uuid, "");
    }
}