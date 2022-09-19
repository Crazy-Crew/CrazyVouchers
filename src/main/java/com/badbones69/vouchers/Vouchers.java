package com.badbones69.vouchers;

import com.badbones69.vouchers.api.enums.Messages;
import com.badbones69.vouchers.controllers.GUI;
import com.badbones69.vouchers.api.FileManager;
import com.badbones69.vouchers.api.FileManager.Files;
import com.badbones69.vouchers.api.CrazyManager;
import com.badbones69.vouchers.commands.VoucherCommands;
import com.badbones69.vouchers.commands.VoucherTab;
import com.badbones69.vouchers.controllers.FireworkDamageAPI;
import com.badbones69.vouchers.controllers.VoucherClick;
import org.bstats.bukkit.Metrics;
import org.bukkit.block.data.type.Fire;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Vouchers extends JavaPlugin implements Listener {

    private static Vouchers plugin;

    private FileManager fileManager;

    private CrazyManager crazyManager;

    private Methods methods;

    private FireworkDamageAPI fireworkDamageAPI;

    private GUI gui;

    @Override
    public void onEnable() {
        plugin = this;

        fileManager = new FileManager();

        crazyManager = new CrazyManager();

        methods = new Methods();

        fileManager.logInfo(true).setup();

        if (!Files.DATA.getFile().contains("Players")) {
            Files.DATA.getFile().set("Players.Clear", null);
            Files.DATA.saveFile();
        }

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new VoucherClick(), this);
        pluginManager.registerEvents(gui = new GUI(), this);
        pluginManager.registerEvents(fireworkDamageAPI = new FireworkDamageAPI(), this);

        getCommand("vouchers").setExecutor(new VoucherCommands());
        getCommand("vouchers").setTabCompleter(new VoucherTab());

        Messages.addMissingMessages();

        boolean metricsEnabled = Files.CONFIG.getFile().getBoolean("Settings.Toggle-Metrics");

        if (Files.CONFIG.getFile().getString("Settings.Toggle-Metrics") != null) {
            if (metricsEnabled) new Metrics(this, 4536);
        } else {
            getLogger().warning("Metrics was automatically enabled.");
            getLogger().warning("Please add Toggle-Metrics: false to the top of your Config.yml");
            getLogger().warning("https://github.com/Crazy-Crew/Vouchers/blob/main/Config.yml");

            getLogger().warning("An example if confused is linked above.");

            new Metrics(this, 4536);
        }

        crazyManager.load();
    }

    public static Vouchers getPlugin() {
        return plugin;
    }

    public CrazyManager getCrazyManager() {
        return crazyManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public Methods getMethods() {
        return methods;
    }

    public FireworkDamageAPI getFireworkDamageAPI() {
        return fireworkDamageAPI;
    }

    public GUI getGui() {
        return gui;
    }
}