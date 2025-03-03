package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import java.io.File;

public class VoucherSwitchMigrator extends IVoucherMigrator {

    public VoucherSwitchMigrator(@NotNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_SWITCH);
    }

    @Override
    public void run() {
        switch (this.config.getProperty(ConfigKeys.file_system)) {
            case SINGLE -> {

            }

            case MULTIPLE -> {

            }
        }
    }

    @Override
    public <T> void set(@NotNull final ConfigurationSection section, @NotNull final String path, @NotNull final T value) {
        section.set(path, value);
    }

    @Override
    public final File getVouchersDirectory() {
        return new File(this.plugin.getDataFolder(), "vouchers");
    }

    @Override
    public final File getCodesDirectory() {
        return new File(this.plugin.getDataFolder(), "codes");
    }
}