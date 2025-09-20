package com.badbones69.crazyvouchers.commands.features.admin.migrate.types.deprecation;

import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.paper.files.types.PaperCustomFile;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LegacyColorMigrator extends IVoucherMigrator {

    public LegacyColorMigrator(@NotNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_COLOR);
    }

    @Override
    public void run() {
        final List<String> failed = new ArrayList<>();
        final List<String> success = new ArrayList<>();

        try {
            this.config.setProperty(ConfigKeys.command_prefix, this.utils.convertLegacy(this.config.getProperty(ConfigKeys.command_prefix), true));
            this.config.setProperty(ConfigKeys.dupe_protection_warning, this.utils.convertLegacy(this.config.getProperty(ConfigKeys.dupe_protection_warning), true));

            success.add("<green>⤷ config.yml");

            this.config.save();
            this.config.reload();
        } catch (Exception exception) {
            failed.add("<red>⤷ config.yml");
        }

        try {
            for (final Messages message : Messages.values()) {
                message.migrate();
            }

            success.add("<green>⤷ messages.yml");

            this.messages.save();
            this.messages.reload();
        } catch (Exception exception) {
            failed.add("<red>⤷ messages.yml");
        }

        switch (this.config.getProperty(ConfigKeys.file_system)) {
            case SINGLE -> {
                final YamlConfiguration vouchers = FileKeys.vouchers.getConfiguration();

                final ConfigurationSection voucherSection = vouchers.getConfigurationSection("vouchers");

                if (voucherSection != null) {
                    for (final String key : voucherSection.getKeys(false)) {
                        final ConfigurationSection voucher = voucherSection.getConfigurationSection(key);

                        if (voucher == null) continue;

                        processItems(key, voucher);
                    }

                    success.add("<green>⤷ vouchers.yml");

                    FileKeys.vouchers.save();
                } else {
                    this.fusion.log("warn", "Failed to migrate vouchers.yml due to the configuration section being null.");

                    failed.add("<red>⤷ vouchers.yml");
                }

                final YamlConfiguration codes = FileKeys.codes.getConfiguration();

                final ConfigurationSection codeSection = codes.getConfigurationSection("voucher-codes");

                if (codeSection != null) {
                    process(codeSection);

                    success.add("<green>⤷ codes.yml");

                    FileKeys.codes.save();
                } else {
                    this.fusion.log("warn", "Failed to migrate codes.yml due to the configuration section being null.");

                    failed.add("<red>⤷ codes.yml");
                }
            }

            case MULTIPLE -> {
                final List<VoucherCode> codes = this.crazyManager.getVoucherCodes();

                final Path code_dir = this.dataPath.resolve("codes");

                for (final VoucherCode code : codes) {
                    final String file = code.getName();

                    final @NotNull Optional<PaperCustomFile> customFile = this.fileManager.getPaperFile(code_dir.resolve(file));

                    if (customFile.isEmpty()) {
                        this.fusion.log("warn", "Failed to migrate code {}, because the file is not in the cache.", file);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final PaperCustomFile safeCustomFile = customFile.get();

                    if (!safeCustomFile.isLoaded()) {
                        this.fusion.log("warn", "Failed to migrate code {}, because the file configuration is null.", file);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final YamlConfiguration configuration = safeCustomFile.getConfiguration();

                    final ConfigurationSection section = configuration.getConfigurationSection("voucher-code");

                    if (section == null) {
                        this.fusion.log("warn", "Failed to migrate code {}, because the configuration section is null.", file);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    process(section);

                    success.add("<green>⤷ " + file);

                    safeCustomFile.save();
                }

                final List<Voucher> vouchers = this.crazyManager.getVouchers();

                final Path voucher_dir = this.dataPath.resolve("vouchers");

                for (final Voucher voucher : vouchers) {
                    final String name = voucher.getStrippedName();
                    final String file = voucher.getName();

                    final @NotNull Optional<PaperCustomFile> customFile = this.fileManager.getPaperFile(voucher_dir.resolve(file));

                    if (customFile.isEmpty()) {
                        this.fusion.log("warn", "Failed to migrate code {}, because the file is not in the cache.", file);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final PaperCustomFile safeCustomFile = customFile.get();

                    if (!safeCustomFile.isLoaded()) {
                        this.fusion.log("warn", "Failed to migrate code {}, because the file configuration is null.", file);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final YamlConfiguration configuration = safeCustomFile.getConfiguration();

                    final ConfigurationSection section = configuration.getConfigurationSection("voucher");

                    if (section == null) {
                        this.fusion.log("warn", "Failed to migrate code {}, because the configuration section is null.", file);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    processItems(name, section);

                    success.add("<green>⤷ " + file);

                    safeCustomFile.save();
                }
            }
        }

        final int convertedCount = success.size();
        final int failedCount = failed.size();

        sendMessage(new ArrayList<>(failedCount + convertedCount) {{
            addAll(failed);
            addAll(success);
        }}, convertedCount, failedCount);

        this.crazyManager.load(true);
    }

    private void processItems(@NotNull final String name, @NotNull final ConfigurationSection section) {
        final String itemName = section.getString("name", name);
        final List<String> itemLore = section.getStringList("lore");

        section.set("lore", this.utils.convertLegacy(itemLore, true));

        final String optionsMessage = section.getString("options.message", "");
        final String optionsWorldMessage = section.getString("options.whitelist-worlds.message",
                "{prefix}You are not in any of the whitelisted worlds.");
        final String optionsWhitelistMessage = section.getString("options.permission.whitelist-permission.message",
                "{prefix}You do not have the permission <red>{permission} <gray>to use this voucher.");
        final String optionsBlacklistMessage = section.getString("options.permission.blacklist-permission.message",
                "{prefix}You already have the permission <red>{permission} <gray>so you can''t use this voucher.");

        section.set("name", this.utils.convertLegacy(itemName));

        section.set("options.message", this.utils.convertLegacy(optionsMessage));
        section.set("options.whitelist-worlds.message", this.utils.convertLegacy(optionsWorldMessage));

        section.set("options.permission.whitelist-permission.message", this.utils.convertLegacy(optionsWhitelistMessage));
        section.set("options.permission.blacklist-permission.message", this.utils.convertLegacy(optionsBlacklistMessage));
    }

    private void process(@NotNull final ConfigurationSection section) {
        final String optionsMessage = section.getString("options.message", "");
        final String optionsWorldMessage = section.getString("options.whitelist-worlds.message",
                "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.");

        final String optionsWhitelistMessage = section.getString("options.permission.whitelist-permission.message",
                "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.");

        final String optionsBlacklistMessage = section.getString("options.permission.blacklist-permission.message",
                "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.");

        section.set("options.message", this.utils.convertLegacy(optionsMessage));
        section.set("options.whitelist-worlds.message", this.utils.convertLegacy(optionsWorldMessage));

        section.set("options.permission.whitelist-permission.message", this.utils.convertLegacy(optionsWhitelistMessage));
        section.set("options.permission.blacklist-permission.message", this.utils.convertLegacy(optionsBlacklistMessage));
    }

    @Override
    public <T> void set(@NotNull final ConfigurationSection section, @NotNull final String path, @NotNull final T value) {
        section.set(path, value);
    }

    @Override
    public @NotNull final Path getVouchersDirectory() {
        return this.dataPath.resolve("vouchers");
    }

    @Override
    public @NotNull final Path getCodesDirectory() {
        return this.dataPath.resolve("codes");
    }
}