package us.crazycrew.crazyvouchers.api.enums;

import com.ryderbelserion.fusion.api.exceptions.FusionException;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.files.enums.FileType;
import com.ryderbelserion.fusion.files.types.configurate.JsonCustomFile;
import com.ryderbelserion.fusion.files.types.configurate.YamlCustomFile;
import com.ryderbelserion.fusion.kyori.FusionKyori;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.VouchersProvider;
import us.crazycrew.crazyvouchers.api.CrazyVouchers;
import java.nio.file.Path;
import java.util.Optional;

@NullMarked
public enum FileKeys {

    config("config.yml", FileType.YAML),
    users("users.yml", FileType.YAML),
    data("data.yml", FileType.YAML),
    version("version.json", FileType.JSON);

    private final CrazyVouchers plugin = VouchersProvider.api();
    private final FusionKyori fusion = this.plugin.getFusion();
    private final FileManager fileManager = this.fusion.getFileManager();

    private final FileType fileType;
    private final Path path;

    FileKeys(final String path, final FileType fileType) {
        this.path = this.plugin.getDataPath().resolve(path);
        this.fileType = fileType;
    }

    public final JsonCustomFile getJsonCustomFile() {
        final Optional<JsonCustomFile> customFile = this.fileManager.getJsonFile(this.path);

        if (customFile.isEmpty()) {
            throw new FusionException("Could not find custom file for " + this.path);
        }

        return customFile.get();
    }

    public final BasicConfigurationNode getJsonConfiguration() {
        return getJsonCustomFile().getConfiguration();
    }

    public final YamlCustomFile getYamlCustomFile() {
        final Optional<YamlCustomFile> customFile = this.fileManager.getYamlFile(this.path);

        if (customFile.isEmpty()) {
            throw new FusionException("Could not find custom file for " + this.path);
        }

        return customFile.get();
    }

    public final CommentedConfigurationNode getConfiguration() {
        return getYamlCustomFile().getConfiguration();
    }

    public void saveFile() {
        this.fileManager.saveFile(this.path);
    }

    public void addFile() {
        this.fileManager.addFile(this.path, this.fileType);
    }

    public final Path getPath() {
        return this.path;
    }
}