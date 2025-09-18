package com.badbones69.crazyvouchers.api.objects;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.utils.ItemUtils;
import com.ryderbelserion.fusion.paper.api.builders.items.ItemBuilder;
import com.ryderbelserion.fusion.paper.utils.ColorUtils;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoucherCode {

    private @NotNull final SettingsManager config = ConfigManager.getConfig();

    private final String name;
    private final String code;
    private final boolean enabled;
    private final boolean caseSensitive;
    private final String message;
    private final List<String> commands;
    private final boolean whitelistPermissionToggle;
    private final List<String> whitelistPermissions = new ArrayList<>();
    private List<String> whitelistCommands = new ArrayList<>();
    private final boolean whitelistWorldsToggle;
    private String whitelistWorldMessage;
    private final List<String> whitelistWorlds = new ArrayList<>();
    private List<String> whitelistWorldCommands = new ArrayList<>();
    private final boolean blacklistPermissionsToggle;
    private String blacklistPermissionMessage;
    private List<String> blacklistCommands = new ArrayList<>();
    private List<String> blacklistPermissions = new ArrayList<>();
    private final boolean limiterToggle;
    private Integer limiterLimit;
    private final boolean soundToggle;
    private final List<Sound> sounds = new ArrayList<>();
    private float volume;
    private float pitch;
    private final boolean fireworkToggle;
    private final List<Color> fireworkColors = new ArrayList<>();

    private final Map<String, VoucherCommand> randomCommands = new HashMap<>();
    private double totalWeight = 0.0D;

    private final List<ItemBuilder> items;

    public VoucherCode(@NotNull final FileConfiguration file, @NotNull final String name) {
        this.name = name;

        final FileSystem system = ConfigManager.getConfig().getProperty(ConfigKeys.file_system);

        final String path = system == FileSystem.SINGLE ? "voucher-codes." + name + "." : "voucher-code.";

        this.enabled = file.getBoolean(path + "options.enabled");
        this.code = file.getString(path + "code", "");
        this.commands = file.getStringList(path + "commands");

        if (file.contains(path + "random-commands")) { // combined random and chance commands
            final ConfigurationSection section = file.getConfigurationSection(path + "random-commands");

            if (section != null) {
                for (final String key : section.getKeys(false)) {
                    final ConfigurationSection command = section.getConfigurationSection(key);

                    if (command == null) continue;

                    this.randomCommands.putIfAbsent(key, new VoucherCommand(command.getStringList("commands"), command.getDouble("weight", 0.0D)));
                }
            }

            this.totalWeight = this.randomCommands.values().stream().filter(filter -> filter.getWeight() <= 0.0D).mapToDouble(VoucherCommand::getWeight).sum();
        }

        if (this.config.getProperty(ConfigKeys.use_different_items_layout) && !file.isList("items")) {
            this.items = ItemUtils.convertConfigurationSection(file.getConfigurationSection("items"));
        } else {
            this.items = ItemUtils.convertStringList(file.getStringList(path + "items"));
        }

        this.caseSensitive = file.getBoolean(path + "options.case-sensitive", false);

        if (file.contains(path + "options.message")) {
            this.message = file.getString(path + "options.message");
        } else {
            this.message = "";
        }

        if (file.contains(path + "options.permission.whitelist-permission")) {
            this.whitelistPermissionToggle = file.getBoolean(path + "options.permission.whitelist-permission.toggle");

            if (file.contains(path + "options.permission.whitelist-permission.node")) {
                this.whitelistPermissions.add("voucher." + file.getString(path + "options.permission.whitelist-permission.node", "").toLowerCase());
            }

            this.whitelistPermissions.addAll(file.getStringList(path + "options.permission.whitelist-permission.permissions").stream().map(String::toLowerCase).toList());
            this.whitelistCommands = file.getStringList(path + "options.permission.whitelist-permission.commands");
        } else {
            this.whitelistPermissionToggle = false;
        }

        if (file.contains(path + "options.whitelist-worlds.toggle")) {
            this.whitelistWorlds.addAll(file.getStringList(path + "options.whitelist-worlds.worlds").stream().map(String::toLowerCase).toList());

            if (file.contains(path + "options.whitelist-worlds.message")) {
                this.whitelistWorldMessage = file.getString(path + "options.whitelist-worlds.message");
            } else {
                this.whitelistWorldMessage = Messages.not_in_whitelisted_world.getString();
            }

            this.whitelistWorldCommands = file.getStringList(path + "options.whitelist-worlds.commands");
            this.whitelistWorldsToggle = !this.whitelistWorlds.isEmpty() && file.getBoolean(path + "options.whitelist-worlds.toggle");
        } else {
            this.whitelistWorldsToggle = false;
        }

        if (file.contains(path + "options.permission.blacklist-permissions")) {
            this.blacklistPermissionsToggle = file.getBoolean(path + "options.permission.blacklist-permissions.toggle");

            if (file.contains(path + "options.permission.blacklist-permissions.message")) {
                this.blacklistPermissionMessage = file.getString(path + "options.permission.blacklist-permissions.message");
            } else {
                this.blacklistPermissionMessage = Messages.has_blacklist_permission.getString();
            }

            this.blacklistPermissions = file.getStringList(path + "options.permission.blacklist-permissions.permissions");
            this.blacklistCommands = file.getStringList(path + "options.permission.blacklist-permissions.commands");
        } else {
            this.blacklistPermissionsToggle = false;
        }

        if (file.contains(path + "options.limiter")) {
            this.limiterToggle = file.getBoolean(path + "options.limiter.toggle");
            this.limiterLimit = file.getInt(path + "options.limiter.limit");
        } else {
            this.limiterToggle = false;
        }

        if (file.contains(path + "options.sound")) {
            this.soundToggle = file.getBoolean(path + "options.sound.toggle");

            this.volume = (float) file.getDouble(path + ".options.sound.volume");
            this.pitch = (float) file.getDouble(path + ".options.sound.pitch");

            for (final String sound : file.getStringList(path + "options.sound.sounds")) {
                this.sounds.add(com.ryderbelserion.fusion.paper.utils.ItemUtils.getSound(sound));
            }
        } else {
            this.soundToggle = false;
        }

        if (file.contains(path + "options.firework")) {
            this.fireworkToggle = file.getBoolean(path + "options.firework.toggle");

            for (String color : file.getString(path + "options.firework.colors", "").split(", ")) {
                this.fireworkColors.add(ColorUtils.getColor(color));
            }
        } else {
            this.fireworkToggle = false;
        }
    }

    public void dispatchCommands(@NotNull final Player player, @NotNull final Map<String, String> placeholders) {
        Methods.dispatch(player, this.commands, placeholders, true); // dispatch normal commands

        // dispatch commands without a weight option randomly
        final List<VoucherCommand> randomCommands = this.randomCommands.values().stream().filter(filter -> filter.getWeight() > 0.0D).toList();
        final VoucherCommand randomCommand = randomCommands.get(Methods.getRandom(randomCommands.size()));

        Methods.dispatch(player, randomCommand.getCommands(), placeholders, true);

        // dispatch commands while accounting for the weight on each one.
        // if a section has Weight, and a list of commands. all those commands will execute if the Weight is picked.
        final List<VoucherCommand> chanceCommands = this.randomCommands.values().stream().filter(filter -> filter.getWeight() <= 0.0D).toList();

        Methods.dispatch(player, getCommand(chanceCommands).getCommands(), placeholders, true);
    }

    public VoucherCommand getCommand(@NotNull final List<VoucherCommand> commands) {
        int index = 0;

        for (double value = Methods.getRandom().nextDouble() * this.totalWeight; index < commands.size() - 1; index++) {
            value -= commands.get(index).getWeight();

            if (value <= 0.0) break;
        }

        return commands.get(index);
    }

    public boolean hasPermission(@NotNull final Player player, @NotNull final List<String> permissions, @NotNull final List<String> commands, @NotNull final Map<String, String> placeholders, @NotNull final String message, @NotNull final String argument) {
        return Methods.hasPermission(false, player, permissions, commands, placeholders, message, argument);
    }

    public String getStrippedName() {
        return this.name.replaceAll(".yml", "");
    }
    
    public String getName() {
        return this.name;
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
    
    public Map<String, VoucherCommand> getRandomCommands() {
        return this.randomCommands;
    }

    public double getTotalWeight() {
        return this.totalWeight;
    }

    public List<ItemBuilder> getItems() {
        return this.items;
    }
}