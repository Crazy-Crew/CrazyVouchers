package com.badbones69.crazyvouchers.listeners;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemEvent;
import com.badbones69.crazyvouchers.api.objects.other.ItemBuilder;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import com.ryderbelserion.vital.paper.api.enums.Support;
import io.papermc.paper.persistence.PersistentDataContainerView;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoucherClickListener implements Listener {

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NotNull final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final SettingsManager config = ConfigManager.getConfig();
    
    private final Map<UUID, String> twoAuth = new HashMap<>();

    private final Map<String, String> placeholders = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnerChange(PlayerInteractEvent event) {
        ItemStack item = getItemInHand(event.getPlayer());
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();

        if (action != Action.RIGHT_CLICK_BLOCK) return;

        if (block == null) return;

        if (block.getType() != Material.SPAWNER) return;

        if (!item.getType().toString().endsWith("SPAWN_EGG")) return;

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            Voucher voucher = this.crazyManager.getVoucherFromItem(player.getInventory().getItemInOffHand());

            if (voucher != null) {
                event.setCancelled(true);
            }
        }

        Voucher voucher = this.crazyManager.getVoucherFromItem(player.getInventory().getItemInMainHand());

        if (voucher != null) {
            event.setCancelled(true);
        }
    }
    
    // This must run as highest, so it doesn't cause other plugins to check
    // the items that were added to the players inventory and replaced the item in the player's hand.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoucherClick(PlayerInteractEvent event) {
        ItemStack item = getItemInHand(event.getPlayer());
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (event.getHand() == EquipmentSlot.OFF_HAND && event.getHand() != null) {
            Voucher voucher = this.crazyManager.getVoucherFromItem(player.getInventory().getItemInOffHand());

            if (voucher != null && !voucher.isEdible()) {
                event.setCancelled(true);

                Messages.no_permission_to_use_voucher_offhand.sendMessage(player);
            }

            return;
        }

        if (item.getType() != Material.AIR) {
            if (event.getHand() != EquipmentSlot.HAND) return;

            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                Voucher voucher = this.crazyManager.getVoucherFromItem(item);

                if (voucher != null && !voucher.isEdible()) {
                    event.setCancelled(true);

                    useVoucher(player, voucher, item);
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        Voucher voucher = this.crazyManager.getVoucherFromItem(item);

        if (voucher != null && voucher.isEdible()) {
            Player player = event.getPlayer();

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
        FileConfiguration user = Files.users.getConfiguration();
        FileConfiguration data = Files.data.getConfiguration();

        String argument = this.crazyManager.getArgument(item, voucher);

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
                        if (staff.hasPermission("crazyvouchers.notify.duped")) {
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
            String uuid = player.getUniqueId().toString();

            if (!player.hasPermission("voucher.bypass") && voucher.useLimiter() && user.contains("Players." + uuid + ".Vouchers." + voucher.getName())) {
                int amount = user.getInt("Players." + uuid + ".Vouchers." + voucher.getName());

                if (amount >= voucher.getLimiterLimit()) {
                    Messages.hit_voucher_limit.sendMessage(player);

                    return;
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
                if (this.twoAuth.containsKey(player.getUniqueId())) {
                    if (!this.twoAuth.get(player.getUniqueId()).equalsIgnoreCase(voucher.getName())) {
                        Messages.two_step_authentication.sendMessage(player);

                        this.twoAuth.put(player.getUniqueId(), voucher.getName());

                        return;
                    }
                } else {
                    Messages.two_step_authentication.sendMessage(player);

                    this.twoAuth.put(player.getUniqueId(), voucher.getName());

                    return;
                }
            }

            this.twoAuth.remove(player.getUniqueId());

            VoucherRedeemEvent event = new VoucherRedeemEvent(player, voucher, argument);
            this.plugin.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) voucherClick(player, item, voucher, argument);
        }
    }

    private ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    private boolean passesPermissionChecks(Player player, Voucher voucher, String argument) {
        populate(player, argument);

        if (!player.isOp()) {
            if (voucher.useWhiteListPermissions()) {
                for (String permission : voucher.getWhitelistPermissions()) {
                    if (!player.hasPermission(permission.toLowerCase().replace("{arg}", argument != null ? argument : "{arg}"))) {
                        this.placeholders.put("{permission}", permission);

                        player.sendMessage(Methods.replacePlaceholders(this.placeholders, voucher.getWhitelistPermissionMessage(), false));

                        for (String command : voucher.getWhitelistCommands()) {
                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(this.placeholders, command, false));
                        }

                        return false;
                    }
                }
            }

            if (voucher.usesWhitelistWorlds() && !voucher.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                player.sendMessage(Methods.replacePlaceholders(this.placeholders, voucher.getWhitelistWorldMessage(), false));

                for (String command : voucher.getWhitelistWorldCommands()) {
                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(this.placeholders, command, true));
                }

                return false;
            }

            if (voucher.useBlackListPermissions()) {
                for (String permission : voucher.getBlackListPermissions()) {
                    if (player.hasPermission(permission.toLowerCase().replace("{arg}", argument != null ? argument : "{arg}"))) {
                        this.placeholders.put("{permission}", permission);

                        player.sendMessage(Methods.replacePlaceholders(this.placeholders, voucher.getBlackListMessage(), false));

                        for (String command : voucher.getBlacklistCommands()) {
                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(this.placeholders, command, true));
                        }

                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void populate(Player player, String argument) {
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

        populate(player, argument);

        for (String command : voucher.getCommands()) {
            command = replacePlaceholders(command, player);

            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(this.placeholders, this.crazyManager.replaceRandom(command), true));
        }

        if (!voucher.getRandomCommands().isEmpty()) { // Picks a random command from the Random-Commands list.
            for (String command : voucher.getRandomCommands().get(getRandom(voucher.getRandomCommands().size())).getCommands()) {
                command = replacePlaceholders(command, player);

                plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(this.placeholders, this.crazyManager.replaceRandom(command), true));
            }
        }

        if (!voucher.getChanceCommands().isEmpty()) { // Picks a command based on the chance system of the Chance-Commands list.
            for (String command : voucher.getChanceCommands().get(getRandom(voucher.getChanceCommands().size())).getCommands()) {
                command = replacePlaceholders(command, player);

                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(this.placeholders, this.crazyManager.replaceRandom(command), true));
            }
        }

        for (ItemBuilder itemStack : voucher.getItems()) {
            if (!Methods.isInventoryFull(player)) {
                Methods.addItem(player, itemStack.build());
            } else {
                player.getWorld().dropItem(player.getLocation(), itemStack.build());
            }
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
            FileConfiguration configuration = Files.users.getConfiguration();

            configuration.set("Players." + player.getUniqueId() + ".UserName", player.getName());
            configuration.set("Players." + player.getUniqueId() + ".Vouchers." + voucher.getName(), configuration.getInt("Players." + player.getUniqueId() + ".Vouchers." + voucher.getName()) + 1);

            Files.users.save();
        }

        if (this.config.getProperty(ConfigKeys.dupe_protection)) {
            final PersistentDataContainerView view = item.getPersistentDataContainer();

            if (view.has(PersistentKeys.dupe_protection.getNamespacedKey())) {
                final String id = view.get(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING);

                FileConfiguration configuration = Files.data.getConfiguration();

                List<String> vouchers = new ArrayList<>(configuration.getStringList("Used-Vouchers"));

                if (!vouchers.contains(id)) {
                    vouchers.add(id);

                    configuration.set("Used-Vouchers", vouchers);

                    Files.data.save();
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
    
    private int getRandom(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }
}