package com.ryderbelserion.crazyvouchers.storage.holder;

import com.ryderbelserion.crazyvouchers.storage.impl.ConnectionFactory;
import org.jspecify.annotations.NullMarked;
import us.crazycrew.crazyvouchers.api.storage.IStorageHolder;
import java.util.UUID;

@NullMarked
public final class StorageHolder implements IStorageHolder {

    private final ConnectionFactory factory;

    public StorageHolder(final ConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public StorageHolder init() {
        this.factory.init();

        return this;
    }

    @Override
    public StorageHolder save() {
        this.factory.save();

        return this;
    }

    @Override
    public StorageHolder stop() {
        this.factory.stop();

        return this;
    }

    // dupe protection start
    @Override
    public boolean isVoucherProtected(final UUID voucher_id) {
        return this.factory.isVoucherProtected(voucher_id);
    }

    @Override
    public boolean isVoucherProtected(final String voucher_id) {
        return isVoucherProtected(UUID.fromString(voucher_id));
    }

    @Override
    public void removeProtectedVoucher(final String voucher_id) {
        removeProtectedVoucher(UUID.fromString(voucher_id));
    }

    @Override
    public void removeProtectedVoucher(final UUID voucher_id) {
        this.factory.removeProtectedVoucher(voucher_id);
    }

    @Override
    public void addProtectedVoucher(final UUID voucher_id, final UUID player_uuid) {
        this.factory.addProtectedVoucher(voucher_id, player_uuid);
    }

    @Override
    public void addProtectedVoucher(final String voucher_id, final String player_uuid) {
        addProtectedVoucher(UUID.fromString(voucher_id), UUID.fromString(player_uuid));
    }
    // dupe protection end

    // generic voucher checks start
    @Override
    public boolean isVoucherRedeemed(final String voucher_id) {
        return this.factory.isVoucherRedeemed(voucher_id);
    }

    @Override
    public void removeRedeemedVoucher(final String voucher_id) {
        this.factory.removeRedeemedVoucher(voucher_id);
    }

    @Override
    public void addRedeemedVoucher(final String voucher_id, final UUID player_uuid, final int amount) {
        this.factory.addRedeemedVoucher(voucher_id, player_uuid, amount);
    }

    @Override
    public void addRedeemedVoucher(final String voucher_id, final UUID player_uuid) {
        addRedeemedVoucher(voucher_id, player_uuid, 1);
    }

    @Override
    public int getVoucherLimit(final String voucher_id, final UUID player_uuid) {
        return this.factory.getVoucherLimit(voucher_id, player_uuid);
    }

    @Override
    public void incrementVoucherLimit(final String voucher_id, final UUID player_uuid) {
        this.factory.incrementVoucherLimit(voucher_id, player_uuid);
    }
    // generic voucher checks end

    // generic code checks start
    @Override
    public boolean isCodeRedeemed(final String code) {
        return this.factory.isCodeRedeemed(code);
    }

    @Override
    public void removeRedeemedCode(final String code) {
        this.factory.removeRedeemedCode(code);
    }

    @Override
    public void addRedeemedCode(final String code, final UUID player_uuid) {
        this.factory.addRedeemedCode(code, player_uuid);
    }

    @Override
    public void addRedeemedCode(final String code, final String player_uuid) {
        addRedeemedCode(code, UUID.fromString(player_uuid));
    }

    @Override
    public int getCodeLimit(final String code, final UUID player_uuid) {
        return this.factory.getCodeLimit(code, player_uuid);
    }

    @Override
    public void incrementCodeLimit(final String code, final UUID player_uuid) {
        this.factory.incrementCodeLimit(code, player_uuid);
    }
    // generic code checks stop

    public boolean tableExists(final String table) {
        return this.factory.tableExists(table);
    }
}