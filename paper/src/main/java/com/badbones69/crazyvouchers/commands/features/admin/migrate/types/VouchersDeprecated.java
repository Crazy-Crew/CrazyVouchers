package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
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

                            if (!voucher.contains("item")) {
                                this.fusion.log("warn", "<red>vouchers.{}.item</red> does not exist.", key);

                                continue;
                            }

                            final String displayItem = voucher.getString("item", "");

                            if (!displayItem.contains("#")) {
                                this.fusion.log("warn", "<red>vouchers.{}.item</red> does not contain <red>custom model data</red>, We cannot migrate.", key);

                                continue;
                            }

                            final String[] split = displayItem.split("#");
                            final String material = split[0];
                            final String model_data = split[1];

                            section.set("item", material);
                            section.set("custom-model-data", model_data);
                        }

                        FileKeys.vouchers.save();
                    }

                    success.add("<green>⤷ vouchers.yml");
                } catch (final Exception exception) {
                    failed.add("<red>⤷ vouchers.yml");
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

                    if (!section.contains("item")) {
                        this.fusion.log("warn", "<red>{}</red> does not contain <red>voucher.item</red> path in the configuration section.", name);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final String displayItem = section.getString("item", "");

                    if (!displayItem.contains("#")) {
                        this.fusion.log("warn", "<red>voucher.item</red> does not contain <red>custom model data</red>, We cannot migrate for file {}.", name);

                        failed.add("<red>⤷ " + file);

                        continue;
                    }

                    final String[] split = displayItem.split("#");
                    final String material = split[0];
                    final String model_data = split[1];

                    section.set("item", material);
                    section.set("custom-model-data", model_data);

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
}