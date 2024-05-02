package com.badbones69.crazyvouchers.commands;

import com.badbones69.crazyvouchers.platform.util.MiscUtil;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.builders.types.VoucherGuiMenu;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.builders.ItemBuilder;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemCodeEvent;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.platform.util.MsgUtil;
import com.ryderbelserion.vital.files.FileManager;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class VoucherCommands implements CommandExecutor {

    private final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final @NotNull FileManager fileManager = this.plugin.getFileManager();

    private final @NotNull CrazyManager crazyManager = this.plugin.getCrazyManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        if (args.length == 0) {
            if (MiscUtil.hasPermission(sender, "access")) Messages.help.sendMessage(sender);

            return true;
        } else {
            switch (args[0].toLowerCase()) {
                case "help" -> {
                    if (MiscUtil.hasPermission(sender, "access")) Messages.help.sendMessage(sender);

                    return true;
                }

                case "reload" -> {
                    if (MiscUtil.hasPermission(sender, "admin")) {
                        this.fileManager.create();

                        FileConfiguration configuration = Files.users.getFile();

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
                    if (MiscUtil.hasPermission(sender, "admin")) {
                        int page = 1;

                        if (args.length >= 2) {
                            page = MiscUtil.isInt(args[1]) ? Integer.parseInt(args[1]) : 1;
                        }

                        Player player = (Player) sender;

                        VoucherGuiMenu menu = new VoucherGuiMenu(player, 54, MsgUtil.color("<dark_gray><bold><underlined>Vouchers"));

                        player.openInventory(menu.build(page).getInventory());
                    }

                    return true;
                }

                case "types", "list" -> {
                    if (MiscUtil.hasPermission(sender, "admin")) {
                        StringBuilder vouchers = new StringBuilder();
                        StringBuilder codes = new StringBuilder();

                        for (Voucher voucher : this.crazyManager.getVouchers()) {
                            vouchers.append("<green>").append(voucher.getName()).append("<dark_gray>, ");
                        }

                        for (VoucherCode code : this.crazyManager.getVoucherCodes()) {
                            codes.append("<green>").append(code.getCode()).append("<dark_gray>, ");
                        }

                        vouchers = new StringBuilder((vouchers.isEmpty()) ? "<red>None" : vouchers.substring(0, vouchers.length() - 2));
                        codes = new StringBuilder((codes.isEmpty()) ? "<red>None" : codes.substring(0, codes.length() - 2));

                        sender.sendRichMessage("<bold><gold>Vouchers</bold> <gold>#" + this.crazyManager.getVouchers().size() + ":<white> " + vouchers);
                        sender.sendRichMessage("<bold><gold>Voucher Codes</bold> <gold>#" + this.crazyManager.getVoucherCodes().size() + ":<white> " + codes);
                    }

                    return true;
                }

                case "redeem" -> {
                    if (MiscUtil.hasPermission(sender, "redeem")) {
                        if (args.length >= 2) {
                            String code = args[1];

                            if (!(sender instanceof Player player)) {
                                Messages.player_only.sendMessage(sender);

                                return true;
                            }

                            HashMap<String, String> placeholders = new HashMap<>();

                            placeholders.put("{arg}", code);
                            placeholders.put("{player}", player.getName());
                            placeholders.put("{world}", player.getWorld().getName());
                            placeholders.put("{x}", String.valueOf(player.getLocation().getBlockX()));
                            placeholders.put("{y}", String.valueOf(player.getLocation().getBlockY()));
                            placeholders.put("{z}", String.valueOf(player.getLocation().getBlockZ()));
                            placeholders.put("{prefix}", MsgUtil.getPrefix());

                            if (this.crazyManager.isVoucherCode(code)) {
                                VoucherCode voucherCode = this.crazyManager.getVoucherCode(code);

                                // Checking the permissions of the code.
                                if (!player.isOp() && !player.hasPermission("voucher.bypass")) {
                                    if (voucherCode.useWhiteListPermissions()) {
                                        for (String permission : voucherCode.getWhitelistPermissions()) {
                                            if (!player.hasPermission(permission)) {
                                                Messages.no_permission_to_use_voucher.sendMessage(player, placeholders);

                                                for (String command : voucherCode.getWhitelistCommands()) {
                                                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                                                }

                                                return true;
                                            }
                                        }
                                    }

                                    if (voucherCode.useWhitelistWorlds()) {
                                        if (voucherCode.getWhitelistWorlds().contains(player.getWorld().getName().toLowerCase())) {
                                            player.sendMessage(MiscUtil.replacePlaceholders(placeholders, voucherCode.getWhitelistWorldMessage(), true));

                                            for (String command : voucherCode.getWhitelistWorldCommands()) {
                                                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                                            }

                                            return true;
                                        }
                                    }

                                    if (voucherCode.useBlacklistPermissions()) {
                                        for (String permission : voucherCode.getBlacklistPermissions()) {
                                            if (player.hasPermission(permission.toLowerCase())) {
                                                player.sendMessage(MiscUtil.replacePlaceholders(placeholders, voucherCode.getBlacklistMessage(), true));

                                                for (String command : voucherCode.getBlacklistCommands()) {
                                                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
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
                                            Messages.code_used.sendMessage(player, placeholders);
                                            return true;
                                        }
                                    }
                                }

                                // Checking the limit of the code.
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
                                        this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, crazyManager.replaceRandom(command), true));
                                    }

                                    if (!voucherCode.getRandomCommands().isEmpty()) { // Picks a random command from the Random-Commands list.
                                        for (String command : voucherCode.getRandomCommands().get(ThreadLocalRandom.current().nextInt(voucherCode.getRandomCommands().size())).getCommands()) {
                                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                                        }
                                    }

                                    if (!voucherCode.getChanceCommands().isEmpty()) { // Picks a command based on the chance system of the Chance-Commands list.
                                        for (String command : voucherCode.getChanceCommands().get(ThreadLocalRandom.current().nextInt(voucherCode.getChanceCommands().size())).getCommands()) {
                                            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyManager.replaceRandom(command), true));
                                        }
                                    }

                                    for (ItemBuilder itemBuilder : voucherCode.getItems()) {
                                        if (!MiscUtil.isInventoryFull(player)) {
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

                                    if (voucherCode.useFireworks()) MiscUtil.firework(player.getLocation(), voucherCode.getFireworkColors());

                                    if (!voucherCode.getMessage().isEmpty()) player.sendMessage(MsgUtil.color(MiscUtil.replacePlaceholders(placeholders, voucherCode.getMessage(), true)));
                                }
                            } else {
                                Messages.code_unavailable.sendMessage(player, placeholders);
                                return true;
                            }

                            return true;
                        }

                        sender.sendRichMessage(MiscUtil.getPrefix("<red>/crazyvouchers redeem <code>"));
                    }

                    return true;
                }

                case "give" -> { // /crazyvouchers 0give 1<type> 2[amount] 3[player] 4[arguments]
                    if (MiscUtil.hasPermission(sender, "admin")) {
                        if (args.length > 1) {
                            String name = sender.getName();

                            if (this.crazyManager.isVoucherName(args[1])) {
                                Messages.not_a_voucher.sendMessage(sender);

                                return true;
                            }

                            Voucher voucher = this.crazyManager.getVoucher(args[1]);
                            int amount = 1;

                            if (args.length >= 3) {
                                if (!MiscUtil.isInt(sender, args[2])) return true;

                                amount = Integer.parseInt(args[2]);
                            }

                            if (args.length >= 4) {
                                name = args[3];

                                if (!MiscUtil.isOnline(sender, name)) return true;
                            }

                            Player player = this.plugin.getServer().getPlayer(name);
                            String argument = "";

                            if (args.length >= 5) {
                                // Gives a random number as the argument.
                                // /crazyvouchers give test 1 {player} {random}:1-1000
                                argument = crazyManager.replaceRandom(args[4].replace("%random%", "{random}"));
                            }

                            ItemStack item = args.length >= 5 ? voucher.buildItem(argument, amount) : voucher.buildItem(amount);

                            if (player != null) {
                                if (MiscUtil.isInventoryFull(player)) {
                                    player.getWorld().dropItem(player.getLocation(), item);
                                } else {
                                    player.getInventory().addItem(item);
                                    player.updateInventory();
                                }

                                HashMap<String, String> placeholders = new HashMap<>();
                                placeholders.put("{player}", player.getName());
                                placeholders.put("{voucher}", voucher.getName());

                                if (!Messages.sent_voucher.isBlank()) Messages.sent_voucher.sendMessage(sender, placeholders);
                            } else {
                                Messages.not_online.sendMessage(sender);
                            }

                            return true;
                        }

                        sender.sendRichMessage(MiscUtil.getPrefix("<red>/crazyvouchers give <type> [amount] [player] [arguments]"));

                        return true;
                    }

                    return true;
                }

                case "giveall" -> { // /crazyvouchers 0giveall 1<type> 2[amount] 3[arguments]
                    if (MiscUtil.hasPermission(sender, "admin")) {
                        if (args.length > 1) {
                            if (this.crazyManager.isVoucherName(args[1])) {
                                Messages.not_a_voucher.sendMessage(sender);

                                return true;
                            }

                            Voucher voucher = this.crazyManager.getVoucher(args[1]);
                            int amount = 1;

                            if (args.length >= 3) {
                                if (!MiscUtil.isInt(sender, args[2])) return true;

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
                                if (MiscUtil.isInventoryFull(player)) {
                                    player.getWorld().dropItem(player.getLocation(), item);
                                } else {
                                    player.getInventory().addItem(item);
                                    player.updateInventory();
                                }
                            }

                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("{voucher}", voucher.getName());

                            Messages.sent_everyone_voucher.sendMessage(sender, placeholders);

                            return true;
                        }

                        sender.sendRichMessage(MiscUtil.getPrefix("<red>/voucher giveall <type> [amount] [arguments]"));

                        return true;
                    }

                    return true;
                }

                default -> {
                    sender.sendRichMessage(MiscUtil.getPrefix("<red>Please do /voucher help for more information."));

                    return true;
                }
            }
        }
    }
}