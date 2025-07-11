package io.github.dhruv1110.jcachex;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A probabilistic frequency sketch based on CountMinSketch for efficient
 * frequency tracking with minimal memory overhead.
 * <p>
 * This implementation uses a 4-bit CountMinSketch to estimate the frequency
 * of cache accesses. It provides constant-time operations for both frequency
 * updates and queries, making it suitable for high-performance caching.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>4-bit Counters:</strong> Balance between accuracy and memory
 * usage</li>
 * <li><strong>Multiple Hash Functions:</strong> Reduce collision
 * probability</li>
 * <li><strong>Minimal Increment:</strong> Only increment smallest counters</li>
 * <li><strong>Aging Support:</strong> Periodic halving of counters</li>
 * <li><strong>Doorkeeper:</strong> Single-bit bloom filter for rare items</li>
 * </ul>
 *
 * <h3>Memory Layout:</h3>
 * Each long value in the table holds 16 4-bit counters (64 bits total).
 * This layout maximizes cache efficiency by keeping related counters
 * in the same cache line.
 *
 * @param <E> the type of elements being tracked
 * @since 1.0.0
 */
public class FrequencySketch<E> {

    // Each long holds 16 4-bit counters (64 bits total)
    private final long[] table;
    private final int blockMask;
    private final int sampleSize;
    private final AtomicLong size;

    // Doorkeeper - single-bit bloom filter for rare items
    private final long[] doorkeeper;
    private final int doorkeeperMask;

    // Constants for hash functions
    private static final long RESET_MASK = 0x7777777777777777L;
    private static final long ONE_MASK = 0x1111111111111111L;

    /**
     * Creates a new frequency sketch with the specified maximum size.
     *
     * @param maximumSize the maximum number of elements to track
     */
    public FrequencySketch(long maximumSize) {
        this.sampleSize = (int) Math.min(maximumSize * 10, Integer.MAX_VALUE >>> 1);
        this.size = new AtomicLong();

        // Calculate table size (power of 2)
        int tableSize = calculateTableSize(maximumSize);
        this.table = new long[tableSize];
        this.blockMask = (tableSize >>> 3) - 1;

        // Initialize doorkeeper (smaller bloom filter)
        int doorkeeperSize = Math.max(1, tableSize >>> 6);
        this.doorkeeper = new long[doorkeeperSize];
        this.doorkeeperMask = doorkeeperSize - 1;
    }

    /**
     * Calculates the optimal table size based on maximum cache size.
     *
     * @param maximumSize the maximum cache size
     * @return the table size (power of 2)
     */
    private static int calculateTableSize(long maximumSize) {
        // Use power of 2 for efficient bit masking
        long tableSize = Math.max(8, Long.highestOneBit(maximumSize * 2L - 1) << 1);
        return (int) Math.min(tableSize, Integer.MAX_VALUE >>> 1);
    }

    /**
     * Records an access to the specified element.
     *
     * @param element the element being accessed
     */
    public void increment(E element) {
        if (element == null) {
            return;
        }

        // Check doorkeeper first (single-bit bloom filter)
        int doorkeeperHash = spread(element.hashCode());
        if (!checkDoorkeeper(doorkeeperHash)) {
            // First time seeing this element, add to doorkeeper
            addToDoorkeeper(doorkeeperHash);
            return;
        }

        // Element has been seen before, increment frequency
        int hash = spread(element.hashCode());
        int secondHash = rehash(hash);

        incrementFrequency(hash, secondHash);

        // Check if we need to perform aging
        if (size.incrementAndGet() >= sampleSize) {
            reset();
        }
    }

    /**
     * Returns the estimated frequency of the specified element.
     *
     * @param element the element to query
     * @return the estimated frequency (0-15)
     */
    public int frequency(E element) {
        if (element == null) {
            return 0;
        }

        // Check doorkeeper first
        int doorkeeperHash = spread(element.hashCode());
        if (!checkDoorkeeper(doorkeeperHash)) {
            return 0;
        }

        int hash = spread(element.hashCode());
        int secondHash = rehash(hash);

        return Math.min(15, getMinFrequency(hash, secondHash));
    }

    /**
     * Checks if the element is in the doorkeeper.
     *
     * @param hash the hash of the element
     * @return true if the element might be present
     */
    private boolean checkDoorkeeper(int hash) {
        int index = hash & doorkeeperMask;
        int bit = (hash >>> 16) & 63;
        return (doorkeeper[index] & (1L << bit)) != 0;
    }

    /**
     * Adds an element to the doorkeeper.
     *
     * @param hash the hash of the element
     */
    private void addToDoorkeeper(int hash) {
        int index = hash & doorkeeperMask;
        int bit = (hash >>> 16) & 63;
        doorkeeper[index] |= (1L << bit);
    }

    /**
     * Increments the frequency counters for the specified hashes.
     * Uses minimal increment strategy - only increments the smallest counters.
     *
     * @param hash       the primary hash
     * @param secondHash the secondary hash
     */
    private void incrementFrequency(int hash, int secondHash) {
        int block = (hash & blockMask) << 3;

        // Calculate the 4 counter positions
        int[] counters = new int[4];
        long[] values = new long[4];

        for (int i = 0; i < 4; i++) {
            int h = secondHash >>> (i << 3);
            int index = (h >>> 1) & 15;
            int offset = h & 1;
            int tableIndex = block + offset + (i << 1);

            counters[i] = (int) ((table[tableIndex] >>> (index << 2)) & 0xfL);
            values[i] = table[tableIndex];
        }

        // Find minimum frequency
        int minFreq = counters[0];
        for (int i = 1; i < 4; i++) {
            minFreq = Math.min(minFreq, counters[i]);
        }

        // Increment only the minimal counters (and not already at max)
        for (int i = 0; i < 4; i++) {
            if (counters[i] == minFreq && counters[i] < 15) {
                int h = secondHash >>> (i << 3);
                int index = (h >>> 1) & 15;
                int offset = h & 1;
                int tableIndex = block + offset + (i << 1);

                // Increment the 4-bit counter
                table[tableIndex] += (1L << (index << 2));
            }
        }
    }

    /**
     * Gets the minimum frequency across all hash positions.
     *
     * @param hash       the primary hash
     * @param secondHash the secondary hash
     * @return the minimum frequency
     */
    private int getMinFrequency(int hash, int secondHash) {
        int block = (hash & blockMask) << 3;
        int minFreq = Integer.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            int h = secondHash >>> (i << 3);
            int index = (h >>> 1) & 15;
            int offset = h & 1;
            int tableIndex = block + offset + (i << 1);

            int freq = (int) ((table[tableIndex] >>> (index << 2)) & 0xfL);
            minFreq = Math.min(minFreq, freq);
        }

        return minFreq;
    }

    /**
     * Resets the frequency counters by halving all values.
     * This aging process keeps the frequency data fresh.
     */
    private void reset() {
        // Only one thread should perform reset
        if (size.compareAndSet(sampleSize, sampleSize >>> 1)) {
            // Halve all counters
            for (int i = 0; i < table.length; i++) {
                table[i] = (table[i] >>> 1) & RESET_MASK;
            }

            // Reset doorkeeper periodically
            if (ThreadLocalRandom.current().nextInt(8) == 0) {
                for (int i = 0; i < doorkeeper.length; i++) {
                    doorkeeper[i] = 0;
                }
            }
        }
    }

    /**
     * Applies a supplemental hash function to defend against poor quality hash.
     *
     * @param hash the original hash
     * @return the spread hash
     */
    private static int spread(int hash) {
        hash ^= hash >>> 17;
        hash *= 0xed5ad4bb;
        hash ^= hash >>> 11;
        hash *= 0xac4c1b51;
        hash ^= hash >>> 15;
        return hash;
    }

    /**
     * Applies another round of hashing for additional randomization.
     *
     * @param hash the spread hash
     * @return the rehashed value
     */
    private static int rehash(int hash) {
        hash *= 0x31848bab;
        hash ^= hash >>> 14;
        return hash;
    }

    /**
     * Checks if the sketch has been initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return table.length > 0;
    }

    /**
     * Returns the current sample size.
     *
     * @return the sample size
     */
    public int sampleSize() {
        return sampleSize;
    }

    /**
     * Returns the current size counter.
     *
     * @return the current size
     */
    public long size() {
        return size.get();
    }

    /**
     * Clears all frequency data.
     */
    public void clear() {
        size.set(0);
        for (int i = 0; i < table.length; i++) {
            table[i] = 0;
        }
        for (int i = 0; i < doorkeeper.length; i++) {
            doorkeeper[i] = 0;
        }
    }

    /**
     * Returns the current capacity of the frequency sketch.
     *
     * @return the capacity
     */
    public int getCapacity() {
        return table.length * 16; // Each long holds 16 counters
    }

    /**
     * Ensures the frequency sketch can handle at least the specified capacity.
     * This is a no-op since the capacity is fixed at construction time.
     *
     * @param capacity the desired capacity
     */
    public void ensureCapacity(int capacity) {
        // No-op: capacity is fixed at construction time
        // This method exists for compatibility
    }

}
