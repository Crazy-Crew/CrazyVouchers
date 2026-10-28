package com.badbones69.crazyvouchers.api.adapters;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyPlatform;
import com.ryderbelserion.crazyvouchers.utils.ConfigUtils;
import com.ryderbelserion.fusion.api.objects.FusionKey;
import com.ryderbelserion.fusion.core.api.registry.message.MessageRegistry;
import com.ryderbelserion.fusion.paper.FusionPaper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import us.crazycrew.crazyvouchers.api.adapters.sender.ISenderAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static us.crazycrew.crazyvouchers.api.CrazyVouchers.CONSOLE_NAME;
import static us.crazycrew.crazyvouchers.api.CrazyVouchers.CONSOLE_UUID;

public class PaperSenderAdapter extends ISenderAdapter<Component, Audience> {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final CrazyPlatform platform = this.plugin.getPlatform();

    private final FusionPaper fusion = this.platform.getFusion();

    private final MessageRegistry messageRegistry = this.fusion.getMessageRegistry();

    @Override
    public UUID getUniqueId(@NonNull final Audience sender) {
        if (sender instanceof Player player) {
            return player.getUniqueId();
        }

        return CONSOLE_UUID;
    }

    @Override
    public String getName(@NonNull final Audience sender) {
        if (sender instanceof Player player) {
            return player.getName();
        }

        return CONSOLE_NAME;
    }

    @Override
    public void sendActionBar(@NonNull final Audience sender, @NonNull final FusionKey id, @NonNull final Map<String, String> placeholders) {
        final Component component = getComponent(sender, id, placeholders);

        if (component.equals(Component.empty())) {
            return;
        }

        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(component);

            return;
        }

        sender.sendActionBar(component);
    }

    @Override
    public void sendMessage(@NonNull final Audience sender, @NonNull final FusionKey id, @NonNull final Map<String, String> placeholders) {
        final Component component = getComponent(sender, id, placeholders);

        if (component.equals(Component.empty())) {
            return;
        }

        sender.sendMessage(component);
    }

    @Override
    public Component getComponent(@NonNull final Audience sender, @NonNull final FusionKey id, @NonNull final Map<String, String> placeholders) {
        final List<String> values = new ArrayList<>();

        this.messageRegistry.getMessage(id).ifPresent(value -> values.add(value.getValue()));

        if (values.isEmpty()) {
            return Component.empty();
        }

        final String value = values.getFirst();

        if (value.isEmpty()) {
            return Component.empty();
        }

        final Map<String, String> map = new HashMap<>(placeholders);

        final String prefix = ConfigUtils.getPrefix();

        if (!prefix.isEmpty()) {
            map.putIfAbsent("{prefix}", prefix);
            map.putIfAbsent("%prefix%", prefix);
            map.putIfAbsent("%Prefix%", prefix);
        }

        return this.fusion.asComponent(sender, value, map);
    }

    @Override
    public String getMessage(@NonNull final Audience sender, @NonNull final FusionKey id, @NonNull final Map<String, String> placeholders) {
        final List<String> values = new ArrayList<>();

        this.messageRegistry.getMessage(id).ifPresent(value -> values.add(value.getValue()));

        if (values.isEmpty()) {
            return "";
        }

        final String value = values.getFirst();

        if (value.isEmpty()) {
            return "";
        }

        final Map<String, String> map = new HashMap<>(placeholders);

        final String prefix = ConfigUtils.getPrefix();

        if (!prefix.isEmpty()) {
            map.putIfAbsent("{prefix}", prefix);
            map.putIfAbsent("%prefix%", prefix);
            map.putIfAbsent("%Prefix%", prefix);
        }

        return this.fusion.replacePlaceholders(this.fusion.papi(sender, value), map);
    }

    @Override
    public boolean isConsole(@NonNull final Audience sender) {
        return sender instanceof ConsoleCommandSender;
    }
}