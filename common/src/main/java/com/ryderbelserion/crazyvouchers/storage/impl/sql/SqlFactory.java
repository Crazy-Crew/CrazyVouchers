package com.ryderbelserion.crazyvouchers.storage.impl.sql;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.impl.file.FlatFactory;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.api.exceptions.FusionException;
import com.zaxxer.hikari.HikariDataSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public abstract class SqlFactory extends FlatFactory {

    protected final String create_vouchers_redeemed_table = "create table if not exists vouchers_redeemed(%s, %s, %s, %s)".formatted(
            "voucher_id varchar(36) not null",
            "player_uuid varchar(36) not null",
            "redeem_limit bigint not null", // tracks how many times they redeemed this type of voucher. only useful if the dupe protection option is off.
            "timestamp_redeemed bigint"
    ); // store vouchers for things like a redeem limit

    protected final String index_vouchers_uuid = "create index if not exists idx_vouchers_redeemed_player_uuid on vouchers_redeemed(player_uuid)"; // set index on player uuid

    protected final String index_vouchers_id = "create index if not exists idx_vouchers_redeemed_id on vouchers_redeemed(voucher_id)";

    protected final String create_vouchers_tracker_table = "create table if not exists vouchers_tracker(%s, %s, %s)".formatted(
            "voucher_id varchar(36) not null",
            "player_uuid varchar(36) not null",
            "timestamp_redeemed bigint"
    ); // tracks vouchers for when dupe protection is enabled

    protected final String index_tracker_uuid = "create index if not exists idx_vouchers_tracker_player_uuid on vouchers_tracker(player_uuid)"; // set index on player uuid

    protected final String index_tracker_id = "create index if not exists idx_vouchers_tracker_voucher_id on vouchers_tracker(voucher_id)";

    protected final String create_vouchers_dupe_table = "create table if not exists vouchers_dupe_attempts(%s, %s)".formatted(
            "player_uuid varchar(36) not null",
            "timestamp_attempted bigint"
    ); // track players who tried to use a duplicated voucher.

    protected final String create_users_table = "create table if not exists vouchers_players(%s)".formatted("player_uuid varchar(36) primary key"); // stores user data

    protected final String create_codes_table = "create table if not exists codes_redeemed(%s, %s, %s, %s)".formatted(
            "code varchar(64) not null", // voucher code
            "player_uuid varchar(36) not null", // player uuid,
            "redeem_limit bigint not null", // tracks how many times it was redeemed
            "timestamp_redeemed bigint" // time stamp
    );

    protected final String index_codes_uuid = "create index if not exists idx_code_player_uuid on codes_redeemed(player_uuid)";

    protected final String index_codes_id = "create index if not exists idx_code_id on codes_redeemed(code)";

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
                    statement.addBatch(this.create_users_table);

                    statement.addBatch(this.create_vouchers_tracker_table);
                    statement.addBatch(this.index_tracker_uuid);
                    statement.addBatch(this.index_tracker_id);

                    statement.addBatch(this.create_vouchers_dupe_table);

                    statement.addBatch(this.create_vouchers_redeemed_table);
                    statement.addBatch(this.index_vouchers_uuid);
                    statement.addBatch(this.index_vouchers_id);

                    statement.addBatch(this.create_codes_table);
                    statement.addBatch(this.index_codes_uuid);
                    statement.addBatch(this.index_codes_id);

                    statement.executeBatch();
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while initializing the tables!", exception.getMessage());
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
            this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while closing the connection!", exception.getMessage());
        }
    }

    @Override
    public void save() {

    }

    @Override
    @NullUnmarked
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
            this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while checking if a table exists!", exception.getMessage());

            return false;
        }
    }
}