package com.badbones69.crazyvouchers.commands;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.InventoryManager;
import com.ryderbelserion.fusion.paper.files.FileManager;
import dev.triumphteam.cmd.core.annotations.Command;

@Command(value = "crazyvouchers", alias = {"vouchers", "voucher"})
public abstract class BaseCommand {

    protected final CrazyVouchers plugin = CrazyVouchers.get();

    protected final FileManager fileManager = this.plugin.getFileManager();

    protected final CrazyManager crazyManager = this.plugin.getCrazyManager();

    protected final InventoryManager inventoryManager = this.plugin.getInventoryManager();

}