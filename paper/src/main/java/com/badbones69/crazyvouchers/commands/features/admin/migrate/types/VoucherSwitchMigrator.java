package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.api.enums.FileType;
import com.ryderbelserion.fusion.api.utils.FileUtils;
import com.ryderbelserion.fusion.paper.files.LegacyCustomFile;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class VoucherSwitchMigrator extends IVoucherMigrator {

    public VoucherSwitchMigrator(@NotNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_SWITCH);
    }

    @Override
    public void run() {
        switch (this.config.getProperty(ConfigKeys.file_system)) {
            case SINGLE -> {
                final File voucher_directory = getVouchersDirectory();
                final File code_directory = getCodesDirectory();

                final File voucher_file = new File(this.dataFolder, "vouchers.yml");

                YamlConfiguration voucher_config = null;

                if (voucher_file.exists()) {
                    voucher_config = YamlConfiguration.loadConfiguration(voucher_file);
                }

                if (voucher_config != null) {
                    final ConfigurationSection vouchers = voucher_config.getConfigurationSection("vouchers");

                    if (vouchers != null) {
                        for (final String key : vouchers.getKeys(false)) {
                            final ConfigurationSection entry = vouchers.getConfigurationSection(key);

                            final File file = new File(voucher_directory, key);

                            if (!file.exists()) {
                                try {
                                    file.createNewFile();
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                }
                            }

                            final LegacyCustomFile customFile = new LegacyCustomFile(FileType.YAML, file, true);

                            customFile.load();

                            final YamlConfiguration configuration = customFile.getConfiguration();

                            if (configuration == null) continue;

                            if (entry == null) continue;

                            final ConfigurationSection section = configuration.createSection("voucher");

                            processItems(entry, section);
                        }
                    }
                }

                final File code_file = new File(this.dataFolder, "codes.yml");

                YamlConfiguration code_config = null;

                if (code_file.exists()) {
                    code_config = YamlConfiguration.loadConfiguration(code_file);
                }

                if (code_config != null) {
                    final ConfigurationSection codes = code_config.getConfigurationSection("voucher-codes");

                    if (codes != null) {
                        for (final String key : codes.getKeys(false)) {
                            final ConfigurationSection entry = codes.getConfigurationSection(key);

                            final File file = new File(code_directory, key);

                            if (!file.exists()) {
                                try {
                                    file.createNewFile();
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                }
                            }

                            final LegacyCustomFile customFile = new LegacyCustomFile(FileType.YAML, file, true);

                            customFile.load();

                            final YamlConfiguration configuration = customFile.getConfiguration();

                            if (configuration == null) continue;

                            if (entry == null) continue;

                            final ConfigurationSection section = configuration.createSection("voucher-code");

                            process(entry, section);
                        }
                    }
                }
            }

            case MULTIPLE -> {
                final File voucher_file = new File(this.dataFolder, "vouchers.yml");

                if (!voucher_file.exists()) {
                    try {
                        voucher_file.createNewFile();
                    } catch (final IOException exception) {
                        exception.printStackTrace();
                    }
                }

                final LegacyCustomFile voucher_custom_file = new LegacyCustomFile(FileType.YAML, voucher_file, true).load();

                final YamlConfiguration voucher_config = voucher_custom_file.getConfiguration();

                if (voucher_config != null) {
                    final ConfigurationSection new_section = voucher_config.contains("vouchers") ? voucher_config.getConfigurationSection("vouchers") : voucher_config.createSection("vouchers");

                    if (new_section != null) {
                        final List<File> vouchers = FileUtils.getFiles(new File(this.dataFolder, "vouchers"), ".yml", true);

                        for (final File voucher : vouchers) {
                            final String fileName = voucher.getName();

                            final LegacyCustomFile customFile = this.fileManager.getFile(fileName, FileType.YAML);

                            if (customFile == null) continue;

                            final YamlConfiguration configuration = customFile.getConfiguration();

                            if (configuration == null) continue;

                            final ConfigurationSection entry = configuration.getConfigurationSection("voucher");

                            if (entry == null) continue;

                            final ConfigurationSection section = new_section.createSection(fileName);

                            processItems(entry, section);

                            voucher_custom_file.save();
                        }

                        this.fileManager.addFile(voucher_custom_file);
                        this.fileManager.removeFile("vouchers.yml", FileType.YAML, false);
                    }
                }

                final File code_file = new File(this.dataFolder, "codes.yml");

                if (!code_file.exists()) {
                    try {
                        code_file.createNewFile();
                    } catch (final IOException exception) {
                        exception.printStackTrace();
                    }
                }

                final LegacyCustomFile code_custom_file = new LegacyCustomFile(FileType.YAML, code_file, true).load();

                final YamlConfiguration code_config = code_custom_file.getConfiguration();

                if (code_config != null) {
                    final ConfigurationSection new_section = code_config.contains("voucher-codes") ? code_config.getConfigurationSection("voucher-codes") : code_config.createSection("voucher-codes");

                    if (new_section != null) {
                        final List<File> codes = FileUtils.getFiles(new File(this.dataFolder, "codes"), ".yml", true);

                        for (final File code : codes) {
                            final String fileName = code.getName();

                            final LegacyCustomFile customFile = this.fileManager.getFile(fileName, FileType.YAML);

                            if (customFile == null) continue;

                            final YamlConfiguration configuration = customFile.getConfiguration();

                            if (configuration == null) continue;

                            final ConfigurationSection entry = configuration.getConfigurationSection("voucher-code");

                            if (entry == null) continue;

                            final ConfigurationSection section = new_section.createSection(fileName);

                            processItems(entry, section);

                            code_custom_file.save();
                        }

                        this.fileManager.addFile(code_custom_file);
                        this.fileManager.removeFile("codes.yml", FileType.YAML, false);
                    }
                }
            }
        }
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

    private void processItems(ConfigurationSection entry, ConfigurationSection section) {
        if (entry.contains("item")) {
            final String item = entry.getString("item");

            section.set("item", item);
        }

        if (entry.contains("name")) {
            final String name = entry.getString("name");

            section.set("name", name);
        }

        if (entry.contains("lore")) {
            final List<String> lore = entry.getStringList("lore");

            section.set("lore", lore);
        }

        if (entry.contains("player")) {
            final String player = entry.getString("player");

            section.set("player", player);
        }

        if (entry.contains("glowing")) {
            final boolean glowing = entry.getBoolean("glowing");

            section.set("glowing", glowing);
        }

        if (entry.contains("display-damage")) {
            final int display_damage = entry.getInt("display-damage");

            section.set("display-damage", display_damage);
        }

        if (entry.contains("display-trim.material") && entry.contains("display-trim.pattern")) {
            final String trim_material = entry.getString("display-trim.material");
            final String trim_pattern = entry.getString("display-trim.pattern");

            section.set("display-trim.material", trim_material);
            section.set("display-trim.pattern", trim_pattern);
        }

        if (entry.contains("components.hide-tooltip")) {
            final boolean hide_tooltip = entry.getBoolean("components.hide-tooltip");

            section.set("components.hide-tooltip", hide_tooltip);
        }

        if (entry.contains("options.is-edible")) {
            final boolean edible = entry.getBoolean("options.is-edible");

            section.set("options.is-edible", edible);
        }

        process(entry, section);
    }

    private void process(final ConfigurationSection entry, final ConfigurationSection section) {
        if (entry.contains("commands")) {
            final List<String> commands = entry.getStringList("commands");

            section.set("commands", commands);
        }

        if (entry.contains("items")) {
            final List<String> items = entry.getStringList("items");

            section.set("items", items);
        }

        if (entry.contains("random-commands")) {
            final List<String> random_commands = entry.getStringList("random-commands");

            section.set("random-commands", random_commands);
        }

        if (entry.contains("chance-commands")) {
            final List<String> chance_commands = entry.getStringList("chance-commands");

            section.set("chance-commands", chance_commands);
        }

        if (entry.contains("options.message")) {
            final String message = entry.getString("options.message");

            section.set("options.message", message);
        }

        if (entry.contains("options.whitelist-worlds.toggle")) {
            final boolean toggle = entry.getBoolean("options.whitelist-worlds.toggle");

            section.set("options.whitelist-worlds.toggle", toggle);
        }

        if (entry.contains("options.whitelist-worlds.message")) {
            final String message = entry.getString("options.whitelist-worlds.message");

            section.set("options.whitelist-worlds.message", message);
        }

        if (entry.contains("options.whitelist-worlds.worlds")) {
            final List<String> worlds = entry.getStringList("options.whitelist-worlds.worlds");

            section.set("options.whitelist-worlds.worlds", worlds);
        }

        if (entry.contains("options.permission.whitelist-permission.toggle")) {
            final boolean toggle = entry.getBoolean("options.permission.whitelist-permission.toggle");

            section.set("options.permission.whitelist-permission.toggle", toggle);
        }

        if (entry.contains("options.permission.whitelist-permission.message")) {
            final String message = entry.getString("options.permission.whitelist-permission.message");

            section.set("options.permission.whitelist-permission.message", message);
        }

        if (entry.contains("options.permission.whitelist-permission.permissions")) {
            final List<String> permissions = entry.getStringList("options.permission.whitelist-permission.permissions");

            section.set("options.permission.whitelist-permission.permissions", permissions);
        }

        if (entry.contains("options.permission.blacklist-permission.toggle")) {
            final boolean toggle = entry.getBoolean("options.permission.blacklist-permission.toggle");

            section.set("options.permission.blacklist-permission.toggle", toggle);
        }

        if (entry.contains("options.permission.blacklist-permission.message")) {
            final String message = entry.getString("options.permission.blacklist-permission.message");

            section.set("options.permission.blacklist-permission.message", message);
        }

        if (entry.contains("options.permission.blacklist-permission.permissions")) {
            final List<String> permissions = entry.getStringList("options.permission.blacklist-permission.permissions");

            section.set("options.permission.blacklist-permission.permissions", permissions);
        }

        if (entry.contains("options.limiter.toggle")) {
            final boolean toggle = entry.getBoolean("options.limiter.toggle");

            section.set("options.limiter.toggle", toggle);
        }

        if (entry.contains("options.limiter.amount")) {
            final int amount = entry.getInt("options.limiter.amount");

            section.set("options.limiter.amount", amount);
        }

        if (entry.contains("options.two-step-authentication")) {
            final boolean two_step_authentication = entry.getBoolean("options.two-step-authentication");

            section.set("options.two-step-authentication", two_step_authentication);
        }

        if (entry.contains("options.sound.toggle")) {
            final boolean toggle = entry.getBoolean("options.sound.toggle");

            section.set("options.sound.toggle", toggle);
        }

        if (entry.contains("options.sound.volume")) {
            final double volume = entry.getDouble("options.sound.volume");

            section.set("options.sound.volume", volume);
        }

        if (entry.contains("options.sound.pitch")) {
            final double pitch = entry.getDouble("options.sound.pitch");

            section.set("options.sound.pitch", pitch);
        }

        if (entry.contains("options.sound.sounds")) {
            final List<String> sounds = entry.getStringList("options.sound.sounds");

            section.set("options.sound.sounds", sounds);
        }

        if (entry.contains("options.firework.toggle")) {
            final boolean toggle = entry.getBoolean("options.firework.toggle");

            section.set("options.firework.toggle", toggle);
        }

        if (entry.contains("options.fireworks.colors")) {
            final String colors = entry.getString("options.fireworks.colors");

            section.set("options.fireworks.colors", colors);
        }
    }
}