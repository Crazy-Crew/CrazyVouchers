package com.ryderbelserion.crazyvouchers.storage.holder;

import us.crazycrew.crazyvouchers.api.objects.VoucherHistory;
import com.ryderbelserion.crazyvouchers.storage.impl.ConnectionFactory;
import com.ryderbelserion.fusion.api.exceptions.FusionException;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import us.crazycrew.crazyvouchers.api.objects.VoucherSimple;
import us.crazycrew.crazyvouchers.api.storage.IStorageHolder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@NullMarked
public final class StorageHolder extends IStorageHolder {

    private final ConnectionFactory factory;

    public StorageHolder(final ConnectionFactory factory) {
        this.factory = factory;
    }

    @ApiStatus.Internal
    @Override
    public Connection getConnection() {
        try {
            return this.factory.getConnection();
        } catch (final SQLException exception) {
            throw new FusionException("Failed to get connection. (%s)".formatted(exception.getMessage()));
        }
    }

    @Override
    public IStorageHolder migrate() {
        this.factory.migrate();

        return this;
    }

    @Override
    public StorageHolder init() {
        this.factory.init();

        return this;
    }

    @Override
    public IStorageHolder post() {
        this.factory.post();

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

    @Override
    public void removeUser(final UUID player_uuid) {
        this.factory.removeUser(player_uuid);
    }

    @Override
    public void addUser(final UUID player_uuid) {
        this.factory.addUser(player_uuid);
    }

    @Override
    public boolean hasUser(final UUID player_uuid) {
        return this.factory.hasUser(player_uuid);
    }

    @Override
    public void addOfflineVoucher(final UUID player_uuid, final String voucher_id, final int amount) {
        this.factory.addOfflineVoucher(player_uuid, voucher_id, Math.max(1, amount));
    }

    @Override
    public void updateOfflineVoucher(final UUID player_uuid, final String voucher_id, final int amount) {
        this.factory.updateOfflineVoucher(player_uuid, voucher_id, Math.max(1, amount));
    }

    @Override
    public void removeOfflineVoucher(final VoucherSimple voucher) {
        this.factory.removeOfflineVoucher(voucher);
    }

    @Override
    public void removeOfflineVouchers(final List<VoucherSimple> vouchers) {
        this.factory.removeOfflineVouchers(vouchers);
    }

    @Override
    public List<VoucherSimple> getOfflineVouchers(final UUID player_uuid) {
        return this.factory.getOfflineVouchers(player_uuid);
    }

    @Override
    public boolean hasOfflineVoucher(final String voucher_id) {
        return this.factory.hasOfflineVoucher(voucher_id);
    }

    @Override
    public boolean hasOfflinePlayer(final UUID player_uuid) {
        return this.factory.hasOfflinePlayer(player_uuid);
    }

    // dupe protection start
    @Override
    public boolean isVoucherProtected(final UUID voucher_id) {
        return this.factory.isVoucherProtected(voucher_id);
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
        this.factory.addRedeemedVoucher(voucher_id, player_uuid, Math.max(1, amount));
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
    public int getCodeLimit(final String code, final UUID player_uuid) {
        return this.factory.getCodeLimit(code, player_uuid);
    }

    @Override
    public void incrementCodeLimit(final String code, final UUID player_uuid) {
        this.factory.incrementCodeLimit(code, player_uuid);
    }

    @Override
    public List<VoucherHistory> getRedeemedVouchersByPage(final UUID player_uuid, final int page, final int perPage) {
        return this.factory.getRedeemedVouchersByPage(player_uuid, page, perPage);
    }

    @Override
    public List<VoucherHistory> getRedeemedVouchers(final UUID player_uuid) {
        return this.factory.getRedeemedVouchers(player_uuid);
    }

    @Override
    public int countRedeemedVouchers(final UUID player_uuid) {
        return this.factory.countRedeemedVouchers(player_uuid);
    }

    @Override
    public List<VoucherHistory> getRedeemedCodesByPage(final UUID player_uuid, final int page, final int perPage) {
        return this.factory.getRedeemedCodesByPage(player_uuid, page, perPage);
    }

    @Override
    public List<VoucherHistory> getRedeemedCodes(final UUID player_uuid) {
        return this.factory.getRedeemedCodes(player_uuid);
    }

    @Override
    public int countRedeemedCodes(final UUID player_uuid) {
        return this.factory.countRedeemedCodes(player_uuid);
    }
    // generic code checks stop

    public boolean tableExists(final String table) {
        return this.factory.tableExists(table);
    }
}