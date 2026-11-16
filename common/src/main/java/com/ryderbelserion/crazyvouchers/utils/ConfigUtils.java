package com.ryderbelserion.crazyvouchers.utils;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.VouchersProvider;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import java.util.List;

@NullMarked
public class ConfigUtils {

    private static final CrazyVouchersPlugin plugin = (CrazyVouchersPlugin) VouchersProvider.api();

    private static final ConfigManager configManager = plugin.getConfigManager();

    public static List<String> getStringList(final CommentedConfigurationNode section, final List<String> defaultValue, final Object... path) {
        final CommentedConfigurationNode node = section.node(path);

        return node.isList() ? StringUtils.getStringList(node, defaultValue) : List.of(node.getString(defaultValue.toString()));
    }

    public static String getPrefix() {
        return configManager.getConfig().getProperty(RootKeys.command_prefix);
    }
}