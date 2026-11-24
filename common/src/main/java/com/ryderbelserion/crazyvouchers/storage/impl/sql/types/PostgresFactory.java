package com.ryderbelserion.crazyvouchers.storage.impl.sql.types;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.StorageCredentials;
import com.ryderbelserion.crazyvouchers.storage.impl.sql.HikariFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PostgresFactory extends HikariFactory {

    public PostgresFactory(final StorageCredentials credentials, final CrazyVouchersPlugin plugin) {
        super(credentials, plugin, "PostgresSQL");
    }

    @Override
    public String getDriverIdentifier() {
        return "org.postgresql.Driver";
    }

    @Override
    public String getJDBCIdentifier() {
        return "postgresql";
    }

    @Override
    public int getDefaultPort() {
        return 5432;
    }

    @Override
    public String url() {
        return "jdbc:%s://%s:%s/%s";
    }
}