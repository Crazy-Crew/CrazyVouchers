package com.badbones69.crazyvouchers.api.objects;

import com.badbones69.crazyvouchers.CrazyVouchers;
import de.tr7zw.changeme.nbtapi.NBTItem;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.FileManager.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Voucher {
    
    private final String name;
    private Boolean usesArgs;
    private final ItemBuilder itemBuilder;
    private boolean glowing;
    private final String usedMessage;
    private final Boolean whitelistPermissionToggle;
    private final List<String> whitelistPermissions = new ArrayList<>();
    private List<String> whitelistCommands = new ArrayList<>();
    private String whitelistPermissionMessage;
    private final Boolean whitelistWorldsToggle;
    private String whitelistWorldMessage;
    private final List<String> whitelistWorlds = new ArrayList<>();
    private List<String> whitelistWorldCommands = new ArrayList<>();
    private final Boolean blacklistPermissionsToggle;
    private String blacklistPermissionMessage;
    private List<String> blacklistCommands = new ArrayList<>();
    private List<String> blacklistPermissions = new ArrayList<>();
    private final Boolean limiterToggle;
    private Integer limiterLimit;
    private final Boolean twoStepAuthentication;
    private final Boolean soundToggle;
    private final List<Sound> sounds = new ArrayList<>();
    private final Boolean fireworkToggle;
    private final List<Color> fireworkColors = new ArrayList<>();
    private boolean isEdible;
    private List<String> commands = new ArrayList<>();
    private final List<VoucherCommand> randomCommands = new ArrayList<>();
    private final List<VoucherCommand> chanceCommands = new ArrayList<>();
    private final List<ItemBuilder> items = new ArrayList<>();

    /**
     * This is just used for imputing fake vouchers.
     *
     * @param number Fake Voucher Number.
     */
    public Voucher(int number) {
        this.name = number + "";
        this.usesArgs = false;
        itemBuilder = new ItemBuilder().setMaterial("Stone").setName(number + "");
        this.usedMessage = "";
        this.whitelistPermissionToggle = false;
        this.whitelistPermissionMessage = "";
        this.whitelistWorldsToggle = false;
        this.whitelistWorldMessage = "";
        this.blacklistPermissionsToggle = false;
        this.blacklistPermissionMessage = "blacklistPermissionMessage";
        this.limiterToggle = false;
        this.limiterLimit = 0;
        this.twoStepAuthentication = false;
        this.soundToggle = false;
        this.fireworkToggle = false;
        this.isEdible = false;
    }

    private final CrazyVouchers plugin = CrazyVouchers.getPlugin();

    private final Methods methods = plugin.getMethods();
    
    public Voucher(String name) {
        this.name = name;
        this.usesArgs = false;
        FileConfiguration config = Files.CONFIG.getFile();
        String path = "Vouchers." + name + ".";
        itemBuilder = new ItemBuilder()
        .setMaterial(config.getString(path + "Item", "Stone"))
        .setName(config.getString(path + "Name", ""))
        .setLore(config.getStringList(path + "Lore"))
        .setPlayerName(config.getString(path + "Player"))
        .setFlagsFromStrings(config.getStringList(path + "Flags"));
        this.glowing = config.getBoolean(path + "Glowing");

        if (itemBuilder.getName().toLowerCase().contains("%arg%")) this.usesArgs = true;

        if (!usesArgs) {
            for (String lore : itemBuilder.getLore()) {
                if (lore.toLowerCase().contains("%arg%")) {
                    this.usesArgs = true;
                    break;
                }
            }
        }

        this.commands = config.getStringList(path + "Commands");

        for (String commands : config.getStringList(path + "Random-Commands")) {
            this.randomCommands.add(new VoucherCommand(commands));
        }

        for (String line : config.getStringList(path + "Chance-Commands")) { // - '%chance% %command%, %command%, %command%, ... etc'
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

        this.usedMessage = getMessage(path + "Options.Message");

        if (config.contains(path + "Options.Permission.Whitelist-Permission")) {
            this.whitelistPermissionToggle = config.getBoolean(path + "Options.Permission.Whitelist-Permission.Toggle");

            if (config.contains(path + "Options.Permission.Whitelist-Permission.Node")) whitelistPermissions.add("voucher." + config.getString(path + "Options.Permission.Whitelist-Permission.Node").toLowerCase());

            whitelistPermissions.addAll(config.getStringList(path + "Options.Permission.Whitelist-Permission.Permissions").stream().map(String :: toLowerCase).collect(Collectors.toList()));
            this.whitelistCommands = config.getStringList(path + "Options.Permission.Whitelist-Permission.Commands");
            this.whitelistPermissionMessage = config.contains(path + "Options.Permission.Whitelist-Permission.Message") ? getMessage(path + "Options.Permission.Whitelist-Permission.Message") : Messages.NO_PERMISSION_TO_VOUCHER.getMessageNoPrefix();
        } else {
            this.whitelistPermissionToggle = false;
        }

        if (config.contains(path + "Options.Whitelist-Worlds.Toggle")) {
            this.whitelistWorlds.addAll(config.getStringList(path + "Options.Whitelist-Worlds.Worlds").stream().map(String :: toLowerCase).collect(Collectors.toList()));

            if (config.contains(path + "Options.Whitelist-Worlds.Message")) {
                this.whitelistWorldMessage = getMessage(path + "Options.Whitelist-Worlds.Message");
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
                this.blacklistPermissionMessage = getMessage(path + "Options.Permission.Blacklist-Permissions.Message");
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

        this.twoStepAuthentication = config.contains(path + "Options.Two-Step-Authentication") && config.getBoolean(path + "Options.Two-Step-Authentication.Toggle");

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

        if (config.getBoolean(path + "Options.Firework.Toggle")) {
            for (String color : config.getString(path + "Options.Firework.Colors", "").split(", ")) {
                this.fireworkColors.add(methods.getColor(color));
            }

            this.fireworkToggle = !fireworkColors.isEmpty();
        } else {
            this.fireworkToggle = false;
        }

        if (config.getBoolean(path + "Options.Is-Edible")) {
            this.isEdible = itemBuilder.build().getType().isEdible();

            switch (itemBuilder.getMaterial().toString()) {
                case "MILK_BUCKET", "POTION" -> this.isEdible = true;
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public Boolean usesArguments() {
        return usesArgs;
    }
    
    public ItemStack buildItem() {
        return buildItem(1);
    }
    
    public ItemStack buildItem(int amount) {
        ItemStack item = itemBuilder.setAmount(amount).setGlow(glowing).build();
        NBTItem nbt = new NBTItem(item);
        nbt.setString("voucher", name);

        return nbt.getItem();
    }
    
    public ItemStack buildItem(String argument) {
        return buildItem(argument, 1);
    }
    
    public ItemStack buildItem(String argument, int amount) {
        ItemStack item = itemBuilder.setAmount(amount).addLorePlaceholder("%Arg%", argument).addNamePlaceholder("%Arg%", argument).setGlow(glowing).build();

        NBTItem nbt = new NBTItem(item);

        nbt.setString("voucher", name);
        nbt.setString("argument", argument);

        return nbt.getItem();
    }
    
    public String getVoucherUsedMessage() {
        return usedMessage;
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
    
    public String getWhitelistPermissionMessage() {
        return whitelistPermissionMessage;
    }
    
    public Boolean usesWhitelistWorlds() {
        return whitelistWorldsToggle;
    }
    
    public List<String> getWhitelistWorlds() {
        return whitelistWorlds;
    }
    
    public String getWhitelistWorldMessage() {
        return whitelistWorldMessage;
    }
    
    public List<String> getWhitelistWorldCommands() {
        return whitelistWorldCommands;
    }
    
    public Boolean useBlackListPermissions() {
        return blacklistPermissionsToggle;
    }
    
    public List<String> getBlackListPermissions() {
        return blacklistPermissions;
    }
    
    public String getBlackListMessage() {
        return blacklistPermissionMessage;
    }
    
    public List<String> getBlacklistCommands() {
        return blacklistCommands;
    }
    
    public Boolean useLimiter() {
        return limiterToggle;
    }
    
    public Integer getLimiterLimit() {
        return limiterLimit;
    }
    
    public Boolean useTwoStepAuthentication() {
        return twoStepAuthentication;
    }
    
    public Boolean playSounds() {
        return soundToggle;
    }
    
    public List<Sound> getSounds() {
        return sounds;
    }
    
    public Boolean useFirework() {
        return fireworkToggle;
    }
    
    public List<Color> getFireworkColors() {
        return fireworkColors;
    }
    
    public List<String> getCommands() {
        return commands;
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
    
    public boolean isEdible() {
        return isEdible;
    }
    
    private String getMessage(String path) {
        FileConfiguration config = Files.CONFIG.getFile();
        String messageString;

        if (isList(path)) {
            messageString = methods.color(Messages.convertList(config.getStringList(path)));
        } else {
            messageString = config.getString(path, "");
            if (!messageString.isEmpty()) messageString = methods.getPrefix(messageString);
        }

        return messageString;
    }
    
    private boolean isList(String path) {
        return Files.CONFIG.getFile().contains(path) && !Files.CONFIG.getFile().getStringList(path).isEmpty();
    }
}