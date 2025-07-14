package com.badbones69.crazyvouchers.commands;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.files.FileManager;
import dev.triumphteam.cmd.core.annotations.Command;
import java.nio.file.Path;

@Command(value = "crazyvouchers", alias = {"vouchers", "voucher"})
public abstract class BaseCommand {

    protected final CrazyVouchers plugin = CrazyVouchers.get();

    protected final Path dataPath = this.plugin.getDataPath();

    protected final FileManager fileManager = this.plugin.getFileManager();

    protected final CrazyManager crazyManager = this.plugin.getCrazyManager();

    protected final InventoryManager inventoryManager = this.plugin.getInventoryManager();

    protected final FusionPaper fusion = this.plugin.getFusion();

    protected final SettingsManager config = ConfigManager.getConfig();

}