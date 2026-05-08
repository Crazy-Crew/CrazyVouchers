package com.badbones69.crazyvouchers.api.enums;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.core.api.exceptions.FusionException;
import com.ryderbelserion.fusion.files.enums.FileType;
import com.ryderbelserion.fusion.files.types.configurate.JsonCustomFile;
import com.ryderbelserion.fusion.paper.files.PaperFileManager;
import com.ryderbelserion.fusion.paper.files.types.PaperCustomFile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.BasicConfigurationNode;

import java.nio.file.Path;
import java.util.Optional;

public enum FileKeys {

    vouchers(FileType.PAPER_YAML, "vouchers.yml"),
    codes(FileType.PAPER_YAML, "codes.yml"),
    users(FileType.PAPER_YAML, "users.yml"),
    data(FileType.PAPER_YAML, "data.yml"),

    version(FileType.JSON, "versions.json");

    private final FileType fileType;
    private final String name;
    private final Path path;

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NotNull final PaperFileManager fileManager = this.plugin.getFileManager();

    /**
     * A constructor to build a file
     *
     * @param fileName the name of the file
     */
    FileKeys(@NotNull final FileType fileType, @NotNull final String fileName) {
        this.path = this.plugin.getDataPath().resolve(fileName);
        this.fileType = fileType;
        this.name = fileName;
    }

    public @NotNull final BasicConfigurationNode getConfigurationNode() {
        return getJsonCustomFile().getConfiguration();
    }

    public @NotNull final JsonCustomFile getJsonCustomFile() {
        final Optional<JsonCustomFile> customFile = this.fileManager.getJsonFile(this.path);

        if (customFile.isEmpty()) {
            throw new FusionException("Could not find custom file for " + this.path);
        }

        return customFile.get();
    }

    public @NotNull final YamlConfiguration getConfiguration() {
        return getPaperCustomFile().getConfiguration();
    }

    public @NotNull final PaperCustomFile getPaperCustomFile() {
        final Optional<PaperCustomFile> customFile = this.fileManager.getPaperFile(this.path);

        if (customFile.isEmpty()) {
            throw new FusionException("Could not find custom file for " + this.path);
        }

        return customFile.get();
    }

    public @NotNull final FileType getFileType() {
        return this.fileType;
    }

    public @NotNull final Path getPath() {
        return this.path;
    }

    public final String getName() {
        return this.name;
    }

    public void save() {
        this.fileManager.saveFile(this.path);
    }
}