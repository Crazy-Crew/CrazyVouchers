package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.badbones69.crazyvouchers.utils.ScheduleUtils;
import com.ryderbelserion.fusion.core.api.utils.StringUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Methods {

    private static @NotNull final Pattern randomNumberMatcher = Pattern.compile("\\{random}:(\\d+)-(\\d+)");

    private static @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private static @NotNull final FusionPaper fusion = plugin.getFusion();

    public static void removeItem(@NotNull final ItemStack item, @NotNull final Player player) {
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null); // it's always the main hand, we don't allow off-hand usage
        } else if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    public static void janitor() {
        final FileConfiguration configuration = FileKeys.users.getConfiguration();

        if (!configuration.contains("Players")) {
            configuration.set("Players.Clear", null);

            FileKeys.users.save();
        }

        final FileConfiguration data = FileKeys.data.getConfiguration();

        if (!data.contains("Used-Vouchers")) {
            data.set("Used-Vouchers.Clear", null);

            FileKeys.data.save();
        }
    }

    public static boolean useDifferentRandom() {
        return ConfigManager.getConfig().getProperty(ConfigKeys.use_different_random);
    }

    public static Random getRandom() {
        return useDifferentRandom() ? ThreadLocalRandom.current() : new Random();
    }

    public static int getRandom(final int max) {
        return getRandom().nextInt(max);
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public static boolean hasPermission(final boolean execute, @NotNull final Player player, @NotNull final List<String> permissions, @NotNull final List<String> commands, @NotNull final Map<String, String> placeholders, @NotNull final String message, @NotNull final String argument) {
        boolean hasPermission = false;

        for (final String permission : permissions) {
            final String arg = argument.isEmpty() ? "{arg}" : argument;
            final String cleanPermission = permission.toLowerCase().replace("{arg}", arg);

            if (player.hasPermission(cleanPermission)) {
                if (execute) {
                    placeholders.put("{permission}", permission);

                    dispatch(player, List.of(message), placeholders, false);
                    dispatch(player, commands, placeholders, true);
                }

                hasPermission = true;

                break;
            }
        }

        return hasPermission;
    }

    public static void addItem(@NotNull final Player player, @NotNull final ItemStack... items) {
        final PlayerInventory inventory = player.getInventory();

        Arrays.asList(items).forEach(item -> {
            if (isInventoryFull(inventory)) {
                player.getWorld().dropItem(player.getLocation(), item.clone());
            } else {
                inventory.addItem(item.clone());
            }
        });
    }

    public static String placeholders(@NotNull final CommandSender sender, @NotNull final String message, @NotNull final Map<String, String> placeholders) {
        String safeLine = message;

        for (final String placeholder : placeholders.keySet()) {
            safeLine = safeLine.replace(placeholder, placeholders.get(placeholder)).replace(placeholder.toLowerCase(), placeholders.get(placeholder));
        }

        return fusion.parsePlaceholders(sender, safeLine);
    }

    public static void dispatch(@NotNull final Player player, @NotNull final List<String> values, @NotNull final Map<String, String> placeholders, final boolean isCommand) {
        if (values.isEmpty()) return;

        final Server server = player.getServer();
        final CommandSender sender = server.getConsoleSender();

        if (isCommand) {
            ScheduleUtils.dispatch(consumer -> {
                for (final String value : values) {
                    server.dispatchCommand(sender, placeholders(player, getRandomNumber(value), placeholders));
                }
            });

            return;
        }

        for (final String value : values) {
            player.sendMessage(fusion.color(player, value, placeholders));
        }
    }

    public static String getRandomNumber(@NotNull final String value) {
        String safeLine = value;

        if (safeLine.contains("{random}")) {
            final Matcher matcher = randomNumberMatcher.matcher(safeLine);

            final Optional<Number> minRange = StringUtils.tryParseInt(matcher.group(1));
            final Optional<Number> maxRange = StringUtils.tryParseInt(matcher.group(2));

            if (minRange.isPresent() && maxRange.isPresent()) {
                final int minimum = minRange.get().intValue();
                final int maximum = maxRange.get().intValue();

                final int amount = Methods.getRandom().nextInt(minimum, maximum);

                safeLine = safeLine.replace("{random}:%s-%s".formatted(minimum, maximum), String.valueOf(amount));
            } else {
                fusion.log("warn", "The values supplied with {random} seem to not be integers. {}", value);
            }
        }

        return safeLine;
    }

    public static boolean isInventoryFull(@NotNull final PlayerInventory inventory) {
        return inventory.firstEmpty() == -1;
    }

    public static void firework(@NotNull final Location location, @NotNull final List<Color> list) {
        if (location.getWorld() == null) return;

        final Firework firework = location.getWorld().spawn(location, Firework.class);

        final FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffects(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(list).trail(false).flicker(false).build());
        meta.setPower(0);

        firework.setFireworkMeta(meta);

        final PersistentDataContainer container = firework.getPersistentDataContainer();

        container.set(PersistentKeys.no_firework_damage.getNamespacedKey(), PersistentDataType.BOOLEAN, true);

        new FoliaScheduler(plugin, null, firework) {
            public void run() {
                firework.detonate();
            }
        }.runDelayed(2);
    }
}