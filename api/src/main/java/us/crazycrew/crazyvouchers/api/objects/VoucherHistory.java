package us.crazycrew.crazyvouchers.api.objects;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class VoucherHistory {

    private final String voucher_id;
    private final UUID player_uuid;
    private final int redeemLimit;
    private final long timestamp;

    public VoucherHistory(final String voucher_id, final UUID player_uuid, final int redeemLimit, final long timestamp) {
        this.voucher_id = voucher_id;
        this.player_uuid = player_uuid;
        this.redeemLimit = redeemLimit;
        this.timestamp = timestamp;
    }

    public String getVoucherId() {
        return this.voucher_id;
    }

    public int getRedeemLimit() {
        return this.redeemLimit;
    }

    public String getTimestamp() { //todo() get/store player timezone
        return Instant.ofEpochSecond(this.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
    }

    public UUID getPlayerId() {
        return this.player_uuid;
    }
}