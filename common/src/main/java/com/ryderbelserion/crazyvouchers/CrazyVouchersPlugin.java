package com.ryderbelserion.crazyvouchers;

import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.api.objects.FusionKey;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.kyori.FusionKyori;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.api.CrazyVouchers;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import java.nio.file.Path;
import java.util.List;

@NullMarked
public abstract class CrazyVouchersPlugin<S> extends CrazyVouchers<Component, S> {

    private final ConfigManager configManager;
    private final FileManager fileManager;
    private final FusionKyori<S> fusion;

    public CrazyVouchersPlugin(final FusionKyori<S> fusion, final Path path) {
        super(path);

        this.configManager = new ConfigManager();
        this.fileManager = fusion.getFileManager();
        this.fusion = fusion;
    }

    @Override
    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public FusionKyori<S> getFusion() {
        return this.fusion;
    }

    @Override
    public void loadMessages() {
        final List<Path> paths = this.fileManager.getFilesByPath(this.path.resolve("locale"), ".yml");

        this.fusion.getMessageRegistry().init(action -> {
            for (final Path path : paths) {
                this.fileManager.getYamlFile(path).ifPresentOrElse(customFile -> {
                    final String fileName = customFile.getFileName();

                    final FusionKey key = FusionKey.key(namespace, fileName.equalsIgnoreCase("en-US.yml") ? "default" : fileName.toLowerCase());

                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    for (final Message message : Message.values()) {
                        message.addKey(action, customFile, configuration, key);
                    }
                }, () -> this.fusion.log(Level.info, "Path %s not found in cache.".formatted(path)));
            }
        });
    }

    @Override
    public void disable() {

    }

    @Override
    public void reload() {

    }

    @Override
    public void init() {

    }
}