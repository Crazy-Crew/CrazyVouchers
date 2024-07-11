package com.badbones69.crazyvouchers.config.types;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;
import java.util.List;
import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class MessageKeys implements SettingsHolder {

    protected MessageKeys() {}

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] header = {
                "Support: https://discord.gg/badbones-s-live-chat-182615261403283459",
                "Github: https://github.com/Crazy-Crew",
                "",
                "Issues: https://github.com/Crazy-Crew/CrazyVouchers/issues",
                "Features: https://github.com/Crazy-Crew/CrazyVouchers/issues",
                "",
                "Tips:",
                " 1. Make sure to use the {prefix} to add the prefix in front of messages.",
                " 2. If you wish to use more than one line for a message just go from a line to a list.",
                "Examples:",
                "  Line:",
                "    No-Permission: '{prefix}&cYou do not have permission to use that command.'",
                "  List:",
                "    No-Permission:",
                "      - '{prefix}&cYou do not have permission'",
                "      - '&cto use that command. Please try another.'"
        };

        String[] deprecation = {
                "",
                "Warning: This section is subject to change so it is considered deprecated.",
                "This is your warning before the change happens.",
                ""
        };
        
        conf.setComment("player", header);
    }

    public static final Property<String> survival_mode = newProperty("player.survival-only", "{prefix}&cYou must be in survival mode to use vouchers.");

    public static final Property<String> no_permission = newProperty("player.no-permission", "{prefix}&cYou do not have permission to use that command!");

    public static final Property<String> no_permission_to_use_voucher = newProperty("player.voucher.no-permission", "{prefix}&cYou do not have permission to use that voucher.");

    public static final Property<String> no_permission_to_use_voucher_in_offhand = newProperty("player.voucher.no-permission-offhand" ,"{prefix}&cYou do not have permission to use vouchers in your off hand.");

    public static final Property<String> cannot_put_items_in_crafting_table = newProperty("player.voucher.cant-put-in-crafting-table", "{prefix}&cYou cannot put vouchers in the crafting table.");

    public static final Property<String> not_online = newProperty("player.target-not-online", "{prefix}&cThat player is not online.");

    public static final Property<String> hit_voucher_limit = newProperty("player.hit-limit", "{prefix}&cYou have hit your limit for using this voucher.");

    public static final Property<String> two_step_authentication = newProperty("player.two-step-authentication", "{prefix}&7Right click again to confirm that you want to use this voucher.");

    public static final Property<String> not_a_number = newProperty("voucher.requirements.not-a-number", "{prefix}&c{arg} is not a number.");

    public static final Property<String> not_a_voucher = newProperty("voucher.requirements.not-a-voucher", "{prefix}&cThat is not a Voucher Type.");
    public static final Property<String> has_blacklist_permission = newProperty("voucher.requirements.un-stack-item", "{prefix}&cSorry but you can not use this voucher because you have a black-listed permission.");

    public static final Property<String> not_in_whitelist_world = newProperty("voucher.requirements.not-in-world", "{prefix}&cYou can not use that voucher here as you are not in a whitelisted world for this voucher.");

    public static final Property<String> unstack_item = newProperty("voucher.requirements.has-blacklist-perm", "{prefix}&cYou need to unstack that item before you can use it.");

    public static final Property<String> code_unavailable = newProperty("voucher.code.unavailable", "{prefix}&cThe Voucher code &6{arg} &cis incorrect or unavailable at this time.");

    public static final Property<String> code_used = newProperty("voucher.code.used", "{prefix}&cThe voucher code &6{arg} &chas already been redeemed.");

    public static final Property<String> sent_voucher = newProperty("voucher.sent-voucher", "{prefix}&3You have just given &6{player} &3a &6{voucher} &3voucher.");

    public static final Property<String> sent_everyone_voucher = newProperty("voucher.sent-everyone-voucher", "{prefix}&3You have just given all players a &6{voucher} &3voucher.");

    public static final Property<String> config_reload = newProperty("misc.config-reload", "{prefix}&7You have just reloaded the configurations.");

    public static final Property<String> player_only = newProperty("misc.player-only", "{prefix}&cOnly players can use this command.");

    public static final Property<List<String>> help = newListProperty("misc.help", List.of(
            "&8- &6/Voucher help &3Lists all the commands for vouchers.",
            "&8- &6/Voucher list &3Lists all available types of vouchers and codes.",
            "&8- &6/Voucher redeem <code> &3Allows the player to redeem a voucher code.",
            "&8- &6/Voucher give <voucher> [amount] [player] [arguments] &3Gives a player a voucher.",
            "&8- &6/Voucher giveAll <voucher> [amount] [arguments] &3Gives all players a voucher.",
            "&8- &6/Voucher open [page] &3Opens a GUI so you can get vouchers easily.",
            "&8- &6/Voucher reload &3Reloaded the configuration files."
    ));
}