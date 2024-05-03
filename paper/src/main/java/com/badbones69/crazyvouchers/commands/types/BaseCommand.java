package com.badbones69.crazyvouchers.commands.types;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyHandler;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import com.ryderbelserion.vital.files.FileManager;
import dev.triumphteam.cmd.core.annotations.Command;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Command(value = "crazyvouchers")
public abstract class BaseCommand {

    protected final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    protected final @NotNull FileManager fileManager = this.plugin.getFileManager();

    protected final @NotNull CrazyHandler crazyHandler = this.plugin.getCrazyHandler();

    protected final @NotNull CrazyManager crazyManager = this.plugin.getCrazyManager();

    protected final @NotNull SettingsManager config = ConfigManager.getConfig();

}