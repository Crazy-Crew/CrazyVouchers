package com.ryderbelserion.crazyvouchers.storage.impl;

import org.jspecify.annotations.NullMarked;
import us.crazycrew.crazyvouchers.api.storage.ISharedMethods;
import java.sql.Connection;
import java.sql.SQLException;

@NullMarked
public abstract class ConnectionFactory extends ISharedMethods {

    public abstract Connection getConnection() throws SQLException;

    public abstract String getImpl();

    public abstract void migrate();

    public abstract void init();

    public abstract void post();

    public abstract void stop();

    public abstract void save();

    public abstract int queryTimeout();

    public abstract int timeout();

    public boolean tableExists(final String table) {
        return true;
    }
}