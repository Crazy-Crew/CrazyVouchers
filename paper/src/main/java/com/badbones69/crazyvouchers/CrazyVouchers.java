package com.badbones69.crazyvouchers;

import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.Files;
import com.badbones69.crazyvouchers.listeners.FireworkDamageListener;
import com.badbones69.crazyvouchers.listeners.VoucherMenuListener;
import com.badbones69.crazyvouchers.listeners.VoucherClickListener;
import com.badbones69.crazyvouchers.listeners.VoucherCraftListener;
import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import com.ryderbelserion.vital.VitalPaper;
import com.ryderbelserion.vital.files.FileManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public class CrazyVouchers extends JavaPlugin {

    private CrazyManager crazyManager;
    private FileManager fileManager;

    @Override
    public void onEnable() {
        String version = getServer().getMinecraftVersion();

        if (!version.equals("1.20.6")) {
            List.of(
                    "You are not running 1.20.6, Please download Paper or Purpur 1.20.6",
                    "Paper Downloads: https://papermc.io/downloads/paper",
                    "Purpur Downloads: https://purpurmc.org/downloads",
                    "",
                    "We only support 1.20.6, If you need older versions. You can downgrade versions of the plugin.",
                    "All our older versions can be found in the versions tab on Modrinth",
                    "The older versions do not get updates or fixes."
            ).forEach(getLogger()::severe);

            getServer().getPluginManager().disablePlugin(this);

            return;
        }

        ConfigManager.load(getDataFolder());

        new VitalPaper(this);

        this.fileManager = new FileManager();
        this.fileManager
                .addDefaultFile("vouchers", "Example.yml")
                .addDefaultFile("vouchers", "Example-Arg.yml")
                .addDefaultFile("vouchers", "PlayerHead.yml")
                .addDefaultFile("codes", "Starter-Money.yml")
                .addStaticFile("users.yml")
                .addFolder("vouchers")
                .addFolder("codes").create();

        this.crazyManager = new CrazyManager();
        this.crazyManager.load();

        FileConfiguration configuration = Files.users.getFile();

        if (!configuration.contains("Players")) {
            configuration.set("Players.Clear", null);

            Files.users.save();
        }

        getServer().getPluginManager().registerEvents(new VoucherClickListener(), this);
        getServer().getPluginManager().registerEvents(new VoucherCraftListener(), this);
        getServer().getPluginManager().registerEvents(new VoucherMenuListener(), this);
        getServer().getPluginManager().registerEvents(new FireworkDamageListener(), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public FileManager getFileManager() {
        return this.fileManager;
    }

    public CrazyManager getCrazyManager() {
        return this.crazyManager;
    }
}