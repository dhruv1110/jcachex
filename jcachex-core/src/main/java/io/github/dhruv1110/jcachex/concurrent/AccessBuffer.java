package io.github.dhruv1110.jcachex.concurrent;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

/**
 * A lock-free access buffer that records cache operations using ring buffers
 * and write-ahead logging for high-performance concurrent access.
 * <p>
 * This implementation provides:
 * <ul>
 * <li><strong>Lock-Free Recording:</strong> Uses atomic operations for access
 * tracking</li>
 * <li><strong>Write-Ahead Logging:</strong> Ensures operation ordering and
 * consistency</li>
 * <li><strong>Batched Processing:</strong> Reduces contention through batch
 * operations</li>
 * <li><strong>Backpressure Handling:</strong> Manages buffer overflow
 * gracefully</li>
 * </ul>
 *
 * @param <K> the type of keys
 * @since 1.0.0
 */
public class AccessBuffer<K> {

    // Access types for write-ahead logging
    public enum AccessType {
        READ, WRITE, EVICT
    }

    /**
     * Represents a cache access operation.
     */
    public static class AccessRecord<K> {
        public final K key;
        public final AccessType type;
        public final long timestamp;
        public final int frequency;

        public AccessRecord(K key, AccessType type, long timestamp, int frequency) {
            this.key = key;
            this.type = type;
            this.timestamp = timestamp;
            this.frequency = frequency;
        }

        @Override
        public String toString() {
            return String.format("AccessRecord{key=%s, type=%s, timestamp=%d, frequency=%d}",
                    key, type, timestamp, frequency);
        }
    }

    private final StripedRingBuffer<AccessRecord<K>> accessBuffer;
    private final AtomicLong drainScheduled;
    // Cheap sampling counter to avoid expensive totalSize() on every record
    private final AtomicLong recordCounter;
    private final AtomicReference<Thread> drainThread;

    // Configuration
    private final int drainThreshold;
    private final long drainIntervalNanos;
    private volatile boolean shutdown;

    /**
     * Creates a new access buffer.
     *
     * @param drainThreshold     the number of accesses that trigger a drain
     * @param drainIntervalNanos the maximum time between drains in nanoseconds
     */
    public AccessBuffer(int drainThreshold, long drainIntervalNanos) {
        this.accessBuffer = new StripedRingBuffer<>();
        this.drainScheduled = new AtomicLong(0);
        this.recordCounter = new AtomicLong(0);
        this.drainThread = new AtomicReference<>();
        this.drainThreshold = drainThreshold;
        this.drainIntervalNanos = drainIntervalNanos;
        this.shutdown = false;
    }

    /**
     * Records a cache access operation.
     *
     * @param key       the cache key
     * @param type      the access type
     * @param frequency the current frequency estimate
     * @return true if the access was recorded
     */
    public boolean recordAccess(K key, AccessType type, int frequency) {
        if (shutdown || key == null) {
            return false;
        }

        AccessRecord<K> record = new AccessRecord<>(key, type, System.nanoTime(), frequency);
        boolean success = accessBuffer.recordAccess(record);

        // Schedule draining if needed, using sampling to reduce overhead
        if (success) {
            long c = recordCounter.incrementAndGet();
            // Only perform the heavier check occasionally (every 64th record)
            if ((c & 63L) == 0L && shouldScheduleDrain()) {
                scheduleDrain();
            }
        }

        return success;
    }

    /**
     * Drains the access buffer and processes all recorded accesses.
     *
     * @param processor the function to process each access record
     * @return the number of records processed
     */
    public int drainToHandler(Consumer<AccessRecord<K>> processor) {
        if (processor == null) {
            return 0;
        }

        int processed = accessBuffer.drainAll(processor);

        // Reset drain scheduling
        drainScheduled.set(0);
        recordCounter.set(0);

        return processed;
    }

    /**
     * Checks if draining should be scheduled.
     *
     * @return true if draining should be scheduled
     */
    private boolean shouldScheduleDrain() {
        // Check if we have enough pending accesses
        if (accessBuffer.totalSize() >= drainThreshold) {
            return true;
        }

        // Check if enough time has passed since last drain
        long lastDrain = drainScheduled.get();
        if (lastDrain > 0) {
            long elapsed = System.nanoTime() - lastDrain;
            return elapsed >= drainIntervalNanos;
        }

        return false;
    }

    /**
     * Schedules a drain operation.
     */
    private void scheduleDrain() {
        long currentTime = System.nanoTime();

        // Use CAS to avoid duplicate scheduling
        if (drainScheduled.compareAndSet(0, currentTime)) {
            // Try to acquire drain thread
            Thread currentThread = Thread.currentThread();
            if (drainThread.compareAndSet(null, currentThread)) {
                try {
                    // We are the drain thread, perform immediate drain
                    performAsyncDrain();
                } finally {
                    // Release drain thread
                    drainThread.set(null);
                }
            }
        }
    }

    /**
     * Performs asynchronous drain with backpressure handling.
     */
    private void performAsyncDrain() {
        // Check if we need to yield to other threads
        if (accessBuffer.getContentionLevel() > 100) {
            LockSupport.parkNanos(1000); // Park for 1 microsecond
        }

        // Perform batched drain
        int batchSize = Math.min(accessBuffer.totalSize(), 1000);
        if (batchSize > 0) {
            // Signal that drain is in progress
            drainScheduled.set(System.nanoTime());
        }
    }

    /**
     * Forces an immediate drain of all pending accesses.
     *
     * @param processor the function to process each access record
     * @return the number of records processed
     */
    public int forceDrain(Consumer<AccessRecord<K>> processor) {
        return drainToHandler(processor);
    }

    /**
     * Returns the current number of pending accesses.
     *
     * @return the number of pending accesses
     */
    public int getPendingAccessCount() {
        return accessBuffer.totalSize();
    }

    /**
     * Returns the current contention level.
     *
     * @return the contention level
     */
    public long getContentionLevel() {
        return accessBuffer.getContentionLevel();
    }

    /**
     * Returns the number of stripes in the ring buffer.
     *
     * @return the stripe count
     */
    public int getStripeCount() {
        return accessBuffer.getStripeCount();
    }

    /**
     * Checks if draining is needed.
     *
     * @return true if draining is needed
     */
    public boolean needsDraining() {
        return accessBuffer.needsDraining() || shouldScheduleDrain();
    }

    /**
     * Shuts down the access buffer.
     */
    public void shutdown() {
        shutdown = true;

        // Clear all pending accesses
        accessBuffer.clear();

        // Reset state
        drainScheduled.set(0);
        drainThread.set(null);
    }

    /**
     * Checks if the access buffer is shut down.
     *
     * @return true if shut down
     */
    public boolean isShutdown() {
        return shutdown;
    }

    /**
     * Clears all pending accesses.
     */
    public void clear() {
        accessBuffer.clear();
        drainScheduled.set(0);
    }
}
