package com.ryderbelserion.crazyvouchers.storage.impl.sql;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.impl.file.FlatFactory;
import com.ryderbelserion.fusion.api.exceptions.FusionException;
import com.zaxxer.hikari.HikariDataSource;
import org.jspecify.annotations.NonNull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public abstract class SqlFactory extends FlatFactory {

    protected final String create_vouchers_redeemed_table = "create table if not exists vouchers_redeemed(%s, %s, %s, %s, %s)".formatted(
            "voucher_id varchar(36) primary key",
            "player_uuid varchar(36) not null",
            "redeem_limit bigint not null default 0", // tracks how many times they redeemed this type of voucher.
            "timestamp_redeemed bigint",
            "foreign key(player_uuid) references players(player_uuid) on delete cascade"
    ); // store vouchers for things like a redeem limit

    protected final String index_vouchers_table_uuid = "create index idx_voucher_player_uuid on vouchers_redeemed(player_uuid)"; // set index on player uuid

    protected final String duplicate_voucher_tracker = "create table if not exists duplicate_voucher_tracker(%s, %s, %s, %s)".formatted(
            "voucher_id varchar(36) primary key",
            "player_uuid varchar(36) not null",
            "timestamp_redeemed bigint",
            "foreign key(player_uuid) references players(player_uuid) on delete cascade"
    ); // store vouchers for dupe tracking

    protected final String index_vouchers_tracker_uuid = "create index idx_voucher_tracker_player_uuid on duplicate_voucher_tracker(player_uuid)"; // set index on player uuid

    protected final String create_vouchers_dupe_table = "create table if not exists duplicate_voucher_attempts(%s, %s)".formatted(
            "player_uuid varchar(36) primary key",
            "timestamp_attempted bigint"
    ); // track players who tried to use a duplicated voucher.

    protected final String create_users_table = "create table if not exists players(%s)".formatted("player_uuid varchar(36) primary key"); // stores user data

    protected final String create_codes_table = "create table if not exists codes_redeemed(%s, %s, %s, %s, %s)".formatted(
            "code varchar(64) primary key", // voucher code
            "player_uuid varchar(36) not null", // player uuid,
            "redeem_limit bigint not null default 0", // tracks how many times it was redeemed
            "timestamp_redeemed bigint", // time stamp
            "foreign key(player_uuid) references players(player_uuid) on delete cascade"
    );

    protected final String index_codes_table = "create index idx_code_player_uuid on codes_redeemed(player_uuid)"; // set index on voucher code as well1

    protected HikariDataSource source;

    public SqlFactory(final CrazyVouchersPlugin plugin, final String impl) {
        super(plugin, impl);
    }

    public abstract String url();

    @Override
    public void init() {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = getConnection()) {
                if (connection == null) return;

                try (final Statement statement = connection.createStatement()) {
                    statement.addBatch(this.create_vouchers_dupe_table);
                    statement.addBatch(this.index_vouchers_table_uuid);

                    statement.addBatch(this.duplicate_voucher_tracker);
                    statement.addBatch(this.index_vouchers_tracker_uuid);

                    statement.addBatch(this.create_vouchers_redeemed_table);

                    statement.addBatch(this.create_codes_table);
                    statement.addBatch(this.index_codes_table);

                    statement.addBatch(this.create_users_table);

                    statement.executeBatch();
                }
            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void stop() {
        try {
            final Connection connection = getConnection();

            if (connection != null) {
                connection.close();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void save() {

    }

    public Connection getConnection() throws SQLException {
        if (this.source.isClosed()) {
            throw new FusionException("Failed to get connection from pool. (Source returned closed)");
        }

        return this.source.getConnection();
    }

    @Override
    public boolean tableExists(@NonNull final String table) {
        try (final ResultSet resultSet = getConnection().getMetaData().getTables(
                null,
                null,
                table,
                null
        )) {
            return resultSet.next();
        } catch (SQLException exception) {
            exception.printStackTrace();

            return false;
        }
    }
}