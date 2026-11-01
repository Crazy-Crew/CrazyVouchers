package com.badbones69.crazyvouchers.commands.features.admin.migrate.types.database;

import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.files.enums.FileType;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseMigrator extends IVoucherMigrator {

    public DatabaseMigrator(@NonNull final CommandSender sender) {
        super(sender, MigrationType.DATABASE_MIGRATION);
    }

    @Override
    public void run() {
        final List<String> failed = new ArrayList<>();
        final List<String> success = new ArrayList<>();

        this.holder.migrate(); // disable indexes

        if (Files.exists(this.dataPath.resolve("data.yml"))) {
            this.fileManager.addFile(this.dataPath.resolve("data.yml"), FileType.YAML);

            this.fileManager.getYamlFile(this.dataPath.resolve("data.yml")).ifPresent(customFile -> {
                final List<String> values = customFile.getStringList("Used-Vouchers");

                CompletableFuture.runAsync(() -> {
                    try (final Connection connection = this.holder.getConnection()) {
                        connection.setAutoCommit(false);

                        try (final PreparedStatement statement = connection.prepareStatement("insert into vouchers_tracker(voucher_id, player_uuid, timestamp_redeemed) values (?, ?, ?)")) {
                            final UUID uuid = CrazyVouchersPlugin.CONSOLE_UUID;

                            for (final String value : values) {
                                statement.setString(1, value);
                                statement.setString(2, uuid.toString());
                                statement.setLong(3, Instant.now().toEpochMilli());

                                statement.addBatch();

                                success.add(value);
                            }

                            statement.executeBatch();
                            connection.commit();
                        } catch (final SQLException exception) {
                            connection.rollback();

                            success.clear(); // all success failed because roll back!
                            failed.addAll(values);

                            this.fusion.log(true, Level.error, "<red>Failed to execute batch for data.yml, Rolling back! (<gold>%s</gold>)", exception.getMessage());
                        } finally {
                            connection.setAutoCommit(true);
                        }
                    } catch (final SQLException exception) {
                        this.fusion.log(true, Level.error, "<red>Failed to fetch connection! Cannot migrate data.yml. (<gold>%s</gold>)", exception.getMessage());
                    }
                });
            });
        }

        if (Files.exists(this.dataPath.resolve("users.yml"))) {
            this.fileManager.addFile(this.dataPath.resolve("users.yml"), FileType.YAML);

            this.fileManager.getYamlFile(this.dataPath.resolve("users.yml")).ifPresent(customFile -> {
                final CommentedConfigurationNode configuration = customFile.getConfiguration();
                final Set<Map.Entry<Object, CommentedConfigurationNode>> values = configuration.node("Players").childrenMap().entrySet();

                CompletableFuture.runAsync(() -> {
                    try (final Connection connection = this.holder.getConnection()) {
                        connection.setAutoCommit(false);

                        try (final PreparedStatement statement = connection.prepareStatement("insert into vouchers_players(player_uuid) values (?)")) {
                            for (final Map.Entry<Object, CommentedConfigurationNode> entries : values) {
                                final String id = entries.getKey().toString();
                                final UUID uuid = UUID.fromString(id);

                                statement.setString(1, uuid.toString());
                                statement.addBatch();

                                success.add(id);
                            }

                            statement.executeBatch();
                            connection.commit();
                        } catch (final SQLException exception) {
                            connection.rollback();

                            this.fusion.log(true, Level.error, "<red>Failed to execute batch for users.yml, Rolling back! (<gold>%s</gold>)", exception.getMessage());
                        }

                        try (final PreparedStatement statement = connection.prepareStatement("insert into codes_redeemed(code, player_uuid, redeem_limit, timestamp_redeemed) values (?, ?, ?, ?)")) {
                            for (final Map.Entry<Object, CommentedConfigurationNode> entries : values) {
                                final CommentedConfigurationNode entry = entries.getValue();
                                final String id = entries.getKey().toString();
                                final UUID uuid = UUID.fromString(id);

                                for (final Object index : entry.node("Codes").childrenMap().keySet()) { // if the id is in here, it's used lol
                                    statement.setString(1, index.toString());
                                    statement.setString(2, uuid.toString());
                                    statement.setLong(3, Instant.now().toEpochMilli());

                                    statement.addBatch();

                                    success.add(id);
                                }
                            }

                            statement.executeBatch();
                            connection.commit();
                        } catch (final SQLException exception) {
                            connection.rollback();

                            this.fusion.log(true, Level.error, "<red>Failed to execute batch for users.yml, Rolling back! (<gold>%s</gold>)", exception.getMessage());
                        }

                        try (final PreparedStatement statement = connection.prepareStatement("insert into vouchers_redeemed(voucher_id, player_uuid, redeem_limit, timestamp_redeemed) values (?, ?, ?, ?)")) {
                            for (final Map.Entry<Object, CommentedConfigurationNode> entries : values) {
                                final CommentedConfigurationNode entry = entries.getValue();
                                final String id = entries.getKey().toString();
                                final UUID uuid = UUID.fromString(id);

                                for (final Map.Entry<Object, CommentedConfigurationNode> index : entry.node("Vouchers").childrenMap().entrySet()) {
                                    final CommentedConfigurationNode config = index.getValue();
                                    final String voucher_id = index.getKey().toString();

                                    statement.setString(1, voucher_id);
                                    statement.setString(2, uuid.toString());
                                    statement.setInt(3, config.getInt(0));
                                    statement.setLong(4, Instant.now().toEpochMilli());

                                    statement.addBatch();

                                    success.add(id);
                                }
                            }

                            statement.executeBatch();
                            connection.commit();
                        } catch (final SQLException exception) {
                            connection.rollback();

                            this.fusion.log(true, Level.error, "<red>Failed to execute batch for users.yml, Rolling back! (<gold>%s</gold>)", exception.getMessage());
                        }

                        connection.setAutoCommit(true);
                    } catch (final SQLException exception) {
                        this.fusion.log(true, Level.error, "<red>Failed to fetch connection! Cannot migrate users.yml, Rolling back! (<gold>%s</gold>)", exception.getMessage());
                    }
                });
            });
        }

        this.holder.post(); // enable indexes

        final int convertedCrates = success.size();
        final int failedCrates = failed.size();

        final List<String> files = new ArrayList<>(failedCrates + convertedCrates);

        files.addAll(failed);
        files.addAll(success);

        sendMessage(files, convertedCrates, failedCrates);
    }
}