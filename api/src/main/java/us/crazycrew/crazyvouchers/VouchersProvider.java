package us.crazycrew.crazyvouchers;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyvouchers.api.CrazyVouchers;

public class VouchersProvider {

    private static CrazyVouchers vouchers;

    private VouchersProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    @ApiStatus.Internal
    public static void register(@NotNull final CrazyVouchers instance) {
        if (VouchersProvider.vouchers != null) return;

        VouchersProvider.vouchers = instance;
    }

    @ApiStatus.Internal
    public static void unregister() {
        VouchersProvider.vouchers = null;
    }

    public static CrazyVouchers api() {
        return vouchers;
    }
}