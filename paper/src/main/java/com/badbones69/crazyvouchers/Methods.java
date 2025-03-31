package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.enums.FileKeys;
import com.badbones69.crazyvouchers.api.enums.misc.PersistentKeys;
import com.badbones69.crazyvouchers.utils.ScheduleUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.api.enums.Support;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Methods {

    public static void removeItem(final ItemStack item, final Player player) {
        if (item.getAmount() <= 1) {
            player.getInventory().removeItem(item);
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

    public static int getRandom(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }

    public static boolean hasPermission(final boolean execute, final Player player, final List<String> permissions, final List<String> commands, final Map<String, String> placeholders, final String message, final String argument) {
        boolean hasPermission = false;

        final Server server = player.getServer();

        for (String permission : permissions) {
            if (player.hasPermission(permission.toLowerCase().replace("{arg}", argument != null ? argument : "{arg}"))) {
                if (execute) {
                    placeholders.put("{permission}", permission);

                    player.sendMessage(fusion.color(player, message, placeholders));

                    ScheduleUtils.dispatch(consumer -> {
                        for (final String command : commands) {
                            server.dispatchCommand(server.getConsoleSender(), placeholders(player, command, placeholders));
                        }
                    });
                }

                hasPermission = true;

                break;
            }
        }

        return hasPermission;
    }

    public static void addItem(final Player player, final ItemStack... items) {
        final PlayerInventory inventory = player.getInventory();

        inventory.setMaxStackSize(64);

        final List<ItemStack> itemStacks = Arrays.asList(items);

        itemStacks.forEach(item -> {
            if (isInventoryFull(inventory)) {
                player.getWorld().dropItem(player.getLocation(), item);
            } else {
                inventory.setItem(inventory.firstEmpty(), item);
            }
        });
    }

    public static String placeholders(final CommandSender sender, final String message, final Map<String, String> placeholders) {
        String line = message;

        if (sender instanceof Player player && Support.placeholder_api.isEnabled()) {
            line = PlaceholderAPI.setPlaceholders(player, line);
        }

        for (final String placeholder : placeholders.keySet()) {
            line = line.replace(placeholder, placeholders.get(placeholder)).replace(placeholder.toLowerCase(), placeholders.get(placeholder));
        }

        return line;
    }

    private static final CrazyVouchers plugin = CrazyVouchers.get();

    private static final FusionPaper fusion = plugin.getFusion();

    public static Component color(final String message, final Map<String, String> placeholders) {
        return fusion.color(message, placeholders);
    }

    public static boolean isInventoryFull(final PlayerInventory inventory) {
        return inventory.firstEmpty() == -1;
    }

    public static void firework(final Location location, final List<Color> list) {
        if (location.getWorld() == null) return;

        Firework firework = location.getWorld().spawn(location, Firework.class);

        FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffects(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(list).trail(false).flicker(false).build());
        meta.setPower(0);

        firework.setFireworkMeta(meta);

        PersistentDataContainer container = firework.getPersistentDataContainer();

        container.set(PersistentKeys.no_firework_damage.getNamespacedKey(), PersistentDataType.BOOLEAN, true);

        new FoliaScheduler(firework.getLocation()) {
            @Override
            public void run() {
                firework.detonate();
            }
        }.runDelayed(2);
    }
}