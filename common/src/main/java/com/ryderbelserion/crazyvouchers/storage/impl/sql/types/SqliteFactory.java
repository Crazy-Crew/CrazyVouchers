package com.ryderbelserion.crazyvouchers.storage.impl.sql.types;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.impl.sql.SqlFactory;
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
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class SqliteFactory extends SqlFactory {

    private final Path path;

    public SqliteFactory(final CrazyVouchersPlugin plugin) {
        super(plugin, "SQLite");

        this.path = plugin.getDataPath().resolve("crazycrates.db");
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
                exception.printStackTrace();
            }
        }

        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl(url());
        config.setMaximumPoolSize(5); // 5 is enough for flat file.
        config.setConnectionInitSql("PRAGMA foreign_keys = ON;");

        this.source = new HikariDataSource(config);

        super.init();
    }

    @Override
    public boolean isVoucherProtected(final UUID voucher_id) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("select 1 from duplicate_voucher_tracker where voucher_id=? limit 1")) {
                statement.setString(1, voucher_id.toString());

                return statement.executeQuery().next();
            } catch (final SQLException exception) {
                exception.printStackTrace();
            }

            return false;
        }).join();
    }

    @Override
    public void removeProtectedVoucher(UUID voucher_id) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("delete from duplicate_voucher_tracker where voucher_id=?")) {
                statement.setString(1, voucher_id.toString());
            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void addProtectedVoucher(UUID voucher_id, UUID player_uuid) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("insert into duplicate_voucher_tracker(voucher_id, player_uuid, timestamp_redeemed) values (?, ?, ?)")) {
                statement.setString(1, voucher_id.toString());
                statement.setString(2, player_uuid.toString());
                statement.setInt(3, 0);
                statement.setLong(4, Instant.now().toEpochMilli());
            } catch (final SQLException exception) {
                exception.printStackTrace();
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
                exception.printStackTrace();
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
                exception.printStackTrace();
            }

            return -1;
        }).join();
    }

    @Override
    public void incrementVoucherLimit(final String voucher_id, final UUID player_uuid) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("update vouchers_redeemed set redeem_limit + 1 where voucher_id=? and player_uuid=? limit 1")) {
                statement.setString(1, voucher_id);
                statement.setString(2, player_uuid.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void removeRedeemedVoucher(final String voucher_id) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("delete from vouchers_redeemed where voucher_id=?")) {
                statement.setString(1, voucher_id);
            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void addRedeemedVoucher(final String voucher_id, final UUID player_uuid, final int amount) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("insert into vouchers_redeemed(voucher_id, player_uuid, redeem_limit, timestamp_redeemed) values (?, ?, ?)")) {
                statement.setString(1, voucher_id);
                statement.setString(2, player_uuid.toString());
                statement.setInt(3, amount);
                statement.setLong(4, Instant.now().toEpochMilli());
            } catch (final SQLException exception) {
                exception.printStackTrace();
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
           } catch (final SQLException exception) {
               exception.printStackTrace();
           }
        });
    }

    @Override
    public void removeRedeemedCode(final String code) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("delete from codes_redeemed where code=?")) {
                statement.setString(1, code);
            } catch (final SQLException exception) {
                exception.printStackTrace();
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
                exception.printStackTrace();
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
                exception.printStackTrace();
            }

            return -1;
        }).join();
    }

    @Override
    public void incrementCodeLimit(final String code, final UUID player_uuid) {
        CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.source.getConnection(); final PreparedStatement statement =
                    connection.prepareStatement("update codes_redeemed set redeem_limit + 1 where code=? and player_uuid=?")) {
                statement.setString(1, code);
                statement.setString(2, player_uuid.toString());

                statement.executeUpdate();
            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public String url() {
        return "jdbc:sqlite:" + this.path.toFile().getAbsolutePath();
    }
}