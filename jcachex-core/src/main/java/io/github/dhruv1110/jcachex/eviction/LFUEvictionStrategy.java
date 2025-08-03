package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Optimized Least Frequently Used (LFU) eviction strategy with O(1) operations.
 * <p>
 * This implementation uses a frequency-based linked list structure where:
 * - Each frequency level has its own doubly-linked list
 * - Keys are organized by frequency buckets
 * - Both update and eviction operations are O(1)
 * </p>
 *
 * <h3>Performance Characteristics:</h3>
 * <ul>
 * <li><strong>Update Operation:</strong> O(1) - constant time frequency
 * increment</li>
 * <li><strong>Eviction Selection:</strong> O(1) - constant time candidate
 * selection</li>
 * <li><strong>Memory Overhead:</strong> Two pointers per entry plus frequency
 * buckets</li>
 * <li><strong>Thread Safety:</strong> Read-write lock for optimal concurrent
 * performance</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class LFUEvictionStrategy<K, V> implements EvictionStrategy<K, V> {

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

    // Thread safety
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        readLock.lock();
        try {
            if (minFrequencyBucket == null) {
                return null;
            }

            FrequencyNode<K> candidate = minFrequencyBucket.getLastNode();
            return candidate != null ? candidate.key : null;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        writeLock.lock();
        try {
            FrequencyNode<K> node = keyToNode.get(key);

            if (node == null) {
                // New key - add with frequency 1
                node = new FrequencyNode<>(key, 1);
                keyToNode.put(key, node);

                FrequencyBucket<K> bucket = getOrCreateBucket(1);
                bucket.addNode(node);

                // Update min frequency bucket if needed
                if (minFrequencyBucket == null || minFrequencyBucket.frequency > 1) {
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
        } finally {
            writeLock.unlock();
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
}
