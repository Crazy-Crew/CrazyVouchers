package us.crazycrew.crazyvouchers.api.config.types;

import com.ryderbelserion.fusion.core.api.config.annotations.Comment;
import com.ryderbelserion.fusion.core.api.config.properties.builders.CommentsBuilder;
import com.ryderbelserion.fusion.core.api.config.properties.interfaces.IPropertyHolder;
import com.ryderbelserion.fusion.core.api.config.properties.objects.interfaces.Property;
import org.jspecify.annotations.NonNull;
import us.crazycrew.crazyvouchers.api.enums.messages.State;
import us.crazycrew.crazyvouchers.api.enums.other.FileSystem;

import java.util.List;
import static com.ryderbelserion.fusion.core.api.config.properties.PropertyBuilder.newBeanProperty;
import static com.ryderbelserion.fusion.core.api.config.properties.PropertyBuilder.newProperty;

public class RootKeys implements IPropertyHolder {

    @Override
    public void registerComments(@NonNull final CommentsBuilder configuration) {
        final List<String> deprecation = List.of(
                " ",
                "Warning: This section is subject to change so it is considered deprecated.",
                "This is your warning before the change happens. Please read the latest changelogs",
                " "
        );

        configuration.setComment(List.of(
                "Support: https://discord.gg/badbones-s-live-chat-182615261403283459",
                "Github: https://github.com/Crazy-Crew",
                " ",
                "Issues: https://github.com/Crazy-Crew/CrazyCrates/issues",
                "Features: https://github.com/Crazy-Crew/CrazyCrates/issues",
                " ",
                "Sounds: https://mudkipdev.github.io/minecraft-sound-explorer/"
        ), "root");

        configuration.setComment("Configure everything CrazyVouchers has to offer!", "settings");

        configuration.setComment("Allows you to protect your server against dupe protection!", "settings", "dupe-protection");

        configuration.setComment("Should the server or staff be informed of a duplicated item?", "settings", "dupe-protection", "warning");

        configuration.setComment("Allows you to configure whether vouchers can be used in recipes!", "settings", "recipes");
    }

    @Comment("Sends anonymous statistics to https://bstats.org/plugin/bukkit/Vouchers/4536")
    public static final Property<Boolean> toggle_metrics = newProperty(true, "root", "toggle-metrics");

    @Comment({
            "This will allow you to use a new items layout which should be much more straight forward, less prone to errors.",
            "You can run /crazyvouchers migrate -mt NewItemFormat which should migrate all existing prizes after you set this to true",
            "",
            "Take a backup before you run the migration as there may be bugs."
    })
    public static final Property<Boolean> use_different_items_layout = newProperty(false, "root", "use-different-items-layout");

    @Comment({
            "This option will tell the plugin to send all messages as action bars or messages in chat.",
            "",
            "send_message -> sends messages in chat.",
            "send_actionbar -> sends messages in actionbar.",
            ""
    })
    public static final Property<State> message_state = newBeanProperty(State.class, State.send_message, "root", "message-state");

    @Comment("This will wipe the example folder on /crazyvouchers reload or plugin startup. so you always have fresh examples to look at.")
    public static final Property<Boolean> update_examples_folder = newProperty(true, "root", "update-examples-folder");

    @Comment("The prefix that shows up for all commands.")
    public static final Property<String> command_prefix = newProperty("<gray>[<gold>CrazyVouchers<gray>]: ", "settings", "prefix");

    @Comment("""
            This option allows you to switch between the following:
            ⤷ vouchers.yml and codes.yml
             ⤷ these files handle all files!
            ⤷ vouchers and codes that act like the crates folder in CrazyCrates
             ⤷ each file is considered a voucher with the name of the file being the voucher name
            
            ⤷ Once you switch this, You should run /crazyvouchers migrate -mt VouchersSwitch otherwise you will have no vouchers.
            
            ⤷ Available types
              ⤷ SINGLE
              ⤷ MULTIPLE
            """)
    public static final Property<FileSystem> file_system = newBeanProperty(FileSystem.class, FileSystem.MULTIPLE, "settings", "file-system");

    @Comment({
            "Vouchers will no longer be able to stack, as each one has a unique identifier",
            "Once the voucher is used, the uuid attached is thrown in a file.",
            "",
            "If you turn this option off, no new uuids will be attached or checked",
            "however previous vouchers will still not stack, obviously."
    })
    public static final Property<Boolean> dupe_protection = newProperty(false, "settings", "dupe-protection", "enabled");

    @Comment("This decides whether a line should be added to duplicated voucher lore to avoid scams.")
    public static final Property<Boolean> dupe_protection_warning_toggle = newProperty(false, "settings", "dupe-protection", "warning", "enabled");

    @Comment("The line to add to the lore of an item. This message supports PlaceholderAPI")
    public static final Property<String> dupe_protection_warning_text = newProperty("<red>This item has been duplicated", "settings", "dupe-protection", "warning", "value");

    @Comment("If you set this to true, you will be allowing vouchers to be used in off hand.")
    public static final Property<Boolean> allow_off_hand_usage = newProperty(false, "settings", "allow-off-hand-usage");

    @Comment("Whether you should only be allowed to use vouchers in survival.")
    public static final Property<Boolean> must_be_in_survival = newProperty(true, "settings", "survival-only");

    @Comment({
            "If you have this set to true, The voucher cannot be used in recipes.",
            "If you have this set to false, The voucher can be used in recipes.",
    })
    public static final Property<Boolean> prevent_using_vouchers_in_recipes_toggle = newProperty(true, "settings", "recipes", "toggle");

    @Comment({
            "If you have this set to true, The alert is sent to the player.",
            "If you have this set to false, There is no alert.",
    })
    public static final Property<Boolean> prevent_using_vouchers_in_recipes_alert = newProperty(false, "settings", "recipes", "alert");

}