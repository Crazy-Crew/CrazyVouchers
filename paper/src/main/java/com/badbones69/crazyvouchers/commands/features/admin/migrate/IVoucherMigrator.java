package com.badbones69.crazyvouchers.commands.features.admin.migrate;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.paper.FusionPaper;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class IVoucherMigrator {

    protected @NonNull final CrazyVouchers plugin = CrazyVouchers.get();

    protected @NonNull final FusionPaper fusion = this.plugin.getFusion();

    protected @NonNull final CrazyManager crazyManager = this.plugin.getCrazyManager();

    protected @NonNull final SettingsManager config = ConfigManager.getConfig();

    protected @NonNull final SettingsManager messages = ConfigManager.getMessages();

    protected @NonNull final FileManager fileManager = this.plugin.getFileManager();

    protected @NonNull final Path dataPath = this.plugin.getDataPath();

    protected final CommandSender sender;

    protected final MigrationType type;

    protected final long startTime;

    public IVoucherMigrator(@NonNull final CommandSender sender, @NonNull final MigrationType type) {
        this.startTime = System.nanoTime();

        this.sender = sender;
        this.type = type;
    }

    public <T> void set(@NonNull final ConfigurationSection section, @NonNull final String path, @NonNull final T value) {}

    public abstract void run();

    public @NonNull Path getVouchersDirectory() {
        return this.dataPath;
    }

    public @NonNull Path getCodesDirectory() {
        return this.dataPath;
    }

    public void sendMessage(@NonNull final List<String> files, final int success, final int failed) {
        final Map<String, String> placeholders = new HashMap<>();

        if (files.size() > 1) {
            placeholders.put("{files}", StringUtils.toString(files));
        } else {
            placeholders.put("{files}", files.getFirst());
        }

        placeholders.put("{succeeded_amount}", String.valueOf(success));
        placeholders.put("{failed_amount}", String.valueOf(failed));
        placeholders.put("{type}", type.getName());
        placeholders.put("{time}", time());

        Messages.successfully_migrated.sendMessage(this.sender, placeholders);
    }

    public @NonNull final String time() {
        final double time = (double) (System.nanoTime() - this.startTime) / 1.0E9D;

        return String.format(Locale.ROOT, "%.3fs", time);
    }
}