package me.badbones69.vouchers.controllers;

import me.badbones69.vouchers.Methods;
import me.badbones69.vouchers.api.FileManager.Files;
import me.badbones69.vouchers.api.Vouchers;
import me.badbones69.vouchers.api.enums.Messages;
import me.badbones69.vouchers.api.enums.Version;
import me.badbones69.vouchers.api.events.RedeemVoucherEvent;
import me.badbones69.vouchers.api.objects.Voucher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class VoucherClick implements Listener {
    
    private HashMap<Player, String> twoAuth = new HashMap<>();
    
    @EventHandler
    public void onVoucherClick(PlayerInteractEvent e) {
        ItemStack item = getItemInHand(e.getPlayer());
        Player player = e.getPlayer();
        Action action = e.getAction();
        if (item != null && item.getType() != Material.AIR) {
            if (Version.getCurrentVersion().isNewer(Version.v1_8_R3)) {
                if (e.getHand() != EquipmentSlot.HAND) {
                    return;
                }
            }
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                Voucher voucher = Vouchers.getVoucherFromItem(item);
                if (voucher != null && !voucher.isEdible()) {
                    e.setCancelled(true);
                    useVoucher(player, voucher, item);
                }
            }
        }
    }
    
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        Voucher voucher = Vouchers.getVoucherFromItem(item);
        if (voucher != null && voucher.isEdible()) {
            Player player = e.getPlayer();
            e.setCancelled(true);
            if (item.getAmount() > 1) {
                player.sendMessage(Messages.UNSTACK_ITEM.getMessage());
            } else {
                useVoucher(player, voucher, item);
            }
        }
    }
    
    private void useVoucher(Player player, Voucher voucher, ItemStack item) {
        FileConfiguration data = Files.DATA.getFile();
        String argument = Vouchers.getArgument(item, voucher);
        if (passesPermissionChecks(player, item, voucher, argument)) {
            String uuid = player.getUniqueId().toString();
            if (!player.hasPermission("voucher.bypass")) {
                if (voucher.useLimiter()) {
                    if (data.contains("Players." + uuid)) {
                        if (data.contains("Players." + uuid + ".Vouchers." + voucher.getName())) {
                            int amount = data.getInt("Players." + uuid + ".Vouchers." + voucher.getName());
                            if (amount >= voucher.getLimiterLimit()) {
                                player.sendMessage(Messages.HIT_LIMIT.getMessage());
                                return;
                            }
                        }
                    }
                }
            }
            if (!voucher.isEdible()) {
                if (voucher.useTwoStepAuthentication() && !voucher.isEdible()) {
                    if (twoAuth.containsKey(player)) {
                        if (!twoAuth.get(player).equalsIgnoreCase(voucher.getName())) {
                            player.sendMessage(Messages.TWO_STEP_AUTHENTICATION.getMessage());
                            twoAuth.put(player, voucher.getName());
                            return;
                        }
                    } else {
                        player.sendMessage(Messages.TWO_STEP_AUTHENTICATION.getMessage());
                        twoAuth.put(player, voucher.getName());
                        return;
                    }
                }
            }
            twoAuth.remove(player);
            RedeemVoucherEvent event = new RedeemVoucherEvent(player, voucher, argument);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                voucherClick(player, item, voucher, argument);
            }
        }
    }
    
    private ItemStack getItemInHand(Player player) {
        if (Version.getCurrentVersion().isNewer(Version.v1_8_R3)) {
            return player.getInventory().getItemInMainHand();
        } else {
            return player.getItemInHand();
        }
    }
    
    private boolean passesPermissionChecks(Player player, ItemStack item, Voucher voucher, String argument) {
        if (!player.isOp()) {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("%Arg%", argument != null ? argument : "%arg%");
            placeholders.put("%Player%", player.getName());
            placeholders.put("%World%", player.getWorld().getName());
            placeholders.put("%X%", player.getLocation().getBlockX() + "");
            placeholders.put("%Y%", player.getLocation().getBlockY() + "");
            placeholders.put("%Z%", player.getLocation().getBlockZ() + "");
            if (voucher.useWhiteListPermissions()) {
                for (String permission : voucher.getWhitelistPermissions()) {
                    if (!player.hasPermission(permission.toLowerCase().replaceAll("%arg%", argument != null ? argument : "%arg%"))) {
                        player.sendMessage(Messages.NO_PERMISSION_TO_VOUCHER.getMessage(placeholders));
                        for (String command : voucher.getWhitelistCommands()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Messages.replacePlaceholders(placeholders, command));
                        }
                        return false;
                    }
                }
            }
            if (voucher.usesWhitelistWorlds()) {
                if (!voucher.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                    player.sendMessage(Methods.getPrefix(Messages.replacePlaceholders(placeholders, voucher.getWhitelistWorldMessage())));
                    for (String command : voucher.getWhitelistWorldCommands()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Messages.replacePlaceholders(placeholders, command));
                    }
                    return false;
                }
            }
            if (voucher.useBlackListPermissions()) {
                for (String permission : voucher.getBlackListPermissions()) {
                    if (player.hasPermission(permission.toLowerCase().replaceAll("%arg%", argument != null ? argument : "%arg%"))) {
                        player.sendMessage(Methods.getPrefix(Messages.replacePlaceholders(placeholders, voucher.getBlackListMessage())));
                        for (String command : voucher.getBlacklistCommands()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Messages.replacePlaceholders(placeholders, command));
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private void voucherClick(Player player, ItemStack item, Voucher voucher, String argument) {
        String name = player.getName();
        Methods.removeItem(item, player);
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%Arg%", argument != null ? argument : "%arg%");
        placeholders.put("%Player%", player.getName());
        placeholders.put("%World%", player.getWorld().getName());
        placeholders.put("%X%", player.getLocation().getBlockX() + "");
        placeholders.put("%Y%", player.getLocation().getBlockY() + "");
        placeholders.put("%Z%", player.getLocation().getBlockZ() + "");
        for (String command : voucher.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Messages.replacePlaceholders(placeholders, command));
        }
        if (voucher.getRandomCoammnds().size() >= 1) {// Picks a random command from the Random-Commands list.
            for (String command : voucher.getRandomCoammnds().get(new Random().nextInt(voucher.getRandomCoammnds().size())).getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Messages.replacePlaceholders(placeholders, command));
            }
        }
        if (voucher.getChanceCommands().size() >= 1) {// Picks a command based on the chance system of the Chance-Commands list.
            for (String command : voucher.getChanceCommands().get(new Random().nextInt(voucher.getChanceCommands().size())).getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Messages.replacePlaceholders(placeholders, command));
            }
        }
        for (ItemStack it : voucher.getItems()) {
            if (!Methods.isInventoryFull(player)) {
                player.getInventory().addItem(it);
            } else {
                player.getWorld().dropItem(player.getLocation(), it);
            }
        }
        if (voucher.playSounds()) {
            for (Sound sound : voucher.getSounds()) {
                player.playSound(player.getLocation(), sound, 1, 1);
            }
        }
        if (voucher.useFirework()) {
            Methods.fireWork(player.getLocation(), voucher.getFireworkColors());
        }
        if (!voucher.getVoucherUsedMessage().equals("")) {
            player.sendMessage(Methods.getPrefix(Messages.replacePlaceholders(placeholders, voucher.getVoucherUsedMessage())));
        }
        if (voucher.useLimiter()) {
            Files.DATA.getFile().set("Players." + player.getUniqueId() + ".UserName", player.getName());
            Files.DATA.getFile().set("Players." + player.getUniqueId() + ".Vouchers." + voucher.getName(), Files.DATA.getFile().getInt("Players." + player.getUniqueId() + ".Vouchers." + voucher.getName()) + 1);
            Files.DATA.saveFile();
        }
    }
    
}