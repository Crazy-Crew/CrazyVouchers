package me.badbones69.vouchers;

import me.badbones69.vouchers.api.FileManager;
import me.badbones69.vouchers.api.FileManager.Files;
import me.badbones69.vouchers.api.Vouchers;
import me.badbones69.vouchers.api.enums.Messages;
import me.badbones69.vouchers.api.enums.Version;
import me.badbones69.vouchers.api.events.RedeemVoucherCodeEvent;
import me.badbones69.vouchers.api.objects.Voucher;
import me.badbones69.vouchers.api.objects.VoucherCode;
import me.badbones69.vouchers.controllers.FireworkDamageAPI;
import me.badbones69.vouchers.controllers.GUI;
import me.badbones69.vouchers.controllers.Metrics;
import me.badbones69.vouchers.controllers.VoucherClick;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Random;

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
		new Metrics(this);
		Vouchers.load();
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
					for(String co : Files.VOUCHER_CODES.getFile().getConfigurationSection("Voucher-Codes").getKeys(false)) {
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
						HashMap<String, String> placeholders = new HashMap<>();
						placeholders.put("%Arg%", code);
						placeholders.put("%arg%", code);
						if(Vouchers.isVoucherCode(code)) {
							VoucherCode voucherCode = Vouchers.getVoucherCode(code);
							//Checking the permissions of the code.
							if(!player.isOp() && !player.hasPermission("voucher.bypass")) {
								if(voucherCode.useWhitelistPermission()) {
									if(!player.hasPermission(voucherCode.getWhitelistPermission())) {
										player.sendMessage(Messages.NO_PERMISSION_TO_VOUCHER.getMessage().replace("%arg%", voucherCode.getWhitelistPermission()));
										return true;
									}
								}
								if(voucherCode.useWhitelistWorlds()) {
									if(voucherCode.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
										player.sendMessage(voucherCode.getWhitelistWorldMessage());
										return true;
									}
								}
								if(voucherCode.useBlacklistPermissions()) {
									for(String permission : voucherCode.getBlacklistPermissions()) {
										if(player.hasPermission(permission.toLowerCase())) {
											player.sendMessage(Methods.color(Methods.getPrefix() + voucherCode.getBlacklistMessage()));
											return true;
										}
									}
								}
							}
							//Has permission to continue.
							FileConfiguration data = Files.DATA.getFile();
							String uuid = player.getUniqueId().toString();
							//Checking if the player has used the code before.
							if(data.contains("Players." + uuid)) {
								if(data.contains("Players." + uuid + ".Codes." + voucherCode.getName())) {
									if(data.getString("Players." + uuid + ".Codes." + voucherCode.getName()).equalsIgnoreCase("used")) {
										player.sendMessage(Messages.CODE_USED.getMessage(placeholders));
										return true;
									}
								}
							}
							//Checking the limit of the code.
							if(voucherCode.useLimiter()) {
								if(data.contains("Voucher-Limit." + voucherCode.getName())) {
									if(data.getInt("Voucher-Limit." + voucherCode.getName()) < 1) {
										player.sendMessage(Messages.CODE_UNAVAILABLE.getMessage(placeholders));
										return true;
									}
									data.set("Voucher-Limit." + voucherCode.getName(), (data.getInt("Voucher-Limit." + voucherCode.getName()) - 1));
								}else {
									data.set("Voucher-Limit." + voucherCode.getName(), (voucherCode.getLimit() - 1));
								}
								Files.DATA.saveFile();
							}
							//Gives the reward to the player.
							RedeemVoucherCodeEvent event = new RedeemVoucherCodeEvent(player, voucherCode);
							Bukkit.getPluginManager().callEvent(event);
							if(!event.isCancelled()) {
								String name = player.getName();
								data.set("Players." + uuid + ".Codes." + voucherCode.getName(), "used");
								Files.DATA.saveFile();
								for(String command : voucherCode.getCommands()) {
									Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%Player%", name).replaceAll("%player%", name)
									.replaceAll("%World%", player.getWorld().getName()).replaceAll("%world%", player.getWorld().getName())
									.replaceAll("%X%", player.getLocation().getBlockX() + "").replaceAll("%x%", player.getLocation().getBlockX() + "")
									.replaceAll("%Y%", player.getLocation().getBlockY() + "").replaceAll("%y%", player.getLocation().getBlockY() + "")
									.replaceAll("%Z%", player.getLocation().getBlockZ() + "").replaceAll("%z%", player.getLocation().getBlockZ() + ""));
								}
								if(voucherCode.getRandomCoammnds().size() >= 1) {// Picks a random command from the Random-Commands list.
									for(String command : voucherCode.getRandomCoammnds().get(new Random().nextInt(voucherCode.getRandomCoammnds().size())).getCommands()) {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
										.replaceAll("%Player%", name).replaceAll("%player%", name)
										.replaceAll("%World%", player.getWorld().getName()).replaceAll("%world%", player.getWorld().getName())
										.replaceAll("%X%", player.getLocation().getBlockX() + "").replaceAll("%x%", player.getLocation().getBlockX() + "")
										.replaceAll("%Y%", player.getLocation().getBlockY() + "").replaceAll("%y%", player.getLocation().getBlockY() + "")
										.replaceAll("%Z%", player.getLocation().getBlockZ() + "").replaceAll("%z%", player.getLocation().getBlockZ() + ""));
									}
								}
								if(voucherCode.getChanceCommands().size() >= 1) {// Picks a command based on the chance system of the Chance-Commands list.
									for(String command : voucherCode.getChanceCommands().get(new Random().nextInt(voucherCode.getChanceCommands().size())).getCommands()) {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
										.replaceAll("%Player%", name).replaceAll("%player%", name)
										.replaceAll("%World%", player.getWorld().getName()).replaceAll("%world%", player.getWorld().getName())
										.replaceAll("%X%", player.getLocation().getBlockX() + "").replaceAll("%x%", player.getLocation().getBlockX() + "")
										.replaceAll("%Y%", player.getLocation().getBlockY() + "").replaceAll("%y%", player.getLocation().getBlockY() + "")
										.replaceAll("%Z%", player.getLocation().getBlockZ() + "").replaceAll("%z%", player.getLocation().getBlockZ() + ""));
									}
								}
								for(ItemStack it : voucherCode.getItems()) {
									if(!Methods.isInventoryFull(player)) {
										player.getInventory().addItem(it);
									}else {
										player.getWorld().dropItem(player.getLocation(), it);
									}
								}
								if(voucherCode.useSounds()) {
									for(Sound sound : voucherCode.getSounds()) {
										player.playSound(player.getLocation(), sound, 1, 1);
									}
								}
								if(voucherCode.useFireworks()) {
									Methods.fireWork(player.getLocation(), voucherCode.getFireworkColors());
								}
								if(!voucherCode.getMessage().equals("")) {
									player.sendMessage(Methods.color(voucherCode.getMessage()
									.replaceAll("%Player%", name).replaceAll("%player%", name)
									.replaceAll("%Prefix%", Methods.getPrefix()).replaceAll("%prefix%", Methods.getPrefix())
									.replaceAll("%World%", player.getWorld().getName()).replaceAll("%world%", player.getWorld().getName())
									.replaceAll("%X%", player.getLocation().getBlockX() + "").replaceAll("%x%", player.getLocation().getBlockX() + "")
									.replaceAll("%Y%", player.getLocation().getBlockY() + "").replaceAll("%y%", player.getLocation().getBlockY() + "")
									.replaceAll("%Z%", player.getLocation().getBlockZ() + "").replaceAll("%z%", player.getLocation().getBlockZ() + "")));
								}
							}
						}else {
							player.sendMessage(Messages.CODE_UNAVAILABLE.getMessage(placeholders));
							return true;
						}
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
						String argument = "";
						if(args.length >= 5) {
							argument = args[4];
							//Gives a random number as the argument.
							// /Voucher give test 1 %player% %random%:1-1000
							if(argument.startsWith("%random%:")) {
								argument = argument.replace("%random%:", "");
								try {
									int min = Integer.parseInt(argument.split("-")[0]);
									int max = Integer.parseInt(argument.split("-")[1]);
									argument += pickNumber(min, max) + " ";
								}catch(Exception e) {
									argument += "1";
								}
							}
						}
						ItemStack item = args.length >= 5 ? voucher.buildItem(argument, amount) : voucher.buildItem(amount);
						if(Methods.isInventoryFull(player)) {
							player.getWorld().dropItem(player.getLocation(), item);
						}else {
							player.getInventory().addItem(item);
							player.updateInventory();
						}
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
						String argument = "";
						if(args.length >= 4) {
							argument = args[3];
							//Gives a random number as the argument.
							// /Voucher give test 1 %player% %random%:1-1000
							if(argument.startsWith("%random%:")) {
								argument = argument.replace("%random%:", "");
								try {
									int min = Integer.parseInt(argument.split("-")[0]);
									int max = Integer.parseInt(argument.split("-")[1]);
									argument += pickNumber(min, max) + " ";
								}catch(Exception e) {
									argument += "1";
								}
							}
						}
						ItemStack item = args.length >= 4 ? voucher.buildItem(argument, amount) : voucher.buildItem(amount);
						for(Player player : Bukkit.getServer().getOnlinePlayers()) {
							if(Methods.isInventoryFull(player)) {
								player.getWorld().dropItem(player.getLocation(), item);
							}else {
								player.getInventory().addItem(item);
								player.updateInventory();
							}
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
	
	private Integer pickNumber(int min, int max) {
		max++;
		return min + new Random().nextInt(max - min);
	}
	
}