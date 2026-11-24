package com.ryderbelserion.crazyvouchers.storage;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.holder.StorageHolder;
import com.ryderbelserion.crazyvouchers.storage.impl.file.SqliteFactory;
import com.ryderbelserion.crazyvouchers.storage.impl.sql.types.MariadbFactory;
import com.ryderbelserion.crazyvouchers.storage.impl.sql.types.PostgresFactory;
import com.ryderbelserion.fusion.config.properties.PropertyManager;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.DatabaseKeys;
import java.util.HashMap;
import java.util.Map;

public class StorageManager {

    private final CrazyVouchersPlugin plugin;
    private final ConfigManager configManager;

    public StorageManager(final CrazyVouchersPlugin plugin) {
        this.plugin = plugin;
        this.configManager = this.plugin.getConfigManager();
    }

    public StorageHolder init() {
        final PropertyManager config = this.configManager.getConfig();

        final Map<String, Object> values = new HashMap<>();

        for (final String line : config.getProperty(DatabaseKeys.properties)) {
            if (!line.contains(":")) {
                continue;
            }

            final String[] splitter = line.split(":");

            final String key = splitter[0];
            final Object value = splitter[1];

            values.putIfAbsent(key, value);
        }

        final StorageCredentials credentials = new StorageCredentials(
                config.getProperty(DatabaseKeys.database_name),
                config.getProperty(DatabaseKeys.database_username),
                config.getProperty(DatabaseKeys.database_password),
                config.getProperty(DatabaseKeys.database_address),
                config.getProperty(DatabaseKeys.connection_timeout),
                config.getProperty(DatabaseKeys.query_timeout),
                config.getProperty(DatabaseKeys.maximum_lifetime),
                config.getProperty(DatabaseKeys.maximum_pool_size),
                config.getProperty(DatabaseKeys.connection_heartbeat),
                config.getProperty(DatabaseKeys.minimum_pool_size),
                config.getProperty(DatabaseKeys.database_port),
                values
        );

        final String storageType = config.getProperty(DatabaseKeys.storage_type);

        return switch (storageType.toLowerCase()) {
            case "sqlite" -> new StorageHolder(new SqliteFactory(credentials, this.plugin)).init();
            case "mariadb" -> new StorageHolder(new MariadbFactory(credentials, this.plugin)).init();
            case "postgres" -> new StorageHolder(new PostgresFactory(credentials, this.plugin)).init();
            default -> throw new IllegalStateException("Unexpected storage type value: " + storageType);
        };
    }
}