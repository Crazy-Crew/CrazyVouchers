package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.Methods;
import org.bukkit.OfflinePlayer;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.commands.BaseCommand;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandGive extends BaseCommand {

    @Command(value = "give")
    @Permission(value = "crazyvouchers.give", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers give [voucher] [amount] [player] [argument]")
    public void give(final CommandSender sender, @ArgName("voucher") @Suggestion("vouchers") String name, @ArgName("amount") @Suggestion("numbers") int amount, @ArgName("player") @Suggestion("players") PlayerBuilder target, @Optional String argument) {
        final Voucher voucher = this.platform.getVoucher(name);

        if (voucher == null) {
            Message.not_a_voucher.sendMessage(sender);

            return;
        }

        final java.util.Optional<OfflinePlayer> index = target.getTarget();

        index.ifPresentOrElse(offlinePlayer -> {
            final String id = voucher.getStrippedName();

            final Map<String, String> placeholders = new HashMap<>();

            placeholders.put("{voucher}", id);
            placeholders.put("{amount}", String.valueOf(amount));

            if (this.config.getProperty(RootKeys.offline_player_support) && !offlinePlayer.isOnline()) {
                final UUID uuid = offlinePlayer.getUniqueId();

                if (this.holder.hasOfflineVoucher(id)) {
                    this.holder.updateOfflineVoucher(uuid, id, amount);
                } else {
                    this.holder.addOfflineVoucher(uuid, id, amount);
                }

                placeholders.put("{player}", target.getName());

                Message.sent_voucher_offline.sendMessage(sender, placeholders);

                return;
            }

            final Player player = offlinePlayer.getPlayer();

            if (player == null) {
                placeholders.put("{player}", target.getName());

                Message.not_online.sendMessage(sender, placeholders);

                return;
            }

            build(player, amount, argument, voucher).forEach(item -> Methods.addItem(player, item));

            placeholders.put("{player}", player.getName());

            Message.sent_voucher.sendMessage(player, placeholders);
        }, () -> Message.not_online.sendMessage(sender));
    }

    @Command(value = "giveall")
    @Permission(value = "crazyvouchers.giveall", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers giveall [voucher] [amount] [argument]")
    public void all(final CommandSender sender, @ArgName("voucher") @Suggestion("vouchers") String name, @ArgName("amount") @Suggestion("numbers") int amount, @Optional String argument) {
        final Voucher voucher = this.platform.getVoucher(name);

        if (voucher == null) {
            Message.not_a_voucher.sendMessage(sender);

            return;
        }

        for (final Player player : this.server.getOnlinePlayers()) {
            for (final ItemStack item : build(player, amount, argument, voucher)) {
                Methods.addItem(player, item);
            }
        }

        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{voucher}", voucher.getStrippedName());

        Message.sent_everyone_voucher.sendMessage(sender, placeholders);
    }

    private List<ItemStack> build(final Player player, final int amount, final String argument, final Voucher voucher) {
        return voucher.buildItems(player, argument != null ? argument.replace("%random%", "{random}") : "", Math.max(amount, 1));
    }
}