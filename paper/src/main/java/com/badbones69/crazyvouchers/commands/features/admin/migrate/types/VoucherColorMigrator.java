package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.core.api.enums.FileType;
import com.ryderbelserion.fusion.core.util.StringUtils;
import com.ryderbelserion.fusion.paper.files.CustomFile;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VoucherColorMigrator extends IVoucherMigrator {

    public VoucherColorMigrator(@NotNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_COLOR);
    }

    @Override
    public void run() {
        final List<String> failed = new ArrayList<>();
        final List<String> success = new ArrayList<>();

        try {
            this.config.setProperty(ConfigKeys.command_prefix, StringUtils.convert(this.config.getProperty(ConfigKeys.command_prefix), true));
            this.config.setProperty(ConfigKeys.dupe_protection_warning, StringUtils.convert(this.config.getProperty(ConfigKeys.dupe_protection_warning), true));

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

                if (vouchers != null) {
                    final ConfigurationSection section = vouchers.getConfigurationSection("vouchers");

                    if (section != null) {
                        for (final String key : section.getKeys(false)) {
                            final ConfigurationSection voucher = section.getConfigurationSection(key);

                            if (voucher == null) continue;

                            final String itemName = voucher.getString("name", key);
                            final List<String> itemLore = voucher.getStringList("lore");

                            voucher.set("lore", StringUtils.convert(itemLore, true));

                            final String optionsMessage = voucher.getString("options.message", "");
                            final String optionsWorldMessage = voucher.getString("options.whitelist-worlds.message",
                                    "{prefix}You are not in any of the whitelisted worlds.");
                            final String optionsWhitelistMessage = voucher.getString("options.permission.whitelist-permission.message",
                                    "{prefix}You do not have the permission <red>{permission} <gray>to use this voucher.");
                            final String optionsBlacklistMessage = voucher.getString("options.permission.blacklist-permission.message",
                                    "{prefix}You already have the permission <red>{permission} <gray>so you can''t use this voucher.");

                            voucher.set("name", StringUtils.convert(itemName));

                            voucher.set("options.message", optionsMessage);
                            voucher.set("options.whitelist-worlds.message", optionsWorldMessage);

                            voucher.set("options.permission.whitelist-permission.message", optionsWhitelistMessage);
                            voucher.set("options.permission.blacklist-permission.message", optionsBlacklistMessage);
                        }

                        success.add("<green>⤷ vouchers.yml");

                        FileKeys.vouchers.save();
                    } else {
                        failed.add("<red>⤷ vouchers.yml");
                    }
                } else {
                    failed.add("<red>⤷ vouchers.yml");
                }

                final YamlConfiguration codes = FileKeys.codes.getConfiguration();

                if (codes != null) {
                    final ConfigurationSection section = codes.getConfigurationSection("voucher-codes");

                    if (section != null) {
                        final String optionsMessage = section.getString("options.message", "");
                        final String optionsWorldMessage = section.getString("options.whitelist-worlds.message",
                                "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.");

                        final String optionsWhitelistMessage = section.getString("options.permission.whitelist-permission.message",
                                "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.");

                        final String optionsBlacklistMessage = section.getString("options.permission.blacklist-permission.message",
                                "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.");

                        section.set("options.message", optionsMessage);
                        section.set("options.whitelist-worlds.message", optionsWorldMessage);

                        section.set("options.permission.whitelist-permission.message", optionsWhitelistMessage);
                        section.set("options.permission.blacklist-permission.message", optionsBlacklistMessage);

                        success.add("<green>⤷ codes.yml");

                        FileKeys.codes.save();
                    } else {
                        failed.add("<red>⤷ codes.yml");
                    }
                } else {
                    failed.add("<red>⤷ codes.yml");
                }
            }

            case MULTIPLE -> {
                final List<VoucherCode> codes = this.crazyManager.getVoucherCodes();

                for (final VoucherCode code : codes) {
                    final String name = code.getName();
                    final String file = code.getName() + ".yml";

                    final CustomFile customFile = this.fileManager.getFile(name, FileType.YAML);

                    if (customFile == null) {
                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final YamlConfiguration configuration = customFile.getConfiguration();

                    if (configuration == null) {
                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final ConfigurationSection section = configuration.getConfigurationSection("voucher-code");

                    if (section == null) {
                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final String optionsMessage = section.getString("options.message", "");
                    final String optionsWorldMessage = section.getString("options.whitelist-worlds.message",
                            "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.");

                    final String optionsWhitelistMessage = section.getString("options.permission.whitelist-permission.message",
                            "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.");

                    final String optionsBlacklistMessage = section.getString("options.permission.blacklist-permission.message",
                            "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.");

                    section.set("options.message", optionsMessage);
                    section.set("options.whitelist-worlds.message", optionsWorldMessage);

                    section.set("options.permission.whitelist-permission.message", optionsWhitelistMessage);
                    section.set("options.permission.blacklist-permission.message", optionsBlacklistMessage);

                    success.add("<green>⤷ " + file);

                    customFile.save();
                }

                final List<Voucher> vouchers = this.crazyManager.getVouchers();

                for (final Voucher voucher : vouchers) {
                    final String name = voucher.getName();
                    final String file = voucher.getName() + ".yml";

                    final CustomFile customFile = this.fileManager.getFile(name, FileType.YAML);

                    if (customFile == null) {
                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final YamlConfiguration configuration = customFile.getConfiguration();

                    if (configuration == null) {
                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final ConfigurationSection section = configuration.getConfigurationSection("voucher");

                    if (section == null) {
                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final String itemName = section.getString("name", name);
                    final List<String> itemLore = section.getStringList("lore");

                    section.set("lore", StringUtils.convert(itemLore, true));

                    final String optionsMessage = section.getString("options.message", "");
                    final String optionsWorldMessage = section.getString("options.whitelist-worlds.message",
                            "{prefix}You are not in any of the whitelisted worlds.");
                    final String optionsWhitelistMessage = section.getString("options.permission.whitelist-permission.message",
                            "{prefix}You do not have the permission <red>{permission} <gray>to use this voucher.");
                    final String optionsBlacklistMessage = section.getString("options.permission.blacklist-permission.message",
                            "{prefix}You already have the permission <red>{permission} <gray>so you can''t use this voucher.");

                    section.set("name", StringUtils.convert(itemName));

                    section.set("options.message", optionsMessage);
                    section.set("options.whitelist-worlds.message", optionsWorldMessage);

                    section.set("options.permission.whitelist-permission.message", optionsWhitelistMessage);
                    section.set("options.permission.blacklist-permission.message", optionsBlacklistMessage);

                    success.add("<green>⤷ " + file);

                    customFile.save();
                }
            }
        }

        final int convertedCount = success.size();
        final int failedCount = failed.size();

        sendMessage(new ArrayList<>(failedCount + convertedCount) {{
            addAll(failed);
            addAll(success);
        }}, convertedCount, failedCount);

        this.fileManager.init();

        this.crazyManager.load();
    }

    @Override
    public <T> void set(@NotNull final ConfigurationSection section, @NotNull final String path, @NotNull final T value) {
        section.set(path, value);
    }

    @Override
    public final File getVouchersDirectory() {
        return new File(this.plugin.getDataFolder(), "vouchers");
    }

    @Override
    public final File getCodesDirectory() {
        return new File(this.plugin.getDataFolder(), "codes");
    }
}