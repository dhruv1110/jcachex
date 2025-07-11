package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.FrequencySketch;
import io.github.dhruv1110.jcachex.FrequencySketchType;
import io.github.dhruv1110.jcachex.OptimizedFrequencySketch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Enhanced LRU eviction strategy with frequency sketch support.
 * <p>
 * This implementation extends the basic LRU strategy by incorporating
 * frequency information to make better eviction decisions. It combines
 * recency (LRU) with frequency tracking to avoid evicting frequently
 * accessed items even if they haven't been accessed recently.
 * </p>
 *
 * <h3>Performance Characteristics:</h3>
 * <ul>
 * <li><strong>Update Operation:</strong> O(1) - constant time access order
 * update</li>
 * <li><strong>Eviction Selection:</strong> O(1) - constant time candidate
 * selection</li>
 * <li><strong>Memory Overhead:</strong> Two pointers per entry plus frequency
 * sketch</li>
 * <li><strong>Thread Safety:</strong> Read-write lock for optimal concurrent
 * performance</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 * @since 1.0.0
 */
public class EnhancedLRUEvictionStrategy<K, V> implements EvictionStrategy<K, V> {

    /**
     * Node in the doubly-linked list representing an entry's position in access
     * order.
     */
    private static class AccessNode<K> {
        K key;
        AccessNode<K> prev;
        AccessNode<K> next;

        AccessNode(K key) {
            this.key = key;
        }
    }

    // Map from key to its position node in the access order list
    private final ConcurrentHashMap<K, AccessNode<K>> nodeMap = new ConcurrentHashMap<>();

    // Doubly-linked list sentinel nodes (head = most recent, tail = least recent)
    private final AccessNode<K> head = new AccessNode<>(null);
    private final AccessNode<K> tail = new AccessNode<>(null);

    // Frequency sketch for tracking access patterns
    private final FrequencySketchType sketchType;
    private final FrequencySketch<K> basicSketch;
    private final OptimizedFrequencySketch<K> optimizedSketch;

    // Thread safety
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    /**
     * Creates an enhanced LRU eviction strategy with default frequency sketch.
     */
    public EnhancedLRUEvictionStrategy() {
        this(FrequencySketchType.BASIC, 1000);
    }

    /**
     * Creates an enhanced LRU eviction strategy with specified frequency sketch
     * type.
     *
     * @param sketchType the type of frequency sketch to use
     * @param capacity   the estimated capacity for frequency sketch sizing
     */
    public EnhancedLRUEvictionStrategy(FrequencySketchType sketchType, long capacity) {
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

        // Initialize empty doubly-linked list
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        writeLock.lock();
        try {
            // Update frequency sketch
            updateFrequencySketch(key);

            AccessNode<K> node = nodeMap.get(key);
            if (node != null) {
                // Move existing node to head (most recent)
                moveToHead(node);
            } else {
                // Create new node and add to head
                node = new AccessNode<>(key);
                nodeMap.put(key, node);
                addToHead(node);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        writeLock.lock();
        try {
            AccessNode<K> node = nodeMap.remove(key);
            if (node != null) {
                removeNode(node);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            nodeMap.clear();
            head.next = tail;
            tail.prev = head;

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

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        readLock.lock();
        try {
            if (sketchType == FrequencySketchType.NONE) {
                // Standard LRU eviction
                AccessNode<K> lru = tail.prev;
                return (lru != head) ? lru.key : null;
            } else {
                // Frequency-aware LRU eviction
                return selectFrequencyAwareCandidate(entries);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Selects eviction candidate using frequency information.
     *
     * @param entries the current cache entries
     * @return the key to evict
     */
    private K selectFrequencyAwareCandidate(Map<K, CacheEntry<V>> entries) {
        // Look at the N least recently used items and pick the one with lowest
        // frequency
        final int candidateCount = Math.min(5, nodeMap.size());

        K bestCandidate = null;
        int lowestFrequency = Integer.MAX_VALUE;

        AccessNode<K> current = tail.prev;
        for (int i = 0; i < candidateCount && current != head; i++) {
            K key = current.key;
            int frequency = getFrequency(key);

            if (frequency < lowestFrequency) {
                lowestFrequency = frequency;
                bestCandidate = key;
            }

            current = current.prev;
        }

        return bestCandidate;
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
    private int getFrequency(K key) {
        if (basicSketch != null) {
            return basicSketch.frequency(key);
        } else if (optimizedSketch != null) {
            return optimizedSketch.frequency(key);
        } else {
            return 0; // No frequency tracking
        }
    }

    /**
     * Add node to the head of the list (most recently used position).
     */
    private void addToHead(AccessNode<K> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * Remove a node from the list.
     */
    private void removeNode(AccessNode<K> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /**
     * Move an existing node to the head (most recently used position).
     */
    private void moveToHead(AccessNode<K> node) {
        removeNode(node);
        addToHead(node);
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
