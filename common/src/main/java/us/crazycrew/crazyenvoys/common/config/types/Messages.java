package us.crazycrew.crazyenvoys.common.config.types;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class Messages implements SettingsHolder {

    protected Messages() {}

    @Override
    public void registerComments(@NotNull CommentsConfiguration conf) {
        String[] header = {
                "Support: https://discord.gg/badbones-s-live-chat-182615261403283459",
                "Github: https://github.com/Crazy-Crew",
                "",
                "Issues: https://github.com/Crazy-Crew/CrazyVouchers/issues",
                "Features: https://github.com/Crazy-Crew/CrazyVouchers/issues",
                "",
                "Tips:",
                " 1. Make sure to use the %prefix% to add the prefix in front of messages.",
                " 2. If you wish to use more than one line for a message just go from a line to a list.",
                "Examples:",
                "  Line:",
                "    No-Permission: '%prefix%&cYou do not have permission to use that command.'",
                "  List:",
                "    No-Permission:",
                "      - '%prefix%&cYou do not have permission'",
                "      - '&cto use that command. Please try another.'"
        };

        String[] deprecation = {
                "",
                "Warning: This section is subject to change so it is considered deprecated.",
                "This is your warning before the change happens.",
                ""
        };
        
        conf.setComment("Messages", header);
    }

    public static final Property<String> config_reload = newProperty("Messages.Config-Reload", "&7You have just reloaded the configurations.");

    public static final Property<String> survival_mode = newProperty("Messages.Survival-Mode", "&cYou must be in survival mode to use vouchers.");

    public static final Property<String> player_only = newProperty("Messages.Players-Only", "&cOnly players can use this command.");

    public static final Property<String> no_permission = newProperty("Messages.No-Permission", "&cYou do not have permission to use that command!");

    public static final Property<String> no_permission_to_use_voucher = newProperty("Messages.No-Permission-To-Voucher", "&cYou do not have permission to use that voucher.");

    public static final Property<String> no_permission_to_use_voucher_in_offhand = newProperty("Messages.No-Permission-To-Use-Voucher-In-OffHand" ,"&cYou do not have permission to use vouchers in your off hand.");

    public static final Property<String> cannot_put_items_in_crafting_table = newProperty("Messages.Cannot-Put-Items-In-Crafting-Table", "&cYou cannot put vouchers in the crafting table.");

    public static final Property<String> not_online = newProperty("Messages.Not-Online", "&cThat player is not online.");

    public static final Property<String> not_a_number = newProperty("Messages.Not-A-Number", "&c%arg% is not a number.");

    public static final Property<String> not_a_voucher = newProperty("Messages.Not-A-Voucher", "&cThat is not a Voucher Type.");

    public static final Property<String> code_unavailable = newProperty("Messages.Code-UnAvailable", "&cThe Voucher code &6%arg% &cis incorrect or unavailable at this time.");

    public static final Property<String> code_used = newProperty("Messages.Code-Used", "&cThe voucher code &6%arg% &chas already been redeemed.");

    public static final Property<String> sent_voucher = newProperty("Messages.Given-A-Voucher", "&3You have just given &6%player% &3a &6%voucher% &3voucher.");

    public static final Property<String> sent_everyone_voucher = newProperty("Messages.Given-All-Players-Voucher", "&3You have just given all players a &6%voucher% &3voucher.");

    public static final Property<String> hit_voucher_limit = newProperty("Messages.Hit-Limit", "&cYou have hit your limit for using this voucher.");

    public static final Property<String> two_step_authentication = newProperty("Messages.Two-Step-Authentication", "&7Right click again to confirm that you want to use this voucher.");

    public static final Property<String> has_blacklist_permission = newProperty("Messages.Has-Blacklist-Permission", "&cSorry but you can not use this voucher because you have a black-listed permission.");

    public static final Property<String> not_in_whitelist_world = newProperty("Messages.Not-In-Whitelisted-World", "&cYou can not use that voucher here as you are not in a whitelisted world for this voucher.");

    public static final Property<String> unstack_item = newProperty("Messages.Unstack-Item", "&cYou need to unstack that item before you can use it.");

    public static final Property<List<String>> help = newListProperty("Messages.Help", List.of(
            "&8- &6/Voucher help &3Lists all the commands for vouchers.",
            "&8- &6/Voucher list &3Lists all available types of vouchers and codes.",
            "&8- &6/Voucher redeem <code> &3Allows the player to redeem a voucher code.",
            "&8- &6/Voucher give <voucher> [amount] [player] [arguments] &3Gives a player a voucher.",
            "&8- &6/Voucher giveAll <voucher> [amount] [arguments] &3Gives all players a voucher.",
            "&8- &6/Voucher open [page] &3Opens a GUI so you can get vouchers easily.",
            "&8- &6/Voucher reload &3Reloaded the configuration files."
    ));
}