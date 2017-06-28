package me.badbones69.vouchers.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.badbones69.vouchers.Main;
import me.badbones69.vouchers.Methods;

public class Vouchers {
	
	private static ArrayList<String> vouchers = new ArrayList<String>();
	private static HashMap<String, List<String>> commands = new HashMap<String, List<String>>();
	
	public static void load(){
		vouchers.clear();
		commands.clear();
		for(String vouch : getConfig().getConfigurationSection("Vouchers").getKeys(false)){
			vouchers.add(vouch);
			commands.put(vouch, getConfig().getStringList("Vouchers." + vouch + ".Commands"));
		}
	}
	
	public static ArrayList<String> getVouchers(){
		return vouchers;
	}
	
	public static Boolean isVoucher(String voucher){
		for(String vouch : vouchers){
			if(vouch.equalsIgnoreCase(voucher)){
				return true;
			}
		}
		return false;
	}
	
	public static String getVoucherName(String voucher){
		for(String vouch : vouchers){
			if(vouch.equalsIgnoreCase(voucher)){
				return vouch;
			}
		}
		return null;
	}
	
	public static Boolean isVoucher(ItemStack item){
		try{
			if(item != null){
				if(item.hasItemMeta()){
					if(item.getItemMeta().hasDisplayName() && item.getItemMeta().hasLore()){
						for(String voucher : getVouchers()){
							if(getVoucher(voucher, "%Arg%").getItemMeta().getDisplayName().contains("%Arg%")){
								if(hasVoucherItemName(item, voucher)){
									int line = 0;
									Boolean sameLore = true;
									ItemStack voucherItem = getVoucher(voucher, getVoucherArgumentItemName(item, voucher));
									for(String lore : item.getItemMeta().getLore()){
										if(!lore.equals(voucherItem.getItemMeta().getLore().get(line))){
											sameLore = false;
											break;
										}
										line++;
									}
									if(sameLore){
										return true;
									}
									return true;
								}
							}else if(item.getItemMeta().getDisplayName().equals(getVoucher(voucher).getItemMeta().getDisplayName())){
								int line = 0;
								Boolean sameLore = true;
								ItemStack voucherItem = getVoucher(voucher);
								for(String lore : item.getItemMeta().getLore()){
									if(!lore.equals(voucherItem.getItemMeta().getLore().get(line))){
										sameLore = false;
										break;
									}
									line++;
								}
								if(sameLore){
									return true;
								}
							}
						}
					}
				}
			}
		}catch(Exception e){}
		return false;
	}
	
	public static String getItemVoucher(ItemStack item){
		try{
			if(item != null){
				if(item.hasItemMeta()){
					if(item.getItemMeta().hasDisplayName() && item.getItemMeta().hasLore()){
						for(String voucher : getVouchers()){
							if(getVoucher(voucher, "%Arg%").getItemMeta().getDisplayName().contains("%Arg%")){
								if(hasVoucherItemName(item, voucher)){
									int line = 0;
									Boolean sameLore = true;
									ItemStack voucherItem = getVoucher(voucher, getVoucherArgumentItemName(item, voucher));
									for(String lore : item.getItemMeta().getLore()){
										if(!lore.equals(voucherItem.getItemMeta().getLore().get(line))){
											sameLore = false;
											break;
										}
										line++;
									}
									if(sameLore){
										return voucher;
									}
									return voucher;
								}
							}else if(item.getItemMeta().getDisplayName().equals(getVoucher(voucher).getItemMeta().getDisplayName())){
								int line = 0;
								Boolean sameLore = true;
								ItemStack voucherItem = getVoucher(voucher);
								for(String lore : item.getItemMeta().getLore()){
									if(!lore.equals(voucherItem.getItemMeta().getLore().get(line))){
										sameLore = false;
										break;
									}
									line++;
								}
								if(sameLore){
									return voucher;
								}
							}
						}
					}
				}
			}
		}catch(Exception e){}
		return null;
	}
	
	public static Boolean hasVoucherItemName(ItemStack item, String voucher){
		String name = item.getItemMeta().getDisplayName();
		String argument = "";
		String vName = getVoucher(voucher, "%Arg%").getItemMeta().getDisplayName();
		if(vName.contains("%Arg%") || vName.contains("%arg%")){
			String[] b = vName.split("%Arg%");
			if(!name.startsWith(b[0])){
				return false;
			}
			if(b.length >= 1)argument = name.replace(b[0], "");
			if(b.length >= 2)argument = argument.replace(b[1], "");
			if(name.equalsIgnoreCase(getVoucher(voucher, argument).getItemMeta().getDisplayName())){
				return true;
			}
		}
		return false;
	}
	
	public static String getVoucherArgumentItemName(ItemStack item, String voucher){
		String name = item.getItemMeta().getDisplayName();
		String argument = "";
		String vName = getVoucher(voucher, "%Arg%").getItemMeta().getDisplayName();
		if(vName.contains("%Arg%") || vName.contains("%arg%")){
			String[] b = vName.split("%Arg%");
			if(b.length>=1)argument = name.replace(b[0], "");
			if(b.length>=2)argument = argument.replace(b[1], "");
			if(name.equalsIgnoreCase(getVoucher(voucher, argument).getItemMeta().getDisplayName())){
				return argument;
			}
		}
		return null;
	}
	
	public static String getVoucherItemName(ItemStack item){
		String name = item.getItemMeta().getDisplayName();
		String argument = "";
		for(String voucher : vouchers){
			String vName = getVoucher(voucher, "%Arg%").getItemMeta().getDisplayName();
			if(vName.contains("%Arg%") || vName.contains("%arg%")){
				String[] b = vName.split("%Arg%");
				if(b.length>=1)argument = name.replace(b[0], "");
				if(b.length>=2)argument = argument.replace(b[1], "");
				if(name.equalsIgnoreCase(getVoucher(voucher, argument).getItemMeta().getDisplayName())){
					return voucher;
				}
			}
		}
		return null;
	}
	
	public static List<String> getCommands(String voucher, Player player, String argument){
		ArrayList<String> cmds = new ArrayList<String>();
		for(String cmd : commands.get(voucher)){
			cmds.add(cmd.replaceAll("%Player%", player.getName()).replaceAll("%player%", player.getName())
					.replaceAll("%Arg%", argument).replaceAll("%arg%", argument));
		}
		return cmds;
	}
	
	public static ItemStack getVoucher(String voucher){
		String id = getConfig().getString("Vouchers." + voucher + ".Item");
		String name = getConfig().getString("Vouchers." + voucher + ".Name");
		List<String> lore = getConfig().getStringList("Vouchers." + voucher + ".Lore");
		Boolean glowing = false;
		if(getConfig().contains("Vouchers." + voucher + ".Glowing")){
			glowing = getConfig().getBoolean("Vouchers." + voucher + ".Glowing");
		}
		return Methods.addGlow(Methods.makeItem(id, 1, name, lore), glowing);
	}
	
	public static ItemStack getVoucher(String voucher, Integer amount){
		String id = getConfig().getString("Vouchers." + voucher + ".Item");
		String name = getConfig().getString("Vouchers." + voucher + ".Name");
		List<String> lore = getConfig().getStringList("Vouchers." + voucher + ".Lore");
		Boolean glowing = false;
		if(getConfig().contains("Vouchers." + voucher + ".Glowing")){
			glowing = getConfig().getBoolean("Vouchers." + voucher + ".Glowing");
		}
		return Methods.addGlow(Methods.makeItem(id, amount, name, lore), glowing);
	}
	
	public static ItemStack getVoucher(String voucher, String argument){
		String id = getConfig().getString("Vouchers." + voucher + ".Item");
		String name = getConfig().getString("Vouchers." + voucher + ".Name")
				.replaceAll("%Arg%", argument).replaceAll("%arg%", argument);
		List<String> lore = new ArrayList<String>();
		for(String l : getConfig().getStringList("Vouchers." + voucher + ".Lore")){
			lore.add(l.replaceAll("%Arg%", argument).replaceAll("%arg%", argument));
		}
		Boolean glowing = false;
		if(getConfig().contains("Vouchers." + voucher + ".Glowing")){
			glowing = getConfig().getBoolean("Vouchers." + voucher + ".Glowing");
		}
		return Methods.addGlow(Methods.makeItem(id, 1, name, lore), glowing);
	}
	
	public static ItemStack getVoucher(String voucher, String argument, Integer amount){
		String id = getConfig().getString("Vouchers." + voucher + ".Item");
		String name = getConfig().getString("Vouchers." + voucher + ".Name")
				.replaceAll("%Arg%", argument).replaceAll("%arg%", argument);
		List<String> lore = new ArrayList<String>();
		for(String l : getConfig().getStringList("Vouchers." + voucher + ".Lore")){
			lore.add(l.replaceAll("%Arg%", argument).replaceAll("%arg%", argument));
		}
		Boolean glowing = false;
		if(getConfig().contains("Vouchers." + voucher + ".Glowing")){
			glowing = getConfig().getBoolean("Vouchers." + voucher + ".Glowing");
		}
		return Methods.addGlow(Methods.makeItem(id, amount, name, lore), glowing);
	}
	
	public static ArrayList<ItemStack> getItems(String voucher){
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		for(String i : getConfig().getStringList("Vouchers." + voucher + ".Items")){
			String id = "1";
			Integer amount = 1;
			String name = "";
			List<String> lore = new ArrayList<String>();
			HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			for(String d : i.split(", ")){
				if(d.startsWith("Item:")){
					id = d.replace("Item:", "");
				}else if(d.startsWith("Amount:")){
					if(Methods.isInt(d.replace("Amount:", ""))){
						amount = Integer.parseInt(d.replace("Amount:", ""));
					}
				}else if(d.startsWith("Name:")){
					name = d.replace("Name:", "");
				}else if(d.startsWith("Lore:")){
					d = d.replace("Lore:", "");
					if(d.contains(",")){
						for(String D : d.split(",")){
							lore.add(D);
						}
					}else{
						lore.add(d);
					}
				}
				for(Enchantment ench : Enchantment.values()){
					if(d.startsWith(ench.getName() + ":") || d.startsWith(Methods.getEnchantmentName(ench) + ":")){
						String[] breakdown = d.split(":");
						int lvl = Integer.parseInt(breakdown[1]);
						enchantments.put(ench, lvl);
					}
				}
			}
			items.add(Methods.makeItem(id, amount, name, lore, enchantments));
		}
		return items;
	}
	
	public static Boolean isFireworkEnabled(String voucher){
		return getConfig().getBoolean("Vouchers." + voucher + ".Options.Firework.Toggle");
	}
	
	public static ArrayList<Color> getFireworkColors(String voucher){
		ArrayList<Color> colors = new ArrayList<Color>();
		if(getConfig().getString("Vouchers." + voucher + ".Options.Firework.Colors").contains(", ")){
			for(String c : getConfig().getString("Vouchers." + voucher + ".Options.Firework.Colors").split(", ")){
				Color color = getColor(c);
				if(color != null){
					colors.add(color);
				}
			}
		}else{
			Color color = getColor(getConfig().getString("Vouchers." + voucher + ".Options.Firework.Colors"));
			if(color != null){
				colors.add(color);
			}
		}
		return colors;
	}
	
	public static Boolean isWhitelistPermissionEnabled(String voucher){
		return getConfig().getBoolean("Vouchers." + voucher + ".Options.Permission.Whitelist-Permission.Toggle");
	}
	
	public static String getWhitelistPermissionNode(String voucher){
		return getConfig().getString("Vouchers." + voucher + ".Options.Permission.Whitelist-Permission.Node");
	}
	
	public static Boolean isBlacklistPermissionsEnabled(String voucher){
		return getConfig().getBoolean("Vouchers." + voucher + ".Options.Permission.Blacklist-Permissions.Toggle");
	}
	
	public static List<String> getBlacklistPermissions(String voucher){
		return getConfig().getStringList("Vouchers." + voucher + ".Options.Permission.Blacklist-Permissions.Permissions");
	}
	
	public static Boolean isLimiterEnabled(String voucher){
		return getConfig().getBoolean("Vouchers." + voucher + ".Options.Limiter.Toggle");
	}
	
	public static Integer getLimiter(String voucher){
		return getConfig().getInt("Vouchers." + voucher + ".Options.Limiter.Limit");
	}
	
	public static Boolean isSoundEnabled(String voucher){
		return getConfig().getBoolean("Vouchers." + voucher + ".Options.Sound.Toggle");
	}
	
	public static ArrayList<Sound> getSound(String voucher){
		ArrayList<Sound> sounds = new ArrayList<Sound>();
		try{
			for(String sound : getConfig().getStringList("Vouchers." + voucher + ".Options.Sound.Sounds")){
				sounds.add(Sound.valueOf(sound));
			}
		}catch(Exception e){
			Bukkit.getLogger().log(Level.WARNING, "[Vouchers]>> The voucher " + voucher + "'s sound that you set to is not a sound. "
					+ "Please go to the config and set a correct sound or turn the sound off in the SoundToggle setting.");	
		}
		return sounds;
	}
	
	private static Color getColor(String color) {
		if (color.equalsIgnoreCase("AQUA")) return Color.AQUA;
		if (color.equalsIgnoreCase("BLACK")) return Color.BLACK;
		if (color.equalsIgnoreCase("BLUE")) return Color.BLUE;
		if (color.equalsIgnoreCase("FUCHSIA")) return Color.FUCHSIA;
		if (color.equalsIgnoreCase("GRAY")) return Color.GRAY;
		if (color.equalsIgnoreCase("GREEN")) return Color.GREEN;
		if (color.equalsIgnoreCase("LIME")) return Color.LIME;
		if (color.equalsIgnoreCase("MAROON")) return Color.MAROON;
		if (color.equalsIgnoreCase("NAVY")) return Color.NAVY;
		if (color.equalsIgnoreCase("OLIVE")) return Color.OLIVE;
		if (color.equalsIgnoreCase("ORANGE")) return Color.ORANGE;
		if (color.equalsIgnoreCase("PURPLE")) return Color.PURPLE;
		if (color.equalsIgnoreCase("RED")) return Color.RED;
		if (color.equalsIgnoreCase("SILVER")) return Color.SILVER;
		if (color.equalsIgnoreCase("TEAL")) return Color.TEAL;
		if (color.equalsIgnoreCase("WHITE")) return Color.WHITE;
		if (color.equalsIgnoreCase("YELLOW")) return Color.YELLOW;
		return Color.WHITE;
	}
	
	private static FileConfiguration getConfig(){
		return Main.settings.getConfig();
	}
	
}