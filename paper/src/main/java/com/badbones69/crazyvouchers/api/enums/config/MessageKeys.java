package com.badbones69.crazyvouchers.api.enums.config;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.properties.Property;
import com.badbones69.crazyvouchers.config.types.locale.RootKeys;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import com.ryderbelserion.fusion.core.util.StringUtils;
import com.ryderbelserion.fusion.paper.enums.Support;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.config.ConfigManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MessageKeys {

    must_be_console_sender(RootKeys.must_be_console_sender),
    inventory_not_empty(RootKeys.inventory_not_empty),
    feature_disabled(RootKeys.feature_disabled),
    unknown_command(RootKeys.unknown_command),
    correct_usage(RootKeys.correct_usage),
    same_player(RootKeys.same_player),

    config_reload(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.config_reload),
    not_online(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.not_online),
    player_only(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.player_only),
    not_a_number(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.not_a_number),
    no_permission(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.no_permission),
    survival_mode(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.survival_mode),
    dupe_protection(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.dupe_protection),
    notify_staff(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.notify_staff),
    no_permission_to_use_voucher(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.no_permission_to_use_voucher),
    no_permission_to_use_voucher_offhand(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.no_permission_to_use_voucher_in_offhand),
    not_a_voucher(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.not_a_voucher),
    code_unavailable(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.code_unavailable),
    code_used(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.code_used),
    sent_voucher(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.sent_voucher),
    sent_everyone_voucher(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.sent_everyone_voucher),
    hit_voucher_limit(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.hit_voucher_limit),
    two_step_authentication(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.two_step_authentication),
    has_blacklist_permission(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.has_blacklist_permission),
    cooldown_active(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.cooldown_active),
    not_in_whitelisted_world(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.not_in_whitelist_world),
    unstack_item(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.unstack_item),
    cannot_put_items_in_crafting_table(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.cannot_put_items_in_crafting_table),
    migrated_old_vouchers(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.migrated_old_vouchers),
    help(com.badbones69.crazyvouchers.config.types.locale.MessageKeys.help, true);

    private Property<String> property;

    private Property<List<String>> properties;
    private boolean isList = false;

    MessageKeys(@NotNull final Property<String> property) {
        this.property = property;
    }

    MessageKeys(@NotNull final Property<List<String>> properties, final boolean isList) {
        this.properties = properties;
        this.isList = isList;
    }

    private final SettingsManager messages = ConfigManager.getMessages();

    private boolean isList() {
        return this.isList;
    }

    public String getString() {
        return this.messages.getProperty(this.property);
    }

    public List<String> getList() {
        return this.messages.getProperty(this.properties);
    }

    public String getMessage(@NotNull final CommandSender sender) {
        return getMessage(sender, new HashMap<>());
    }

    public String getMessage(@NotNull final CommandSender sender, @NotNull final String placeholder, @NotNull final String replacement) {
        Map<String, String> placeholders = new HashMap<>() {{
            put(placeholder, replacement);
        }};

        return getMessage(sender, placeholders);
    }

    public String getMessage(@NotNull final CommandSender sender, @NotNull final Map<String, String> placeholders) {
        return parse(sender, placeholders).replaceAll("\\{prefix}", MsgUtils.getPrefix());
    }

    public void sendMessage(final CommandSender sender, final String placeholder, final String replacement) {
        sender.sendMessage(getMessage(sender, placeholder, replacement));
    }

    public void sendMessage(final CommandSender sender, final Map<String, String> placeholders) {
        sender.sendMessage(getMessage(sender, placeholders));
    }

    public void sendMessage(final CommandSender sender) {
        sender.sendMessage(getMessage(sender));
    }

    private @NotNull String parse(@NotNull final CommandSender sender, @NotNull final Map<String, String> placeholders) {
        String message;

        if (isList()) {
            message = StringUtils.toString(getList());
        } else {
            message = getString();
        }

        if (sender instanceof Player player) {
            if (Support.placeholder_api.isEnabled()) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }
        }

        if (!placeholders.isEmpty()) {
            for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
                message = message.replace(placeholder.getKey(), placeholder.getValue()).replace(placeholder.getKey().toLowerCase(), placeholder.getValue());
            }
        }

        return MsgUtils.color(message);
    }
}