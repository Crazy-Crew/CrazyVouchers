package us.crazycrew.crazyvouchers.api.storage;

import org.jetbrains.annotations.ApiStatus;
import us.crazycrew.crazyvouchers.api.objects.VoucherHistory;
import us.crazycrew.crazyvouchers.api.objects.VoucherSimple;

import java.util.List;
import java.util.UUID;

@ApiStatus.Internal
public abstract class AbstractUserHolder {

    public abstract void removeUser(final UUID player_uuid);

    public abstract void addUser(final UUID player_uuid);

    public abstract boolean hasUser(final UUID player_uuid);

    public abstract void addOfflineVoucher(final UUID player_uuid, final String voucher_id, final int amount);

    public abstract void updateOfflineVoucher(final UUID player_uuid, final String voucher_id, final int amount);

    public abstract void removeOfflineVouchers(final List<VoucherSimple> vouchers);

    public abstract void removeOfflineVoucher(final VoucherSimple voucher);

    public abstract List<VoucherSimple> getOfflineVouchers(final UUID player_uuid);

    public abstract boolean hasOfflinePlayer(final UUID player_uuid);

    public abstract boolean hasOfflineVoucher(final String voucher_id);

    public abstract boolean isVoucherProtected(final UUID voucher_id);

    public boolean isVoucherProtected(final String voucher_id) {
        return isVoucherProtected(UUID.fromString(voucher_id));
    }

    public void removeProtectedVoucher(final String voucher_id) {
        removeProtectedVoucher(UUID.fromString(voucher_id));
    }

    public abstract void removeProtectedVoucher(final UUID voucher_id);

    public abstract void addProtectedVoucher(final UUID voucher_id, final UUID player_uuid);

    public void addProtectedVoucher(final String voucher_id, final String player_uuid) {
        addProtectedVoucher(UUID.fromString(voucher_id), UUID.fromString(player_uuid));
    }

    public abstract boolean isVoucherRedeemed(final String voucher_id);

    public abstract void removeRedeemedVoucher(final String voucher_id);

    public abstract void addRedeemedVoucher(final String voucher_id, final UUID player_uuid, final int amount);

    public void addRedeemedVoucher(final String voucher_id, final UUID player_uuid) {
        addRedeemedVoucher(voucher_id, player_uuid, 1);
    }

    public abstract List<VoucherHistory> getRedeemedVouchersByPage(final UUID player_uuid, final int page, final int perPage);

    public abstract List<VoucherHistory> getRedeemedVouchers(final UUID player_uuid);

    public abstract int countRedeemedVouchers(final UUID player_uuid);

    public abstract int getVoucherLimit(final String voucher_id, final UUID player_uuid);

    public abstract void incrementVoucherLimit(final String voucher_id, final UUID player_uuid);

    public abstract boolean isCodeRedeemed(final String code);

    public abstract void removeRedeemedCode(final String code);

    public abstract void addRedeemedCode(final String code, final UUID player_uuid);

    public void addRedeemedCode(final String code, final String player_uuid) {
        addRedeemedCode(code, UUID.fromString(player_uuid));
    }

    public abstract int getCodeLimit(final String code, final UUID player_uuid);

    public abstract void incrementCodeLimit(final String code, final UUID player_uuid);

    public abstract List<VoucherHistory> getRedeemedCodesByPage(final UUID player_uuid, final int page, final int perPage);

    public abstract List<VoucherHistory> getRedeemedCodes(final UUID player_uuid);

    public abstract int countRedeemedCodes(final UUID player_uuid);
}