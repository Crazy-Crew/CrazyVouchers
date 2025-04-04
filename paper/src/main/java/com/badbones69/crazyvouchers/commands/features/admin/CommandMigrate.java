package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.types.deprecation.LegacyColorMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.types.VoucherFileMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.types.VoucherNbtMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.types.VoucherSwitchMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.types.deprecation.NewItemMigrator;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Flag;
import dev.triumphteam.cmd.core.annotations.Permission;
import dev.triumphteam.cmd.core.annotations.Syntax;
import dev.triumphteam.cmd.core.argument.keyed.Flags;
import dev.triumphteam.cmd.core.enums.Mode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Optional;

public class CommandMigrate extends BaseCommand {

    @Command(value = "migrate")
    @Permission(value = "crazyvouchers.migrate", def = Mode.OP)
    @Syntax("/crazyvouchers migrate -mt [type]")
    @Flag(flag = "mt", longFlag = "migration_type", argument = String.class, suggestion = "migrators")
    public void migrate(final CommandSender sender, final Flags flags) {
        final boolean hasFlag = flags.hasFlag("mt");

        if (!hasFlag) {
            Messages.lacking_flag.sendMessage(sender, new HashMap<>() {{
                put("{flag}", "-mt");
                put("{usage}", "/crazyvouchers migrate -mt <migration_type>");
            }});

            return;
        }

        final Optional<String> key = flags.getFlagValue("mt");

        if (key.isEmpty()) {
            Messages.migration_not_available.sendMessage(sender);

            return;
        }

        final MigrationType type = MigrationType.fromName(key.get());

        if (type == null) {
            Messages.migration_not_available.sendMessage(sender);

            return;
        }

        switch (type) {
            case VOUCHERS_SWITCH -> new VoucherSwitchMigrator(sender).run();

            case VOUCHERS_NBT_API -> {
                if (!(sender instanceof Player)) {
                    Messages.player_only.sendMessage(sender);

                    return;
                }

                new VoucherNbtMigrator(sender).run();
            }

            case NEW_ITEM_FORMAT -> new NewItemMigrator(sender).run();

            case VOUCHERS_COLOR -> new LegacyColorMigrator(sender).run();

            case VOUCHERS_RENAME -> new VoucherFileMigrator(sender, type).run();
        }
    }
}