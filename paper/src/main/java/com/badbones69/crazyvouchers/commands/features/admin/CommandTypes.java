package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.commands.BaseCommand;
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
    public void types(final CommandSender sender) {
        StringBuilder vouchers = new StringBuilder();
        StringBuilder codes = new StringBuilder();

        final List<Voucher> voucherCache = this.crazyManager.getVouchers();

        for (Voucher voucher : voucherCache) {
            vouchers.append("<green>").append(voucher.getName()).append("<dark_gray>, ");
        }

        final List<VoucherCode> voucherCodes = this.crazyManager.getVoucherCodes();

        for (VoucherCode code : voucherCodes) {
            codes.append("<green>").append(code.getCode()).append("<dark_gray>, ");
        }

        vouchers = new StringBuilder((vouchers.isEmpty()) ? "<red>None" : vouchers.substring(0, vouchers.length() - 2));
        codes = new StringBuilder((codes.isEmpty()) ? "<red>None" : codes.substring(0, codes.length() - 2));

        //sender.sendMessage(MsgUtils.color("<yellow>Vouchers #" + voucherCache.size() + ":&f " + vouchers));
        //sender.sendMessage(MsgUtils.color("<yellow>Voucher Codes #" + voucherCodes.size() + ":&f " + codes));
    }
}