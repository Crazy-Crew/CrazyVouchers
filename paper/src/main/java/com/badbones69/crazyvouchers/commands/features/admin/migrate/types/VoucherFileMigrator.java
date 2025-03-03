package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.core.api.enums.FileType;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.io.File;

public class VoucherFileMigrator extends IVoucherMigrator {

    public VoucherFileMigrator(@NotNull final CommandSender sender, @NotNull final MigrationType type) {
        super(sender, type);
    }

    @Override
    public void run() {
        switch (this.type) {
            case VOUCHERS_RENAME -> { // rename voucher-codes.yml to codes.yml
                final File file = new File(this.plugin.getDataFolder(), "codes.yml");

                if (file.exists()) {
                    final File backup = new File(this.plugin.getDataFolder(), "backups");

                    if (!backup.exists()) {
                        backup.mkdirs();
                    }

                    file.renameTo(new File(backup, "codes-backup.yml"));
                }

                final File oldFile = new File(this.plugin.getDataFolder(), "voucher-codes.yml");

                if (!oldFile.exists()) return;

                oldFile.renameTo(file);

                if (this.config.getProperty(ConfigKeys.file_system) == FileSystem.SINGLE) {
                    this.fileManager.removeFile("voucher-codes.yml", FileType.YAML, false);
                    this.fileManager.addFile("codes.yml", FileType.YAML);

                    this.crazyManager.loadCodes();
                }
            }
        }
    }
}