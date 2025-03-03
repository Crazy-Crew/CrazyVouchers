package com.badbones69.crazyvouchers.config.types.locale;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;
import java.util.List;
import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class CommandKeys implements SettingsHolder {

    protected CommandKeys() {}

    @Comment("A list of available placeholders: {file}, {type}, {reason}")
    public static final Property<String> error_migrating = newProperty("command.migrate.error", "{prefix}<red>We could not migrate <green>{file} <red>using <green>{type} <red>migration for <green>{reason}.");

    @Comment("A list of available placeholders: {prefix}")
    public static final Property<String> migration_not_available = newProperty("command.migrate.not-available", "{prefix}<green>This migration type is not available.");

    @Comment("A list of available placeholders: {name}")
    public static final Property<String> migration_plugin_not_enabled = newProperty("command.migrate.plugin-not-available", "{prefix}<green>The plugin <red>{name} <green>is not enabled. Cannot use as migration!");

    public static final Property<String> migration_no_vouchers_available = newProperty("command.migrate.no-vouchers-available", "{prefix}<green>There is no vouchers available for migration!");

    public static final Property<String> migration_no_codes_available = newProperty("command.migrate.no-codes-available", "{prefix}<green>There is no codes available for migration!");

    public static final Property<List<String>> successfully_migrated_users = newListProperty("command.migrate.success-users", List.of(
            "<bold><gold>━━━━━━━━━━━━━━━━━━━ Migration Stats ━━━━━━━━━━━━━━━━━━━</gold></bold>",
            "<dark_gray>»</dark_gray> <green>Successful Conversions: ",
            " ⤷ {succeeded_amount}</green>",
            "<dark_gray>»</dark_gray> <red>Failed Conversions: ",
            " ⤷ {failed_amount}</red>",
            "",
            "<red>Conversion Time: <yellow>{time}",
            "<red>Conversion Type: <yellow>{type}",
            "",
            "<bold><gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold></bold>"
    ));

    @Comment({
            "A list of available placeholders: {type}, {files}",
            "",
            "{files} will output multiple vouchers if migrating from another plugin"
    })
    public static final Property<List<String>> successfully_migrated = newListProperty("command.migrate.success", List.of(
            "<bold><gold>━━━━━━━━━━━━━━━━━━━ Migration Stats ━━━━━━━━━━━━━━━━━━━</gold></bold>",
            "<dark_gray>»</dark_gray> <green>Successful Conversions: ",
            " ⤷ {succeeded_amount}</green>",
            "<dark_gray>»</dark_gray> <red>Failed Conversions: ",
            " ⤷ {failed_amount}</red>",
            "",
            "<red>Conversion Time: <yellow>{time}",
            "<red>Conversion Type: <yellow>{type}",
            "",
            "<red>Converted Files:",
            "{files}",
            "",
            "<bold><gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold></bold>"
    ));
}
