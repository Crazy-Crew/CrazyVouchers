package com.badbones69.crazyvouchers.config.types.locale;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;
import java.util.List;
import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class MiscKeys implements SettingsHolder {

    protected MiscKeys() {}

    @Comment("A list of available placeholders: {command}")
    public static final Property<String> unknown_command = newProperty("misc.unknown-command", "{prefix}<red>{command} is not a known command.");

    @Comment("A list of available placeholders: {usage}")
    public static final Property<String> correct_usage = newProperty("misc.correct-usage", "{prefix}<red>The correct usage for this command is <yellow>{usage}");

    public static final Property<String> feature_disabled = newProperty("misc.feature-disabled", "{prefix}<red>This feature is disabled.");

    public static final Property<String> must_be_console_sender = newProperty("misc.must-be-console-sender", "{prefix}<red>You must be using console to use this command.");

    public static final Property<String> same_player = newProperty("misc.target-same-player", "{prefix}<red>You cannot use this command on yourself.");

    public static final Property<String> inventory_not_empty = newProperty("misc.inventory-not-empty", "{prefix}<red>Inventory is not empty, Please clear up some room.");

    @Comment("A list of available placeholders: {flag}, {usage}")
    public static final Property<String> lacking_flag = newProperty("misc.lacking-flag", "{prefix}<red>{flag} is not present in the command, expected format: {usage}");

    @Comment({
            "The format for the /crazyvouchers list command",
            "",
            "A list of available placeholders",
            " ⤷ {total_vouchers} » Shows the total vouchers count",
            " ⤷ {total_codes} » Shows the total codes count",
            " ⤷ {active_vouchers} » Shows the active vouchers count",
            " ⤷ {active_codes} » Shows the active codes count",
            " ⤷ {broken_vouchers} » Shows the broken vouchers count",
            " ⤷ {broken_codes} » Shows the broken codes count",
            " ⤷ {vouchers} » Shows all vouchers broken and not broken",
            " ⤷ {codes} » Shows all codes broken and not broken",
            ""
    })
    public static final Property<List<String>> vouchers_list = newListProperty("misc.vouchers-list", List.of(
            "<bold><gold>━━━━━━━━━━━━━━━━━━━ Plugin Stats ━━━━━━━━━━━━━━━━━━━</gold></bold>",
            "<dark_gray>»</dark_gray> <gold>Code Statistics <dark_gray>[</dark_gray><green>{active_codes}</green><dark_gray>/</dark_gray><gold>{total_codes}</gold><dark_gray>]</dark_gray>",
            "<dark_gray>-</dark_gray> {codes}",
            "<dark_gray>»</dark_gray> <gold>Voucher Statistics <dark_gray>[</dark_gray><green>{active_vouchers}</green><dark_gray>/</dark_gray><gold>{total_vouchers}</gold><dark_gray>]</dark_gray>",
            "<dark_gray>-</dark_gray> {vouchers}",
            "<bold><gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold></bold>"
    ));
}