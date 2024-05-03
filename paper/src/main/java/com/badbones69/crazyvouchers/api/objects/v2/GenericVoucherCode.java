package com.badbones69.crazyvouchers.api.objects.v2;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class GenericVoucherCode extends AbstractVoucher {

    public GenericVoucherCode(ConfigurationSection section, String file) {
        super(section, file, "");
    }

    @Override
    public boolean execute(Player player) {
        return false;
    }
}