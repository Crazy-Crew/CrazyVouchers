package com.ryderbelserion.crazyvouchers.storage.impl.file;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.impl.ConnectionFactory;
import com.ryderbelserion.fusion.core.FusionCore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class FlatFactory extends ConnectionFactory {

    protected final CrazyVouchersPlugin plugin;
    protected final FusionCore fusion;

    private final String impl;

    public FlatFactory(final CrazyVouchersPlugin plugin, final String impl) {
        this.fusion = plugin.getFusion();
        this.plugin = plugin;
        this.impl = impl;
    }

    @Override
    public String getImpl() {
        return this.impl;
    }
}