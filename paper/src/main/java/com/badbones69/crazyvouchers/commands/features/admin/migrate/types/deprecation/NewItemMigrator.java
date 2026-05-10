package com.badbones69.crazyvouchers.commands.features.admin.migrate.types.deprecation;

import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.Comments;
import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.core.api.enums.Level;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.paper.files.types.PaperCustomFile;
import com.ryderbelserion.fusion.paper.utils.ItemUtils;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
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

        final FileSystem system = this.config.getProperty(ConfigKeys.file_system);

        switch (system) {
            case SINGLE -> {
                final YamlConfiguration codes = FileKeys.codes.getConfiguration();

                final ConfigurationSection codeSection = codes.getConfigurationSection("voucher-codes");

                if (codeSection != null) {
                    for (final String code : codeSection.getKeys(false)) {
                        final ConfigurationSection item = codeSection.getConfigurationSection(code);

                        if (item == null) {
                            this.fusion.log(Level.WARNING, "Failed to migrate code %s, because section is null.", code);

                            continue;
                        }

                        final boolean isSave = process(item);

                        if (isSave) {
                            this.fusion.log(Level.INFO, "Successfully migrated code %s, and saved to file!", code);

                            FileKeys.codes.save();
                        }
                    }
                }

                final YamlConfiguration vouchers = FileKeys.vouchers.getConfiguration();

                final ConfigurationSection voucherSection = vouchers.getConfigurationSection("vouchers");

                if (voucherSection != null) {
                    for (final String voucher : voucherSection.getKeys(false)) {
                        final ConfigurationSection item = voucherSection.getConfigurationSection(voucher);

                        if (item == null) {
                            this.fusion.log(Level.WARNING, "Failed to migrate voucher %s, because section is null.", voucher);

                            continue;
                        }

                        final boolean isSave = process(item);

                        if (isSave) {
                            this.fusion.log(Level.INFO, "Successfully migrated voucher %s, and saved to file!", voucher);

                            FileKeys.vouchers.save();
                        }
                    }
                }
            }

            case MULTIPLE -> {
                final List<Path> vouchers = this.fusion.getFilesByPath(getVouchersDirectory(), ".yml");

                for (final Path voucher : vouchers) {
                    final Optional<PaperCustomFile> optional = this.fileManager.getPaperFile(voucher);

                    if (optional.isEmpty()) {
                        this.fusion.log(Level.WARNING, "Failed to migrate voucher %s, because file does not exist in the cache.", voucher);

                        continue;
                    }

                    final PaperCustomFile customFile = optional.get();

                    try {
                        if (!customFile.isLoaded()) {
                            this.fusion.log(Level.WARNING, "Failed to migrate voucher %s, because section is null.", voucher);

                            continue;
                        }

                        final YamlConfiguration configuration = customFile.getConfiguration();

                        final ConfigurationSection section = configuration.getConfigurationSection("voucher");

                        if (section == null) {
                            this.fusion.log(Level.WARNING, "Failed to migrate voucher %s, because configuration section is null.", voucher);

                            continue;
                        }

                        boolean isSave = process(section);

                        if (isSave) {
                            this.fusion.log(Level.INFO, "Successfully migrated voucher %s, and saved to file!", voucher);

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
                    final Optional<PaperCustomFile> optional = this.fileManager.getPaperFile(code);

                    if (optional.isEmpty()) {
                        this.fusion.log(Level.WARNING, "Failed to migrate code %s, because file does not exist in the cache.", code);

                        continue;
                    }

                    final PaperCustomFile customFile = optional.get();

                    try {
                        if (!customFile.isLoaded()) {
                            this.fusion.log(Level.WARNING, "Failed to migrate code %s, because section is null.", code);

                            continue;
                        }

                        final YamlConfiguration configuration = customFile.getConfiguration();

                        final ConfigurationSection section = configuration.getConfigurationSection("voucher-code");

                        if (section == null) {
                            this.fusion.log(Level.WARNING, "Failed to migrate code %s, because configuration section is null.", code);

                            continue;
                        }

                        boolean isSave = process(section);

                        if (isSave) {
                            this.fusion.log(Level.INFO, "Successfully migrated code %s, and saved to file!", code);

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

        this.crazyManager.reloadVouchers();
        this.crazyManager.reloadCodes();
    }

    @Override
    public <T> void set(@NotNull final ConfigurationSection section, @NotNull final String path, @NotNull final T value) {
        section.set(path, value);
    }

    private boolean process(final ConfigurationSection section) {
        boolean isSave = false;

        for (final String value : section.getKeys(false)) {
            if (!section.isList("items")) continue;

            final List<String> items = section.getStringList("items");

            section.set("items", null);

            final ConfigurationSection itemSection = section.createSection("items");

            items.forEach(item -> {
                final Map<String, String> patterns = new HashMap<>();
                final Map<String, Integer> enchantments = new HashMap<>();
                final String uuid = Methods.randomUUID();

                final ConfigurationSection uuidSection = itemSection.createSection(uuid);

                for (final String key : item.split(", ")) {
                    final String option = key.split(":")[0];
                    final String type = key.replace(option + ":", "").replace(option, "");

                    switch (option.toLowerCase()) {
                        case "item" -> {
                            uuidSection.set("material", type);
                            uuidSection.setComments("material", Comments.material.getComments());
                        }
                        case "data" -> {
                            uuidSection.set("data", type);
                            uuidSection.setComments("data", Comments.base64.getComments());
                        }
                        case "name" -> {
                            uuidSection.set("name", type);
                            uuidSection.setComments("name", Comments.name.getComments());
                        }
                        case "mob" -> {
                            uuidSection.set("settings.mob.type", type);
                            uuidSection.setComments("settings.mob.type", Comments.mob_type.getComments());
                        }
                        case "glowing" -> {
                            uuidSection.set("settings.glowing", type);
                            uuidSection.setComments("settings.glowing", Comments.glowing.getComments());
                        }
                        case "amount" -> {
                            uuidSection.set("amount", type);
                            uuidSection.setComments("amount", Comments.amount.getComments());
                        }
                        case "damage" -> {
                            uuidSection.set("settings.damage", type);
                            uuidSection.setComments("settings.damage", Comments.damage.getComments());
                        }
                        case "lore" -> {
                            uuidSection.set("lore", List.of(type.split(",")));
                            uuidSection.setComments("lore", Comments.lore.getComments());
                        }
                        case "player" -> {
                            uuidSection.set("settings.player", type);
                            uuidSection.setComments("settings.player", Comments.player.getComments());
                        }
                        case "skull" -> {
                            uuidSection.set("settings.skull", type);
                            uuidSection.setComments("settings.skull", Comments.skull.getComments());
                        }
                        case "custom-model-data" -> {
                            uuidSection.set("custom-model-data", type);
                            uuidSection.setComments("custom-model-data", Comments.custom_model_data.getComments());
                        }
                        case "unbreakable-item" -> {
                            uuidSection.set("unbreakable-item", type);
                            uuidSection.setComments("unbreakable-item", Comments.unbreakable.getComments());
                        }
                        case "hide-tool-tip" -> {
                            uuidSection.set("hide-tool-tip", type);
                            uuidSection.setComments("hide-tool-tip", Comments.hide_tool_tip.getComments());
                        }
                        case "trim-pattern" -> {
                            uuidSection.set("settings.trim.pattern", type);
                            uuidSection.setComments("trim.pattern", Comments.trim_pattern.getComments());
                        }
                        case "trim-material" -> {
                            uuidSection.set("settings.trim.material", type);
                            uuidSection.setComments("settings.trim.material", Comments.trim_material.getComments());
                        }
                        case "trim" -> {
                            final String[] split = type.split("!");

                            final String trim = split[0];
                            final String material = split[1];

                            uuidSection.set("settings.trim.pattern", trim);
                            uuidSection.setComments("trim.pattern", Comments.trim_pattern.getComments());

                            uuidSection.set("settings.trim.material", material);
                            uuidSection.setComments("settings.trim.material", Comments.trim_material.getComments());
                        }
                        case "rgb" -> {
                            uuidSection.set("settings.rgb", type);
                            uuidSection.setComments("settings.rgb", Comments.rgb.getComments());
                        }
                        case "color" -> {
                            uuidSection.set("settings.color", type);
                            uuidSection.setComments("settings.color", Comments.color.getComments());
                        }
                        default -> {
                            final String placeholder = option.toLowerCase();

                            try {
                                final PotionEffectType effect = ItemUtils.getPotionEffect(placeholder);

                                if (effect != null) {
                                    final ConfigurationSection potionsSection = uuidSection.createSection("settings.potions");

                                    final ConfigurationSection potionSection = potionsSection.createSection(placeholder);

                                    potionSection.set("duration", 60);
                                    potionSection.set("level", 1);

                                    potionSection.set("style.icon", true);
                                    potionSection.set("style.ambient", true);
                                    potionSection.set("style.particles", true);

                                    uuidSection.set("settings.potions", Comments.potions.getComments());
                                }
                            } catch (Exception ignored) {
                            }

                            if (ItemUtils.getEnchantment(placeholder) != null) {
                                enchantments.put(option.toLowerCase(), StringUtils.tryParseInt(value).map(Number::intValue).orElse(1));

                                final ConfigurationSection enchantmentSection = uuidSection.createSection("enchantments");

                                uuidSection.setComments("enchantments", Comments.enchantments.getComments());

                                enchantments.forEach(enchantmentSection::set);

                                break;
                            }

                            if (!uuidSection.contains("hide-tool-tip")) {
                                for (ItemFlag itemFlag : ItemFlag.values()) {
                                    if (itemFlag.name().equalsIgnoreCase(option)) {
                                        uuidSection.set("hide-tool-tip", true);
                                        uuidSection.setComments("hide-tool-tip", Comments.hide_tool_tip.getComments());

                                        break;
                                    }
                                }
                            }

                            try {
                                final PatternType patternType = ItemUtils.getPatternType(placeholder);

                                if (patternType != null) {
                                    patterns.put(placeholder, type);

                                    final ConfigurationSection patternsSection = uuidSection.createSection("settings.patterns");

                                    uuidSection.setComments("settings.patterns", Comments.patterns.getComments());

                                    patterns.forEach(patternsSection::set);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });

            isSave = true;
        }

        return isSave;
    }

    @Override
    public @NotNull final Path getVouchersDirectory() {
        return this.dataPath.resolve("vouchers");
    }

    @Override
    public @NotNull final Path getCodesDirectory() {
        return this.dataPath.resolve("codes");
    }
}