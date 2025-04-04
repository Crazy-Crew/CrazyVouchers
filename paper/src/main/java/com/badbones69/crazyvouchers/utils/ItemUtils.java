package com.badbones69.crazyvouchers.utils;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.api.utils.StringUtils;
import com.ryderbelserion.fusion.paper.api.builder.items.modern.ItemBuilder;
import com.ryderbelserion.fusion.paper.api.builder.items.modern.types.PatternBuilder;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
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
                    case "item" -> itemBuilder.withType(value.toLowerCase());
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

                        itemBuilder.setTrim(trim.toLowerCase(), material.toLowerCase(), false);
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
        } catch (Exception exception) {
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