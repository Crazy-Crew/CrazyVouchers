package me.badbones69.vouchers.api.objects;

import me.badbones69.vouchers.Methods;
import me.badbones69.vouchers.api.FileManager.Files;
import me.badbones69.vouchers.api.enums.Messages;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VoucherCode {
    
    private String name;
    private String code;
    private Boolean enabled;
    private Boolean caseSensitive;
    private Boolean limited;
    private Integer limit;
    private String message;
    private List<String> commands;
    private Boolean whitelistPermissionToggle;
    private List<String> whitelistPermissions = new ArrayList<>();
    private List<String> whitelistCommands = new ArrayList<>();
    private Boolean whitelistWorldsToggle;
    private String whitelistWorldMessage;
    private List<String> whitelistWorlds = new ArrayList<>();
    private List<String> whitelistWorldCommands = new ArrayList<>();
    private Boolean blacklistPermissionsToggle;
    private String blacklistPermissionMessage;
    private List<String> blacklistCommands = new ArrayList<>();
    private List<String> blacklistPermissions = new ArrayList<>();
    private Boolean limiterToggle;
    private Integer limiterLimit;
    private Boolean soundToggle;
    private List<Sound> sounds = new ArrayList<>();
    private Boolean fireworkToggle;
    private List<Color> fireworkColors = new ArrayList<>();
    private List<VoucherCommand> randomCoammnds = new ArrayList<>();
    private List<VoucherCommand> chanceCommands = new ArrayList<>();
    private List<ItemStack> items = new ArrayList<>();
    
    public VoucherCode(String name) {
        this.name = name;
        FileConfiguration config = Files.VOUCHER_CODES.getFile();
        String path = "Voucher-Codes." + name + ".";
        this.enabled = config.getBoolean(path + "Options.Enabled");
        this.code = config.getString(path + "Code", "");
        this.commands = config.getStringList(path + "Commands");
        for (String commands : config.getStringList(path + "Random-Commands")) {
            this.randomCoammnds.add(new VoucherCommand(commands));
        }
        for (String line : config.getStringList(path + "Chance-Commands")) {
            try {
                String[] split = line.split(" ");
                VoucherCommand voucherCommand = new VoucherCommand(line.substring(split[0].length() + 1));
                for (int i = 1; i <= Integer.parseInt(split[0]); i++) {
                    chanceCommands.add(voucherCommand);
                }
            } catch (Exception e) {
                System.out.println("[Vouchers] An issue occurred when trying to use chance commands.");
                e.printStackTrace();
            }
        }
        for (String itemString : config.getStringList(path + "Items")) {
            this.items.add(Methods.makeItem(itemString));
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
                } catch (Exception e) {
                }
            }
        } else {
            this.soundToggle = false;
        }
        if (config.contains(path + "Options.Firework")) {
            this.fireworkToggle = config.getBoolean(path + "Options.Firework.Toggle");
            for (String color : config.getString(path + "Options.Firework.Colors").split(", ")) {
                this.fireworkColors.add(Methods.getColor(color));
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
    
    public Boolean isEnabled() {
        return enabled;
    }
    
    public Boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public Boolean useWhiteListPermissions() {
        return whitelistPermissionToggle;
    }
    
    public List<String> getWhitelistPermissions() {
        return whitelistPermissions;
    }
    
    public List<String> getWhitelistCommands() {
        return whitelistCommands;
    }
    
    public Boolean useWhitelistWorlds() {
        return whitelistWorldsToggle;
    }
    
    public String getWhitelistWorldMessage() {
        return whitelistWorldMessage;
    }
    
    public List<String> getWhitelistWorlds() {
        return whitelistWorlds;
    }
    
    public Boolean useBlacklistPermissions() {
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
    
    public Boolean useLimiter() {
        return limiterToggle;
    }
    
    public Integer getLimit() {
        return limiterLimit;
    }
    
    public Boolean useSounds() {
        return soundToggle;
    }
    
    public List<Sound> getSounds() {
        return sounds;
    }
    
    public Boolean useFireworks() {
        return fireworkToggle;
    }
    
    public List<Color> getFireworkColors() {
        return fireworkColors;
    }
    
    public List<VoucherCommand> getRandomCoammnds() {
        return randomCoammnds;
    }
    
    public List<VoucherCommand> getChanceCommands() {
        return chanceCommands;
    }
    
    public List<ItemStack> getItems() {
        return items;
    }
    
}