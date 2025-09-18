package com.badbones69.crazyvouchers.api.objects;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.utils.ItemUtils;
import com.ryderbelserion.fusion.core.api.utils.StringUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.api.builders.items.ItemBuilder;
import com.ryderbelserion.fusion.paper.utils.ColorUtils;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class Voucher {

    private final CrazyVouchers plugin = CrazyVouchers.get();
    private final FusionPaper fusion = this.plugin.getFusion();

    private final ItemBuilder itemBuilder;

    private final String name;

    private final boolean hasCooldown;
    private final int cooldownInterval;

    private boolean usesArgs;
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

    private final Map<String, VoucherCommand> randomCommands = new HashMap<>();
    private double totalWeight = 0.0D;

    private List<String> commands = new ArrayList<>();

    private final Map<String, String> requiredPlaceholders = new HashMap<>();

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private List<ItemBuilder> items = new ArrayList<>();

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

        this.hasCooldown = false;
        this.cooldownInterval = 0;
    }

    public Voucher(@NotNull final FileConfiguration fileConfiguration, @NotNull final String name) {
        this.usesArgs = false;
        this.name = name;

        final FileSystem system = this.config.getProperty(ConfigKeys.file_system);

        final String path = system == FileSystem.SINGLE ? "vouchers." + name + "." : "voucher.";

        this.hasCooldown = fileConfiguration.getBoolean(path + "cooldown.toggle", false);
        this.cooldownInterval = fileConfiguration.getInt(path + "cooldown.interval", 5);

        String material = fileConfiguration.getString(path + "item", "stone").toLowerCase();
        String model_data = "";

        if (material.contains("#")) {
            final String[] split = material.split("#");

            material = split[0];
            model_data = split[1];
        }

        this.itemBuilder = ItemBuilder.from(material)
                .setDisplayName(fileConfiguration.getString(path + "name", ""))
                .withDisplayLore(fileConfiguration.getStringList(path + "lore"));

        if (!model_data.isEmpty()) {
            this.itemBuilder.setCustomModelData(model_data);
        }

        if (fileConfiguration.contains(path + "custom-model-data")) {
            if (fileConfiguration.isInt(path + "custom-model-data")) {
                this.itemBuilder.setCustomModelData(fileConfiguration.getInt(path + "custom-model-data", -1));
            } else {
                this.itemBuilder.setCustomModelData(fileConfiguration.getString(path + "custom-model-data", ""));
            }
        }

        if (fileConfiguration.contains(path + "player")) {
            final String playerName = fileConfiguration.getString(path + "player", "");

            if (!playerName.isEmpty()) {
                this.itemBuilder.asSkullBuilder().withName(playerName).build();
            }
        }

        if (fileConfiguration.contains(path + "display-damage")) this.itemBuilder.setItemDamage(fileConfiguration.getInt(path + "display-damage"));

        if (fileConfiguration.contains(path + "display-trim.material") && fileConfiguration.contains(path + "display-trim.pattern")) {
            final String trimMaterial = fileConfiguration.getString(path + "display-trim.material", "quartz").toLowerCase();
            final String trimPattern = fileConfiguration.getString(path + "display-trim.pattern", "sentry").toLowerCase();

            this.itemBuilder.setTrim(trimPattern, trimMaterial);
        }

        if (fileConfiguration.contains(path + "skull")) {
            this.itemBuilder.withSkull(fileConfiguration.getString(path + "skull", ""));
        }

        if (fileConfiguration.contains(path + "glowing")) {
            this.itemBuilder.setEnchantGlint(fileConfiguration.getBoolean(path + "glowing", false));
        }

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

        if (fileConfiguration.contains(path + "chance-commands")) {
            this.fusion.log("warn", "We've detected that you have the list version of chance-commands which is no longer used, Please run /crazyvouchers migrate -mt VouchersDeprecated");
        }

        if (fileConfiguration.contains(path + "random-commands")) { // combined random and chance commands
            if (fileConfiguration.isList(path + "random-commands")) {
                this.fusion.log("warn", "We've detected that you have the list version of random-commands which is no longer used, Please run /crazyvouchers migrate -mt VouchersDeprecated");
            } else {
                final ConfigurationSection section = fileConfiguration.getConfigurationSection(path + "random-commands");

                if (section != null) {
                    for (final String key : section.getKeys(false)) {
                        final ConfigurationSection command = section.getConfigurationSection(key);

                        if (command == null) continue;

                        this.randomCommands.putIfAbsent(key, new VoucherCommand(command.getStringList("commands"), command.getDouble("weight", -1)));
                    }
                }

                this.totalWeight = this.randomCommands.values().stream().filter(filter -> filter.getWeight() <= 0.0D).mapToDouble(VoucherCommand::getWeight).sum();
            }
        }

        if (this.config.getProperty(ConfigKeys.use_different_items_layout) && !fileConfiguration.isList("items")) {
            this.items = ItemUtils.convertConfigurationSection(fileConfiguration.getConfigurationSection("items"));
        } else {
            this.items = ItemUtils.convertStringList(fileConfiguration.getStringList(path + "items"));
        }

        this.usedMessage = getMessage(path + "options.message", fileConfiguration);

        if (fileConfiguration.contains(path + "options.permission.whitelist-permission")) {
            this.whitelistPermissionToggle = fileConfiguration.getBoolean(path + "options.permission.whitelist-permission.toggle");

            //todo() check if empty instead
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
            for (final String key : fileConfiguration.getConfigurationSection(path + "options.required-placeholders").getKeys(false)) { //todo() null check instead
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

            for (final String sound : fileConfiguration.getStringList(path + "options.sound.sounds")) {
                this.sounds.add(com.ryderbelserion.fusion.paper.utils.ItemUtils.getSound(sound));
            }
        } else {
            this.soundToggle = false;
        }

        if (fileConfiguration.getBoolean(path + "components.hide-tooltip", false)) {
            this.itemBuilder.hideToolTip();
        }

        if (fileConfiguration.contains(path + "components.hide-tooltip-advanced") && fileConfiguration.isList(path + "components.hide-tooltip-advanced")) {
            this.itemBuilder.hideComponents(fileConfiguration.getStringList(path + "components.hide-tooltip-advanced"));
        }

        if (fileConfiguration.contains(path + "components.item-model.namespace") && fileConfiguration.contains(path + "components.item-model.key")) {
            this.itemBuilder.setItemModel(fileConfiguration.getString(path + "components.item-model.namespace", ""), fileConfiguration.getString(path + "components.item-model.key", ""));
        }

        if (fileConfiguration.getBoolean(path + "options.firework.toggle")) {
            for (String color : fileConfiguration.getString(path + "options.firework.colors", "").split(", ")) {
                this.fireworkColors.add(ColorUtils.getColor(color));
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

    public String getStrippedName() {
        return getName().replace(".yml", "");
    }
    
    public boolean usesArguments() {
        return this.usesArgs;
    }
    
    public ItemStack buildItem() {
        return buildItem(1);
    }

    private @NotNull final SettingsManager config = ConfigManager.getConfig();
    
    public ItemStack buildItem(final int amount) {
        this.itemBuilder.setAmount(amount);

        final ItemStack item = this.itemBuilder.build().asItemStack();

        setUniqueId(item);

        item.editPersistentDataContainer(container -> container.set(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, getName()));

        return item;
    }

    public List<ItemStack> buildItems(@NotNull final String argument, final int amount) {
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
    
    public ItemStack buildItem(@NotNull final String argument, final int amount) {
        final ItemStack item = this.itemBuilder.setAmount(amount).addPlaceholder("{arg}", argument).asItemStack();

        setUniqueId(item);

        item.editPersistentDataContainer(container -> {
            container.set(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, getStrippedName());

            if (!argument.isEmpty()) container.set(PersistentKeys.voucher_arg.getNamespacedKey(), PersistentDataType.STRING, argument);
        });

        return item;
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

    private void setUniqueId(@NotNull final ItemStack item) {
        if (this.config.getProperty(ConfigKeys.dupe_protection)) {
            final String uuid = UUID.randomUUID().toString();

            item.editPersistentDataContainer(container -> container.set(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING, uuid));
        }
    }

    public boolean hasPermission(@NotNull final Player player, @NotNull final List<String> permissions, @NotNull final List<String> commands, @NotNull final Map<String, String> placeholders, @NotNull final String message, @NotNull final String argument) {
        return Methods.hasPermission(true, player, permissions, commands, placeholders, message, argument);
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

    public Map<String, VoucherCommand> getRandomCommands() {
        return this.randomCommands;
    }

    public double getTotalWeight() {
        return this.totalWeight;
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

    public boolean isCooldown(@NotNull final Player player) {
        return this.cooldowns.getOrDefault(player.getUniqueId(), 0L) >= System.currentTimeMillis();
    }

    public void addCooldown(@NotNull final Player player) {
        this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (1000L * getCooldown()));
    }

    public void removeCooldown(@NotNull final Player player) {
        this.cooldowns.remove(player.getUniqueId());
    }

    private String getMessage(@NotNull final String path, @NotNull final FileConfiguration file) {
        String messageString;

        if (isList(path, file)) {
            messageString = StringUtils.toString(file.getStringList(path));
        } else {
            messageString = file.getString(path, "");
        }

        return messageString;
    }
    
    private boolean isList(@NotNull final String path, @NotNull final FileConfiguration file) {
        return file.contains(path) && !file.getStringList(path).isEmpty();
    }
}