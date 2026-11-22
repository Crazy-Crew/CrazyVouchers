package com.ryderbelserion.crazyvouchers.storage.impl.sql.types;

import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.crazyvouchers.storage.StorageCredentials;
import com.ryderbelserion.crazyvouchers.storage.impl.sql.HikariFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MariadbFactory extends HikariFactory {

    public MariadbFactory(final StorageCredentials credentials, final CrazyVouchersPlugin plugin) {
        super(credentials, plugin, "MariaDB");
    }

    @Override
    public String getDriverIdentifier() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    public String getJDBCIdentifier() {
        return "mariadb";
    }

    @Override
    public int getDefaultPort() {
        return 3306;
    }
}