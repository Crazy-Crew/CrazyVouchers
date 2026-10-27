package com.badbones69.crazyvouchers.api.objects;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.CrazyManager;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import com.badbones69.crazyvouchers.api.enums.PermissionKeys;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemEvent;
import us.crazycrew.crazyvouchers.api.config.types.locale.MessageKeys;
import com.badbones69.crazyvouchers.utils.ItemUtils;
import com.ryderbelserion.fusion.api.enums.constants.ModSupport;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.builders.items.ItemBuilder;
import com.ryderbelserion.fusion.paper.builders.items.types.custom.CustomBuilder;
import com.ryderbelserion.fusion.paper.utils.ColorUtils;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.persistence.PersistentDataContainerView;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import com.badbones69.crazyvouchers.config.ConfigManager;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Voucher {

    private final CrazyVouchers plugin = CrazyVouchers.get();
    private final Server server = this.plugin.getServer();
    private final FusionPaper fusion = this.plugin.getFusion();

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

    private final List<String> twoFactorAuthMessage;
    private final boolean twoFactorAuthToggle;

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

    public Voucher(@NonNull final CommentedConfigurationNode section, @NonNull final String name) {
        this.cleanName = name.replace(".yml", "");
        this.name = name;

        this.isAntiDupeOverridden = section.node("override-anti-dupe").getBoolean(false);

        this.isItemFramePlacementToggled = section.node("allow-vouchers-in-item-frames").getBoolean(false);

        this.hasCooldown = section.node("cooldown", "toggle").getBoolean(false);
        this.cooldownInterval = section.node("cooldown", "interval").getInt(5);

        String material = section.node("item").getString("stone").toLowerCase();
        String model_data = "";

        if (material.contains("#")) {
            final String[] split = material.split("#");

            material = split[0];
            model_data = split[1];
        }

        final String displayName = section.node("name").getString("");

        final List<String> displayLore = section.node("lore").isList() ? StringUtils.getStringList(section.node("lore")) : List.of(section.node("lore").getString(""));

        this.itemBuilder = new ItemBuilder(material)
                .withDisplayName(displayName)
                .withDisplayLore(displayLore);

        final CustomBuilder customBuilder = this.itemBuilder.asCustomBuilder();

        if (!model_data.isEmpty()) {
            customBuilder.setCustomModelData(model_data);
        }

        if (section.hasChild("custom-model-data")) {
            customBuilder.setCustomModelData(section.node("custom-model-data").getString("-1"));
        }

        final String playerName = section.node("player").getString("");

        if (!playerName.isEmpty()) {
            this.itemBuilder.asSkullBuilder().withName(playerName).build();
        }

        this.itemBuilder.setItemDamage(section.node("display-damage").getInt(-1));

        final String trimMaterial = section.node("display-trim", "material").getString("").toLowerCase();
        final String trimPattern = section.node("display-trim", "pattern").getString("").toLowerCase();

        if (!trimMaterial.isEmpty() && !trimPattern.isEmpty()) {
            this.itemBuilder.setTrim(trimPattern, trimMaterial);
        }

        final String rgb = section.node("settings", "rgb").getString("");

        final String color = section.node("settings", "color").getString("");

        this.itemBuilder.setColor(!color.isEmpty() ? color : !rgb.isEmpty() ? rgb : "");

        this.itemBuilder.withSkull(section.node("skull").getString(""));

        switch (section.node("glowing").getString("none").toLowerCase()) {
            case "add_glow", "true" -> this.itemBuilder.addEnchantGlint();
            case "remove_glow", "false" -> this.itemBuilder.removeEnchantGlint();
            case "none" -> {}
        }

        this.commands = section.node("commands").isList() ? StringUtils.getStringList(section.node("commands")) : List.of(section.node("commands").getString(""));

        this.hasArgument = section.node("has-argument").getBoolean(false);

        if (this.config.getProperty(ConfigKeys.use_different_items_layout) && !section.node("items").isList()) {
            this.items = ItemUtils.convertConfigurationSection(section.node("items"));
        } else {
            this.items = ItemUtils.convertStringList(StringUtils.getStringList(section.node("item")));
        }

        this.usedMessage = getMessage(section, "options.message", "");

        this.whitelistPermissionToggle = section.node("options", "permission", "whitelist-permission", "toggle").getBoolean(false);
        this.whitelistCommands = section.node("options", "permission", "whitelist-permission", "commands").isList() ? StringUtils.getStringList(section.node("options", "permission", "whitelist-permission", "commands")) :
                List.of(section.node("options", "permission", "whitelist-permission", "commands").getString(""));
        this.whitelistPermissionMessage = getMessage(section, "options.permission.whitelist-permission.message", Message.no_permission_to_use_voucher.getString());

        this.whitelistPermissions.addAll(StringUtils.getStringList(section.node("options", "permission", "whitelist-permission", "permissions")).stream().map(String::toLowerCase).toList());

        final String permission = section.node("options", "permission", "whitelist-permission", "node").getString("").toLowerCase();

        if (!permission.isEmpty()) {
            this.whitelistPermissions.add(permission);
        }

        this.whitelistWorldsToggle = section.node("options", "whitelist-worlds", "toggle").getBoolean(false);
        this.whitelistWorldCommands = section.node("options", "whitelist-worlds", "commands").isList() ? StringUtils.getStringList(section.node("options", "whitelist-worlds", "commands")) : List.of(section.node("options", "whitelist-worlds", "commands").getString(""));
        this.whitelistWorldMessage = getMessage(section, "options.whitelist-worlds.message", Message.not_in_whitelisted_world.getString());

        this.whitelistWorlds.addAll(StringUtils.getStringList(section.node("options", "whitelist-worlds", "worlds")).stream().map(String::toLowerCase).toList());

        this.blacklistPermissionsToggle = section.node("options", "permission", "blacklist-permission", "toggle").getBoolean(false);
        this.blacklistPermissionMessage = getMessage(section, "options.permission.blacklist-permission.message", Message.has_blacklist_permission.getString());
        this.blacklistPermissions = section.node("options", "permission", "blacklist-permission", "permissions").isList() ? StringUtils.getStringList(section.node("options", "permission", "blacklist-permission", "permissions"))
                : List.of(section.node("options", "permission", "blacklist-permission", "permissions").getString(""));
        this.blacklistCommands = section.node("options", "permission", "blacklist-permission", "commands").isList() ? StringUtils.getStringList(section.node("options", "permission", "blacklist-permission", "commands"))
                : List.of(section.node("options", "permission", "blacklist-permission", "commands").getString(""));

        this.limiterToggle = section.node("options", "limiter", "toggle").getBoolean(false);
        this.limiterLimit = section.node("options", "limiter", "limit").getInt(0);

        this.requiredPlaceholdersMessage = getMessage(section, "options.required-placeholders-message", "");

        if (section.hasChild("options", "required-placeholders")) {
            for (final Map.Entry<Object, CommentedConfigurationNode> entry : section.node("options", "required-placeholders").childrenMap().entrySet()) {
                final CommentedConfigurationNode value = entry.getValue();

                final String placeholder = value.node("placeholder").getString("");

                if (placeholder.isEmpty()) continue;

                final String text = value.node("value").getString("");

                if (text.isEmpty()) continue;

                this.requiredPlaceholders.put(placeholder, text);
            }
        }

        this.twoFactorAuthToggle = section.hasChild("options", "two-step-authentication") ? section.node("options", "two-step-authentication").getBoolean(false) : section.node("options", "two-factor", "toggle").getBoolean(false);
        this.twoFactorAuthMessage = section.hasChild("options", "two-factor", "message") ? StringUtils.getStringList(section.node("options", "two-factor", "message")) : List.of(this.locale.getProperty(MessageKeys.two_step_authentication));

        this.soundToggle = section.node("options", "sound", "toggle").getBoolean(false);
        this.volume = (float) section.node("options", "sound", "volume").getDouble(1.0);
        this.pitch = (float) section.node("options", "sound", "pitch").getDouble(1.0);

        if (this.soundToggle) {
            for (final String sound : StringUtils.getStringList(section.node("options", "sound", "sounds"))) {
                com.ryderbelserion.fusion.paper.utils.ItemUtils.getSound(sound).ifPresent(this.sounds::add);
            }
        }

        if (section.node("components", "hide-tooltip").getBoolean(false)) {
            this.itemBuilder.hideToolTip();
        }

        if (section.node("components", "hide-tooltip-advanced").isList()) {
            this.itemBuilder.hideComponents(StringUtils.getStringList(section.node("components", "hide-tooltip-advanced")));
        }

        customBuilder.setItemModel(section.node("components", "item-model", "namespace").getString(""), section.node("components", "item-model", "key").getString(""));

        customBuilder.build();

        this.fireworkToggle = section.node("options", "firework", "toggle").getBoolean(false);

        if (this.fireworkToggle) {
            for (final String fireworkColor : section.node("options", "firework", "colors").getString("").split(", ")) {
                this.fireworkColors.add(ColorUtils.getColor(fireworkColor));
            }
        }

        this.isEdible = section.node("options", "is-edible").getBoolean(false) && this.itemBuilder.isEdible();

        if (section.hasChild("chance-commands")) {
            this.fusion.log(Level.warn, "We detected that you have the list version of chance-commands which is no longer used, Please run /crazyvouchers migrate -mt VouchersDeprecated");
        }

        if (section.hasChild("random-commands")) {
            final CommentedConfigurationNode node = section.node("random-commands");

            if (node.isList()) {
                this.fusion.log(Level.warn, "We've detected that you have the list version of random-commands which is no longer used, Please run /crazyvouchers migrate -mt VouchersDeprecated");
            } else {
                node.childrenMap().forEach((id, key) -> this.randomCommands.add(new VoucherCommand(
                        StringUtils.getStringList(key.node("commands")),
                        key.node("weight").getDouble(-1)
                )));
            }
        }

        // if the prize weight is greater than 0.0, grab it.
        this.totalWeight = this.randomCommands.stream().filter(filter -> filter.getWeight() > 0.0).mapToDouble(VoucherCommand::getWeight).sum();
    }

    public @NonNull String getArgument(@NonNull final ItemStack item) {
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

    public void execute(@NonNull final Player player, @NonNull final ItemStack item, @NonNull final EquipmentSlot slot) {
        if (player.getGameMode() == GameMode.CREATIVE && this.config.getProperty(ConfigKeys.must_be_in_survival)) {
            Message.survival_mode.sendMessage(player);

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

        final CommentedConfigurationNode data = FileKeys.data.getConfiguration();

        if (!this.isAntiDupeOverridden && this.config.getProperty(ConfigKeys.dupe_protection)) {
            final PersistentDataContainerView view = item.getPersistentDataContainer();

            if (view.has(PersistentKeys.dupe_protection.getNamespacedKey())) {
                final String id = view.get(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING);

                final List<String> vouchers = StringUtils.getStringList(data.node("Used-Vouchers"));

                if (vouchers.contains(id)) {
                    Message.dupe_protection.sendMessage(player);

                    this.server.getOnlinePlayers().forEach(staff -> {
                        if (PermissionKeys.crazyvouchers_notify.hasPermission(staff)) {
                            final Map<String, String> values = new HashMap<>();

                            values.put("{player}", player.getName());
                            values.put("{id}", id);

                            Message.notify_staff.sendMessage(staff, values);
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

                        final Component warning_text = this.fusion.asComponent(player, text, placeholders);

                        builder.addLine(warning_text);

                        item.setData(DataComponentTypes.LORE, builder.build());

                        item.editPersistentDataContainer(container -> container.set(PersistentKeys.dupe_protection_warning.getNamespacedKey(), PersistentDataType.STRING, text));
                    }

                    return;
                }
            }
        }

        if (!hasPassedChecks(player, argument, placeholders)) return;

        final UUID uuid = player.getUniqueId();
        final String asString = uuid.toString();
        final String cleanName = getStrippedName();

        final CommentedConfigurationNode section = FileKeys.users.getConfiguration().node("Players");

        if (!PermissionKeys.crazyvouchers_bypass.hasPermission(player) && useLimiter() && section.hasChild(asString, "Vouchers", cleanName)) {
            int amount = section.node(asString, "Vouchers", cleanName).getInt();

            if (amount >= getLimiterLimit()) {
                Message.hit_voucher_limit.sendMessage(player);

                return;
            }

            if (hasCooldown() && isCooldown(player)){
                Message.cooldown_active.sendMessage(player, "{time}", String.valueOf(getCooldown()));

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
                    player.sendMessage(this.fusion.asComponent(player, getRequiredPlaceholdersMessage(), placeholders));

                    shouldCancel.set(true);
                }
            });

            if (shouldCancel.get()) return;
        }

        if (!this.isEdible && this.twoFactorAuthToggle) {
            if (!PermissionKeys.crazyvouchers_bypass_2fa.hasPermission(player)) {
                if (this.crazyManager.isVoucherAuthActive(uuid)) {
                    final String voucher_name = this.crazyManager.getActiveVoucherAuth(uuid);

                    if (!voucher_name.equalsIgnoreCase(cleanName)) {
                        player.sendMessage(this.fusion.asComponent(player, StringUtils.toString(this.twoFactorAuthMessage), Map.of(
                                "{prefix}", this.config.getProperty(ConfigKeys.command_prefix)
                        )));

                        this.crazyManager.addVoucherAuth(uuid, cleanName);

                        return;
                    }
                } else {
                    player.sendMessage(this.fusion.asComponent(player, StringUtils.toString(this.twoFactorAuthMessage), Map.of(
                            "{prefix}", this.config.getProperty(ConfigKeys.command_prefix)
                    )));

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
            player.sendMessage(this.fusion.asComponent(player, message, placeholders));
        }

        if (useLimiter()) {
            final CommentedConfigurationNode user = section.node(uuid.toString());

            try {
                user.node("UserName").set(String.class, player.getName());
            } catch (final SerializationException exception) {
                exception.printStackTrace();
            }

            final CommentedConfigurationNode voucher = user.node("Vouchers", cleanName);

            try {
                voucher.set(voucher.getInt()+1);
            } catch (final SerializationException exception) {
                exception.printStackTrace();
            }

            // configuration.set("Players." + uuid + ".Vouchers." + cleanName, configuration.getInt("Players." + uuid + ".Vouchers." + cleanName) + 1); //todo() wtf

            FileKeys.users.save();
        }

        if (this.config.getProperty(ConfigKeys.dupe_protection)) {
            final PersistentDataContainerView view = item.getPersistentDataContainer();

            if (view.has(PersistentKeys.dupe_protection.getNamespacedKey())) {
                final String id = view.get(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING);

                final CommentedConfigurationNode configuration = FileKeys.data.getConfiguration().node("Used-Vouchers");

                final List<String> vouchers = new ArrayList<>(StringUtils.getStringList(configuration));

                if (!vouchers.contains(id)) {
                    vouchers.add(id);

                    try {
                        configuration.set(new TypeToken<>() {}, vouchers);

                        FileKeys.data.save();
                    } catch (final SerializationException exception) {
                        exception.printStackTrace();
                    }
                } else {
                    this.fusion.log(Level.warn, "%s is already in the data.yml somehow.", id == null ? "N/A" : id);
                }
            }
        }
    }

    private boolean hasPassedChecks(@NonNull final Player player, @NonNull final String argument, @NonNull final Map<String, String> placeholders) {
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
    
    public ItemStack buildItem(@NonNull final Player player) {
        return buildItem(player, 1);
    }

    private @NonNull final SettingsManager config = ConfigManager.getConfig();

    private @NonNull final SettingsManager locale = ConfigManager.getMessages();

    public ItemStack buildItem(@NonNull final Player player, final int amount) {
        this.itemBuilder.setAmount(amount);

        final ItemStack item = this.itemBuilder.build().asItemStack(player);

        setUniqueId(item);

        item.editPersistentDataContainer(container -> container.set(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, getStrippedName()));

        return item;
    }

    public List<ItemStack> buildItems(@NonNull final Player player, @NonNull final String argument, final int amount) {
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
    
    public ItemStack buildItem(@NonNull final Player player, @NonNull final String argument, final int amount) {
        final ItemStack item = this.itemBuilder.setAmount(amount).addPlaceholder("{arg}", argument).asItemStack(player);

        setUniqueId(item);

        item.editPersistentDataContainer(container -> {
            container.set(PersistentKeys.voucher_item.getNamespacedKey(), PersistentDataType.STRING, getStrippedName());

            if (!argument.isEmpty()) container.set(PersistentKeys.voucher_arg.getNamespacedKey(), PersistentDataType.STRING, argument);
        });

        return item;
    }

    public void dispatchCommands(@NonNull final Player player, @NonNull final Map<String, String> placeholders) {
        Methods.dispatch(player, this.commands, placeholders, true); // dispatch normal commands

        if (this.randomCommands.isEmpty()) return;

        // dispatch commands without a weight option randomly
        // if the prize weight is greater than 0.0D, remove it.
        final List<VoucherCommand> randomCommands = this.randomCommands.stream().filter(filter -> filter.getWeight() <= 0.0).toList();

        if (!randomCommands.isEmpty()) {
            final VoucherCommand randomCommand = randomCommands.get(Methods.getRandom(randomCommands.size()));

            Methods.dispatch(player, randomCommand.getCommands(), placeholders, true);
        }

        // dispatch commands while accounting for the weight on each one.
        // if a prize weight is less than or equal to, remove it.
        final List<VoucherCommand> chanceCommands = this.randomCommands.stream().filter(filter -> filter.getWeight() > 0.0).toList();

        if (!chanceCommands.isEmpty()) {
            Methods.dispatch(player, Methods.getCommand(chanceCommands, this.totalWeight).getCommands(), placeholders, true);
        }
    }

    private void setUniqueId(@NonNull final ItemStack item) {
        if (!this.isAntiDupeOverridden && this.config.getProperty(ConfigKeys.dupe_protection)) {
            final String uuid = UUID.randomUUID().toString();

            item.editPersistentDataContainer(container -> container.set(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING, uuid));
        }
    }

    public boolean hasPermission(@NonNull final Player player, @NonNull final List<String> permissions, @NonNull final List<String> commands, @NonNull final Map<String, String> placeholders, @NonNull final String message, @NonNull final String argument) {
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
        return this.twoFactorAuthToggle;
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

    public boolean isCooldown(@NonNull final Player player) {
        return this.cooldowns.getOrDefault(player.getUniqueId(), 0L) >= System.currentTimeMillis();
    }

    public void addCooldown(@NonNull final Player player) {
        this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (1000L * getCooldown()));
    }

    public void removeCooldown(@NonNull final Player player) {
        this.cooldowns.remove(player.getUniqueId());
    }

    private String getMessage(@NonNull final CommentedConfigurationNode section, @NonNull final String defaultValue, @NonNull final Object... path) {
        final CommentedConfigurationNode node = section.node(path);
        
        return node.isList() ? StringUtils.toString(StringUtils.getStringList(node, defaultValue)) : node.getString(defaultValue);
    }
}