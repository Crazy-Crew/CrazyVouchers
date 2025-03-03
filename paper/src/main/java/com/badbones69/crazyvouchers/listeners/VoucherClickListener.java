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
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import com.ryderbelserion.fusion.paper.builder.items.modern.ItemBuilder;
import com.ryderbelserion.fusion.paper.enums.Scheduler;
import com.ryderbelserion.fusion.paper.enums.Support;
import com.ryderbelserion.fusion.paper.util.scheduler.FoliaScheduler;
import io.papermc.paper.persistence.PersistentDataContainerView;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
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
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoucherClickListener implements Listener {

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NotNull final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final Server server = this.plugin.getServer();

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
            Voucher voucher = this.crazyManager.getVoucherFromItem(inventory.getItemInOffHand());

            if (voucher != null) {
                event.setCancelled(true);
            }
        }

        Voucher voucher = this.crazyManager.getVoucherFromItem(inventory.getItemInMainHand());

        if (voucher != null) {
            event.setCancelled(true);
        }
    }
    
    // This must run as highest, so it doesn't cause other plugins to check
    // the items that were added to the players inventory and replaced the item in the player's hand.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoucherClick(PlayerInteractEvent event) { // this whole event confuses me, I hate interact events.
        final Player player = event.getPlayer();
        final Action action = event.getAction();

        final PlayerInventory inventory = player.getInventory();

        if (event.getHand() == EquipmentSlot.OFF_HAND && event.getHand() != null) { // ???
            final ItemStack itemStack = inventory.getItemInOffHand();

            Voucher voucher = this.crazyManager.getVoucherFromItem(itemStack);

            if (voucher != null && !voucher.isEdible()) {
                event.setCancelled(true);

                Messages.no_permission_to_use_voucher_offhand.sendMessage(player);
            }

            return;
        }

        ItemStack item = inventory.getItemInMainHand();

        if (event.getHand() != EquipmentSlot.HAND) return; // ???

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            Voucher voucher = this.crazyManager.getVoucherFromItem(item);

            if (voucher != null && !voucher.isEdible()) {
                event.setCancelled(true);

                useVoucher(player, voucher, item);
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        final ItemStack item = event.getItem();

        final Voucher voucher = this.crazyManager.getVoucherFromItem(item);

        if (voucher != null && voucher.isEdible()) {
            final Player player = event.getPlayer();

            event.setCancelled(true);
            
            if (item.getAmount() > 1) {
                Messages.unstack_item.sendMessage(player);
            } else {
                useVoucher(player, voucher, item);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onArmorStandClick(PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && this.crazyManager.getVoucherFromItem(getItemInHand(event.getPlayer())) != null) event.setCancelled(true);
    }
    
    private void useVoucher(Player player, Voucher voucher, ItemStack item) {
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

                    this.plugin.getServer().getOnlinePlayers().forEach(staff -> {
                        if (PermissionKeys.crazyvouchers_notify.hasPermission(staff)) {
                            Messages.notify_staff.sendMessage(staff, new HashMap<>() {{
                                put("{player}", player.getName());
                                put("{id}", id);
                            }});
                        }
                    });

                    if (this.config.getProperty(ConfigKeys.dupe_protection_toggle_warning)) {
                        List<String> lore = item.getLore(); //todo() deprecated, switch to minimessage

                        if (lore == null) lore = new ArrayList<>();

                        final String option = this.config.getProperty(ConfigKeys.dupe_protection_warning);

                        boolean hasLine = false;

                        for (String line : lore) {
                            final String cleanLine = ChatColor.stripColor(line); //todo() deprecated
                            final String cleanOption = ChatColor.stripColor(MsgUtils.color(option)); //todo() deprecated

                            if (cleanLine.equalsIgnoreCase(cleanOption)) {
                                hasLine = true;

                                break;
                            }
                        }

                        if (hasLine) return;

                        final List<String> finalLore = lore;

                        item.editMeta(itemMeta -> {
                            List<String> messages = new ArrayList<>(finalLore);

                            messages.add(Support.placeholder_api.isEnabled() ? PlaceholderAPI.setPlaceholders(player, MsgUtils.color(option)) : MsgUtils.color(option));

                            itemMeta.setLore(messages); //todo() deprecated
                        });
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
                    player.sendMessage(Messages.cooldown_active.getMessage(player, "{time}", String.valueOf(voucher.getCooldown())));

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
                        String message = replacePlaceholders(voucher.getRequiredPlaceholdersMessage(), player);

                        player.sendMessage(Methods.replacePlaceholders(this.placeholders, message, false));

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

    private ItemStack getItemInHand(final Player player) {
        return player.getInventory().getItemInMainHand();
    }

    private boolean passesPermissionChecks(final Player player, final Voucher voucher, final String argument) {
        populate(player, argument);

        if (!player.isOp()) {
            if (voucher.useWhiteListPermissions()) {
                return voucher.hasPermission(true, player, voucher.getWhitelistPermissions(), voucher.getWhitelistCommands(), this.placeholders, voucher.getWhitelistPermissionMessage(), argument);
            }

            if (voucher.usesWhitelistWorlds() && !voucher.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                player.sendMessage(Methods.replacePlaceholders(this.placeholders, voucher.getWhitelistWorldMessage(), false));

                new FoliaScheduler(Scheduler.global_scheduler) {
                    @Override
                    public void run() {
                        for (final String command : voucher.getWhitelistWorldCommands()) {
                            server.dispatchCommand(server.getConsoleSender(), Methods.replacePlaceholders(placeholders, command, true));
                        }
                    }
                }.run();

                return false;
            }

            if (voucher.useBlackListPermissions()) {
                return voucher.hasPermission(true, player, voucher.getBlackListPermissions(), voucher.getBlacklistCommands(), this.placeholders, voucher.getBlackListMessage(), argument);
            }
        }

        return true;
    }

    private void populate(final Player player, final String argument) {
        this.placeholders.put("{arg}", argument != null ? argument : "{arg}");
        this.placeholders.put("{player}", player.getName());
        this.placeholders.put("{world}", player.getWorld().getName());

        final Location location = player.getLocation();

        this.placeholders.put("{x}", String.valueOf(location.getBlockX()));
        this.placeholders.put("{y}", String.valueOf(location.getBlockY()));
        this.placeholders.put("{z}", String.valueOf(location.getBlockZ()));
        this.placeholders.put("{prefix}", ConfigManager.getConfig().getProperty(ConfigKeys.command_prefix));
    }

    private void voucherClick(Player player, ItemStack item, Voucher voucher, String argument) {
        Methods.removeItem(item, player);

        if (voucher.hasCooldown()){
            voucher.addCooldown(player);
        }

        populate(player, argument);

        for (String command : voucher.getCommands()) {
            command = replacePlaceholders(command, player);

            this.server.dispatchCommand(this.server.getConsoleSender(), Methods.replacePlaceholders(this.placeholders, this.crazyManager.replaceRandom(command), true));
        }

        if (!voucher.getRandomCommands().isEmpty()) { // Picks a random command from the Random-Commands list.
            for (String command : voucher.getRandomCommands().get(Methods.getRandom(voucher.getRandomCommands().size())).getCommands()) {
                command = replacePlaceholders(command, player);

                this.server.dispatchCommand(this.server.getConsoleSender(), Methods.replacePlaceholders(this.placeholders, this.crazyManager.replaceRandom(command), true));
            }
        }

        if (!voucher.getChanceCommands().isEmpty()) { // Picks a command based on the chance system of the Chance-Commands list.
            for (String command : voucher.getChanceCommands().get(Methods.getRandom(voucher.getChanceCommands().size())).getCommands()) {
                command = replacePlaceholders(command, player);

                this.server.dispatchCommand(this.server.getConsoleSender(), Methods.replacePlaceholders(this.placeholders, this.crazyManager.replaceRandom(command), true));
            }
        }

        for (final ItemBuilder itemStack : voucher.getItems()) {
            Methods.addItem(player, itemStack.asItemStack(true));
        }

        if (voucher.playSounds()) {
            for (Sound sound : voucher.getSounds()) {
                player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, voucher.getVolume(), voucher.getPitch());
            }
        }

        if (voucher.useFirework()) Methods.firework(player.getLocation(), voucher.getFireworkColors());

        if (!voucher.getVoucherUsedMessage().isEmpty()) {
            String message = replacePlaceholders(voucher.getVoucherUsedMessage(), player);

            player.sendMessage(Methods.replacePlaceholders(this.placeholders, message, false));
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
                    this.plugin.getLogger().warning(id + " is already in the data.yml somehow.");
                }
            }
        }
    }

    private String replacePlaceholders(String string, Player player) {
        if (Support.placeholder_api.isEnabled()) return PlaceholderAPI.setPlaceholders(player, string);

        return MsgUtils.color(string);
    }
}