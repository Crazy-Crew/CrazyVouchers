package me.badbones69.vouchers;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.badbones69.vouchers.api.Vouchers;
import me.badbones69.vouchers.controlers.GUI;
import me.badbones69.vouchers.controlers.VoucherClick;

public class Main extends JavaPlugin implements Listener{
	
	public static SettingsManager settings = SettingsManager.getInstance();
	
	@Override
	public void onEnable() {
		settings.setup(this);
		if(!settings.getData().contains("Players")){
			settings.getData().set("Players.Clear", null);
			settings.saveData();
		}
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(new VoucherClick(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new GUI(), this);
		Vouchers.onLoad();
		try {
			Metrics metrics = new Metrics(this); metrics.start();
		} catch (IOException e) { // Failed to submit the stats :-(
			System.out.println("Error Submitting stats!");
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLable, String[] args){
		if(commandLable.equalsIgnoreCase("Voucher")||commandLable.equalsIgnoreCase("Vouch")){
			if(args.length == 0){
				Bukkit.dispatchCommand(sender, "voucher help");
				return true;
			}
			if(args.length>=1){
				if(args[0].equalsIgnoreCase("Help")){
					if(!Methods.hasPermission(sender, "Access"))return true;
					sender.sendMessage(Methods.color("&8- &6/Voucher Help &3Lists all the commands for vouchers."));
					sender.sendMessage(Methods.color("&8- &6/Voucher Types &3Lists all types of vouchers and codes."));
					sender.sendMessage(Methods.color("&8- &6/Voucher Redeem <Code> &3Allows player to redeem a voucher code."));
					sender.sendMessage(Methods.color("&8- &6/Voucher Give <Type> [Amount] [Player] [Arguments] &3Gives a player a voucher."));
					sender.sendMessage(Methods.color("&8- &6/Voucher GiveAll <Type> [Amount] [Arguments] &3Gives all players a voucher."));
					sender.sendMessage(Methods.color("&8- &6/Voucher Open &3Opens a GUI so you can get vouchers easy."));
					sender.sendMessage(Methods.color("&8- &6/Voucher Reload &3Reloadeds the config.yml."));
					return true;
				}
				if(args[0].equalsIgnoreCase("Open")){
					if(!Methods.hasPermission(sender, "Admin"))return true;
					GUI.openGUI((Player) sender);
					return true;
				}
				if(args[0].equalsIgnoreCase("Types")||args[0].equalsIgnoreCase("List")){
					if(!Methods.hasPermission(sender, "Admin"))return true;
					String voucher = "";
					String codes = "";
					for(String vo : settings.getConfig().getConfigurationSection("Vouchers").getKeys(false)){
						voucher += Methods.color("&a"+vo+"&8, ");
					}
					for(String co : settings.getCode().getConfigurationSection("Codes").getKeys(false)){
						codes += Methods.color("&a"+co+"&8, ");
					}
					voucher = voucher.substring(0, voucher.length()-2);
					codes = codes.substring(0, codes.length()-2);
					sender.sendMessage(Methods.color("&e&lVouchers:&f "+voucher));
					sender.sendMessage(Methods.color("&e&lVoucher Codes:&f "+codes));
					return true;
				}
				if(args[0].equalsIgnoreCase("Reload")){
					if(!Methods.hasPermission(sender, "Admin"))return true;
					settings.reloadConfig();
					settings.reloadData();
					settings.reloadCode();
					settings.setup(this);
					if(!settings.getData().contains("Players")){
						settings.getData().set("Players.Clear", null);
						settings.saveData();
					}
					Vouchers.onLoad();
					sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Config-Reload")));
					return true;
				}
				if(args[0].equalsIgnoreCase("Redeem")){
					if(!Methods.hasPermission(sender, "Redeem"))return true;
					if(args.length>=2){
						String code = args[1];
						if(!(sender instanceof Player)){
							sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Not-A-Player")));
							return true;
						}
						Player player = (Player) sender;
						if(!Methods.isRealCode(player, code))return true;
						if(!Methods.isCodeEnabled(player, code))return true;
						if(!Methods.hasCodePerm(player, code))return true;
						Methods.codeRedeem(player, code);
						return true;
					}
					sender.sendMessage(Methods.getPrefix() + Methods.color("&c/Voucher Redeem <Code>"));
					return true;
				}
				if(args[0].equalsIgnoreCase("Give")){// /Voucher 0Give 1<Type> 2[Amount] 3[Player] 4[Arguments]
					if(!Methods.hasPermission(sender, "Admin"))return true;
					if(args.length==1){
						if(!(sender instanceof Player)){
							sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Not-A-Player")));
							return true;
						}
					}
					if(args.length>1){
						String name = sender.getName();
						String voucher = args[1];
						if(!Vouchers.isVoucher(voucher)){
							sender.sendMessage(Methods.getPrefix() + Methods.color(Main.settings.getMsgs().getString("Messages.Not-A-Voucher")));
							return true;
						}
						voucher = Vouchers.getVoucherName(voucher);
						int amount = 1;
						if(args.length>=3){
							if(!Methods.isInt(sender, args[2]))return true;
							amount = Integer.parseInt(args[2]);
						}
						if(args.length>=4){
							name = args[3];
							if(!Methods.isOnline(sender, name))return true;
						}
						Player player = Bukkit.getPlayer(name);
						if(args.length >= 5){
							player.getInventory().addItem(Vouchers.getVoucher(voucher, args[4], amount));
						}else{
							player.getInventory().addItem(Vouchers.getVoucher(voucher, amount));
						}
						player.updateInventory();
						sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Given-A-Voucher")
								.replace("%Player%", player.getName()).replace("%player%", player.getName())
								.replace("%Voucher%", voucher).replace("%voucher%", voucher)));
						return true;
					}
					sender.sendMessage(Methods.getPrefix() + Methods.color("&c/Voucher Give <Type> [Amount] [Player] [Arguments]"));
					return true;
				}
				if(args[0].equalsIgnoreCase("GiveAll")){// /Voucher 0GiveAll 1<Type> 2[Amount] 3[Arguments]
					if(!Methods.hasPermission(sender, "Admin"))return true;
					if(args.length==1){
						if(!(sender instanceof Player)){
							sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Not=A-Player")));
							return true;
						}
					}
					if(args.length>1){
						String voucher = args[1];
						if(!Vouchers.isVoucher(voucher)){
							sender.sendMessage(Methods.getPrefix() + Methods.color(Main.settings.getMsgs().getString("Messages.Not-A-Voucher")));
							return true;
						}
						voucher = Vouchers.getVoucherName(voucher);
						int amount = 1;
						if(args.length >= 3){
							if(!Methods.isInt(sender, args[2]))return true;
							amount = Integer.parseInt(args[2]);
						}
						for(Player player : Bukkit.getServer().getOnlinePlayers()){
							if(args.length >= 4){
								player.getInventory().addItem(Vouchers.getVoucher(voucher, args[3], amount));
							}else{
								player.getInventory().addItem(Vouchers.getVoucher(voucher, amount));
							}
							player.updateInventory();
						}
						sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Given-All-Players-Voucher")
								.replace("%Voucher%", voucher).replace("%voucher%", voucher)));
						return true;
					}
					sender.sendMessage(Methods.getPrefix() + Methods.color("&c/Voucher GiveAll <Type> [Amount] [Arguments]"));
					return true;
				}
			}
			sender.sendMessage(Methods.getPrefix() + Methods.color("&cPlease do /Voucher Help for more Information."));
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		final Player player = e.getPlayer();
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			@Override
			public void run() {
				if(player.getName().equals("BadBones69")){
					player.sendMessage(Methods.color("&8[&bVouchers&8]: "+"&7This server is running your Vouchers Plugin. "
							+ "&7It is running version &av"+Bukkit.getServer().getPluginManager().getPlugin("Vouchers").getDescription().getVersion()+"&7."));
				}
				if(player.isOp()){
					Methods.hasUpdate(player);
				}
			}
		}, 1*20);
	}
	
}