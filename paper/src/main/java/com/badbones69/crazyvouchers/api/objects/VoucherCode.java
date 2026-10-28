package com.badbones69.crazyvouchers.api.objects;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.CrazyPlatform;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import com.badbones69.crazyvouchers.utils.ItemUtils;
import com.ryderbelserion.crazyvouchers.utils.ConfigUtils;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.builders.items.ItemBuilder;
import com.ryderbelserion.fusion.paper.utils.ColorUtils;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VoucherCode {

    private @NonNull final CrazyVouchers plugin = CrazyVouchers.get();
    private @NonNull final FusionPaper fusion = this.plugin.getFusion();
    private @NonNull final CrazyPlatform platform = this.plugin.getPlatform();
    private @NonNull final ConfigManager configManager = this.platform.getConfigManager();
    private @NonNull final PropertyManager config = this.configManager.getConfig();

    private final String fileName;
    private final String name;
    private final String code;

    private final boolean caseSensitive;
    private final boolean enabled;

    private final String message;

    private final List<String> commands;

    private final List<String> whitelistPermissions = new ArrayList<>();
    private List<String> whitelistCommands = new ArrayList<>();
    private final String whitelistPermissionMessage;
    private final boolean whitelistPermissionToggle;

    private final List<String> whitelistWorldCommands = new ArrayList<>();
    private final List<String> whitelistWorlds = new ArrayList<>();
    private final boolean whitelistWorldsToggle;
    private final String whitelistWorldMessage;

    private final List<String> blacklistCommands = new ArrayList<>();
    private final List<String> blacklistPermissions = new ArrayList<>();
    private final boolean blacklistPermissionsToggle;
    private final String blacklistPermissionMessage;

    private final boolean limiterToggle;
    private final int limiterLimit;

    private final List<Sound> sounds = new ArrayList<>();
    private final boolean soundToggle;
    private final float volume;
    private final float pitch;

    private final boolean fireworkToggle;
    private final List<Color> fireworkColors = new ArrayList<>();

    private final List<VoucherCommand> randomCommands = new ArrayList<>();
    private final double totalWeight;

    private final List<ItemBuilder> items;

    public VoucherCode(@NonNull final CommentedConfigurationNode section, @NonNull final String name) {
        this.fileName = name.replaceAll(".yml", "");
        this.name = name;

        this.enabled = section.node("options", "enabled").getBoolean(false);
        this.code = section.node("code").getString("");
        this.commands = StringUtils.getStringList(section.node("commands"));

        if (this.config.getProperty(RootKeys.use_different_items_layout) && !section.node("items").isList()) {
            this.items = ItemUtils.convertConfigurationSection(section.node("items"));
        } else {
            this.items = ItemUtils.convertStringList(StringUtils.getStringList(section.node("items")));
        }

        this.caseSensitive = section.node("options", "case-sensitive").getBoolean(false);

        this.message = getMessage(section, "options", "message");

        this.whitelistPermissionToggle = section.node("options", "permission", "whitelist-permission", "toggle").getBoolean(false);
        this.whitelistCommands = ConfigUtils.getStringList(section, List.of(), "options", "permission", "whitelist-permission", "commands");
        this.whitelistPermissionMessage = getMessage(section, "options.permission.whitelist-permission.message", Message.no_permission_to_use_voucher.getMessage());

        this.whitelistPermissions.addAll(StringUtils.getStringList(section.node("options", "permission", "whitelist-permission", "permissions")).stream().map(String::toLowerCase).toList());

        final String permission = section.node("options", "permission", "whitelist-permission", "node").getString("").toLowerCase();

        if (!permission.isEmpty()) {
            this.whitelistPermissions.add(permission);
        }

        this.whitelistWorldsToggle = section.node("options", "whitelist-worlds", "toggle").getBoolean(false);
        this.whitelistWorldCommands.addAll(ConfigUtils.getStringList(section, List.of(), "options", "whitelist-worlds", "commands"));
        this.whitelistWorldMessage = getMessage(section, Message.not_in_whitelist_world.getMessage(), "options", "whitelist-worlds", "message");

        this.whitelistWorlds.addAll(StringUtils.getStringList(section.node("options", "whitelist-worlds", "worlds")).stream().map(String::toLowerCase).toList());

        this.blacklistPermissionsToggle = section.node("options", "permission", "blacklist-permission", "toggle").getBoolean(false);
        this.blacklistPermissionMessage = getMessage(section, Message.has_blacklist_permission.getMessage(), "options", "permission", "blacklist-permission", "message");
        this.blacklistPermissions.addAll(ConfigUtils.getStringList(section, List.of(), "options", "permission", "blacklist-permission", "permissions"));
        this.blacklistCommands.addAll(ConfigUtils.getStringList(section, List.of(), "options", "permission", "blacklist-permission", "commands"));

        this.limiterToggle = section.node("options", "limiter", "toggle").getBoolean(false);
        this.limiterLimit = section.node("options", "limiter", "limit").getInt(0);

        this.soundToggle = section.node("options", "sound", "toggle").getBoolean(false);
        this.volume = (float) section.node("options", "sound", "volume").getDouble(1.0);
        this.pitch = (float) section.node("options", "sound", "pitch").getDouble(1.0);

        if (this.soundToggle) {
            for (final String sound : StringUtils.getStringList(section.node("options", "sound", "sounds"))) {
                com.ryderbelserion.fusion.paper.utils.ItemUtils.getSound(sound).ifPresent(this.sounds::add);
            }
        }

        this.fireworkToggle = section.node("options", "firework", "toggle").getBoolean(false);

        if (this.fireworkToggle) {
            for (final String color : section.node("options", "firework", "colors").getString("").split(", ")) {
                this.fireworkColors.add(ColorUtils.getColor(color));
            }
        }

        if (section.hasChild("chance-commands")) {
            this.fusion.log(Level.warn, "We detected that you have the list version of chance-commands which is no longer used, Please run /crazyvouchers migrate -mt VouchersDeprecated");
        }

        if (section.hasChild("random-commands")) {
            final CommentedConfigurationNode node = section.node("random-commands");

            if (node.isList()) {
                this.fusion.log(Level.warn, "We've detected that you have the list version of random-commands which is no longer used, Please run /crazyvouchers migrate -mt VouchersDeprecated");
            } else {
                node.childrenMap().forEach((id, key) -> this.randomCommands.add(new VoucherCommand(
                        StringUtils.getStringList(key.node("commands")),
                        key.node("weight").getDouble(-1)
                )));
            }
        }

        // if the prize weight is greater than 0.0, grab it.
        this.totalWeight = this.randomCommands.stream().filter(filter -> filter.getWeight() > 0.0).mapToDouble(VoucherCommand::getWeight).sum();
    }

    public void dispatchCommands(@NonNull final Player player, @NonNull final Map<String, String> placeholders) {
        Methods.dispatch(player, this.commands, placeholders, true); // dispatch normal commands

        if (this.randomCommands.isEmpty()) return;

        // dispatch commands without a weight option randomly
        // if the prize weight is greater than 0.0D, remove it.
        final List<VoucherCommand> randomCommands = this.randomCommands.stream().filter(filter -> filter.getWeight() == -1).toList();

        if (!randomCommands.isEmpty()) {
            final VoucherCommand randomCommand = randomCommands.get(Methods.getRandom(randomCommands.size()));

            Methods.dispatch(player, randomCommand.getCommands(), placeholders, true);
        }

        // dispatch commands while accounting for the weight on each one.
        // if a prize weight is less than or equal to, remove it.
        final List<VoucherCommand> chanceCommands = this.randomCommands.stream().filter(filter -> filter.getWeight() != -1).toList();

        if (!chanceCommands.isEmpty()) {
            Methods.dispatch(player, Methods.getCommand(chanceCommands, this.totalWeight).getCommands(), placeholders, true);
        }
    }

    public boolean hasPermission(@NonNull final Player player, @NonNull final List<String> permissions, @NonNull final List<String> commands, @NonNull final Map<String, String> placeholders, @NonNull final String message, @NonNull final String argument) {
        return Methods.hasPermission(false, player, permissions, commands, placeholders, message, argument);
    }

    public String getStrippedName() {
        return this.name;
    }

    public String getFileName() {
        return this.fileName;
    }
    
    public String getCode() {
        return this.code;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public boolean isCaseSensitive() {
        return this.caseSensitive;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public List<String> getCommands() {
        return this.commands;
    }
    
    public boolean useWhiteListPermissions() {
        return this.whitelistPermissionToggle;
    }
    
    public List<String> getWhitelistPermissions() {
        return this.whitelistPermissions;
    }

    public String getWhitelistPermissionMessage() {
        return this.whitelistPermissionMessage;
    }

    public List<String> getWhitelistCommands() {
        return this.whitelistCommands;
    }
    
    public boolean useWhitelistWorlds() {
        return this.whitelistWorldsToggle;
    }
    
    public String getWhitelistWorldMessage() {
        return this.whitelistWorldMessage;
    }
    
    public List<String> getWhitelistWorlds() {
        return this.whitelistWorlds;
    }
    
    public boolean useBlacklistPermissions() {
        return this.blacklistPermissionsToggle;
    }
    
    public List<String> getWhitelistWorldCommands() {
        return this.whitelistWorldCommands;
    }
    
    public String getBlacklistMessage() {
        return this.blacklistPermissionMessage;
    }
    
    public List<String> getBlacklistPermissions() {
        return this.blacklistPermissions;
    }
    
    public List<String> getBlacklistCommands() {
        return this.blacklistCommands;
    }
    
    public boolean useLimiter() {
        return this.limiterToggle;
    }
    
    public int getLimit() {
        return this.limiterLimit;
    }
    
    public boolean useSounds() {
        return this.soundToggle;
    }
    
    public List<Sound> getSounds() {
        return this.sounds;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getVolume() {
        return this.volume;
    }
    
    public boolean useFireworks() {
        return this.fireworkToggle;
    }
    
    public List<Color> getFireworkColors() {
        return this.fireworkColors;
    }

    public double getTotalWeight() {
        return this.totalWeight;
    }

    public List<ItemBuilder> getItems() {
        return this.items;
    }

    private String getMessage(@NonNull final CommentedConfigurationNode section, @NonNull final String defaultValue, @NonNull final Object... path) {
        final CommentedConfigurationNode node = section.node(path);

        return node.isList() ? StringUtils.toString(StringUtils.getStringList(node, defaultValue)) : node.getString(defaultValue);
    }
}