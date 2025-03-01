package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Permission;
import dev.triumphteam.cmd.core.annotations.Syntax;
import dev.triumphteam.cmd.core.enums.Mode;
import org.bukkit.command.CommandSender;
import java.util.List;

public class CommandTypes extends BaseCommand {

    @Command(value = "types", alias = "list")
    @Permission(value = "crazyvouchers.types", def = Mode.OP)
    @Syntax("/crazyvouchers types")
    public void base(final CommandSender sender) {
        StringBuilder vouchers = new StringBuilder();
        StringBuilder codes = new StringBuilder();

        final List<Voucher> voucherCache = this.crazyManager.getVouchers();

        for (Voucher voucher : voucherCache) {
            vouchers.append("&a").append(voucher.getName()).append("&8, ");
        }

        final List<VoucherCode> voucherCodes = this.crazyManager.getVoucherCodes();

        for (VoucherCode code : voucherCodes) {
            codes.append("&a").append(code.getCode()).append("&8, ");
        }

        vouchers = new StringBuilder((vouchers.isEmpty()) ? "&cNone" : vouchers.substring(0, vouchers.length() - 2));
        codes = new StringBuilder((codes.isEmpty()) ? "&cNone" : codes.substring(0, codes.length() - 2));

        sender.sendMessage(MsgUtils.color("&e&lVouchers #" + voucherCache.size() + ":&f " + vouchers));
        sender.sendMessage(MsgUtils.color("&e&lVoucher Codes #" + voucherCodes.size() + ":&f " + codes));
    }
}