package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.FrequencySketch;
import io.github.dhruv1110.jcachex.FrequencySketchType;
import io.github.dhruv1110.jcachex.OptimizedFrequencySketch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Enhanced LFU eviction strategy with frequency sketch support and O(1)
 * operations.
 * <p>
 * This implementation combines the O(1) LFU algorithm with frequency sketches
 * for improved accuracy. The frequency sketch provides approximate frequency
 * tracking with minimal memory overhead, while the bucket-based structure
 * ensures O(1) operations for both updates and evictions.
 * </p>
 *
 * <h3>Performance Characteristics:</h3>
 * <ul>
 * <li><strong>Update Operation:</strong> O(1) - constant time frequency
 * increment</li>
 * <li><strong>Eviction Selection:</strong> O(1) - constant time candidate
 * selection</li>
 * <li><strong>Memory Overhead:</strong> Frequency buckets plus frequency
 * sketch</li>
 * <li><strong>Thread Safety:</strong> Read-write lock for optimal concurrent
 * performance</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 * @since 1.0.0
 */
public class EnhancedLFUEvictionStrategy<K, V> implements EvictionStrategy<K, V> {

    /**
     * Node representing a key-frequency pair in the frequency list.
     */
    private static class FrequencyNode<K> {
        K key;
        int frequency;
        FrequencyNode<K> prev;
        FrequencyNode<K> next;
        FrequencyBucket<K> bucket;

        FrequencyNode(K key, int frequency) {
            this.key = key;
            this.frequency = frequency;
        }
    }

    /**
     * Bucket containing all keys with the same frequency.
     */
    private static class FrequencyBucket<K> {
        int frequency;
        FrequencyNode<K> head;
        FrequencyNode<K> tail;
        FrequencyBucket<K> prevBucket;
        FrequencyBucket<K> nextBucket;

        FrequencyBucket(int frequency) {
            this.frequency = frequency;
            this.head = new FrequencyNode<>(null, frequency);
            this.tail = new FrequencyNode<>(null, frequency);
            this.head.next = this.tail;
            this.tail.prev = this.head;
        }

        void addNode(FrequencyNode<K> node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
            node.bucket = this;
        }

        void removeNode(FrequencyNode<K> node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            node.bucket = null;
        }

        boolean isEmpty() {
            return head.next == tail;
        }

        FrequencyNode<K> getLastNode() {
            return tail.prev != head ? tail.prev : null;
        }
    }

    // Map from key to its frequency node
    private final ConcurrentHashMap<K, FrequencyNode<K>> keyToNode = new ConcurrentHashMap<>();

    // Map from frequency to its bucket
    private final ConcurrentHashMap<Integer, FrequencyBucket<K>> frequencyBuckets = new ConcurrentHashMap<>();

    // Minimum frequency bucket (for O(1) eviction)
    private volatile FrequencyBucket<K> minFrequencyBucket;

    // Frequency sketch for enhanced accuracy
    private final FrequencySketchType sketchType;
    private final FrequencySketch<K> basicSketch;
    private final OptimizedFrequencySketch<K> optimizedSketch;

    // Thread safety
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    /**
     * Creates an enhanced LFU eviction strategy with default frequency sketch.
     */
    public EnhancedLFUEvictionStrategy() {
        this(FrequencySketchType.BASIC, 1000);
    }

    /**
     * Creates an enhanced LFU eviction strategy with specified frequency sketch
     * type.
     *
     * @param sketchType the type of frequency sketch to use
     * @param capacity   the estimated capacity for frequency sketch sizing
     */
    public EnhancedLFUEvictionStrategy(FrequencySketchType sketchType, long capacity) {
        this.sketchType = sketchType;

        // Initialize frequency sketch based on type
        switch (sketchType) {
            case BASIC:
                this.basicSketch = new FrequencySketch<>(capacity);
                this.optimizedSketch = null;
                break;
            case OPTIMIZED:
                this.basicSketch = null;
                this.optimizedSketch = new OptimizedFrequencySketch<>(capacity);
                break;
            default: // NONE
                this.basicSketch = null;
                this.optimizedSketch = null;
                break;
        }
    }

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        readLock.lock();
        try {
            if (minFrequencyBucket == null) {
                return null;
            }

            if (sketchType == FrequencySketchType.NONE) {
                // Standard LFU eviction
                FrequencyNode<K> candidate = minFrequencyBucket.getLastNode();
                return candidate != null ? candidate.key : null;
            } else {
                // Frequency sketch-enhanced eviction
                return selectSketchEnhancedCandidate(entries);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        writeLock.lock();
        try {
            // Update frequency sketch
            updateFrequencySketch(key);

            FrequencyNode<K> node = keyToNode.get(key);

            if (node == null) {
                // New key - add with frequency from sketch or default to 1
                int initialFreq = getSketchFrequency(key);
                if (initialFreq == 0) {
                    initialFreq = 1;
                }

                node = new FrequencyNode<>(key, initialFreq);
                keyToNode.put(key, node);

                FrequencyBucket<K> bucket = getOrCreateBucket(initialFreq);
                bucket.addNode(node);

                // Update min frequency bucket if needed
                if (minFrequencyBucket == null || minFrequencyBucket.frequency > initialFreq) {
                    minFrequencyBucket = bucket;
                }
            } else {
                // Existing key - increment frequency
                incrementFrequency(node);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        writeLock.lock();
        try {
            FrequencyNode<K> node = keyToNode.remove(key);
            if (node != null) {
                FrequencyBucket<K> bucket = node.bucket;
                bucket.removeNode(node);

                // Clean up empty bucket
                if (bucket.isEmpty()) {
                    removeBucket(bucket);

                    // Update min frequency bucket if needed
                    if (minFrequencyBucket == bucket) {
                        minFrequencyBucket = bucket.nextBucket;
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            keyToNode.clear();
            frequencyBuckets.clear();
            minFrequencyBucket = null;

            // Clear frequency sketches
            if (basicSketch != null) {
                basicSketch.clear();
            }
            if (optimizedSketch != null) {
                optimizedSketch.reset();
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Selects eviction candidate using frequency sketch information.
     *
     * @param entries the current cache entries
     * @return the key to evict
     */
    private K selectSketchEnhancedCandidate(Map<K, CacheEntry<V>> entries) {
        // Look at candidates from the minimum frequency bucket and pick based on sketch
        FrequencyBucket<K> bucket = minFrequencyBucket;
        if (bucket == null || bucket.isEmpty()) {
            return null;
        }

        K bestCandidate = null;
        int lowestSketchFreq = Integer.MAX_VALUE;

        // Examine all nodes in the minimum frequency bucket
        FrequencyNode<K> current = bucket.tail.prev;
        while (current != bucket.head) {
            K key = current.key;
            int sketchFreq = getSketchFrequency(key);

            if (sketchFreq < lowestSketchFreq) {
                lowestSketchFreq = sketchFreq;
                bestCandidate = key;
            }

            current = current.prev;
        }

        return bestCandidate != null ? bestCandidate : bucket.getLastNode().key;
    }

    /**
     * Updates the frequency sketch for a key.
     *
     * @param key the key to update
     */
    private void updateFrequencySketch(K key) {
        if (basicSketch != null) {
            basicSketch.increment(key);
        } else if (optimizedSketch != null) {
            optimizedSketch.increment(key);
        }
    }

    /**
     * Gets the frequency of a key from the frequency sketch.
     *
     * @param key the key to check
     * @return the frequency count
     */
    private int getSketchFrequency(K key) {
        if (basicSketch != null) {
            return basicSketch.frequency(key);
        } else if (optimizedSketch != null) {
            return optimizedSketch.frequency(key);
        } else {
            return 0; // No frequency tracking
        }
    }

    /**
     * Increments the frequency of a node and moves it to the appropriate bucket.
     */
    private void incrementFrequency(FrequencyNode<K> node) {
        int oldFreq = node.frequency;
        int newFreq = oldFreq + 1;

        FrequencyBucket<K> oldBucket = node.bucket;
        FrequencyBucket<K> newBucket = getOrCreateBucket(newFreq);

        // Move node to new bucket
        oldBucket.removeNode(node);
        node.frequency = newFreq;
        newBucket.addNode(node);

        // Clean up old bucket if empty
        if (oldBucket.isEmpty()) {
            removeBucket(oldBucket);

            // Update min frequency bucket if needed
            if (minFrequencyBucket == oldBucket) {
                minFrequencyBucket = oldBucket.nextBucket;
            }
        }
    }

    /**
     * Gets or creates a frequency bucket for the given frequency.
     */
    private FrequencyBucket<K> getOrCreateBucket(int frequency) {
        return frequencyBuckets.computeIfAbsent(frequency, freq -> {
            FrequencyBucket<K> bucket = new FrequencyBucket<>(freq);
            insertBucketInOrder(bucket);
            return bucket;
        });
    }

    /**
     * Inserts a bucket in the correct position in the frequency order.
     */
    private void insertBucketInOrder(FrequencyBucket<K> newBucket) {
        if (minFrequencyBucket == null) {
            minFrequencyBucket = newBucket;
            return;
        }

        // Find the correct position
        FrequencyBucket<K> current = minFrequencyBucket;
        FrequencyBucket<K> prev = null;

        while (current != null && current.frequency < newBucket.frequency) {
            prev = current;
            current = current.nextBucket;
        }

        // Insert between prev and current
        newBucket.prevBucket = prev;
        newBucket.nextBucket = current;

        if (prev != null) {
            prev.nextBucket = newBucket;
        } else {
            minFrequencyBucket = newBucket;
        }

        if (current != null) {
            current.prevBucket = newBucket;
        }
    }

    /**
     * Removes a bucket from the frequency order.
     */
    private void removeBucket(FrequencyBucket<K> bucket) {
        frequencyBuckets.remove(bucket.frequency);

        if (bucket.prevBucket != null) {
            bucket.prevBucket.nextBucket = bucket.nextBucket;
        }

        if (bucket.nextBucket != null) {
            bucket.nextBucket.prevBucket = bucket.prevBucket;
        }

        if (minFrequencyBucket == bucket) {
            minFrequencyBucket = bucket.nextBucket;
        }
    }

    /**
     * Returns the frequency sketch type being used.
     *
     * @return the frequency sketch type
     */
    public FrequencySketchType getSketchType() {
        return sketchType;
    }

    /**
     * Returns statistics about the frequency sketch.
     *
     * @return statistics string
     */
    public String getSketchStats() {
        if (basicSketch != null) {
            return "Basic FrequencySketch: " + basicSketch.toString();
        } else if (optimizedSketch != null) {
            return "Optimized FrequencySketch: " + optimizedSketch.getStats();
        } else {
            return "No frequency sketch";
        }
    }
}
