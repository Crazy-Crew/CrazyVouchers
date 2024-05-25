package com.badbones69.crazyvouchers.api.objects;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.objects.other.ItemBuilder;
import com.ryderbelserion.vital.paper.util.DyeUtil;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyvouchers.common.config.ConfigManager;
import us.crazycrew.crazyvouchers.common.config.types.ConfigKeys;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class VoucherCode {

    @NotNull
    private final CrazyVouchers plugin = CrazyVouchers.get();
    
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
    private final List<VoucherCommand> randomCommands = new ArrayList<>();
    private final List<VoucherCommand> chanceCommands = new ArrayList<>();
    private final List<ItemBuilder> items = new ArrayList<>();

    public VoucherCode(FileConfiguration file, String name) {
        this.name = name;


        @NotNull ConfigManager configManager = this.plugin.getCrazyHandler().getConfigManager();
        boolean loadOldWay = configManager.getConfig().getProperty(ConfigKeys.mono_file);

        String path = loadOldWay ? "voucher-codes." + name + "." : "voucher.";

        this.enabled = file.getBoolean(path + "options.enabled");
        this.code = file.getString(path + "code", "");
        this.commands = file.getStringList(path + "commands");

        for (String commands : file.getStringList(path + "random-commands")) {
            this.randomCommands.add(new VoucherCommand(commands));
        }

        for (String line : file.getStringList(path + "chance-commands")) {
            try {
                String[] split = line.split(" ");
                VoucherCommand voucherCommand = new VoucherCommand(line.substring(split[0].length() + 1));

                for (int i = 1; i <= Integer.parseInt(split[0]); i++) {
                    chanceCommands.add(voucherCommand);
                }
            } catch (Exception exception) {
                this.plugin.getLogger().log(Level.SEVERE, "An issued occurred when trying to use chance commands.", exception);
            }
        }

        for (String itemString : file.getStringList(path + "items")) {
            this.items.add(ItemBuilder.convertString(itemString));
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
                this.whitelistPermissions.add("voucher." + file.getString(path + "options.permission.whitelist-permission.node").toLowerCase());
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

            for (String sound : file.getStringList(path + "options.sound.sounds")) {
                try {
                    this.sounds.add(Sound.valueOf(sound));
                } catch (Exception ignored) {}
            }
        } else {
            this.soundToggle = false;
        }

        if (file.contains(path + "options.firework")) {
            this.fireworkToggle = file.getBoolean(path + "options.firework.toggle");

            for (String color : file.getString(path + "options.firework.colors").split(", ")) {
                this.fireworkColors.add(DyeUtil.getDefaultColor(color));
            }
        } else {
            this.fireworkToggle = false;
        }
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
    
    public List<VoucherCommand> getRandomCommands() {
        return this.randomCommands;
    }
    
    public List<VoucherCommand> getChanceCommands() {
        return this.chanceCommands;
    }
    
    public List<ItemBuilder> getItems() {
        return this.items;
    }
}