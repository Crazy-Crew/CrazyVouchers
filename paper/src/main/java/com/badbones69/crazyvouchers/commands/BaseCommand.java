package com.badbones69.crazyvouchers.commands;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.badbones69.crazyvouchers.config.ConfigManager;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.files.PaperFileManager;
import dev.triumphteam.cmd.core.annotations.Command;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;

@Command(value = "crazyvouchers", alias = {"vouchers", "voucher"})
public abstract class BaseCommand {

    protected @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    protected @NotNull final Path dataPath = this.plugin.getDataPath();

    protected @NotNull final PaperFileManager fileManager = this.plugin.getFileManager();

    protected @NotNull final CrazyManager crazyManager = this.plugin.getCrazyManager();

    protected @NotNull final InventoryManager inventoryManager = this.plugin.getInventoryManager();

    protected @NotNull final FusionPaper fusion = this.plugin.getFusion();

    protected @NotNull final SettingsManager config = ConfigManager.getConfig();

}