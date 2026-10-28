package us.crazycrew.crazyvouchers.api;

import com.ryderbelserion.fusion.kyori.FusionKyori;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import us.crazycrew.crazyvouchers.api.adapters.sender.ISenderAdapter;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@NullMarked
public abstract class CrazyVouchers<C, S> {

    public static final UUID CONSOLE_UUID = new UUID(0, 0);
    public static final String CONSOLE_NAME = "Console";
    public static final String namespace = "crazyvouchers";

    protected final Path path;

    public CrazyVouchers(final Path path) {
        this.path = path;
    }

    public abstract List<Path> getVoucherFiles();

    public abstract List<Path> getCodeFiles();

    @ApiStatus.Internal
    public abstract ISenderAdapter<C, S> getSenderAdapter();

    @ApiStatus.Internal
    public abstract ConfigManager getConfigManager();

    @ApiStatus.Internal
    public abstract FusionKyori getFusion();

    public Path getDataPath() {
        return this.path;
    }

    @ApiStatus.Internal
    public abstract void loadMessages();

    @ApiStatus.Internal
    public abstract void loadExamples();

    public abstract void updateVouchers();

    public abstract void loadVouchers();

    public abstract void updateCodes();

    public abstract void loadCodes();

    public abstract void disable();

    public abstract void reload();

    public abstract void init();

}