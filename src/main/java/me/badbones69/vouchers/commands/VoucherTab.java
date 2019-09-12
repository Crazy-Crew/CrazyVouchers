package me.badbones69.vouchers.commands;

import me.badbones69.vouchers.api.Vouchers;
import me.badbones69.vouchers.controllers.GUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoucherTab implements TabCompleter {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String commandLable, String[] args) {
		List<String> completions = new ArrayList<>();
		if(args.length == 1) {// /voucher
			if(hasPermission(sender, "admin")) completions.add("help");
			if(hasPermission(sender, "admin")) completions.add("list");
			if(hasPermission(sender, "redeem")) completions.add("redeem");
			if(hasPermission(sender, "admin")) completions.add("give");
			if(hasPermission(sender, "admin")) completions.add("giveall");
			if(hasPermission(sender, "admin")) completions.add("open");
			if(hasPermission(sender, "admin")) completions.add("reload");
			return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
		}else if(args.length == 2) {// /voucher arg0
			switch(args[0].toLowerCase()) {
				case "redeem":
					//Only want admins to be able to see all the voucher codes.
					if(hasPermission(sender, "admin")) Vouchers.getVoucherCodes().forEach(voucherCode -> completions.add(voucherCode.getCode()));
					break;
				case "open":
					for(int i = 1; i <= GUI.getMaxPage(); i++) completions.add(i + "");
					break;
				case "give":
				case "giveall":
					Vouchers.getVouchers().forEach(voucher -> completions.add(voucher.getName()));
					break;
			}
			return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
		}else if(args.length == 3) {// /voucher arg0 arg1
			switch(args[0].toLowerCase()) {
				case "give":
				case "giveall":
					completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "10", "32", "64"));
					break;
			}
			return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
		}else if(args.length == 4) {// /voucher arg0 arg1 arg2
			if(args[0].equalsIgnoreCase("give")) {
				Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
			}
			return StringUtil.copyPartialMatches(args[3], completions, new ArrayList<>());
		}
		return new ArrayList<>();
	}
	
	private boolean hasPermission(CommandSender sender, String node) {
		return sender.hasPermission("voucher." + node) || sender.hasPermission("voucher.admin");
	}
	
}