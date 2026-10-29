package com.badbones69.crazyvouchers.commands.features.admin.migrate.types.database;

import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.ryderbelserion.fusion.files.enums.FileType;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.api.config.types.DatabaseKeys;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseMigrator extends IVoucherMigrator {

    public DatabaseMigrator(@NonNull final CommandSender sender) {
        super(sender, MigrationType.DATABASE_MIGRATION);
    }

    @Override
    public void run() {
        if (!this.config.getProperty(DatabaseKeys.storage_type).equalsIgnoreCase("yaml")) {
            Message.migration_not_available.sendMessage(this.sender, Map.of(
                    "{file}", "data.yml",
                    "{type}", this.type.getName(),
                    "{reason}", "root.storage.type is not set to yaml!"
            ));

            return;
        }

        final List<String> failed = new ArrayList<>();
        final List<String> success = new ArrayList<>();

        if (Files.exists(this.dataPath.resolve("data.yml"))) {
            this.fileManager.addFile(this.dataPath.resolve("data.yml"), FileType.YAML);

            this.fileManager.getYamlFile(this.dataPath.resolve("data.yml")).ifPresent(customFile -> {
                final List<String> values = customFile.getStringList("Used-Vouchers");

                for (final String value : values) {
                    if (this.holder.isVoucherProtected(value)) {
                        failed.add(value);

                        continue; // already added
                    }

                    this.holder.addProtectedVoucher(value, "N/A"); // add it

                    success.add(value);
                }
            });
        }

        if (Files.exists(this.dataPath.resolve("users.yml"))) {
            this.fileManager.addFile(this.dataPath.resolve("users.yml"), FileType.YAML);

            this.fileManager.getYamlFile(this.dataPath.resolve("users.yml")).ifPresent(customFile -> {
                final CommentedConfigurationNode configuration = customFile.getConfiguration();

                for (final Map.Entry<Object, CommentedConfigurationNode> entries : configuration.node("Players").childrenMap().entrySet()) {
                    final CommentedConfigurationNode entry = entries.getValue();
                    final String id = entries.getKey().toString();
                    final UUID uuid = UUID.fromString(id);

                    for (final Object index : entry.node("Codes").childrenMap().keySet()) { // if the id is in here, it's used lol
                        this.holder.addRedeemedCode(index.toString(), uuid);
                    }

                    for (final Map.Entry<Object, CommentedConfigurationNode> index : configuration.node("Vouchers").childrenMap().entrySet()) {
                        final CommentedConfigurationNode config = index.getValue();
                        final String voucher_id = index.getKey().toString();

                        this.holder.addRedeemedVoucher(voucher_id, uuid, config.getInt(0));
                    }
                }
            });
        }

        final int convertedCrates = success.size();
        final int failedCrates = failed.size();

        final List<String> files = new ArrayList<>(failedCrates + convertedCrates);

        files.addAll(failed);
        files.addAll(success);

        sendMessage(files, convertedCrates, failedCrates);
    }
}