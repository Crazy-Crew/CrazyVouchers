package us.crazycrew.crazyvouchers.api.storage;

import java.util.UUID;

public interface IStorageHolder {

    IStorageHolder init();

    IStorageHolder save();

    IStorageHolder stop();

    boolean isVoucherProtected(final UUID voucher_id);

    boolean isVoucherProtected(final String voucher_id);

    void removeProtectedVoucher(final String voucher_id);

    void removeProtectedVoucher(final UUID voucher_id);

    void addProtectedVoucher(final UUID voucher_id, final UUID player_uuid);

    void addProtectedVoucher(final String voucher_id, final String player_uuid);

    boolean isVoucherRedeemed(final String voucher_id);

    void removeRedeemedVoucher(final String voucher_id);

    void addRedeemedVoucher(final String voucher_id, final UUID player_uuid, final int amount);

    void addRedeemedVoucher(final String voucher_id, final UUID player_uuid);

    int getVoucherLimit(final String voucher_id, final UUID player_uuid);

    void incrementVoucherLimit(final String voucher_id, final UUID player_uuid);

    boolean isCodeRedeemed(final String code);

    void removeRedeemedCode(final String code);

    void addRedeemedCode(final String code, final UUID player_uuid);

    void addRedeemedCode(final String code, final String player_uuid);

    int getCodeLimit(final String code, final UUID player_uuid);

    void incrementCodeLimit(final String code, final UUID player_uuid);
}