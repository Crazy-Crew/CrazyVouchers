package com.badbones69.crazyvouchers.api.enums;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.vital.files.FileManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public enum Files {

    users("users.yml");

    private final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private final @NotNull FileManager fileManager = this.plugin.getFileManager();

    private final FileConfiguration config;
    private final String fileName;

    Files(String fileName) {
        this.config = this.fileManager.getStaticFile(fileName);

        this.fileName = fileName;
    }

    public FileConfiguration getFile() {
        return this.config;
    }

    public void save() {
        this.fileManager.saveStaticFile(this.fileName);
    }

    public void reload() {
        this.fileManager.reloadStaticFile(this.fileName);
    }
}