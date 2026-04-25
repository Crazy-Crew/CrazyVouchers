package com.badbones69.crazyvouchers.support;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class NexoSupport {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("Nexo");
    }

    /**
     * Builds an ItemStack from a Nexo item ID, applying optional overrides from CrazyVouchers config.
     * A non-empty overrideLore replaces the Nexo lore; glowing/customModelData are applied only when not at their
     * "unset" sentinel values ("none" and -1 respectively).
     *
     * @return the built ItemStack, or null if the ID doesn't exist in Nexo (triggers fallback).
     */
    public static @Nullable ItemStack buildItem(@NotNull final String nexoId,
                                                @NotNull final List<String> overrideLore,
                                                @NotNull final String overrideGlowing,
                                                final int overrideCustomModelData,
                                                final int amount) {
        final ItemBuilder builder = NexoItems.itemFromId(nexoId);

        if (builder == null) return null;

        if (!overrideLore.isEmpty()) {
            final List<Component> components = overrideLore.stream()
                    .map(MM::deserialize)
                    .toList();
            builder.lore(components);
        }

        switch (overrideGlowing.toLowerCase()) {
            case "add_glow", "true" -> builder.setEnchantmentGlintOverride(true);
            case "remove_glow", "false" -> builder.setEnchantmentGlintOverride(false);
            default -> {}
        }

        if (overrideCustomModelData != -1) {
            builder.customModelData(overrideCustomModelData);
        }

        final ItemStack item = builder.build();

        if (item != null) item.setAmount(amount);

        return item;
    }
}
