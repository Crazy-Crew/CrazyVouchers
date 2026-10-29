package com.ryderbelserion.crazyvouchers.storage;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.holder.StorageHolder;
import com.ryderbelserion.crazyvouchers.storage.impl.sql.types.SqliteFactory;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import us.crazycrew.crazyvouchers.VouchersProvider;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.DatabaseKeys;

public class StorageManager {

    private final CrazyVouchersPlugin plugin = (CrazyVouchersPlugin) VouchersProvider.api();
    private final ConfigManager configManager = this.plugin.getConfigManager();

    public StorageHolder init() {
        final PropertyManager holder = this.configManager.getConfig();

        final String storageType = holder.getProperty(DatabaseKeys.storage_type);

        return switch (storageType.toLowerCase()) {
            case "sqlite" -> new StorageHolder(new SqliteFactory(this.plugin)).init();
            default -> throw new IllegalStateException("Unexpected storage type value: " + storageType);
        };
    }
}