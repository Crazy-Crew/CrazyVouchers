package com.ryderbelserion.crazyvouchers;

import com.ryderbelserion.fusion.files.enums.FileType;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.FileKeys;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.api.objects.FusionKey;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.kyori.FusionKyori;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.api.CrazyVouchers;
import us.crazycrew.crazyvouchers.api.enums.other.FileSystem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@NullMarked
public abstract class CrazyVouchersPlugin<S, F extends FusionKyori<S>> extends CrazyVouchers<Component, S> {

    private final ConfigManager configManager;
    private final FileManager fileManager;
    private final F fusion;

    public CrazyVouchersPlugin(final F fusion, final Path path) {
        super(path);

        this.configManager = new ConfigManager();
        this.fileManager = fusion.getFileManager();
        this.fusion = fusion;
    }

    @Override
    public void init() {
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

        this.fileManager.addFolder(locale, FileType.YAML);

        final FileSystem system = this.configManager.getConfig().getProperty(RootKeys.file_system);

        switch (system) {
            case SINGLE -> this.fileManager.addFile(path.resolve("codes.yml"), FileType.YAML).addFile(path.resolve("vouchers.yml"), FileType.YAML);

            case MULTIPLE -> this.fileManager.addFolder(path.resolve("codes"), FileType.YAML).addFolder(path.resolve("vouchers"), FileType.YAML);
        }
    }

    @Override
    public void disable() {

    }

    @Override
    public void reload() {
        this.fileManager.refresh(false);
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
    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public F getFusion() {
        return this.fusion;
    }

    public void move(final Path input, final Path output) {
        if (Files.exists(input)) {
            try {
                Files.move(input, output, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException exception) {
                this.fusion.log(Level.error, "Failed to copy %s to %s", exception, input, output);
            }
        }
    }
}