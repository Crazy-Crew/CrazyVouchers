package com.badbones69.crazyvouchers.paper.api.objects;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import com.badbones69.crazyvouchers.paper.api.CrazyManager;
import de.tr7zw.changeme.nbtapi.NBTItem;
import com.badbones69.crazyvouchers.paper.Methods;
import com.badbones69.crazyvouchers.paper.api.FileManager.Files;
import com.badbones69.crazyvouchers.paper.api.enums.Messages;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Voucher {

    private final ItemBuilder itemBuilder;
    
    private final String name;
    private boolean usesArgs;
    private boolean glowing;
    private String usedMessage;
    private boolean whitelistPermissionToggle;
    private final List<String> whitelistPermissions = new ArrayList<>();
    private List<String> whitelistCommands = new ArrayList<>();
    private String whitelistPermissionMessage;
    private boolean whitelistWorldsToggle;
    private String whitelistWorldMessage;
    private final List<String> whitelistWorlds = new ArrayList<>();
    private List<String> whitelistWorldCommands = new ArrayList<>();
    private boolean blacklistPermissionsToggle;
    private String blacklistPermissionMessage;
    private List<String> blacklistCommands = new ArrayList<>();
    private List<String> blacklistPermissions = new ArrayList<>();
    private boolean limiterToggle;
    private int limiterLimit;
    private boolean twoStepAuthentication;
    private boolean soundToggle;
    private final List<Sound> sounds = new ArrayList<>();
    private boolean fireworkToggle;
    private final List<Color> fireworkColors = new ArrayList<>();
    private boolean isEdible;
    private List<String> commands = new ArrayList<>();
    private final List<VoucherCommand> randomCommands = new ArrayList<>();
    private final List<VoucherCommand> chanceCommands = new ArrayList<>();
    private final List<ItemBuilder> items = new ArrayList<ItemBuilder>();
    private final Map<String, String> requiredPlaceholders = new HashMap<>();
    private String requiredPlaceholdersMessage;

    private final CrazyVouchers plugin = CrazyVouchers.getPlugin();
    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final Methods methods = plugin.getMethods();

    /**
     * This is just used for imputing fake vouchers.
     *
     * @param number Fake Voucher Number.
     */
    public Voucher(int number) {
        this.name = number + "";
        this.usesArgs = false;
        this.itemBuilder = new ItemBuilder().setMaterial("Stone").setName(number + "");
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
        this.requiredPlaceholdersMessage = "";
    }
    
    public Voucher(String name) {
        this.name = name;
        this.usesArgs = false;

        FileConfiguration config = Files.CONFIG.getFile();
        String path = "Vouchers." + name;

        this.itemBuilder = new ItemBuilder()
                .setMaterial(config.getString(path + ".Item", "Stone"))
                .setName(config.getString(path + ".Name", ""))
                .setLore(config.getStringList(path + ".Lore"))
                .setPlayerName(config.getString(path + ".Player"))
                .setFlagsFromStrings(config.getStringList(path + ".Flags"));

        if (config.contains(path + ".DisplayDamage")) this.itemBuilder.setDamage(config.getInt(path + ".DisplayDamage"));

        if (config.contains(path + ".DisplayTrim.Material") && config.contains(path + ".DisplayTrim.Pattern")) {
            this.itemBuilder.setTrimMaterial(Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(config.getString(path + ".DisplayTrim.Material", "QUARTZ").toLowerCase())))
                    .setTrimPattern(Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(config.getString(path + ".DisplayTrim.Pattern", "SENTRY").toLowerCase())));
        }

        this.glowing = config.getBoolean(path + ".Glowing");

        if (this.itemBuilder.getName().toLowerCase().contains("%arg%")) this.usesArgs = true;

        if (!this.usesArgs) {
            for (String lore : this.itemBuilder.getLore()) {
                if (lore.toLowerCase().contains("%arg%")) {
                    this.usesArgs = true;
                    break;
                }
            }
        }

        this.commands = config.getStringList(path + ".Commands");

        for (String commands : config.getStringList(path + ".Random-Commands")) {
            this.randomCommands.add(new VoucherCommand(commands));
        }

        for (String line : config.getStringList(path + ".Chance-Commands")) { // - '%chance% %command%, %command%, %command%, ... etc'
            try {
                String[] split = line.split(" ");
                VoucherCommand voucherCommand = new VoucherCommand(line.substring(split[0].length() + 1));

                for (int i = 1; i <= Integer.parseInt(split[0]); i++) {
                    this.chanceCommands.add(voucherCommand);
                }
            } catch (Exception e) {
                this.plugin.getLogger().info("An issue occurred when trying to use chance commands.");
                e.printStackTrace();
            }
        }

        this.items.addAll(this.crazyManager.getItems(config, this.name));

        this.usedMessage = getMessage(path + ".Options.Message");

        if (config.contains(path + ".Options.Permission.Whitelist-Permission")) {
            this.whitelistPermissionToggle = config.getBoolean(path + ".Options.Permission.Whitelist-Permission.Toggle");

            if (config.contains(path + ".Options.Permission.Whitelist-Permission.Node")) this.whitelistPermissions.add("voucher." + config.getString(path + ".Options.Permission.Whitelist-Permission.Node").toLowerCase());

            this.whitelistPermissions.addAll(config.getStringList(path + ".Options.Permission.Whitelist-Permission.Permissions").stream().map(String :: toLowerCase).collect(Collectors.toList()));
            this.whitelistCommands = config.getStringList(path + ".Options.Permission.Whitelist-Permission.Commands");
            this.whitelistPermissionMessage = config.contains(path + ".Options.Permission.Whitelist-Permission.Message") ? getMessage(path + ".Options.Permission.Whitelist-Permission.Message") : Messages.NO_PERMISSION_TO_VOUCHER.getMessageNoPrefix();
        } else {
            this.whitelistPermissionToggle = false;
        }

        if (config.contains(path + ".Options.Whitelist-Worlds.Toggle")) {
            this.whitelistWorlds.addAll(config.getStringList(path + ".Options.Whitelist-Worlds.Worlds").stream().map(String :: toLowerCase).collect(Collectors.toList()));

            if (config.contains(path + ".Options.Whitelist-Worlds.Message")) {
                this.whitelistWorldMessage = getMessage(path + ".Options.Whitelist-Worlds.Message");
            } else {
                this.whitelistWorldMessage = Messages.NOT_IN_WHITELISTED_WORLD.getMessageNoPrefix();
            }

            this.whitelistWorldCommands = config.getStringList(path + ".Options.Whitelist-Worlds.Commands");
            this.whitelistWorldsToggle = !this.whitelistWorlds.isEmpty() && config.getBoolean(path + ".Options.Whitelist-Worlds.Toggle");
        } else {
            this.whitelistWorldsToggle = false;
        }

        if (config.contains(path + ".Options.Permission.Blacklist-Permissions")) {
            this.blacklistPermissionsToggle = config.getBoolean(path + ".Options.Permission.Blacklist-Permissions.Toggle");

            if (config.contains(path + ".Options.Permission.Blacklist-Permissions.Message")) {
                this.blacklistPermissionMessage = getMessage(path + ".Options.Permission.Blacklist-Permissions.Message");
            } else {
                this.blacklistPermissionMessage = Messages.HAS_BLACKLIST_PERMISSION.getMessageNoPrefix();
            }

            this.blacklistPermissions = config.getStringList(path + ".Options.Permission.Blacklist-Permissions.Permissions");
            this.blacklistCommands = config.getStringList(path + ".Options.Permission.Blacklist-Permissions.Commands");
        } else {
            this.blacklistPermissionsToggle = false;
        }

        if (config.contains(path + ".Options.Limiter")) {
            this.limiterToggle = config.getBoolean(path + ".Options.Limiter.Toggle");
            this.limiterLimit = config.getInt(path + ".Options.Limiter.Limit");
        } else {
            this.limiterToggle = false;
        }

        if (config.contains(path + ".Options.Required-Placeholders-Message")) {
            this.requiredPlaceholdersMessage = getMessage(path + ".Options.Required-Placeholders-Message");
        }

        if (config.contains(path + ".Options.Required-Placeholders")) {
            for (String key : config.getConfigurationSection(path + ".Options.Required-Placeholders").getKeys(false)) {
                String placeholder = config.getString(path + ".Options.Required-Placeholders." + key + ".Placeholder");
                String value = config.getString(path + ".Options.Required-Placeholders." + key + ".Value");
                this.requiredPlaceholders.put(placeholder, value);
            }
        }

        this.twoStepAuthentication = config.contains(path + ".Options.Two-Step-Authentication") && config.getBoolean(path + ".Options.Two-Step-Authentication.Toggle");

        if (config.contains(path + ".Options.Sound")) {
            this.soundToggle = config.getBoolean(path + ".Options.Sound.Toggle");

            for (String sound : config.getStringList(path + ".Options.Sound.Sounds")) {
                try {
                    this.sounds.add(Sound.valueOf(sound));
                } catch (Exception ignored) {}
            }
        } else {
            this.soundToggle = false;
        }

        if (config.getBoolean(path + ".Options.Firework.Toggle")) {
            for (String color : config.getString(path + ".Options.Firework.Colors", "").split(", ")) {
                this.fireworkColors.add(methods.getColor(color));
            }

            this.fireworkToggle = !fireworkColors.isEmpty();
        } else {
            this.fireworkToggle = false;
        }

        if (config.getBoolean(path + ".Options.Is-Edible")) {
            this.isEdible = itemBuilder.build().getType().isEdible();

            switch (this.itemBuilder.getMaterial().toString()) {
                case "MILK_BUCKET", "POTION" -> this.isEdible = true;
            }
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean usesArguments() {
        return this.usesArgs;
    }
    
    public ItemStack buildItem() {
        return buildItem(1);
    }
    
    public ItemStack buildItem(int amount) {
        ItemStack item = this.itemBuilder.setAmount(amount).setGlow(this.glowing).build();
        NBTItem nbt = new NBTItem(item);
        nbt.setString("voucher", this.name);

        return nbt.getItem();
    }
    
    public ItemStack buildItem(String argument) {
        return buildItem(argument, 1);
    }
    
    public ItemStack buildItem(String argument, int amount) {
        ItemStack item = this.itemBuilder.setAmount(amount).addLorePlaceholder("%Arg%", argument).addNamePlaceholder("%Arg%", argument).setGlow(this.glowing).build();

        NBTItem nbt = new NBTItem(item);

        nbt.setString("voucher", this.name);
        nbt.setString("argument", argument);

        return nbt.getItem();
    }
    
    public String getVoucherUsedMessage() {
        return this.usedMessage;
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
    
    public String getWhitelistPermissionMessage() {
        return this.whitelistPermissionMessage;
    }
    
    public boolean usesWhitelistWorlds() {
        return this.whitelistWorldsToggle;
    }
    
    public List<String> getWhitelistWorlds() {
        return this.whitelistWorlds;
    }
    
    public String getWhitelistWorldMessage() {
        return this.whitelistWorldMessage;
    }
    
    public List<String> getWhitelistWorldCommands() {
        return this.whitelistWorldCommands;
    }
    
    public boolean useBlackListPermissions() {
        return this.blacklistPermissionsToggle;
    }
    
    public List<String> getBlackListPermissions() {
        return this.blacklistPermissions;
    }
    
    public String getBlackListMessage() {
        return this.blacklistPermissionMessage;
    }
    
    public List<String> getBlacklistCommands() {
        return this.blacklistCommands;
    }
    
    public boolean useLimiter() {
        return this.limiterToggle;
    }
    
    public int getLimiterLimit() {
        return this.limiterLimit;
    }
    
    public boolean useTwoStepAuthentication() {
        return this.twoStepAuthentication;
    }
    
    public boolean playSounds() {
        return this.soundToggle;
    }
    
    public List<Sound> getSounds() {
        return this.sounds;
    }
    
    public boolean useFirework() {
        return this.fireworkToggle;
    }
    
    public List<Color> getFireworkColors() {
        return this.fireworkColors;
    }
    
    public List<String> getCommands() {
        return this.commands;
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

    public Map<String, String> getRequiredPlaceholders() {
        return this.requiredPlaceholders;
    }

    public String getRequiredPlaceholdersMessage() {
        return this.requiredPlaceholdersMessage;
    }

    public boolean isEdible() {
        return this.isEdible;
    }
    
    private String getMessage(String path) {
        FileConfiguration config = Files.CONFIG.getFile();
        String messageString;

        if (isList(path)) {
            messageString = this.methods.color(Messages.convertList(config.getStringList(path)));
        } else {
            messageString = config.getString(path, "");
            if (!messageString.isEmpty()) messageString = this.methods.getPrefix(messageString);
        }

        return messageString;
    }
    
    private boolean isList(String path) {
        return Files.CONFIG.getFile().contains(path) && !Files.CONFIG.getFile().getStringList(path).isEmpty();
    }
}