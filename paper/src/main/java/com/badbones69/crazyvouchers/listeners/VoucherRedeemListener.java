package com.badbones69.crazyvouchers.listeners;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyHandler;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemCodeEvent;
import com.badbones69.crazyvouchers.api.events.VoucherRedeemEvent;
import com.badbones69.crazyvouchers.api.objects.VoucherCommand;
import com.badbones69.crazyvouchers.api.objects.v2.GenericVoucher;
import com.badbones69.crazyvouchers.platform.util.MiscUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class VoucherRedeemListener implements Listener {

    private final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final @NotNull CrazyHandler crazyHandler = this.plugin.getCrazyHandler();

    private final Map<UUID, String> twoAuth = new HashMap<>();

    private final @NotNull Server server = this.plugin.getServer();

    @EventHandler(ignoreCancelled = true)
    public void onVoucherRedeem(VoucherRedeemEvent event) {
        // Get the voucher.
        GenericVoucher voucher = event.getVoucher();

        // Get the player.
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Get the argument.
        String argument = event.getArgument();

        // Decides whether to cancel the event or not.
        event.setCancelled(voucher.execute(player, argument));

        String fileName = voucher.getFileName();

        // Check if edible and if two-step enabled.
        if (!voucher.isEdible() && voucher.twoStep()) {
            if (this.twoAuth.containsKey(uuid)) {
                if (!this.twoAuth.get(uuid).equalsIgnoreCase(fileName)) {
                    Messages.two_step_authentication.sendMessage(player);
                    this.twoAuth.put(uuid, fileName);

                    event.setCancelled(true);

                    return;
                }
            } else {
                Messages.two_step_authentication.sendMessage(player);

                this.twoAuth.put(uuid, fileName);

                event.setCancelled(true);

                return;
            }
        }

        // Remove from two auth
        this.twoAuth.remove(uuid);

        // Remove the item from inventory.
        MiscUtil.removeItem(event.getItemStack(), player);

        // Get location
        Location location = player.getLocation();

        // Get the placeholders map
        Map<String, String> placeholders = voucher.getPlaceholders();

        List<VoucherCommand> randomCommands = voucher.getRandomCommands();
        randomCommands.get(getRandom(randomCommands.size())).getCommands().forEach(command -> this.server.dispatchCommand(this.server.getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyHandler.replaceRandom(command))));

        List<VoucherCommand> chanceCommands = voucher.getChanceCommands();
        chanceCommands.get(getRandom(chanceCommands.size())).getCommands().forEach(command -> this.server.dispatchCommand(this.server.getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyHandler.replaceRandom(command))));

        voucher.getCommands().forEach(command -> this.server.dispatchCommand(this.server.getConsoleSender(), MiscUtil.replacePlaceholders(placeholders, this.crazyHandler.replaceRandom(voucher.replacePlaceholders(command, player)))));

        PlayerInventory inventory = player.getInventory();

        voucher.getBuilders().forEach(builder -> {
            ItemStack itemStack = builder.setTarget(player).build();

            if (MiscUtil.isInventoryFull(player)) {
                inventory.setItem(inventory.firstEmpty(), itemStack);
            } else {
                player.getWorld().dropItem(location, itemStack);
            }
        });

        float volume = voucher.getVolume();
        float pitch = voucher.getPitch();

        if (voucher.isSoundToggle()) {
            voucher.getSounds().forEach(sound -> player.playSound(location, sound, SoundCategory.PLAYERS, volume, pitch));
        }

        if (voucher.isFireworkToggle()) MiscUtil.firework(player, location, voucher.getFireworkColors());

        String msg = voucher.getMessage();

        if (!msg.isEmpty()) {
            player.sendRichMessage(MiscUtil.replacePlaceholders(placeholders, msg));
        }

        if (voucher.isLimiterToggle()) {
            FileConfiguration users = Files.users.getFile();

            users.set("Players." + uuid + ".UserName", player.getName());
            users.set("Players." + uuid + ".Vouchers." + fileName, users.getInt("Players." + uuid + ".Vouchers." + fileName));

            Files.users.save();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVoucherCodeRedeem(VoucherRedeemCodeEvent event) {

    }

    private int getRandom(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }
}