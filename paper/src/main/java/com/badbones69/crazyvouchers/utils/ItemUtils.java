package com.badbones69.crazyvouchers.utils;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.core.api.utils.StringUtils;
import com.ryderbelserion.fusion.paper.api.builders.items.ItemBuilder;
import com.ryderbelserion.fusion.paper.api.builders.items.types.PatternBuilder;
import com.ryderbelserion.fusion.paper.api.builders.items.types.PotionBuilder;
import com.ryderbelserion.fusion.paper.api.builders.items.types.SkullBuilder;
import com.ryderbelserion.fusion.paper.api.builders.items.types.SpawnerBuilder;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemUtils {

    private static final CrazyVouchers plugin = CrazyVouchers.get();

    private static final boolean isLogging = plugin.getFusion().isVerbose();

    private static final ComponentLogger logger = plugin.getComponentLogger();

    /**
     * Converts a String to an ItemBuilder.
     *
     * @param itemString the string you wish to convert.
     * @return the string as an ItemBuilder.
     */
    public static ItemBuilder convertString(String itemString) {
        return convertString(itemString, null);
    }

    public static List<ItemBuilder> convertConfigurationSection(final ConfigurationSection section) {
        final List<ItemBuilder> cache = new ArrayList<>();

        if (section == null) return cache;

        for (final String key : section.getKeys(false)) {
            final ConfigurationSection item = section.getConfigurationSection(key);

            if (item == null) continue;

            final ItemBuilder itemBuilder = ItemBuilder.from(item.getString("material", "stone"));

            if (item.contains("data")) {
                final String base64 = item.getString("data", null);

                if (base64 != null && !base64.isEmpty()) { //todo() move this if check to fusion's itembuilder as we should not set a name if it's empty to ensure Minecraft can do it's thing.
                    itemBuilder.withBase64(base64);
                }
            }

            if (item.contains("name")) { //todo() move this if check to fusion's itembuilder as we should not set a name if it's empty to ensure Minecraft can do it's thing.
                itemBuilder.setDisplayName(item.getString("name", ""));
            }

            if (item.contains("lore")) {
                itemBuilder.withDisplayLore(item.getStringList("lore"));
            }

            itemBuilder.setAmount(item.getInt("amount", 1));

            final ConfigurationSection enchantments = item.getConfigurationSection("enchantments");

            if (enchantments != null) {
                for (final String enchantment : enchantments.getKeys(false)) {
                    final int level = enchantments.getInt(enchantment);

                    itemBuilder.addEnchantment(enchantment, level);
                }
            }

            itemBuilder.setCustomModelData(item.getString("custom-model-data", ""));

            if (item.getBoolean("hide-tool-tip", false)) {
                itemBuilder.hideToolTip();
            }

            if (item.contains("hide-tooltip-advanced") && item.isList("hide-tooltip-advanced")) {
                itemBuilder.hideComponents(item.getStringList("hide-tooltip-advanced"));
            }

            itemBuilder.setItemModel(item.getString("item-model.namespace", ""), item.getString("item-model.key", ""));

            itemBuilder.setUnbreakable(item.getBoolean("unbreakable-item", false));

            // settings
            itemBuilder.setEnchantGlint(item.getBoolean("settings.glowing", false));

            final String player = item.getString("settings.player", null);

            if (player != null && !player.isEmpty()) {
                final SkullBuilder skullBuilder = itemBuilder.asSkullBuilder();

                skullBuilder.withName(player).build();
            }

            itemBuilder.setItemDamage(item.getInt("settings.damage", 0));

            itemBuilder.withSkull(item.getString("settings.skull", ""));

            final String rgb = item.getString("settings.rgb", "");

            final String color = item.getString("settings.color", "");

            itemBuilder.setColor(!color.isEmpty() ? color : !rgb.isEmpty() ? rgb : "");

            final String mobType = item.getString("settings.mob.type", null);

            if (mobType != null && !mobType.isEmpty()) {
                final SpawnerBuilder spawnerBuilder = itemBuilder.asSpawnerBuilder();

                spawnerBuilder.withEntityType(com.ryderbelserion.fusion.paper.utils.ItemUtils.getEntity(mobType)).build();
            }

            itemBuilder.setTrim(item.getString("settings.trim.pattern", ""), item.getString("settings.trim.material", ""));

            final ConfigurationSection potions = item.getConfigurationSection("settings.potions");

            if (potions != null) {
                final PotionBuilder potionBuilder = itemBuilder.asPotionBuilder();

                for (final String potion : potions.getKeys(false)) {
                    final PotionEffectType type = com.ryderbelserion.fusion.paper.utils.ItemUtils.getPotionEffect(potion);

                    if (type != null) {
                        final ConfigurationSection data = potions.getConfigurationSection(potion);

                        if (data != null) {
                            final int duration = data.getInt("duration", 10) * 20;
                            final int level = data.getInt("level", 1);

                            final boolean icon = data.getBoolean("style.icon", false);
                            final boolean ambient = data.getBoolean("style.ambient", false);
                            final boolean particles = data.getBoolean("style.particles", false);

                            potionBuilder.withPotionEffect(type, duration, level, ambient, particles, icon);
                        }
                    }
                }

                potionBuilder.build();
            }

            final ConfigurationSection patterns = item.getConfigurationSection("settings.patterns");

            if (patterns != null) {
                for (final String pattern : patterns.getKeys(false)) {
                    final String patternColor = patterns.getString(pattern, "white");

                    final PatternBuilder patternBuilder = itemBuilder.asPatternBuilder();

                    patternBuilder.addPattern(pattern, patternColor);

                    patternBuilder.build();
                }
            }

            cache.add(itemBuilder);
        }

        return cache;
    }

    /**
     * Converts a string to an ItemBuilder with a placeholder for errors.
     *
     * @param itemString the string you wish to convert.
     * @param placeHolder the placeholder to use if there is an error.
     * @return the string as an ItemBuilder.
     */
    public static ItemBuilder convertString(String itemString, String placeHolder) {
        ItemBuilder itemBuilder = ItemBuilder.from(ItemType.STONE);

        try {
            for (String optionString : itemString.split(", ")) {
                String option = optionString.split(":")[0];
                String value = optionString.replace(option + ":", "").replace(option, "");

                switch (option.toLowerCase()) {
                    case "item" -> itemBuilder.withCustomItem(value.toLowerCase());
                    case "name" -> itemBuilder.setDisplayName(value);
                    case "amount" -> {
                        try {
                            itemBuilder.setAmount(Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            itemBuilder.setAmount(1);
                        }
                    }
                    case "damage" -> {
                        try {
                            itemBuilder.setItemDamage(Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            itemBuilder.setItemDamage(0);
                        }
                    }
                    case "lore" -> itemBuilder.withDisplayLore(Arrays.asList(value.split(",")));
                    case "player" -> {
                        try {
                            itemBuilder.asSkullBuilder().withName(value);
                        } catch (final Exception exception) {
                            if (isLogging) {
                                logger.warn("Could create skull builder because the item is not a player head.");
                                logger.warn("This warning is safe to ignore, it's a restriction of the current system.");
                            }
                        }
                    }
                    case "skull" -> itemBuilder.withSkull(value);
                    case "unbreakable-item" -> {
                        if (value.isEmpty() || value.equalsIgnoreCase("true")) itemBuilder.setUnbreakable(true);
                    }

                    case "trim" -> {
                        String[] split = value.split("!"); // trim:trim_pattern!trim_material

                        String trim = split[0];
                        String material = split[1];

                        itemBuilder.setTrim(trim.toLowerCase(), material.toLowerCase());
                    }

                    default -> {
                        final Enchantment enchantment = com.ryderbelserion.fusion.paper.utils.ItemUtils.getEnchantment(getEnchant(option));

                        if (enchantment != null) {
                            final Optional<Number> level = StringUtils.tryParseInt(value);

                            itemBuilder.addEnchantment(getEnchant(option), level.map(Number::intValue).orElse(1));
                        }

                        if (itemBuilder.isBanner() || itemBuilder.isShield()) {
                            final PatternBuilder builder = itemBuilder.asPatternBuilder();

                            builder.addPattern(value, option).build();
                        }
                    }
                }
            }
        } catch (final Exception exception) {
            itemBuilder.withType(ItemType.RED_TERRACOTTA).setDisplayName("<red>ERROR").withDisplayLore(Arrays.asList("<red>There is an error", "<red>For : <red>" + (placeHolder != null ? placeHolder : "")));

            exception.printStackTrace();
        }

        return itemBuilder;
    }

    /**
     * Converts a list of Strings to a list of ItemBuilders.
     *
     * @param itemStrings the list of Strings.
     * @return the list of ItemBuilders.
     */
    public static List<ItemBuilder> convertStringList(List<String> itemStrings) {
        return convertStringList(itemStrings, null);
    }

    /**
     * Converts a list of Strings to a list of ItemBuilders with a placeholder for errors.
     *
     * @param itemStrings the list of strings.
     * @param placeholder the placeholder for errors.
     * @return the list of ItemBuilders.
     */
    public static List<ItemBuilder> convertStringList(List<String> itemStrings, String placeholder) {
        return itemStrings.stream().map(itemString -> convertString(itemString, placeholder)).collect(Collectors.toList());
    }

    public static String getEnchant(String enchant) {
        if (enchant.isEmpty()) return "";

        switch (enchant) {
            case "PROTECTION_ENVIRONMENTAL" -> {
                return "protection";
            }

            case "PROTECTION_FIRE" -> {
                return "fire_protection";
            }

            case "PROTECTION_FALL" -> {
                return "feather_falling";
            }

            case "PROTECTION_EXPLOSIONS" -> {
                return "blast_protection";
            }

            case "PROTECTION_PROJECTILE" -> {
                return "projectile_protection";
            }

            case "OXYGEN" -> {
                return "respiration";
            }

            case "WATER_WORKER" -> {
                return "aqua_affinity";
            }

            case "DAMAGE_ALL" -> {
                return "sharpness";
            }

            case "DAMAGE_UNDEAD" -> {
                return "smite";
            }

            case "DAMAGE_ARTHROPODS" -> {
                return "bane_of_arthropods";
            }

            case "LOOT_BONUS_MOBS" -> {
                return "looting";
            }

            case "SWEEPING_EDGE" -> {
                return "sweeping";
            }

            case "DIG_SPEED" -> {
                return "efficiency";
            }

            case "DURABILITY" -> {
                return "unbreaking";
            }

            case "LOOT_BONUS_BLOCKS" -> {
                return "fortune";
            }

            case "ARROW_DAMAGE" -> {
                return "power";
            }

            case "ARROW_KNOCKBACK" -> {
                return "punch";
            }

            case "ARROW_FIRE" -> {
                return "flame";
            }

            case "ARROW_INFINITE" -> {
                return "infinity";
            }

            case "LUCK" -> {
                return "luck_of_the_sea";
            }

            default -> {
                return enchant.toLowerCase();
            }
        }
    }
}