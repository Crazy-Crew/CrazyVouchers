package com.badbones69.crazyvouchers.config.migrate;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.migration.PlainMigrationService;
import ch.jalu.configme.resource.PropertyReader;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.api.enums.config.PropertyKeys;

public class LocaleMigration extends PlainMigrationService {

    @Override
    protected boolean performMigrations(@NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
        return PropertyKeys.survival_mode.moveString(reader, configurationData)
                | PropertyKeys.no_permission.moveString(reader, configurationData)
                | PropertyKeys.no_permission_to_use_voucher_in_offhand.moveString(reader, configurationData)
                | PropertyKeys.no_permission_to_use_voucher.moveString(reader, configurationData)
                | PropertyKeys.cannot_put_items_in_crafting_table.moveString(reader, configurationData)
                | PropertyKeys.not_online.moveString(reader, configurationData)
                | PropertyKeys.two_step_authentication.moveString(reader, configurationData)
                | PropertyKeys.hit_voucher_limit.moveString(reader, configurationData)
                | PropertyKeys.not_a_number.moveString(reader, configurationData)
                | PropertyKeys.not_a_voucher.moveString(reader, configurationData)
                | PropertyKeys.not_in_whitelist_world.moveString(reader, configurationData)
                | PropertyKeys.unstack_item.moveString(reader, configurationData)
                | PropertyKeys.has_blacklist_permission.moveString(reader, configurationData)
                | PropertyKeys.code_used.moveString(reader, configurationData)
                | PropertyKeys.code_unavailable.moveString(reader, configurationData)
                | PropertyKeys.sent_voucher.moveString(reader, configurationData)
                | PropertyKeys.sent_everyone_voucher.moveString(reader, configurationData)
                | PropertyKeys.player_only.moveString(reader, configurationData)
                | PropertyKeys.config_reload.moveString(reader, configurationData)
                | PropertyKeys.help.moveList(reader, configurationData);
    }
}