package com.badbones69.crazyvouchers.api.enums;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.paper.files.FileManager;
import com.ryderbelserion.fusion.paper.files.types.PaperCustomFile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;

public enum FileKeys {

    vouchers("vouchers.yml"),
    codes("codes.yml"),
    users("users.yml"),
    data("data.yml");

    private final Path path;

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NotNull final Path dataPath = this.plugin.getDataPath();

    private @NotNull final FileManager fileManager = this.plugin.getFileManager();

    /**
     * A constructor to build a file
     *
     * @param fileName the name of the file
     */
    FileKeys(@NotNull final String fileName) {
        this.path = this.dataPath.resolve(fileName);
    }

    public final YamlConfiguration getConfiguration() {
        return getCustomFile().getConfiguration();
    }

    public final PaperCustomFile getCustomFile() {
        return this.fileManager.getPaperCustomFile(this.path);
    }

    public void save() {
        this.fileManager.saveFile(this.path);
    }
}