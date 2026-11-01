package com.ryderbelserion.crazyvouchers.storage.impl.sql.types;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
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
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class SqliteFactory extends SqlFactory {

    private final String drop_index_vouchers_uuid = "drop index if exists idx_vouchers_redeemed_player_uuid";
    private final String drop_index_vouchers_id = "drop index if exists idx_vouchers_redeemed_id";

    private final String drop_index_tracker_uuid = "drop index if exists idx_vouchers_tracker_player_uuid";
    private final String drop_index_tracker_id = "drop index if exists idx_vouchers_tracker_voucher_id";

    private final String drop_index_codes_uuid = "drop index if exists idx_code_player_uuid";
    private final String drop_index_codes_id = "drop index if exists idx_code_id";

    private final Path path;

    public SqliteFactory(final CrazyVouchersPlugin plugin) {
        super(plugin, "SQLite");

        this.path = plugin.getDataPath().resolve("crazyvouchers.db");
    }

    @Override
    public void migrate() {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = getConnection()) {
                if (connection == null) return;

                try (final Statement statement = connection.createStatement()) {
                    statement.addBatch(this.drop_index_vouchers_uuid);
                    statement.addBatch(this.drop_index_vouchers_id);

                    statement.addBatch(this.drop_index_tracker_uuid);
                    statement.addBatch(this.drop_index_tracker_id);

                    statement.addBatch(this.drop_index_codes_uuid);
                    statement.addBatch(this.drop_index_codes_id);

                    statement.executeBatch();
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while removing indexes for migration!", exception.getMessage());
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
    public void post() {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = getConnection()) {
                if (connection == null) return;

                try (final Statement statement = connection.createStatement()) {
                    statement.addBatch(this.index_tracker_uuid);
                    statement.addBatch(this.index_tracker_id);

                    statement.addBatch(this.index_vouchers_uuid);
                    statement.addBatch(this.index_vouchers_id);

                    statement.addBatch(this.index_codes_uuid);
                    statement.addBatch(this.index_codes_id);

                    statement.executeBatch();
                }
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while adding indexing to the columns!", exception.getMessage());
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
                statement.setString(1, player_uuid.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while deleting a user using the <gold>uuid %s</gold>!", exception.getMessage(), player_uuid);
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
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while inserting a user using the <gold>uuid %s</gold>!", exception.getMessage(), player_uuid);
            }
        });
    }

    @Override
    public boolean hasUser(final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from vouchers_players where player_uuid=? limit 1")) {
                statement.setString(1, player_uuid.toString());

                return statement.executeQuery().next();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while checking if <gold>%s</gold> exists!", exception.getMessage(), player_uuid);
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

                return statement.executeQuery().next();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while checking if the voucher <gold>%s</gold> is protected!", exception.getMessage(), voucher_id);
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
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while deleting voucher_id <gold>%s</gold>!", exception.getMessage(), voucher_id);
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
                statement.setLong(3, Instant.now().toEpochMilli());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while adding a protected voucher using the <gold>uuid %s</gold> (<green>voucher_id %s</green>)!", exception.getMessage(), player_uuid,  voucher_id);
            }
        });
    }

    @Override
    public boolean isVoucherRedeemed(final String voucher_id) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from vouchers_redeemed where voucher_id=? limit 1")) {
                statement.setString(1, voucher_id);

                return statement.executeQuery().next();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while checking if the voucher <gold>%s</gold> is redeemed!", exception.getMessage(), voucher_id);
            }

            return false;
        }).join();
    }

    @Override
    public int getVoucherLimit(final String voucher_id, final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from vouchers_redeemed where voucher_id=? and player_uuid=? limit 1")) {
                statement.setString(1, voucher_id);
                statement.setString(2, player_uuid.toString());

                final ResultSet resultSet = statement.executeQuery();

                return resultSet.getInt("redeem_limit");
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while checking the voucher <gold>%s's</gold> redeem limit!", exception.getMessage(), voucher_id);
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
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while incrementing the redeem_limit for voucher <gold>%s</gold> with player id <gold>%s</gold>!", exception.getMessage(), voucher_id, player_uuid);
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
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while removing the redeemed voucher id <gold>%s</gold>!", exception.getMessage(), voucher_id);
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
                statement.setLong(4, Instant.now().toEpochMilli());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while adding a voucher using the player uuid <gold>%s</gold> (<green>voucher_id %s</green>)!", exception.getMessage(), player_uuid,  voucher_id);
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
               statement.setInt(3, 0);
               statement.setLong(4, Instant.now().toEpochMilli());

               statement.executeUpdate();
           } catch (final SQLException exception) {
               this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while adding the code <gold>%s</gold> with player id <gold>%s</gold>!", exception.getMessage(), code, player_uuid);
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
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while removing the code <gold>%s</gold>!", exception.getMessage(), code);
            }
        });
    }

    @Override
    public boolean isCodeRedeemed(final String code) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from codes_redeemed where code=? limit 1")) {
                statement.setString(1, code);

                return statement.executeQuery().next();
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while checking if the voucher code <gold>%s</gold> is redeemed!", exception.getMessage(), code);
            }

            return false;
        }).join();
    }

    @Override
    public int getCodeLimit(final String code, final UUID player_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from codes_redeemed where code=? and player_uuid=? limit 1")) {
                statement.setString(1, code);
                statement.setString(2, player_uuid.toString());

                final ResultSet resultSet = statement.executeQuery();

                return resultSet.getInt("redeem_limit");
            } catch (final SQLException exception) {
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while checking the voucher code <gold>%s's</gold> redeem limit!", exception.getMessage(), code);
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
                this.fusion.log(true, Level.error, "<red>An error has occurred with the reason <gold>%s</gold> while incrementing the redeem_limit for code <gold>%s</gold> with player id <gold>%s</gold>!", exception.getMessage(), code, player_uuid);
            }
        });
    }

    @Override
    public String url() {
        return "jdbc:sqlite:" + this.path.toFile().getAbsolutePath();
    }
}