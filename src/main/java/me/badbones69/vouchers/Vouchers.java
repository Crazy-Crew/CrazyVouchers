package me.badbones69.vouchers;

import me.badbones69.vouchers.api.FileManager;
import me.badbones69.vouchers.api.FileManager.Files;
import me.badbones69.vouchers.api.VouchersManager;
import me.badbones69.vouchers.api.enums.Version;
import me.badbones69.vouchers.commands.VoucherCommands;
import me.badbones69.vouchers.commands.VoucherTab;
import me.badbones69.vouchers.controllers.FireworkDamageAPI;
import me.badbones69.vouchers.controllers.GUI;
import me.badbones69.vouchers.controllers.Metrics;
import me.badbones69.vouchers.controllers.VoucherClick;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Vouchers extends JavaPlugin implements Listener {
    
    private final FileManager fileManager = FileManager.getInstance();
    
    @Override
    public void onEnable() {
        fileManager.logInfo(true).setup(this);

        if (!Files.DATA.getFile().contains("Players")) {
            Files.DATA.getFile().set("Players.Clear", null);
            Files.DATA.saveFile();
        }

        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new VoucherClick(), this);
        pm.registerEvents(new GUI(), this);

        getCommand("vouchers").setExecutor(new VoucherCommands());
        getCommand("vouchers").setTabCompleter(new VoucherTab());

        try {
            if (Version.isNewer(Version.v1_10_R1)) {
                pm.registerEvents(new FireworkDamageAPI(this), this);
            }
        } catch (Exception ignored) { }
        new Metrics(this);

        VouchersManager.load();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        if (player.isOp()) {
            if (Files.CONFIG.getFile().contains("Settings.Updater")) {
                if (Files.CONFIG.getFile().getBoolean("Settings.Updater")) {
                    Methods.hasUpdate(player);
                }
            } else {
                Methods.hasUpdate(player);
            }
        }
    }
}