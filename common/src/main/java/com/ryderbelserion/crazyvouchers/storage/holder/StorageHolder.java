package com.ryderbelserion.crazyvouchers.storage.holder;

import com.ryderbelserion.crazyvouchers.storage.impl.ConnectionFactory;
import org.jspecify.annotations.NullMarked;
import java.util.UUID;

@NullMarked
public final class StorageHolder {

    private final ConnectionFactory factory;

    public StorageHolder(final ConnectionFactory factory) {
        this.factory = factory;
    }

    public StorageHolder init() {
        this.factory.init();

        return this;
    }

    public StorageHolder save() {
        this.factory.save();

        return this;
    }

    public StorageHolder stop() {
        this.factory.stop();

        return this;
    }

    // dupe protection start
    public boolean isVoucherProtected(final UUID voucher_id) {
        return this.factory.isVoucherProtected(voucher_id);
    }

    public boolean isVoucherProtected(final String voucher_id) {
        return isVoucherProtected(UUID.fromString(voucher_id));
    }

    public void removeProtectedVoucher(final String voucher_id) {
        removeProtectedVoucher(UUID.fromString(voucher_id));
    }

    public void removeProtectedVoucher(final UUID voucher_id) {
        this.factory.removeProtectedVoucher(voucher_id);
    }

    public void addProtectedVoucher(final UUID voucher_id, final UUID player_uuid) {
        this.factory.addProtectedVoucher(voucher_id, player_uuid);
    }

    public void addProtectedVoucher(final String voucher_id, final String player_uuid) {
        addProtectedVoucher(UUID.fromString(voucher_id), UUID.fromString(player_uuid));
    }
    // dupe protection end

    // generic voucher checks start
    public boolean isVoucherRedeemed(final String voucher_id) {
        return this.factory.isVoucherRedeemed(voucher_id);
    }

    public void removeRedeemedVoucher(final String voucher_id) {
        this.factory.removeRedeemedVoucher(voucher_id);
    }

    public void addRedeemedVoucher(final String voucher_id, final UUID player_uuid, final int amount) {
        this.factory.addRedeemedVoucher(voucher_id, player_uuid, amount);
    }

    public void addRedeemedVoucher(final String voucher_id, final UUID player_uuid) {
        addRedeemedVoucher(voucher_id, player_uuid, 1);
    }

    public int getVoucherLimit(final String voucher_id, final UUID player_uuid) {
        return this.factory.getVoucherLimit(voucher_id, player_uuid);
    }

    public void incrementVoucherLimit(final String voucher_id, final UUID player_uuid) {
        this.factory.incrementVoucherLimit(voucher_id, player_uuid);
    }
    // generic voucher checks end

    // generic code checks start
    public boolean isCodeRedeemed(final String code) {
        return this.factory.isCodeRedeemed(code);
    }

    public void removeRedeemedCode(final String code) {
        this.factory.removeRedeemedCode(code);
    }

    public void addRedeemedCode(final String code, final UUID player_uuid) {
        this.factory.addRedeemedCode(code, player_uuid);
    }

    public void addRedeemedCode(final String code, final String player_uuid) {
        addRedeemedCode(code, UUID.fromString(player_uuid));
    }

    public int getCodeLimit(final String code, final UUID player_uuid) {
        return this.factory.getCodeLimit(code, player_uuid);
    }

    public void incrementCodeLimit(final String code, final UUID player_uuid) {
        this.factory.incrementCodeLimit(code, player_uuid);
    }
    // generic code checks stop

    public boolean tableExists(final String table) {
        return this.factory.tableExists(table);
    }
}