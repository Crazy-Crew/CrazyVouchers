package us.crazycrew.crazyvouchers.api.plugin;

import com.badbones69.crazyvouchers.api.FileManager;
import us.crazycrew.crazyvouchers.api.MetricsHandler;
import us.crazycrew.crazyvouchers.api.plugin.migration.MigrationService;
import us.crazycrew.crazyvouchers.common.config.ConfigManager;
import us.crazycrew.crazyvouchers.common.config.types.ConfigKeys;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyvouchers.common.CrazyVouchersPlugin;
import java.io.File;

public class CrazyHandler extends CrazyVouchersPlugin {

    private MetricsHandler metrics;
    private FileManager fileManager;

    public CrazyHandler(File dataFolder) {
        super(dataFolder);
    }

    public void install() {
        super.enable();

        boolean loadOldWay = getConfigManager().getConfig().getProperty(ConfigKeys.mono_file);

        MigrationService migrationService = new MigrationService();
        migrationService.migrate(loadOldWay);

        this.fileManager = new FileManager();
        this.fileManager
                .registerDefaultGenerateFiles("Example.yml", "/vouchers", "/vouchers")
                .registerDefaultGenerateFiles("Example-Arg.yml", "/vouchers", "/vouchers")
                .registerDefaultGenerateFiles("PlayerHead.yml", "/vouchers", "/vouchers")
                .registerDefaultGenerateFiles("Starter-Money.yml", "/codes", "/codes")
                .registerCustomFilesFolder("/vouchers")
                .registerCustomFilesFolder("/codes")
                .setup();

        boolean metrics = getConfigManager().getConfig().getProperty(ConfigKeys.toggle_metrics);

        this.metrics = new MetricsHandler();
        if (metrics) this.metrics.start();
    }

    public void uninstall() {
        // Disable crazyenvoys api.
        super.disable();
    }

    /**
     * Inherited methods.
     */
    @Override
    public @NotNull ConfigManager getConfigManager() {
        return super.getConfigManager();
    }

    public @NotNull FileManager getFileManager() {
        return this.fileManager;
    }

    public @NotNull MetricsHandler getMetrics() {
        return this.metrics;
    }
}