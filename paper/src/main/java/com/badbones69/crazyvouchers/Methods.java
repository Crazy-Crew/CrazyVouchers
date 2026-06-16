package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.api.objects.VoucherCommand;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.badbones69.crazyvouchers.utils.ScheduleUtils;
import com.ryderbelserion.fusion.core.api.enums.Level;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.builders.folia.FoliaScheduler;
import org.bukkit.*;
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

    private static @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private static @NotNull final FusionPaper fusion = plugin.getFusion();

    public static VoucherCommand getCommand(@NotNull final List<VoucherCommand> origin, final double totalWeight) {
        int index = 0;

        final List<VoucherCommand> commands = origin.stream().filter(command -> command.getWeight() > 0.0).toList();

        for (double value = Methods.getRandom().nextDouble() * totalWeight; index < commands.size() - 1; index++) {
            final double weight = commands.get(index).getWeight();

            value -= weight;

            if (value <= 0.0) break;
        }

        return commands.get(index);
    }

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

        final World world = player.getWorld();
        final Location location = player.getLocation();

        Arrays.asList(items).forEach(item -> {
            if (isInventoryFull(inventory)) {
                new FoliaScheduler(plugin, location) {
                    public void run() {
                        world.dropItem(location, item.clone());
                    }
                }.runNow();
            } else {
                inventory.addItem(item.clone());
            }
        });
    }

    public static String placeholders(@NotNull final CommandSender sender, @NotNull final String message, @NotNull final Map<String, String> placeholders) {
        placeholders.putAll(getRandomNumber(message));

        return fusion.parse(sender, message, placeholders);
    }

    public static void dispatch(@NotNull final Player player, @NotNull final List<String> values, @NotNull final Map<String, String> placeholders, final boolean isCommand) {
        if (values.isEmpty()) return;

        final Server server = player.getServer();
        final CommandSender sender = server.getConsoleSender();

        if (isCommand) {
            ScheduleUtils.dispatch(_ -> {
                for (final String value : values) {
                    if (value.isEmpty()) continue;

                    server.dispatchCommand(sender, placeholders(player, value, placeholders));
                }
            });

            return;
        }

        for (final String value : values) {
            player.sendMessage(fusion.parse(player, value, placeholders));
        }
    }

    public static Map<String, String> getRandomNumber(@NotNull final String value) {
        final Pattern text = Pattern.compile("\\{random}:(\\d+)-(\\d+)");
        final Matcher matcher = text.matcher(value);

        final Map<String, String> placeholders = new HashMap<>();

        while (matcher.find()) {
            final int minimum = StringUtils.tryParseInt(matcher.group(1)).orElse(1).intValue();
            final int maximum = StringUtils.tryParseInt(matcher.group(2)).orElse(10).intValue();

            if (minimum <= 0 || maximum <= 0) {
                fusion.log(Level.ERROR, "The minimum/maximum value cannot be less than or equal to 0. (Min: %s, Max: %s)", minimum, maximum);

                continue;
            }

            if (minimum > maximum) {
                fusion.log(Level.ERROR, "The minimum value cannot be larger than the maximum value! (Min: %s, Max: %s)", minimum, maximum);

                continue;
            }

            try {
                placeholders.putIfAbsent(matcher.group(), String.valueOf(getRandom().nextInt(minimum, maximum)));
            } catch (final Exception exception) {
                return placeholders;
            }
        }

        return placeholders;
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