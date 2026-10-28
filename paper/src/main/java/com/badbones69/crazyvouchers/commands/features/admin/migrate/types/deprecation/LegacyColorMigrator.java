package com.badbones69.crazyvouchers.commands.features.admin.migrate.types.deprecation;

import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.ryderbelserion.fusion.kyori.utils.AdvUtils;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LegacyColorMigrator extends IVoucherMigrator {

    public LegacyColorMigrator(@NonNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_COLOR);
    }

    @Override
    public void run() {
        final List<String> failed = new ArrayList<>();
        final List<String> success = new ArrayList<>();

        try {
            this.config.setProperty(RootKeys.command_prefix, AdvUtils.convert(this.config.getProperty(RootKeys.command_prefix), true));
            this.config.setProperty(RootKeys.dupe_protection_warning_text, AdvUtils.convert(this.config.getProperty(RootKeys.dupe_protection_warning_text), true));

            success.add("<green>⤷ config.yml");

            this.config.save();
            this.config.reload();
        } catch (Exception exception) {
            failed.add("<red>⤷ config.yml");
        }

        try {
            for (final Message message : Message.values()) {
                message.convert();
            }

            success.add("<green>⤷ messages.yml");
        } catch (Exception exception) {
            failed.add("<red>⤷ messages.yml");
        }

        switch (this.config.getProperty(RootKeys.file_system)) {
            case SINGLE -> {
                this.fileManager.getYamlFile(this.dataPath.resolve("vouchers.yml")).ifPresentOrElse(customFile -> {
                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    if (configuration.hasChild("vouchers")) {
                        for (final Map.Entry<Object, CommentedConfigurationNode> section : configuration.node("vouchers").childrenMap().entrySet()) {
                            final String id = section.toString();

                            processItems(id, section.getValue());
                        }

                        success.add("<green>⤷ vouchers.yml");

                        customFile.save();

                        return;
                    }

                    failed.add("<red>⤷ vouchers.yml");
                }, () -> failed.add("<red>⤷ vouchers.yml"));

                this.fileManager.getYamlFile(this.dataPath.resolve("codes.yml")).ifPresentOrElse(customFile -> {
                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    if (configuration.hasChild("voucher-codes")) {
                        for (final Map.Entry<Object, CommentedConfigurationNode> section : configuration.node("voucher-codes").childrenMap().entrySet()) {
                            final String id = section.toString();

                            processItems(id, section.getValue());
                        }

                        success.add("<green>⤷ codes.yml");

                        customFile.save();

                        return;
                    }

                    failed.add("<red>⤷ codes.yml");
                }, () -> failed.add("<red>⤷ codes.yml"));
            }

            case MULTIPLE -> {
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

                        process(configuration.node("voucher-code"));

                        success.add("<green>⤷ " + fileName);

                        customFile.save();
                    }, () -> {
                        this.fusion.log(Level.warn, "<red>%s</red> does not exist in the file cache", fileName);

                        failed.add("<red>⤷ " + fileName);
                    });
                }

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

                        processItems(customFile.getPrettyName(), configuration.node("voucher"));

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

    private void processItems(@NonNull final String name, @NonNull final CommentedConfigurationNode section) {
        setMessage(section.node("lore"), "");
        setMessage(section.node("name"), name);

        process(section);
    }

    private void process(@NonNull final CommentedConfigurationNode section) {
        setMessage(section.node("options", "message"), "");
        setMessage(section.node("options", "whitelist-worlds", "message"), "{prefix}You are not in any of the whitelisted worlds.");

        setMessage(section.node("options", "permission", "whitelist-permission", "message"), "{prefix}You do not have the permission <red>{permission} <gray>to use this voucher.");

        setMessage(section.node("options", "permission", "blacklist-permission", "message"), "{prefix}You already have the permission <red>{permission} <gray>so you can''t use this voucher.");
    }

    @Override
    public @NonNull final Path getVouchersDirectory() {
        return this.dataPath.resolve("vouchers");
    }

    @Override
    public @NonNull final Path getCodesDirectory() {
        return this.dataPath.resolve("codes");
    }

    private void setMessage(@NonNull final CommentedConfigurationNode section, @NonNull final String defaultValue) {
        try {
            if (section.isList()) {
                section.setList(String.class, AdvUtils.convert(StringUtils.getStringList(section), true));

                return;
            }

            section.set(String.class, AdvUtils.convert(section.getString(defaultValue), true));
        } catch (final SerializationException exception) {
            exception.printStackTrace();
        }
    }
}