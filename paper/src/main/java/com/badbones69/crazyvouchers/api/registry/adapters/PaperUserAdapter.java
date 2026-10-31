package com.badbones69.crazyvouchers.api.registry.adapters;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.utils.ConfigUtils;
import com.ryderbelserion.fusion.api.FusionProvider;
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

public class PaperUserAdapter extends ISenderAdapter<Component, Audience> {

    private final FusionPaper fusion = (FusionPaper) FusionProvider.api();

    private final MessageRegistry registry = this.fusion.getMessageRegistry();

    private final String name;
    private final UUID uuid;

    public PaperUserAdapter(@NonNull final Audience sender) {
        super(sender);

        if (sender instanceof Player index) {
            setLocale(index.locale().toString());

            this.uuid = index.getUniqueId();
            this.name = index.getName();

            return;
        }

        this.uuid = CONSOLE_UUID;
        this.name = CONSOLE_NAME;

        setLocale("en-US.yml");
    }

    private FusionKey locale;

    @Override
    public void sendActionBar(@NonNull final FusionKey id, @NonNull final Map<String, String> placeholders) {
        final Component component = getComponent(id, placeholders);

        if (component.equals(Component.empty())) {
            return;
        }

        if (isConsole()) {
            this.sender.sendMessage(component);

            return;
        }

        this.sender.sendActionBar(component);
    }

    @Override
    public void sendMessage(@NonNull final FusionKey id, @NonNull final Map<String, String> placeholders) {
        final Component component = getComponent(id, placeholders);

        if (component.equals(Component.empty())) {
            return;
        }

        this.sender.sendMessage(component);
    }

    @Override
    public Component getComponent(@NonNull final FusionKey id, @NonNull final Map<String, String> placeholders) {
        final List<String> values = new ArrayList<>();

        this.registry.getMessage(id).ifPresent(value -> values.add(value.getValue()));

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

        return this.fusion.asComponent(this.sender, value, map);
    }

    @Override
    public String getMessage(@NonNull final FusionKey id, @NonNull final Map<String, String> placeholders) {
        final List<String> values = new ArrayList<>();

        this.registry.getMessage(id).ifPresent(value -> values.add(value.getValue()));

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

        return this.fusion.replacePlaceholders(this.fusion.papi(this.sender, value), map);
    }

    @Override
    public FusionKey getLocaleKey() {
        return this.locale;
    }

    @Override
    public void setLocale(@NonNull final String locale) {
        final String[] splitter = locale.contains("-") ? locale.split("-") : locale.split("_");

        final String language = splitter[0];
        final String country = splitter[1];

        final String value = "%s_%s.yml".formatted(language, country).toLowerCase();

        if (!value.equalsIgnoreCase("en_us.yml")) {
            this.locale = FusionKey.key(CrazyVouchersPlugin.namespace, value);
        }
    }

    @Override
    public boolean isConsole() {
        return this.sender instanceof ConsoleCommandSender;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }
}