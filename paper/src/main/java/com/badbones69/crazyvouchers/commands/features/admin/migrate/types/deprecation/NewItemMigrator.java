package com.badbones69.crazyvouchers.commands.features.admin.migrate.types.deprecation;

import com.badbones69.crazyvouchers.Methods;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.FileComments;
import us.crazycrew.crazyvouchers.api.enums.other.FileSystem;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.files.types.configurate.YamlCustomFile;
import com.ryderbelserion.fusion.paper.utils.ItemUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemFlag;
import org.jspecify.annotations.NonNull;
import java.nio.file.Path;
import java.util.*;

public class NewItemMigrator extends IVoucherMigrator {

    public NewItemMigrator(final CommandSender sender) {
        super(sender, MigrationType.NEW_ITEM_FORMAT);
    }

    @Override
    public void run() {
        final List<String> failed = new ArrayList<>();
        final List<String> success = new ArrayList<>();

        final FileSystem system = this.config.getProperty(RootKeys.file_system);

        switch (system) {
            case SINGLE -> {
                this.fileManager.getYamlFile(this.dataPath.resolve("codes.yml")).ifPresent(customFile -> {
                    final CommentedConfigurationNode section = customFile.getConfiguration();

                    if (section.hasChild("voucher-codes")) {
                        final CommentedConfigurationNode codes = section.node("voucher-codes");

                        for (final Map.Entry<Object, CommentedConfigurationNode> index : codes.childrenMap().entrySet()) {
                            final CommentedConfigurationNode entry = index.getValue();
                            final String id = index.getKey().toString();

                            if (process(entry)) {
                                this.fusion.log(Level.info, "Successfully migrated code %s, and saved to file!", id);

                                customFile.save();
                            }
                        }
                    }
                });

                this.fileManager.getYamlFile(this.dataPath.resolve("vouchers.yml")).ifPresent(customFile -> {
                    final CommentedConfigurationNode section = customFile.getConfiguration();

                    if (section.hasChild("vouchers")) {
                        final CommentedConfigurationNode vouchers = section.node("vouchers");

                        for (final Map.Entry<Object, CommentedConfigurationNode> index : vouchers.childrenMap().entrySet()) {
                            final CommentedConfigurationNode entry = index.getValue();
                            final String id = index.getKey().toString();

                            if (process(entry)) {
                                this.fusion.log(Level.info, "Successfully migrated voucher %s, and saved to file!", id);

                                customFile.save();
                            }
                        }
                    }
                });
            }

            case MULTIPLE -> {
                final List<Path> vouchers = this.fusion.getFilesByPath(getVouchersDirectory(), ".yml");

                for (final Path voucher : vouchers) {
                    final Optional<YamlCustomFile> optional = this.fileManager.getYamlFile(voucher);

                    if (optional.isEmpty()) {
                        this.fusion.log(Level.warn, "Failed to migrate voucher %s, because file does not exist in the cache.", voucher);

                        continue;
                    }

                    final YamlCustomFile customFile = optional.get();

                    try {
                        if (!customFile.isLoaded()) {
                            this.fusion.log(Level.warn, "Failed to migrate voucher %s, because section is null.", voucher);

                            continue;
                        }

                        final CommentedConfigurationNode configuration = customFile.getConfiguration();

                        if (!configuration.hasChild("voucher")) {
                            this.fusion.log(Level.warn, "Failed to migrate voucher %s, because configuration section is null.", voucher);

                            continue;
                        }

                        if (process(configuration.node("voucher"))) {
                            this.fusion.log(Level.info, "Successfully migrated voucher %s, and saved to file!", voucher);

                            customFile.save();
                        }

                        success.add("<green>⤷ " + customFile.getPrettyName());
                    } catch (final Exception exception) {
                        failed.add("<red>⤷ " + customFile.getPrettyName());

                        exception.printStackTrace();
                    }
                }

                final List<Path> codes = this.fusion.getFilesByPath(getCodesDirectory(), ".yml");

                for (final Path code : codes) {
                    final Optional<YamlCustomFile> optional = this.fileManager.getYamlFile(code);

                    if (optional.isEmpty()) {
                        this.fusion.log(Level.warn, "Failed to migrate voucher code %s, because file does not exist in the cache.", code);

                        continue;
                    }

                    final YamlCustomFile customFile = optional.get();

                    try {
                        if (!customFile.isLoaded()) {
                            this.fusion.log(Level.warn, "Failed to migrate voucher code %s, because section is null.", code);

                            continue;
                        }

                        final CommentedConfigurationNode configuration = customFile.getConfiguration();

                        if (!configuration.hasChild("voucher-code")) {
                            this.fusion.log(Level.warn, "Failed to migrate voucher code %s, because configuration section is null.", code);

                            continue;
                        }

                        if (process(configuration.node("voucher-code"))) {
                            this.fusion.log(Level.info, "Successfully migrated voucher code %s, and saved to file!", code);

                            customFile.save();
                        }

                        success.add("<green>⤷ " + customFile.getPrettyName());
                    } catch (final Exception exception) {
                        failed.add("<red>⤷ " + customFile.getPrettyName());
                    }
                }
            }
        }

        final int convertedCrates = success.size();
        final int failedCrates = failed.size();

        final List<String> files = new ArrayList<>(failedCrates + convertedCrates);

        files.addAll(failed);
        files.addAll(success);

        sendMessage(files, convertedCrates, failedCrates);

        this.platform.updateVouchers();
        this.platform.updateCodes();
    }

    private boolean process(final CommentedConfigurationNode section) {
        boolean isSave = false;

        for (final CommentedConfigurationNode index : section.childrenMap().values()) {
            final CommentedConfigurationNode items = index.node("Items");

            if (!items.isList()) continue;

            final List<String> values = StringUtils.getStringList(items);

            try {
                items.set(null);
            } catch (final SerializationException exception) {
                exception.printStackTrace();
            }

            for (final String value : values) {
                if (value.isBlank()) continue;

                final Map<String, Integer> enchantments = new HashMap<>();
                final Map<String, String> patterns = new HashMap<>();
                final String uuid = Methods.randomUUID();

                final CommentedConfigurationNode uuidSection = items.node(uuid);

                for (final String key : value.split(", ")) {
                    final String option = key.split(":")[0];
                    final String type = key.replace(option + ":", "").replace(option, "");

                    switch (option.toLowerCase()) {
                        case "item" -> {
                            set(uuidSection, type, String.class, "material");
                            comment(uuidSection.node("material"), FileComments.material.getComments());
                        }
                        case "data" -> {
                            set(uuidSection, type, String.class, "data");
                            comment(uuidSection.node("data"), FileComments.base64.getComments());
                        }
                        case "name" -> {
                            set(uuidSection, type, String.class, "name");
                            comment(uuidSection.node("name"), FileComments.name.getComments());
                        }
                        case "mob" -> {
                            set(uuidSection, type, String.class, "settings", "mob", "type");
                            comment(uuidSection.node("settings", "mob", "type"), FileComments.mob_type.getComments());
                        }
                        case "glowing" -> {
                            set(uuidSection, type, boolean.class, "settings", "glowing");
                            comment(uuidSection.node("settings", "glowing"), FileComments.glowing.getComments());
                        }
                        case "amount" -> {
                            set(uuidSection, type, int.class, "amount");
                            comment(uuidSection.node("amount"), FileComments.amount.getComments());
                        }
                        case "damage" -> {
                            set(uuidSection, type, String.class, "settings", "damage");
                            comment(uuidSection.node("settings", "damage"), FileComments.damage.getComments());
                        }
                        case "lore" -> {
                            set(uuidSection, List.of(type.split(",")), List.class, "data");
                            comment(uuidSection.node("data"), FileComments.lore.getComments());
                        }
                        case "player" -> {
                            set(uuidSection, type, String.class, "settings", "player");
                            comment(uuidSection.node("settings", "player"), FileComments.player.getComments());
                        }
                        case "skull" -> {
                            set(uuidSection, type, String.class, "settings", "skull");
                            comment(uuidSection.node("settings", "skull"), FileComments.skull.getComments());
                        }
                        case "custom-model-data" -> {
                            set(uuidSection, type, String.class, "custom-model-data");
                            comment(uuidSection.node("custom-model-data"), FileComments.custom_model_data.getComments());
                        }
                        case "unbreakable-item" -> {
                            set(uuidSection, type, String.class, "unbreakable-item");
                            comment(uuidSection.node("unbreakable-item"), FileComments.unbreakable.getComments());
                        }
                        case "hide-tool-tip" -> {
                            set(uuidSection, type, String.class, "hide-tool-tip");
                            comment(uuidSection.node("hide-tool-tip"), FileComments.hide_tool_tip.getComments());
                        }
                        case "trim-pattern" -> {
                            set(uuidSection, type, String.class, "settings", "trim", "pattern");
                            comment(uuidSection.node("settings", "trim", "pattern"), FileComments.trim_pattern.getComments());
                        }
                        case "trim-material" -> {
                            set(uuidSection, type, String.class, "settings", "trim", "material");
                            comment(uuidSection.node("settings", "trim", "material"), FileComments.trim_material.getComments());
                        }
                        case "trim" -> {
                            final String[] split = type.split("!");

                            final String trim = split[0];
                            final String material = split[1];

                            set(uuidSection, trim, String.class, "settings", "trim", "pattern");
                            comment(uuidSection.node("settings", "trim", "pattern"), FileComments.trim_pattern.getComments());

                            set(uuidSection, material, String.class, "settings", "trim", "material");
                            comment(uuidSection.node("settings", "trim", "material"), FileComments.trim_material.getComments());
                        }
                        case "rgb" -> {
                            set(uuidSection, type, String.class, "settings", "rgb");
                            comment(uuidSection.node("settings", "rgb"), FileComments.rgb.getComments());
                        }
                        case "color" -> {
                            set(uuidSection, type, String.class, "settings", "color");
                            comment(uuidSection.node("settings", "color"), FileComments.color.getComments());
                        }
                        default -> {
                            final String placeholder = option.toLowerCase();

                            ItemUtils.getPotionEffect(placeholder).ifPresent(_ -> {
                                final CommentedConfigurationNode potions = uuidSection.node("settings", "potions");
                                final CommentedConfigurationNode potion = potions.node(placeholder);

                                set(potion, 60, int.class, "duration");
                                set(potion, 1, int.class, "level");

                                set(potion, true, boolean.class, "style", "icon");
                                set(potion, true, boolean.class, "style", "ambient");
                                set(potion, true, boolean.class, "style", "particles");

                                comment(potion, FileComments.potions.getComments());
                            });

                            if (ItemUtils.getEnchantment(placeholder).isPresent()) {
                                enchantments.put(option.toLowerCase(), StringUtils.tryParseInt(value).map(Number::intValue).orElse(1));

                                final CommentedConfigurationNode enchantmentSection = uuidSection.node("enchantments");

                                comment(enchantmentSection, FileComments.enchantments.getComments());

                                enchantments.forEach((enchantment, level) -> {
                                    try {
                                        enchantmentSection.node(enchantment).set(int.class, level);
                                    } catch (SerializationException exception) {
                                        exception.printStackTrace();
                                    }
                                });

                                break;
                            }

                            if (!uuidSection.hasChild("hide-tool-tip")) {
                                for (ItemFlag itemFlag : ItemFlag.values()) {
                                    if (itemFlag.name().equalsIgnoreCase(option)) {
                                        set(uuidSection, true, Boolean.class, "hide-tool-tip");
                                        comment(uuidSection.node("hide-tool-tip"), FileComments.hide_tool_tip.getComments());
                                        
                                        break;
                                    }
                                }
                            }

                            if (ItemUtils.getPatternType(placeholder).isPresent()) {
                                patterns.put(placeholder, type);

                                final CommentedConfigurationNode patternsSection = uuidSection.node("settings", "patterns");

                                comment(patternsSection, FileComments.patterns.getComments());

                                patterns.forEach((pattern, patternType) -> {
                                    try {
                                        patternsSection.node(pattern).set(String.class, patternType);
                                    } catch (SerializationException exception) {
                                        exception.printStackTrace();
                                    }
                                });
                            }
                        }
                    }
                }
                
                isSave = true;
            }
        }

        return isSave;
    }

    @Override
    public @NonNull final Path getVouchersDirectory() {
        return this.dataPath.resolve("vouchers");
    }

    @Override
    public @NonNull final Path getCodesDirectory() {
        return this.dataPath.resolve("codes");
    }
}