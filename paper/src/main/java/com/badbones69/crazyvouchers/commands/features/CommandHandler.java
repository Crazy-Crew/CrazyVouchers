package com.badbones69.crazyvouchers.commands.features;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.enums.misc.PermissionKeys;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.commands.features.admin.CommandGive;
import com.badbones69.crazyvouchers.commands.features.admin.CommandMenu;
import com.badbones69.crazyvouchers.commands.features.admin.CommandMigrate;
import com.badbones69.crazyvouchers.commands.features.admin.CommandRedeem;
import com.badbones69.crazyvouchers.commands.features.admin.CommandTypes;
import com.badbones69.crazyvouchers.commands.features.base.CommandHelp;
import com.badbones69.crazyvouchers.commands.features.base.CommandReload;
import com.badbones69.crazyvouchers.commands.features.relations.ArgumentRelations;
import com.ryderbelserion.fusion.paper.builder.PlayerBuilder;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this.plugin);

    public CommandHandler() {
        load();
    }

    public void load() {
        final Server server = this.plugin.getServer();

        final PluginManager pluginManager = server.getPluginManager();

        new ArgumentRelations(this.commandManager).build();

        this.commandManager.registerSuggestion(SuggestionKey.of("players"), (sender, context) -> server.getOnlinePlayers().stream().map(Player::getName).toList());

        this.commandManager.registerSuggestion(SuggestionKey.of("numbers"), (sender, context) -> {
            final List<String> numbers = new ArrayList<>();

            for (int i = 1; i <= 100; i++) numbers.add(String.valueOf(i));

            return numbers;
        });

        this.commandManager.registerSuggestion(SuggestionKey.of("doubles"), (sender, context) -> {
            final List<String> numbers = new ArrayList<>();

            int count = 0;

            while (count <= 1000) {
                double x = count / 10.0;

                numbers.add(String.valueOf(x));

                count++;
            }

            return numbers;
        });

        this.commandManager.registerSuggestion(SuggestionKey.of("codes"), (sender, context) -> {
            final List<VoucherCode> codes = this.crazyManager.getVoucherCodes();

            final List<String> suggestions = new ArrayList<>();

            if (sender instanceof Player player) {
                if (PermissionKeys.crazyvouchers_admin.hasPermission(player)) { // if they have this permission, they get all the codes.
                    suggestions.addAll(codes.stream().map(VoucherCode::getCode).toList());

                    return suggestions;
                }

                codes.forEach(code -> {
                    final boolean blacklist = code.useBlacklistPermissions();
                    final boolean whitelist = code.useWhiteListPermissions();

                    final List<String> permissions = blacklist ? code.getBlacklistPermissions() : whitelist ? code.getWhitelistPermissions() : List.of();
                    final List<String> commands = blacklist ? code.getBlacklistCommands() : whitelist ? code.getWhitelistCommands() : List.of();
                    final String message = blacklist ? code.getBlacklistMessage() : whitelist ? code.getWhitelistWorldMessage() : "";

                    if (code.hasPermission(false, player, permissions, commands, null, message, "")) {
                        suggestions.add(code.getCode());
                    }
                });
            }

            return suggestions;
        });

        this.commandManager.registerSuggestion(SuggestionKey.of("vouchers"), (sender, context) -> {
            final List<Voucher> codes = this.crazyManager.getVouchers();

            final List<String> suggestions = new ArrayList<>();

            if (sender instanceof Player player) {
                if (PermissionKeys.crazyvouchers_admin.hasPermission(player)) { // if they have this permission, they get all the codes.
                    suggestions.addAll(codes.stream().map(Voucher::getName).toList());

                    return suggestions;
                }
            }

            return suggestions;
        });

        this.commandManager.registerArgument(PlayerBuilder.class, (sender, context) -> new PlayerBuilder(context));

        List.of(
                new CommandGive(),
                new CommandMenu(),
                new CommandMigrate(),
                new CommandRedeem(),
                new CommandTypes(),
                new CommandHelp(),
                new CommandReload()
        ).forEach(this.commandManager::registerCommand);

        for (final PermissionKeys key : PermissionKeys.values()) {
            final String node = key.getPermission();

            final Permission current = pluginManager.getPermission(node);

            if (current != null) continue;

            final Permission permission = new Permission(
                    node,
                    key.getDescription(),
                    key.isDefault(),
                    key.getChildren()
            );

            pluginManager.addPermission(permission);
        }
    }

    public final BukkitCommandManager<CommandSender> getCommandManager() {
        return this.commandManager;
    }
}