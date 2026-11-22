package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.paper.builders.items.PlayerBuilder;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.ArgName;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import dev.triumphteam.cmd.core.annotations.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import us.crazycrew.crazyvouchers.api.objects.VoucherHistory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandHistory extends BaseCommand {

    @Command(value = "history")
    @Permission(value = "crazyvouchers.history", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers history [player]")
    public void give(final CommandSender sender, @ArgName("player") @Suggestion("players") PlayerBuilder target) {
        target.getPlayer().ifPresentOrElse(player -> {
            final UUID uuid = player.getUniqueId();
            final String name = player.getName();

            if (!this.holder.hasUser(uuid)) {
                Message.storage_user_does_not_exist.sendMessage(sender);

                return;
            }

            final List<VoucherHistory> vouchers = this.holder.getRedeemedVouchersByPage(uuid, 1, 5);
            final int maxPages = calculateMaxPages(5, this.holder.getRedeemedVouchers(uuid)); //todo() find an alternative eventually.

            final Map<String, String> placeholders = new HashMap<>();

            final List<String> messages = new ArrayList<>();

            vouchers.forEach(voucher -> {
                final String format = Message.history_format.getMessage(sender, Map.of(
                        "{position}", String.valueOf(voucher.getPosition()),
                        "{voucher_id}", voucher.getVoucherId(),
                        "{redeem_limit}", String.valueOf(voucher.getRedeemLimit()),
                        "{timestamp}", voucher.getTimestamp()
                ));

                messages.add(format);
            });

            final String message = StringUtils.toString(messages);

            placeholders.putIfAbsent("{header}", Message.history_header.getMessage(sender, Map.of(
                    "{back}", Message.history_back_text.getMessage(),
                    "{next}", Message.history_next_text.getMessage(),
                    "{player}", name,
                    "{page}", String.valueOf(1),
                    "{max_pages}", String.valueOf(maxPages)
            )));
            placeholders.putIfAbsent("{footer}", Message.history_footer.getMessage(sender, Map.of(
                    "{back}", Message.history_back_text.getMessage(),
                    "{next}", Message.history_next_text.getMessage(),
                    "{player}", name,
                    "{page}", String.valueOf(1),
                    "{max_pages}", String.valueOf(maxPages)
            )));

            placeholders.putIfAbsent("{vouchers}", message);

            Message.history_body.sendMessage(sender, placeholders);
        }, () -> Message.not_online.sendMessage(sender));
    }

    public int calculateMaxPages(final int perPage, final List<VoucherHistory> vouchers) {
        return Math.max(1, Math.ceilDiv(vouchers.size(), perPage));
    }
}