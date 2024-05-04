package com.badbones69.crazyvouchers.api.objects.v2;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenericVoucherCode extends AbstractVoucher {

    public GenericVoucherCode(@NotNull final ConfigurationSection section, @NotNull final String file) {
        super(section, file, "");
    }

    @Override
    public boolean execute(@NotNull final Player player) {
        return false;
    }

    // Not needed in this class.
    @Override
    public boolean execute(@NotNull final Player player, @Nullable final String argument) { return false; }
}