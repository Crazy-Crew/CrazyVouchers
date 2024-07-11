package com.badbones69.crazyvouchers.api.enums;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.properties.Property;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import com.ryderbelserion.vital.core.util.StringUtil;
import com.ryderbelserion.vital.paper.enums.Support;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.badbones69.crazyvouchers.config.types.MessageKeys;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Messages {

    config_reload(MessageKeys.config_reload),
    not_online(MessageKeys.not_online),
    player_only(MessageKeys.player_only),
    not_a_number(MessageKeys.not_a_number),
    no_permission(MessageKeys.no_permission),
    survival_mode(MessageKeys.survival_mode),
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
    not_in_whitelisted_world(MessageKeys.not_in_whitelist_world),
    unstack_item(MessageKeys.unstack_item),
    cannot_put_items_in_crafting_table(MessageKeys.cannot_put_items_in_crafting_table),
    help(MessageKeys.help, true);

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

    public String getMessage() {
        return getMessage(null, new HashMap<>());
    }

    public String getMessage(@Nullable final CommandSender sender) {
        if (sender instanceof Player player) {
            return getMessage(player, new HashMap<>());
        }

        return getMessage(null, new HashMap<>());
    }

    public String getMessage(@NotNull final Map<String, String> placeholders) {
        return getMessage(null, placeholders);
    }

    public String getMessage(@NotNull final String placeholder, @NotNull final String replacement) {
        return getMessage(null, placeholder, replacement);
    }

    public String getMessage(@Nullable final CommandSender sender, @NotNull final String placeholder, @NotNull final String replacement) {
        Map<String, String> placeholders = new HashMap<>() {{
            put(placeholder, replacement);
        }};

        if (sender instanceof Player player) {
            return getMessage(player, placeholders);
        }

        return getMessage(null, placeholders);
    }

    public String getMessage(@Nullable final CommandSender sender, @NotNull final Map<String, String> placeholders) {
        if (sender instanceof Player player) {
            return getMessage(player, placeholders);
        }

        return getMessage(null, placeholders);
    }

    public String getMessage(@Nullable final Player player, @NotNull final Map<String, String> placeholders) {
        String prefix = this.config.getProperty(ConfigKeys.command_prefix);

        if (player != null) {
            return parse(player, placeholders).replaceAll("\\{prefix}", prefix);
        }

        return parse(null, placeholders).replaceAll("\\{prefix}", prefix);
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

    private @NotNull String parse(@Nullable final Player player, @NotNull final Map<String, String> placeholders) {
        String message;

        if (isList()) {
            message = StringUtils.chomp(StringUtil.convertList(getList()));
        } else {
            message = getString();
        }

        if (player != null) {
            if (Support.placeholder_api.isEnabled()) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }
        }

        if (!placeholders.isEmpty()) {
            for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
                message = message.replace(placeholder.getKey(), placeholder.getValue()).replace(placeholder.getKey().toLowerCase(), placeholder.getValue());
            }
        }

        return message;
    }
}