package com.badbones69.crazyvouchers.config.types.locale;

import ch.jalu.configme.Comment;
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
                "    No-Permission: '{prefix}<red>You do not have permission to use that command.'",
                "  List:",
                "    No-Permission:",
                "      - '{prefix}<red>You do not have permission'",
                "      - '<red>to use that command. Please try another.'"
        };

        String[] deprecation = {
                "",
                "Warning: This section is subject to change so it is considered deprecated.",
                "This is your warning before the change happens.",
                ""
        };
        
        conf.setComment("player", header);
    }

    public static final Property<String> survival_mode = newProperty("player.survival-only", "{prefix}<red>You must be in survival mode to use vouchers.");

    @Comment("A list of available placeholders: {permission}")
    public static final Property<String> no_permission = newProperty("player.no-permission", "{prefix}<red>You do not have permission to use that command!");

    public static final Property<String> no_permission_to_use_voucher = newProperty("player.voucher.no-permission", "{prefix}<red>You do not have permission to use that voucher.");

    public static final Property<String> dupe_protection = newProperty("player.voucher.already-used.value", "{prefix}<red>This voucher has already been used, likely a duped voucher");

    @Comment({
            "This requires the permission crazyvouchers.notify.duped",
            "",
            "Available placeholders: {player}, {id}"
    })
    public static final Property<String> notify_staff = newProperty("player.voucher.already-used.notify-staff", "{prefix}{player} has been potentially caught using a duped voucher with the id {id}.");

    public static final Property<String> no_permission_to_use_voucher_in_offhand = newProperty("player.voucher.no-permission-offhand" ,"{prefix}<red>You do not have permission to use vouchers in your off hand.");

    public static final Property<String> cannot_put_items_in_crafting_table = newProperty("player.voucher.cant-put-in-crafting-table", "{prefix}<red>You cannot put vouchers in the crafting table.");

    public static final Property<String> migrated_old_vouchers = newProperty("player.voucher.migrated", "{prefix}<red>Successfully migrated old vouchers in your inventory.");

    @Comment("A list of available placeholders: {player}")
    public static final Property<String> not_online = newProperty("player.target-not-online", "{prefix}<red>That player is not online.");

    public static final Property<String> hit_voucher_limit = newProperty("player.hit-limit", "{prefix}<red>You have hit your limit for using this voucher.");

    public static final Property<String> two_step_authentication = newProperty("player.two-step-authentication", "{prefix}<gray>Right click again to confirm that you want to use this voucher.");

    public static final Property<String> not_a_number = newProperty("voucher.requirements.not-a-number", "{prefix}<red>{arg} is not a number.");

    public static final Property<String> not_a_voucher = newProperty("voucher.requirements.not-a-voucher", "{prefix}<red>That is not a Voucher Type.");

    public static final Property<String> has_blacklist_permission = newProperty("voucher.requirements.un-stack-item", "{prefix}<red>Sorry but you can not use this voucher because you have a black-listed permission.");

    public static final Property<String> not_in_whitelist_world = newProperty("voucher.requirements.not-in-world", "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.");

    public static final Property<String> unstack_item = newProperty("voucher.requirements.has-blacklist-perm", "{prefix}<red>You need to unstack that item before you can use it.");

    public static final Property<String> code_unavailable = newProperty("voucher.code.unavailable", "{prefix}<red>The Voucher code <gold>{arg} <red>is incorrect or unavailable at this time.");

    public static final Property<String> code_used = newProperty("voucher.code.used", "{prefix}<red>The voucher code <gold>{arg} <red>has already been redeemed.");

    public static final Property<String> sent_voucher = newProperty("voucher.sent-voucher", "{prefix}<aqua>You have just given <gold>{player} <aqua>a <gold>{voucher} <aqua>voucher.");

    @Comment("A list of available placeholders: {time}")
    public static final Property<String> cooldown_active = newProperty("voucher.cooldown", "{prefix}Please wait {time}, before using the voucher again");

    public static final Property<String> sent_everyone_voucher = newProperty("voucher.sent-everyone-voucher", "{prefix}<aqua>You have just given all players a <gold>{voucher} <aqua>voucher.");

    public static final Property<String> config_reload = newProperty("misc.config-reload", "{prefix}<gray>You have just reloaded the configurations.");

    public static final Property<String> player_only = newProperty("misc.player-only", "{prefix}<red>Only players can use this command.");

    public static final Property<List<String>> help = newListProperty("misc.help", List.of(
            "<dark_gray>- <gold>/crazyvouchers help <aqua>Lists all the commands for vouchers.",
            "<dark_gray>- <gold>/crazyvouchers list <aqua>Lists all available types of vouchers and codes.",
            "<dark_gray>- <gold>/crazyvouchers redeem <code> <aqua>Allows the player to redeem a voucher code.",
            "<dark_gray>- <gold>/crazyvouchers give <voucher> [amount] [player] [arguments] <aqua>Gives a player a voucher.",
            "<dark_gray>- <gold>/crazyvouchers giveall <voucher> [amount] [arguments] <aqua>Gives all players a voucher.",
            "<dark_gray>- <gold>/crazyvouchers open [page] <aqua>Opens a GUI so you can get vouchers easily.",
            "<dark_gray>- <gold>/crazyvouchers migrate -mt [type] <aqua>Runs multiple migration types to make migrating easier.",
            "<dark_gray>- <gold>/crazyvouchers reload <aqua>Reloaded the configuration files."
    ));
}