package com.ryderbelserion.crazyvouchers.storage.impl.file;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.StorageCredentials;
import us.crazycrew.crazyvouchers.api.objects.VoucherHistory;
import com.ryderbelserion.crazyvouchers.storage.impl.sql.SqlFactory;
import com.ryderbelserion.fusion.api.enums.Level;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jspecify.annotations.NullMarked;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@NullMarked
public final class SqliteFactory extends SqlFactory {

    private final String drop_index_vouchers_uuid = "drop index if exists idx_vouchers_redeemed_player_uuid";
    private final String drop_index_vouchers_id = "drop index if exists idx_vouchers_redeemed_id";

    private final String drop_index_tracker_uuid = "drop index if exists idx_vouchers_tracker_player_uuid";
    private final String drop_index_tracker_id = "drop index if exists idx_vouchers_tracker_voucher_id";

    private final String drop_index_codes_uuid = "drop index if exists idx_code_player_uuid";
    private final String drop_index_codes_id = "drop index if exists idx_code_id";

    private final Path path;

    public SqliteFactory(final StorageCredentials credentials, final CrazyVouchersPlugin plugin) {
        super(credentials.queryTimeout(), plugin, "SQLite");

        this.path = plugin.getDataPath().resolve("crazyvouchers.db");
    }

    @Override
    public void migrate() {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = getConnection()) {
                if (connection == null) {
                    return;
                }

                connection.setAutoCommit(false);

                try (final Statement statement = connection.createStatement()) {
                    statement.setQueryTimeout(this.queryTimeout);

                    statement.addBatch(this.drop_index_vouchers_uuid);
                    statement.addBatch(this.drop_index_vouchers_id);

                    statement.addBatch(this.drop_index_tracker_uuid);
                    statement.addBatch(this.drop_index_tracker_id);

                    statement.addBatch(this.drop_index_codes_uuid);
                    statement.addBatch(this.drop_index_codes_id);

                    statement.executeBatch();
                } catch (final SQLException exception) {
                    connection.rollback();
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while removing indexes for migration!",
                        this.impl, exception.getMessage());
            }
        });
    }

    @Override
    public void init() {
        if (!Files.exists(this.path)) {
            try {
                final Path parent = this.path.getParent();

                if (!Files.exists(parent)) {
                    Files.createDirectory(parent);
                }

                Files.createFile(this.path);
            } catch (final IOException exception) {
                this.fusion.log(true, Level.error, "<red>Failed to create the file %s. (<gold>%s</gold>)", this.path, exception.getMessage() != null ? exception.getMessage() : "N/A");
            }
        }

        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl(url());
        config.setMaximumPoolSize(5); // 5 is enough for flat file.
        config.setConnectionInitSql("PRAGMA foreign_keys = ON;");

        this.source = new HikariDataSource(config);

        CompletableFuture.runAsync(() -> {
            try (final Connection connection = getConnection()) {
                if (connection == null) {
                    return;
                }

                connection.setAutoCommit(false);

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
                } catch (final SQLException exception) {
                    connection.rollback();
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while initializing the tables!",
                        this.impl, exception.getMessage());
            }
        });
    }

    @Override
    public void post() {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = getConnection()) {
                if (connection == null) {
                    return;
                }

                connection.setAutoCommit(false);

                try (final Statement statement = connection.createStatement()) {
                    statement.setQueryTimeout(this.queryTimeout);

                    statement.addBatch(this.index_tracker_uuid);
                    statement.addBatch(this.index_tracker_id);

                    statement.addBatch(this.index_vouchers_uuid);
                    statement.addBatch(this.index_vouchers_id);

                    statement.addBatch(this.index_codes_uuid);
                    statement.addBatch(this.index_codes_id);

                    statement.executeBatch();

                    connection.commit();
                } catch (final SQLException exception) {
                    connection.rollback();
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason for data type <green>%s</green>, <gold>%s</gold> while adding indexing to the columns!",
                        this.impl, exception.getMessage());
            }
        });
    }

    @Override
    public List<VoucherHistory> getRedeemedVouchersByPage(final UUID player_uuid, final int page, final int perPage) {
        return CompletableFuture.supplyAsync(() -> {
            final List<VoucherHistory> vouchers = new ArrayList<>();

            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select voucher_id, redeem_limit, timestamp_redeemed from vouchers_redeemed where player_uuid=? order by rowid limit ? offset ?")) {
                statement.setQueryTimeout(this.queryTimeout);

                statement.setString(1, player_uuid.toString());
                statement.setInt(2, perPage);
                statement.setInt(3, (page - 1) * perPage);

                try (final ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        vouchers.add(new VoucherHistory(
                                resultSet.getString("voucher_id"),
                                resultSet.getRow(),
                                player_uuid,
                                resultSet.getInt("redeem_limit"),
                                resultSet.getLong("timestamp_redeemed")));
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
                    while (resultSet.next()) {
                        vouchers.add(new VoucherHistory(
                                resultSet.getString("voucher_id"),
                                resultSet.getRow(),
                                player_uuid,
                                resultSet.getInt("redeem_limit"),
                                resultSet.getLong("timestamp_redeemed")));
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

                try (final ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        vouchers.add(new VoucherHistory(
                                resultSet.getString("code"),
                                resultSet.getRow(),
                                player_uuid,
                                resultSet.getInt("redeem_limit"),
                                resultSet.getLong("timestamp_redeemed")));
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
                    while (resultSet.next()) {
                        vouchers.add(new VoucherHistory(
                                resultSet.getString("code"),
                                resultSet.getRow(),
                                player_uuid,
                                resultSet.getInt("redeem_limit"),
                                resultSet.getLong("timestamp_redeemed")
                        ));
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
    public String url() {
        return "jdbc:sqlite:" + this.path.toFile().getAbsolutePath();
    }
}