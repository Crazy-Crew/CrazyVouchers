package com.badbones69.crazyvouchers.api.objects;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.utils.ItemUtils;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.builders.ItemBuilder;
import com.ryderbelserion.fusion.paper.utils.ColorUtils;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VoucherCode {

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();
    private @NotNull final FusionPaper fusion = this.plugin.getFusion();
    private @NotNull final StringUtils utils = this.fusion.getStringUtils();
    private @NotNull final SettingsManager config = ConfigManager.getConfig();

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

    public VoucherCode(@NotNull final ConfigurationSection section, @NotNull final String name) {
        this.fileName = name.replaceAll(".yml", "");
        this.name = name;

        this.enabled = section.getBoolean("options.enabled", false);
        this.code = section.getString("code", "");
        this.commands = section.getStringList("commands");

        if (this.config.getProperty(ConfigKeys.use_different_items_layout) && !section.isList("items")) {
            this.items = ItemUtils.convertConfigurationSection(section.getConfigurationSection("items"));
        } else {
            this.items = ItemUtils.convertStringList(section.getStringList("items"));
        }

        this.caseSensitive = section.getBoolean("options.case-sensitive", false);

        this.message = getMessage(section, "options.message", "");

        this.whitelistPermissionToggle = section.getBoolean("options.permission.whitelist-permission.toggle", false);
        this.whitelistCommands = section.isList("options.permission.whitelist-permission.commands") ? section.getStringList("options.permission.whitelist-permission.commands")
                : List.of(section.getString("options.permission.whitelist-permission.commands", ""));
        this.whitelistPermissionMessage = getMessage(section, "options.permission.whitelist-permission.message", Messages.no_permission_to_use_voucher.getString());

        this.whitelistPermissions.addAll(section.getStringList("options.permission.whitelist-permission.permissions").stream().map(String::toLowerCase).toList());

        final String permission = section.getString("options.permission.whitelist-permission.node", "").toLowerCase();

        if (!permission.isEmpty()) {
            this.whitelistPermissions.add(permission);
        }

        this.whitelistWorldsToggle = section.getBoolean("options.whitelist-worlds.toggle", false);
        this.whitelistWorldCommands.addAll(section.isList("options.whitelist-worlds.commands") ? section.getStringList("options.whitelist-worlds.commands")
                : List.of(section.getString("options.whitelist-worlds.commands", "")));
        this.whitelistWorldMessage = getMessage(section, "options.whitelist-worlds.message", Messages.not_in_whitelisted_world.getString());

        this.whitelistWorlds.addAll(section.getStringList("options.whitelist-worlds.worlds").stream().map(String::toLowerCase).toList());

        this.blacklistPermissionsToggle = section.getBoolean("options.permission.blacklist-permission.toggle", false);
        this.blacklistPermissionMessage = getMessage(section, "options.permission.blacklist-permission.message", Messages.has_blacklist_permission.getString());
        this.blacklistPermissions.addAll(section.isList("options.permission.blacklist-permission.permissions") ? section.getStringList("options.permission.blacklist-permission.permissions")
                : List.of(section.getString("options.permission.blacklist-permission.permissions", "")));
        this.blacklistCommands.addAll(section.isList("options.permission.blacklist-permission.commands") ? section.getStringList("options.permission.blacklist-permission.commands")
                : List.of(section.getString("options.permission.blacklist-permission.commands", "")));

        this.limiterToggle = section.getBoolean("options.limiter.toggle", false);
        this.limiterLimit = section.getInt("options.limiter.limit", 0);

        this.soundToggle = section.getBoolean("options.sound.toggle", false);
        this.volume = (float) section.getDouble("options.sound.volume", 1.0);
        this.pitch = (float) section.getDouble("options.sound.pitch", 1.0);

        if (this.soundToggle) {
            for (final String sound : section.getStringList("options.sound.sounds")) {
                this.sounds.add(com.ryderbelserion.fusion.paper.utils.ItemUtils.getSound(sound));
            }
        }

        this.fireworkToggle = section.getBoolean("options.firework.toggle", false);

        if (this.fireworkToggle) {
            for (final String color : section.getString("options.firework.colors", "").split(", ")) {
                this.fireworkColors.add(ColorUtils.getColor(color));
            }
        }

        if (section.contains("chance-commands")) {
            this.fusion.log("warn", "We detected that you have the list version of chance-commands which is no longer used, Please run /crazyvouchers migrate -mt VouchersDeprecated");
        }

        if (section.contains("random-commands")) {
            if (section.isList("random-commands")) {
                this.fusion.log("warn", "We've detected that you have the list version of random-commands which is no longer used, Please run /crazyvouchers migrate -mt VouchersDeprecated");
            } else {
                final ConfigurationSection randomCommands = section.getConfigurationSection("random-commands");

                if (randomCommands != null) {
                    for (final String key : randomCommands.getKeys(false)) {
                        final ConfigurationSection command = randomCommands.getConfigurationSection(key);

                        if (command == null) continue;

                        this.randomCommands.add(new VoucherCommand(command.getStringList("commands"), command.getDouble("weight", 0.0D)));
                    }
                }
            }
        }

        // if the prize weight is greater than 0.0, grab it.
        this.totalWeight = this.randomCommands.stream().filter(filter -> filter.getWeight() > 0.0D).mapToDouble(VoucherCommand::getWeight).sum();
    }

    public void dispatchCommands(@NotNull final Player player, @NotNull final Map<String, String> placeholders) {
        Methods.dispatch(player, this.commands, placeholders, true); // dispatch normal commands

        if (this.randomCommands.isEmpty()) return;

        // dispatch commands without a weight option randomly
        // if the prize weight is less than 0.0, grab it.
        final List<VoucherCommand> randomCommands = this.randomCommands.stream().filter(filter -> filter.getWeight() < 0.0D).toList();

        if (!randomCommands.isEmpty()) {
            final VoucherCommand randomCommand = randomCommands.get(Methods.getRandom(randomCommands.size()));

            Methods.dispatch(player, randomCommand.getCommands(), placeholders, true);
        }

        // dispatch commands while accounting for the weight on each one.
        // if the prize weight is greater than 0.0, grab it.
        final List<VoucherCommand> chanceCommands = this.randomCommands.stream().filter(filter -> filter.getWeight() > 0.0D).toList();

        if (!chanceCommands.isEmpty()) {
            Methods.dispatch(player, getCommand(chanceCommands).getCommands(), placeholders, true);
        }
    }

    public VoucherCommand getCommand(@NotNull final List<VoucherCommand> commands) {
        int index = 0;

        for (double value = Methods.getRandom().nextDouble() * this.totalWeight; index < commands.size() - 1; ++index) {
            value -= commands.get(index).getWeight();

            if (value <= 0.0) break;
        }

        return commands.get(index);
    }

    public boolean hasPermission(@NotNull final Player player, @NotNull final List<String> permissions, @NotNull final List<String> commands, @NotNull final Map<String, String> placeholders, @NotNull final String message, @NotNull final String argument) {
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

    private String getMessage(@NotNull final ConfigurationSection section, @NotNull final String path, @NotNull final String defaultValue) {
        String safeMessage;

        if (section.isList(path)) {
            safeMessage = this.utils.toString(section.getStringList(path));

            return safeMessage;
        }

        safeMessage = section.getString(path, defaultValue);

        return safeMessage;
    }
}