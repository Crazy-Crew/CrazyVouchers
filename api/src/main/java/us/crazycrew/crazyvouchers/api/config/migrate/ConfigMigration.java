package us.crazycrew.crazyvouchers.api.config.migrate;

import com.ryderbelserion.fusion.core.api.config.migration.MigrationService;
import com.ryderbelserion.fusion.core.api.config.properties.interfaces.IPropertyData;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;

@NullMarked
public class ConfigMigration implements MigrationService {

    @Override
    public void copy(final CommentedConfigurationNode configuration, final IPropertyData property) {
        property.setProperty(
                configuration,
                RootKeys.command_prefix,
                ""
        );
    }
}