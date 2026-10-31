package us.crazycrew.crazyvouchers.api.adapters.sender;

import com.ryderbelserion.fusion.api.objects.FusionKey;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ISenderAdapter<C, S> {

    protected final S sender;

    public ISenderAdapter(@NonNull final S sender) {
        this.sender = sender;
    }

    public abstract UUID getUniqueId();

    public abstract String getName();

    public abstract void sendActionBar(@NonNull final FusionKey id, @NonNull final Map<String, String> placeholders);

    public abstract void sendMessage(@NonNull final FusionKey id, @NonNull final Map<String, String> placeholders);

    public abstract C getComponent(@NonNull final FusionKey id, @NonNull final Map<String, String> placeholders);

    public abstract String getMessage(@NonNull final FusionKey id, @NonNull final Map<String, String> placeholders);

    public String getMessage(@NonNull final FusionKey id) {
        return getMessage(id, Map.of());
    }

    public C getComponent(@NonNull final FusionKey id) {
        return getComponent(id, new HashMap<>());
    }

    public void sendActionBar(@NonNull final FusionKey id) {
        sendActionBar(id, new HashMap<>());
    }

    public void sendMessage(@NonNull final FusionKey id) {
        sendMessage(id, new HashMap<>());
    }

    public void init() {
        /*if (this.storage.hasUser(this.getUniqueId())) {
            this.storage.populate(this);

            return;
        }

        this.storage.insertUser(this, Instant.now().getEpochSecond());*/
    }

    public abstract boolean isConsole();

    public abstract FusionKey getLocaleKey();

    @ApiStatus.Internal
    public abstract void setLocale(final String locale);

    public String getLocale() {
        return getLocaleKey().asString();
    }
}