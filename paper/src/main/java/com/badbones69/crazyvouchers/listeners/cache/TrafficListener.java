package com.badbones69.crazyvouchers.listeners.cache;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyPlatform;
import com.badbones69.crazyvouchers.api.registry.PaperUserRegistry;
import com.badbones69.crazyvouchers.api.registry.adapters.PaperUserAdapter;
import com.ryderbelserion.fusion.config.properties.PropertyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;

public class TrafficListener implements Listener {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final CrazyPlatform platform = this.plugin.getPlatform();

    private final ConfigManager configManager = this.platform.getConfigManager();

    private final PropertyManager config = this.configManager.getConfig();

    private final PaperUserRegistry userRegistry = this.platform.getUserRegistry();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        final PaperUserAdapter adapter = this.userRegistry.addUser(player);

        if (!this.config.getProperty(RootKeys.offline_player_support)) {
            return;
        }

        if (this.config.getProperty(RootKeys.offline_player_support_on_join)) {
            this.userRegistry.loadVouchers(adapter);

            return;
        }

        if (this.config.getProperty(RootKeys.offline_player_support_vouchers_not_claimed)) {
            Message.claim_vouchers.sendMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.userRegistry.removeUser(event.getPlayer().getUniqueId());
    }
}