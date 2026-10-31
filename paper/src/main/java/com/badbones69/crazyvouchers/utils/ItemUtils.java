package com.badbones69.crazyvouchers.utils;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.api.enums.Level;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.builders.items.ItemBuilder;
import com.ryderbelserion.fusion.paper.builders.items.types.PatternBuilder;
import com.ryderbelserion.fusion.paper.builders.items.types.PotionBuilder;
import com.ryderbelserion.fusion.paper.builders.items.types.SkullBuilder;
import com.ryderbelserion.fusion.paper.builders.items.types.custom.CustomBuilder;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemUtils {

    private static @NonNull final CrazyVouchers plugin = CrazyVouchers.get();

    private static @NonNull final FusionPaper fusion = plugin.getFusion();

    public static List<ItemBuilder> convertConfigurationSection(@Nullable final CommentedConfigurationNode section) {
        final List<ItemBuilder> cache = new ArrayList<>();

        if (section == null) return cache;

        for (final Map.Entry<Object, CommentedConfigurationNode> key : section.childrenMap().entrySet()) {
            final CommentedConfigurationNode item = key.getValue();

            final ItemBuilder itemBuilder = new ItemBuilder(item.node("material").getString("stone"));

            itemBuilder.withBase64(item.node("data").getString(""));

            itemBuilder.withDisplayName(item.node("name").getString(""));

            itemBuilder.withDisplayLore(StringUtils.getStringList(item.node("lore")));

            itemBuilder.setAmount(item.node("amount").getInt(1));

            item.node("enchantments").childrenMap().forEach((id, node) -> {
                final int level = node.node(id).getInt(1);

                itemBuilder.addEnchantment(id.toString(), level);
            });

            final CustomBuilder customBuilder = itemBuilder.asCustomBuilder();

            customBuilder.setCustomModelData(item.node("custom-model-data").getString(""));

            if (item.node("hide-tool-tip").getBoolean(false)) {
                itemBuilder.hideToolTip();
            }

            if (item.hasChild("hide-tooltip-advanced") && item.node("hide-tooltip-advanced").isList()) {
                itemBuilder.hideComponents(StringUtils.getStringList(item.node("hide-tooltip-advanced")));
            }

            if (item.hasChild("item-model")) {
                customBuilder.setItemModel(item.node("item-model", "namespace").getString(""), item.node("item-model", "key").getString(""));
            }

            itemBuilder.setUnbreakable(item.node("unbreakable-item").getBoolean(false));

            // settings
            switch (item.node("settings", "glowing").getString("none").toLowerCase()) {
                case "add_glow", "true" -> itemBuilder.addEnchantGlint();
                case "remove_glow", "false" -> itemBuilder.removeEnchantGlint();
                case "none" -> {}
            }

            final String player = item.node("settings", "player").getString(null);

            if (player != null && !player.isEmpty()) {
                final SkullBuilder skullBuilder = itemBuilder.asSkullBuilder();

                skullBuilder.withName(player).build();
            }

            itemBuilder.setItemDamage(item.node("settings", "damage").getInt(0));

            itemBuilder.withSkull(item.node("settings", "skull").getString(""));

            final String rgb = item.node("settings", "rgb").getString("");

            final String color = item.node("settings", "color").getString("");

            itemBuilder.setColor(!color.isEmpty() ? color : !rgb.isEmpty() ? rgb : "");

            itemBuilder.setTrim(item.node("settings", "trim", "pattern").getString(""), item.node("settings", "trim", "material").getString(""));

            if (item.hasChild("settings", "potions")) {
                final PotionBuilder potionBuilder = itemBuilder.asPotionBuilder();

                final CommentedConfigurationNode potions = item.node("settings", "potions");

                potions.childrenMap().keySet().stream().map(Object::toString).forEach(id -> {
                    com.ryderbelserion.fusion.paper.utils.ItemUtils.getPotionEffect(id).ifPresent(type -> {
                        final CommentedConfigurationNode potion = potions.node(id);

                        final int duration = potion.node("duration").getInt(10) * 20;
                        final int level = potion.node("level").getInt(1);

                        final boolean hasIcon = potion.node("style", "icon").getBoolean(false);
                        final boolean isAmbient = potion.node("style", "ambient").getBoolean(false);
                        final boolean hasParticles = potion.node("style", "particles").getBoolean(false);

                        potionBuilder.withPotionEffect(type, duration, level, isAmbient, hasParticles, hasIcon);
                    });
                });

                potionBuilder.build();
            }

            if (item.hasChild("settings", "patterns")) {
                for (final Map.Entry<Object, CommentedConfigurationNode> entry : item.node("settings", "patterns").childrenMap().entrySet()) {
                    final CommentedConfigurationNode config = entry.getValue();
                }

                final CommentedConfigurationNode patterns = item.node("settings", "patterns");

                final PatternBuilder patternBuilder = itemBuilder.asPatternBuilder();

                item.node("settings", "patterns").childrenMap().keySet().stream().map(Object::toString).forEach(id -> {
                    final CommentedConfigurationNode pattern = patterns.node(id);

                    patternBuilder.addPattern(id, pattern.getString("white"));
                });

                patternBuilder.build();
            }

            customBuilder.build();

            cache.add(itemBuilder);
        }

        return cache;
    }

    /**
     * Converts a string to an ItemBuilder with a placeholder for errors.
     *
     * @param itemString the string you wish to convert.
     * @param placeholder the placeholder to use if there is an error.
     * @return the string as an ItemBuilder.
     */
    public static ItemBuilder convertString(@NonNull final String itemString, @NonNull final String placeholder) {
        ItemBuilder itemBuilder = new ItemBuilder(ItemType.STONE);

        try {
            for (String optionString : itemString.split(", ")) {
                String option = optionString.split(":")[0];
                String value = optionString.replace(option + ":", "").replace(option, "");

                switch (option.toLowerCase()) {
                    case "item" -> itemBuilder.withCustomItem(value.toLowerCase());
                    case "name" -> itemBuilder.withDisplayName(value);
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
                            itemBuilder.asSkullBuilder().withName(value).build();
                        } catch (final Exception exception) {
                            fusion.log(Level.warn, "Could create skull builder because the item is not a player head. You can ignore this, This is a restriction of the current system.");
                        }
                    }
                    case "skull" -> itemBuilder.withSkull(value);
                    case "unbreakable-item" -> itemBuilder.setUnbreakable(StringUtils.tryParseBoolean(value).orElse(false));
                    case "custom-model-data" -> itemBuilder.asCustomBuilder().setCustomModelData(value);
                    case "hide-tool-tip" -> itemBuilder.hideToolTip();
                    case "trim" -> {
                        String[] split = value.split("!"); // trim:trim_pattern!trim_material

                        String trim = split[0];
                        String material = split[1];

                        itemBuilder.setTrim(trim.toLowerCase(), material.toLowerCase());
                    }

                    case "glowing" -> {
                        final boolean isGlowing = StringUtils.tryParseBoolean(value).orElse(false);

                        if (isGlowing) {
                            itemBuilder.addEnchantGlint();
                        } else {
                            itemBuilder.removeEnchantGlint();
                        }
                    }

                    default -> {
                        itemBuilder.addEnchantment(getEnchant(option), StringUtils.tryParseInt(value).map(Number::intValue).orElse(1));

                        if (itemBuilder.isBanner() || itemBuilder.isShield()) {
                            final PatternBuilder builder = itemBuilder.asPatternBuilder();

                            builder.addPattern(value, option).build();
                        }
                    }
                }
            }
        } catch (final Exception exception) {
            itemBuilder.withType(ItemType.RED_TERRACOTTA).withDisplayName("<red>ERROR").withDisplayLore(Arrays.asList("<red>There is an error", "<red>For : <red>" + placeholder));

            exception.printStackTrace();
        }

        return itemBuilder;
    }

    /**
     * Converts a list of Strings to a list of ItemBuilders.
     *
     * @param items the list of Strings.
     * @return the list of ItemBuilders.
     */
    public static List<ItemBuilder> convertStringList(@NonNull final List<String> items) {
        return convertStringList(items, "");
    }

    /**
     * Converts a list of Strings to a list of ItemBuilders with a placeholder for errors.
     *
     * @param items the list of strings.
     * @param placeholder the placeholder for errors.
     * @return the list of ItemBuilders.
     */
    public static List<ItemBuilder> convertStringList(@NonNull final List<String> items, @NonNull final String placeholder) {
        return items.stream().map(item -> convertString(item, placeholder)).collect(Collectors.toList());
    }

    public static String getEnchant(@NonNull final String enchant) {
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