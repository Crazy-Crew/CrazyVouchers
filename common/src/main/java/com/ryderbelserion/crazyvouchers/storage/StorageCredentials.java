package com.ryderbelserion.crazyvouchers.storage;

import org.jspecify.annotations.NonNull;

public record StorageCredentials(
        String database, String username, String password, String address,
        long connectionTimeout, long maxLifetime, int maxPoolSize, long heartbeat, int minIdle,
        int port
) {

    public StorageCredentials(
            @NonNull final String database,
            @NonNull final String username,
            @NonNull final String password,
            @NonNull final String address,

            final long connectionTimeout,
            final long maxLifetime,
            final int maxPoolSize,
            final long heartbeat,
            final int minIdle,
            final int port
    ) {
        this.database = database;
        this.username = username;
        this.password = password;
        this.address = address;

        this.connectionTimeout = connectionTimeout;
        this.maxLifetime = maxLifetime;
        this.maxPoolSize = maxPoolSize;
        this.heartbeat = heartbeat;
        this.minIdle = minIdle;
        this.port = port;
    }
}