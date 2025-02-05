package com.badbones69.crazyvouchers.api.objects;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import com.ryderbelserion.core.util.StringUtils;
import com.ryderbelserion.paper.builder.items.modern.ItemBuilder;
import com.ryderbelserion.paper.util.PaperMethods;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Voucher {

    private final ItemBuilder itemBuilder;

    private final String name;

    private final boolean hasCooldown;
    private final int cooldownInterval;

    private boolean usesArgs;
    private final boolean glowing;
    private final String usedMessage;
    private final boolean whitelistPermissionToggle;
    private final List<String> whitelistPermissions = new ArrayList<>();
    private List<String> whitelistCommands = new ArrayList<>();
    private String whitelistPermissionMessage;
    private final boolean whitelistWorldsToggle;
    private String whitelistWorldMessage;
    private final List<String> whitelistWorlds = new ArrayList<>();
    private List<String> whitelistWorldCommands = new ArrayList<>();
    private final boolean blacklistPermissionsToggle;
    private String blacklistPermissionMessage;
    private List<String> blacklistCommands = new ArrayList<>();
    private List<String> blacklistPermissions = new ArrayList<>();
    private final boolean limiterToggle;
    private int limiterLimit;
    private final boolean twoStepAuthentication;
    private final boolean soundToggle;
    private float volume;
    private float pitch;
    private final List<Sound> sounds = new ArrayList<>();
    private final boolean fireworkToggle;
    private final List<Color> fireworkColors = new ArrayList<>();
    private boolean isEdible;
    private List<String> commands = new ArrayList<>();
    private final List<VoucherCommand> randomCommands = new ArrayList<>();
    private final List<VoucherCommand> chanceCommands = new ArrayList<>();
    private final List<ItemBuilder> items = new ArrayList<>();
    private final Map<String, String> requiredPlaceholders = new HashMap<>();

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private String requiredPlaceholdersMessage;

    public Voucher(int number) {
        this.name = number + "";
        this.usesArgs = false;
        this.itemBuilder = ItemBuilder.from(ItemType.STONE).setDisplayName(number + "");
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
        this.glowing = false;

        this.hasCooldown = false;
        this.cooldownInterval = 0;
    }

    public Voucher(FileConfiguration fileConfiguration, String name) {
        this.name = name;
        this.usesArgs = false;

        final CrazyVouchers plugin = CrazyVouchers.get();
        final boolean loadOldWay = ConfigManager.getConfig().getProperty(ConfigKeys.mono_file);

        String path = loadOldWay ? "vouchers." + name + "." : "voucher.";

        this.hasCooldown = fileConfiguration.getBoolean(path + "cooldown.toggle", false);
        this.cooldownInterval = fileConfiguration.getInt(path + "cooldown.interval", 5);

        this.itemBuilder = ItemBuilder.from(fileConfiguration.getString(path + "item", "Stone").toLowerCase())
                .setDisplayName(fileConfiguration.getString(path + "name", ""))
                .withDisplayLore(fileConfiguration.getStringList(path + "lore"));

        if (fileConfiguration.contains(path + "player")) {
            final String playerName = fileConfiguration.getString(path + "player", "");

            if (!playerName.isEmpty() || !playerName.isBlank()) {
                this.itemBuilder.asSkullBuilder().withName(playerName).build();
            }
        }

        if (fileConfiguration.contains(path + "display-damage")) this.itemBuilder.setItemDamage(fileConfiguration.getInt(path + "display-damage"));

        if (fileConfiguration.contains(path + "display-trim.material") && fileConfiguration.contains(path + "display-trim.pattern")) {
            final String trimMaterial = fileConfiguration.getString(path + "display-trim.material", "QUARTZ").toLowerCase();
            final String trimPattern = fileConfiguration.getString(path + "display-trim.pattern", "SENTRY").toLowerCase();

            this.itemBuilder.setTrim(trimPattern, trimMaterial, false);
        }

        if (fileConfiguration.contains(path + "skull")) {
            this.itemBuilder.withSkull(fileConfiguration.getString(path + "skull", ""));
        }

        this.glowing = fileConfiguration.getBoolean(path + "glowing");

        if (this.itemBuilder.getPlainName().toLowerCase().contains("{arg}")) this.usesArgs = true;

        if (!this.usesArgs) {
            for (String lore : this.itemBuilder.getPlainLore()) {
                if (lore.toLowerCase().contains("{arg}")) {
                    this.usesArgs = true;

                    break;
                }
            }
        }

        if (fileConfiguration.contains(path + "commands")) this.commands = fileConfiguration.getStringList(path + "commands");

        if (fileConfiguration.contains(path + "random-commands")) {
            for (String commands : fileConfiguration.getStringList(path + "random-commands")) {
                this.randomCommands.add(new VoucherCommand(commands));
            }
        }

        if (fileConfiguration.contains(path + "chance-commands")) {
            for (String line : fileConfiguration.getStringList(path + "chance-commands")) { // - '{chance} {command}, {command}, {command}, ... etc'
                try {
                    String[] split = line.split(" ");
                    VoucherCommand voucherCommand = new VoucherCommand(line.substring(split[0].length() + 1));

                    for (int i = 1; i <= Integer.parseInt(split[0]); i++) {
                        this.chanceCommands.add(voucherCommand);
                    }
                } catch (Exception exception) {
                    plugin.getLogger().log(Level.SEVERE,"An issue occurred when trying to use chance commands.", exception);
                }
            }
        }

        CrazyManager crazyManager = plugin.getCrazyManager();

        this.items.addAll(crazyManager.getItems(fileConfiguration, this.name));

        this.usedMessage = getMessage(path + "options.message", fileConfiguration);

        if (fileConfiguration.contains(path + "options.permission.whitelist-permission")) {
            this.whitelistPermissionToggle = fileConfiguration.getBoolean(path + "options.permission.whitelist-permission.toggle");

            if (fileConfiguration.contains(path + "options.permission.whitelist-permission.node")) this.whitelistPermissions.add(fileConfiguration.getString(path + "options.permission.whitelist-permission.node").toLowerCase());

            this.whitelistPermissions.addAll(fileConfiguration.getStringList(path + "options.permission.whitelist-permission.permissions").stream().map(String::toLowerCase).toList());
            this.whitelistCommands = fileConfiguration.getStringList(path + "options.permission.whitelist-permission.commands");
            this.whitelistPermissionMessage = fileConfiguration.contains(path + "options.permission.whitelist-permission.message") ? getMessage(path + "options.permission.whitelist-permission.message", fileConfiguration) : Messages.no_permission_to_use_voucher.getString();
        } else {
            this.whitelistPermissionToggle = false;
        }

        if (fileConfiguration.contains(path + "options.whitelist-worlds.toggle")) {
            this.whitelistWorlds.addAll(fileConfiguration.getStringList(path + "options.whitelist-worlds.worlds").stream().map(String::toLowerCase).toList());

            if (fileConfiguration.contains(path + "options.whitelist-worlds.message")) {
                this.whitelistWorldMessage = getMessage(path + "options.whitelist-worlds.message", fileConfiguration);
            } else {
                this.whitelistWorldMessage = Messages.not_in_whitelisted_world.getString();
            }

            this.whitelistWorldCommands = fileConfiguration.getStringList(path + "options.whitelist-worlds.commands");
            this.whitelistWorldsToggle = !this.whitelistWorlds.isEmpty() && fileConfiguration.getBoolean(path + "options.whitelist-worlds.toggle");
        } else {
            this.whitelistWorldsToggle = false;
        }

        if (fileConfiguration.contains(path + "options.permission.blacklist-permission")) {
            this.blacklistPermissionsToggle = fileConfiguration.getBoolean(path + "options.permission.blacklist-permission.toggle");

            if (fileConfiguration.contains(path + "options.permission.blacklist-permission.message")) {
                this.blacklistPermissionMessage = getMessage(path + "options.permission.blacklist-permission.message", fileConfiguration);
            } else {
                this.blacklistPermissionMessage = Messages.has_blacklist_permission.getString();
            }

            this.blacklistPermissions = fileConfiguration.getStringList(path + "options.permission.blacklist-permission.permissions");
            this.blacklistCommands = fileConfiguration.getStringList(path + "options.permission.blacklist-permission.commands");
        } else {
            this.blacklistPermissionsToggle = false;
        }

        if (fileConfiguration.contains(path + "options.limiter")) {
            this.limiterToggle = fileConfiguration.getBoolean(path + "options.limiter.toggle");
            this.limiterLimit = fileConfiguration.getInt(path + "options.limiter.limit");
        } else {
            this.limiterToggle = false;
        }

        if (fileConfiguration.contains(path + "options.required-placeholders-message")) {
            this.requiredPlaceholdersMessage = getMessage(path + "options.required-placeholders-message", fileConfiguration);
        }

        if (fileConfiguration.contains(path + "options.required-placeholders")) {
            for (String key : fileConfiguration.getConfigurationSection(path + "options.required-placeholders").getKeys(false)) {
                String placeholder = fileConfiguration.getString(path + "options.required-placeholders." + key + ".placeholder");
                String value = fileConfiguration.getString(path + "options.required-placeholders." + key + ".value");
                this.requiredPlaceholders.put(placeholder, value);
            }
        }

        this.twoStepAuthentication = fileConfiguration.contains(path + "options.two-step-authentication") && fileConfiguration.getBoolean(path + "options.two-step-authentication");

        if (fileConfiguration.contains(path + "options.sound")) {
            this.soundToggle = fileConfiguration.getBoolean(path + "options.sound.toggle");

            this.volume = (float) fileConfiguration.getDouble(path + "options.sound.volume");
            this.pitch = (float) fileConfiguration.getDouble(path + "options.sound.pitch");

            for (String sound : fileConfiguration.getStringList(path + "options.sound.sounds")) {
                try {
                    this.sounds.add(Sound.valueOf(sound)); //todo() add support for resource packs
                } catch (Exception ignored) {}
            }
        } else {
            this.soundToggle = false;
        }

        if (fileConfiguration.getBoolean(path + "components.hide-tooltip", false)) {
            this.itemBuilder.hideToolTip();
        }

        if (fileConfiguration.getBoolean(path + "options.firework.toggle")) {
            for (String color : fileConfiguration.getString(path + "options.firework.colors", "").split(", ")) {
                this.fireworkColors.add(PaperMethods.getColor(color));
            }

            this.fireworkToggle = !fireworkColors.isEmpty();
        } else {
            this.fireworkToggle = false;
        }

        if (fileConfiguration.getBoolean(path + "options.is-edible")) {
            this.isEdible = this.itemBuilder.isEdible();
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

    private final SettingsManager config = ConfigManager.getConfig();
    
    public ItemStack buildItem(int amount) {
        final ItemStack item = this.itemBuilder.setAmount(amount).setEnchantGlint(this.glowing).asItemStack(true);

        setUniqueId(item);

        item.editMeta(itemMeta -> {
            final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            container.set(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, getName());
        });

        return item;
    }

    public List<ItemStack> buildItems(String argument, int amount) {
        final List<ItemStack> itemStacks = new ArrayList<>();

        if (this.config.getProperty(ConfigKeys.dupe_protection)) {
            while (itemStacks.size() < amount) {
                itemStacks.add(buildItem(argument, 1));
            }
        } else {
            itemStacks.add(buildItem(argument, amount));
        }

        return itemStacks;
    }
    
    public ItemStack buildItem(final String argument, final int amount) {
        final ItemStack item = this.itemBuilder.setAmount(amount).addPlaceholder("{arg}", argument).setEnchantGlint(this.glowing).asItemStack(true);

        setUniqueId(item);

        item.editMeta(itemMeta -> {
            final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            container.set(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, getName());

            if (!argument.isEmpty()) container.set(PersistentKeys.voucher_arg.getNamespacedKey(), PersistentDataType.STRING, argument);
        });

        return item;
    }

    private void setUniqueId(final ItemStack item) {
        final String uuid = UUID.randomUUID().toString();

        if (this.config.getProperty(ConfigKeys.dupe_protection)) {
            item.editMeta(itemMeta -> {
                final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                if (this.config.getProperty(ConfigKeys.dupe_protection)) {
                    container.set(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING, uuid);
                }
            });
        }
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

    public float getPitch() {
        return this.pitch;
    }

    public float getVolume() {
        return this.volume;
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

    public boolean hasCooldown() {
        return this.hasCooldown;
    }

    public int getCooldown() {
        return this.cooldownInterval;
    }

    public boolean isCooldown(final Player player) {
        return this.cooldowns.getOrDefault(player.getUniqueId(), 0L) >= System.currentTimeMillis();
    }

    public void addCooldown(final Player player) {
        this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (1000L * getCooldown()));
    }

    public void removeCooldown(final Player player) {
        this.cooldowns.remove(player.getUniqueId());
    }

    private String getMessage(String path, FileConfiguration file) {
        String messageString;

        if (isList(path, file)) {
            messageString = MsgUtils.color(StringUtils.toString(file.getStringList(path)));
        } else {
            messageString = file.getString(path, "");
        }

        return messageString;
    }
    
    private boolean isList(String path, FileConfiguration file) {
        return file.contains(path) && !file.getStringList(path).isEmpty();
    }
}