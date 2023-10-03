package com.badbones69.crazyvouchers.paper.commands;

import com.badbones69.crazyvouchers.paper.Methods;
import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import com.badbones69.crazyvouchers.paper.api.enums.Translation;
import com.badbones69.crazyvouchers.paper.api.objects.ItemBuilder;
import com.badbones69.crazyvouchers.paper.api.objects.Voucher;
import com.badbones69.crazyvouchers.paper.listeners.VoucherMenuListener;
import com.badbones69.crazyvouchers.paper.api.FileManager;
import com.badbones69.crazyvouchers.paper.api.FileManager.Files;
import com.badbones69.crazyvouchers.paper.api.CrazyManager;
import com.badbones69.crazyvouchers.paper.api.events.RedeemVoucherCodeEvent;
import com.badbones69.crazyvouchers.paper.api.objects.VoucherCode;
import com.ryderbelserion.cluster.bukkit.utils.LegacyUtils;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyenvoys.common.config.types.Config;

import java.util.HashMap;
import java.util.Random;

public class VoucherCommands implements CommandExecutor {
    
    private final CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final FileManager fileManager = this.plugin.getFileManager();

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final Methods methods = this.plugin.getMethods();

    private final VoucherMenuListener voucherMenuListener = this.plugin.getGui();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        if (args.length == 0) {
            if (this.methods.hasPermission(sender, "access")) Translation.help.sendMessage(sender);
            return true;
        } else {
            switch (args[0].toLowerCase()) {
                case "help" -> {
                    if (this.methods.hasPermission(sender, "access")) Translation.help.sendMessage(sender);
                    return true;
                }
                case "reload" -> {
                    if (this.methods.hasPermission(sender, "admin")) {
                        this.fileManager.reloadAllFiles();
                        this.fileManager.setup();

                        if (!Files.users.getFile().contains("Players")) {
                            Files.users.getFile().set("Players.Clear", null);
                            Files.users.saveFile();
                        }

                        this.crazyManager.reload(false);
                        Translation.config_reload.sendMessage(sender);
                    }
                    return true;
                }
                case "open", "admin" -> {
                    if (this.methods.hasPermission(sender, "admin")) {
                        int page = 1;

                        if (args.length >= 2) {
                            page = this.methods.isInt(args[1]) ? Integer.parseInt(args[1]) : 1;
                        }

                        this.voucherMenuListener.openGUI((Player) sender, page);
                    }
                    return true;
                }
                case "types", "list" -> {
                    if (methods.hasPermission(sender, "admin")) {
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
                        sender.sendMessage(LegacyUtils.color("&e&lVouchers #" + crazyManager.getVouchers().size() + ":&f " + vouchers));
                        sender.sendMessage(LegacyUtils.color("&e&lVoucher Codes #" + crazyManager.getVoucherCodes().size() + ":&f " + codes));
                    }
                    return true;
                }
                case "redeem" -> {
                    if (this.methods.hasPermission(sender, "redeem")) {
                        if (args.length >= 2) {
                            String code = args[1];

                            if (!(sender instanceof Player player)) {
                                Translation.player_only.sendMessage(sender);
                                return true;
                            }

                            HashMap<String, String> placeholders = new HashMap<>();

                            placeholders.put("{arg}", code);
                            placeholders.put("{player}", player.getName());
                            placeholders.put("{world}", player.getWorld().getName());
                            placeholders.put("{x}", String.valueOf(player.getLocation().getBlockX()));
                            placeholders.put("{y}", String.valueOf(player.getLocation().getBlockY()));
                            placeholders.put("{z}", String.valueOf(player.getLocation().getBlockZ()));
                            placeholders.put("{prefix}", this.plugin.getCrazyHandler().getConfigManager().getConfig().getProperty(Config.command_prefix));

                            if (this.crazyManager.isVoucherCode(code)) {
                                VoucherCode voucherCode = this.crazyManager.getVoucherCode(code);

                                // Checking the permissions of the code.
                                if (!player.isOp() && !player.hasPermission("voucher.bypass")) {
                                    if (voucherCode.useWhiteListPermissions()) {
                                        for (String permission : voucherCode.getWhitelistPermissions()) {
                                            if (!player.hasPermission(permission)) {
                                                Translation.no_permission_to_use_voucher.sendMessage(player, placeholders);

                                                for (String command : voucherCode.getWhitelistCommands()) {
                                                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command)));
                                                }

                                                return true;
                                            }
                                        }
                                    }

                                    if (voucherCode.useWhitelistWorlds()) {
                                        if (voucherCode.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                                            player.sendMessage(this.methods.getPrefix(this.methods.replacePlaceholders(placeholders, voucherCode.getWhitelistWorldMessage())));

                                            for (String command : voucherCode.getWhitelistWorldCommands()) {
                                                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command)));
                                            }

                                            return true;
                                        }
                                    }

                                    if (voucherCode.useBlacklistPermissions()) {
                                        for (String permission : voucherCode.getBlacklistPermissions()) {
                                            if (player.hasPermission(permission.toLowerCase())) {
                                                player.sendMessage(this.methods.getPrefix(this.methods.replacePlaceholders(placeholders, voucherCode.getBlacklistMessage())));

                                                for (String command : voucherCode.getBlacklistCommands()) {
                                                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command)));
                                                }

                                                return true;
                                            }
                                        }
                                    }
                                }

                                // Has permission to continue.
                                FileConfiguration data = Files.users.getFile();
                                String uuid = player.getUniqueId().toString();
                                // Checking if the player has used the code before.

                                if (data.contains("Players." + uuid)) {
                                    if (data.contains("Players." + uuid + ".Codes." + voucherCode.getName())) {
                                        if (data.getString("Players." + uuid + ".Codes." + voucherCode.getName()).equalsIgnoreCase("used")) {
                                            Translation.code_used.sendMessage(player, placeholders);
                                            return true;
                                        }
                                    }
                                }

                                //Checking the limit of the code.
                                if (voucherCode.useLimiter()) {
                                    if (data.contains("Voucher-Limit." + voucherCode.getName())) {
                                        if (data.getInt("Voucher-Limit." + voucherCode.getName()) < 1) {
                                            Translation.code_unavailable.sendMessage(player, placeholders);
                                            return true;
                                        }

                                        data.set("Voucher-Limit." + voucherCode.getName(), (data.getInt("Voucher-Limit." + voucherCode.getName()) - 1));
                                    } else {
                                        data.set("Voucher-Limit." + voucherCode.getName(), (voucherCode.getLimit() - 1));
                                    }

                                    Files.users.saveFile();
                                }

                                // Gives the reward to the player.
                                RedeemVoucherCodeEvent event = new RedeemVoucherCodeEvent(player, voucherCode);
                                this.plugin.getServer().getPluginManager().callEvent(event);

                                if (!event.isCancelled()) {
                                    data.set("Players." + uuid + ".Codes." + voucherCode.getName(), "used");
                                    Files.users.saveFile();

                                    for (String command : voucherCode.getCommands()) {
                                        this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(placeholders, crazyManager.replaceRandom(command)));
                                    }

                                    if (!voucherCode.getRandomCommands().isEmpty()) { // Picks a random command from the Random-Commands list.
                                        for (String command : voucherCode.getRandomCommands().get(new Random().nextInt(voucherCode.getRandomCommands().size())).getCommands()) {
                                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command)));
                                        }
                                    }

                                    if (!voucherCode.getChanceCommands().isEmpty()) { // Picks a command based on the chance system of the Chance-Commands list.
                                        for (String command : voucherCode.getChanceCommands().get(new Random().nextInt(voucherCode.getChanceCommands().size())).getCommands()) {
                                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), this.methods.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command)));
                                        }
                                    }

                                    for (ItemBuilder itemBuilder : voucherCode.getItems()) {
                                        if (!this.methods.isInventoryFull(player)) {
                                            player.getInventory().addItem(itemBuilder.build());
                                        } else {
                                            player.getWorld().dropItem(player.getLocation(), itemBuilder.build());
                                        }
                                    }

                                    if (voucherCode.useSounds()) {
                                        for (Sound sound : voucherCode.getSounds()) {
                                            player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, voucherCode.getVolume(), voucherCode.getPitch());
                                        }
                                    }

                                    if (voucherCode.useFireworks()) this.methods.fireWork(player.getLocation(), voucherCode.getFireworkColors());

                                    if (!voucherCode.getMessage().isEmpty()) player.sendMessage(LegacyUtils.color(this.methods.replacePlaceholders(placeholders, voucherCode.getMessage())));
                                }
                            } else {
                                Translation.code_unavailable.sendMessage(player, placeholders);
                                return true;
                            }

                            return true;
                        }
                        sender.sendMessage(this.methods.getPrefix("&c/voucher redeem <code>"));
                    }
                    return true;
                }
                case "give" -> { // /Voucher 0Give 1<Type> 2[Amount] 3[Player] 4[Arguments]
                    if (methods.hasPermission(sender, "admin")) {

                        if (args.length > 1) {
                            String name = sender.getName();

                            if (this.crazyManager.isVoucherName(args[1])) {
                                Translation.not_a_voucher.sendMessage(sender);
                                return true;
                            }

                            Voucher voucher = this.crazyManager.getVoucher(args[1]);
                            int amount = 1;

                            if (args.length >= 3) {
                                if (!this.methods.isInt(sender, args[2])) return true;
                                amount = Integer.parseInt(args[2]);
                            }

                            if (args.length >= 4) {
                                name = args[3];
                                if (!this.methods.isOnline(sender, name)) return true;
                            }

                            Player player = this.plugin.getServer().getPlayer(name);
                            String argument = "";

                            if (args.length >= 5) {
                                // Gives a random number as the argument.
                                // /Voucher give test 1 {player} {random}:1-1000
                                argument = crazyManager.replaceRandom(args[4].replace("%random%", "{random}"));
                            }

                            ItemStack item = args.length >= 5 ? voucher.buildItem(argument, amount) : voucher.buildItem(amount);

                            if (this.methods.isInventoryFull(player)) {
                                player.getWorld().dropItem(player.getLocation(), item);
                            } else {
                                player.getInventory().addItem(item);
                                player.updateInventory();
                            }

                            HashMap<String, String> placeholders = new HashMap<>();
                            placeholders.put("{player}", player.getName());
                            placeholders.put("{voucher}", voucher.getName());

                            if (!Translation.sent_voucher.isBlank()) Translation.sent_voucher.sendMessage(sender, placeholders);

                            return true;
                        }

                        sender.sendMessage(this.methods.getPrefix("&c/voucher give <type> [amount] [player] [arguments]"));
                        return true;
                    }
                    return true;
                }
                case "giveall" -> { // /Voucher 0GiveAll 1<Type> 2[Amount] 3[Arguments]
                    if (this.methods.hasPermission(sender, "admin")) {
                        if (args.length > 1) {
                            if (this.crazyManager.isVoucherName(args[1])) {
                                Translation.not_a_voucher.sendMessage(sender);
                                return true;
                            }

                            Voucher voucher = this.crazyManager.getVoucher(args[1]);
                            int amount = 1;

                            if (args.length >= 3) {
                                if (!this.methods.isInt(sender, args[2])) return true;
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
                                if (this.methods.isInventoryFull(player)) {
                                    player.getWorld().dropItem(player.getLocation(), item);
                                } else {
                                    player.getInventory().addItem(item);
                                    player.updateInventory();
                                }
                            }

                            HashMap<String, String> placeholders = new HashMap<>();
                            placeholders.put("{voucher}", voucher.getName());
                            Translation.sent_everyone_voucher.sendMessage(sender, placeholders);
                            return true;
                        }

                        sender.sendMessage(this.methods.getPrefix("&c/voucher giveall <type> [amount] [arguments]"));
                        return true;
                    }
                    return true;
                }
                default -> {
                    sender.sendMessage(this.methods.getPrefix("&cPlease do /voucher help for more information."));
                    return true;
                }
            }
        }
    }
}