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

    private final Map<UUID, Map<Integer, VoucherHistory>> pages = new HashMap<>();

    @Command(value = "history")
    @Permission(value = "crazyvouchers.history", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers history [player]")
    public void give(final CommandSender sender, @ArgName("player") @Suggestion("players") PlayerBuilder target) {
        target.getPlayer().ifPresentOrElse(player -> {
            final UUID uuid = player.getUniqueId();

            if (!this.holder.hasUser(uuid)) {
                //todo() send message

                return;
            }

            final List<VoucherHistory> vouchers = this.holder.getRedeemedVouchers(uuid);

            final List<String> values = new ArrayList<>();

            if (vouchers.isEmpty()) {
                values.add("N/A");
            } else {
                vouchers.forEach(voucher -> values.add("Voucher: %s, Limit: %s, Timestamp: %s".formatted(voucher.getVoucherId(), voucher.getRedeemLimit(), voucher.getTimestamp())));
            }

            final String message = this.fusion.replacePlaceholders(StringUtils.toString(List.of(
                    "=== === === {player} === === ===",
                    "",
                    "{vouchers}",
                    "",
                    "=== === === {player} === === ==="
            )), Map.of(
                    "{player}", player.getName(),
                    "{vouchers}", StringUtils.toString(values)
            ));

            sender.sendRichMessage(message);
        }, () -> Message.not_online.sendMessage(sender));
    }
}