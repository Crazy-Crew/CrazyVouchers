package com.badbones69.crazyvouchers.commands;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.CrazyPlatform;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import com.ryderbelserion.fusion.files.FileManager;
import com.ryderbelserion.fusion.paper.FusionPaper;
import dev.triumphteam.cmd.core.annotations.Command;
import org.jspecify.annotations.NonNull;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import java.nio.file.Path;

@Command(value = "crazyvouchers", alias = {"vouchers", "voucher"})
public abstract class BaseCommand {

    protected @NonNull final CrazyVouchers plugin = CrazyVouchers.get();

    protected @NonNull final CrazyPlatform platform = this.plugin.getPlatform();

    protected @NonNull final ConfigManager configManager = this.platform.getConfigManager();

    protected @NonNull final PropertyManager config = this.configManager.getConfig();

    protected @NonNull final Path dataPath = this.plugin.getDataPath();

    protected @NonNull final FusionPaper fusion = this.plugin.getFusion();

    protected @NonNull final FileManager fileManager = this.fusion.getFileManager();

    protected @NonNull final CrazyManager crazyManager = this.plugin.getCrazyManager();

    protected @NonNull final InventoryManager inventoryManager = this.plugin.getInventoryManager();

}