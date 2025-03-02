package com.badbones69.crazyvouchers.config.migrate;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.migration.PlainMigrationService;
import ch.jalu.configme.resource.PropertyReader;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.api.enums.config.PropertyKeys;

public class ConfigMigration extends PlainMigrationService {

    @Override
    protected boolean performMigrations(@NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
        return PropertyKeys.command_prefix.moveString(reader, configurationData)
                | PropertyKeys.must_be_in_survival.moveBoolean(reader, configurationData)
                | PropertyKeys.prevent_using_vouchers_in_recipes_toggle.moveBoolean(reader, configurationData)
                | PropertyKeys.prevent_using_vouchers_in_recipes_alert.moveBoolean(reader, configurationData);
    }
}