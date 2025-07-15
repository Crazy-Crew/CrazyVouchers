package com.badbones69.crazyvouchers.listeners;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.api.enums.misc.PermissionKeys;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemEvent;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCommand;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.utils.ScheduleUtils;
import com.ryderbelserion.fusion.core.api.enums.Support;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.api.builders.items.ItemBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.persistence.PersistentDataContainerView;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoucherClickListener implements Listener {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final Server server = this.plugin.getServer();

    private final FusionPaper fusion = this.plugin.getFusion();

    private final SettingsManager config = ConfigManager.getConfig();
    
    private final Map<UUID, String> twoAuth = new HashMap<>();

    private final Map<String, String> placeholders = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnerChange(PlayerInteractEvent event) {
        final ItemStack item = getItemInHand(event.getPlayer());
        final Player player = event.getPlayer();
        final Action action = event.getAction();
        final Block block = event.getClickedBlock();

        if (action != Action.RIGHT_CLICK_BLOCK) return;

        if (block == null) return;

        if (block.getType() != Material.SPAWNER) return;

        if (!item.getType().toString().endsWith("SPAWN_EGG")) return;

        final PlayerInventory inventory = player.getInventory();

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            @Nullable final Voucher voucher = this.crazyManager.getVoucherFromItem(inventory.getItemInOffHand());

            if (voucher != null) {
                event.setCancelled(true);
            }
        }

        @Nullable final Voucher voucher = this.crazyManager.getVoucherFromItem(inventory.getItemInMainHand());

        if (voucher != null) {
            event.setCancelled(true);
        }
    }
    
    // This must run as highest, so it doesn't cause other plugins to check
    // the items that were added to the players inventory and replaced the item in the player's hand.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoucherClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Action action = event.getAction();

        final PlayerInventory inventory = player.getInventory();

        final EquipmentSlot slot = event.getHand();

        if (slot == null) return;

        if (slot == EquipmentSlot.OFF_HAND) {
            final Voucher voucher = this.crazyManager.getVoucherFromItem(inventory.getItemInOffHand());

            if (voucher == null) return;
            if (voucher.isEdible()) return;

            Messages.no_permission_to_use_voucher_offhand.sendMessage(player);

            this.fusion.log("warn", "{} tried to use the voucher in off-hand.", player.getName());

            event.setCancelled(true);

            return;
        }

        if (slot != EquipmentSlot.HAND) return;
        if (action != Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) return;

        final ItemStack item = inventory.getItemInMainHand();
        final Voucher voucher = this.crazyManager.getVoucherFromItem(item);

        if (voucher == null) return;
        if (voucher.isEdible()) return;

        useVoucher(player, voucher, item);

        event.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        final EquipmentSlot slot = event.getHand();

        if (slot == null) return;
        if (slot == EquipmentSlot.HAND) return;

        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();

        if (slot == EquipmentSlot.OFF_HAND) {
            final Voucher voucher = this.crazyManager.getVoucherFromItem(inventory.getItemInOffHand());

            if (voucher == null) return;
            if (!voucher.isEdible()) return;

            Messages.no_permission_to_use_voucher_offhand.sendMessage(player);

            this.fusion.log("warn", "{} tried to use the voucher in off-hand like it's a piece of food.", player.getName());

            event.setCancelled(true);

            return;
        }

        final ItemStack item = event.getItem();

        final Voucher voucher = this.crazyManager.getVoucherFromItem(item);

        if (voucher == null) return;
        if (!voucher.isEdible()) return;

        event.setCancelled(true);

        if (item.getAmount() > 1) {
            Messages.unstack_item.sendMessage(player);

            return;
        }

        useVoucher(player, voucher, item);
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onArmorStandClick(PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && this.crazyManager.getVoucherFromItem(getItemInHand(event.getPlayer())) != null) event.setCancelled(true);
    }
    
    private void useVoucher(@NotNull final Player player, @NotNull final Voucher voucher, @NotNull final ItemStack item) {
        final FileConfiguration user = FileKeys.users.getConfiguration();
        final FileConfiguration data = FileKeys.data.getConfiguration();

        final String argument = this.crazyManager.getArgument(item, voucher);

        if (player.getGameMode() == GameMode.CREATIVE && this.config.getProperty(ConfigKeys.must_be_in_survival)) {
            Messages.survival_mode.sendMessage(player);

            return;
        }

        if (this.config.getProperty(ConfigKeys.dupe_protection)) {
            final PersistentDataContainerView view = item.getPersistentDataContainer();

            if (view.has(PersistentKeys.dupe_protection.getNamespacedKey())) {
                final String id = view.get(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING);

                final List<String> vouchers = data.getStringList("Used-Vouchers");

                if (vouchers.contains(id)) {
                    Messages.dupe_protection.sendMessage(player);

                    this.server.getOnlinePlayers().forEach(staff -> {
                        if (PermissionKeys.crazyvouchers_notify.hasPermission(staff)) {
                            Messages.notify_staff.sendMessage(staff, new HashMap<>() {{
                                put("{player}", player.getName());
                                put("{id}", id);
                            }});
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

                        final Component warning_text = this.fusion.color(player, text, this.placeholders);

                        builder.addLine(warning_text);

                        item.setData(DataComponentTypes.LORE, builder.build());

                        item.editPersistentDataContainer(container -> container.set(PersistentKeys.dupe_protection_warning.getNamespacedKey(), PersistentDataType.STRING, text));
                    }

                    return;
                }
            }
        }

        if (passesPermissionChecks(player, voucher, argument)) {
            final UUID uuid = player.getUniqueId();
            final String asString = uuid.toString();

            if (!PermissionKeys.crazyvouchers_bypass.hasPermission(player) && voucher.useLimiter() && user.contains("Players." + asString + ".Vouchers." + voucher.getName())) {
                int amount = user.getInt("Players." + asString + ".Vouchers." + voucher.getName());

                if (amount >= voucher.getLimiterLimit()) {
                    Messages.hit_voucher_limit.sendMessage(player);

                    return;
                }

                if (voucher.hasCooldown() && voucher.isCooldown(player)){
                    Messages.cooldown_active.sendMessage(player, "{time}", String.valueOf(voucher.getCooldown()));

                    return;
                } else {
                    voucher.removeCooldown(player); // remove cooldown, to avoid the gc not cleaning it up just in case.
                }
            }

            if (Support.placeholder_api.isEnabled()) {
                AtomicBoolean shouldCancel = new AtomicBoolean(false);

                voucher.getRequiredPlaceholders().forEach((placeholder, value) -> {
                    String newValue = PlaceholderAPI.setPlaceholders(player, placeholder);

                    if (!newValue.equals(value)) {
                        player.sendMessage(this.fusion.color(player, voucher.getRequiredPlaceholdersMessage(), this.placeholders));

                        shouldCancel.set(true);
                    }
                });

                if (shouldCancel.get()) return;
            }

            if (!voucher.isEdible() && voucher.useTwoStepAuthentication()) {
                if (this.twoAuth.containsKey(uuid)) {
                    if (!this.twoAuth.get(uuid).equalsIgnoreCase(voucher.getName())) {
                        Messages.two_step_authentication.sendMessage(player);

                        this.twoAuth.put(uuid, voucher.getName());

                        return;
                    }
                } else {
                    Messages.two_step_authentication.sendMessage(player);

                    this.twoAuth.put(uuid, voucher.getName());

                    return;
                }
            }

            this.twoAuth.remove(uuid);

            VoucherRedeemEvent event = new VoucherRedeemEvent(player, voucher, argument);
            this.server.getPluginManager().callEvent(event);

            if (!event.isCancelled()) voucherClick(player, item, voucher, argument);
        }
    }

    private ItemStack getItemInHand(@NotNull final Player player) {
        return player.getInventory().getItemInMainHand();
    }

    private boolean passesPermissionChecks(@NotNull final Player player, @NotNull final Voucher voucher, @NotNull final String argument) {
        populate(player, argument);

        if (!player.isOp()) {
            if (voucher.useWhiteListPermissions()) {
                return voucher.hasPermission(true, player, voucher.getWhitelistPermissions(), voucher.getWhitelistCommands(), this.placeholders, voucher.getWhitelistPermissionMessage(), argument);
            }

            if (voucher.usesWhitelistWorlds() && !voucher.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                player.sendMessage(this.fusion.color(player, voucher.getWhitelistWorldMessage(), this.placeholders));

                ScheduleUtils.dispatch(consumer -> {
                    for (final String command : voucher.getWhitelistWorldCommands()) {
                        server.dispatchCommand(server.getConsoleSender(), Methods.placeholders(player, command, placeholders));
                    }
                });

                return false;
            }

            if (voucher.useBlackListPermissions()) {
                return voucher.hasPermission(true, player, voucher.getBlackListPermissions(), voucher.getBlacklistCommands(), this.placeholders, voucher.getBlackListMessage(), argument);
            }
        }

        return true;
    }

    private void populate(@NotNull final Player player, @NotNull final String argument) {
        this.placeholders.put("{arg}", argument != null ? argument : "{arg}");
        this.placeholders.put("{player}", player.getName());
        this.placeholders.put("{world}", player.getWorld().getName());

        final Location location = player.getLocation();

        this.placeholders.put("{x}", String.valueOf(location.getBlockX()));
        this.placeholders.put("{y}", String.valueOf(location.getBlockY()));
        this.placeholders.put("{z}", String.valueOf(location.getBlockZ()));
        this.placeholders.put("{prefix}", ConfigManager.getConfig().getProperty(ConfigKeys.command_prefix));
    }

    private void voucherClick(@NotNull final Player player, @NotNull final ItemStack item, @NotNull final Voucher voucher, @NotNull final String argument) {
        Methods.removeItem(item, player);

        if (voucher.hasCooldown()){
            voucher.addCooldown(player);
        }

        populate(player, argument);

        ScheduleUtils.dispatch(consumer -> {
            for (final String command : voucher.getCommands()) {
                this.server.dispatchCommand(this.server.getConsoleSender(), Methods.placeholders(player, this.crazyManager.replaceRandom(command), placeholders));
            }

            final List<VoucherCommand> randomCommands = voucher.getRandomCommands();

            if (!randomCommands.isEmpty()) { // Picks a random command from the Random-Commands list.
                for (final String command : randomCommands.get(Methods.getRandom(randomCommands.size())).getCommands()) {
                    this.server.dispatchCommand(this.server.getConsoleSender(), Methods.placeholders(player, this.crazyManager.replaceRandom(command), placeholders));
                }
            }

            final List<VoucherCommand> chanceCommands = voucher.getChanceCommands();

            if (!chanceCommands.isEmpty()) { // Picks a command based on the chance system of the Chance-Commands list.
                for (final String command : chanceCommands.get(Methods.getRandom(chanceCommands.size())).getCommands()) {
                    this.server.dispatchCommand(this.server.getConsoleSender(), Methods.placeholders(player, this.crazyManager.replaceRandom(command), placeholders));
                }
            }
        });

        for (final ItemBuilder itemStack : voucher.getItems()) {
            Methods.addItem(player, itemStack.asItemStack());
        }

        if (voucher.playSounds()) {
            for (final Sound sound : voucher.getSounds()) {
                player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, voucher.getVolume(), voucher.getPitch());
            }
        }

        if (voucher.useFirework()) Methods.firework(player.getLocation(), voucher.getFireworkColors());

        final String message = voucher.getVoucherUsedMessage();

        if (!message.isEmpty()) {
            player.sendMessage(this.fusion.color(player, message, this.placeholders));
        }

        if (voucher.useLimiter()) {
            FileConfiguration configuration = FileKeys.users.getConfiguration();

            final UUID uuid = player.getUniqueId();

            configuration.set("Players." + uuid + ".UserName", player.getName());
            configuration.set("Players." + uuid + ".Vouchers." + voucher.getName(), configuration.getInt("Players." + uuid + ".Vouchers." + voucher.getName()) + 1);

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
                    this.plugin.getComponentLogger().warn("{} is already in the data.yml somehow.", id);
                }
            }
        }
    }
}