package com.badbones69.crazyvouchers.commands.types.admin;

import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.commands.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class CommandTypes extends BaseCommand {

    @Command(value = "types", alias = "list")
    @Permission(value = "voucher.admin", def = PermissionDefault.OP)
    public void types(CommandSender sender) {
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
}