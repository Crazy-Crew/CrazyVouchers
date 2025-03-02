package com.badbones69.crazyvouchers.api.enums.config;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.resource.PropertyReader;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.badbones69.crazyvouchers.config.types.locale.MessageKeys;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public enum PropertyKeys {

    command_prefix(ConfigKeys.command_prefix, newProperty("Settings.Prefix", ConfigKeys.command_prefix.getDefaultValue())),
    must_be_in_survival(ConfigKeys.must_be_in_survival, newProperty("Settings.Must-Be-In-Survival", ConfigKeys.must_be_in_survival.getDefaultValue()), false),
    prevent_using_vouchers_in_recipes_toggle(ConfigKeys.prevent_using_vouchers_in_recipes_toggle, newProperty("Settings.Prevent-Using-Vouchers-In-Recipes.Toggle", ConfigKeys.prevent_using_vouchers_in_recipes_toggle.getDefaultValue()), false),
    prevent_using_vouchers_in_recipes_alert(ConfigKeys.prevent_using_vouchers_in_recipes_alert, newProperty("Settings.Prevent-Using-Vouchers-In-Recipes.Alert", ConfigKeys.prevent_using_vouchers_in_recipes_alert.getDefaultValue()), false),

    survival_mode(MessageKeys.survival_mode, newProperty("Messages.Survival-Mode", MessageKeys.survival_mode.getDefaultValue())),
    no_permission(MessageKeys.no_permission, newProperty("Messages.No-Permission", MessageKeys.no_permission.getDefaultValue())),
    no_permission_to_use_voucher(MessageKeys.no_permission_to_use_voucher, newProperty("Messages.No-Permission-To-Voucher", MessageKeys.no_permission_to_use_voucher.getDefaultValue())),
    no_permission_to_use_voucher_in_offhand(MessageKeys.no_permission_to_use_voucher_in_offhand, newProperty("Messages.No-Permission-To-Use-Voucher-In-OffHand", MessageKeys.no_permission_to_use_voucher_in_offhand.getDefaultValue())),
    cannot_put_items_in_crafting_table(MessageKeys.cannot_put_items_in_crafting_table, newProperty("Messages.Cannot-Put-Items-In-Crafting-Table", MessageKeys.cannot_put_items_in_crafting_table.getDefaultValue())),
    not_online(MessageKeys.not_online, newProperty("Messages.Not-Online", MessageKeys.not_online.getDefaultValue())),
    two_step_authentication(MessageKeys.two_step_authentication, newProperty("Messages.Two-Step-Authentication", MessageKeys.two_step_authentication.getDefaultValue())),
    hit_voucher_limit(MessageKeys.hit_voucher_limit, newProperty("Messages.Hit-Limit", MessageKeys.hit_voucher_limit.getDefaultValue())),
    not_a_number(MessageKeys.not_a_number, newProperty("Messages.Not-A-Number", MessageKeys.not_a_number.getDefaultValue())),
    not_a_voucher(MessageKeys.not_a_voucher, newProperty("Messages.Not-A-Voucher", MessageKeys.not_a_voucher.getDefaultValue())),
    not_in_whitelist_world(MessageKeys.not_in_whitelist_world, newProperty("Messages.Not-In-Whitelisted-World", MessageKeys.not_in_whitelist_world.getDefaultValue())),
    unstack_item(MessageKeys.unstack_item, newProperty("Messages.Unstack-Item", MessageKeys.unstack_item.getDefaultValue())),
    has_blacklist_permission(MessageKeys.has_blacklist_permission, newProperty("Messages.Has-Blacklist-Permission", MessageKeys.has_blacklist_permission.getDefaultValue())),
    code_used(MessageKeys.code_used, newProperty("Messages.Code-Used", MessageKeys.code_used.getDefaultValue())),
    code_unavailable(MessageKeys.code_unavailable, newProperty("Messages.Code-UnAvailable", MessageKeys.code_unavailable.getDefaultValue())),
    sent_voucher(MessageKeys.sent_voucher, newProperty("Messages.Given-A-Voucher", MessageKeys.sent_voucher.getDefaultValue())),
    sent_everyone_voucher(MessageKeys.sent_everyone_voucher, newProperty("Messages.Given-All-Players-Voucher", MessageKeys.sent_everyone_voucher.getDefaultValue())),
    player_only(MessageKeys.player_only, newProperty("Messages.Players-Only", MessageKeys.player_only.getDefaultValue())),
    config_reload(MessageKeys.config_reload, newProperty("Messages.Config-Reload", MessageKeys.config_reload.getDefaultValue())),
    help(MessageKeys.help, newListProperty("Messages.Help", MessageKeys.help.getDefaultValue()), Collections.emptyList());

    private Property<String> newString;
    private Property<String> oldString;

    /**
     * A constructor moving the new and old string property for migration
     *
     * @param newString the new property
     * @param oldString the old property
     */
    PropertyKeys(Property<String> newString, Property<String> oldString) {
        this.newString = newString;
        this.oldString = oldString;
    }

    /**
     * Moves the old value to the new value
     *
     * @param reader the config reader
     * @param configuration the configuration data
     * @return true or false
     */
    public boolean moveString(PropertyReader reader, ConfigurationData configuration) {
        String key = reader.getString(this.oldString.getPath());

        if (key == null) return false;

        configuration.setValue(this.newString, replace(this.oldString.determineValue(reader).getValue()));

        return true;
    }

    private Property<Boolean> newBoolean;
    private Property<Boolean> oldBoolean;

    /**
     * A constructor consisting of the new and old boolean property for migration
     *
     * @param newBoolean the new property
     * @param oldBoolean the old property
     * @param dummy only to differentiate from previous constructors
     */
    PropertyKeys(Property<Boolean> newBoolean, Property<Boolean> oldBoolean, boolean dummy) {
        this.newBoolean = newBoolean;
        this.oldBoolean = oldBoolean;
    }

    /**
     * Moves the old value to the new value
     *
     * @param reader the config reader
     * @param configuration the configuration data
     * @return true or false
     */
    public boolean moveBoolean(PropertyReader reader, ConfigurationData configuration) {
        Boolean key = reader.getBoolean(this.oldBoolean.getPath());

        if (key == null) return false;

        configuration.setValue(this.newBoolean, this.oldBoolean.determineValue(reader).getValue());

        return true;
    }

    private Property<Integer> newInteger;
    private Property<Integer> oldInteger;

    /**
     * A constructor consisting of the new and old int property for migration
     *
     * @param newInteger the new property
     * @param oldInteger the old property
     * @param dummy only to differentiate from previous constructors
     */
    PropertyKeys(Property<Integer> newInteger, Property<Integer> oldInteger, int dummy) {
        this.newInteger = newInteger;
        this.oldInteger = oldInteger;
    }

    /**
     * Moves the old value to the new value
     *
     * @param reader the config reader
     * @param configuration the configuration data
     * @return true or false
     */
    public boolean moveInteger(PropertyReader reader, ConfigurationData configuration) {
        Integer key = reader.getInt(this.oldInteger.getPath());

        if (key == null) return false;

        configuration.setValue(this.newInteger, this.oldInteger.determineValue(reader).getValue());

        return true;
    }

    private Property<List<String>> newList;
    private Property<List<String>> oldList;

    /**
     * A constructor consisting of the new and old list property for migration
     *
     * @param newList the new property
     * @param oldList the old property
     * @param dummy only to differentiate from previous constructors
     */
    PropertyKeys(Property<List<String>> newList, Property<List<String>> oldList, List<String> dummy) {
        this.newList = newList;
        this.oldList = oldList;
    }

    /**
     * Moves the old value to the new value
     *
     * @param reader the config reader
     * @param configuration the configuration data
     * @return true or false
     */
    public boolean moveList(PropertyReader reader, ConfigurationData configuration) {
        List<?> key = reader.getList(this.oldList.getPath());

        if (key == null) return false;

        List<String> list = new ArrayList<>();

        this.oldList.determineValue(reader).getValue().forEach(line -> list.add(replace(line)));

        configuration.setValue(this.newList, list);

        return true;
    }

    /**
     * Replaces old placeholders in the option when migrating.
     *
     * @param message the message to check
     * @return the finalized message to set
     */
    private String replace(String message) {
        return message.replaceAll("%Arg%", "{arg}")
                .replaceAll("%arg%", "{arg}")
                .replaceAll("%Player%", "{player}")
                .replaceAll("%player%", "{player}")
                .replaceAll("%Prefix%", "")
                .replaceAll("%prefix%", "")
                .replaceAll("%Random%", "{random}")
                .replaceAll("%random%", "{random}")
                .replaceAll("%World%", "{world}")
                .replaceAll("%world%", "{world}")
                .replaceAll("%voucher%", "{voucher}")
                .replaceAll("%Voucher%", "{voucher}")
                .replaceAll("%X%", "{x}")
                .replaceAll("%x%", "{x}")
                .replaceAll("%Y%", "{y}")
                .replaceAll("%y%", "{y}")
                .replaceAll("%Z%", "{z}")
                .replaceAll("%z%", "{z}")
                .replaceAll("%Permission%", "{permission}")
                .replaceAll("%permission%", "{permission}");
    }
}