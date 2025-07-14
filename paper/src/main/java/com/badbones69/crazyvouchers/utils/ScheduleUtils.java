package com.badbones69.crazyvouchers.utils;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.paper.api.enums.Scheduler;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import java.util.function.Consumer;

public class ScheduleUtils {

    private static final CrazyVouchers plugin = CrazyVouchers.get();

    public static void dispatch(final Consumer<Runnable> consumer) {
        new FoliaScheduler(plugin, Scheduler.global_scheduler) {
            @Override
            public void run() {
                consumer.accept(this);
            }
        }.runNow();
    }
}