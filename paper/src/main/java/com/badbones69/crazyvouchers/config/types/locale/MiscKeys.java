package com.badbones69.crazyvouchers.config.types.locale;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;
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

}