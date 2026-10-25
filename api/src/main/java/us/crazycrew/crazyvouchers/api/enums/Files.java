package us.crazycrew.crazyvouchers.api.enums;

import org.jspecify.annotations.NullMarked;
import us.crazycrew.crazyvouchers.VouchersProvider;

import java.nio.file.Path;

@NullMarked
public enum Files {

    config("config.yml");

    private final Path dataPath = VouchersProvider.api().getDataPath();

    private final Path path;

    Files(final String path) {
        this.path = this.dataPath.resolve(path);
    }

    public final Path getPath() {
        return this.path;
    }
}