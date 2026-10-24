package com.badbones69.crazyvouchers.api.enums;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.api.exceptions.FusionException;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.files.enums.FileType;
import com.ryderbelserion.fusion.files.types.configurate.JsonCustomFile;
import com.ryderbelserion.fusion.files.types.configurate.YamlCustomFile;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import java.nio.file.Path;
import java.util.Optional;

public enum FileKeys {

    vouchers(FileType.YAML, "vouchers.yml"),
    codes(FileType.YAML, "codes.yml"),
    users(FileType.YAML, "users.yml"),
    data(FileType.YAML, "data.yml"),

    version(FileType.JSON, "versions.json");

    private final FileType fileType;
    private final String name;
    private final Path path;

    private @NonNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NonNull final FileManager fileManager = this.plugin.getFileManager();

    /**
     * A constructor to build a file
     *
     * @param fileName the name of the file
     */
    FileKeys(@NonNull final FileType fileType, @NonNull final String fileName) {
        this.path = this.plugin.getDataPath().resolve(fileName);
        this.fileType = fileType;
        this.name = fileName;
    }

    public @NonNull final BasicConfigurationNode getConfigurationNode() {
        return getJsonCustomFile().getConfiguration();
    }

    public @NonNull final JsonCustomFile getJsonCustomFile() {
        final Optional<JsonCustomFile> customFile = this.fileManager.getJsonFile(this.path);

        if (customFile.isEmpty()) {
            throw new FusionException("Could not find custom file for " + this.path);
        }

        return customFile.get();
    }

    public @NonNull final CommentedConfigurationNode getConfiguration() {
        return getPaperCustomFile().getConfiguration();
    }

    public @NonNull final YamlCustomFile getPaperCustomFile() {
        final Optional<YamlCustomFile> customFile = this.fileManager.getYamlFile(this.path);

        if (customFile.isEmpty()) {
            throw new FusionException("Could not find custom file for " + this.path);
        }

        return customFile.get();
    }

    public @NonNull final FileType getFileType() {
        return this.fileType;
    }

    public @NonNull final Path getPath() {
        return this.path;
    }

    public final String getName() {
        return this.name;
    }

    public void save() {
        this.fileManager.saveFile(this.path);
    }
}