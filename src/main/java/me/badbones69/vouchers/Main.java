package me.badbones69.vouchers;

import com.massivestats.MassiveStats;
import me.badbones69.vouchers.api.FileManager;
import me.badbones69.vouchers.api.FileManager.Files;
import me.badbones69.vouchers.api.Vouchers;
import me.badbones69.vouchers.api.enums.Messages;
import me.badbones69.vouchers.api.enums.Version;
import me.badbones69.vouchers.api.objects.Voucher;
import me.badbones69.vouchers.controlers.FireworkDamageAPI;
import me.badbones69.vouchers.controlers.GUI;
import me.badbones69.vouchers.controlers.VoucherClick;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class Main extends JavaPlugin implements Listener {
	
	private FileManager fileManager = FileManager.getInstance();
	
	@Override
	public void onEnable() {
		fileManager.logInfo(true).setup(this);
		if(!Files.DATA.getFile().contains("Players")) {
			Files.DATA.getFile().set("Players.Clear", null);
			Files.DATA.saveFile();
		}
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(new VoucherClick(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new GUI(), this);
		try {
			if(Version.getCurrentVersion().comparedTo(Version.v1_11_R1) >= 0) {
				Bukkit.getServer().getPluginManager().registerEvents(new FireworkDamageAPI(this), this);
			}
		}catch(Exception e) {
		}
		Vouchers.load();
		try {
			MassiveStats massiveStats = new MassiveStats(this);
			if(Files.CONFIG.getFile().contains("Settings.Updater")) {
				massiveStats.setListenerDisabled(!Files.CONFIG.getFile().getBoolean("Settings.Updater"));
			}
		}catch(Exception e) {
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLable, String[] args) {
		if(commandLable.equalsIgnoreCase("Voucher") || commandLable.equalsIgnoreCase("Vouch")) {
			if(args.length == 0) {
				Bukkit.dispatchCommand(sender, "voucher help");
				return true;
			}else {
				if(args[0].equalsIgnoreCase("Help")) {
					if(!Methods.hasPermission(sender, "Access")) return true;
					sender.sendMessage(Messages.HELP.getMessageNoPrefix());
					return true;
				}
				if(args[0].equalsIgnoreCase("Open")) {
					if(!Methods.hasPermission(sender, "Admin")) return true;
					if(args.length >= 2) {
						if(Methods.isInt(args[1])) {
							GUI.openGUI((Player) sender, Integer.parseInt(args[1]));
							return true;
						}
					}
					GUI.openGUI((Player) sender, 1);
					return true;
				}
				if(args[0].equalsIgnoreCase("Types") || args[0].equalsIgnoreCase("List")) {
					if(!Methods.hasPermission(sender, "Admin")) return true;
					String voucher = "";
					String codes = "";
					for(String vo : Files.CONFIG.getFile().getConfigurationSection("Vouchers").getKeys(false)) {
						voucher += Methods.color("&a" + vo + "&8, ");
					}
					for(String co : Files.VOUCHER_CODES.getFile().getConfigurationSection("Codes").getKeys(false)) {
						codes += Methods.color("&a" + co + "&8, ");
					}
					voucher = voucher.substring(0, voucher.length() - 2);
					codes = codes.substring(0, codes.length() - 2);
					sender.sendMessage(Methods.color("&e&lVouchers:&f " + voucher));
					sender.sendMessage(Methods.color("&e&lVoucher Codes:&f " + codes));
					return true;
				}
				if(args[0].equalsIgnoreCase("Reload")) {
					if(!Methods.hasPermission(sender, "Admin")) return true;
					Files.CONFIG.relaodFile();
					Files.DATA.relaodFile();
					Files.MESSAGES.relaodFile();
					Files.VOUCHER_CODES.relaodFile();
					fileManager.setup(this);
					if(!Files.DATA.getFile().contains("Players")) {
						Files.DATA.getFile().set("Players.Clear", null);
						Files.DATA.saveFile();
					}
					Vouchers.load();
					sender.sendMessage(Messages.RELOAD.getMessage());
					return true;
				}
				if(args[0].equalsIgnoreCase("Redeem")) {
					if(!Methods.hasPermission(sender, "Redeem")) return true;
					if(args.length >= 2) {
						String code = args[1];
						if(!(sender instanceof Player)) {
							sender.sendMessage(Messages.NOT_A_PLAYER.getMessage());
							return true;
						}
						Player player = (Player) sender;
						if(!Methods.isRealCode(player, code)) return true;
						if(!Methods.isCodeEnabled(player, code)) return true;
						if(!Methods.hasCodePerm(player, code)) return true;
						Methods.codeRedeem(player, code);
						return true;
					}
					sender.sendMessage(Methods.getPrefix() + Methods.color("&c/Voucher Redeem <Code>"));
					return true;
				}
				if(args[0].equalsIgnoreCase("Give")) {// /Voucher 0Give 1<Type> 2[Amount] 3[Player] 4[Arguments]
					if(!Methods.hasPermission(sender, "Admin")) return true;
					if(args.length == 1) {
						if(!(sender instanceof Player)) {
							sender.sendMessage(Messages.NOT_A_PLAYER.getMessage());
							return true;
						}
					}
					if(args.length > 1) {
						String name = sender.getName();
						if(!Vouchers.isVoucherName(args[1])) {
							sender.sendMessage(Messages.NOT_A_VOUCHER.getMessage());
							return true;
						}
						Voucher voucher = Vouchers.getVoucher(args[1]);
						int amount = 1;
						if(args.length >= 3) {
							if(!Methods.isInt(sender, args[2])) return true;
							amount = Integer.parseInt(args[2]);
						}
						if(args.length >= 4) {
							name = args[3];
							if(!Methods.isOnline(sender, name)) return true;
						}
						Player player = Bukkit.getPlayer(name);
						if(args.length >= 5) {
							player.getInventory().addItem(voucher.buildItem(args[4], amount));
						}else {
							player.getInventory().addItem(voucher.buildItem(amount));
						}
						player.updateInventory();
						HashMap<String, String> placeholders = new HashMap<>();
						placeholders.put("%Player%", player.getName());
						placeholders.put("%player%", player.getName());
						placeholders.put("%Voucher%", voucher.getName());
						placeholders.put("%voucher%", voucher.getName());
						sender.sendMessage(Messages.GIVEN_A_VOUCHER.getMessage(placeholders));
						return true;
					}
					sender.sendMessage(Methods.getPrefix() + Methods.color("&c/Voucher Give <Type> [Amount] [Player] [Arguments]"));
					return true;
				}
				if(args[0].equalsIgnoreCase("GiveAll")) {// /Voucher 0GiveAll 1<Type> 2[Amount] 3[Arguments]
					if(!Methods.hasPermission(sender, "Admin")) return true;
					if(args.length == 1) {
						if(!(sender instanceof Player)) {
							sender.sendMessage(Messages.NOT_A_PLAYER.getMessage());
							return true;
						}
					}
					if(args.length > 1) {
						if(!Vouchers.isVoucherName(args[1])) {
							sender.sendMessage(Messages.NOT_A_VOUCHER.getMessage());
							return true;
						}
						Voucher voucher = Vouchers.getVoucher(args[1]);
						int amount = 1;
						if(args.length >= 3) {
							if(!Methods.isInt(sender, args[2])) return true;
							amount = Integer.parseInt(args[2]);
						}
						for(Player player : Bukkit.getServer().getOnlinePlayers()) {
							if(args.length >= 4) {
								player.getInventory().addItem(voucher.buildItem(args[3], amount));
							}else {
								player.getInventory().addItem(voucher.buildItem(amount));
							}
							player.updateInventory();
						}
						HashMap<String, String> placeholders = new HashMap<>();
						placeholders.put("%Voucher%", voucher.getName());
						placeholders.put("%voucher%", voucher.getName());
						sender.sendMessage(Messages.GIVEN_ALL_PLAYERS_VOUCHER.getMessage(placeholders));
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
	public void onPlayerJoin(PlayerJoinEvent e) {
		final Player player = e.getPlayer();
		new BukkitRunnable() {
			@Override
			public void run() {
				if(player.getName().equals("BadBones69")) {
					player.sendMessage(Methods.color("&8[&bVouchers&8]: " + "&7This server is running your Vouchers Plugin. " + "&7It is running version &av" + Bukkit.getServer().getPluginManager().getPlugin("Vouchers").getDescription().getVersion() + "&7."));
				}
				if(player.isOp()) {
					if(Files.CONFIG.getFile().contains("Settings.Updater")) {
						if(Files.CONFIG.getFile().getBoolean("Settings.Updater")) {
							Methods.hasUpdate(player);
						}
					}else {
						Methods.hasUpdate(player);
					}
				}
			}
		}.runTaskLaterAsynchronously(this, 20);
	}
	
}