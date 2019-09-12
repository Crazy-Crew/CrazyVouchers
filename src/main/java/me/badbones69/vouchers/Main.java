package me.badbones69.vouchers;

import me.badbones69.vouchers.api.FileManager;
import me.badbones69.vouchers.api.FileManager.Files;
import me.badbones69.vouchers.api.Vouchers;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener {
	
	private FileManager fileManager = FileManager.getInstance();
	
	@Override
	public void onEnable() {
		fileManager.logInfo(true).setup(this);
		if(!Files.DATA.getFile().contains("Players")) {
			Files.DATA.getFile().set("Players.Clear", null);
			Files.DATA.saveFile();
		}
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(new VoucherClick(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new GUI(), this);
		getCommand("vouchers").setExecutor(new VoucherCommands());
		getCommand("vouchers").setTabCompleter(new VoucherTab());
		try {
			if(Version.getCurrentVersion().isNewer(Version.v1_10_R1)) {
				Bukkit.getServer().getPluginManager().registerEvents(new FireworkDamageAPI(this), this);
			}
		}catch(Exception e) {
		}
		new Metrics(this);
		Vouchers.load();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		final Player player = e.getPlayer();
		new BukkitRunnable() {
			@Override
			public void run() {
				if(player.getName().equals("BadBones69")) {
					player.sendMessage(Methods.color("&8[&bVouchers&8]: " + "&7This server is running your Vouchers Plugin. " + "&7It is running version &av" + Bukkit.getServer().getPluginManager().getPlugin("Vouchers").getDescription().getVersion() + "&7."));
				}
				if(player.isOp()) {
					if(Files.CONFIG.getFile().contains("Settings.Updater")) {
						if(Files.CONFIG.getFile().getBoolean("Settings.Updater")) {
							Methods.hasUpdate(player);
						}
					}else {
						Methods.hasUpdate(player);
					}
				}
			}
		}.runTaskLaterAsynchronously(this, 20);
	}
	
}