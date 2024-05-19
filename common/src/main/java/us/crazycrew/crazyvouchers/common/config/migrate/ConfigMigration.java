package us.crazycrew.crazyvouchers.common.config.migrate;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.migration.PlainMigrationService;
import ch.jalu.configme.resource.PropertyReader;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyvouchers.common.api.enums.FileProperty;

public class ConfigMigration extends PlainMigrationService {

    @Override
    protected boolean performMigrations(@NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
        return migrateLocale(reader, configurationData);
    }

    private boolean migrateLocale(@NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
        return FileProperty.command_prefix.moveString(reader, configurationData)
                | FileProperty.must_be_in_survival.moveBoolean(reader, configurationData)
                | FileProperty.prevent_using_vouchers_in_recipes_toggle.moveBoolean(reader, configurationData)
                | FileProperty.prevent_using_vouchers_in_recipes_alert.moveBoolean(reader, configurationData)
                | FileProperty.toggle_metrics.moveBoolean(reader, configurationData);
    }
}