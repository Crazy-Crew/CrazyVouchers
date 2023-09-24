package com.badbones69.crazyvouchers.paper.listeners;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import com.badbones69.crazyvouchers.paper.Methods;
import com.badbones69.crazyvouchers.paper.api.CrazyManager;
import com.badbones69.crazyvouchers.paper.api.FileManager.Files;
import com.badbones69.crazyvouchers.paper.api.enums.Translation;
import com.badbones69.crazyvouchers.paper.api.events.RedeemVoucherEvent;
import com.badbones69.crazyvouchers.paper.api.objects.ItemBuilder;
import com.badbones69.crazyvouchers.paper.api.objects.Voucher;
import com.badbones69.crazyvouchers.paper.support.PluginSupport;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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
import org.bukkit.plugin.java.JavaPlugin;
import us.crazycrew.crazyenvoys.common.config.types.Config;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoucherClickListener implements Listener {
    
    private final CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);
    
    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final Methods methods = this.plugin.getMethods();
    
    private final HashMap<UUID, String> twoAuth = new HashMap<>();

    private final HashMap<String, String> placeholders = new HashMap<>();
    
    // This must run as highest, so it doesn't cause other plugins to check
    // the items that were added to the players inventory and replaced the item in the player's hand.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoucherClick(PlayerInteractEvent e) {
        ItemStack item = getItemInHand(e.getPlayer());
        Player player = e.getPlayer();
        Action action = e.getAction();

        if (e.getHand() == EquipmentSlot.OFF_HAND && e.getHand() != null) {
            Voucher voucher = this.crazyManager.getVoucherFromItem(player.getInventory().getItemInOffHand());

            if (voucher != null && !voucher.isEdible()) {
                e.setCancelled(true);
                Translation.no_permission_to_use_voucher_offhand.sendMessage(player);
            }

            return;
        }

        if (item.getType() != Material.AIR) {
            if (e.getHand() != EquipmentSlot.HAND) return;

            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                Voucher voucher = this.crazyManager.getVoucherFromItem(item);

                if (voucher != null && !voucher.isEdible()) {
                    e.setCancelled(true);
                    useVoucher(player, voucher, item);
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        Voucher voucher = this.crazyManager.getVoucherFromItem(item);

        if (voucher != null && voucher.isEdible()) {
            Player player = e.getPlayer();
            e.setCancelled(true);
            
            if (item.getAmount() > 1) {
                Translation.unstack_item.sendMessage(player);
            } else {
                useVoucher(player, voucher, item);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onArmorStandClick(PlayerInteractEntityEvent e) {
        if (e.getHand() == EquipmentSlot.HAND && this.crazyManager.getVoucherFromItem(getItemInHand(e.getPlayer())) != null) e.setCancelled(true);
    }
    
    private void useVoucher(Player player, Voucher voucher, ItemStack item) {
        FileConfiguration data = Files.users.getFile();
        String argument = this.crazyManager.getArgument(item, voucher);

        if (player.getGameMode() == GameMode.CREATIVE && this.plugin.getCrazyHandler().getConfigManager().getConfig().getProperty(Config.must_be_in_survival)) {
            Translation.survival_mode.sendMessage(player);
            return;
        }

        if (passesPermissionChecks(player, voucher, argument)) {
            String uuid = player.getUniqueId().toString();

            if (!player.hasPermission("voucher.bypass") && voucher.useLimiter() && data.contains("Players." + uuid + ".Vouchers." + voucher.getName())) {
                int amount = data.getInt("Players." + uuid + ".Vouchers." + voucher.getName());

                if (amount >= voucher.getLimiterLimit()) {
                    Translation.hit_voucher_limit.sendMessage(player);
                    return;
                }
            }

            if (PluginSupport.PLACEHOLDERAPI.isPluginEnabled()) {
                AtomicBoolean shouldCancel = new AtomicBoolean(false);
                voucher.getRequiredPlaceholders().forEach((placeholder, value) -> {
                    String newValue = PlaceholderAPI.setPlaceholders(player, placeholder);
                    if (!newValue.equals(value)) {
                        String message = replacePlaceholders(voucher.getRequiredPlaceholdersMessage(), player);
                        player.sendMessage(this.methods.replacePlaceholders(this.placeholders, message));
                        shouldCancel.set(true);
                    }
                });

                if (shouldCancel.get()) return;
            }

            if (!voucher.isEdible() && voucher.useTwoStepAuthentication()) {
                if (this.twoAuth.containsKey(player.getUniqueId())) {
                    if (!this.twoAuth.get(player.getUniqueId()).equalsIgnoreCase(voucher.getName())) {
                        Translation.two_step_authentication.sendMessage(player);
                        this.twoAuth.put(player.getUniqueId(), voucher.getName());
                        return;
                    }
                } else {
                    Translation.two_step_authentication.sendMessage(player);
                    this.twoAuth.put(player.getUniqueId(), voucher.getName());
                    return;
                }
            }

            this.twoAuth.remove(player.getUniqueId());
            RedeemVoucherEvent event = new RedeemVoucherEvent(player, voucher, argument);
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
                        player.sendMessage(this.methods.replacePlaceholders(this.placeholders, voucher.getWhitelistPermissionMessage()));

                        for (String command : voucher.getWhitelistCommands()) {
                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(this.placeholders, command));
                        }

                        return false;
                    }
                }
            }

            if (voucher.usesWhitelistWorlds() && !voucher.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                player.sendMessage(this.methods.replacePlaceholders(this.placeholders, voucher.getWhitelistWorldMessage()));

                for (String command : voucher.getWhitelistWorldCommands()) {
                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(this.placeholders, command));
                }

                return false;
            }

            if (voucher.useBlackListPermissions()) {
                for (String permission : voucher.getBlackListPermissions()) {
                    if (player.hasPermission(permission.toLowerCase().replace("{arg}", argument != null ? argument : "{arg}"))) {
                        player.sendMessage(this.methods.replacePlaceholders(this.placeholders, voucher.getBlackListMessage()));

                        for (String command : voucher.getBlacklistCommands()) {
                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(this.placeholders, command));
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
        this.placeholders.put("{x}", String.valueOf(player.getLocation().getBlockX()));
        this.placeholders.put("{y}", String.valueOf(player.getLocation().getBlockY()));
        this.placeholders.put("{z}", String.valueOf(player.getLocation().getBlockZ()));
        this.placeholders.put("{prefix}", this.plugin.getCrazyHandler().getConfigManager().getConfig().getProperty(Config.command_prefix));
    }

    private void voucherClick(Player player, ItemStack item, Voucher voucher, String argument) {
        this.methods.removeItem(item, player);

        populate(player, argument);

        for (String command : voucher.getCommands()) {
            command = replacePlaceholders(command, player);
            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(this.placeholders, this.crazyManager.replaceRandom(command)));
        }

        if (!voucher.getRandomCommands().isEmpty()) { // Picks a random command from the Random-Commands list.
            for (String command : voucher.getRandomCommands().get(getRandom(voucher.getRandomCommands().size())).getCommands()) {
                command = replacePlaceholders(command, player);
                plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(this.placeholders, this.crazyManager.replaceRandom(command)));
            }
        }

        if (!voucher.getChanceCommands().isEmpty()) { // Picks a command based on the chance system of the Chance-Commands list.
            for (String command : voucher.getChanceCommands().get(getRandom(voucher.getChanceCommands().size())).getCommands()) {
                command = replacePlaceholders(command, player);
                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(this.placeholders, this.crazyManager.replaceRandom(command)));
            }
        }

        for (ItemBuilder itemStack : voucher.getItems()) {
            if (!this.methods.isInventoryFull(player)) {
                player.getInventory().addItem(itemStack.build());
            } else {
                player.getWorld().dropItem(player.getLocation(), itemStack.build());
            }
        }

        if (voucher.playSounds()) {
            for (Sound sound : voucher.getSounds()) {
                player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, voucher.getVolume(), voucher.getPitch());
            }
        }

        if (voucher.useFirework()) this.methods.fireWork(player.getLocation(), voucher.getFireworkColors());

        if (!voucher.getVoucherUsedMessage().isEmpty()) {
            String message = replacePlaceholders(voucher.getVoucherUsedMessage(), player);
            player.sendMessage(this.methods.replacePlaceholders(this.placeholders, message));
        }

        if (voucher.useLimiter()) {
            Files.users.getFile().set("Players." + player.getUniqueId() + ".UserName", player.getName());
            Files.users.getFile().set("Players." + player.getUniqueId() + ".Vouchers." + voucher.getName(), Files.users.getFile().getInt("Players." + player.getUniqueId() + ".Vouchers." + voucher.getName()) + 1);
            Files.users.saveFile();
        }
    }

    private String replacePlaceholders(String string, Player player) {
        if (PluginSupport.PLACEHOLDERAPI.isPluginEnabled()) return PlaceholderAPI.setPlaceholders(player, string);

        return string;
    }
    
    private int getRandom(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }
}