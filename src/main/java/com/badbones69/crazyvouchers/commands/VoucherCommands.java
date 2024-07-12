package com.badbones69.crazyvouchers.commands;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.objects.other.ItemBuilder;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemCodeEvent;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import com.ryderbelserion.vital.paper.files.config.FileManager;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VoucherCommands implements CommandExecutor {

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NotNull final InventoryManager inventoryManager = this.plugin.getInventoryManager();

    private @NotNull final FileManager fileManager = this.plugin.getFileManager();

    private @NotNull final CrazyManager crazyManager = this.plugin.getCrazyManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        if (args.length == 0) {
            if (Methods.hasPermission(sender, "access")) Messages.help.sendMessage(sender);

            return true;
        } else {
            switch (args[0].toLowerCase()) {
                case "help" -> {
                    if (Methods.hasPermission(sender, "access")) Messages.help.sendMessage(sender);

                    return true;
                }

                case "reload" -> {
                    if (Methods.hasPermission(sender, "admin")) {
                        this.plugin.getInstance().reload();

                        this.fileManager.reloadFiles().init();

                        final FileConfiguration configuration = Files.users.getConfiguration();

                        if (!configuration.contains("Players")) {
                            configuration.set("Players.Clear", null);

                            Files.users.save();
                        }

                        this.crazyManager.reload();

                        Messages.config_reload.sendMessage(sender);
                    }

                    return true;
                }

                case "open", "admin" -> {
                    if (Methods.hasPermission(sender, "admin")) {
                        Player player = (Player) sender;

                        int page = 1;

                        if (args.length >= 2) {
                            page = Methods.isInt(args[1]) ? Integer.parseInt(args[1]) : 1;
                        }

                        this.inventoryManager.buildInventory(player, page);
                    }

                    return true;
                }

                case "types", "list" -> {
                    if (Methods.hasPermission(sender, "admin")) {
                        StringBuilder vouchers = new StringBuilder();
                        StringBuilder codes = new StringBuilder();

                        for (Voucher voucher : this.crazyManager.getVouchers()) {
                            vouchers.append("&a").append(voucher.getName()).append("&8, ");
                        }

                        for (VoucherCode code : this.crazyManager.getVoucherCodes()) {
                            codes.append("&a").append(code.getCode()).append("&8, ");
                        }

                        vouchers = new StringBuilder((vouchers.isEmpty()) ? "&cNone" : vouchers.substring(0, vouchers.length() - 2));
                        codes = new StringBuilder((codes.isEmpty()) ? "&cNone" : codes.substring(0, codes.length() - 2));

                        sender.sendMessage(MsgUtils.color("&e&lVouchers #" + this.crazyManager.getVouchers().size() + ":&f " + vouchers));
                        sender.sendMessage(MsgUtils.color("&e&lVoucher Codes #" + this.crazyManager.getVoucherCodes().size() + ":&f " + codes));
                    }

                    return true;
                }

                case "redeem" -> {
                    if (Methods.hasPermission(sender, "redeem")) {
                        if (args.length >= 2) {
                            String code = args[1];

                            if (!(sender instanceof Player player)) {
                                Messages.player_only.sendMessage(sender);

                                return true;
                            }

                            Map<String, String> placeholders = new HashMap<>();

                            placeholders.put("{arg}", code);
                            placeholders.put("{player}", player.getName());
                            placeholders.put("{world}", player.getWorld().getName());
                            placeholders.put("{x}", String.valueOf(player.getLocation().getBlockX()));
                            placeholders.put("{y}", String.valueOf(player.getLocation().getBlockY()));
                            placeholders.put("{z}", String.valueOf(player.getLocation().getBlockZ()));
                            placeholders.put("{prefix}", ConfigManager.getConfig().getProperty(ConfigKeys.command_prefix));

                            if (this.crazyManager.isVoucherCode(code)) {
                                VoucherCode voucherCode = this.crazyManager.getVoucherCode(code);

                                // Checking the permissions of the code.
                                if (!player.isOp() && !player.hasPermission("voucher.bypass")) {
                                    if (voucherCode.useWhiteListPermissions()) {
                                        for (String permission : voucherCode.getWhitelistPermissions()) {
                                            if (!player.hasPermission(permission)) {
                                                Messages.no_permission_to_use_voucher.sendMessage(player, placeholders);

                                                for (String command : voucherCode.getWhitelistCommands()) {
                                                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                                                }

                                                return true;
                                            }
                                        }
                                    }

                                    if (voucherCode.useWhitelistWorlds()) {
                                        if (voucherCode.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                                            player.sendMessage(Methods.replacePlaceholders(placeholders, voucherCode.getWhitelistWorldMessage(), true));

                                            for (String command : voucherCode.getWhitelistWorldCommands()) {
                                                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                                            }

                                            return true;
                                        }
                                    }

                                    if (voucherCode.useBlacklistPermissions()) {
                                        for (String permission : voucherCode.getBlacklistPermissions()) {
                                            if (player.hasPermission(permission.toLowerCase())) {
                                                player.sendMessage(Methods.replacePlaceholders(placeholders, voucherCode.getBlacklistMessage(), true));

                                                for (String command : voucherCode.getBlacklistCommands()) {
                                                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                                                }

                                                return true;
                                            }
                                        }
                                    }
                                }

                                // Has permission to continue.
                                FileConfiguration data = Files.users.getConfiguration();
                                String uuid = player.getUniqueId().toString();
                                // Checking if the player has used the code before.

                                if (data.contains("Players." + uuid)) {
                                    if (data.contains("Players." + uuid + ".Codes." + voucherCode.getName())) {
                                        if (data.getString("Players." + uuid + ".Codes." + voucherCode.getName()).equalsIgnoreCase("used")) {
                                            Messages.code_used.sendMessage(player, placeholders);
                                            return true;
                                        }
                                    }
                                }

                                //Checking the limit of the code.
                                if (voucherCode.useLimiter()) {
                                    if (data.contains("Voucher-Limit." + voucherCode.getName())) {
                                        if (data.getInt("Voucher-Limit." + voucherCode.getName()) < 1) {
                                            Messages.code_unavailable.sendMessage(player, placeholders);
                                            return true;
                                        }

                                        data.set("Voucher-Limit." + voucherCode.getName(), (data.getInt("Voucher-Limit." + voucherCode.getName()) - 1));
                                    } else {
                                        data.set("Voucher-Limit." + voucherCode.getName(), (voucherCode.getLimit() - 1));
                                    }

                                    Files.users.save();
                                }

                                // Gives the reward to the player.
                                VoucherRedeemCodeEvent event = new VoucherRedeemCodeEvent(player, voucherCode);
                                this.plugin.getServer().getPluginManager().callEvent(event);

                                if (!event.isCancelled()) {
                                    data.set("Players." + uuid + ".Codes." + voucherCode.getName(), "used");
                                    Files.users.save();

                                    for (String command : voucherCode.getCommands()) {
                                        this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(placeholders, crazyManager.replaceRandom(command), true));
                                    }

                                    if (!voucherCode.getRandomCommands().isEmpty()) { // Picks a random command from the Random-Commands list.
                                        for (String command : voucherCode.getRandomCommands().get(new Random().nextInt(voucherCode.getRandomCommands().size())).getCommands()) {
                                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                                        }
                                    }

                                    if (!voucherCode.getChanceCommands().isEmpty()) { // Picks a command based on the chance system of the Chance-Commands list.
                                        for (String command : voucherCode.getChanceCommands().get(new Random().nextInt(voucherCode.getChanceCommands().size())).getCommands()) {
                                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), Methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                                        }
                                    }

                                    for (ItemBuilder itemBuilder : voucherCode.getItems()) {
                                        if (!Methods.isInventoryFull(player)) {
                                            Methods.addItem(player, itemBuilder.build());
                                        } else {
                                            player.getWorld().dropItem(player.getLocation(), itemBuilder.build());
                                        }
                                    }

                                    if (voucherCode.useSounds()) {
                                        for (Sound sound : voucherCode.getSounds()) {
                                            player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, voucherCode.getVolume(), voucherCode.getPitch());
                                        }
                                    }

                                    if (voucherCode.useFireworks()) Methods.firework(player.getLocation(), voucherCode.getFireworkColors());

                                    if (!voucherCode.getMessage().isEmpty()) player.sendMessage(MsgUtils.color(Methods.replacePlaceholders(placeholders, voucherCode.getMessage(), true)));
                                }
                            } else {
                                Messages.code_unavailable.sendMessage(player, placeholders);

                                return true;
                            }

                            return true;
                        }

                        sender.sendMessage(Methods.getPrefix("&c/voucher redeem <code>"));
                    }

                    return true;
                }

                case "give" -> { // /Voucher 0Give 1<Type> 2[Amount] 3[Player] 4[Arguments]
                    if (Methods.hasPermission(sender, "admin")) {

                        if (args.length > 1) {
                            String name = sender.getName();

                            if (this.crazyManager.isVoucherName(args[1])) {
                                Messages.not_a_voucher.sendMessage(sender);

                                return true;
                            }

                            Voucher voucher = this.crazyManager.getVoucher(args[1]);
                            int amount = 1;

                            if (args.length >= 3) {
                                if (!Methods.isInt(sender, args[2])) return true;

                                amount = Integer.parseInt(args[2]);
                            }

                            if (args.length >= 4) {
                                name = args[3];

                                if (!Methods.isOnline(sender, name)) return true;
                            }

                            Player player = this.plugin.getServer().getPlayer(name);
                            String argument = "";

                            if (args.length >= 5) {
                                // Gives a random number as the argument.
                                // /Voucher give test 1 {player} {random}:1-1000
                                argument = crazyManager.replaceRandom(args[4].replace("%random%", "{random}"));
                            }

                            ItemStack item = args.length >= 5 ? voucher.buildItem(argument, amount) : voucher.buildItem(amount);

                            if (player != null) {
                                if (Methods.isInventoryFull(player)) {
                                    player.getWorld().dropItem(player.getLocation(), item);
                                } else {
                                    Methods.addItem(player, item);
                                    player.updateInventory();
                                }

                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("{player}", player.getName());
                                placeholders.put("{voucher}", voucher.getName());

                                if (!Messages.sent_voucher.getMessage(sender).isBlank()) Messages.sent_voucher.sendMessage(sender, placeholders);
                            } else {
                                Messages.not_online.sendMessage(sender);
                            }

                            return true;
                        }

                        sender.sendMessage(Methods.getPrefix("&c/voucher give <type> [amount] [player] [arguments]"));

                        return true;
                    }

                    return true;
                }

                case "giveall" -> { // /Voucher 0GiveAll 1<Type> 2[Amount] 3[Arguments]
                    if (Methods.hasPermission(sender, "admin")) {
                        if (args.length > 1) {
                            if (this.crazyManager.isVoucherName(args[1])) {
                                Messages.not_a_voucher.sendMessage(sender);

                                return true;
                            }

                            Voucher voucher = this.crazyManager.getVoucher(args[1]);

                            int amount = 1;

                            if (args.length >= 3) {
                                if (!Methods.isInt(sender, args[2])) return true;

                                amount = Integer.parseInt(args[2]);
                            }

                            String argument = "";

                            if (args.length >= 4) {
                                // Gives a random number as the argument.
                                // /voucher give test 1 {player} {random}:1-1000
                                argument = this.crazyManager.replaceRandom(args[3].replace("%random%", "{random}"));
                            }

                            ItemStack item = args.length >= 4 ? voucher.buildItem(argument, amount) : voucher.buildItem(amount);

                            for (Player player : this.plugin.getServer().getOnlinePlayers()) {
                                if (Methods.isInventoryFull(player)) {
                                    player.getWorld().dropItem(player.getLocation(), item);
                                } else {
                                    Methods.addItem(player, item);
                                    player.updateInventory();
                                }
                            }

                            Map<String, String> placeholders = new HashMap<>();

                            placeholders.put("{voucher}", voucher.getName());

                            Messages.sent_everyone_voucher.sendMessage(sender, placeholders);

                            return true;
                        }

                        sender.sendMessage(Methods.getPrefix("&c/voucher giveall <type> [amount] [arguments]"));

                        return true;
                    }

                    return true;
                }

                default -> {
                    sender.sendMessage(Methods.getPrefix("&cPlease do /voucher help for more information."));

                    return true;
                }
            }
        }
    }
}