package com.badbones69.crazyvouchers.api.enums;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.properties.Property;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import com.ryderbelserion.vital.core.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyvouchers.common.config.ConfigManager;
import us.crazycrew.crazyvouchers.common.config.types.ConfigKeys;
import us.crazycrew.crazyvouchers.common.config.types.MessageKeys;
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

    private Property<List<String>> listProperty;

    private boolean isList = false;

    private String message;

    /**
     * Used for strings
     *
     * @param property the property
     */
    Messages(Property<String> property) {
        this.property = property;
    }

    /**
     * Used for string lists
     *
     * @param listProperty the list property
     * @param isList Defines if it's a list or not.
     */
    Messages(Property<List<String>> listProperty, boolean isList) {
        this.listProperty = listProperty;

        this.isList = isList;
    }

    private final CrazyVouchers plugin = CrazyVouchers.get();
    private final ConfigManager configManager = this.plugin.getCrazyHandler().getConfigManager();
    private final SettingsManager messages = this.configManager.getMessages();

    private boolean isList() {
        return this.isList;
    }

    private @NotNull List<String> getPropertyList(Property<List<String>> properties) {
        return this.messages.getProperty(properties);
    }

    private @NotNull String getProperty(Property<String> property) {
        return this.messages.getProperty(property);
    }

    public String getString() {
        return getMessage().toString();
    }

    public Messages getMessage() {
        return getMessage(new HashMap<>());
    }

    public boolean isBlank() {
        return getMessage().toString().isBlank();
    }

    public boolean isListBlank() {
        return getMessage().toListString().isEmpty();
    }

    public Messages getMessage(String placeholder, String replacement) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, replacement);

        return getMessage(placeholders);
    }

    public Messages getMessage(Map<String, String> placeholders) {
        // Get the string first.
        String message;

        if (isList()) {
            message = StringUtil.chomp(StringUtil.convertList(getPropertyList(this.listProperty)));
        } else {
            message = getProperty(this.property);
        }

        if (!placeholders.isEmpty()) {
            for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
                message = message.replace(placeholder.getKey(), placeholder.getValue()).replace(placeholder.getKey().toLowerCase(), placeholder.getValue());
            }
        }

        this.message = message;

        return this;
    }

    public void sendMessage(Player player) {
        sendMessage(player, new HashMap<>());
    }

    public void sendMessage(Player player, Map<String, String> placeholder) {
        player.sendMessage(getMessage(placeholder).asString());
    }

    public void sendMessage(CommandSender sender) {
        sendMessage(sender, new HashMap<>());
    }

    public void sendMessage(CommandSender sender, Map<String, String> placeholder) {
        sender.sendMessage(getMessage(placeholder).asString());
    }

    public String asString() {
        return MsgUtils.color(this.message.replaceAll("\\{prefix}", this.configManager.getConfig().getProperty(ConfigKeys.command_prefix)));
    }

    public List<String> toListString() {
        ArrayList<String> components = new ArrayList<>();

        getPropertyList(this.listProperty).forEach(line -> components.add(MsgUtils.color(line)));

        return components;
    }
}