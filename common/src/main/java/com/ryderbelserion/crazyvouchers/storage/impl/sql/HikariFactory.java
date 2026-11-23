package com.ryderbelserion.crazyvouchers.storage.impl.sql;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.StorageCredentials;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.api.exceptions.FusionException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jspecify.annotations.NonNull;
import us.crazycrew.crazyvouchers.api.objects.VoucherHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class HikariFactory extends SqlFactory {

    private final StorageCredentials credentials;

    private HikariDataSource source;

    public HikariFactory(
            @NonNull final StorageCredentials credentials,
            @NonNull final CrazyVouchersPlugin plugin,
            @NonNull final String impl
    ) {
        super(plugin, impl);

        this.credentials = credentials;
    }

    public abstract String getDriverIdentifier();

    public abstract String getJDBCIdentifier();

    public abstract int getDefaultPort();

    @Override
    public void migrate() {

    }

    @Override
    public void post() {

    }

    @Override
    public String url() {
        return "";
    }

    protected void configure(
            @NonNull final HikariConfig config,
            @NonNull final String database,
            @NonNull final String username,
            @NonNull final String password,
            @NonNull final String address,
            final int port
    ) {
        config.setDriverClassName(getDriverIdentifier());

        config.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s", getJDBCIdentifier(), address, port == -1 ? getDefaultPort() : port, database));

        config.setUsername(username);
        config.setPassword(password);
    }

    @Override
    public void init() {
        final HikariConfig config = new HikariConfig();

        config.setPoolName("corecraft-hikari");

        configure(
                config,
                this.credentials.database(),
                this.credentials.username(),
                this.credentials.password(),
                this.credentials.address(),
                this.credentials.port()
        );

        config.setMaximumPoolSize(this.credentials.maxPoolSize());
        config.setMinimumIdle(this.credentials.minIdle());
        config.setMaxLifetime(this.credentials.maxLifetime());
        config.setKeepaliveTime(this.credentials.heartbeat());
        config.setConnectionTimeout(this.credentials.connectionTimeout());

        config.setInitializationFailTimeout(-1);

        this.source = new HikariDataSource(config);

        super.init();
    }

    @Override
    public void stop() {
        if (this.source != null) {
            this.source.close();
        }
    }

    @Override
    public void removeUser(final UUID player_uuid) {
        if (!hasUser(player_uuid)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("delete from vouchers_players where player_uuid=?")) {
                statement.setString(1, player_uuid.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while deleting a user using the <gold>uuid %s</gold>!",
                        this.impl, exception.getMessage(), player_uuid);
            }
        });
    }

    @Override
    public void addUser(final UUID player_uuid) {
        if (hasUser(player_uuid)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("insert into vouchers_players(player_uuid) values (?)")) {
                statement.setString(1, player_uuid.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while inserting a user using the <gold>uuid %s</gold>!",
                        this.impl, exception.getMessage(), player_uuid);
            }
        });
    }

    @Override
    public boolean hasUser(final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from vouchers_players where player_uuid=? limit 1")) {
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
    public void removeProtectedVoucher(final UUID voucher_id) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("delete from vouchers_tracker where voucher_id=?")) {
                statement.setString(1, voucher_id.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while deleting voucher_id <gold>%s</gold>!",
                        this.impl, exception.getMessage(), voucher_id);
            }
        });
    }

    @Override
    public void addProtectedVoucher(final UUID voucher_id, final UUID player_uuid) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("insert into vouchers_tracker(voucher_id, player_uuid, timestamp_redeemed) values (?, ?, ?)")) {
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
    public boolean isVoucherRedeemed(final String voucher_id) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from vouchers_redeemed where voucher_id=? limit 1")) {
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
    public List<VoucherHistory> getRedeemedVouchersByPage(final UUID player_uuid, final int page, final int perPage) {
        final List<VoucherHistory> vouchers = new ArrayList<>();

        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select voucher_id, redeem_limit, timestamp_redeemed from vouchers_redeemed where player_uuid=? order by timestamp_redeemed limit ? offset ?")) {
                statement.setString(1, player_uuid.toString());
                statement.setInt(2, perPage);
                statement.setInt(3, (page - 1) * perPage);

                int position = 1;

                try (final ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        vouchers.add(new VoucherHistory(
                                resultSet.getString("voucher_id"),
                                position,
                                player_uuid,
                                resultSet.getInt("redeem_limit"),
                                resultSet.getLong("timestamp_redeemed")));

                        position++;
                    }
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while fetching all redeemed vouchers by page for <gold>%s's</gold>!",
                        this.impl, exception.getMessage(), player_uuid);
            }
        }).join();

        return vouchers;
    }

    @Override
    public List<VoucherHistory> getRedeemedVouchers(final UUID player_uuid) {
        final List<VoucherHistory> vouchers = new ArrayList<>();

        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select voucher_id, redeem_limit, timestamp_redeemed from vouchers_redeemed where player_uuid=?")) {
                statement.setString(1, player_uuid.toString());

                try (final ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        vouchers.add(new VoucherHistory(
                                resultSet.getString("voucher_id"),
                                resultSet.getRow(),
                                player_uuid,
                                resultSet.getInt("redeem_limit"),
                                resultSet.getLong("timestamp_redeemed")
                        ));
                    }
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while fetching all redeemed vouchers for <gold>%s's</gold>!",
                        this.impl, exception.getMessage(), player_uuid);
            }
        }).join();

        return vouchers;
    }

    @Override
    public int getVoucherLimit(final String voucher_id, final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select redeem_limit from vouchers_redeemed where voucher_id=? and player_uuid=? limit 1")) {
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
    public void removeRedeemedVoucher(final String voucher_id) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("delete from vouchers_redeemed where voucher_id=?")) {
                statement.setString(1, voucher_id);

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while removing the redeemed voucher id <gold>%s</gold>!",
                        this.impl, exception.getMessage(), voucher_id);
            }
        });
    }

    @Override
    public void addRedeemedVoucher(final String voucher_id, final UUID player_uuid, final int amount) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("insert into vouchers_redeemed(voucher_id, player_uuid, redeem_limit, timestamp_redeemed) values (?, ?, ?, ?)")) {
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
    public void addRedeemedCode(final String code, final UUID player_uuid) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("insert into codes_redeemed(code, player_uuid, redeem_limit, timestamp_redeemed) values (?, ?, ?, ?)")) {
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
    public int countRedeemedVouchers(final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select count(*) from vouchers_redeemed where player_uuid=?")) {
                statement.setString(1, player_uuid.toString());

                try (final ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while counting how many times player <gold>%s</gold> appears!",
                        this.impl, exception.getMessage(), player_uuid);
            }

            return -1;
        }).join();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (this.source == null) {
            throw new FusionException("Failed to get connection from pool. (Source returned null)");
        }

        final Connection connection = this.source.getConnection();

        if (connection == null) {
            throw new FusionException("Failed to get connection from pool. (getConnection returned null)");
        }

        return connection;
    }
}