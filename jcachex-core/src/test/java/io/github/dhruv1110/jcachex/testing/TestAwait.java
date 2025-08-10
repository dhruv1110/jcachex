package io.github.dhruv1110.jcachex.testing;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Minimal await helper for tests to avoid fixed Thread.sleep calls.
 * Uses polling with a timeout to wait for a condition to become true.
 */
public final class TestAwait {
    private TestAwait() {}

    public static void awaitTrue(Supplier<Boolean> condition, long timeoutMs) throws InterruptedException {
        awaitTrue(condition, timeoutMs, 10);
    }

    public static void awaitTrue(Supplier<Boolean> condition, long timeoutMs, long pollIntervalMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (Boolean.TRUE.equals(condition.get())) {
                return;
            }
            Thread.sleep(pollIntervalMs);
        }
        assertTrue(Boolean.TRUE.equals(condition.get()), "Condition not satisfied within " + timeoutMs + "ms");
    }

    public static <T> T awaitNotNull(Supplier<T> supplier, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        T value;
        while (System.currentTimeMillis() < deadline) {
            value = supplier.get();
            if (value != null) {
                return value;
            }
            Thread.sleep(10);
        }
        value = supplier.get();
        assertTrue(Objects.nonNull(value), "Expected non-null value within " + timeoutMs + "ms");
        return value;
    }

    public static void awaitMillis(long millis) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(millis);
    }
}


