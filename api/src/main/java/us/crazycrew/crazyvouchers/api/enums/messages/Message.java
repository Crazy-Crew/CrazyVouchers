package us.crazycrew.crazyvouchers.api.enums.messages;

import com.ryderbelserion.fusion.api.objects.FusionKey;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import com.ryderbelserion.fusion.core.api.config.properties.objects.enums.PropertyType;
import com.ryderbelserion.fusion.core.api.registry.message.MessageRegistry;
import com.ryderbelserion.fusion.core.api.registry.message.adapter.YamlMessageAdapter;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.files.types.configurate.YamlCustomFile;
import com.ryderbelserion.fusion.kyori.utils.AdvUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import us.crazycrew.crazyvouchers.VouchersProvider;
import us.crazycrew.crazyvouchers.api.CrazyVouchers;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static us.crazycrew.crazyvouchers.api.CrazyVouchers.namespace;

@NullMarked
public enum Message {

    error_migration("error.migration", "Available placeholders: {file}, {type}, {reason}", "{prefix}<red>We could not migrate <green>{file} <red>using <green>{type} <red>migration for <green>{reason}.", "command", "migrate", "error"),
    migration_not_available("migration.not.available", "",  "{prefix}<green>This migration type is not available.", "command", "migrate", "not-available"),
    migration_plugin_not_enabled("migration.plugin.not.enabled", List.of("Available placeholders: {name}"), "{prefix}<green>The plugin <red>{name} <green>is not enabled. Cannot use as migration!", "command", "migrate", "plugin-not-available"),
    migration_no_vouchers_available("migration.no.vouchers.available", "", "{prefix}<green>There is no vouchers available for migration!", "command", "migrate", "no-vouchers-available"),
    migration_no_codes_available("migration.no.codes.available", "", "{prefix}<green>There is no codes available for migration!", "command", "migrate", "no-codes-available"),
    successfully_migrated_users("successfully.migrated.users", List.of(
            "Available placeholders: {type}, {files}",
            "",
            "{files} will output multiple vouchers if migrating from another plugin."
    ), List.of(
            "<bold><gold>━━━━━━━━━━━━━━━━━━━ Migration Stats ━━━━━━━━━━━━━━━━━━━</gold></bold>",
            "<dark_gray>»</dark_gray> <green>Successful Conversions: ",
            " ⤷ {succeeded_amount}</green>",
            "<dark_gray>»</dark_gray> <red>Failed Conversions: ",
            " ⤷ {failed_amount}</red>",
            "",
            "<red>Conversion Time: <yellow>{time}",
            "<red>Conversion Type: <yellow>{type}",
            "",
            "<bold><gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold></bold>"
    ), "command", "migrate", "success-users"),

    unknown_command("misc.unknown-command", "Available placeholders: {command}", "{prefix}<red>{command} is not a known command.", "misc", "unknown-command"),
    correct_usage("misc.correct-usage", "Available placeholders: {usage}", "{prefix}<red>The correct usage for this command is <yellow>{usage}", "misc", "correct-usage"),
    feature_disabled("misc.feature-disabled", "", "{prefix}<red>This feature is disabled.", "misc", "feature-disabled"),

    must_be_console_sender("misc.must.be.console.sender", "", "{prefix}<red>You must be using console to use this command.", "misc", "must-be-console-sender"),

    same_player("misc.target.same.player", "", "{prefix}<red>You cannot use this command on yourself.", "misc", "target-same-player"),

    inventory_not_empty("misc.inventory.not.empty", "", "{prefix}<red>Inventory is not empty, Please clear up some room.", "misc", "inventory-not-empty"),

    lacking_flag("misc.lacking.flag", "Available placeholders: {flag}, {usage}", "{prefix}<red>{flag} is not present in the command, expected format: {usage}", "misc", "lacking-flag"),

    vouchers_list("misc.vouchers.list", List.of(
            "The format for the /crazyvouchers list command",
            "",
            "A list of available placeholders",
            " ⤷ {total_vouchers} » Shows the total vouchers count",
            " ⤷ {total_codes} » Shows the total codes count",
            " ⤷ {active_vouchers} » Shows the active vouchers count",
            " ⤷ {active_codes} » Shows the active codes count",
            " ⤷ {broken_vouchers} » Shows the broken vouchers count",
            " ⤷ {broken_codes} » Shows the broken codes count",
            " ⤷ {vouchers} » Shows all vouchers broken and not broken",
            " ⤷ {codes} » Shows all codes broken and not broken",
            ""
    ), List.of(
            "<bold><gold>━━━━━━━━━━━━━━━━━━━ Plugin Stats ━━━━━━━━━━━━━━━━━━━</gold></bold>",
            "<dark_gray>»</dark_gray> <gold>Code Statistics <dark_gray>[</dark_gray><green>{active_codes}</green><dark_gray>/</dark_gray><gold>{total_codes}</gold><dark_gray>]</dark_gray>",
            "<dark_gray>-</dark_gray> {codes}",
            "<dark_gray>»</dark_gray> <gold>Voucher Statistics <dark_gray>[</dark_gray><green>{active_vouchers}</green><dark_gray>/</dark_gray><gold>{total_vouchers}</gold><dark_gray>]</dark_gray>",
            "<dark_gray>-</dark_gray> {vouchers}",
            "<bold><gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold></bold>"
    ), "misc", "vouchers-list"),

    survival_only("player.survival.only", "", "{prefix}<red>You must be in survival mode to use vouchers.", "player", "survival-only"),

    no_permission("player.no.permission", "Available placeholders: {permission}", "{prefix}<red>You do not have permission to use that command!", "player", "no-permission"),

    no_permission_to_use_voucher("player.voucher.no.permission", "", "{prefix}<red>You do not have permission to use that voucher.", "player", "voucher", "no-permission"),

    dupe_protection("player.voucher.already.used.value", "", "{prefix}<red>This voucher has already been used, likely a duped voucher", "player", "voucher", "already-used", "value"),

    dupe_protection_notify("player.voucher.already.used.notify.staff", List.of(
            "This requires the permission crazyvouchers.notify.duped",
            "",
            "Available placeholders: {player}, {id}"
    ), "{prefix}{player} has been potentially caught using a duped voucher with the id {id}.", "player", "voucher", "already-used", "notify-staff"),

    no_permission_to_use_voucher_in_offhand("player.voucher.no.permission.offhand", "", "{prefix}<red>You do not have permission to use vouchers in your off hand.", "player", "voucher", "no-permission-offhand"),
    cannot_put_items_in_crafting_table("player.voucher.cant.put.in.crafting.table", "", "{prefix}<red>You cannot put vouchers in the crafting table.", "player", "voucher", "cant-put-in-crafting-table"),
    migrated_old_vouchers("player.voucher.migrated", "", "{prefix}<red>Successfully migrated old vouchers in your inventory.", "player", "voucher", "migrated"),
    not_online("player.target.not.online", "Available placeholders: {player}", "{prefix}<red>That player is not online.", "player", "target-not-online"),

    hit_voucher_limit("player.hit.limit", "", "{prefix}<red>You have hit your limit for using this voucher.", "player", "hit-limit"),
    two_step_authentication("player.two.step.authentication", "", "{prefix}<gray>Right click again to confirm that you want to use this voucher.", "player", "two-step-authentication"),

    not_a_number("voucher.requirements.not.a.number", "", "{prefix}<red>{arg} is not a number.", "voucher", "requirements", "not-a-number"),
    not_a_voucher("voucher.requirements.not.a.voucher", "", "{prefix}<red>That is not a Voucher Type.", "voucher", "requirements", "not-a-voucher"),

    has_blacklist_permission("voucher.requirements.has.blacklist.perm", "", "{prefix}<red>Sorry but you can not use this voucher because you have a black-listed permission.", "voucher", "requirements", "has-blacklist-perm"),
    unstack_item("voucher.requirements.unstack.item", "", "{prefix}<red>You need to unstack that item before you can use it.", "voucher", "requirements", "unstack-item"),

    not_in_whitelist_world("voucher.requirements.not.in.world", "", "{prefix}<red>You can not use that voucher here as you are not in a whitelisted world for this voucher.", "voucher", "requirements", "not-in-world"),

    code_unavailable("voucher.code.unavailable", "", "{prefix}<red>The Voucher code <gold>{arg} <red>is incorrect or unavailable at this time.", "voucher", "code", "unavailable"),
    code_used("voucher.code.used", "", "{prefix}<red>The voucher code <gold>{arg} <red>has already been redeemed.", "voucher", "code", "used"),

    sent_everyone_voucher("voucher.sent.everyone.voucher", "", "{prefix}<aqua>You have just given all players a <gold>{voucher} <aqua>voucher.", "voucher", "sent-everyone-voucher"),
    sent_voucher("voucher.sent.voucher", "", "{prefix}<aqua>You have just given <gold>{player} <aqua>a <gold>{voucher} <aqua>voucher.", "voucher", "sent-voucher"),
    cooldown_active("voucher.cooldown", "Available placeholders: {time}", "{prefix}Please wait {time}, before using the voucher again", "voucher", "cooldown"),

    config_reload("misc.config.reload", "", "{prefix}<gray>You have just reloaded the configurations.", "misc", "config-reload"),
    player_only("misc.player.only", "", "{prefix}<red>Only players can use this command.", "misc", "player-only"),

    help("misc.help", "", List.of(
            "<dark_gray>- <gold>/crazyvouchers help <aqua>Lists all the commands for vouchers.",
            "<dark_gray>- <gold>/crazyvouchers list <aqua>Lists all available types of vouchers and codes.",
            "<dark_gray>- <gold>/crazyvouchers redeem <code> <aqua>Allows the player to redeem a voucher code.",
            "<dark_gray>- <gold>/crazyvouchers give <voucher> [amount] [player] [arguments] <aqua>Gives a player a voucher.",
            "<dark_gray>- <gold>/crazyvouchers giveall <voucher> [amount] [arguments] <aqua>Gives all players a voucher.",
            "<dark_gray>- <gold>/crazyvouchers open [page] <aqua>Opens a GUI so you can get vouchers easily.",
            "<dark_gray>- <gold>/crazyvouchers migrate -mt [type] <aqua>Runs multiple migration types to make migrating easier.",
            "<dark_gray>- <gold>/crazyvouchers reload <aqua>Reloaded the configuration files."
    ), "misc", "help");


    private final CrazyVouchers platform = VouchersProvider.api();

    private final Path dataPath = this.platform.getDataPath();

    private final FileManager fileManager = this.platform.getFusion().getFileManager();

    private final ConfigManager configManager = this.platform.getConfigManager();

    private final PropertyManager pluginConfig = this.configManager.getConfig();

    private final List<String> defaultComment;
    private final String defaultValue;
    private final PropertyType type;
    private final Object[] path;
    private final FusionKey id;

    Message(final String id, final List<String> defaultComment, final String defaultValue, final Object... path) {
        this.defaultComment = defaultComment;
        this.defaultValue = defaultValue;
        this.id = FusionKey.key(namespace, id);
        this.type = PropertyType.STRING;
        this.path = path;
    }

    Message(final String id, final List<String> defaultComment, final List<String> defaultValue, final Object... path) {
        this.defaultValue = StringUtils.toString(defaultValue);
        this.defaultComment = defaultComment;
        this.id = FusionKey.key(namespace, id);
        this.type = PropertyType.STRING_LIST;
        this.path = path;
    }

    Message(final String id, final String defaultComment, final String defaultValue, final Object... path) {
        this(id, List.of(defaultComment), defaultValue, path);
    }

    Message(final String id, final String defaultComment, final List<String> defaultValue, final Object... path) {
        this(id, List.of(defaultComment), defaultValue, path);
    }

    public void addKey(final MessageRegistry registry, final YamlCustomFile customFile, final CommentedConfigurationNode configuration, final FusionKey id) {
        final YamlMessageAdapter adapter = new YamlMessageAdapter(configuration, this.defaultValue, this.path);

        if (!configuration.hasChild(this.path)) {
            final CommentedConfigurationNode section = configuration.node(this.path);

            switch (this.type) {
                case STRING_LIST -> {
                    try {
                        section.set(toList(this.defaultValue));

                        if (!this.defaultComment.isEmpty()) {
                            section.commentIfAbsent(StringUtils.toString(this.defaultComment));
                        }

                        customFile.save();
                    } catch (final SerializationException exception) {
                        exception.printStackTrace();
                    }
                }

                case STRING -> {
                    try {
                        section.set(this.defaultValue);

                        if (!this.defaultComment.isEmpty()) {
                            section.commentIfAbsent(StringUtils.toString(this.defaultComment));
                        }

                        customFile.save();
                    } catch (final SerializationException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }

        registry.addKey(
                id,
                this.id,
                adapter
        );
    }

    public List<String> toList(final String value) {
        return value.lines().toList();
    }

    public void sendMessage(final Audience audience, final Map<String, String> placeholders) {
        final UUID uuid = audience.getOrDefault(Identity.UUID, CrazyVouchers.CONSOLE_UUID);

        switch (this.pluginConfig.getProperty(RootKeys.message_state)) {
            case send_message -> this.platform.getSenderAdapter(uuid).sendMessage(this.id, placeholders);
            case send_actionbar -> this.platform.getSenderAdapter(uuid).sendActionBar(this.id, placeholders);
        }
    }

    public void sendMessage(final Audience audience, final String placeholder, final String value) {
        sendMessage(audience, Map.of(placeholder, value));
    }

    public void sendMessage(final Audience audience) {
        sendMessage(audience, Map.of());
    }

    public void convert() {
        this.fileManager.getYamlFile(this.dataPath.resolve("messages.yml")).ifPresent(customFile -> {
            final String message = getMessage();

            final CommentedConfigurationNode node = customFile.getConfiguration().node(this.path);

            if (this.type == PropertyType.STRING_LIST) {
                final List<String> values = new ArrayList<>();

                for (final String line : message.split("\n")) {
                    if (line.isBlank()) {
                        values.add(line);

                        continue;
                    }

                    values.add(AdvUtils.convert(line, true));
                }

                try {
                    node.set(List.class, values);
                } catch (final SerializationException exception) {
                    exception.printStackTrace();
                }

                return;
            }

            try {
                node.set(List.class, AdvUtils.convert(message, true));
            } catch (final SerializationException exception) {
                exception.printStackTrace();
            }
        });
    }

    public String getMessage(final Audience audience, final Map<String, String> placeholders) {
        return this.platform.getSenderAdapter(audience.getOrDefault(Identity.UUID, CrazyVouchers.CONSOLE_UUID)).getMessage(this.id, placeholders);
    }

    public String getMessage(final Audience audience, final String placeholder, final String value) {
        return getMessage(audience, Map.of(placeholder, value));
    }

    public String getMessage(final Audience audience) {
        return this.platform.getSenderAdapter(audience.getOrDefault(Identity.UUID, CrazyVouchers.CONSOLE_UUID)).getMessage(this.id);
    }

    public String getMessage() {
        return getMessage(Audience.empty());
    }

    public FusionKey getKey() {
        return this.id;
    }
}