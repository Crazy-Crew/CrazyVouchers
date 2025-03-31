package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.commands.BaseCommand;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Permission;
import dev.triumphteam.cmd.core.annotations.Syntax;
import dev.triumphteam.cmd.core.enums.Mode;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandTypes extends BaseCommand {

    @Command(value = "types", alias = "list")
    @Permission(value = "crazyvouchers.types", def = Mode.OP)
    @Syntax("/crazyvouchers types")
    public void types(final CommandSender sender) {
        final List<Voucher> vouchers = this.crazyManager.getVouchers();
        final List<VoucherCode> codes = this.crazyManager.getVoucherCodes();

        final int voucherCount = vouchers.size();
        final int codeCount = codes.size();

        final List<String> brokeVouchers = this.crazyManager.getBrokenVouchers();
        final List<String> brokeCodes = this.crazyManager.getBrokenVoucherCodes();

        final int inactiveVouchers = brokeVouchers.size();
        final int inactiveCodes = brokeCodes.size();

        final int activeVouchers = voucherCount - inactiveVouchers;
        final int activeCodes = codeCount - inactiveCodes;

        final List<String> coloredVouchers = new ArrayList<>();

        brokeVouchers.forEach(voucher -> coloredVouchers.add("<red>" + voucher + "</red>"));
        vouchers.forEach(voucher -> {
            final String name = voucher.getName();

            if (coloredVouchers.contains(name)) return;

            coloredVouchers.add("<green>" + name + "</green>");
        });

        final List<String> coloredCodes = new ArrayList<>();
        brokeCodes.forEach(code -> coloredCodes.add("<red>" + code + "</red>"));
        codes.forEach(code -> {
            final String name = code.getName();

            if (coloredCodes.contains(name)) return;

            coloredCodes.add("<green>" + name + "</green>");
        });

        Messages.vouchers_list.sendMessage(sender, new HashMap<>() {{
            put("{total_vouchers}", String.valueOf(voucherCount));
            put("{total_codes}", String.valueOf(codeCount));

            put("{active_vouchers}", String.valueOf(activeVouchers));
            put("{active_codes}", String.valueOf(activeCodes));

            put("{broken_vouchers}", String.valueOf(inactiveVouchers));
            put("{broken_codes}", String.valueOf(inactiveVouchers));

            put("{vouchers}", String.valueOf(coloredVouchers));
            put("{codes}", String.valueOf(coloredCodes));
        }});
    }
}