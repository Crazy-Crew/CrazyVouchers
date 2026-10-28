package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VoucherDeprecated extends IVoucherMigrator {

    public VoucherDeprecated(@NonNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_DEPRECATED);
    }

    @Override
    public void run() {
        final List<String> failed = new ArrayList<>();
        final List<String> success = new ArrayList<>();

        switch (this.config.getProperty(RootKeys.file_system)) {
            case SINGLE -> {
                this.fileManager.getYamlFile(this.dataPath.resolve("vouchers.yml")).ifPresentOrElse(customFile -> {
                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    try {
                        final CommentedConfigurationNode section = configuration.node("vouchers");

                        for (final Map.Entry<Object, CommentedConfigurationNode> entries : section.childrenMap().entrySet()) {
                            final String id = entries.getKey().toString();
                            final CommentedConfigurationNode entry = entries.getValue();

                            if (entry.hasChild("item")) {
                                final String item = entry.node("item").getString("");

                                if (!item.isBlank() && item.contains("#")) {
                                    final String[] split = item.split("#");
                                    final String material = split[0];
                                    final String model_data = split[1];

                                    set(entry.node("item"), material, String.class);
                                    set(entry.node("custom-model-data"), model_data, String.class, model_data);
                                } else {
                                    this.fusion.log(Level.warn, "<red>vouchers.%s.item</red> does not contain <red>custom model data</red>, We cannot migrate.", id);
                                }
                            } else {
                                this.fusion.log(Level.warn, "<red>vouchers.%s.item</red> does not exist.", id);
                            }

                            if (entry.hasChild("random-commands")) {
                                final CommentedConfigurationNode index = entry.node("random-commands");

                                if (index.isList()) {
                                    final List<String> commands = StringUtils.getStringList(index);

                                    set(index, null, List.class);

                                    set(index.node(UUID.randomUUID().toString()), commands, List.class);
                                }
                            }

                            if (entry.hasChild("chance-commands")) {
                                convertCommands(entry, StringUtils.getStringList(entry.node("chance-commands")));
                            }
                        }

                        customFile.save();

                        success.add("<green>⤷ vouchers.yml");
                    } catch (final Exception exception) {
                        failed.add("<red>⤷ vouchers.yml");
                    }
                }, () -> failed.add("<red>⤷ vouchers.yml"));

                this.fileManager.getYamlFile(this.dataPath.resolve("codes.yml")).ifPresentOrElse(customFile -> {
                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    try {
                        final CommentedConfigurationNode section = configuration.node("voucher-codes");

                        for (final Map.Entry<Object, CommentedConfigurationNode> entries : section.childrenMap().entrySet()) {
                            final String id = entries.getKey().toString();
                            final CommentedConfigurationNode entry = entries.getValue();

                            if (entry.hasChild("item")) {
                                final String item = entry.node("item").getString("");

                                if (!item.isBlank() && item.contains("#")) {
                                    final String[] split = item.split("#");
                                    final String material = split[0];
                                    final String model_data = split[1];

                                    set(entry.node("item"), material, String.class);
                                    set(entry.node("custom-model-data"), model_data, String.class, model_data);
                                } else {
                                    this.fusion.log(Level.warn, "<red>vouchers.%s.item</red> does not contain <red>custom model data</red>, We cannot migrate.", id);
                                }
                            } else {
                                this.fusion.log(Level.warn, "<red>vouchers.%s.item</red> does not exist.", id);
                            }

                            if (entry.hasChild("random-commands")) {
                                final CommentedConfigurationNode index = entry.node("random-commands");

                                if (index.isList()) {
                                    final List<String> commands = StringUtils.getStringList(index);

                                    set(index, null, List.class);

                                    set(index.node(UUID.randomUUID().toString()), commands, List.class);
                                }
                            }

                            if (entry.hasChild("chance-commands")) {
                                convertCommands(entry, StringUtils.getStringList(entry.node("chance-commands")));
                            }
                        }

                        customFile.save();

                        success.add("<green>⤷ codes.yml");
                    } catch (final Exception exception) {
                        failed.add("<red>⤷ codes.yml");
                    }
                }, () -> failed.add("<red>⤷ codes.yml"));
            }

            case MULTIPLE -> {
                for (final Path path : this.fusion.getFilesByPath(this.dataPath.resolve("vouchers"), ".yml")) {
                    final String fileName = path.getFileName().toString();

                    this.fileManager.getYamlFile(path).ifPresentOrElse(customFile -> {
                        if (!customFile.isLoaded()) {
                            this.fusion.log(Level.warn, "<red>%s</red> configuration is invalid, likely not loaded properly. Please check console :)", fileName);

                            failed.add("<red>⤷ " + fileName);

                            return;
                        }

                        final CommentedConfigurationNode configuration = customFile.getConfiguration();

                        if (!configuration.hasChild("voucher")) {
                            this.fusion.log(Level.warn, "Configuration section for <red>%s</red> was not found.", fileName);

                            failed.add("<red>⤷ " + fileName);

                            return;
                        }

                        if (configuration.hasChild("item")) {
                            final String item = configuration.node("item").getString("");

                            if (!item.isBlank() && item.contains("#")) {
                                final String[] split = item.split("#");
                                final String material = split[0];
                                final String model_data = split[1];

                                set(configuration.node("item"), material, String.class);
                                set(configuration.node("custom-model-data"), model_data, String.class, model_data);
                            } else {
                                this.fusion.log(Level.warn, "<red>voucher.item</red> does not contain <red>custom model data</red>, We cannot migrate for %s.", fileName);
                            }
                        } else {
                            this.fusion.log(Level.warn, "<red>voucher.item</red> does not exist for %s.", fileName);
                        }

                        if (configuration.hasChild("random-commands")) {
                            final CommentedConfigurationNode index = configuration.node("random-commands");

                            if (index.isList()) {
                                final List<String> commands = StringUtils.getStringList(index);

                                set(index, null, List.class);

                                set(index.node(UUID.randomUUID().toString()), commands, List.class);
                            }
                        }

                        if (configuration.hasChild("chance-commands")) {
                            convertCommands(configuration, StringUtils.getStringList(configuration.node("chance-commands")));
                        }

                        success.add("<green>⤷ " + fileName);

                        customFile.save();
                    }, () -> {
                        this.fusion.log(Level.warn, "<red>%s</red> does not exist in the file cache", fileName);

                        failed.add("<red>⤷ " + fileName);
                    });
                }

                for (final Path path : this.fusion.getFilesByPath(this.dataPath.resolve("codes"), ".yml")) {
                    final String fileName = path.getFileName().toString();

                    this.fileManager.getYamlFile(path).ifPresentOrElse(customFile -> {
                        if (!customFile.isLoaded()) {
                            this.fusion.log(Level.warn, "<red>%s</red> configuration is invalid, likely not loaded properly. Please check console :)", fileName);

                            failed.add("<red>⤷ " + fileName);

                            return;
                        }

                        final CommentedConfigurationNode configuration = customFile.getConfiguration();

                        if (!configuration.hasChild("voucher-code")) {
                            this.fusion.log(Level.warn, "Configuration section for <red>%s</red> was not found.", fileName);

                            failed.add("<red>⤷ " + fileName);

                            return;
                        }

                        if (configuration.hasChild("item")) {
                            final String item = configuration.node("item").getString("");

                            if (!item.isBlank() && item.contains("#")) {
                                final String[] split = item.split("#");
                                final String material = split[0];
                                final String model_data = split[1];

                                set(configuration.node("item"), material, String.class);
                                set(configuration.node("custom-model-data"), model_data, String.class, model_data);
                            } else {
                                this.fusion.log(Level.warn, "<red>voucher-code.item</red> does not contain <red>custom model data</red>, We cannot migrate for %s.", fileName);
                            }
                        } else {
                            this.fusion.log(Level.warn, "<red>voucher-code.item</red> does not exist for %s.", fileName);
                        }

                        if (configuration.hasChild("random-commands")) {
                            final CommentedConfigurationNode index = configuration.node("random-commands");

                            if (index.isList()) {
                                final List<String> commands = StringUtils.getStringList(index);

                                set(index, null, List.class);

                                set(index.node(UUID.randomUUID().toString()), commands, List.class);
                            }
                        }

                        if (configuration.hasChild("chance-commands")) {
                            convertCommands(configuration, StringUtils.getStringList(configuration.node("chance-commands")));
                        }

                        success.add("<green>⤷ " + fileName);

                        customFile.save();
                    }, () -> {
                        this.fusion.log(Level.warn, "<red>%s</red> does not exist in the file cache", fileName);

                        failed.add("<red>⤷ " + fileName);
                    });
                }
            }
        }

        final int convertedCount = success.size();
        final int failedCount = failed.size();

        final List<String> files = new ArrayList<>(failedCount + convertedCount);

        files.addAll(failed);
        files.addAll(success);

        sendMessage(files, convertedCount, failedCount);

        this.crazyManager.reloadVouchers();
        this.crazyManager.reloadCodes();
    }

    private void convertCommands(@NonNull final CommentedConfigurationNode section, @NonNull final List<String> commands) {
        final CommentedConfigurationNode index = section.node("random-commands");

        for (final String command : commands) {
            final CommentedConfigurationNode entry = index.node(UUID.randomUUID().toString());

            final String[] chance = command.split(" ");

            set(entry, Double.parseDouble(chance[0]), Double.class, "weight");

            if (entry.hasChild("commands")) {
                final List<String> values = StringUtils.getStringList(entry.node("commands"));

                values.add(command.replaceFirst(chance[0], "").replaceFirst(" ", ""));

                set(entry.node("commands"), values, List.class);
            } else {
                set(entry.node("commands"), List.of(command.replaceFirst(chance[0], "").replaceFirst(" ", "")), List.class);
            }
        }

        set(section.node("chance-commands"), null, List.class);
    }
}