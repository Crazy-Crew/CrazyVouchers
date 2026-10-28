package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.ryderbelserion.fusion.core.api.enums.Level;
import com.ryderbelserion.fusion.paper.builders.items.PlayerBuilder;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.ArgName;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Optional;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import dev.triumphteam.cmd.core.annotations.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandGive extends BaseCommand {

    @Command(value = "give")
    @Permission(value = "crazyvouchers.give", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers give [voucher] [amount] [player] [argument]")
    public void give(final CommandSender sender, @ArgName("voucher") @Suggestion("vouchers") String name, @ArgName("amount") @Suggestion("numbers") int amount, @ArgName("player") @Suggestion("players") PlayerBuilder target, @Optional String argument) {
        final Voucher voucher = this.crazyManager.getVoucher(name);

        if (voucher == null) {
            Messages.not_a_voucher.sendMessage(sender);

            return;
        }

        target.getPlayer().ifPresentOrElse(player -> {
            build(player, amount, argument, voucher).forEach(item -> {
                this.fusion.log(Level.ERROR, "<red>Id: %s", item.getPersistentDataContainer().getOrDefault(PersistentKeys.dupe_protection.getNamespacedKey(), PersistentDataType.STRING, "N/A"));

                Methods.addItem(player, item);
            });

            final Map<String, String> placeholders = new HashMap<>();

            placeholders.put("{player}", player.getName());
            placeholders.put("{voucher}", voucher.getStrippedName());

            Messages.sent_voucher.sendMessage(sender, placeholders);
        }, () -> Messages.not_online.sendMessage(sender));
    }

    @Command(value = "giveall")
    @Permission(value = "crazyvouchers.giveall", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers giveall [voucher] [amount] [argument]")
    public void all(final CommandSender sender, @ArgName("voucher") @Suggestion("vouchers") String name, @ArgName("amount") @Suggestion("numbers") int amount, @Optional String argument) {
        final Voucher voucher = this.crazyManager.getVoucher(name);

        if (voucher == null) {
            Messages.not_a_voucher.sendMessage(sender);

            return;
        }

        for (final Player player : server.getOnlinePlayers()) {
            for (final ItemStack item : build(player, amount, argument, voucher)) {
                Methods.addItem(player, item);
            }
        }

        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{voucher}", voucher.getStrippedName());

        Messages.sent_everyone_voucher.sendMessage(sender, placeholders);
    }

    private List<ItemStack> build(final Player player, final int amount, final String argument, final Voucher voucher) {
        return voucher.buildItems(player, argument != null ? argument.replace("%random%", "{random}") : "", Math.max(amount, 1));
    }
}