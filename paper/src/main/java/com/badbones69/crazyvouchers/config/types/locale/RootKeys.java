package com.badbones69.crazyvouchers.config.types.locale;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class RootKeys implements SettingsHolder {

    protected RootKeys() {}

    @Comment("A list of available placeholders: {command}")
    public static final Property<String> unknown_command = newProperty("misc.unknown-command", "{prefix}&c{command} is not a known command.");

    @Comment("A list of available placeholders: {usage}")
    public static final Property<String> correct_usage = newProperty("misc.correct-usage", "{prefix}&cThe correct usage for this command is &e{usage}");

    public static final Property<String> feature_disabled = newProperty("misc.feature-disabled", "{prefix}&cThis feature is disabled.");

    public static final Property<String> must_be_console_sender = newProperty("misc.must-be-console-sender", "{prefix}&cYou must be using console to use this command.");

    public static final Property<String> same_player = newProperty("misc.target-same-player", "{prefix}&cYou cannot use this command on yourself.");

    public static final Property<String> inventory_not_empty = newProperty("misc.inventory-not-empty", "{prefix}&cInventory is not empty, Please clear up some room.");

}