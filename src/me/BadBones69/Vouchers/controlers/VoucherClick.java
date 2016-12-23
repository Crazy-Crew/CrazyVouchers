package me.BadBones69.Vouchers.controlers;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.BadBones69.Vouchers.Main;
import me.BadBones69.Vouchers.Methods;
import me.BadBones69.Vouchers.API.Vouchers;

public class VoucherClick implements Listener{
	
	private HashMap<Player, String> twoAuth = new HashMap<Player, String>();
	
	@EventHandler
	public void onVoucherClick(PlayerInteractEvent e){
		ItemStack item = e.getItem();
		Player player = e.getPlayer();
		Action action = e.getAction();
		FileConfiguration data = Main.settings.getData();
		FileConfiguration config = Main.settings.getConfig();
		if(action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR){
			if(item==null)return;
			if(item.hasItemMeta()){
				if(item.getItemMeta().hasDisplayName()&&item.getItemMeta().hasLore()){
					for(String voucher : Vouchers.getVouchers()){
						if(Vouchers.hasVoucherItemName(item, voucher) || item.getItemMeta().getDisplayName().equalsIgnoreCase(Vouchers.getVoucher(voucher).getItemMeta().getDisplayName())){
							e.setCancelled(true);
							String id = config.getString("Vouchers." + voucher + ".Item");
							ItemStack i = Methods.makeItem(id, 1);
							if(item.getType() == i.getType()){
								if(Methods.perVoucherPerm(player, Vouchers.getPermissionNode(voucher), Vouchers.isPermissionEnabled(voucher))){
									String uuid = player.getUniqueId().toString();
									if(!player.hasPermission("Voucher.Bypass")){
										if(Vouchers.isLimiterEnabled(voucher)){
											if(data.contains("Players." + uuid)){
												if(data.contains("Players." + uuid + ".Vouchers." + voucher)){
													int amount = data.getInt("Players." + uuid + ".Vouchers." + voucher);
													if(amount >= Vouchers.getLimiter(voucher)){
														player.sendMessage(Methods.getPrefix() + Methods.color(Main.settings.getMsgs().getString("Messages.Hit-Limit")));
														return;
													}
												}
											}
										}
									}
									if(config.getBoolean("Vouchers." + voucher + ".Options.Two-Step-Authentication.Toggle")){
										if(twoAuth.containsKey(player)){
											if(!twoAuth.get(player).equalsIgnoreCase(voucher)){
												player.sendMessage(Methods.getPrefix() + Methods.color(Main.settings.getMsgs().getString("Messages.Two-Step-Authentication")));
												twoAuth.put(player, voucher);
												return;
											}
										}else{
											player.sendMessage(Methods.getPrefix() + Methods.color(Main.settings.getMsgs().getString("Messages.Two-Step-Authentication")));
											twoAuth.put(player, voucher);
											return;
										}
									}
									voucherClick(player, item, voucher);
									if(twoAuth.containsKey(player)){
										twoAuth.remove(player);
									}
									return;
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void voucherClick(Player player, ItemStack item, String voucher){
		List<String> lore = item.getItemMeta().getLore();
		List<String> L = Main.settings.getConfig().getStringList("Vouchers." + voucher + ".Lore");
		String name = player.getName();
		String argument = "";
		if(Vouchers.hasVoucherItemName(item, voucher)){
			argument = Vouchers.getVoucherArgumentItemName(item, voucher);
		}
		if(argument == ""){
			int i = 0;
			for(String l : L){
				l = Methods.color(l);
				l = Methods.Args(l);
				String lo = lore.get(i);
				lo = Methods.Args(lo);
				if(l.contains("%Arg%")){
					String[] b = l.split("%Arg%");
					if(b.length>=1)argument = lo.replace(b[0], "");
					if(b.length>=2)argument = argument.replace(b[1], "");
				}
				i++;
			}
		}
		Methods.removeItem(item, player);
		for(String cmd : Vouchers.getCommands(voucher, player, argument)){
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
		}
		for(ItemStack it : Vouchers.getItems(voucher)){
			if(!Methods.isInvFull(player)){
				player.getInventory().addItem(it);
			}else{
				player.getWorld().dropItem(player.getLocation(), it);
			}
		}
		if(Vouchers.isSoundEnabled(voucher)){
			player.playSound(player.getLocation(), Vouchers.getSound(voucher), 1, 1);
		}
		if(Vouchers.isFireworkEnabled(voucher)){
			Methods.fireWork(player.getLocation(), Vouchers.getFireworkColors(voucher));
		}
		String msg = Main.settings.getConfig().getString("Vouchers." + voucher + ".Options.Message");
		msg = msg.replaceAll("%Player%", name).replaceAll("%player%", name)
				.replaceAll("%Arg%", argument).replaceAll("%arg%", argument);
		if(!msg.equals("")){
			player.sendMessage(Methods.getPrefix() + Methods.color(msg));
		}
		int amount = 0;
		if(Main.settings.getData().contains("Players."+player.getUniqueId()+".Vouchers." + voucher)){
			amount = Main.settings.getData().getInt("Players."+player.getUniqueId()+".Vouchers." + voucher);
		}
		amount = amount+1;
		Main.settings.getData().set("Players."+player.getUniqueId()+".UserName",player.getName());
		Main.settings.getData().set("Players."+player.getUniqueId()+".Vouchers." + voucher, amount);
		Main.settings.saveData();
	}
	
}