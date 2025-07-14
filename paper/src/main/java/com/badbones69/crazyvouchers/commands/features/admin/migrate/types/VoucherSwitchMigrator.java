package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.core.api.utils.FileUtils;
import com.ryderbelserion.fusion.paper.files.types.PaperCustomFile;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VoucherSwitchMigrator extends IVoucherMigrator {

    public VoucherSwitchMigrator(@NotNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_SWITCH);
    }

    @Override
    public void run() {
        switch (this.config.getProperty(ConfigKeys.file_system)) {
            case SINGLE -> {
                final Path voucher_directory = getVouchersDirectory();
                final Path code_directory = getCodesDirectory();

                final Path voucher_file = this.dataPath.resolve("vouchers.yml");

                YamlConfiguration voucher_config = null;

                if (Files.exists(voucher_file)) {
                    voucher_config = YamlConfiguration.loadConfiguration(voucher_file.toFile());
                }

                if (voucher_config != null) {
                    final ConfigurationSection vouchers = voucher_config.getConfigurationSection("vouchers");

                    if (vouchers != null) {
                        for (final String key : vouchers.getKeys(false)) {
                            final ConfigurationSection entry = vouchers.getConfigurationSection(key);

                            final Path path = voucher_directory.resolve(key);

                            if (!Files.exists(path)) {
                                try {
                                    Files.createFile(path);
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                }
                            }

                            final PaperCustomFile customFile = new PaperCustomFile(path, new ArrayList<>());

                            customFile.load();

                            final YamlConfiguration configuration = customFile.getConfiguration();

                            if (configuration == null) continue;

                            if (entry == null) continue;

                            final ConfigurationSection section = configuration.createSection("voucher");

                            processItems(entry, section);
                        }
                    }
                }

                final Path code_file = this.dataPath.resolve("codes.yml");

                YamlConfiguration code_config = null;

                if (Files.exists(code_file)) {
                    code_config = YamlConfiguration.loadConfiguration(code_file.toFile());
                }

                if (code_config != null) {
                    final ConfigurationSection codes = code_config.getConfigurationSection("voucher-codes");

                    if (codes != null) {
                        for (final String key : codes.getKeys(false)) {
                            final ConfigurationSection entry = codes.getConfigurationSection(key);

                            final Path path = code_directory.resolve(key);

                            if (!Files.exists(path)) {
                                try {
                                    Files.createFile(path);
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                }
                            }

                            final PaperCustomFile customFile = new PaperCustomFile(path, new ArrayList<>());

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
                final Path voucher_file = this.dataPath.resolve("vouchers.yml");

                if (Files.exists(voucher_file)) {
                    try {
                        Files.createFile(voucher_file);
                    } catch (final IOException exception) {
                        exception.printStackTrace();
                    }
                }

                final PaperCustomFile voucher_custom_file = new PaperCustomFile(voucher_file, new ArrayList<>()).load();

                final YamlConfiguration voucher_config = voucher_custom_file.getConfiguration();

                if (voucher_config != null) {
                    final ConfigurationSection new_section = voucher_config.contains("vouchers") ? voucher_config.getConfigurationSection("vouchers") : voucher_config.createSection("vouchers");

                    if (new_section != null) {
                        final List<Path> vouchers = FileUtils.getFiles(this.dataPath.resolve("vouchers"), ".yml");

                        for (final Path voucher : vouchers) {
                            final String fileName = voucher.getFileName().toString();

                            final PaperCustomFile customFile = new PaperCustomFile(voucher, new ArrayList<>());

                            final YamlConfiguration configuration = customFile.getConfiguration();

                            if (configuration == null) continue;

                            final ConfigurationSection entry = configuration.getConfigurationSection("voucher");

                            if (entry == null) continue;

                            final ConfigurationSection section = new_section.createSection(fileName);

                            processItems(entry, section);

                            voucher_custom_file.save();
                        }

                        this.fileManager.addFile(voucher_custom_file);
                        this.fileManager.removeFile(voucher_file, null);
                    }
                }

                final Path code_file = this.dataPath.resolve("codes.yml");

                if (Files.exists(code_file)) {
                    try {
                        Files.createFile(code_file);
                    } catch (final IOException exception) {
                        exception.printStackTrace();
                    }
                }

                final PaperCustomFile code_custom_file = new PaperCustomFile(code_file, new ArrayList<>()).load();

                final YamlConfiguration code_config = code_custom_file.getConfiguration();

                if (code_config != null) {
                    final ConfigurationSection new_section = code_config.contains("voucher-codes") ? code_config.getConfigurationSection("voucher-codes") : code_config.createSection("voucher-codes");

                    if (new_section != null) {
                        final List<Path> codes = FileUtils.getFiles(this.plugin.getDataPath().resolve("codes"), ".yml");

                        for (final Path code : codes) {
                            final String fileName = code.getFileName().toString();

                            final PaperCustomFile customFile = new PaperCustomFile(code, new ArrayList<>());

                            final YamlConfiguration configuration = customFile.getConfiguration();

                            if (configuration == null) continue;

                            final ConfigurationSection entry = configuration.getConfigurationSection("voucher-code");

                            if (entry == null) continue;

                            final ConfigurationSection section = new_section.createSection(fileName);

                            processItems(entry, section);

                            code_custom_file.save();
                        }

                        this.fileManager.addFile(code_custom_file);
                        this.fileManager.removeFile(code_file, null);
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
    public final Path getVouchersDirectory() {
        return this.dataPath.resolve("vouchers");
    }

    @Override
    public final Path getCodesDirectory() {
        return this.dataPath.resolve("codes");
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