package com.ryderbelserion.crazyvouchers.storage.impl.sql;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.impl.file.FlatFactory;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.api.exceptions.FusionException;
import com.zaxxer.hikari.HikariDataSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    protected final int queryTimeout;
    protected HikariDataSource source;

    public SqlFactory(final int queryTimeout, final CrazyVouchersPlugin plugin, final String impl) {
        super(plugin, impl);

        this.queryTimeout = queryTimeout;
    }

    public abstract String url();

    @Override
    public void init() {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = getConnection()) {
                if (connection == null) return;

                try (final Statement statement = connection.createStatement()) {
                    statement.setQueryTimeout(this.queryTimeout);

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
    public void addUser(final UUID player_uuid) {
        if (hasUser(player_uuid)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("insert into vouchers_players(player_uuid) values (?)")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, player_uuid.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while inserting a user using the <gold>uuid %s</gold>!",
                        this.impl, exception.getMessage(), player_uuid);
            }
        });
    }

    @Override
    public void removeUser(final UUID player_uuid) {
        if (!hasUser(player_uuid)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("delete from vouchers_players where player_uuid=?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, player_uuid.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while deleting a user using the <gold>uuid %s</gold>!",
                        this.impl, exception.getMessage(), player_uuid);
            }
        });
    }

    @Override
    public boolean hasUser(final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from vouchers_players where player_uuid=? limit 1")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, player_uuid.toString());

                try (final ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while checking if <gold>%s</gold> exists!",
                        this.impl, exception.getMessage(), player_uuid);
            }

            return false;
        }).join();
    }

    @Override
    public boolean isVoucherProtected(final UUID voucher_id) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from vouchers_tracker where voucher_id=? limit 1")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, voucher_id.toString());

                try (final ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while checking if the voucher <gold>%s</gold> is protected!",
                        this.impl, exception.getMessage(), voucher_id);
            }

            return false;
        }).join();
    }

    @Override
    public void addProtectedVoucher(final UUID voucher_id, final UUID player_uuid) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("insert into vouchers_tracker(voucher_id, player_uuid, timestamp_redeemed) values (?, ?, ?)")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, voucher_id.toString());
                statement.setString(2, player_uuid.toString());
                statement.setLong(3, Instant.now().getEpochSecond());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while adding a private voucher using the <gold>uuid %s</gold> (<green>voucher_id %s</green>)!",
                        this.impl, exception.getMessage(), player_uuid,  voucher_id);
            }
        });
    }

    @Override
    public void removeProtectedVoucher(final UUID voucher_id) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("delete from vouchers_tracker where voucher_id=?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, voucher_id.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while deleting voucher_id <gold>%s</gold>!",
                        this.impl, exception.getMessage(), voucher_id);
            }
        });
    }

    @Override
    public boolean isVoucherRedeemed(final String voucher_id) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from vouchers_redeemed where voucher_id=? limit 1")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, voucher_id);

                try (final ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while checking if the voucher <gold>%s</gold> is redeemed!",
                        this.impl, exception.getMessage(), voucher_id);
            }

            return false;
        }).join();
    }

    @Override
    public int getVoucherLimit(final String voucher_id, final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select redeem_limit from vouchers_redeemed where voucher_id=? and player_uuid=? limit 1")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, voucher_id);
                statement.setString(2, player_uuid.toString());

                try (final ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt("redeem_limit") : -1;
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while checking the voucher <gold>%s's</gold> redeem limit!",
                        this.impl, exception.getMessage(), voucher_id);
            }

            return -1;
        }).join();
    }

    @Override
    public void incrementVoucherLimit(final String voucher_id, final UUID player_uuid) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("update vouchers_redeemed set redeem_limit = redeem_limit+1 where voucher_id=? and player_uuid=?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, voucher_id);
                statement.setString(2, player_uuid.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while incrementing the redeem_limit for voucher <gold>%s</gold> with player id <gold>%s</gold>!",
                        this.impl, exception.getMessage(), voucher_id, player_uuid);
            }
        });
    }

    @Override
    public int countRedeemedVouchers(final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select count(*) from vouchers_redeemed where player_uuid=?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, player_uuid.toString());

                try (final ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while counting how many times player <gold>%s</gold> opened vouchers!",
                        this.impl, exception.getMessage(), player_uuid);
            }

            return -1;
        }).join();
    }

    @Override
    public void addRedeemedVoucher(final String voucher_id, final UUID player_uuid, final int amount) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("insert into vouchers_redeemed(voucher_id, player_uuid, redeem_limit, timestamp_redeemed) values (?, ?, ?, ?)")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, voucher_id);
                statement.setString(2, player_uuid.toString());
                statement.setInt(3, amount);
                statement.setLong(4, Instant.now().getEpochSecond());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while adding a voucher using the player uuid <gold>%s</gold> (<green>voucher_id %s</green>)!",
                        this.impl, exception.getMessage(), player_uuid,  voucher_id);
            }
        });
    }

    @Override
    public void removeRedeemedVoucher(final String voucher_id) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("delete from vouchers_redeemed where voucher_id=?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, voucher_id);

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while removing the redeemed voucher id <gold>%s</gold>!",
                        this.impl, exception.getMessage(), voucher_id);
            }
        });
    }

    @Override
    public void addRedeemedCode(final String code, final UUID player_uuid) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("insert into codes_redeemed(code, player_uuid, redeem_limit, timestamp_redeemed) values (?, ?, ?, ?)")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, code);
                statement.setString(2, player_uuid.toString());
                statement.setInt(3, 1); // redeemed once already
                statement.setLong(4, Instant.now().getEpochSecond());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while adding the code <gold>%s</gold> with player id <gold>%s</gold>!",
                        this.impl, exception.getMessage(), code, player_uuid);
            }
        });
    }

    @Override
    public void removeRedeemedCode(final String code) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("delete from codes_redeemed where code=?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, code);

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while removing the code <gold>%s</gold>!",
                        this.impl, exception.getMessage(), code);
            }
        });
    }

    @Override
    public boolean isCodeRedeemed(final String code) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from codes_redeemed where code=? limit 1")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, code);

                try (final ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while checking if the voucher code <gold>%s</gold> is redeemed!",
                        this.impl, exception.getMessage(), code);
            }

            return false;
        }).join();
    }

    @Override
    public int getCodeLimit(final String code, final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select redeem_limit from codes_redeemed where code=? and player_uuid=? limit 1")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, code);
                statement.setString(2, player_uuid.toString());

                try (final ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt("redeem_limit") : -1;
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while checking the voucher code <gold>%s's</gold> redeem limit!",
                        this.impl, exception.getMessage(), code);
            }

            return -1;
        }).join();
    }

    @Override
    public void incrementCodeLimit(final String code, final UUID player_uuid) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("update codes_redeemed set redeem_limit = redeem_limit+1 where code=? and player_uuid=?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, code);
                statement.setString(2, player_uuid.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while incrementing the redeem_limit for code <gold>%s</gold> with player id <gold>%s</gold>!",
                        this.impl, exception.getMessage(), code, player_uuid);
            }
        });
    }

    @Override
    public int countRedeemedCodes(final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select count(*) from codes_redeemed where player_uuid=?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, player_uuid.toString());

                try (final ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while counting how many times player <gold>%s</gold> opened codes!",
                        this.impl, exception.getMessage(), player_uuid);
            }

            return -1;
        }).join();
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