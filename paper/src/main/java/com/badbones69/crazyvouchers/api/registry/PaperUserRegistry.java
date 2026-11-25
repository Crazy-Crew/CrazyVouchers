package com.badbones69.crazyvouchers.api.registry;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.CrazyPlatform;
import com.badbones69.crazyvouchers.api.objects.Voucher;
import com.badbones69.crazyvouchers.api.registry.adapters.PaperUserAdapter;
import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.fusion.config.properties.PropertyManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NonNull;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import us.crazycrew.crazyvouchers.api.enums.messages.Message;
import us.crazycrew.crazyvouchers.api.objects.VoucherSimple;
import us.crazycrew.crazyvouchers.api.registry.IUserRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PaperUserRegistry implements IUserRegistry<Component, Audience, PaperUserAdapter> {

    private final Map<UUID, PaperUserAdapter> users = new HashMap<>();

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final CrazyPlatform platform = this.plugin.getPlatform();

    private final ConfigManager configManager = this.platform.getConfigManager();

    private final PropertyManager config = this.configManager.getConfig();

    private final Server server = this.plugin.getServer();

    @Override
    public void init() {
        this.users.put(CrazyVouchersPlugin.CONSOLE_UUID, new PaperUserAdapter(this.server.getConsoleSender()));
    }

    @Override
    public PaperUserAdapter addUser(@NonNull final Audience sender) {
        final Optional<UUID> optional = sender.get(Identity.UUID);

        return optional.map(uuid -> this.users.computeIfAbsent(uuid, _ -> new PaperUserAdapter(sender).init())).orElse(null);
    }

    @Override
    public void loadVouchers(final PaperUserAdapter adapter) {
        if (!adapter.hasOfflinePlayer()) {
            return;
        }

        final Player player = adapter.getSender();

        final PlayerInventory inventory = player.getInventory();

        final List<VoucherSimple> vouchers = adapter.getOfflineVouchers();

        final List<VoucherSimple> success = new ArrayList<>();

        final boolean isToggleEnabled = this.config.getProperty(RootKeys.offline_player_support_vouchers_not_claimed);

        for (final VoucherSimple index : vouchers) {
            final Voucher voucher = this.platform.getVoucher(index.getId());

            if (voucher == null) {
                continue;
            }

            if (!inventory.isEmpty()) { // stop the loop
                if (isToggleEnabled) {
                    Message.claim_vouchers.sendMessage(player);
                }

                break;
            }

            final ItemStack itemStack = voucher.buildItem(player, index.getAmount());

            final Map<Integer, ItemStack> items = inventory.addItem(itemStack);

            if (!items.isEmpty()) { // if any failed to add to inventory
                for (final ItemStack item : items.values()) {
                    final int amount = item.getAmount(); // remaining vouchers

                    index.setAmount(amount);
                }

                if (isToggleEnabled) {
                    Message.claim_vouchers.sendMessage(player);
                }

                success.add(index);

                break;
            }

            index.setAmount(0); // set to 0 since we're done

            success.add(index);
        }

        if (!success.isEmpty()) {
            adapter.removeOfflineVouchers(success);
        }
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