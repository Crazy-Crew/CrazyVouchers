package us.crazycrew.crazyvouchers.common.config.migrate;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.migration.PlainMigrationService;
import ch.jalu.configme.resource.PropertyReader;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyvouchers.common.api.enums.FileProperty;

public class LocaleMigration extends PlainMigrationService {

    @Override
    protected boolean performMigrations(@NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
        return FileProperty.survival_mode.moveString(reader, configurationData)
                | FileProperty.no_permission.moveString(reader, configurationData)
                | FileProperty.no_permission_to_use_voucher_in_offhand.moveString(reader, configurationData)
                | FileProperty.no_permission_to_use_voucher.moveString(reader, configurationData)
                | FileProperty.cannot_put_items_in_crafting_table.moveString(reader, configurationData)
                | FileProperty.not_online.moveString(reader, configurationData)
                | FileProperty.two_step_authentication.moveString(reader, configurationData)
                | FileProperty.hit_voucher_limit.moveString(reader, configurationData)
                | FileProperty.not_a_number.moveString(reader, configurationData)
                | FileProperty.not_a_voucher.moveString(reader, configurationData)
                | FileProperty.not_in_whitelist_world.moveString(reader, configurationData)
                | FileProperty.unstack_item.moveString(reader, configurationData)
                | FileProperty.has_blacklist_permission.moveString(reader, configurationData)
                | FileProperty.code_used.moveString(reader, configurationData)
                | FileProperty.code_unavailable.moveString(reader, configurationData)
                | FileProperty.sent_voucher.moveString(reader, configurationData)
                | FileProperty.sent_everyone_voucher.moveString(reader, configurationData)
                | FileProperty.player_only.moveString(reader, configurationData)
                | FileProperty.config_reload.moveString(reader, configurationData)
                | FileProperty.help.moveList(reader, configurationData);
    }
}