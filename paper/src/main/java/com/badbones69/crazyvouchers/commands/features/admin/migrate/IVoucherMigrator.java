package com.badbones69.crazyvouchers.commands.features.admin.migrate;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.CrazyPlatform;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.paper.FusionPaper;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class IVoucherMigrator {

    protected @NonNull final CrazyVouchers plugin = CrazyVouchers.get();

    protected @NonNull final CrazyPlatform platform = this.plugin.getPlatform();

    protected @NonNull final ConfigManager configManager = this.platform.getConfigManager();

    protected @NonNull final FusionPaper fusion = this.plugin.getFusion();

    protected @NonNull final CrazyManager crazyManager = this.plugin.getCrazyManager();

    protected @NonNull final PropertyManager config = this.configManager.getConfig();

    protected @NonNull final FileManager fileManager = this.fusion.getFileManager();

    protected @NonNull final Path dataPath = this.plugin.getDataPath();

    protected final CommandSender sender;

    protected final MigrationType type;

    protected final long startTime;

    public IVoucherMigrator(@NonNull final CommandSender sender, @NonNull final MigrationType type) {
        this.startTime = System.nanoTime();

        this.sender = sender;
        this.type = type;
    }

    public <T> void set(@NonNull final CommentedConfigurationNode section, @Nullable final T value, @NonNull final Type type, @NonNull final Object... path) {
        try {
            section.node(path).set(type, value);
        } catch (final SerializationException exception) {
            exception.printStackTrace();
        }
    }

    public <T> void set(@NonNull final CommentedConfigurationNode section, @Nullable final T value, @NonNull final Type type) {
        try {
            section.set(type, value);
        } catch (final SerializationException exception) {
            exception.printStackTrace();
        }
    }

    public void comment(@NonNull final CommentedConfigurationNode section, @NonNull final List<String> comment) {
        section.commentIfAbsent(StringUtils.toString(comment));
    }

    public void comment(@NonNull final CommentedConfigurationNode section, @NonNull final String comment) {
        comment(section, List.of(comment));
    }

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

        Message.successfully_migrated_users.sendMessage(this.sender, placeholders);
    }

    public @NonNull final String time() {
        final double time = (double) (System.nanoTime() - this.startTime) / 1.0E9D;

        return String.format(Locale.ROOT, "%.3fs", time);
    }
}