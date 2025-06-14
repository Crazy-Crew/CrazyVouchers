package com.badbones69.crazyvouchers.commands.features.admin;

import com.badbones69.crazyvouchers.commands.BaseCommand;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Syntax;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class CommandMenu extends BaseCommand {

    @Command(value = "open", alias = "admin")
    @Permission(value = "crazyvouchers.open", def = PermissionDefault.OP)
    @Syntax("/crazyvouchers open [page]")
    public void open(final Player player, final int page) {
        this.inventoryManager.buildInventory(player, Math.max(page, 1));
    }
}