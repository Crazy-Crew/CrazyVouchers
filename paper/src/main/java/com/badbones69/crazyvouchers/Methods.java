package com.badbones69.crazyvouchers;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.PersistentKeys;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.badbones69.crazyvouchers.utils.MsgUtils;
import com.ryderbelserion.fusion.paper.enums.Scheduler;
import com.ryderbelserion.fusion.paper.util.scheduler.FoliaScheduler;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Methods {

    private @NotNull static final SettingsManager config = ConfigManager.getConfig();

    public static void removeItem(final ItemStack item, final Player player) {
        if (item.getAmount() <= 1) {
            player.getInventory().removeItem(item);
        } else if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    public static void janitor() {
        final FileConfiguration configuration = Files.users.getConfiguration();

        if (!configuration.contains("Players")) {
            configuration.set("Players.Clear", null);

            Files.users.save();
        }

        final FileConfiguration data = Files.data.getConfiguration();

        if (!data.contains("Used-Vouchers")) {
            data.set("Used-Vouchers.Clear", null);

            Files.data.save();
        }
    }

    public static boolean hasPermission(final boolean execute, final Player player, final List<String> permissions, final List<String> commands, final Map<String, String> placeholders, final String message, final String argument) {
        boolean hasPermission = false;

        final Server server = player.getServer();

        for (String permission : permissions) {
            if (player.hasPermission(permission.toLowerCase().replace("{arg}", argument != null ? argument : "{arg}"))) {
                if (execute) {
                    placeholders.put("{permission}", permission);

                    player.sendMessage(Methods.replacePlaceholders(placeholders, message, false));

                    new FoliaScheduler(Scheduler.global_scheduler) {
                        @Override
                        public void run() {
                            for (String command : commands) {
                                server.dispatchCommand(server.getConsoleSender(), Methods.replacePlaceholders(placeholders, command, true));
                            }
                        }
                    }.run();
                }

                hasPermission = true;

                break;
            }
        }

        return hasPermission;
    }

    public static String getPrefix(final String message) {
        return MsgUtils.color(config.getProperty(ConfigKeys.command_prefix) + message);
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

    public static String replacePlaceholders(final Map<String, String> placeholders, String message, final boolean isCommand) {
        for (String placeholder : placeholders.keySet()) {
            message = message.replace(placeholder, placeholders.get(placeholder)).replace(placeholder.toLowerCase(), placeholders.get(placeholder));
        }

        if (isCommand) return message; else return MsgUtils.color(message);
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