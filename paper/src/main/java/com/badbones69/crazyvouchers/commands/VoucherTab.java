package com.badbones69.crazyvouchers.commands;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.InventoryManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoucherTab implements TabCompleter {

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NotNull final InventoryManager inventoryManager = this.plugin.getInventoryManager();

    private @NotNull final CrazyManager crazyManager = this.plugin.getCrazyManager();
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) { // /crazyvouchers
            if (hasPermission(sender, "admin")) completions.add("help");
            if (hasPermission(sender, "admin")) completions.add("list");
            if (hasPermission(sender, "redeem")) completions.add("redeem");
            if (hasPermission(sender, "admin")) completions.add("give");
            if (hasPermission(sender, "admin")) completions.add("giveall");
            if (hasPermission(sender, "admin")) completions.add("open");
            if (hasPermission(sender, "admin")) completions.add("reload");
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        } else if (args.length == 2) { // /crazyvouchers arg0
            switch (args[0].toLowerCase()) {
                case "redeem" -> {
                    // Only want admins to be able to see all the voucher codes.
                    if (hasPermission(sender, "admin")) this.crazyManager.getVoucherCodes().forEach(voucherCode -> completions.add(voucherCode.getCode()));
                }

                case "open" -> {
                    if (hasPermission(sender, "admin"))
                        for (int i = 1; i <= this.inventoryManager.getMaxPages(); i++) completions.add(String.valueOf(i));
                }

                case "give", "giveall" -> {
                    if (hasPermission(sender, "admin")) this.crazyManager.getVouchers().forEach(voucher -> completions.add(voucher.getName()));
                }
            }

            return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
        } else if (args.length == 3) { // /crazyvouchers arg0 arg1
            switch (args[0].toLowerCase()) {
                case "give", "giveall" -> {
                    if (hasPermission(sender, "admin")) completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "10", "32", "64"));
                }
            }

            return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
        } else if (args.length == 4) { // /crazyvouchers arg0 arg1 arg2
            if (args[0].equalsIgnoreCase("give")) {
                if (hasPermission(sender, "admin")) this.plugin.getServer().getOnlinePlayers().forEach(player -> completions.add(player.getName()));
            }

            return StringUtil.copyPartialMatches(args[3], completions, new ArrayList<>());
        }

        return new ArrayList<>();
    }
    
    private boolean hasPermission(CommandSender sender, String node) {
        return sender.hasPermission("crazyvouchers." + node) || sender.hasPermission("crazyvouchers.admin") || sender.hasPermission("voucher." + node) || sender.hasPermission("voucher.admin");
    }
}