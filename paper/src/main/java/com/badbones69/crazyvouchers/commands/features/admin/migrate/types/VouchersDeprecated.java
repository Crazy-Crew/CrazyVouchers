package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.api.enums.FileKeys;
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
import java.util.UUID;

public class VouchersDeprecated extends IVoucherMigrator {

    public VouchersDeprecated(@NotNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_DEPRECATED);
    }

    @Override
    public void run() {
        final List<String> failed = new ArrayList<>();
        final List<String> success = new ArrayList<>();

        switch (this.config.getProperty(ConfigKeys.file_system)) {
            case SINGLE -> {
                final YamlConfiguration vouchers = FileKeys.vouchers.getConfiguration();

                try {
                    final ConfigurationSection section = vouchers.getConfigurationSection("vouchers");

                    if (section != null) {
                        for (final String key : section.getKeys(false)) {
                            final ConfigurationSection voucher = section.getConfigurationSection(key);

                            if (voucher == null) {
                                this.fusion.log("warn", "<red>{}</red> is not a valid configuration section.", key);

                                continue;
                            }

                            if (voucher.contains("item")) {
                                final String displayItem = voucher.getString("item", "");

                                if (displayItem.contains("#")) {
                                    final String[] split = displayItem.split("#");
                                    final String material = split[0];
                                    final String model_data = split[1];

                                    section.set("item", material);
                                    section.set("custom-model-data", model_data);
                                } else {
                                    this.fusion.log("warn", "<red>vouchers.{}.item</red> does not contain <red>custom model data</red>, We cannot migrate.", key);
                                }
                            } else {
                                this.fusion.log("warn", "<red>vouchers.{}.item</red> does not exist.", key);
                            }

                            if (voucher.contains("random-commands") && voucher.isList("random-commands")) {
                                final List<String> commands = voucher.getStringList("random-commands");

                                voucher.set("random-commands", null);

                                final ConfigurationSection newSection = voucher.createSection("random-commands");

                                newSection.set("%s.commands".formatted(UUID.randomUUID()), commands);
                            }

                            if (voucher.contains("chance-commands")) {
                                final List<String> commands = voucher.getStringList("chance-commands");

                                convertCommands(voucher, commands);
                            }
                        }

                        FileKeys.vouchers.save();
                    }

                    success.add("<green>⤷ vouchers.yml");
                } catch (final Exception exception) {
                    failed.add("<red>⤷ vouchers.yml");
                }

                final YamlConfiguration codes = FileKeys.codes.getConfiguration();

                try {
                    final ConfigurationSection section = codes.getConfigurationSection("voucher-codes");

                    if (section != null) {
                        for (final String key : section.getKeys(false)) {
                            final ConfigurationSection voucher = section.getConfigurationSection(key);

                            if (voucher == null) {
                                this.fusion.log("warn", "<red>{}</red> is not a valid configuration section.", key);

                                continue;
                            }

                            if (voucher.contains("random-commands") && voucher.isList("random-commands")) {
                                final List<String> commands = voucher.getStringList("random-commands");

                                voucher.set("random-commands", null);

                                final ConfigurationSection newSection = voucher.createSection("random-commands");

                                newSection.set("%s.commands".formatted(UUID.randomUUID()), commands);
                            }

                            if (voucher.contains("chance-commands")) {
                                final List<String> commands = voucher.getStringList("chance-commands");

                                convertCommands(voucher, commands);
                            }
                        }

                        FileKeys.codes.save();
                    }

                    success.add("<green>⤷ codes.yml");
                } catch (final Exception exception) {
                    failed.add("<red>⤷ codes.yml");
                }
            }

            case MULTIPLE -> {
                final Path voucher_dir = this.dataPath.resolve("vouchers");

                for (final Voucher voucher : this.crazyManager.getVouchers()) {
                    final String name = voucher.getStrippedName();
                    final String file = voucher.getName();

                    final PaperCustomFile customFile = this.fileManager.getPaperCustomFile(voucher_dir.resolve(file));

                    if (customFile == null) {
                        this.fusion.log("warn", "<red>{}</red> does not exist in the file cache", name);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final YamlConfiguration configuration = customFile.getConfiguration();

                    if (configuration == null) {
                        this.fusion.log("warn", "<red>{}</red> configuration is invalid, likely not loaded properly. Please check console :)", name);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final ConfigurationSection section = configuration.getConfigurationSection("voucher");

                    if (section == null) {
                        this.fusion.log("warn", "Configuration section for <red>{}</red> was not found.", name);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    if (section.contains("item")) {
                        final String displayItem = section.getString("item", "");

                        if (displayItem.contains("#")) {
                            final String[] split = displayItem.split("#");
                            final String material = split[0];
                            final String model_data = split[1];

                            section.set("item", material);
                            section.set("custom-model-data", model_data);
                        } else {
                            this.fusion.log("warn", "<red>{}</red> does not contain <red>voucher.item</red> path in the configuration section.", name);
                        }
                    } else {
                        this.fusion.log("warn", "<red>vouchers.{}.item</red> does not exist.", file);
                    }

                    if (section.contains("random-commands") && section.isList("random-commands")) {
                        final List<String> commands = section.getStringList("random-commands");

                        section.set("random-commands", null);

                        final ConfigurationSection newSection = section.createSection("random-commands");

                        newSection.set("%s.commands".formatted(UUID.randomUUID()), commands);
                    }

                    if (section.contains("chance-commands")) {
                        final List<String> commands = section.getStringList("chance-commands");

                        convertCommands(section, commands);
                    }

                    success.add("<green>⤷ " + file);

                    customFile.save();
                }

                final Path code_dir = this.dataPath.resolve("codes");

                for (final VoucherCode voucher : this.crazyManager.getVoucherCodes()) {
                    final String name = voucher.getStrippedName();
                    final String file = voucher.getName();

                    final PaperCustomFile customFile = this.fileManager.getPaperCustomFile(code_dir.resolve(file));

                    if (customFile == null) {
                        this.fusion.log("warn", "<red>{}</red> does not exist in the file cache", name);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final YamlConfiguration configuration = customFile.getConfiguration();

                    if (configuration == null) {
                        this.fusion.log("warn", "<red>{}</red> configuration is invalid, likely not loaded properly. Please check console :)", name);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final ConfigurationSection section = configuration.getConfigurationSection("voucher-code");

                    if (section == null) {
                        this.fusion.log("warn", "Configuration section for <red>{}</red> was not found.", name);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    if (section.contains("random-commands") && section.isList("random-commands")) {
                        final List<String> commands = section.getStringList("random-commands");

                        section.set("random-commands", null);

                        final ConfigurationSection newSection = section.createSection("random-commands");

                        newSection.set("%s.commands".formatted(UUID.randomUUID()), commands);
                    }

                    if (section.contains("chance-commands")) {
                        final List<String> commands = section.getStringList("chance-commands");

                        convertCommands(section, commands);
                    }

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

        this.fileManager.init(new ArrayList<>());

        this.crazyManager.load(true);
    }

    private void convertCommands(@NotNull final ConfigurationSection section, @NotNull final List<String> commands) {
        for (final String command : commands) {
            final ConfigurationSection existingSection = section.getConfigurationSection("random-commands");

            if (existingSection == null) continue;

            final ConfigurationSection commandSection = existingSection.createSection(UUID.randomUUID().toString());

            final String[] chance = command.split(" ");

            commandSection.set("weight", Integer.parseInt(chance[0])); // gets the first 2 numbers of the string if they followed proper syntax.

            if (commandSection.contains("commands")) {
                final List<String> values = commandSection.getStringList("commands");

                values.add(command.replaceFirst(chance[0], "").replaceFirst(" ", ""));

                commandSection.set("commands", values);
            } else {
                commandSection.set("commands", List.of(command.replaceFirst(chance[0], "").replaceFirst(" ", "")));
            }
        }

        section.set("chance-commands", null);
    }
}