package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheStats;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.AbstractMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.Instant;
import java.time.Duration;

/**
 * Specialized write-heavy cache implementation optimized for write-intensive
 * workloads.
 *
 * Key optimizations:
 * - Asynchronous write operations
 * - Write batching and coalescing
 * - Optimized write-through and write-behind strategies
 * - Minimal read-path interference
 * - Write buffer management
 */
public final class WriteHeavyOptimizedCache<K, V> implements Cache<K, V> {

    // Core data structures
    private final ConcurrentHashMap<K, VersionedEntry<V>> data;
    private final AtomicLong hitCount;
    private final AtomicLong missCount;
    private final AtomicLong writeCount;
    private final long maximumSize;
    private final boolean statsEnabled;

    // Write optimization structures
    private final WriteBuffer<K, V> writeBuffer;
    private final WriteBatcher<K, V> writeBatcher;
    private final WriteCoalescer<K, V> writeCoalescer;

    // Asynchronous write processing
    private final ScheduledExecutorService writeExecutor;
    private final WriteProcessor<K, V> writeProcessor;

    // Configuration
    private static final int WRITE_BUFFER_SIZE = 1024;
    private static final int WRITE_BATCH_SIZE = 64;
    private static final long WRITE_FLUSH_INTERVAL_MS = 10;

    public WriteHeavyOptimizedCache(CacheConfig<K, V> config) {
        this.maximumSize = config.getMaximumSize();
        this.statsEnabled = config.isRecordStats();

        // Initialize with write-optimized capacity
        int capacity = Math.max(16, Integer.highestOneBit((int) maximumSize) << 1);
        this.data = new ConcurrentHashMap<>(capacity, 0.75f, 32); // More segments for writes

        // Initialize counters
        this.hitCount = new AtomicLong(0);
        this.missCount = new AtomicLong(0);
        this.writeCount = new AtomicLong(0);

        // Initialize write optimization structures
        this.writeBuffer = new WriteBuffer<>(WRITE_BUFFER_SIZE);
        this.writeBatcher = new WriteBatcher<>(WRITE_BATCH_SIZE);
        this.writeCoalescer = new WriteCoalescer<>();

        // Initialize asynchronous write processing
        this.writeExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "jcachex-write-processor");
            thread.setDaemon(true);
            return thread;
        });

        this.writeProcessor = new WriteProcessor<>(this);

        // Start write processing
        writeExecutor.scheduleWithFixedDelay(
                writeProcessor::processWrites,
                0,
                WRITE_FLUSH_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public final V get(K key) {
        if (key == null)
            return null;

        // Check write buffer first for most recent data
        V bufferedValue = writeBuffer.get(key);
        if (bufferedValue != null) {
            if (statsEnabled) {
                hitCount.incrementAndGet();
            }
            return bufferedValue;
        }

        // Check main storage
        VersionedEntry<V> entry = data.get(key);
        if (entry == null) {
            if (statsEnabled) {
                missCount.incrementAndGet();
            }
            return null;
        }

        V value = entry.getValue();
        if (value != null) {
            if (statsEnabled) {
                hitCount.incrementAndGet();
            }
        } else {
            if (statsEnabled) {
                missCount.incrementAndGet();
            }
        }

        return value;
    }

    @Override
    public final void put(K key, V value) {
        if (key == null || value == null)
            return;

        // Asynchronous write optimization
        WriteOperation<K, V> writeOp = new WriteOperation<>(key, value, WriteOperation.Type.PUT);

        // Add to write buffer for immediate read consistency
        writeBuffer.put(key, value);

        // Add to write batcher for efficient processing
        writeBatcher.addOperation(writeOp);

        if (statsEnabled) {
            writeCount.incrementAndGet();
        }

        // Trigger immediate flush if buffer is full
        if (writeBuffer.isFull()) {
            writeProcessor.processWrites();
        }
    }

    @Override
    public final V remove(K key) {
        if (key == null)
            return null;

        // Get current value before removal
        V currentValue = get(key);

        // Asynchronous remove optimization
        WriteOperation<K, V> writeOp = new WriteOperation<>(key, null, WriteOperation.Type.REMOVE);

        // Remove from write buffer
        writeBuffer.remove(key);

        // Add to write batcher
        writeBatcher.addOperation(writeOp);

        if (statsEnabled) {
            writeCount.incrementAndGet();
        }

        return currentValue;
    }

    @Override
    public final void clear() {
        // Flush all pending writes first
        writeProcessor.processWrites();

        // Clear all data structures
        data.clear();
        writeBuffer.clear();
        writeBatcher.clear();
        writeCoalescer.clear();
    }

    @Override
    public final long size() {
        return data.size() + writeBuffer.size();
    }

    @Override
    public final boolean containsKey(K key) {
        return key != null && (writeBuffer.containsKey(key) || data.containsKey(key));
    }

    @Override
    public final CacheStats stats() {
        return StatisticsProvider.createBasicStats(hitCount, missCount);
    }

    @Override
    public final CacheConfig<K, V> config() {
        return ConfigurationProvider.createBasicConfig(maximumSize, statsEnabled);
    }

    @Override
    public final Set<K> keys() {
        Set<K> keys = new java.util.HashSet<>(data.keySet());
        keys.addAll(writeBuffer.keys());
        return keys;
    }

    @Override
    public final Collection<V> values() {
        Collection<V> values = new java.util.ArrayList<>();

        // Add values from write buffer (most recent)
        values.addAll(writeBuffer.values());

        // Add values from main storage (excluding those in write buffer)
        for (Map.Entry<K, VersionedEntry<V>> entry : data.entrySet()) {
            if (!writeBuffer.containsKey(entry.getKey())) {
                values.add(entry.getValue().getValue());
            }
        }

        return values;
    }

    @Override
    public final Set<Map.Entry<K, V>> entries() {
        Set<Map.Entry<K, V>> entries = new java.util.HashSet<>();

        // Add entries from write buffer (most recent)
        entries.addAll(writeBuffer.entries());

        // Add entries from main storage (excluding those in write buffer)
        for (Map.Entry<K, VersionedEntry<V>> entry : data.entrySet()) {
            if (!writeBuffer.containsKey(entry.getKey())) {
                entries.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getValue()));
            }
        }

        return entries;
    }

    @Override
    public final CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.completedFuture(get(key));
    }

    @Override
    public final CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.runAsync(() -> put(key, value));
    }

    @Override
    public final CompletableFuture<V> removeAsync(K key) {
        return CompletableFuture.completedFuture(remove(key));
    }

    @Override
    public final CompletableFuture<Void> clearAsync() {
        return CompletableFuture.runAsync(this::clear);
    }

    // Specialized write-heavy operations

    /**
     * Flush all pending writes to main storage.
     */
    public final void flushWrites() {
        writeProcessor.processWrites();
    }

    /**
     * Get write statistics.
     */
    public final long getWriteCount() {
        return writeCount.get();
    }

    /**
     * Get pending write count.
     */
    public final int getPendingWriteCount() {
        return writeBuffer.size() + writeBatcher.size();
    }

    // Shutdown method
    public final void shutdown() {
        // Flush all pending writes
        flushWrites();

        // Shutdown write executor
        writeExecutor.shutdown();
        try {
            if (!writeExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                writeExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            writeExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Versioned entry for write optimization
    private static final class VersionedEntry<V> {
        private final V value;
        private final long version;
        private final long writeTime;

        VersionedEntry(V value, long version) {
            this.value = value;
            this.version = version;
            this.writeTime = System.currentTimeMillis();
        }

        final V getValue() {
            return value;
        }

        final long getVersion() {
            return version;
        }

        final long getWriteTime() {
            return writeTime;
        }
    }

    // Write buffer for immediate consistency
    private static final class WriteBuffer<K, V> {
        private final ConcurrentHashMap<K, V> buffer;
        private final int maxSize;
        private final AtomicLong size;

        WriteBuffer(int maxSize) {
            this.maxSize = maxSize;
            this.buffer = new ConcurrentHashMap<>(maxSize);
            this.size = new AtomicLong(0);
        }

        final V get(K key) {
            return buffer.get(key);
        }

        final void put(K key, V value) {
            if (buffer.put(key, value) == null) {
                size.incrementAndGet();
            }
        }

        final V remove(K key) {
            V removed = buffer.remove(key);
            if (removed != null) {
                size.decrementAndGet();
            }
            return removed;
        }

        final boolean containsKey(K key) {
            return buffer.containsKey(key);
        }

        final int size() {
            return (int) size.get();
        }

        final boolean isFull() {
            return size.get() >= maxSize;
        }

        final void clear() {
            buffer.clear();
            size.set(0);
        }

        final Set<K> keys() {
            return buffer.keySet();
        }

        final Collection<V> values() {
            return buffer.values();
        }

        final Set<Map.Entry<K, V>> entries() {
            return buffer.entrySet();
        }

        final Map<K, V> drainAll() {
            Map<K, V> drained = new java.util.HashMap<>(buffer);
            buffer.clear();
            size.set(0);
            return drained;
        }
    }

    // Write operation for batching
    private static final class WriteOperation<K, V> {
        enum Type {
            PUT, REMOVE
        }

        final K key;
        final V value;
        final Type type;
        final long timestamp;

        WriteOperation(K key, V value, Type type) {
            this.key = key;
            this.value = value;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // Write batcher for efficient processing
    private static final class WriteBatcher<K, V> {
        private final ConcurrentLinkedQueue<WriteOperation<K, V>> operations;
        private final int batchSize;
        private final AtomicLong size;

        WriteBatcher(int batchSize) {
            this.batchSize = batchSize;
            this.operations = new ConcurrentLinkedQueue<>();
            this.size = new AtomicLong(0);
        }

        final void addOperation(WriteOperation<K, V> operation) {
            operations.offer(operation);
            size.incrementAndGet();
        }

        final java.util.List<WriteOperation<K, V>> drainBatch() {
            java.util.List<WriteOperation<K, V>> batch = new java.util.ArrayList<>(batchSize);

            WriteOperation<K, V> operation;
            while (batch.size() < batchSize && (operation = operations.poll()) != null) {
                batch.add(operation);
                size.decrementAndGet();
            }

            return batch;
        }

        final int size() {
            return (int) size.get();
        }

        final void clear() {
            operations.clear();
            size.set(0);
        }
    }

    // Write coalescer for duplicate key optimization
    private static final class WriteCoalescer<K, V> {
        private final ConcurrentHashMap<K, WriteOperation<K, V>> latestOperations;

        WriteCoalescer() {
            this.latestOperations = new ConcurrentHashMap<>();
        }

        final java.util.List<WriteOperation<K, V>> coalesce(java.util.List<WriteOperation<K, V>> operations) {
            latestOperations.clear();

            // Keep only the latest operation for each key
            for (WriteOperation<K, V> operation : operations) {
                WriteOperation<K, V> existing = latestOperations.get(operation.key);
                if (existing == null || operation.timestamp > existing.timestamp) {
                    latestOperations.put(operation.key, operation);
                }
            }

            return new java.util.ArrayList<>(latestOperations.values());
        }

        final void clear() {
            latestOperations.clear();
        }
    }

    // Write processor for asynchronous processing
    private static final class WriteProcessor<K, V> {
        private final WriteHeavyOptimizedCache<K, V> cache;
        private final AtomicLong versionCounter;

        WriteProcessor(WriteHeavyOptimizedCache<K, V> cache) {
            this.cache = cache;
            this.versionCounter = new AtomicLong(0);
        }

        final void processWrites() {
            // Drain write buffer
            Map<K, V> bufferedWrites = cache.writeBuffer.drainAll();

            // Drain write batcher
            java.util.List<WriteOperation<K, V>> batchedOps = cache.writeBatcher.drainBatch();

            // Coalesce operations
            java.util.List<WriteOperation<K, V>> coalescedOps = cache.writeCoalescer.coalesce(batchedOps);

            // Process buffered writes
            for (Map.Entry<K, V> entry : bufferedWrites.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();

                long version = versionCounter.incrementAndGet();
                VersionedEntry<V> versionedEntry = new VersionedEntry<>(value, version);
                cache.data.put(key, versionedEntry);
            }

            // Process coalesced operations
            for (WriteOperation<K, V> operation : coalescedOps) {
                if (operation.type == WriteOperation.Type.PUT) {
                    long version = versionCounter.incrementAndGet();
                    VersionedEntry<V> versionedEntry = new VersionedEntry<>(operation.value, version);
                    cache.data.put(operation.key, versionedEntry);
                } else if (operation.type == WriteOperation.Type.REMOVE) {
                    cache.data.remove(operation.key);
                }
            }
        }
    }
}
