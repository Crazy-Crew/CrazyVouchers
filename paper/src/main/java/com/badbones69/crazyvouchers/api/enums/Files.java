package com.badbones69.crazyvouchers.api.enums;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.vital.paper.api.files.FileManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public enum Files {


    voucher_codes("voucher-codes.yml"),
    vouchers("vouchers.yml"),
    users("users.yml"),
    data("data.yml");

    private final String fileName;
    private final String strippedName;

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NotNull final FileManager fileManager = this.plugin.getFileManager();

    /**
     * A constructor to build a file
     *
     * @param fileName the name of the file
     */
    Files(final String fileName) {
        this.fileName = fileName;
        this.strippedName = this.fileName.replace(".yml", "");
    }

    public final YamlConfiguration getConfiguration() {
        return this.fileManager.getFile(this.fileName).getConfiguration();
    }

    public final String getStrippedName() {
        return this.strippedName;
    }

    public final String getFileName() {
        return this.fileName;
    }

    public void save() {
        this.fileManager.saveFile(this.fileName);
    }

    public void reload() {
        this.fileManager.addFile(this.fileName);
    }
}