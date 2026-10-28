package com.badbones69.crazyvouchers.commands.features.admin.migrate.types;

import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.files.types.configurate.YamlCustomFile;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.NodePath;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VoucherSwitchMigrator extends IVoucherMigrator {

    public VoucherSwitchMigrator(@NonNull final CommandSender sender) {
        super(sender, MigrationType.VOUCHERS_SWITCH);
    }

    @Override
    public void run() {
        switch (this.config.getProperty(RootKeys.file_system)) {
            case SINGLE -> {
                final Path vouchers = getVouchersDirectory();
                final Path codes = getCodesDirectory();

                final Optional<YamlCustomFile> optional = Files.exists(this.dataPath.resolve("vouchers.yml")) ? Optional.empty() :
                        Optional.of(this.fileManager.buildYamlFile(this.dataPath.resolve("vouchers.yml"), "", _ -> {}));

                optional.ifPresentOrElse(customFile -> {
                    final String fileName = customFile.getFileName();

                    if (!customFile.isLoaded()) {
                        this.fusion.log(Level.warn, "Failed to switch the voucher %s because it failed to load!", fileName);

                        return;
                    }

                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    for (final Map.Entry<Object, CommentedConfigurationNode> entries : configuration.childrenMap().entrySet()) {
                        final String id = entries.getKey().toString();
                        final CommentedConfigurationNode entry = entries.getValue();

                        final Path path = vouchers.resolve(id);

                        if (Files.exists(path)) {
                            this.fusion.log(Level.warn, "Cannot migrate %s voucher because the file already exists!", id);

                            continue;
                        }

                        try {
                            Files.createFile(path);
                        } catch (final IOException exception) {
                            exception.printStackTrace();
                        }

                        Optional.of(this.fileManager.buildYamlFile(path, "", _ -> {})).ifPresentOrElse(
                                index -> process(entry, index.getConfiguration().node("voucher")),
                                () -> this.fusion.log(Level.warn, "Failed to build %s voucher!", id));
                    }
                }, () -> this.fusion.log(Level.warn, "Failed to build %s voucher in order to start migration!", "vouchers.yml"));

                final Optional<YamlCustomFile> second = Files.exists(this.dataPath.resolve("codes.yml")) ? Optional.empty() :
                        Optional.of(this.fileManager.buildYamlFile(this.dataPath.resolve("codes.yml"), "", _ -> {}));

                second.ifPresentOrElse(customFile -> {
                    final String fileName = customFile.getFileName();

                    if (!customFile.isLoaded()) {
                        this.fusion.log(Level.warn, "Failed to switch the voucher code %s because it failed to load!", fileName);

                        return;
                    }

                    final CommentedConfigurationNode configuration = customFile.getConfiguration();

                    for (final Map.Entry<Object, CommentedConfigurationNode> entries : configuration.childrenMap().entrySet()) {
                        final String id = entries.getKey().toString();
                        final CommentedConfigurationNode entry = entries.getValue();

                        final Path path = codes.resolve(id);

                        if (Files.exists(path)) {
                            this.fusion.log(Level.warn, "Cannot migrate %s voucher code because the file already exists!", id);

                            continue;
                        }

                        try {
                            Files.createFile(path);
                        } catch (final IOException exception) {
                            exception.printStackTrace();
                        }

                        Optional.of(this.fileManager.buildYamlFile(path, "", _ -> {})).ifPresentOrElse(
                                index -> process(entry, index.getConfiguration().node("voucher-code")),
                                () -> this.fusion.log(Level.warn, "Failed to build %s voucher code!", id));
                    }
                }, () -> this.fusion.log(Level.warn, "Failed to build %s voucher code in order to start migration!", "codes.yml"));
            }

            case MULTIPLE -> {
                final Path vouchers = this.dataPath.resolve("vouchers.yml");

                if (Files.exists(vouchers)) {
                    try {
                        Files.createFile(vouchers);
                    } catch (final IOException exception) {
                        exception.printStackTrace();
                    }
                }

                final YamlCustomFile voucher_custom_file = new YamlCustomFile(this.fileManager, vouchers, _ -> {}).load();

                if (voucher_custom_file.isLoaded()) {
                    final CommentedConfigurationNode configuration = voucher_custom_file.getConfiguration();

                    final CommentedConfigurationNode root_section = configuration.node("vouchers");

                    for (final Path voucher : this.fusion.getFilesByPath(this.dataPath.resolve("vouchers"), ".yml")) {
                        final YamlCustomFile customFile = new YamlCustomFile(this.fileManager, voucher, _ -> {}).load();

                        final String fileName = customFile.getFileName();

                        if (!customFile.isLoaded()) {
                            this.fusion.log(Level.warn, "Failed to switch voucher %s, because configuration is null.", fileName);

                            continue;
                        }

                        final CommentedConfigurationNode section = customFile.getConfiguration();

                        if (!section.hasChild("voucher")) {
                            this.fusion.log(Level.warn, "Failed to switch voucher %s, because configuration section is null.", fileName);

                            continue;
                        }

                        final CommentedConfigurationNode index = section.node("voucher");

                        processItems(root_section, index);

                        voucher_custom_file.save();

                        final NodePath path = index.path();

                        if (path != null) {
                            this.fusion.log(Level.warn, "Successfully moved voucher %s to %s in vouchers.yml.", voucher, path);
                        }
                    }

                    this.fileManager.removeFile(vouchers);
                    this.fileManager.addFile(vouchers, voucher_custom_file);

                    this.platform.updateVouchers();

                    this.fusion.log(Level.warn, "Added voucher %s to the cache, and removed the old voucher file named %s", voucher_custom_file.getPrettyName(), vouchers.getFileName().toString());
                } else {
                    this.fusion.log(Level.warn, "Failed to move vouchers into vouchers.yml, because configuration is null.");
                }

                final Path code_file = this.dataPath.resolve("codes.yml");

                if (Files.exists(code_file)) {
                    try {
                        Files.createFile(code_file);
                    } catch (final IOException exception) {
                        exception.printStackTrace();
                    }
                }

                final YamlCustomFile code_custom_file = new YamlCustomFile(this.fileManager, code_file, _ -> {}).load();

                if (code_custom_file.isLoaded()) {
                    final CommentedConfigurationNode configuration = voucher_custom_file.getConfiguration();

                    final CommentedConfigurationNode root_section = configuration.node("voucher-code");

                    for (final Path voucher : this.fusion.getFilesByPath(this.dataPath.resolve("codes"), ".yml")) {
                        final YamlCustomFile customFile = new YamlCustomFile(this.fileManager, voucher, _ -> {}).load();

                        final String fileName = customFile.getFileName();

                        if (!customFile.isLoaded()) {
                            this.fusion.log(Level.warn, "Failed to switch voucher code %s, because configuration is null.", fileName);

                            continue;
                        }

                        final CommentedConfigurationNode section = customFile.getConfiguration();

                        if (!section.hasChild("voucher-code")) {
                            this.fusion.log(Level.warn, "Failed to switch voucher code %s, because configuration section is null.", fileName);

                            continue;
                        }

                        final CommentedConfigurationNode index = section.node("voucher-code");

                        processItems(root_section, index);

                        voucher_custom_file.save();

                        final NodePath path = index.path();

                        if (path != null) {
                            this.fusion.log(Level.warn, "Successfully moved voucher code %s to %s in codes.yml.", voucher, path);
                        }
                    }

                    this.fileManager.removeFile(vouchers);
                    this.fileManager.addFile(vouchers, voucher_custom_file);

                    this.platform.updateCodes();

                    this.fusion.log(Level.warn, "Added voucher code %s to the cache, and removed the old voucher code file named %s", code_custom_file.getPrettyName(), code_file.getFileName().toString());
                } else {
                    this.fusion.log(Level.warn, "Failed to move voucher codes into codes.yml, because configuration is null.");
                }
            }
        }
    }

    @Override
    public @NonNull final Path getVouchersDirectory() {
        return this.dataPath.resolve("vouchers");
    }

    @Override
    public @NonNull final Path getCodesDirectory() {
        return this.dataPath.resolve("codes");
    }

    private void processItems(@NonNull final CommentedConfigurationNode entry, @NonNull final CommentedConfigurationNode section) {
        if (entry.hasChild("item")) {
            set(section, entry.node("item").getString("stone"), String.class, "item");
        }

        if (entry.hasChild("name")) {
            set(section, entry.node("name").getString(""), String.class, "name");
        }

        if (entry.hasChild("lore")) {
            set(section, StringUtils.getStringList(entry.node("lore")), List.class, "lore");
        }

        if (entry.hasChild("player")) {
            set(section, entry.node("player").getString(""), String.class, "player");
        }

        if (entry.hasChild("glowing")) {
            set(section, entry.node("glowing").getBoolean(false), boolean.class, "glowing");
        }

        if (entry.hasChild("display-damage")) {
            set(section, entry.node("display-damage").getInt(-1), int.class, "display-damage");
        }

        if (entry.hasChild("display-trim", "material")) {
            set(section, entry.node("display-trim", "material").getString(""), String.class, "display-trim", "material");
        }

        if (entry.hasChild("display-trim", "pattern")) {
            set(section, entry.node("display-trim", "pattern").getString(""), String.class, "display-trim", "pattern");
        }

        if (entry.hasChild("components", "hide-tooltip")) {
            set(section, entry.node("components", "hide-tooltip").getBoolean(false), boolean.class, "components", "hide-tooltip");
        }

        if (entry.hasChild("options", "is-edible")) {
            set(section, entry.node("options", "is-edible").getBoolean(false), boolean.class, "options", "is-edible");
        }

        process(entry, section);
    }

    private void process(@NonNull final CommentedConfigurationNode entry, @NonNull final CommentedConfigurationNode section) {
        if (entry.hasChild("commands")) {
            set(section, StringUtils.getStringList(entry.node("commands")), List.class, "commands");
        }

        if (entry.hasChild("items")) {
            set(section, StringUtils.getStringList(entry.node("items")), List.class, "items");
        }

        if (entry.hasChild("random-commands")) {
            set(section, StringUtils.getStringList(entry.node("random-commands")), List.class, "random-commands");
        }

        if (entry.hasChild("chance-commands")) {
            set(section, StringUtils.getStringList(entry.node("chance-commands")), List.class, "chance-commands");
        }

        if (entry.hasChild("options", "whitelist-worlds", "toggle")) {
            set(section, entry.node("options", "message").getString(""), String.class, "options", "message");
        }

        if (entry.hasChild("options", "whitelist-worlds", "toggle")) {
            set(section, entry.node("options", "whitelist-worlds", "toggle").getBoolean(false), boolean.class, "options", "whitelist-worlds", "toggle");
        }

        if (entry.hasChild("options", "whitelist-worlds", "message")) {
            set(section, entry.node("options", "whitelist-worlds", "message").getString(""), String.class, "options", "whitelist-worlds", "message");
        }

        if (entry.hasChild("options", "whitelist-worlds", "worlds")) {
            set(section, StringUtils.getStringList(entry.node("options.whitelist-worlds.worlds")), List.class, "options", "whitelist-worlds", "worlds");
        }

        if (entry.hasChild("options", "permission", "whitelist-permission", "toggle")) {
            set(section, entry.node("options", "permission", "whitelist-permission", "toggle").getBoolean(false), boolean.class, "options", "permission", "whitelist-permission", "toggle");
        }

        if (entry.hasChild("options", "permission", "whitelist-permission", "message")) {
            set(section, entry.node("options", "permission", "whitelist-permission", "message").getString(""), String.class, "options", "permission", "whitelist-permission", "message");
        }

        if (entry.hasChild("options", "permission", "whitelist-permission", "permissions")) {
            final List<String> permissions = StringUtils.getStringList(entry.node("options", "permission", "whitelist-permission", "permissions"));

            set(section, permissions, List.class, "options", "permission", "whitelist-permission", "permissions");
        }

        if (entry.hasChild("options", "permission", "blacklist-permission", "toggle")) {
            set(section, entry.node("options", "permission", "blacklist-permission", "toggle").getBoolean(false), boolean.class, "options", "permission", "blacklist-permission", "toggle");
        }

        if (entry.hasChild("options", "permission", "blacklist-permission", "message")) {
            set(section, entry.node("options", "permission", "blacklist-permission", "message").getString(""), String.class, "options", "permission", "blacklist-permission", "message");
        }

        if (entry.hasChild("options", "permission", "blacklist-permission", "permissions")) {
            set(section, StringUtils.getStringList(entry.node("options", "permission", "blacklist-permission", "permissions")), List.class, "options", "permission", "blacklist-permission", "permissions");
        }

        if (entry.hasChild("options", "limiter", "toggle")) {
            set(section, entry.node("options", "limiter", "toggle").getBoolean(false), boolean.class, "options", "limiter", "toggle");
        }

        if (entry.hasChild("options", "limiter", "amount")) {
            set(section, entry.node("options", "limiter", "amount").getInt(1), int.class, "options", "limiter", "amount");
        }

        if (entry.hasChild("options", "two-step-authentication")) {
            set(section, entry.node("options", "two-step-authentication").getBoolean(false), boolean.class, "options", "two-step-authentication");
        }

        if (entry.hasChild("options", "sound", "toggle")) {
            set(section, entry.node("options", "sound", "toggle").getBoolean(false), boolean.class, "options", "sound", "toggle");
        }

        if (entry.hasChild("options", "sound", "volume")) {
            set(section, entry.node("options", "sound", "volume").getDouble(1.0), double.class, "options", "sound", "volume");
        }

        if (entry.hasChild("options", "sound", "pitch")) {
            set(section, entry.node("options", "sound", "pitch").getDouble(1.0), double.class, "options", "sound", "pitch");
        }

        if (entry.hasChild("options", "sound", "sounds")) {
            set(section, StringUtils.getStringList(entry.node("options", "sound", "sounds")), List.class, "options", "sound", "sounds");
        }

        if (entry.hasChild("options", "firework", "toggle")) {
            set(section, entry.node("options", "firework", "toggle").getBoolean(false), boolean.class, "options", "firework", "toggle");
        }

        if (entry.hasChild("options", "fireworks", "colors")) {
            set(section, entry.node("options", "fireworks", "colors").getString(""), String.class, "options", "fireworks", "colors");
        }
    }
}