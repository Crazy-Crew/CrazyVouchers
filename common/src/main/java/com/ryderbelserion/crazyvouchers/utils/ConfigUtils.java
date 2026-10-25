package com.ryderbelserion.crazyvouchers.utils;

import com.ryderbelserion.fusion.core.utils.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.CommentedConfigurationNode;
import java.util.List;

@NullMarked
public class ConfigUtils {

    public static List<String> getStringList(final CommentedConfigurationNode section, final List<String> defaultValue, final Object... path) {
        final CommentedConfigurationNode node = section.node(path);

        return node.isList() ? StringUtils.getStringList(node, defaultValue) : List.of(node.getString(defaultValue.toString()));
    }
}