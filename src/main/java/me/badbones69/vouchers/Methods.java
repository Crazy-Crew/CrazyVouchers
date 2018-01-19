package me.badbones69.vouchers;

import me.badbones69.vouchers.api.FireworkDamageAPI;
import me.badbones69.vouchers.api.ItemBuilder;
import me.badbones69.vouchers.api.Version;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class Methods {

	public static Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Vouchers");

	public static void removeItem(ItemStack item, Player player) {
		if(item.getAmount() <= 1) {
			player.getInventory().removeItem(item);
		}
		if(item.getAmount() > 1) {
			ItemStack i = item;
			i.setAmount(item.getAmount() - 1);
		}
	}

	public static String getPrefix() {
		return color(Main.settings.getConfig().getString("Settings.Prefix"));
	}

	public static String Args(String arg) {
		arg = ChatColor.stripColor(arg);
		arg = arg.replace("&l", "");
		arg = arg.replace("", "");
		arg = arg.replaceAll("(&([a-f0-9]))", "");
		return arg;
	}

	public static String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static String removeColor(String msg) {
		return ChatColor.stripColor(msg);
	}

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
		}catch(NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean isInt(CommandSender sender, String s) {
		try {
			Integer.parseInt(s);
		}catch(NumberFormatException nfe) {
			sender.sendMessage(color(Main.settings.getMsgs().getString("Messages.Not-A-Number").replace("%Arg%", s).replace("%arg%", s)));
			return false;
		}
		return true;
	}

	public static ItemStack makeItem(String itemString) {
		String id = "1";
		Short itemMetaData = 0;
		Integer amount = 1;
		String name = "";
		List<String> lore = new ArrayList<String>();
		HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
		for(String d : itemString.split(", ")) {
			if(d.startsWith("Item:")) {
				id = d.replace("Item:", "");
			}else if(d.startsWith("Amount:")) {
				if(isInt(d.replace("Amount:", ""))) {
					amount = Integer.parseInt(d.replace("Amount:", ""));
				}
			}else if(d.startsWith("Name:")) {
				name = d.replace("Name:", "");
			}else if(d.startsWith("Lore:")) {
				d = d.replace("Lore:", "");
				if(d.contains(",")) {
					for(String D : d.split(",")) {
						lore.add(D);
					}
				}else {
					lore.add(d);
				}
			}
			for(Enchantment ench : Enchantment.values()) {
				if(d.startsWith(ench.getName() + ":") || d.startsWith(getEnchantmentName(ench) + ":")) {
					String[] breakdown = d.split(":");
					int lvl = Integer.parseInt(breakdown[1]);
					enchantments.put(ench, lvl);
				}
			}
		}
		if(id.contains(":")) {
			String[] b = id.split(":");
			id = b[0];
			itemMetaData = Short.parseShort(b[1]);
		}
		return new ItemBuilder()
		.setMaterial(Material.matchMaterial(id))
		.setMetaData(itemMetaData)
		.setAmount(amount)
		.setName(name)
		.setLore(lore)
		.setEnchantments(enchantments)
		.build();
	}

	public static boolean isOnline(CommandSender sender, String name) {
		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
			if(player.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		sender.sendMessage(color(Main.settings.getMsgs().getString("Messages.Not-Online")));
		return false;
	}

	public static boolean hasPermission(Player player, String perm) {
		if(!player.hasPermission("Voucher." + perm)) {
			player.sendMessage(color(Main.settings.getMsgs().getString("Messages.No-Permission")));
			return false;
		}
		return true;
	}

	public static boolean hasPermission(CommandSender sender, String perm) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(!player.hasPermission("Voucher." + perm)) {
				player.sendMessage(color(Main.settings.getMsgs().getString("Messages.No-Permission")));
				return false;
			}else {
				return true;
			}
		}else {
			return true;
		}
	}

	public static boolean isRealCode(Player player, String code) {
		FileConfiguration Code = Main.settings.getCode();
		if(Code.contains("Codes")) {
			for(String C : Code.getConfigurationSection("Codes").getKeys(false)) {
				boolean toggle = false;
				if(Code.contains("Codes." + C + ".CaseSensitive")) {
					toggle = Code.getBoolean("Codes." + C + ".CaseSensitive");
				}
				if((toggle && C.equals(code)) || (!toggle && C.equalsIgnoreCase(code))) {
					return true;
				}
			}
		}
		player.sendMessage(color(Main.settings.getMsgs().getString("Messages.Code-UnAvailable").replace("%Arg%", code).replace("%arg%", code)));
		return false;
	}

	public static boolean isCodeEnabled(Player player, String code) {
		if(Main.settings.getCode().contains("Codes")) {
			for(String C : Main.settings.getCode().getConfigurationSection("Codes").getKeys(false)) {
				if(C.equalsIgnoreCase(code)) {
					if(Main.settings.getCode().getBoolean("Codes." + C + ".Enabled/Disabled")) {
						return true;
					}
				}
			}
		}
		player.sendMessage(color(Main.settings.getMsgs().getString("Messages.Code-UnAvailable").replace("%Arg%", code).replace("%arg%", code)));
		return false;
	}

	public static boolean hasCodePerm(Player player, String code) {
		if(Main.settings.getCode().contains("Codes")) {
			for(String C : Main.settings.getCode().getConfigurationSection("Codes").getKeys(false)) {
				if(C.equalsIgnoreCase(code)) {
					if(Main.settings.getCode().getBoolean("Codes." + C + ".Permission-Toggle")) {
						if(player.hasPermission("Voucher." + Main.settings.getCode().getString("Codes." + C + ".Permission-Node"))) {
							return true;
						}
					}else {
						return true;
					}
				}
			}
		}
		player.sendMessage(color(Main.settings.getMsgs().getString("Messages.Code-UnAvailable").replace("%Arg%", code).replace("%arg%", code)));
		return false;
	}

	public static void codeRedeem(Player player, String code) {
		FileConfiguration Code = Main.settings.getCode();
		FileConfiguration Data = Main.settings.getData();
		if(Code.contains("Codes")) {
			for(String C : Code.getConfigurationSection("Codes").getKeys(false)) {
				if(C.equalsIgnoreCase(code)) {
					String uuid = player.getUniqueId() + "";
					if(Data.contains("Players." + uuid)) {
						Data.set("Players." + uuid + ".UserName", player.getName());
						Main.settings.saveData();
						if(Data.contains("Players." + uuid + ".Codes." + C)) {
							if(Data.getString("Players." + uuid + ".Codes." + C).equalsIgnoreCase("Used")) {
								player.sendMessage(color("&cYou have used that code already."));
								return;
							}
						}
					}
					if(Code.getInt("Codes." + C + ".CodesLeft") < 1) {
						player.sendMessage(color(Main.settings.getMsgs().getString("Messages.Code-UnAvailable").replace("%Arg%", code).replace("%arg%", code)));
						return;
					}
					if(Code.getBoolean("Codes." + C + ".Limited")) {
						if(Code.getInt("Codes." + C + ".CodesLeft") <= 0) {
							player.sendMessage(color(Main.settings.getMsgs().getString("Messages.Code-UnAvailable").replace("%Arg%", code).replace("%arg%", code)));
							return;
						}else {
							Code.set("Codes." + C + ".CodesLeft", (Code.getInt("Codes." + C + ".CodesLeft") - 1));
						}
					}
					if(Code.contains("Codes." + C + ".Commands")) {
						for(String cmd : Code.getStringList("Codes." + C + ".Commands")) {
							cmd = cmd.replace("%player%", player.getName()).replace("%Player%", player.getName());
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
						}
					}
					if(Code.contains("Codes." + C + ".Messages")) {
						for(String msg : Code.getStringList("Codes." + C + ".Messages")) {
							msg = msg.replace("%player%", player.getName()).replace("%Player%", player.getName());
							msg = msg.replace("%Code%", C).replace("%code%", C);
							player.sendMessage(color(msg));
						}
					}
					if(Code.contains("Codes." + C + ".BroadCasts")) {
						for(String msg : Code.getStringList("Codes." + C + ".BroadCasts")) {
							msg = msg.replace("%player%", player.getName()).replace("%Player%", player.getName());
							Bukkit.broadcastMessage(color(msg));
						}
					}
					if(Code.contains("Codes." + C + ".SoundToggle") && Code.contains("Codes." + C + ".Sound")) {
						if(Code.getBoolean("Codes." + C + ".SoundToggle")) {
							String sound = Code.getString("Codes." + C + ".Sound");
							try {
								player.playSound(player.getLocation(), Sound.valueOf(sound), 1, 1);
							}catch(Exception e) {
								Bukkit.getLogger().log(Level.WARNING, "[Vouchers]>> The voucher " + C + "'s sound that you set to " + sound + " is not a sound. " + "Please go to the config and set a correct sound or turn the sound off in the SoundToggle setting.");
								for(Player p : Bukkit.getServer().getOnlinePlayers()) {
									if(p.isOp()) {
										p.sendMessage(color("&4&l[Vouchers]>> &cThe voucher &6" + C + "'s &csound that you set to &6" + sound + " &cis not a sound. " + "&cPlease go to the config and set a correct sound or turn the sound off in the SoundToggle setting."));
									}
								}
							}
						}
					}
					Data.set("Players." + uuid + ".UserName", player.getName());
					Data.set("Players." + uuid + ".Codes." + C, "Used");
					Main.settings.saveData();
					Main.settings.saveCode();
				}
			}
		}
	}

	public static void hasUpdate() {
		try {
			HttpURLConnection c = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
			c.setDoOutput(true);
			c.setRequestMethod("POST");
			c.getOutputStream().write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=13654").getBytes("UTF-8"));
			String oldVersion = plugin.getDescription().getVersion();
			String newVersion = new BufferedReader(new InputStreamReader(c.getInputStream())).readLine().replaceAll("[a-zA-Z ]", "");
			if(!newVersion.equals(oldVersion)) {
				Bukkit.getConsoleSender().sendMessage(color("&8[&bVouchers&8]: " + "&cYour server is running &7v" + oldVersion + "&c and the newest is &7v" + newVersion + "&c."));
			}
		}catch(Exception e) {
			return;
		}
	}

	public static void hasUpdate(Player player) {
		try {
			HttpURLConnection c = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
			c.setDoOutput(true);
			c.setRequestMethod("POST");
			c.getOutputStream().write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=13654").getBytes("UTF-8"));
			String oldVersion = plugin.getDescription().getVersion();
			String newVersion = new BufferedReader(new InputStreamReader(c.getInputStream())).readLine().replaceAll("[a-zA-Z ]", "");
			if(!newVersion.equals(oldVersion)) {
				player.sendMessage(color("&8[&bVouchers&8]: " + "&cYour server is running &7v" + oldVersion + "&c and the newest is &7v" + newVersion + "&c."));
			}
		}catch(Exception e) {
			return;
		}
	}

	public static boolean isInvFull(Player player) {
		if(player.getInventory().firstEmpty() == -1) {
			return true;
		}
		return false;
	}

	public static void fireWork(Location loc, List<Color> list) {
		final Firework f = loc.getWorld().spawn(loc, Firework.class);
		FireworkMeta fm = f.getFireworkMeta();
		fm.addEffects(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(list).trail(false).flicker(false).build());
		fm.setPower(0);
		f.setFireworkMeta(fm);
		FireworkDamageAPI.addFirework(f);
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				f.detonate();
			}
		}, 2);
	}

	public static String getEnchantmentName(Enchantment en) {
		HashMap<String, String> enchants = new HashMap<String, String>();
		enchants.put("ARROW_DAMAGE", "Power");
		enchants.put("ARROW_FIRE", "Flame");
		enchants.put("ARROW_INFINITE", "Infinity");
		enchants.put("ARROW_KNOCKBACK", "Punch");
		enchants.put("DAMAGE_ALL", "Sharpness");
		enchants.put("DAMAGE_ARTHROPODS", "Bane_Of_Arthropods");
		enchants.put("DAMAGE_UNDEAD", "Smite");
		enchants.put("DEPTH_STRIDER", "Depth_Strider");
		enchants.put("DIG_SPEED", "Efficiency");
		enchants.put("DURABILITY", "Unbreaking");
		enchants.put("FIRE_ASPECT", "Fire_Aspect");
		enchants.put("KNOCKBACK", "KnockBack");
		enchants.put("LOOT_BONUS_BLOCKS", "Fortune");
		enchants.put("LOOT_BONUS_MOBS", "Looting");
		enchants.put("LUCK", "Luck_Of_The_Sea");
		enchants.put("LURE", "Lure");
		enchants.put("OXYGEN", "Respiration");
		enchants.put("PROTECTION_ENVIRONMENTAL", "Protection");
		enchants.put("PROTECTION_EXPLOSIONS", "Blast_Protection");
		enchants.put("PROTECTION_FALL", "Feather_Falling");
		enchants.put("PROTECTION_FIRE", "Fire_Protection");
		enchants.put("PROTECTION_PROJECTILE", "Projectile_Protection");
		enchants.put("SILK_TOUCH", "Silk_Touch");
		enchants.put("THORNS", "Thorns");
		enchants.put("WATER_WORKER", "Aqua_Affinity");
		enchants.put("BINDING_CURSE", "Curse_Of_Binding");
		enchants.put("MENDING", "Mending");
		enchants.put("FROST_WALKER", "Frost_Walker");
		enchants.put("VANISHING_CURSE", "Curse_Of_Vanishing");
		if(enchants.get(en.getName()) == null) {
			return "None Found";
		}
		return enchants.get(en.getName());
	}

	public static ItemStack addGlow(ItemStack item, boolean glowing) {
		if(Version.getCurrentVersion().comparedTo(Version.v1_8_R1) >= 0) {
			if(glowing) {
				if(item != null) {
					if(item.hasItemMeta()) {
						if(item.getItemMeta().hasEnchants()) {
							return item;
						}
					}
					item.addUnsafeEnchantment(Enchantment.LUCK, 1);
					ItemMeta meta = item.getItemMeta();
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					item.setItemMeta(meta);
				}
			}
		}
		return item;
	}

	public static Color getColor(String color) {
		if(color.equalsIgnoreCase("AQUA")) return Color.AQUA;
		if(color.equalsIgnoreCase("BLACK")) return Color.BLACK;
		if(color.equalsIgnoreCase("BLUE")) return Color.BLUE;
		if(color.equalsIgnoreCase("FUCHSIA")) return Color.FUCHSIA;
		if(color.equalsIgnoreCase("GRAY")) return Color.GRAY;
		if(color.equalsIgnoreCase("GREEN")) return Color.GREEN;
		if(color.equalsIgnoreCase("LIME")) return Color.LIME;
		if(color.equalsIgnoreCase("MAROON")) return Color.MAROON;
		if(color.equalsIgnoreCase("NAVY")) return Color.NAVY;
		if(color.equalsIgnoreCase("OLIVE")) return Color.OLIVE;
		if(color.equalsIgnoreCase("ORANGE")) return Color.ORANGE;
		if(color.equalsIgnoreCase("PURPLE")) return Color.PURPLE;
		if(color.equalsIgnoreCase("RED")) return Color.RED;
		if(color.equalsIgnoreCase("SILVER")) return Color.SILVER;
		if(color.equalsIgnoreCase("TEAL")) return Color.TEAL;
		if(color.equalsIgnoreCase("WHITE")) return Color.WHITE;
		if(color.equalsIgnoreCase("YELLOW")) return Color.YELLOW;
		return Color.WHITE;
	}

	public static boolean isSimilar(ItemStack one, ItemStack two) {
		if(one.getType() == two.getType()) {
			if(one.hasItemMeta()) {
				if(one.getItemMeta().hasDisplayName()) {
					if(one.getItemMeta().getDisplayName().equalsIgnoreCase(two.getItemMeta().getDisplayName())) {
						if(one.getItemMeta().hasLore()) {
							if(one.getItemMeta().getLore().size() == two.getItemMeta().getLore().size()) {
								int i = 0;
								for(String lore : one.getItemMeta().getLore()) {
									if(!lore.equals(two.getItemMeta().getLore().get(i))) {
										return false;
									}
									i++;
								}
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

}