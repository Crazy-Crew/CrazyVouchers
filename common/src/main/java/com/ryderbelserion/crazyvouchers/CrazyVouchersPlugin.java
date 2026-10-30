package com.ryderbelserion.crazyvouchers;

import com.ryderbelserion.crazyvouchers.storage.StorageManager;
import com.ryderbelserion.crazyvouchers.storage.holder.StorageHolder;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import com.ryderbelserion.fusion.files.enums.FileType;
import org.jspecify.annotations.NonNull;
import us.crazycrew.crazyvouchers.VouchersProvider;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.FileKeys;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.api.objects.FusionKey;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.kyori.FusionKyori;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.api.CrazyVouchers;
import us.crazycrew.crazyvouchers.api.enums.other.FileSystem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public abstract class CrazyVouchersPlugin<S, F extends FusionKyori<S>> extends CrazyVouchers<Component, S> {

    protected final ConfigManager configManager;
    protected final FileManager fileManager;
    protected final F fusion;

    public CrazyVouchersPlugin(@NonNull final F fusion, @NonNull final Path path) {
        super(path);

        this.configManager = new ConfigManager(path);
        this.fileManager = fusion.getFileManager();
        this.fusion = fusion;
    }

    protected StorageManager storageManager;
    protected StorageHolder storageHolder;
    protected PropertyManager config;

    @Override
    public void init() {
        this.configManager.init();

        this.storageManager = new StorageManager(this);

        this.storageHolder = this.storageManager.init();
        this.storageHolder.save();

        this.config = this.configManager.getConfig();

        final Path version = this.path.resolve("version.json");

        try {
            Files.deleteIfExists(version);
        } catch (final IOException exception) {
            this.fusion.log(Level.warn, "Failed to delete version.json!", exception);
        }

        final Path locale = this.path.resolve("locale");
        final Path path = this.path.resolve("messages.yml");

        final boolean isPresent = Files.exists(path);

        if (!isPresent) { // if messages.yml isn't present, copy Messages.yml to messages.yml
            move(this.path.resolve("Messages.yml"), path);
        }

        if (!isPresent) { // if messages.yml isn't present, copy en-US.yml!
            move(locale.resolve("en-US.yml"), path);
        }

        for (final FileKeys file : FileKeys.values()) {
            file.addFile();
        }

        loadExamples();

        this.fileManager.addFolder(locale, FileType.YAML);

        final FileSystem system = this.config.getProperty(RootKeys.file_system);

        switch (system) {
            case SINGLE -> this.fileManager.addFile(path.resolve("codes.yml"), FileType.YAML).addFile(path.resolve("vouchers.yml"), FileType.YAML);

            case MULTIPLE -> this.fileManager.addFolder(path.resolve("codes"), FileType.YAML).addFolder(path.resolve("vouchers"), FileType.YAML);
        }

        VouchersProvider.register(this);
    }

    @Override
    public void disable() {

    }

    @Override
    public void reload() {
        this.fusion.reload();

        final Path version = this.path.resolve("version.json");

        try {
            Files.deleteIfExists(version);
        } catch (final IOException exception) {
            this.fusion.log(Level.warn, "Failed to delete version.json!", exception);
        }

        this.fileManager.refresh(false).purge();

        for (final FileKeys file : FileKeys.values()) {
            file.addFile();
        }

        this.storageHolder.save();

        loadExamples();

        final Path locale = this.path.resolve("locale");

        this.fileManager.addFolder(locale, FileType.YAML);

        loadMessages();

        final FileSystem system = this.config.getProperty(RootKeys.file_system);

        switch (system) {
            case SINGLE -> this.fileManager.addFile(this.path.resolve("codes.yml"), FileType.YAML).addFile(this.path.resolve("vouchers.yml"), FileType.YAML);

            case MULTIPLE -> this.fileManager.addFolder(this.path.resolve("codes"), FileType.YAML).addFolder(this.path.resolve("vouchers"), FileType.YAML);
        }
    }

    @Override
    public void loadMessages() {
        final List<Path> paths = this.fileManager.getFilesByPath(this.path.resolve("locale"), ".yml");

        paths.add(this.path.resolve("messages.yml")); // add to list

        this.fusion.getMessageRegistry().init(action -> {
            for (final Path path : paths) {
                this.fileManager.getYamlFile(path).ifPresentOrElse(customFile -> {
                    final String fileName = customFile.getFileName();

                    final FusionKey key = FusionKey.key(namespace, fileName.equalsIgnoreCase("messages.yml") ? "default" : fileName.toLowerCase());

                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    for (final Message message : Message.values()) {
                        message.addKey(action, customFile, configuration, key);
                    }
                }, () -> this.fusion.log(Level.info, "Path %s not found in cache.".formatted(path)));
            }
        });
    }

    @Override
    public void loadExamples() {
        final PropertyManager config = this.configManager.getConfig();

        if (config.getProperty(RootKeys.update_examples_folder)) {
            final Path examples = this.path.resolve("examples");

            if (Files.exists(examples)) {
                try (final Stream<Path> values = Files.walk(examples)) {
                    values.sorted(Comparator.reverseOrder()).forEach(path -> { // sorted in reverse order, to ensure the directories are empty first.
                        try {
                            this.fusion.log(Level.warn, "Successfully deleted path %s, re-generating the examples later.", path);

                            Files.delete(path);
                        } catch (final IOException exception) {
                            this.fusion.log(Level.warn, "Failed to delete %s in loop.", exception, path);
                        }
                    });
                } catch (final Exception exception) {
                    this.fusion.log(Level.warn, "Failed to delete %s.", exception, exception);
                }
            }

            this.fileManager.extractFolder("vouchers", FileType.YAML, examples);
            this.fileManager.extractFolder("codes", FileType.YAML, examples);
            this.fileManager.extractFolder("locale", FileType.YAML, examples);

            List.of(
                    "config.yml",
                    "codes.yml",
                    "data.yml",
                    "users.yml",
                    "vouchers.yml"
            ).forEach(file -> this.fileManager.extractFile(file, examples.resolve(file)));
        }
    }

    @Override
    public @NonNull List<Path> getVoucherFiles() {
        return this.config.getProperty(RootKeys.file_system) == FileSystem.MULTIPLE ? this.fileManager.getFilesByPath(this.path.resolve("vouchers"), ".yml") : List.of(this.path.resolve("vouchers.yml"));
    }

    @Override
    public @NonNull List<Path> getCodeFiles() {
        return this.config.getProperty(RootKeys.file_system) == FileSystem.MULTIPLE ? this.fileManager.getFilesByPath(this.path.resolve("codes"), ".yml") : List.of(this.path.resolve("codes.yml"));
    }

    @Override
    public @NonNull ConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public @NonNull F getFusion() {
        return this.fusion;
    }

    public final StorageHolder getStorageHolder() {
        return this.storageHolder;
    }

    public void move(@NonNull final Path input, @NonNull final Path output) {
        if (Files.exists(input)) {
            try {
                Files.move(input, output, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException exception) {
                this.fusion.log(Level.error, "Failed to copy %s to %s", exception, input, output);
            }
        }
    }
}