package com.ryderbelserion.crazyvouchers.storage;

import org.jspecify.annotations.NonNull;
import java.util.Map;

public record StorageCredentials(
        String database, String username, String password, String address,
        long connectionTimeout, int queryTimeout, long maxLifetime, int maxPoolSize, long heartbeat, int minIdle,
        int port, Map<String, Object> values
) {

    public StorageCredentials(
            @NonNull final String database,
            @NonNull final String username,
            @NonNull final String password,
            @NonNull final String address,

            final long connectionTimeout,
            final int queryTimeout,
            final long maxLifetime,
            final int maxPoolSize,
            final long heartbeat,
            final int minIdle,
            final int port,
            final Map<String, Object> values
            ) {
        this.database = database;
        this.username = username;
        this.password = password;
        this.address = address;

        this.connectionTimeout = connectionTimeout;
        this.queryTimeout = queryTimeout;
        this.maxLifetime = maxLifetime;
        this.maxPoolSize = maxPoolSize;
        this.heartbeat = heartbeat;
        this.minIdle = minIdle;
        this.values = values;
        this.port = port;
    }
}