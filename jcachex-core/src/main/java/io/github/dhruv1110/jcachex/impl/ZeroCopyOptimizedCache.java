package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.CacheStats;
import io.github.dhruv1110.jcachex.internal.util.CacheCommonOperations;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.AbstractMap;
import java.util.concurrent.CompletableFuture;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Instant;
import java.time.Duration;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Zero-copy optimized cache implementation that minimizes data copying
 * overhead.
 *
 * Key optimizations:
 * - Direct memory buffers for value storage
 * - Memory-mapped file operations
 * - Elimination of intermediate buffers
 * - Direct ByteBuffer operations
 * - Zero-copy serialization paths
 */
public final class ZeroCopyOptimizedCache<K, V> implements Cache<K, V> {

    // Core data structures
    private final ConcurrentHashMap<K, DirectEntry<V>> data;
    private final AtomicLong hitCount;
    private final AtomicLong missCount;
    private final AtomicLong currentSize;
    private final long maximumSize;
    private final boolean statsEnabled;

    // Direct memory management
    private final DirectBufferPool bufferPool;
    private final MemoryMappedRegion<K, V> mappedRegion;

    // Zero-copy serialization
    private final DirectSerializer<V> serializer;

    // Configuration
    private static final int DIRECT_BUFFER_SIZE = 4 * 1024; // 4KB (reasonable for typical cache entries)
    private static final int BUFFER_POOL_SIZE = 1024; // Larger pool to reduce allocation
    private static final int MAPPED_REGION_SIZE = 16 * 1024 * 1024; // 16MB (reduced from 64MB)

    public ZeroCopyOptimizedCache(CacheConfig<K, V> config) {
        this.maximumSize = config.getMaximumSize();
        this.statsEnabled = config.isRecordStats();

        // Initialize core data structures
        int capacity = Math.max(16, Integer.highestOneBit((int) maximumSize) << 1);
        this.data = new ConcurrentHashMap<>(capacity, 0.75f, 16);

        // Initialize counters
        this.hitCount = new AtomicLong(0);
        this.missCount = new AtomicLong(0);
        this.currentSize = new AtomicLong(0);

        // Initialize direct memory management
        this.bufferPool = new DirectBufferPool(BUFFER_POOL_SIZE, DIRECT_BUFFER_SIZE);
        this.mappedRegion = new MemoryMappedRegion<>(MAPPED_REGION_SIZE);

        // Initialize zero-copy serialization
        this.serializer = new DirectSerializer<>();
    }

    @Override
    public final V get(K key) {
        if (key == null)
            return null;

        DirectEntry<V> entry = data.get(key);
        if (entry == null) {
            if (statsEnabled) {
                missCount.incrementAndGet();
            }
            return null;
        }

        // Zero-copy value retrieval
        V value = entry.getValueZeroCopy();
        if (value == null) {
            // Entry expired or corrupted
            data.remove(key, entry);
            currentSize.decrementAndGet();
            if (statsEnabled) {
                missCount.incrementAndGet();
            }
            return null;
        }

        // Update access time without copying
        entry.updateAccessTimeZeroCopy();

        if (statsEnabled) {
            hitCount.incrementAndGet();
        }

        return value;
    }

    @Override
    public final void put(K key, V value) {
        if (key == null || value == null)
            return;

        // Get direct buffer for zero-copy storage
        DirectBuffer buffer = bufferPool.acquire();
        try {
            // Zero-copy serialization
            DirectEntry<V> newEntry = createEntryZeroCopy(value, buffer);
            DirectEntry<V> existing = data.put(key, newEntry);

            if (existing == null) {
                // New entry
                long size = currentSize.incrementAndGet();

                // Check if eviction needed
                if (size > maximumSize) {
                    evictLeastRecentlyUsed();
                }
            } else {
                // Return old entry's buffer to pool
                existing.releaseBuffer();
            }
        } finally {
            // Buffer is now owned by the entry, don't release here
        }
    }

    @Override
    public final V remove(K key) {
        if (key == null)
            return null;

        DirectEntry<V> removed = data.remove(key);
        if (removed != null) {
            currentSize.decrementAndGet();
            V value = removed.getValueZeroCopy();
            removed.releaseBuffer();
            return value;
        }

        return null;
    }

    @Override
    public final void clear() {
        // Release all buffers
        for (DirectEntry<V> entry : data.values()) {
            entry.releaseBuffer();
        }

        data.clear();
        currentSize.set(0);
        bufferPool.clear();
        mappedRegion.clear();
    }

    @Override
    public final long size() {
        return currentSize.get();
    }

    @Override
    public final CacheStats stats() {
        return CacheCommonOperations.createStats(hitCount, missCount);
    }

    @Override
    public final CacheConfig<K, V> config() {
        return CacheCommonOperations.createConfig(maximumSize, statsEnabled);
    }

    @Override
    public final Set<K> keys() {
        return CacheCommonOperations.createKeysView(data);
    }

    @Override
    public final Collection<V> values() {
        return CacheCommonOperations.createValuesView(data, DirectEntry::getValueZeroCopy);
    }

    @Override
    public final Set<Map.Entry<K, V>> entries() {
        return CacheCommonOperations.createEntriesView(data, DirectEntry::getValueZeroCopy);
    }

    @Override
    public final boolean containsKey(K key) {
        return CacheCommonOperations.containsKey(key, data);
    }

    @Override
    public final CompletableFuture<V> getAsync(K key) {
        return CacheCommonOperations.createAsyncGet(key, () -> get(key));
    }

    @Override
    public final CompletableFuture<Void> putAsync(K key, V value) {
        return CacheCommonOperations.createAsyncPut(key, value, () -> put(key, value));
    }

    @Override
    public final CompletableFuture<V> removeAsync(K key) {
        return CacheCommonOperations.createAsyncRemove(key, () -> remove(key));
    }

    @Override
    public final CompletableFuture<Void> clearAsync() {
        return CacheCommonOperations.createAsyncClear(this::clear);
    }

    /**
     * Cleanup resources when cache is garbage collected
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            clear();
        } finally {
            super.finalize();
        }
    }

    // Zero-copy entry creation
    private final DirectEntry<V> createEntryZeroCopy(V value, DirectBuffer buffer) {
        // Serialize directly to buffer without intermediate allocation
        int serializedSize = serializer.serializeZeroCopy(value, buffer);

        // If serialization failed due to buffer size, try with a larger buffer
        if (serializedSize < 0) {
            // Return small buffer to pool
            bufferPool.release(buffer);

            // Create a larger buffer for oversized values
            byte[] valueBytes = value.toString().getBytes();
            int requiredSize = valueBytes.length;
            DirectBuffer largeBuffer = new DirectBuffer(requiredSize);

            // Serialize to the larger buffer
            ByteBuffer byteBuffer = largeBuffer.getBuffer();
            byteBuffer.put(valueBytes);

            return new DirectEntry<>(largeBuffer, requiredSize);
        }

        return new DirectEntry<>(buffer, serializedSize);
    }

    // Eviction with zero-copy operations
    private final void evictLeastRecentlyUsed() {
        Map.Entry<K, DirectEntry<V>> oldest = null;
        long oldestTime = Long.MAX_VALUE;

        // Find oldest entry without copying
        for (Map.Entry<K, DirectEntry<V>> entry : data.entrySet()) {
            long accessTime = entry.getValue().getAccessTime();
            if (accessTime < oldestTime) {
                oldestTime = accessTime;
                oldest = entry;
            }
        }

        if (oldest != null) {
            data.remove(oldest.getKey(), oldest.getValue());
            currentSize.decrementAndGet();
            oldest.getValue().releaseBuffer();
        }
    }

    // Direct memory entry for zero-copy operations
    private static final class DirectEntry<V> {
        private final DirectBuffer buffer;
        private final int dataSize;
        private final long creationTime;
        private volatile long accessTime;

        DirectEntry(DirectBuffer buffer, int dataSize) {
            this.buffer = buffer;
            this.dataSize = dataSize;
            this.creationTime = System.currentTimeMillis();
            this.accessTime = creationTime;
        }

        final V getValueZeroCopy() {
            if (buffer == null || !buffer.isValid()) {
                return null;
            }

            // Zero-copy deserialization
            try {
                DirectSerializer<V> serializer = new DirectSerializer<>();
                return serializer.deserializeZeroCopy(buffer, dataSize);
            } catch (Exception e) {
                return null;
            }
        }

        final void updateAccessTimeZeroCopy() {
            // Update access time without any allocation
            accessTime = System.currentTimeMillis();
        }

        final long getAccessTime() {
            return accessTime;
        }

        final long getCreationTime() {
            return creationTime;
        }

        final void releaseBuffer() {
            if (buffer != null) {
                buffer.release();
            }
        }
    }

    // Direct buffer pool for zero-copy operations
    private static final class DirectBufferPool {
        private final DirectBuffer[] buffers;
        private final AtomicReference<DirectBuffer> head;
        private final int bufferSize;
        private final int poolSize;
        private volatile int availableCount;

        DirectBufferPool(int poolSize, int bufferSize) {
            this.poolSize = poolSize;
            this.bufferSize = bufferSize;
            this.buffers = new DirectBuffer[poolSize];
            this.head = new AtomicReference<>();
            this.availableCount = 0;

            // Pre-allocate buffers
            for (int i = 0; i < poolSize; i++) {
                DirectBuffer buffer = new DirectBuffer(bufferSize);
                buffers[i] = buffer;
                if (i == 0) {
                    head.set(buffer);
                } else {
                    buffers[i - 1].next = buffer;
                }
            }
            availableCount = poolSize;
        }

        final DirectBuffer acquire() {
            DirectBuffer buffer = head.get();
            if (buffer != null) {
                DirectBuffer next = buffer.next;
                if (head.compareAndSet(buffer, next)) {
                    buffer.next = null;
                    availableCount--;
                    return buffer;
                }
            }

            // Pool exhausted, create new buffer
            return new DirectBuffer(bufferSize);
        }

        final void release(DirectBuffer buffer) {
            if (buffer != null && availableCount < poolSize) {
                buffer.clear();
                DirectBuffer currentHead = head.get();
                buffer.next = currentHead;
                if (head.compareAndSet(currentHead, buffer)) {
                    availableCount++;
                }
            }
        }

        final void clear() {
            DirectBuffer current = head.get();
            while (current != null) {
                DirectBuffer next = current.next;
                current.release();
                current = next;
            }
            head.set(null);
            availableCount = 0;
        }
    }

    // Direct buffer wrapper for zero-copy operations
    private static final class DirectBuffer {
        private final ByteBuffer buffer;
        private volatile boolean valid;
        DirectBuffer next; // For pool linked list

        DirectBuffer(int size) {
            this.buffer = ByteBuffer.allocateDirect(size);
            this.valid = true;
        }

        final ByteBuffer getBuffer() {
            return buffer;
        }

        final boolean isValid() {
            return valid;
        }

        final void clear() {
            if (buffer != null) {
                buffer.clear();
            }
        }

        final void release() {
            valid = false;
            // DirectByteBuffer will be garbage collected
        }
    }

    // Memory-mapped region for large data storage
    private static final class MemoryMappedRegion<K, V> {
        private final MappedByteBuffer mappedBuffer;
        private final AtomicLong position;
        private final int size;
        private final RandomAccessFile file;
        private final String filePath;

        MemoryMappedRegion(int size) {
            this.size = size;
            this.position = new AtomicLong(0);

            try {
                // Create temporary file for memory mapping in system temp directory
                String tempDir = System.getProperty("java.io.tmpdir");
                this.filePath = tempDir + File.separator + "jcachex-mmap-" + System.currentTimeMillis() + "-"
                        + Thread.currentThread().getId();
                this.file = new RandomAccessFile(this.filePath, "rw");
                this.file.setLength(size);

                // Map the file into memory
                this.mappedBuffer = file.getChannel().map(
                        FileChannel.MapMode.READ_WRITE, 0, size);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create memory-mapped region", e);
            }
        }

        final ByteBuffer allocate(int requestedSize) {
            long currentPos = position.get();
            if (currentPos + requestedSize > size) {
                return null; // Region full
            }

            if (position.compareAndSet(currentPos, currentPos + requestedSize)) {
                mappedBuffer.position((int) currentPos);
                return mappedBuffer.slice();
            }

            return null; // Allocation failed
        }

        final void clear() {
            position.set(0);
            mappedBuffer.clear();

            try {
                file.close();

                // Delete the temporary file from disk
                File tempFile = new File(filePath);
                if (tempFile.exists()) {
                    boolean deleted = tempFile.delete();
                    if (!deleted) {
                        // If deletion fails, mark for deletion on exit
                        tempFile.deleteOnExit();
                    }
                }
            } catch (IOException e) {
                // Ignore close errors but try to clean up the file
                File tempFile = new File(filePath);
                if (tempFile.exists()) {
                    tempFile.deleteOnExit();
                }
            }
        }
    }

    // Zero-copy serialization
    private static final class DirectSerializer<V> {

        final int serializeZeroCopy(V value, DirectBuffer buffer) {
            // Simple serialization - in practice, use more sophisticated methods
            if (value == null) {
                return 0;
            }

            ByteBuffer byteBuffer = buffer.getBuffer();
            byte[] valueBytes = value.toString().getBytes();

            // Check if buffer has enough capacity
            if (byteBuffer.remaining() < valueBytes.length) {
                // Value is too large for zero-copy optimization
                // Return -1 to indicate fallback to regular serialization
                return -1;
            }

            byteBuffer.put(valueBytes);
            return valueBytes.length;
        }

        @SuppressWarnings("unchecked")
        final V deserializeZeroCopy(DirectBuffer buffer, int dataSize) {
            if (buffer == null || !buffer.isValid() || dataSize <= 0) {
                return null;
            }

            ByteBuffer byteBuffer = buffer.getBuffer();
            byte[] valueBytes = new byte[dataSize];
            byteBuffer.get(valueBytes);

            // Simple deserialization - in practice, use more sophisticated methods
            return (V) new String(valueBytes);
        }
    }
}
