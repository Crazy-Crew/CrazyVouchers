package us.crazycrew.crazyvouchers.api.storage;

import org.jetbrains.annotations.ApiStatus;
import java.sql.Connection;

public abstract class AbstractStorageHolder extends AbstractUserHolder {

    @ApiStatus.Internal
    protected abstract Connection getConnection();

    public abstract AbstractStorageHolder migrate();

    public abstract AbstractStorageHolder init();

    public abstract AbstractStorageHolder post();

    public abstract AbstractStorageHolder save();

    public abstract AbstractStorageHolder stop();
}