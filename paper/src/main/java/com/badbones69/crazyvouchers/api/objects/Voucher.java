package com.badbones69.crazyvouchers.api.objects;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.api.enums.misc.PermissionKeys;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemEvent;
import com.badbones69.crazyvouchers.utils.ItemUtils;
import com.ryderbelserion.fusion.core.api.support.ModSupport;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.builders.ItemBuilder;
import com.ryderbelserion.fusion.paper.builders.types.custom.CustomBuilder;
import com.ryderbelserion.fusion.paper.utils.ColorUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.persistence.PersistentDataContainerView;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Voucher {

    private final CrazyVouchers plugin = CrazyVouchers.get();
    private final Server server = this.plugin.getServer();
    private final FusionPaper fusion = this.plugin.getFusion();
    private final StringUtils utils = this.fusion.getStringUtils();

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final ItemBuilder itemBuilder;

    private final String cleanName;
    private final String name;

    private final int cooldownInterval;
    private final boolean hasCooldown;

    private final boolean hasArgument;
    private final String usedMessage;

    private final boolean whitelistPermissionToggle;
    private final List<String> whitelistPermissions = new ArrayList<>();
    private List<String> whitelistCommands = new ArrayList<>();
    private final String whitelistPermissionMessage;

    private final boolean whitelistWorldsToggle;
    private final String whitelistWorldMessage;
    private final List<String> whitelistWorlds = new ArrayList<>();
    private List<String> whitelistWorldCommands = new ArrayList<>();

    private final boolean blacklistPermissionsToggle;
    private final String blacklistPermissionMessage;
    private List<String> blacklistCommands = new ArrayList<>();
    private List<String> blacklistPermissions = new ArrayList<>();

    private final boolean isAntiDupeOverridden;

    private final boolean isItemFramePlacementToggled;

    private final boolean limiterToggle;
    private final int limiterLimit;

    private final boolean twoStepAuthentication;

    private final boolean soundToggle;
    private final float volume;
    private final float pitch;
    private final List<Sound> sounds = new ArrayList<>();

    private final boolean fireworkToggle;
    private final List<Color> fireworkColors = new ArrayList<>();
    private final boolean isEdible;

    private final List<VoucherCommand> randomCommands = new ArrayList<>();

    private final Map<String, String> requiredPlaceholders = new HashMap<>();

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private final List<ItemBuilder> items;

    private final String requiredPlaceholdersMessage;

    private final List<String> commands;

    private final double totalWeight;

    public Voucher(@NotNull final ConfigurationSection section, @NotNull final String name) {
        this.cleanName = name.replace(".yml", "");
        this.name = name;

        this.isAntiDupeOverridden = section.getBoolean("override-anti-dupe", false);

        this.isItemFramePlacementToggled = section.getBoolean("allow-vouchers-in-item-frames", false);

        this.hasCooldown = section.getBoolean("cooldown.toggle", false);
        this.cooldownInterval = section.getInt("cooldown.interval", 5);

        String material = section.getString("item", "stone").toLowerCase();
        String model_data = "";

        if (material.contains("#")) {
            final String[] split = material.split("#");

            material = split[0];
            model_data = split[1];
        }

        final String displayName = section.getString("name", "");

        final List<String> displayLore = section.isList("lore") ? section.getStringList("lore") : List.of(section.getString("lore", ""));

        this.itemBuilder = new ItemBuilder(material)
                .withDisplayName(displayName)
                .withDisplayLore(displayLore);

        final CustomBuilder customBuilder = this.itemBuilder.asCustomBuilder();

        if (!model_data.isEmpty()) {
            customBuilder.setCustomModelData(model_data);
        }

        if (section.contains("custom-model-data")) {
            customBuilder.setCustomModelData(section.getString("custom-model-data", "-1"));
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
            this.itemBuilder.addEnchantGlint(section.getBoolean("glowing", false));
        }

        this.commands = section.isList("commands") ? section.getStringList("commands") : List.of(section.getString("commands", ""));

        this.hasArgument = section.getBoolean("has-argument", false);

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

        customBuilder.setItemModel(section.getString("components.item-model.namespace", ""), section.getString("components.item-model.key", ""));

        customBuilder.build();

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

                        this.randomCommands.add(new VoucherCommand(command.getStringList("commands"), command.getDouble("weight", -1)));
                    }
                }
            }
        }

        // if a prize has a weight greater than 0.0, include it with the total weight.
        this.totalWeight = this.randomCommands.stream().filter(filter -> filter.getWeight() != -1).mapToDouble(VoucherCommand::getWeight).sum();
    }

    public @NotNull String getArgument(@NotNull final ItemStack item) {
        if (item.getType() == Material.AIR || !hasArgument()) return "";

        final PersistentDataContainerView container = item.getPersistentDataContainer();

        if (container.has(PersistentKeys.voucher_item.getNamespacedKey()) && container.has(PersistentKeys.voucher_arg.getNamespacedKey())) {
            final String arg = container.getOrDefault(PersistentKeys.voucher_arg.getNamespacedKey(), PersistentDataType.STRING, "");

            final String voucherName = container.getOrDefault(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, "");

            if (!voucherName.isEmpty() && voucherName.equalsIgnoreCase(getStrippedName())) {
                return arg;
            }
        }

        return "";
    }

    public void execute(@NotNull final Player player, @NotNull final ItemStack item, @NotNull final EquipmentSlot slot) {
        if (player.getGameMode() == GameMode.CREATIVE && this.config.getProperty(ConfigKeys.must_be_in_survival)) {
            Messages.survival_mode.sendMessage(player);

            return;
        }

        final String argument = getArgument(item);

        final Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{arg}", !argument.isEmpty() ? argument : "{arg}");

        placeholders.put("{player}", player.getName());
        placeholders.put("{world}", player.getWorld().getName());

        final Location location = player.getLocation();

        placeholders.put("{x}", String.valueOf(location.getBlockX()));
        placeholders.put("{y}", String.valueOf(location.getBlockY()));
        placeholders.put("{z}", String.valueOf(location.getBlockZ()));
        placeholders.put("{prefix}", ConfigManager.getConfig().getProperty(ConfigKeys.command_prefix));

        final FileConfiguration data = FileKeys.data.getConfiguration();

        if (!this.isAntiDupeOverridden && this.config.getProperty(ConfigKeys.dupe_protection)) {
            final PersistentDataContainerView view = item.getPersistentDataContainer();

            if (view.has(PersistentKeys.dupe_protection.getNamespacedKey())) {
                final String id = view.get(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING);

                final List<String> vouchers = data.getStringList("Used-Vouchers");

                if (vouchers.contains(id)) {
                    Messages.dupe_protection.sendMessage(player);

                    this.server.getOnlinePlayers().forEach(staff -> {
                        if (PermissionKeys.crazyvouchers_notify.hasPermission(staff)) {
                            final Map<String, String> values = new HashMap<>();

                            values.put("{player}", player.getName());
                            values.put("{id}", id);

                            Messages.notify_staff.sendMessage(staff, values);
                        }
                    });

                    if (this.config.getProperty(ConfigKeys.dupe_protection_toggle_warning)) {
                        final ItemLore.Builder builder = ItemLore.lore();

                        final String text = this.config.getProperty(ConfigKeys.dupe_protection_warning);

                        final boolean hasWarning = item.getPersistentDataContainer().has(PersistentKeys.dupe_protection_warning.getNamespacedKey());

                        if (hasWarning) return;

                        final ItemLore lore = item.getData(DataComponentTypes.LORE);

                        if (lore != null) {
                            builder.addLines(lore.lines());
                        }

                        final Component warning_text = this.fusion.parse(player, text, placeholders);

                        builder.addLine(warning_text);

                        item.setData(DataComponentTypes.LORE, builder.build());

                        item.editPersistentDataContainer(container -> container.set(PersistentKeys.dupe_protection_warning.getNamespacedKey(), PersistentDataType.STRING, text));
                    }

                    return;
                }
            }
        }

        if (!hasPassedChecks(player, argument, placeholders)) return;

        final FileConfiguration user = FileKeys.users.getConfiguration();

        final UUID uuid = player.getUniqueId();
        final String asString = uuid.toString();
        final String cleanName = getStrippedName();

        if (!PermissionKeys.crazyvouchers_bypass.hasPermission(player) && useLimiter() && user.contains("Players." + asString + ".Vouchers." + cleanName)) {
            int amount = user.getInt("Players." + asString + ".Vouchers." + cleanName);

            if (amount >= getLimiterLimit()) {
                Messages.hit_voucher_limit.sendMessage(player);

                return;
            }

            if (hasCooldown() && isCooldown(player)){
                Messages.cooldown_active.sendMessage(player, "{time}", String.valueOf(getCooldown()));

                return;
            } else {
                removeCooldown(player); // remove cooldown, to avoid the gc not cleaning it up just in case.
            }
        }

        if (this.fusion.isModReady(ModSupport.placeholder_api)) {
            final AtomicBoolean shouldCancel = new AtomicBoolean(false);

            getRequiredPlaceholders().forEach((placeholder, value) -> {
                final String newValue = PlaceholderAPI.setPlaceholders(player, placeholder);

                if (!newValue.equals(value)) {
                    player.sendMessage(this.fusion.parse(player, getRequiredPlaceholdersMessage(), placeholders));

                    shouldCancel.set(true);
                }
            });

            if (shouldCancel.get()) return;
        }

        if (!this.isEdible && this.twoStepAuthentication) {
            if (!PermissionKeys.crazyvouchers_bypass_2fa.hasPermission(player)) {
                if (this.crazyManager.isVoucherAuthActive(uuid)) {
                    final String voucher_name = this.crazyManager.getActiveVoucherAuth(uuid);

                    if (!voucher_name.equalsIgnoreCase(cleanName)) {
                        Messages.two_step_authentication.sendMessage(player);

                        this.crazyManager.addVoucherAuth(uuid, cleanName);

                        return;
                    }
                } else {
                    Messages.two_step_authentication.sendMessage(player);

                    this.crazyManager.addVoucherAuth(uuid, cleanName);

                    return;
                }
            }
        }

        this.crazyManager.removeVoucherAuth(uuid);

        final VoucherRedeemEvent event = new VoucherRedeemEvent(player, this, argument);

        this.server.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        final PlayerInventory inventory = player.getInventory();

        final int amount = item.getAmount();

        if (amount > 1) {
            item.setAmount(amount - 1);
        } else {
            inventory.setItem(slot, null);
        }

        if (hasCooldown()) {
            addCooldown(player);
        }

        dispatchCommands(player, placeholders);

        for (final ItemBuilder itemStack : getItems()) {
            Methods.addItem(player, itemStack.asItemStack(player));
        }

        if (playSounds()) {
            for (final Sound sound : getSounds()) {
                player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, getVolume(), getPitch());
            }
        }

        if (useFirework()) Methods.firework(player.getLocation(), getFireworkColors());

        final String message = getVoucherUsedMessage();

        if (!message.isEmpty()) {
            player.sendMessage(this.fusion.parse(player, message, placeholders));
        }

        if (useLimiter()) {
            FileConfiguration configuration = FileKeys.users.getConfiguration();

            configuration.set("Players." + uuid + ".UserName", player.getName());
            configuration.set("Players." + uuid + ".Vouchers." + cleanName, configuration.getInt("Players." + uuid + ".Vouchers." + cleanName) + 1);

            FileKeys.users.save();
        }

        if (this.config.getProperty(ConfigKeys.dupe_protection)) {
            final PersistentDataContainerView view = item.getPersistentDataContainer();

            if (view.has(PersistentKeys.dupe_protection.getNamespacedKey())) {
                final String id = view.get(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING);

                FileConfiguration configuration = FileKeys.data.getConfiguration();

                List<String> vouchers = new ArrayList<>(configuration.getStringList("Used-Vouchers"));

                if (!vouchers.contains(id)) {
                    vouchers.add(id);

                    configuration.set("Used-Vouchers", vouchers);

                    FileKeys.data.save();
                } else {
                    this.fusion.log("warn", "{} is already in the data.yml somehow.", id == null ? "N/A" : id);
                }
            }
        }
    }

    private boolean hasPassedChecks(@NotNull final Player player, @NotNull final String argument, @NotNull final Map<String, String> placeholders) {
        if (player.isOp()) {
            return true;
        }

        final boolean blacklist = useBlackListPermissions();
        final boolean whitelist = useWhiteListPermissions();

        final List<String> permissions = blacklist ? getBlackListPermissions() : whitelist ? getWhitelistPermissions() : List.of();
        final List<String> commands = blacklist ? getBlacklistCommands() : whitelist ? getWhitelistCommands() : List.of();
        final String message = blacklist ? getBlackListMessage() : whitelist ? getWhitelistPermissionMessage() : "";

        if (whitelist && !hasPermission(player, permissions, commands, placeholders, message, argument)) {
            return false;
        }

        if (usesWhitelistWorlds() && !getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
            Methods.dispatch(player, List.of(getWhitelistWorldMessage()), placeholders, false);

            Methods.dispatch(player, getWhitelistWorldCommands(), placeholders, true);

            return false;
        }

        if (blacklist && hasPermission(player, permissions, commands, placeholders, message, argument)) {
            return false;
        }

        return true;
    }

    public String getStrippedName() {
        return this.cleanName;
    }

    public String getFileName() {
        return this.name;
    }

    public boolean hasArgument() {
        return this.hasArgument;
    }
    
    public ItemStack buildItem(@NotNull final Player player) {
        return buildItem(player, 1);
    }

    private @NotNull final SettingsManager config = ConfigManager.getConfig();
    
    public ItemStack buildItem(@NotNull final Player player, final int amount) {
        this.itemBuilder.setAmount(amount);

        final ItemStack item = this.itemBuilder.build().asItemStack(player);

        setUniqueId(item);

        item.editPersistentDataContainer(container -> container.set(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, getStrippedName()));

        return item;
    }

    public List<ItemStack> buildItems(@NotNull final Player player, @NotNull final String argument, final int amount) {
        final List<ItemStack> itemStacks = new ArrayList<>();

        if (!this.isAntiDupeOverridden && this.config.getProperty(ConfigKeys.dupe_protection)) {
            while (itemStacks.size() < amount) {
                itemStacks.add(buildItem(player, argument, 1));
            }
        } else {
            itemStacks.add(buildItem(player, argument, amount));
        }

        return itemStacks;
    }
    
    public ItemStack buildItem(@NotNull final Player player, @NotNull final String argument, final int amount) {
        final ItemStack item = this.itemBuilder.setAmount(amount).addPlaceholder("{arg}", argument).asItemStack(player);

        setUniqueId(item);

        item.editPersistentDataContainer(container -> {
            container.set(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, getStrippedName());

            if (!argument.isEmpty()) container.set(PersistentKeys.voucher_arg.getNamespacedKey(), PersistentDataType.STRING, argument);
        });

        return item;
    }

    public void dispatchCommands(@NotNull final Player player, @NotNull final Map<String, String> placeholders) {
        Methods.dispatch(player, this.commands, placeholders, true); // dispatch normal commands

        if (this.randomCommands.isEmpty()) return;

        // dispatch commands without a weight option randomly
        // if the prize weight is greater than 0.0D, remove it.
        final List<VoucherCommand> randomCommands = this.randomCommands.stream().filter(filter -> filter.getWeight() == -1).toList();

        if (!randomCommands.isEmpty()) {
            final VoucherCommand randomCommand = randomCommands.get(Methods.getRandom(randomCommands.size()));

            Methods.dispatch(player, randomCommand.getCommands(), placeholders, true);
        }

        // dispatch commands while accounting for the weight on each one.
        // if a prize weight is less than or equal to, remove it.
        final List<VoucherCommand> chanceCommands = this.randomCommands.stream().filter(filter -> filter.getWeight() != -1).toList();

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

    private void setUniqueId(@NotNull final ItemStack item) {
        if (!this.isAntiDupeOverridden && this.config.getProperty(ConfigKeys.dupe_protection)) {
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

    public boolean isAntiDupeOverridden() {
        return this.isAntiDupeOverridden;
    }

    public boolean isItemFramePlacementToggled() {
        return this.isItemFramePlacementToggled;
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
            safeMessage = this.utils.toString(section.getStringList(path));
        
            return safeMessage;
        }
        
        safeMessage = section.getString(path, defaultValue);
        
        return safeMessage;
    }
}