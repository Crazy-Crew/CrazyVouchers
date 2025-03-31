package com.badbones69.crazyvouchers.utils;

import com.ryderbelserion.fusion.paper.api.enums.Scheduler;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import java.util.function.Consumer;

public class ScheduleUtils {

    public static void dispatch(final Consumer<Runnable> consumer) {
        new FoliaScheduler(Scheduler.global_scheduler) {
            @Override
            public void run() {
                consumer.accept(this);
            }
        }.runNow();
    }
}