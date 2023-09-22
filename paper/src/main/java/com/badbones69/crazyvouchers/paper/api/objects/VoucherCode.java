package com.badbones69.crazyvouchers.paper.api.objects;

import com.badbones69.crazyvouchers.paper.Methods;
import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import com.badbones69.crazyvouchers.paper.api.FileManager;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VoucherCode {
    
    private final String name;
    private final String code;
    private final boolean enabled;
    private final boolean caseSensitive;
    private boolean limited;
    private int limit;
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
    private final boolean fireworkToggle;
    private final List<Color> fireworkColors = new ArrayList<>();
    private final List<VoucherCommand> randomCommands = new ArrayList<>();
    private final List<VoucherCommand> chanceCommands = new ArrayList<>();
    private final List<ItemBuilder> items = new ArrayList<>();

    private final CrazyVouchers plugin = CrazyVouchers.getPlugin();

    private final Methods methods = plugin.getMethods();
    
    public VoucherCode(String name) {
        this.name = name;
        FileConfiguration config = FileManager.Files.VOUCHER_CODES.getFile();
        String path = "Voucher-Codes." + name + ".";
        this.enabled = config.getBoolean(path + "Options.Enabled");
        this.code = config.getString(path + "Code", "");
        this.commands = config.getStringList(path + "Commands");

        for (String commands : config.getStringList(path + "Random-Commands")) {
            this.randomCommands.add(new VoucherCommand(commands));
        }

        for (String line : config.getStringList(path + "Chance-Commands")) {
            try {
                String[] split = line.split(" ");
                VoucherCommand voucherCommand = new VoucherCommand(line.substring(split[0].length() + 1));

                for (int i = 1; i <= Integer.parseInt(split[0]); i++) {
                    chanceCommands.add(voucherCommand);
                }
            } catch (Exception e) {
                plugin.getLogger().info("An issue occurred when trying to use chance commands.");
                e.printStackTrace();
            }
        }

        for (String itemString : config.getStringList(path + "Items")) {
            this.items.add(ItemBuilder.convertString(itemString));
        }

        this.caseSensitive = config.getBoolean(path + "Options.Case-Sensitive");

        if (config.contains(path + "Options.Message")) {
            this.message = config.getString(path + "Options.Message");
        } else {
            this.message = "";
        }

        if (config.contains(path + "Options.Permission.Whitelist-Permission")) {
            this.whitelistPermissionToggle = config.getBoolean(path + "Options.Permission.Whitelist-Permission.Toggle");

            if (config.contains(path + "Options.Permission.Whitelist-Permission.Node")) {
                whitelistPermissions.add("voucher." + config.getString(path + "Options.Permission.Whitelist-Permission.Node").toLowerCase());
            }
            whitelistPermissions.addAll(config.getStringList(path + "Options.Permission.Whitelist-Permission.Permissions").stream().map(String :: toLowerCase).collect(Collectors.toList()));
            this.whitelistCommands = config.getStringList(path + "Options.Permission.Whitelist-Permission.Commands");
        } else {
            this.whitelistPermissionToggle = false;
        }

        if (config.contains(path + "Options.Whitelist-Worlds.Toggle")) {
            this.whitelistWorlds.addAll(config.getStringList(path + "Options.Whitelist-Worlds.Worlds").stream().map(String :: toLowerCase).collect(Collectors.toList()));

            if (config.contains(path + "Options.Whitelist-Worlds.Message")) {
                this.whitelistWorldMessage = config.getString(path + "Options.Whitelist-Worlds.Message");
            } else {
                this.whitelistWorldMessage = Messages.NOT_IN_WHITELISTED_WORLD.getMessageNoPrefix();
            }

            this.whitelistWorldCommands = config.getStringList(path + "Options.Whitelist-Worlds.Commands");
            this.whitelistWorldsToggle = !this.whitelistWorlds.isEmpty() && config.getBoolean(path + "Options.Whitelist-Worlds.Toggle");
        } else {
            this.whitelistWorldsToggle = false;
        }

        if (config.contains(path + "Options.Permission.Blacklist-Permissions")) {
            this.blacklistPermissionsToggle = config.getBoolean(path + "Options.Permission.Blacklist-Permissions.Toggle");

            if (config.contains(path + "Options.Permission.Blacklist-Permissions.Message")) {
                this.blacklistPermissionMessage = config.getString(path + "Options.Permission.Blacklist-Permissions.Message");
            } else {
                this.blacklistPermissionMessage = Messages.HAS_BLACKLIST_PERMISSION.getMessageNoPrefix();
            }

            this.blacklistPermissions = config.getStringList(path + "Options.Permission.Blacklist-Permissions.Permissions");
            this.blacklistCommands = config.getStringList(path + "Options.Permission.Blacklist-Permissions.Commands");
        } else {
            this.blacklistPermissionsToggle = false;
        }

        if (config.contains(path + "Options.Limiter")) {
            this.limiterToggle = config.getBoolean(path + "Options.Limiter.Toggle");
            this.limiterLimit = config.getInt(path + "Options.Limiter.Limit");
        } else {
            this.limiterToggle = false;
        }

        if (config.contains(path + "Options.Sound")) {
            this.soundToggle = config.getBoolean(path + "Options.Sound.Toggle");

            for (String sound : config.getStringList(path + "Options.Sound.Sounds")) {
                try {
                    this.sounds.add(Sound.valueOf(sound));
                } catch (Exception ignored) {}
            }
        } else {
            this.soundToggle = false;
        }

        if (config.contains(path + "Options.Firework")) {
            this.fireworkToggle = config.getBoolean(path + "Options.Firework.Toggle");

            for (String color : config.getString(path + "Options.Firework.Colors").split(", ")) {
                this.fireworkColors.add(methods.getColor(color));
            }
        } else {
            this.fireworkToggle = false;
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getCode() {
        return code;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public boolean useWhiteListPermissions() {
        return whitelistPermissionToggle;
    }
    
    public List<String> getWhitelistPermissions() {
        return whitelistPermissions;
    }
    
    public List<String> getWhitelistCommands() {
        return whitelistCommands;
    }
    
    public boolean useWhitelistWorlds() {
        return whitelistWorldsToggle;
    }
    
    public String getWhitelistWorldMessage() {
        return whitelistWorldMessage;
    }
    
    public List<String> getWhitelistWorlds() {
        return whitelistWorlds;
    }
    
    public boolean useBlacklistPermissions() {
        return blacklistPermissionsToggle;
    }
    
    public List<String> getWhitelistWorldCommands() {
        return whitelistWorldCommands;
    }
    
    public String getBlacklistMessage() {
        return blacklistPermissionMessage;
    }
    
    public List<String> getBlacklistPermissions() {
        return blacklistPermissions;
    }
    
    public List<String> getBlacklistCommands() {
        return blacklistCommands;
    }
    
    public boolean useLimiter() {
        return limiterToggle;
    }
    
    public int getLimit() {
        return limiterLimit;
    }
    
    public boolean useSounds() {
        return soundToggle;
    }
    
    public List<Sound> getSounds() {
        return sounds;
    }
    
    public boolean useFireworks() {
        return fireworkToggle;
    }
    
    public List<Color> getFireworkColors() {
        return fireworkColors;
    }
    
    public List<VoucherCommand> getRandomCommands() {
        return randomCommands;
    }
    
    public List<VoucherCommand> getChanceCommands() {
        return chanceCommands;
    }
    
    public List<ItemBuilder> getItems() {
        return items;
    }
}