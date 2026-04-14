package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.core.api.enums.Level;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class VoucherFileMigrator extends IVoucherMigrator {

    public VoucherFileMigrator(@NotNull final CommandSender sender, @NotNull final MigrationType type) {
        super(sender, type);
    }

    @Override
    public void run() {
        switch (this.type) {
            case VOUCHERS_RENAME -> { // rename voucher-codes.yml to codes.yml
                final Path oldPath = this.dataPath.resolve("voucher-codes.yml");

                if (!Files.exists(oldPath)) {
                    this.fusion.log(Level.WARNING, "The file %s does not exist at the path.", oldPath);

                    return;
                }

                final Path newPath = this.dataPath.resolve("codes.yml");

                if (Files.exists(newPath)) {
                    final Path backup = this.dataPath.resolve("backups");

                    if (!Files.exists(backup)) {
                        try {
                            Files.createDirectory(backup);

                            this.fusion.log(Level.WARNING, "Successfully created the backup %s", backup);
                        } catch (final IOException exception) {
                            exception.printStackTrace();
                        }
                    }

                    try {
                        Files.move(backup, newPath, StandardCopyOption.REPLACE_EXISTING);

                        this.fusion.log(Level.WARNING, "Successfully moved %s to %s.", backup, newPath);
                    } catch (final IOException exception) {
                        exception.printStackTrace();
                    }
                }

                try {
                    Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

                    this.fusion.log(Level.WARNING, "Successfully moved %s to %s.", oldPath, newPath);
                } catch (final Exception exception) {
                    exception.printStackTrace();
                }

                if (this.config.getProperty(ConfigKeys.file_system) == FileSystem.SINGLE) {
                    this.fileManager.removeFile(oldPath);

                    this.fileManager.addPaperFile(newPath);

                    this.crazyManager.loadCodes();
                }
            }
        }
    }
}