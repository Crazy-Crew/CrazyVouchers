package com.ryderbelserion.crazyvouchers.storage.impl;

import org.jspecify.annotations.NullMarked;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@NullMarked
public abstract class ConnectionFactory {

    public abstract Connection getConnection() throws SQLException;

    public abstract String getImpl();

    public abstract void migrate();

    public abstract void init();

    public abstract void post();

    public abstract void stop();

    public abstract void save();

    public abstract boolean isVoucherProtected(final UUID voucher_id);

    public abstract void addProtectedVoucher(final UUID voucher_id, final UUID player_uuid);

    public abstract void removeProtectedVoucher(final UUID voucher_id);

    public abstract void removeUser(final UUID player_uuid);

    public abstract void addUser(final UUID player_uuid);

    public abstract boolean hasUser(final UUID player_uuid);

    public abstract void addRedeemedVoucher(final String voucher_id, final UUID player_uuid, final int amount);

    public abstract void removeRedeemedVoucher(final String voucher_id);

    public abstract boolean isVoucherRedeemed(final String voucher_id);

    public abstract int getVoucherLimit(final String voucher_id, final UUID player_uuid);

    public abstract void incrementVoucherLimit(final String voucher_id, final UUID player_uuid);

    public abstract void addRedeemedCode(final String code, final UUID player_uuid);

    public abstract void removeRedeemedCode(final String code);

    public abstract boolean isCodeRedeemed(final String code);

    public abstract int getCodeLimit(final String code, final UUID player_uuid);

    public abstract void incrementCodeLimit(final String code, final UUID player_uuid);

    public boolean tableExists(final String table) {
        return true;
    }
}