package com.badbones69.crazyvouchers.commands.features.admin.migrate.types.deprecation;

import com.badbones69.crazyvouchers.Methods;
import com.badbones69.crazyvouchers.api.enums.Comments;
import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.FileSystem;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.IVoucherMigrator;
import com.badbones69.crazyvouchers.commands.features.admin.migrate.enums.MigrationType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.ryderbelserion.fusion.core.api.utils.FileUtils;
import com.ryderbelserion.fusion.core.api.utils.StringUtils;
import com.ryderbelserion.fusion.paper.files.FileManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewItemMigrator extends IVoucherMigrator {

    private final FileManager fileManager = this.plugin.getFileManager();

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

                if (codes != null) {
                    final ConfigurationSection section = codes.getConfigurationSection("voucher-codes");

                    if (section != null) {
                        for (final String code : section.getKeys(false)) {
                            final ConfigurationSection item = section.getConfigurationSection(code);

                            if (item == null) continue;

                            final boolean isSave = process(item);

                            if (isSave) {
                                FileKeys.codes.save();
                            }
                        }
                    }
                }

                final YamlConfiguration vouchers = FileKeys.vouchers.getConfiguration();

                if (vouchers != null) {
                    final ConfigurationSection section = vouchers.getConfigurationSection("vouchers");

                    if (section != null) {
                        for (final String voucher : section.getKeys(false)) {
                            final ConfigurationSection item = section.getConfigurationSection(voucher);

                            if (item == null) continue;

                            final boolean isSave = process(item);

                            if (isSave) {
                                FileKeys.vouchers.save();
                            }
                        }
                    }
                }
            }

            case MULTIPLE -> {
                final List<Path> vouchers = FileUtils.getFiles(getVouchersDirectory(), ".yml");

                for (final Path voucher : vouchers) {
                    final PaperCustomFile customFile = this.fileManager.getPaperCustomFile(voucher);

                    if (customFile == null) continue;

                    try {
                        final YamlConfiguration configuration = customFile.getConfiguration();

                        if (configuration == null) continue;

                        final ConfigurationSection section = configuration.getConfigurationSection("voucher");

                        if (section == null) continue;

                        boolean isSave = process(section);

                        if (isSave) {
                            customFile.save();
                        }

                        success.add("<green>⤷ " + customFile.getPrettyName());
                    } catch (final Exception exception) {
                        failed.add("<red>⤷ " + customFile.getPrettyName());
                    }
                }

                final List<Path> codes = FileUtils.getFiles(getCodesDirectory(), ".yml");

                for (final Path code : codes) {
                    final PaperCustomFile customFile = this.fileManager.getPaperCustomFile(code);

                    if (customFile == null) continue;

                    try {
                        final YamlConfiguration configuration = customFile.getConfiguration();

                        if (configuration == null) continue;

                        final ConfigurationSection section = configuration.getConfigurationSection("voucher-code");

                        if (section == null) continue;

                        boolean isSave = process(section);

                        if (isSave) {
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

        sendMessage(new ArrayList<>(failedCrates + convertedCrates) {{
            addAll(failed);
            addAll(success);
        }}, convertedCrates, failedCrates);

        this.fileManager.init(new ArrayList<>());

        // reload crates
        this.crazyManager.load(true);
    }

    @Override
    public <T> void set(@NotNull final ConfigurationSection section, @NotNull final String path, @NotNull final T value) {
        section.set(path, value);
    }

    private boolean process(final ConfigurationSection section) {
        boolean isSave = false;

        for (String value : section.getKeys(false)) {
            final ConfigurationSection prizeSection = section.getConfigurationSection(value);

            if (prizeSection == null) continue;

            if (prizeSection.isList("items")) {
                final List<String> items = new ArrayList<>() {{
                    addAll(prizeSection.getStringList("items"));
                }};

                prizeSection.set("items", null);

                items.forEach(item -> {
                    final Map<String, String> patterns = new HashMap<>();
                    final Map<String, Integer> enchantments = new HashMap<>();
                    final String uuid = Methods.randomUUID();

                    for (final String key : item.split(", ")) {
                        final String option = key.split(":")[0];
                        final String type = key.replace(option + ":", "").replace(option, "");

                        switch (option.toLowerCase()) {
                            case "item" -> {
                                prizeSection.set("items." + uuid + ".material", type);
                                prizeSection.setComments("items." + uuid + ".Material", Comments.material.getComments());
                            }
                            case "data" -> {
                                prizeSection.set("items." + uuid + ".data", type);
                                prizeSection.setComments("items." + uuid + ".data", Comments.base64.getComments());
                            }
                            case "name" -> {
                                prizeSection.set("items." + uuid + ".name", type);
                                prizeSection.setComments("items." + uuid + ".name", Comments.name.getComments());
                            }
                            case "mob" -> {
                                prizeSection.set("items." + uuid + ".settings.mob.type", type);
                                prizeSection.setComments("items." + uuid + ".settings.mob.type", Comments.mob_type.getComments());
                            }
                            case "glowing" -> {
                                prizeSection.set("items." + uuid + ".settings.glowing", type);
                                prizeSection.setComments("items." + uuid + ".settings.glowing", Comments.glowing.getComments());
                            }
                            case "amount" -> {
                                prizeSection.set("items." + uuid + ".amount", type);
                                prizeSection.setComments("items." + uuid + ".amount", Comments.amount.getComments());
                            }
                            case "damage" -> {
                                prizeSection.set("items." + uuid + ".settings.damage", type);
                                prizeSection.setComments("items." + uuid + ".settings.damage", Comments.damage.getComments());
                            }
                            case "lore" -> {
                                prizeSection.set("items." + uuid + ".lore", List.of(type.split(",")));
                                prizeSection.setComments("items." + uuid + ".lore", Comments.lore.getComments());
                            }
                            case "player" -> {
                                prizeSection.set("items." + uuid + ".settings.player", type);
                                prizeSection.setComments("items." + uuid + ".settings.player", Comments.player.getComments());
                            }
                            case "skull" -> {
                                prizeSection.set("items." + uuid + ".settings.skull", type);
                                prizeSection.setComments("items." + uuid + ".settings.skull", Comments.skull.getComments());
                            }
                            case "custom-model-data" -> {
                                prizeSection.set("items." + uuid + ".custom-model-data", type);
                                prizeSection.setComments("items." + uuid + ".custom-model-data", Comments.custom_model_data.getComments());
                            }
                            case "unbreakable-item" -> {
                                prizeSection.set("items." + uuid + ".unbreakable-item", type);
                                prizeSection.setComments("items." + uuid + ".unbreakable-item", Comments.unbreakable.getComments());
                            }
                            case "hide-tool-tip" -> {
                                prizeSection.set("items." + uuid + ".hide-tool-tip", type);
                                prizeSection.setComments("items." + uuid + ".hide-tool-tip", Comments.hide_tool_tip.getComments());
                            }
                            case "trim-pattern" -> {
                                prizeSection.set("items." + uuid + ".settings.trim.pattern", type);
                                prizeSection.setComments("items." + uuid + ".trim.pattern", Comments.trim_pattern.getComments());
                            }
                            case "trim-material" -> {
                                prizeSection.set("items." + uuid + ".settings.trim.material", type);
                                prizeSection.setComments("items." + uuid + ".settings.trim.material", Comments.trim_material.getComments());
                            }
                            case "trim" -> {
                                final String[] split = type.split("!");

                                final String trim = split[0];
                                final String material = split[1];

                                prizeSection.set("items." + uuid + ".settings.trim.pattern", trim);
                                prizeSection.setComments("items." + uuid + ".trim.pattern", Comments.trim_pattern.getComments());

                                prizeSection.set("items." + uuid + ".settings.trim.material", material);
                                prizeSection.setComments("items." + uuid + ".settings.trim.material", Comments.trim_material.getComments());
                            }
                            case "rgb" -> {
                                prizeSection.set("items." + uuid + ".settings.rgb", type);
                                prizeSection.setComments("items." + uuid + ".settings.rgb", Comments.rgb.getComments());
                            }
                            case "color" -> {
                                prizeSection.set("items." + uuid + ".settings.color", type);
                                prizeSection.setComments("items." + uuid + ".settings.color", Comments.color.getComments());
                            }
                            default -> {
                                final String placeholder = option.toLowerCase();

                                try {
                                    final PotionEffectType effect = ItemUtils.getPotionEffect(placeholder);

                                    if (effect != null) {
                                        final ConfigurationSection potionsSection = prizeSection.createSection("items." + uuid + ".settings.potions");

                                        final ConfigurationSection potionSection = potionsSection.createSection(placeholder);

                                        potionSection.set("duration", 60);
                                        potionSection.set("level", 1);

                                        potionSection.set("style.icon", true);
                                        potionSection.set("style.ambient", true);
                                        potionSection.set("style.particles", true);

                                        prizeSection.set("items." + uuid + ".settings.potions", Comments.potions.getComments());
                                    }
                                } catch (Exception ignored) {}

                                if (ItemUtils.getEnchantment(placeholder) != null) {
                                    enchantments.put(option.toLowerCase(), StringUtils.tryParseInt(value).map(Number::intValue).orElse(1));

                                    final ConfigurationSection enchantmentSection = prizeSection.createSection("items." + uuid + ".enchantments");

                                    prizeSection.setComments("items." + uuid + ".enchantments", Comments.enchantments.getComments());

                                    enchantments.forEach(enchantmentSection::set);

                                    break;
                                }

                                if (!prizeSection.contains("items." + uuid + ".hide-tool-tip")) {
                                    for (ItemFlag itemFlag : ItemFlag.values()) {
                                        if (itemFlag.name().equalsIgnoreCase(option)) {
                                            prizeSection.set("items." + uuid + ".hide-tool-tip", true);
                                            prizeSection.setComments("items." + uuid + ".hide-tool-tip", Comments.hide_tool_tip.getComments());

                                            break;
                                        }
                                    }
                                }

                                try {
                                    final PatternType patternType = ItemUtils.getPatternType(placeholder);

                                    if (patternType != null) {
                                        patterns.put(placeholder, type);

                                        final ConfigurationSection patternsSection = prizeSection.createSection("items." + uuid + ".settings.patterns");

                                        prizeSection.setComments("items." + uuid + ".settings.patterns", Comments.patterns.getComments());

                                        patterns.forEach(patternsSection::set);
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                });

                isSave = true;
            }
        }

        return isSave;
    }
}