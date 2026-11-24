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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class HikariFactory extends SqlFactory {

    private final StorageCredentials credentials;

    private HikariDataSource source;

    public HikariFactory(
            @NonNull final StorageCredentials credentials,
            @NonNull final CrazyVouchersPlugin plugin,
            @NonNull final String impl
    ) {
        super(credentials.queryTimeout(), plugin, impl);

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

    protected void configure(
            @NonNull final HikariConfig config,
            @NonNull final String database,
            @NonNull final String username,
            @NonNull final String password,
            @NonNull final String address,
            final int port
    ) {
        config.setDriverClassName(getDriverIdentifier());

        config.setJdbcUrl(url().formatted(getJDBCIdentifier(), address, port == -1 ? getDefaultPort() : port, database));

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

        for (final Map.Entry<String, Object> property : this.credentials.values().entrySet()) {
            config.addDataSourceProperty(property.getKey(), property.getValue());
        }

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
    public List<VoucherHistory> getRedeemedVouchersByPage(final UUID player_uuid, final int page, final int perPage) {
        return CompletableFuture.supplyAsync(() -> {
            final List<VoucherHistory> vouchers = new ArrayList<>();

            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select voucher_id, redeem_limit, timestamp_redeemed from vouchers_redeemed where player_uuid=? order by timestamp_redeemed limit ? offset ?")) {
                statement.setQueryTimeout(this.queryTimeout);

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

            return vouchers;
        }).completeOnTimeout(List.of(), 10, TimeUnit.SECONDS).join();
    }

    @Override
    public List<VoucherHistory> getRedeemedVouchers(final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final List<VoucherHistory> vouchers = new ArrayList<>();

            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select voucher_id, redeem_limit, timestamp_redeemed from vouchers_redeemed where player_uuid=?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, player_uuid.toString());

                try (final ResultSet resultSet = statement.executeQuery()) {
                    int position = 1;

                    while (resultSet.next()) {
                        vouchers.add(new VoucherHistory(
                                resultSet.getString("voucher_id"),
                                position,
                                player_uuid,
                                resultSet.getInt("redeem_limit"),
                                resultSet.getLong("timestamp_redeemed")
                        ));

                        position++;
                    }
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while fetching all redeemed vouchers for <gold>%s's</gold>!",
                        this.impl, exception.getMessage(), player_uuid);
            }

            return vouchers;
        }).completeOnTimeout(List.of(), 10, TimeUnit.SECONDS).join();
    }

    @Override
    public List<VoucherHistory> getRedeemedCodesByPage(final UUID player_uuid, final int page, final int perPage) {
        return CompletableFuture.supplyAsync(() -> {
            final List<VoucherHistory> vouchers = new ArrayList<>();

            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select code, redeem_limit, timestamp_redeemed from codes_redeemed where player_uuid=? order by timestamp_redeemed limit ? offset ?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, player_uuid.toString());
                statement.setInt(2, perPage);
                statement.setInt(3, (page - 1) * perPage);

                int position = 1;

                try (final ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        vouchers.add(new VoucherHistory(
                                resultSet.getString("code"),
                                position,
                                player_uuid,
                                resultSet.getInt("redeem_limit"),
                                resultSet.getLong("timestamp_redeemed")));

                        position++;
                    }
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while fetching all redeemed codes by page for <gold>%s's</gold>!",
                        this.impl, exception.getMessage(), player_uuid);
            }

            return vouchers;
        }).completeOnTimeout(List.of(), 10, TimeUnit.SECONDS).join();
    }

    @Override
    public List<VoucherHistory> getRedeemedCodes(final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final List<VoucherHistory> vouchers = new ArrayList<>();

            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select code, redeem_limit, timestamp_redeemed from codes_redeemed where player_uuid=?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, player_uuid.toString());

                try (final ResultSet resultSet = statement.executeQuery()) {
                    int position = 1;

                    while (resultSet.next()) {
                        vouchers.add(new VoucherHistory(
                                resultSet.getString("code"),
                                position,
                                player_uuid,
                                resultSet.getInt("redeem_limit"),
                                resultSet.getLong("timestamp_redeemed")
                        ));

                        position++;
                    }
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while fetching all redeemed codes for <gold>%s's</gold>!",
                        this.impl, exception.getMessage(), player_uuid);
            }

            return vouchers;
        }).completeOnTimeout(List.of(), 10, TimeUnit.SECONDS).join();
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