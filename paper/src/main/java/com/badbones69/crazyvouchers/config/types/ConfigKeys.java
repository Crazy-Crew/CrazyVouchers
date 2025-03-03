package com.badbones69.crazyvouchers.config.types;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import static ch.jalu.configme.properties.PropertyInitializer.newBeanProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class ConfigKeys implements SettingsHolder {
    
    protected ConfigKeys() {}

    @Override
    public void registerComments(CommentsConfiguration conf) {
        String[] header = {
                "Support: https://discord.gg/badbones-s-live-chat-182615261403283459",
                "Github: https://github.com/Crazy-Crew",
                "",
                "Issues: https://github.com/Crazy-Crew/CrazyVouchers/issues",
                "Features: https://github.com/Crazy-Crew/CrazyVouchers/issues",
                ""
        };

        String[] deprecation = {
                "",
                "Warning: This section is subject to change so it is considered deprecated.",
                "This is your warning before the change happens.",
                ""
        };
        
        conf.setComment("settings", header);
    }
    
    @Comment("The prefix that shows up for all commands.")
    public static final Property<String> command_prefix = newProperty("settings.prefix", "&7[&6CrazyVouchers&7]: ");

    @Comment("""
            This option allows you to switch between the following
            ⤷ vouchers.yml and codes.yml
             ⤷ these files handle all files!
            ⤷ vouchers and codes that act like the crates folder in CrazyCrates
             ⤷ each file is considered a voucher with the name of the file being the voucher name
            
            ⤷ Once you switch this, You should run /crazyvouchers migrate -mt VouchersSwitch otherwise you will have no vouchers.
            
            ⤷ Available types
              ⤷ SINGLE
              ⤷ MULTIPLE
            """)
    public static final Property<FileSystem> file_system = newBeanProperty(FileSystem.class, "settings.file-system", FileSystem.MULTIPLE);

    @Comment("Sends anonymous statistics to https://bstats.org/plugin/bukkit/Vouchers/4536")
    public static final Property<Boolean> toggle_metrics = newProperty("root.toggle-metrics", true);

    //@Comment("Should players have a voucher cooldown?")
    //public static final Property<Boolean> cooldown_toggle = newProperty("settings.cooldown.toggle", false);

    //@Comment("How many seconds should a player have to wait, before opening a new voucher?")
    //public static final Property<Integer> cooldown_wait = newProperty("settings.cooldown.interval", 5);

    @Comment({
            "Vouchers will no longer be able to stack, as each one has a unique identifier",
            "Once the voucher is used, the uuid attached is thrown in a file.",
            "",
            "If you turn this option off, no new uuids will be attached or checked",
            "however previous vouchers will still not stack, obviously."
    })
    public static final Property<Boolean> dupe_protection = newProperty("settings.dupe-protection.enabled", false);

    @Comment("This decides whether a line should be added to duplicated voucher lore to avoid scams.")
    public static final Property<Boolean> dupe_protection_toggle_warning = newProperty("settings.dupe-protection.warning.enabled", false);

    @Comment("The line to add to the lore of an item. This message supports PlaceholderAPI")
    public static final Property<String> dupe_protection_warning = newProperty("settings.dupe-protection.warning.value", "&cThis item has been duplicated");

    @Comment("Pick which locale you want to use if your server is in another language.")
    public static final Property<String> locale_file = newProperty("settings.locale", "en-US");

    @Comment("Whether you should only be allowed to use vouchers in survival.")
    public static final Property<Boolean> must_be_in_survival = newProperty("settings.survival-only", true);

    @Comment("Whether they should be allowed to use vouchers in recipes.")
    public static final Property<Boolean> prevent_using_vouchers_in_recipes_toggle = newProperty("settings.recipes.toggle", true);

    @Comment("Whether an alert should be sent when trying to use vouchers in recipes.")
    public static final Property<Boolean> prevent_using_vouchers_in_recipes_alert = newProperty("settings.recipes.alert", false);
}