package com.badbones69.crazyvouchers.api.objects.other;

import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.vital.core.Vital;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import java.io.File;

public class Server extends Vital {

    private final File directory;
    private final ComponentLogger logger;

    public Server(@NotNull final File directory, @NotNull final ComponentLogger logger) {
        this.directory = directory;
        this.logger = logger;

        ConfigManager.load(this.directory);
    }

    /**
     * Reloads the plugin.
     */
    public void reload() {
        ConfigManager.refresh();
    }

    /**
     * @return the plugin directory
     */
    @Override
    public @NotNull final File getDirectory() {
        return this.directory;
    }

    /**
     * @return the plugin logger
     */
    @Override
    public @NotNull final ComponentLogger getLogger() {
        return this.logger;
    }

    @Override
    public boolean isLogging() {
        return ConfigManager.getConfig().getProperty(ConfigKeys.verbose_logging);
    }
}