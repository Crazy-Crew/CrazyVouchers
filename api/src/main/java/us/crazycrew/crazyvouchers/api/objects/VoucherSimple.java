package us.crazycrew.crazyvouchers.api.objects;

import org.jspecify.annotations.NullMarked;
import java.util.UUID;

@NullMarked
public final class VoucherSimple {

    private final UUID player_uuid;
    private final String id;
    private int amount;

    public VoucherSimple(final UUID player_uuid, final String id, final int amount) {
        this.player_uuid = player_uuid;
        this.amount = amount;
        this.id = id;
    }

    public int getAmount() {
        return this.amount;
    }

    // used for leftover values if inventory is full.
    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public UUID getUUID() {
        return this.player_uuid;
    }

    public String getId() {
        return this.id;
    }
}