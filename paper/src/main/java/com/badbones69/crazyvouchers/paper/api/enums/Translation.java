package com.badbones69.crazyvouchers.paper.api.enums;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.properties.Property;
import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import com.ryderbelserion.cluster.bukkit.utils.LegacyLogger;
import com.ryderbelserion.cluster.bukkit.utils.LegacyUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyenvoys.common.config.ConfigManager;
import us.crazycrew.crazyenvoys.common.config.types.Config;
import us.crazycrew.crazyenvoys.common.config.types.Messages;
import us.crazycrew.crazyenvoys.common.utils.MiscUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Translation {

    config_reload(Messages.config_reload),
    not_online(Messages.not_online),
    player_only(Messages.player_only),
    not_a_number(Messages.not_a_number),
    no_permission(Messages.no_permission),
    survival_mode(Messages.survival_mode),
    no_permission_to_use_voucher(Messages.no_permission_to_use_voucher),
    no_permission_to_use_voucher_offhand(Messages.no_permission_to_use_voucher_in_offhand),
    not_a_voucher(Messages.not_a_voucher),
    code_unavailable(Messages.code_unavailable),
    code_used(Messages.code_used),
    sent_voucher(Messages.sent_voucher),
    sent_everyone_voucher(Messages.sent_everyone_voucher),
    hit_voucher_limit(Messages.hit_voucher_limit),
    two_step_authentication(Messages.two_step_authentication),
    has_blacklist_permission(Messages.has_blacklist_permission),
    not_in_whitelisted_world(Messages.not_in_whitelist_world),
    unstack_item(Messages.unstack_item),
    cannot_put_items_in_crafting_table(Messages.cannot_put_items_in_crafting_table),
    help(Messages.help, true);

    private Property<String> property;

    private Property<List<String>> listProperty;

    private boolean isList = false;

    private String message;

    /**
     * Used for strings
     *
     * @param property the property
     */
    Translation(Property<String> property) {
        this.property = property;
    }

    /**
     * Used for string lists
     *
     * @param listProperty the list property
     * @param isList Defines if it's a list or not.
     */
    Translation(Property<List<String>> listProperty, boolean isList) {
        this.listProperty = listProperty;

        this.isList = isList;
    }

    private final CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);
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

    public Translation getMessage() {
        return getMessage(new HashMap<>());
    }

    public boolean isBlank() {
        return getMessage().toString().isBlank();
    }

    public boolean isListBlank() {
        return getMessage().toListString().isEmpty();
    }

    public Translation getMessage(String placeholder, String replacement) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, replacement);

        return getMessage(placeholders);
    }

    public Translation getMessage(Map<String, String> placeholders) {
        // Get the string first.
        String message;

        if (isList()) {
            message = MiscUtils.convertList(getPropertyList(this.listProperty));
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
        return LegacyUtils.color(this.message.replaceAll("\\{prefix}", this.configManager.getConfig().getProperty(Config.command_prefix)));
    }

    public List<String> toListString() {
        ArrayList<String> components = new ArrayList<>();

        getPropertyList(this.listProperty).forEach(line -> components.add(LegacyUtils.color(line)));

        return components;
    }
}