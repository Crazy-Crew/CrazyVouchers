package com.badbones69.crazyvouchers.api.enums.config;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.properties.Property;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.enums.State;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.badbones69.crazyvouchers.config.types.locale.CommandKeys;
import com.badbones69.crazyvouchers.config.types.locale.MessageKeys;
import com.badbones69.crazyvouchers.config.types.locale.MiscKeys;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.config.ConfigManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Messages {

    must_be_console_sender(MiscKeys.must_be_console_sender),
    inventory_not_empty(MiscKeys.inventory_not_empty),
    feature_disabled(MiscKeys.feature_disabled),
    unknown_command(MiscKeys.unknown_command),
    correct_usage(MiscKeys.correct_usage),
    same_player(MiscKeys.same_player),
    lacking_flag(MiscKeys.lacking_flag),
    vouchers_list(MiscKeys.vouchers_list, true),

    error_migrating(CommandKeys.error_migrating),
    migration_not_available(CommandKeys.migration_not_available),
    migration_plugin_not_enabled(CommandKeys.migration_plugin_not_enabled),
    migration_no_vouchers_available(CommandKeys.migration_no_vouchers_available),
    migration_no_codes_available(CommandKeys.migration_no_codes_available),
    successfully_migrated(CommandKeys.successfully_migrated, true),
    successfully_migrated_users(CommandKeys.successfully_migrated_users, true),

    config_reload(MessageKeys.config_reload),
    not_online(MessageKeys.not_online),
    player_only(MessageKeys.player_only),
    not_a_number(MessageKeys.not_a_number),
    no_permission(MessageKeys.no_permission),
    survival_mode(MessageKeys.survival_mode),
    dupe_protection(MessageKeys.dupe_protection),
    notify_staff(MessageKeys.notify_staff),
    no_permission_to_use_voucher(MessageKeys.no_permission_to_use_voucher),
    no_permission_to_use_voucher_offhand(MessageKeys.no_permission_to_use_voucher_in_offhand),
    not_a_voucher(MessageKeys.not_a_voucher),
    code_unavailable(MessageKeys.code_unavailable),
    code_used(MessageKeys.code_used),
    sent_voucher(MessageKeys.sent_voucher),
    sent_everyone_voucher(MessageKeys.sent_everyone_voucher),
    hit_voucher_limit(MessageKeys.hit_voucher_limit),
    two_step_authentication(MessageKeys.two_step_authentication),
    has_blacklist_permission(MessageKeys.has_blacklist_permission),
    cooldown_active(MessageKeys.cooldown_active),
    not_in_whitelisted_world(MessageKeys.not_in_whitelist_world),
    unstack_item(MessageKeys.unstack_item),
    cannot_put_items_in_crafting_table(MessageKeys.cannot_put_items_in_crafting_table),
    migrated_old_vouchers(MessageKeys.migrated_old_vouchers),
    help(MessageKeys.help, true);

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final FusionPaper fusion = this.plugin.getFusion();

    private final StringUtils utils = this.fusion.getStringUtils();

    private Property<String> property;

    private Property<List<String>> properties;
    private boolean isList = false;

    Messages(@NotNull final Property<String> property) {
        this.property = property;
    }

    Messages(@NotNull final Property<List<String>> properties, final boolean isList) {
        this.properties = properties;
        this.isList = isList;
    }

    private final SettingsManager config = ConfigManager.getConfig();

    private final SettingsManager locale = ConfigManager.getMessages();

    public String getString() {
        return this.locale.getProperty(this.property);
    }

    public List<String> getList() {
        return this.locale.getProperty(this.properties);
    }

    public Component getMessage(@NotNull final Audience sender) {
        return getMessage(sender, new HashMap<>());
    }

    public Component getMessage(@NotNull final Audience sender, @NotNull final String placeholder, @NotNull final String replacement) {
        final Map<String, String> placeholders = new HashMap<>();

        placeholders.put(placeholder, replacement);

        return getMessage(sender, placeholders);
    }

    public Component getMessage(@NotNull final Audience sender, @NotNull final Map<String, String> placeholders) {
        placeholders.putIfAbsent("prefix", this.config.getProperty(ConfigKeys.command_prefix));

        return parse(sender, placeholders);
    }

    public void sendMessage(@NotNull final Audience sender, @NotNull final String placeholder, @NotNull final String replacement) {
        final State state = this.config.getProperty(ConfigKeys.message_state);

        switch (state) {
            case send_message -> sendRichMessage(sender, placeholder, replacement);
            case send_actionbar -> sendActionBar(sender, placeholder, replacement);
        }
    }

    public void sendMessage(@NotNull final Audience sender, @NotNull final Map<String, String> placeholders) {
        final State state = this.config.getProperty(ConfigKeys.message_state);

        switch (state) {
            case send_message -> sendRichMessage(sender, placeholders);
            case send_actionbar -> sendActionBar(sender, placeholders);
        }
    }

    public void sendMessage(@NotNull final Audience sender) {
        final State state = this.config.getProperty(ConfigKeys.message_state);

        switch (state) {
            case send_message -> sendRichMessage(sender);
            case send_actionbar -> sendActionBar(sender);
        }
    }

    public void sendActionBar(@NotNull final Audience sender, @NotNull final String placeholder, @NotNull final String replacement) {
        final Component component = getMessage(sender, placeholder, replacement);

        if (component.equals(Component.empty())) return;

        sender.sendActionBar(component);
    }

    public void sendActionBar(@NotNull final Audience sender, @NotNull final Map<String, String> placeholders) {
        final Component component = getMessage(sender, placeholders);

        if (component.equals(Component.empty())) return;

        sender.sendActionBar(component);
    }

    public void sendActionBar(@NotNull final Audience sender) {
        final Component component = getMessage(sender);

        if (component.equals(Component.empty())) return;

        sender.sendActionBar(component);
    }

    public void sendRichMessage(@NotNull final Audience sender, @NotNull final String placeholder, @NotNull final String replacement) {
        final Component component = getMessage(sender, placeholder, replacement);

        if (component.equals(Component.empty())) return;

        sender.sendMessage(component);
    }

    public void sendRichMessage(@NotNull final Audience sender, @NotNull final Map<String, String> placeholders) {
        final Component component = getMessage(sender, placeholders);

        if (component.equals(Component.empty())) return;

        sender.sendMessage(component);
    }

    public void sendRichMessage(@NotNull final Audience sender) {
        final Component component = getMessage(sender);

        if (component.equals(Component.empty())) return;

        sender.sendMessage(component);
    }

    public final boolean isList() {
        return this.isList;
    }

    public void migrate() {
        if (this.isList) {
            this.locale.setProperty(this.properties, this.utils.convertLegacy(this.locale.getProperty(this.properties), true));

            return;
        }

        this.locale.setProperty(this.property, this.utils.convertLegacy(this.locale.getProperty(this.property), true));
    }

    private @NotNull Component parse(@NotNull final Audience audience, @NotNull final Map<String, String> placeholders) {
        String message;

        if (this.isList) {
            message = this.utils.toString(getList());
        } else {
            message = getString();
        }

        return this.fusion.parse(audience, message, placeholders);
    }
}