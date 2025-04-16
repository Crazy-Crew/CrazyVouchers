package com.badbones69.crazyvouchers.commands.features.admin.migrate;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.config.Messages;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.paper.files.LegacyFileManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public abstract class IVoucherMigrator {

    protected final CrazyVouchers plugin = CrazyVouchers.get();

    protected final CrazyManager crazyManager = this.plugin.getCrazyManager();

    protected final SettingsManager config = ConfigManager.getConfig();

    protected final SettingsManager messages = ConfigManager.getMessages();

    protected final LegacyFileManager fileManager = this.plugin.getFileManager();

    protected final File dataFolder = this.plugin.getDataFolder();

    protected final CommandSender sender;

    protected final MigrationType type;

    protected final long startTime;

    public IVoucherMigrator(@NotNull final CommandSender sender, @NotNull final MigrationType type) {
        this.startTime = System.nanoTime();

        this.sender = sender;
        this.type = type;
    }

    public <T> void set(@NotNull final ConfigurationSection section, @NotNull final String path, @NotNull final T value) {}

    public abstract void run();

    public File getVouchersDirectory() {
        return null;
    }

    public File getCodesDirectory() {
        return null;
    }

    public void sendMessage(List<String> files, final int success, final int failed) {
        Messages.successfully_migrated.sendMessage(this.sender, new HashMap<>() {{
            if (files.size() > 1) {
                put("{files}", StringUtils.toString(files));
            } else {
                put("{files}", files.getFirst());
            }

            put("{succeeded_amount}", String.valueOf(success));
            put("{failed_amount}", String.valueOf(failed));
            put("{type}", type.getName());
            put("{time}", time());
        }});
    }

    public final String time() {
        final double time = (double) (System.nanoTime() - this.startTime) / 1.0E9D;

        return String.format(Locale.ROOT, "%.3fs", time);
    }
}