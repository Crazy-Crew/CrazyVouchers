package com.badbones69.crazyvouchers.config.migrate;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.migration.PlainMigrationService;
import ch.jalu.configme.resource.PropertyReader;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.api.enums.config.Properties;

public class LocaleMigration extends PlainMigrationService {

    @Override
    protected boolean performMigrations(@NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
        return Properties.survival_mode.moveString(reader, configurationData)
                | Properties.no_permission.moveString(reader, configurationData)
                | Properties.no_permission_to_use_voucher_in_offhand.moveString(reader, configurationData)
                | Properties.no_permission_to_use_voucher.moveString(reader, configurationData)
                | Properties.cannot_put_items_in_crafting_table.moveString(reader, configurationData)
                | Properties.not_online.moveString(reader, configurationData)
                | Properties.two_step_authentication.moveString(reader, configurationData)
                | Properties.hit_voucher_limit.moveString(reader, configurationData)
                | Properties.not_a_number.moveString(reader, configurationData)
                | Properties.not_a_voucher.moveString(reader, configurationData)
                | Properties.not_in_whitelist_world.moveString(reader, configurationData)
                | Properties.unstack_item.moveString(reader, configurationData)
                | Properties.has_blacklist_permission.moveString(reader, configurationData)
                | Properties.code_used.moveString(reader, configurationData)
                | Properties.code_unavailable.moveString(reader, configurationData)
                | Properties.sent_voucher.moveString(reader, configurationData)
                | Properties.sent_everyone_voucher.moveString(reader, configurationData)
                | Properties.player_only.moveString(reader, configurationData)
                | Properties.config_reload.moveString(reader, configurationData)
                | Properties.help.moveList(reader, configurationData);
    }
}