package me.badbones69.vouchers.controlers;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.badbones69.vouchers.Main;
import me.badbones69.vouchers.Methods;
import me.badbones69.vouchers.api.Version;
import me.badbones69.vouchers.api.Voucher;
import me.badbones69.vouchers.api.Vouchers;

public class VoucherClick implements Listener {
	
	private HashMap<Player, String> twoAuth = new HashMap<Player, String>();
	
	@EventHandler
	public void onVoucherClick(PlayerInteractEvent e) {
		ItemStack item = getItemInHand(e.getPlayer());
		Player player = e.getPlayer();
		Action action = e.getAction();
		FileConfiguration data = Main.settings.getData();
		if(Version.getVersion().getVersionInteger() >= Version.v1_9_R1.getVersionInteger()) {
			if(e.getHand() != EquipmentSlot.HAND) {
				return;
			}
		}
		if(action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
			Voucher voucher = Vouchers.getVoucherFromItem(item);
			if(voucher != null) {
				e.setCancelled(true);
				if(passesPermissionChecks(player, voucher)) {
					String uuid = player.getUniqueId().toString();
					if(!player.hasPermission("Voucher.Bypass")) {
						if(voucher.useLimiter()) {
							if(data.contains("Players." + uuid)) {
								if(data.contains("Players." + uuid + ".Vouchers." + voucher.getName())) {
									int amount = data.getInt("Players." + uuid + ".Vouchers." + voucher.getName());
									if(amount >= voucher.getLimiterLimit()) {
										player.sendMessage(Methods.getPrefix() + Methods.color(Main.settings.getMsgs().getString("Messages.Hit-Limit")));
										return;
									}
								}
							}
						}
					}
					if(voucher.useTwoStepAuthentication()) {
						if(twoAuth.containsKey(player)) {
							if(!twoAuth.get(player).equalsIgnoreCase(voucher.getName())) {
								player.sendMessage(Methods.getPrefix() + Methods.color(Main.settings.getMsgs().getString("Messages.Two-Step-Authentication")));
								twoAuth.put(player, voucher.getName());
								return;
							}
						}else {
							player.sendMessage(Methods.getPrefix() + Methods.color(Main.settings.getMsgs().getString("Messages.Two-Step-Authentication")));
							twoAuth.put(player, voucher.getName());
							return;
						}
					}
					voucherClick(player, item, voucher);
					if(twoAuth.containsKey(player)) {
						twoAuth.remove(player);
					}
					return;
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private ItemStack getItemInHand(Player player) {
		if(Version.getVersion().getVersionInteger() >= Version.v1_9_R1.getVersionInteger()) {
			return player.getInventory().getItemInMainHand();
		}else {
			return player.getItemInHand();
		}
	}
	
	private boolean passesPermissionChecks(Player player, Voucher voucher) {
		Boolean checker = true;
		if(!player.isOp()) {
			if(!player.hasPermission(voucher.getWhiteListPermission()) && voucher.useWhiteListPermissions()) {
				player.sendMessage(Methods.color(Methods.getPrefix() + Main.settings.getMsgs().getString("Messages.No-Permission-To-Voucher")));
				checker = false;
			}
			if(checker) {
				if(voucher.useBlackListPermissions()) {
					for(String permission : voucher.getBlackListPermissions()) {
						if(player.hasPermission(permission.toLowerCase())) {
							player.sendMessage(Methods.color(Methods.getPrefix() + voucher.getBlackListMessage()));
							checker = false;
							break;
						}
					}
				}
			}
		}
		return checker;
	}
	
	private void voucherClick(Player player, ItemStack item, Voucher voucher) {
		String name = player.getName();
		String argument = Vouchers.getArgument(item, voucher);
		if(argument == null) {
			argument = "";
		}
		Methods.removeItem(item, player);
		for(String cmd : voucher.getCommands()) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%Player%", name).replaceAll("%player%", name)
			.replaceAll("%Arg%", argument).replaceAll("%arg%", argument)
			.replaceAll("%World%", player.getWorld().getName()).replaceAll("%world%", player.getWorld().getName())
			.replaceAll("%X%", player.getLocation().getBlockX() + "").replaceAll("%x%", player.getLocation().getBlockX() + "")
			.replaceAll("%Y%", player.getLocation().getBlockY() + "").replaceAll("%y%", player.getLocation().getBlockY() + "")
			.replaceAll("%Z%", player.getLocation().getBlockZ() + "").replaceAll("%z%", player.getLocation().getBlockZ() + ""));
		}
		for(ItemStack it : voucher.getItems()) {
			if(!Methods.isInvFull(player)) {
				player.getInventory().addItem(it);
			}else {
				player.getWorld().dropItem(player.getLocation(), it);
			}
		}
		if(voucher.playSounds()) {
			for(Sound sound : voucher.getSounds()) {
				player.playSound(player.getLocation(), sound, 1, 1);
			}
		}
		if(voucher.useFirework()) {
			Methods.fireWork(player.getLocation(), voucher.getFireworkColors());
		}
		String msg = voucher.getVoucherUsedMessage().replaceAll("%Player%", name).replaceAll("%player%", name)
		.replaceAll("%Arg%", argument).replaceAll("%arg%", argument)
		.replaceAll("%World%", player.getWorld().getName()).replaceAll("%world%", player.getWorld().getName())
		.replaceAll("%X%", player.getLocation().getBlockX() + "").replaceAll("%x%", player.getLocation().getBlockX() + "")
		.replaceAll("%Y%", player.getLocation().getBlockY() + "").replaceAll("%y%", player.getLocation().getBlockY() + "")
		.replaceAll("%Z%", player.getLocation().getBlockZ() + "").replaceAll("%z%", player.getLocation().getBlockZ() + "");
		if(!msg.equals("")) {
			player.sendMessage(Methods.getPrefix() + Methods.color(msg));
		}
		int amount = 0;
		if(Main.settings.getData().contains("Players." + player.getUniqueId() + ".Vouchers." + voucher.getName())) {
			amount = Main.settings.getData().getInt("Players." + player.getUniqueId() + ".Vouchers." + voucher.getName());
		}
		amount++;
		Main.settings.getData().set("Players." + player.getUniqueId() + ".UserName", player.getName());
		Main.settings.getData().set("Players." + player.getUniqueId() + ".Vouchers." + voucher.getName(), amount);
		Main.settings.saveData();
	}
	
}