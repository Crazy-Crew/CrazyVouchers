package us.crazycrew.crazyvouchers.api.config.enums;

import com.ryderbelserion.fusion.api.FusionProvider;
import com.ryderbelserion.fusion.core.api.config.properties.interfaces.IPropertyData;
import com.ryderbelserion.fusion.core.api.config.properties.objects.interfaces.Property;
import com.ryderbelserion.fusion.core.api.config.properties.objects.primitives.string.ListProperty;
import com.ryderbelserion.fusion.core.api.config.properties.objects.primitives.string.StringProperty;
import com.ryderbelserion.fusion.kyori.FusionKyori;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ryderbelserion.fusion.core.api.config.properties.PropertyBuilder.newProperty;

public enum PropertyStorage {

    command_prefix(RootKeys.command_prefix, newProperty(RootKeys.command_prefix.getDefaultValue(), "Settings", "Prefix")),

    must_be_in_survival(RootKeys.must_be_in_survival, newProperty(RootKeys.must_be_in_survival.getDefaultValue(), "Settings", "Must-Be-In-Survival")),

    prevent_using_vouchers_in_recipes_toggle(RootKeys.prevent_using_vouchers_in_recipes_toggle, newProperty(RootKeys.prevent_using_vouchers_in_recipes_toggle.getDefaultValue(), "Settings", "Prevent-Using-Vouchers-In-Recipes", "Toggle")),
    prevent_using_vouchers_in_recipes_alert(RootKeys.prevent_using_vouchers_in_recipes_alert, newProperty(RootKeys.prevent_using_vouchers_in_recipes_toggle.getDefaultValue(), "Settings", "Prevent-Using-Vouchers-In-Recipes", "Alert")),

    file_system(RootKeys.file_system, newProperty("MULTIPLE", "settings", "use-old-file-system")),

    ;

    /*

    survival_mode(MessageKeys.survival_mode, newProperty("Messages.Survival-Mode", MessageKeys.survival_mode.getDefaultValue())),
    no_permission(MessageKeys.no_permission, newProperty("Messages.No-Permission", MessageKeys.no_permission.getDefaultValue())),
    no_permission_to_use_voucher(MessageKeys.no_permission_to_use_voucher, newProperty("Messages.No-Permission-To-Voucher", MessageKeys.no_permission_to_use_voucher.getDefaultValue())),
    no_permission_to_use_voucher_in_offhand(MessageKeys.no_permission_to_use_voucher_in_offhand, newProperty("Messages.No-Permission-To-Use-Voucher-In-OffHand", MessageKeys.no_permission_to_use_voucher_in_offhand.getDefaultValue())),
    cannot_put_items_in_crafting_table(MessageKeys.cannot_put_items_in_crafting_table, newProperty("Messages.Cannot-Put-Items-In-Crafting-Table", MessageKeys.cannot_put_items_in_crafting_table.getDefaultValue())),
    not_online(MessageKeys.not_online, newProperty("Messages.Not-Online", MessageKeys.not_online.getDefaultValue())),
    two_step_authentication(MessageKeys.two_step_authentication, newProperty("Messages.Two-Step-Authentication", MessageKeys.two_step_authentication.getDefaultValue())),
    hit_voucher_limit(MessageKeys.hit_voucher_limit, newProperty("Messages.Hit-Limit", MessageKeys.hit_voucher_limit.getDefaultValue())),
    not_a_number(MessageKeys.not_a_number, newProperty("Messages.Not-A-Number", MessageKeys.not_a_number.getDefaultValue())),
    not_a_voucher(MessageKeys.not_a_voucher, newProperty("Messages.Not-A-Voucher", MessageKeys.not_a_voucher.getDefaultValue())),
    not_in_whitelist_world(MessageKeys.not_in_whitelist_world, newProperty("Messages.Not-In-Whitelisted-World", MessageKeys.not_in_whitelist_world.getDefaultValue())),
    unstack_item(MessageKeys.unstack_item, newProperty("Messages.Unstack-Item", MessageKeys.unstack_item.getDefaultValue())),
    has_blacklist_permission(MessageKeys.has_blacklist_permission, newProperty("Messages.Has-Blacklist-Permission", MessageKeys.has_blacklist_permission.getDefaultValue())),
    code_used(MessageKeys.code_used, newProperty("Messages.Code-Used", MessageKeys.code_used.getDefaultValue())),
    code_unavailable(MessageKeys.code_unavailable, newProperty("Messages.Code-UnAvailable", MessageKeys.code_unavailable.getDefaultValue())),
    sent_voucher(MessageKeys.sent_voucher, newProperty("Messages.Given-A-Voucher", MessageKeys.sent_voucher.getDefaultValue())),
    sent_everyone_voucher(MessageKeys.sent_everyone_voucher, newProperty("Messages.Given-All-Players-Voucher", MessageKeys.sent_everyone_voucher.getDefaultValue())),
    player_only(MessageKeys.player_only, newProperty("Messages.Players-Only", MessageKeys.player_only.getDefaultValue())),
    config_reload(MessageKeys.config_reload, newProperty("Messages.Config-Reload", MessageKeys.config_reload.getDefaultValue())),
    help(MessageKeys.help, newListProperty("Messages.Help", MessageKeys.help.getDefaultValue()), Collections.emptyList());*/

    private final FusionKyori fusion = (FusionKyori) FusionProvider.api();

    private final Property newProperty;
    private final Property oldProperty;

    PropertyStorage(final Property newProperty, final Property oldProperty) {
        this.newProperty = newProperty;
        this.oldProperty = oldProperty;
    }

    public boolean copy(final CommentedConfigurationNode configuration, final IPropertyData propertyData) {
        final Object[] path = this.oldProperty.getPath();

        if (!configuration.hasChild(path)) {
            return false;
        }

        try {
            final CommentedConfigurationNode node = configuration.node(path);

            propertyData.setProperty(configuration, this.newProperty, replace(node, propertyData));

            final CommentedConfigurationNode parent = node.parent();

            if (parent != null) {
                final Map<Object, CommentedConfigurationNode> children = new HashMap<>(parent.childrenMap());

                final int position = path.length - 1;

                final Object value = path[position];

                if (children.containsKey(value)) {
                    final CommentedConfigurationNode child = children.remove(value);

                    child.set(null);
                }

                if (children.isEmpty()) {
                    parent.set(null);
                }
            }
        } catch (final SerializationException exception) {
            throw new RuntimeException(exception);
        }

        return true;
    }

    /**
     * Replaces old placeholders in the option when migrating.
     *
     * @return the finalized message to set
     */
    private @NonNull Object replace(@NonNull final CommentedConfigurationNode configuration, @NonNull final IPropertyData propertyData) {
        final Map<String, String> placeholders = new HashMap<>();

        placeholders.put("%Arg%", "{arg}");
        placeholders.put("%arg%", "{arg}");
        placeholders.put("%Player%", "{player}");
        placeholders.put("%player%", "{player}");
        placeholders.put("%Prefix%", "{prefix}");
        placeholders.put("%prefix%", "{prefix}");
        placeholders.put("%Random%", "{random}");
        placeholders.put("%random%", "{random}");
        placeholders.put("%World%", "{world}");
        placeholders.put("%world%", "{world}");
        placeholders.put("%voucher%", "{voucher}");
        placeholders.put("%Voucher%", "{voucher}");
        placeholders.put("%X%", "{x}");
        placeholders.put("%x%", "{x}");
        placeholders.put("%Y%", "{y}");
        placeholders.put("%y%", "{y}");
        placeholders.put("%Z%", "{z}");
        placeholders.put("%z%", "{z}");
        placeholders.put("%Permission%", "{permission}");
        placeholders.put("%permission%", "{permission}");

        if (this.oldProperty instanceof ListProperty property) {
            final List<String> values = new ArrayList<>();

            propertyData.getProperty(configuration, property).forEach(line -> values.add(this.fusion.replacePlaceholders(line, placeholders)));

            return values;
        }

        if (this.oldProperty instanceof StringProperty property) {
            return this.fusion.replacePlaceholders(propertyData.getProperty(configuration, property), placeholders);
        }

        return propertyData.getProperty(configuration, this.oldProperty);
    }
}