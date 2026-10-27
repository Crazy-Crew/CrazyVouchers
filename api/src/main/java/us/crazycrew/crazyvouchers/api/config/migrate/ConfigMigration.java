package us.crazycrew.crazyvouchers.api.config.migrate;

import com.ryderbelserion.fusion.core.api.config.migration.MigrationService;
import com.ryderbelserion.fusion.core.api.config.properties.interfaces.IPropertyData;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.api.config.enums.PropertyStorage;

@NullMarked
public class ConfigMigration implements MigrationService {

    @Override
    public boolean copy(final CommentedConfigurationNode configuration, final IPropertyData property) {
        return PropertyStorage.command_prefix.copy(configuration, property)
                || PropertyStorage.must_be_in_survival.copy(configuration, property)
                || PropertyStorage.prevent_using_vouchers_in_recipes_toggle.copy(configuration, property)
                || PropertyStorage.prevent_using_vouchers_in_recipes_alert.copy(configuration, property)
                || PropertyStorage.file_system.copy(configuration, property);
    }
}