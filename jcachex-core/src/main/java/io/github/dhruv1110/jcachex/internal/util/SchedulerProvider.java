package io.github.dhruv1110.jcachex.internal.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a shared ScheduledExecutorService for all caches to avoid creating
 * per-cache scheduler threads. The thread pool is daemon-based and lightweight.
 */
public final class SchedulerProvider {

    private static final ScheduledExecutorService SHARED_SCHEDULER =
            Executors.newScheduledThreadPool(2, new NamedDaemonThreadFactory("jcachex-scheduler-"));

    private SchedulerProvider() {}

    public static ScheduledExecutorService get() {
        return SHARED_SCHEDULER;
    }

    private static final class NamedDaemonThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger count = new AtomicInteger(1);

        private NamedDaemonThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + count.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}


