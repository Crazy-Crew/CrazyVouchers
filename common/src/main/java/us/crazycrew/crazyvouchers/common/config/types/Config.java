package us.crazycrew.crazyvouchers.common.config.types;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.PropertyInitializer;
import org.jetbrains.annotations.NotNull;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class Config implements SettingsHolder {
    
    protected Config() {}

    @Override
    public void registerComments(@NotNull CommentsConfiguration conf) {
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

    @Comment("Pick which locale you want to use if your server is in another language.")
    public static final Property<String> locale_file = newProperty("settings.locale", "en-US");

    @Comment("The prefix that shows up for all console logs.")
    public static final Property<String> console_prefix = newProperty("settings.console-prefix", "&7[&cCrazyVouchers&7]: ");

    @Comment("Whether you should only be allowed to use vouchers in survival.")
    public static final Property<Boolean> must_be_in_survival = newProperty("settings.survival-only", true);

    @Comment("Whether they should be allowed to use vouchers in recipes.")
    public static final Property<Boolean> prevent_using_vouchers_in_recipes_toggle = newProperty("settings.recipes.toggle", true);

    @Comment("Whether an alert should be sent when trying to use vouchers in recipes.")
    public static final Property<Boolean> prevent_using_vouchers_in_recipes_alert = newProperty("settings.recipes.alert", false);

    @Comment({
            "Sends anonymous statistics about how the plugin is used to bstats.org.",
            "bstats is a service for plugin developers to find out how the plugin being used,",
            "This information helps us figure out how to better improve the plugin."
    })
    public static final Property<Boolean> toggle_metrics = newProperty("settings.toggle_metrics", true);

    @Comment("Whether you want CrazyVouchers to shut up or not, This option is ignored by errors.")
    public static final Property<Boolean> verbose_logging = PropertyInitializer.newProperty("settings.verbose_logging", true);
}