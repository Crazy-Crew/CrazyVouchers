package me.badbones69.vouchers.api;

import me.badbones69.vouchers.Methods;
import me.badbones69.vouchers.api.FileManager.Files;
import me.badbones69.vouchers.api.itemnbtapi.NBTItem;
import me.badbones69.vouchers.api.objects.Voucher;
import me.badbones69.vouchers.api.objects.VoucherCode;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Vouchers {
	
	private static ArrayList<Voucher> vouchers = new ArrayList<>();
	private static ArrayList<VoucherCode> voucherCodes = new ArrayList<>();
	
	public static void load() {
		vouchers.clear();
		voucherCodes.clear();
		for(String voucherName : Files.CONFIG.getFile().getConfigurationSection("Vouchers").getKeys(false)) {
			vouchers.add(new Voucher(voucherName));
		}
		if(Files.VOUCHER_CODES.getFile().contains("Voucher-Codes")) {
			for(String voucherName : Files.VOUCHER_CODES.getFile().getConfigurationSection("Voucher-Codes").getKeys(false)) {
				voucherCodes.add(new VoucherCode(voucherName));
			}
		}
	}
	
	public static ArrayList<Voucher> getVouchers() {
		return vouchers;
	}
	
	public static ArrayList<VoucherCode> getVoucherCodes() {
		return voucherCodes;
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
	
	public static VoucherCode getVoucherCode(String voucherName) {
		for(VoucherCode voucher : getVoucherCodes()) {
			if(voucher.getCode().equalsIgnoreCase(voucherName)) {
				return voucher;
			}
		}
		return null;
	}
	
	public static Boolean isVoucherCode(String voucherCode) {
		for(VoucherCode voucher : getVoucherCodes()) {
			if(voucher.isEnabled()) {
				if(voucher.isCaseSensitive()) {
					if(voucher.getCode().equals(voucherCode)) {
						return true;
					}
				}else {
					if(voucher.getCode().equalsIgnoreCase(voucherCode)) {
						return true;
					}
				}
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
							boolean sameLore = true;
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
			String voucherName = voucher.buildItem("%arg%").getItemMeta().getDisplayName();
			List<String> voucherLore = voucher.buildItem().getItemMeta().getLore();
			String argument = "";
			int line = 0;
			if(voucherName.contains("%arg%")) {
				String[] b = voucherName.split("%arg%");
				if(b.length >= 1) argument = itemName.replace(b[0], "");
				if(b.length >= 2) argument = argument.replace(b[1], "");
				if(itemName.equalsIgnoreCase(voucher.buildItem(argument).getItemMeta().getDisplayName())) {
					return argument;
				}
			}
			if(itemLore.size() == voucherLore.size()) {
				for(String lore : voucherLore) {
					String itemLine = itemLore.get(line);
					if(lore.contains("%arg%")) {
						String[] b = lore.split("%arg%");
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
	
	public static Plugin getPlugin() {
		return Bukkit.getPluginManager().getPlugin("Vouchers");
	}
	
}