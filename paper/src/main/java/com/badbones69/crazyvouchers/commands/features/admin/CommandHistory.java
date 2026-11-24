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
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import us.crazycrew.crazyvouchers.api.objects.VoucherHistory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CommandHistory extends BaseCommand {

    @Command(value = "history")
    @Permission(value = "crazyvouchers.history", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers history [player [types] [page]")
    public void give(final CommandSender sender, @ArgName("player") @Suggestion("players") PlayerBuilder target, @ArgName("type") @Suggestion("types") final String type, @ArgName("page") @Suggestion("pages") final int page) {
        final Optional<Player> index = target.getTarget();

        index.ifPresentOrElse(player -> {
            final UUID uuid = player.getUniqueId();
            final String name = player.getName();

            if (!this.holder.hasUser(uuid)) {
                Message.storage_user_does_not_exist.sendMessage(player);

                return;
            }

            final int perPage = this.config.getProperty(RootKeys.history_per_page);

            final List<VoucherHistory> vouchers = new ArrayList<>();
            int maxPages = 1;

            switch (type.toLowerCase()) {
                case "voucher" -> {
                    maxPages = Math.max(1, Math.ceilDiv(this.holder.countRedeemedVouchers(uuid), perPage));

                    vouchers.addAll(this.holder.getRedeemedVouchersByPage(uuid, page, perPage));
                }

                case "code" -> {
                    maxPages = Math.max(1, Math.ceilDiv(this.holder.countRedeemedCodes(uuid), perPage));

                    vouchers.addAll(this.holder.getRedeemedCodesByPage(uuid, page, perPage));
                }
            }

            final Map<String, String> placeholders = new HashMap<>();

            final List<String> messages = new ArrayList<>();

            if (vouchers.isEmpty()) {
                messages.add(type.equalsIgnoreCase("voucher") ? Message.history_no_vouchers_found.getMessage(player, Map.of("{player}", name)) : Message.history_no_codes_found.getMessage(player, Map.of("{player}", name)));
            } else {
                vouchers.forEach(voucher -> {
                    final String format = Message.history_format.getMessage(player, Map.of(
                            "{position}", String.valueOf(voucher.getPosition()),
                            "{voucher_id}", voucher.getVoucherId(),
                            "{redeem_limit}", String.valueOf(voucher.getRedeemLimit()),
                            "{timestamp}", voucher.getTimestamp(),
                            "{type}", type
                    ));

                    messages.add(format);
                });
            }

            final String message = StringUtils.toString(messages);

            placeholders.putIfAbsent("{header}", Message.history_header.getMessage(player, Map.of(
                    "{back}", Message.history_back_text.getMessage(),
                    "{next}", Message.history_next_text.getMessage(),
                    "{player}", name,
                    "{page}", String.valueOf(page),
                    "{max_pages}", String.valueOf(maxPages),
                    "{type}", type
            )));
            placeholders.putIfAbsent("{footer}", Message.history_footer.getMessage(player, Map.of(
                    "{back}", Message.history_back_text.getMessage(),
                    "{next}", Message.history_next_text.getMessage(),
                    "{player}", name,
                    "{page}", String.valueOf(page),
                    "{max_pages}", String.valueOf(maxPages),
                    "{type}", type
            )));

            placeholders.putIfAbsent("{vouchers}", message);

            Message.history_body.sendMessage(player, placeholders);
        }, () -> Message.not_online.sendMessage(sender));
    }
}