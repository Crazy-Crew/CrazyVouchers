package us.crazycrew.crazyvouchers.api.config;

import com.ryderbelserion.fusion.core.api.config.ConfigBuilder;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.enums.NodeStyle;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.Files;

public final class ConfigManager {

    private PropertyManager config;

    public void init() {
        this.config = ConfigBuilder.withYamlPath(Files.config.getPath())
                .configuration(RootKeys.class)
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