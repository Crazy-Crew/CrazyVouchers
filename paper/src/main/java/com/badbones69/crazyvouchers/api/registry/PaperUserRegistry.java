package com.badbones69.crazyvouchers.api.registry;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.registry.adapters.PaperUserAdapter;
import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import us.crazycrew.crazyvouchers.api.registry.IUserRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PaperUserRegistry implements IUserRegistry<Component, Audience> {

    private final Map<UUID, PaperUserAdapter> users = new HashMap<>();

    private final CrazyVouchers plugin = CrazyVouchers.get();

    @Override
    public void init() {
        this.users.put(CrazyVouchersPlugin.CONSOLE_UUID, new PaperUserAdapter(Audience.empty()));
    }

    @Override
    public PaperUserAdapter addUser(@NonNull final Audience sender) {
        final PaperUserAdapter adapter = new PaperUserAdapter(sender);

        adapter.init();

        return this.users.putIfAbsent(adapter.getUniqueId(), adapter);
    }

    @Override
    public PaperUserAdapter removeUser(@NonNull final UUID uuid) {
        return this.users.remove(uuid);
    }

    @Override
    public Optional<PaperUserAdapter> getUser(@NonNull final UUID uuid) {
        final Server server = this.plugin.getServer();

        final Player player = server.getPlayer(uuid);

        if (!this.users.containsKey(uuid) && player != null) {
            return Optional.of(addUser(player));
        }

        return Optional.of(this.users.get(uuid));
    }

    @Override
    public @NonNull final PaperUserAdapter getConsole() {
        return this.users.get(CrazyVouchersPlugin.CONSOLE_UUID);
    }
}