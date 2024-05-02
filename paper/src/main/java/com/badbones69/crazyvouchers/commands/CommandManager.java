package com.badbones69.crazyvouchers.commands;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyManager;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.objects.VoucherCode;
import com.badbones69.crazyvouchers.commands.types.admin.CommandGive;
import com.badbones69.crazyvouchers.commands.types.admin.CommandOpen;
import com.badbones69.crazyvouchers.commands.types.admin.CommandReload;
import com.badbones69.crazyvouchers.commands.types.admin.CommandTypes;
import com.badbones69.crazyvouchers.commands.types.player.CommandHelp;
import com.badbones69.crazyvouchers.commands.types.player.CommandRedeem;
import com.ryderbelserion.vital.util.builders.PlayerBuilder;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final static @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);
    private final static @NotNull CrazyManager crazyManager = plugin.getCrazyManager();

    private final static @NotNull BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(plugin);

    /**
     * Loads commands.
     */
    public static void load() {
        commandManager.registerSuggestion(SuggestionKey.of("keys"), (sender, context) -> List.of("virtual", "v", "physical", "p"));

        commandManager.registerSuggestion(SuggestionKey.of("players"), (sender, context) -> plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());

        commandManager.registerSuggestion(SuggestionKey.of("numbers"), (sender, context) -> {
            List<String> numbers = new ArrayList<>();

            for (int i = 1; i <= 64; i++) numbers.add(String.valueOf(i));

            return numbers;
        });

        commandManager.registerSuggestion(SuggestionKey.of("vouchers"), (sender, context) -> crazyManager.getVouchers().stream().map(Voucher::getName).toList());

        commandManager.registerSuggestion(SuggestionKey.of("codes"), (sender, context) -> crazyManager.getVoucherCodes().stream().map(VoucherCode::getCode).toList());

        commandManager.registerArgument(PlayerBuilder.class, (sender, context) -> new PlayerBuilder(context));

        List.of(
                new CommandHelp(),
                new CommandRedeem(),

                new CommandGive(),
                new CommandOpen(),
                new CommandReload(),
                new CommandTypes()
        ).forEach(commandManager::registerCommand);
    }

    public static @NotNull BukkitCommandManager<CommandSender> getCommandManager() {
        return commandManager;
    }
}