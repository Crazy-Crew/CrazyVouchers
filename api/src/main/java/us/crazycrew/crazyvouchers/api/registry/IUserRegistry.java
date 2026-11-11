package us.crazycrew.crazyvouchers.api.registry;

import org.jspecify.annotations.NonNull;
import us.crazycrew.crazyvouchers.api.adapters.sender.ISenderAdapter;
import java.util.Optional;
import java.util.UUID;

public interface IUserRegistry<C, S> {

    Optional<? extends ISenderAdapter<C, S>> getUser(@NonNull final UUID uuid);

    ISenderAdapter<C, S> removeUser(@NonNull final UUID uuid);

    ISenderAdapter<C, S> addUser(@NonNull final S player);

    ISenderAdapter<C, S> getConsole();

    void init();

}