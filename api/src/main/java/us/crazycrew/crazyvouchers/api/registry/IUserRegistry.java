package us.crazycrew.crazyvouchers.api.registry;

import org.jspecify.annotations.NonNull;
import us.crazycrew.crazyvouchers.api.adapters.sender.ISenderAdapter;
import java.util.Optional;
import java.util.UUID;

public interface IUserRegistry<C, S, A extends ISenderAdapter<C, S>> {

    Optional<A> getUser(@NonNull final UUID uuid);

    void loadVouchers(A adapter);

    A removeUser(@NonNull final UUID uuid);

    A addUser(@NonNull final S player);

    A getConsole();

    void init();

}