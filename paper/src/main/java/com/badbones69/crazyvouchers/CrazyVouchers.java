package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.CrazyHandler;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.commands.CommandManager;
import com.badbones69.crazyvouchers.listeners.v2.VoucherClickListener;
import com.badbones69.crazyvouchers.listeners.v2.VoucherRedeemListener;
import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import com.ryderbelserion.vital.VitalPaper;
import com.ryderbelserion.vital.files.FileManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CrazyVouchers extends JavaPlugin {

    private CrazyHandler crazyHandler;
    private FileManager fileManager;

    @Override
    public void onEnable() {
        new VitalPaper(this);

        ConfigManager.load(getDataFolder());

        this.fileManager = new FileManager();
        this.fileManager
                .addDefaultFile("vouchers", "Example.yml")
                .addDefaultFile("vouchers", "Example-Arg.yml")
                .addDefaultFile("vouchers", "PlayerHead.yml")
                .addDefaultFile("codes", "Starter-Money.yml")
                .addStaticFile("users.yml")
                .addFolder("vouchers")
                .addFolder("codes").create();

        this.crazyHandler = new CrazyHandler();
        this.crazyHandler.load();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new VoucherClickListener(), this);
        pluginManager.registerEvents(new VoucherRedeemListener(), this);

        FileConfiguration configuration = Files.users.getFile();

        if (!configuration.contains("Players")) {
            configuration.set("Players.Clear", null);

            Files.users.save();
        }

        getServer().getPluginManager().registerEvents(new VoucherClickListener(), this);
        getServer().getPluginManager().registerEvents(new VoucherCraftListener(), this);
        getServer().getPluginManager().registerEvents(new VoucherMenuListener(), this);
        getServer().getPluginManager().registerEvents(new FireworkDamageListener(), this);

        CommandManager.load();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public FileManager getFileManager() {
        return this.fileManager;
    }

    public CrazyHandler getCrazyHandler() {
        return this.crazyHandler;
    }

    public CrazyManager getCrazyManager() {
        return this.crazyManager;
    }
}