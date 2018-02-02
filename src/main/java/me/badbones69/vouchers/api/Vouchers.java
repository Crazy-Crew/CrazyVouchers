package me.badbones69.vouchers.api;

import de.tr7zw.itemnbtapi.NBTItem;
import me.badbones69.vouchers.Main;
import me.badbones69.vouchers.Methods;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Vouchers {
	
	private static ArrayList<Voucher> vouchers = new ArrayList<>();
	
	public static void load() {
		vouchers.clear();
		for(String voucherName : getConfig().getConfigurationSection("Vouchers").getKeys(false)) {
			vouchers.add(new Voucher(voucherName));
		}
	}
	
	public static ArrayList<Voucher> getVouchers() {
		return vouchers;
	}
	
	public static Voucher getVoucher(String voucherName) {
		for(Voucher voucher : getVouchers()) {
			if(voucher.getName().equalsIgnoreCase(voucherName)) {
				return voucher;
			}
		}
		return null;
	}
	
	public static Boolean isVoucherName(String voucherName) {
		for(Voucher voucher : getVouchers()) {
			if(voucher.getName().equalsIgnoreCase(voucherName)) {
				return true;
			}
		}
		return false;
	}
	
	public static Voucher getVoucherFromItem(ItemStack item) {
		NBTItem nbt = new NBTItem(item);
		if(nbt.hasKey("voucher")) {
			return getVoucher(nbt.getString("voucher"));
		}
		try {
			if(item.hasItemMeta()) {
				if(item.getItemMeta().hasDisplayName() && item.getItemMeta().hasLore()) {
					for(Voucher voucher : getVouchers()) {
						if(voucher.usesArguments()) {
							String argument = getArgument(item, voucher);
							if(argument != null) {
								if(Methods.isSimilar(item, voucher.buildItem(argument))) {
									return voucher;
								}
							}
						}else if(item.getItemMeta().getDisplayName().equals(voucher.buildItem().getItemMeta().getDisplayName())) {
							int line = 0;
							Boolean sameLore = true;
							ItemStack voucherItem = voucher.buildItem();
							for(String lore : item.getItemMeta().getLore()) {
								if(!lore.equals(voucherItem.getItemMeta().getLore().get(line))) {
									sameLore = false;
									break;
								}
								line++;
							}
							if(sameLore) {
								return voucher;
							}
						}
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getArgument(ItemStack item, Voucher voucher) {
		if(voucher.usesArguments()) {
			//Checks to see if the voucher uses nbt tags.
			NBTItem nbt = new NBTItem(item);
			if(nbt.hasKey("voucher") && nbt.hasKey("argument")) {
				if(nbt.getString("voucher").equalsIgnoreCase(voucher.getName())) {
					return nbt.getString("argument");
				}
			}
			//Using the old method to check for old vouchers or vouchers given without nbt tags.
			String itemName = item.getItemMeta().getDisplayName();
			List<String> itemLore = item.getItemMeta().getLore();
			String voucherName = voucher.buildItem("%Arg%").getItemMeta().getDisplayName();
			List<String> voucherLore = voucher.buildItem().getItemMeta().getLore();
			String argument = "";
			int line = 0;
			if(voucherName.contains("%Arg%")) {
				String[] b = voucherName.split("%Arg%");
				if(b.length >= 1) argument = itemName.replace(b[0], "");
				if(b.length >= 2) argument = argument.replace(b[1], "");
				if(itemName.equalsIgnoreCase(voucher.buildItem(argument).getItemMeta().getDisplayName())) {
					return argument;
				}
			}
			if(itemLore.size() == voucherLore.size()) {
				for(String lore : voucherLore) {
					String itemLine = itemLore.get(line);
					if(lore.contains("%Arg%")) {
						String[] b = lore.split("%Arg%");
						if(!itemLine.startsWith(b[0])) {
							break;
						}
						argument = itemLine.replace(b[0], "");
						if(b.length >= 2) argument = argument.replace(b[1], "");
						if(Methods.isSimilar(item, voucher.buildItem(argument))) {
							return argument;
						}
					}else if(!itemLore.get(line).equals(lore)) {
						break;
					}
					line++;
				}
			}
		}
		return null;
	}
	
	private static FileConfiguration getConfig() {
		return Main.settings.getConfig();
	}
}