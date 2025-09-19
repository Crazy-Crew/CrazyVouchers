package com.badbones69.crazyvouchers.api.objects;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.Methods;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

    private int cooldownInterval = 5;
    private boolean hasCooldown = false;

    private boolean usesArgs = false;
    private String usedMessage = "";

    private boolean whitelistPermissionToggle = false;
    private final List<String> whitelistPermissions = new ArrayList<>();
    private List<String> whitelistCommands = new ArrayList<>();
    private final String whitelistPermissionMessage;

    private boolean whitelistWorldsToggle = false;
    private final String whitelistWorldMessage;
    private final List<String> whitelistWorlds = new ArrayList<>();
    private List<String> whitelistWorldCommands = new ArrayList<>();

    private boolean blacklistPermissionsToggle = false;
    private final String blacklistPermissionMessage;
    private List<String> blacklistCommands = new ArrayList<>();
    private List<String> blacklistPermissions = new ArrayList<>();

    private boolean limiterToggle = false;
    private int limiterLimit = 0;

    private boolean twoStepAuthentication = false;

    private boolean soundToggle = false;
    private float volume = 1.0F;
    private float pitch = 1.0F;
    private final List<Sound> sounds = new ArrayList<>();

    private boolean fireworkToggle = false;
    private final List<Color> fireworkColors = new ArrayList<>();
    private boolean isEdible = false;

    private final List<VoucherCommand> randomCommands = new ArrayList<>();

    private final Map<String, String> requiredPlaceholders = new HashMap<>();

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private final List<ItemBuilder> items;

    private final String requiredPlaceholdersMessage;

    private final List<String> commands;

    private final double totalWeight;

    public Voucher(@NotNull final ConfigurationSection section, @NotNull final String name) {
        this.name = name;

        this.hasCooldown = section.getBoolean("cooldown.toggle", false);
        this.cooldownInterval = section.getInt("cooldown.interval", 5);

        String material = section.getString("item", "stone").toLowerCase();
        String model_data = "";

        if (material.contains("#")) {
            final String[] split = material.split("#");

            material = split[0];
            model_data = split[1];
        }

        this.itemBuilder = ItemBuilder.from(material)
                .setDisplayName(section.getString("name", ""))
                .withDisplayLore(section.isList("lore") ? section.getStringList("lore") : List.of(section.getString("lore", "")));

        if (!model_data.isEmpty()) {
            this.itemBuilder.setCustomModelData(model_data);
        }

        if (section.contains("custom-model-data")) {
            this.itemBuilder.setCustomModelData(section.getString("custom-model-data", "-1"));
        }

        final String playerName = section.getString("player", "");

        if (!playerName.isEmpty()) {
            this.itemBuilder.asSkullBuilder().withName(playerName).build();
        }

        this.itemBuilder.setItemDamage(section.getInt("display-damage", -1));

        final String trimMaterial = section.getString("display-trim.material", "").toLowerCase();
        final String trimPattern = section.getString("display-trim.pattern", "").toLowerCase();

        if (!trimMaterial.isEmpty() && !trimPattern.isEmpty()) {
            this.itemBuilder.setTrim(trimPattern, trimMaterial);
        }

        this.itemBuilder.withSkull(section.getString("skull", ""));

        if (section.contains("glowing")) {
            this.itemBuilder.setEnchantGlint(section.getBoolean("glowing", false));
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

        this.commands = section.isList("commands") ? section.getStringList("commands") : List.of(section.getString("commands", ""));

        if (this.config.getProperty(ConfigKeys.use_different_items_layout) && !section.isList("items")) {
            this.items = ItemUtils.convertConfigurationSection(section.getConfigurationSection("items"));
        } else {
            this.items = ItemUtils.convertStringList(section.getStringList("items"));
        }

        this.usedMessage = getMessage(section, "options.message", "");

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
        this.whitelistWorldCommands = section.isList("options.whitelist-worlds.commands") ? section.getStringList("options.whitelist-worlds.commands")
                : List.of(section.getString("options.whitelist-worlds.commands", ""));
        this.whitelistWorldMessage = getMessage(section, "options.whitelist-worlds.message", Messages.not_in_whitelisted_world.getString());

        this.whitelistWorlds.addAll(section.getStringList("options.whitelist-worlds.worlds").stream().map(String::toLowerCase).toList());

        this.blacklistPermissionsToggle = section.getBoolean("options.permission.blacklist-permission.toggle", false);
        this.blacklistPermissionMessage = getMessage(section, "options.permission.blacklist-permission.message", Messages.has_blacklist_permission.getString());
        this.blacklistPermissions = section.isList("options.permission.blacklist-permission.permissions") ? section.getStringList("options.permission.blacklist-permission.permissions")
                : List.of(section.getString("options.permission.blacklist-permission.permissions", ""));
        this.blacklistCommands = section.isList("options.permission.blacklist-permission.commands") ? section.getStringList("options.permission.blacklist-permission.commands")
                : List.of(section.getString("options.permission.blacklist-permission.commands", ""));

        this.limiterToggle = section.getBoolean("options.limiter.toggle", false);
        this.limiterLimit = section.getInt("options.limiter.limit", 0);

        this.requiredPlaceholdersMessage = getMessage(section, "options.required-placeholders-message", "");

        final ConfigurationSection requiredSection = section.getConfigurationSection("options.required-placeholders");

        if (requiredSection != null) {
            for (final String key : requiredSection.getKeys(false)) {
                final ConfigurationSection internalSection = requiredSection.getConfigurationSection(key);

                if (internalSection == null) continue;

                final String placeholder = internalSection.getString("placeholder", "");
                final String value = internalSection.getString("value", "");

                if (placeholder.isEmpty() || value.isEmpty()) continue;

                this.requiredPlaceholders.put(placeholder, value);
            }
        }

        this.twoStepAuthentication = section.getBoolean("options.two-step-authentication", false);

        this.soundToggle = section.getBoolean("options.sound.toggle", false);
        this.volume = (float) section.getDouble("options.sound.volume", 1.0);
        this.pitch = (float) section.getDouble("options.sound.pitch", 1.0);

        if (this.soundToggle) {
            for (final String sound : section.getStringList("options.sound.sounds")) {
                this.sounds.add(com.ryderbelserion.fusion.paper.utils.ItemUtils.getSound(sound));
            }
        }

        if (section.getBoolean("components.hide-tooltip", false)) {
            this.itemBuilder.hideToolTip();
        }

        if (section.isList("components.hide-tooltip-advanced")) {
            this.itemBuilder.hideComponents(section.getStringList("components.hide-tooltip-advanced"));
        }

        this.itemBuilder.setItemModel(section.getString("components.item-model.namespace", ""), section.getString("components.item-model.key", ""));

        this.fireworkToggle = section.getBoolean("options.firework.toggle", false);

        if (this.fireworkToggle) {
            for (final String color : section.getString("options.firework.colors", "").split(", ")) {
                this.fireworkColors.add(ColorUtils.getColor(color));
            }
        }

        this.isEdible = section.getBoolean("options.is-edible", false) && this.itemBuilder.isEdible();

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

        this.totalWeight = this.randomCommands.stream().filter(filter -> filter.getWeight() >= 0.0D).mapToDouble(VoucherCommand::getWeight).sum();
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
        final List<VoucherCommand> randomCommands = this.randomCommands.stream().filter(filter -> filter.getWeight() > 0.0D).toList();

        final VoucherCommand randomCommand = randomCommands.get(Methods.getRandom(randomCommands.size()));

        Methods.dispatch(player, randomCommand.getCommands(), placeholders, true);

        // dispatch commands while accounting for the weight on each one.
        // if a section has Weight, and a list of commands. all those commands will execute if the Weight is picked.
        final List<VoucherCommand> chanceCommands = this.randomCommands.stream().filter(filter -> filter.getWeight() <= 0.0D).toList();

        Methods.dispatch(player, getCommand(chanceCommands).getCommands(), placeholders, true);
    }

    public VoucherCommand getCommand(@NotNull final List<VoucherCommand> commands) {
        int index = 0;

        for (double value = Methods.getRandom().nextDouble() * this.totalWeight; index < commands.size() - 1; ++index) {
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

    public List<VoucherCommand> getRandomCommands() {
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

    private String getMessage(@NotNull final ConfigurationSection section, @NotNull final String path, @NotNull final String defaultValue) {
        String safeMessage;
        
        if (section.isList(path)) {
            safeMessage = StringUtils.toString(section.getStringList(path));
        
            return safeMessage;
        }
        
        safeMessage = section.getString(path, defaultValue);
        
        return safeMessage;
    }
}