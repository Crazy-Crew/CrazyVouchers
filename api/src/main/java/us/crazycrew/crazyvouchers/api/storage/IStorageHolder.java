package us.crazycrew.crazyvouchers.api.storage;

import org.jetbrains.annotations.ApiStatus;
import java.sql.Connection;

public abstract class IStorageHolder extends ISharedMethods {

    @ApiStatus.Internal
    protected abstract Connection getConnection();

    public abstract IStorageHolder migrate();

    public abstract IStorageHolder init();

    public abstract IStorageHolder post();

    public abstract IStorageHolder save();

    public abstract IStorageHolder stop();
}