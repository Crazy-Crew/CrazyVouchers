package com.badbones69.crazyvouchers.utils;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.paper.builders.folia.FoliaScheduler;
import com.ryderbelserion.fusion.paper.builders.folia.Scheduler;
import org.jspecify.annotations.NonNull;
import java.util.function.Consumer;

public class ScheduleUtils {

    private static @NonNull final CrazyVouchers plugin = CrazyVouchers.get();

    public static void dispatch(@NonNull final Consumer<Runnable> consumer) {
        new FoliaScheduler(plugin, Scheduler.global_scheduler) {
            @Override
            public void run() {
                consumer.accept(this);
            }
        }.runNow();
    }
}