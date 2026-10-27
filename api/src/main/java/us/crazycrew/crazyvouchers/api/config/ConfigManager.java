package us.crazycrew.crazyvouchers.api.config;

import com.ryderbelserion.fusion.core.api.config.ConfigBuilder;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.enums.NodeStyle;
import us.crazycrew.crazyvouchers.api.config.migrate.ConfigMigration;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.FileKeys;

public final class ConfigManager {

    private PropertyManager config;

    public void init() {
        this.config = ConfigBuilder.withYamlPath(FileKeys.config.getPath())
                .configuration(RootKeys.class)
                .withMigrationService(new ConfigMigration())
                .withNodeStyle(NodeStyle.BLOCK)
                .withHeaderMode(HeaderMode.PRESERVE)
                .withIndent(1)
                .create();
    }

    public void reload() {
        this.config.reload();
    }

    public @NonNull PropertyManager getConfig() {
        return this.config;
    }
}