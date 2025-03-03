package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.ryderbelserion.fusion.paper.builder.PlayerBuilder;
import dev.triumphteam.cmd.core.annotations.ArgName;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Optional;
import dev.triumphteam.cmd.core.annotations.Permission;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import dev.triumphteam.cmd.core.annotations.Syntax;
import dev.triumphteam.cmd.core.enums.Mode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandGive extends BaseCommand {

    @Command(value = "give")
    @Permission(value = "crazyvouchers.give", def = Mode.OP)
    @Syntax("/crazyvouchers give [voucher] [amount] [player] [argument]")
    public void give(final CommandSender sender, @ArgName("voucher") @Suggestion("vouchers") String name, @ArgName("amount") @Suggestion("numbers") int amount, @ArgName("player") @Suggestion("players") PlayerBuilder target, @Optional String argument) {
        final Voucher voucher = this.crazyManager.getVoucher(name);

        if (voucher == null) {
            Messages.not_a_voucher.sendMessage(sender);

            return;
        }

        final Player player = target.getPlayer();

        if (player == null) {
            Messages.not_online.sendMessage(sender);

            return;
        }

        List<ItemStack> itemStacks = build(amount, argument, voucher);

        itemStacks.forEach(itemStack -> Methods.addItem(player, itemStack));

        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{player}", player.getName());
        placeholders.put("{voucher}", voucher.getName());

        if (!Messages.sent_voucher.getMessage(sender).isBlank()) Messages.sent_voucher.sendMessage(sender, placeholders);
    }

    @Command(value = "giveall")
    @Permission(value = "crazyvouchers.giveall", def = Mode.OP)
    @Syntax("/crazyvouchers giveall [voucher] [amount] [argument]")
    public void all(final CommandSender sender, @ArgName("voucher") @Suggestion("vouchers") String name, @ArgName("amount") @Suggestion("numbers") int amount, @Optional String argument) {
        final Voucher voucher = this.crazyManager.getVoucher(name);

        if (voucher == null) {
            Messages.not_a_voucher.sendMessage(sender);

            return;
        }

        List<ItemStack> itemStacks = build(amount, argument, voucher);

        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            itemStacks.forEach(itemStack -> Methods.addItem(player, itemStack));
        }

        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{voucher}", voucher.getName());

        Messages.sent_everyone_voucher.sendMessage(sender, placeholders);
    }

    private List<ItemStack> build(final int amount, final String argument, final Voucher voucher) {
        final int safety = Math.max(amount, 1);

        final String arg = argument != null ? argument.replace("%random%", "{random}") : "";

        final List<ItemStack> itemStacks = new ArrayList<>();

        if (!arg.isEmpty()) {
            itemStacks.addAll(voucher.buildItems(argument, safety));
        } else {
            itemStacks.addAll(voucher.buildItems("", safety));
        }

        return itemStacks;
    }
}