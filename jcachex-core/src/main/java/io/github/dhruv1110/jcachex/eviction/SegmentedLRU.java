package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Segmented LRU implementation that divides the LRU into protected and
 * probationary segments.
 * <p>
 * This implementation is used as part of the Window TinyLFU strategy to provide
 * better
 * performance characteristics by separating frequently accessed items
 * (protected) from
 * less frequently accessed items (probationary).
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Protected Segment:</strong> 80% of capacity for high-frequency
 * items</li>
 * <li><strong>Probationary Segment:</strong> 20% of capacity for
 * new/low-frequency items</li>
 * <li><strong>Promotion:</strong> Items can be promoted from probationary to
 * protected</li>
 * <li><strong>Demotion:</strong> Items can be demoted from protected to
 * probationary</li>
 * <li><strong>Thread Safety:</strong> Uses read-write locks for concurrent
 * access</li>
 * </ul>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @since 1.0.0
 */
public class SegmentedLRU<K, V> {

    private final int totalCapacity;
    private final int protectedCapacity;
    private final int probationaryCapacity;

    // Doubly linked list nodes for LRU ordering
    private final Node<K, V> protectedHead;
    private final Node<K, V> protectedTail;
    private final Node<K, V> probationaryHead;
    private final Node<K, V> probationaryTail;

    // Fast lookup for nodes
    private final ConcurrentHashMap<K, Node<K, V>> nodeMap;

    // Locks for thread safety
    private final ReadWriteLock protectedLock;
    private final ReadWriteLock probationaryLock;

    // Size tracking
    private volatile int protectedSize;
    private volatile int probationarySize;

    /**
     * Creates a new segmented LRU with the specified capacity.
     *
     * @param totalCapacity the total capacity of the segmented LRU
     */
    public SegmentedLRU(int totalCapacity) {
        this.totalCapacity = totalCapacity;

        // 80% protected, 20% probationary (following Caffeine's design)
        this.protectedCapacity = Math.max(1, (int) (totalCapacity * 0.8));
        this.probationaryCapacity = Math.max(1, totalCapacity - protectedCapacity);

        // Initialize sentinel nodes
        this.protectedHead = new Node<>(null, null);
        this.protectedTail = new Node<>(null, null);
        this.probationaryHead = new Node<>(null, null);
        this.probationaryTail = new Node<>(null, null);

        // Link sentinel nodes
        protectedHead.next = protectedTail;
        protectedTail.prev = protectedHead;
        probationaryHead.next = probationaryTail;
        probationaryTail.prev = probationaryHead;

        // Initialize data structures
        this.nodeMap = new ConcurrentHashMap<>();
        this.protectedLock = new ReentrantReadWriteLock();
        this.probationaryLock = new ReentrantReadWriteLock();

        this.protectedSize = 0;
        this.probationarySize = 0;
    }

    /**
     * Records an access to a key, potentially promoting it between segments.
     *
     * @param key   the key being accessed
     * @param entry the cache entry
     */
    public void recordAccess(K key, CacheEntry<V> entry) {
        Node<K, V> node = nodeMap.get(key);

        if (node == null) {
            // New entry, add to probationary segment
            addToProbationary(key, entry);
        } else {
            // Existing entry, move to front and potentially promote
            if (node.segment == Segment.PROBATIONARY) {
                moveToFrontProbationary(node);
                // Consider promotion based on access frequency
                if (shouldPromote(key, entry)) {
                    promoteToProtected(node);
                }
            } else {
                moveToFrontProtected(node);
            }
        }
    }

    /**
     * Selects a victim for eviction from the appropriate segment.
     *
     * @return the key to evict, or null if no victim found
     */
    public K selectVictim() {
        // First try probationary segment (prefer evicting less valuable items)
        if (probationarySize > 0) {
            probationaryLock.readLock().lock();
            try {
                Node<K, V> victim = probationaryTail.prev;
                if (victim != probationaryHead) {
                    return victim.key;
                }
            } finally {
                probationaryLock.readLock().unlock();
            }
        }

        // If probationary is empty, try protected segment
        if (protectedSize > 0) {
            protectedLock.readLock().lock();
            try {
                Node<K, V> victim = protectedTail.prev;
                if (victim != protectedHead) {
                    return victim.key;
                }
            } finally {
                protectedLock.readLock().unlock();
            }
        }

        return null;
    }

    /**
     * Removes a key from the segmented LRU.
     *
     * @param key the key to remove
     */
    public void remove(K key) {
        Node<K, V> node = nodeMap.remove(key);
        if (node != null) {
            if (node.segment == Segment.PROTECTED) {
                removeFromProtected(node);
            } else {
                removeFromProbationary(node);
            }
        }
    }

    /**
     * Clears all entries from the segmented LRU.
     */
    public void clear() {
        protectedLock.writeLock().lock();
        try {
            probationaryLock.writeLock().lock();
            try {
                nodeMap.clear();

                // Reset protected segment
                protectedHead.next = protectedTail;
                protectedTail.prev = protectedHead;
                protectedSize = 0;

                // Reset probationary segment
                probationaryHead.next = probationaryTail;
                probationaryTail.prev = probationaryHead;
                probationarySize = 0;

            } finally {
                probationaryLock.writeLock().unlock();
            }
        } finally {
            protectedLock.writeLock().unlock();
        }
    }

    /**
     * Returns the current size of the segmented LRU.
     *
     * @return the total number of entries
     */
    public int size() {
        return protectedSize + probationarySize;
    }

    /**
     * Returns performance statistics.
     *
     * @return performance statistics string
     */
    public String getStats() {
        return String.format(
                "SegmentedLRU Stats: Protected=%d/%d, Probationary=%d/%d, Total=%d/%d",
                protectedSize, protectedCapacity,
                probationarySize, probationaryCapacity,
                size(), totalCapacity);
    }

    // Private helper methods

    private void addToProbationary(K key, CacheEntry<V> entry) {
        probationaryLock.writeLock().lock();
        try {
            // Check if we need to evict from probationary
            if (probationarySize >= probationaryCapacity) {
                evictFromProbationary();
            }

            Node<K, V> node = new Node<>(key, entry);
            node.segment = Segment.PROBATIONARY;
            nodeMap.put(key, node);

            addToHeadProbationary(node);
            probationarySize++;

        } finally {
            probationaryLock.writeLock().unlock();
        }
    }

    private void promoteToProtected(Node<K, V> node) {
        probationaryLock.writeLock().lock();
        try {
            protectedLock.writeLock().lock();
            try {
                // Remove from probationary
                removeNodeFromList(node);
                probationarySize--;

                // Check if we need to evict from protected
                if (protectedSize >= protectedCapacity) {
                    evictFromProtected();
                }

                // Add to protected
                node.segment = Segment.PROTECTED;
                addToHeadProtected(node);
                protectedSize++;

            } finally {
                protectedLock.writeLock().unlock();
            }
        } finally {
            probationaryLock.writeLock().unlock();
        }
    }

    private void moveToFrontProtected(Node<K, V> node) {
        protectedLock.writeLock().lock();
        try {
            removeNodeFromList(node);
            addToHeadProtected(node);
        } finally {
            protectedLock.writeLock().unlock();
        }
    }

    private void moveToFrontProbationary(Node<K, V> node) {
        probationaryLock.writeLock().lock();
        try {
            removeNodeFromList(node);
            addToHeadProbationary(node);
        } finally {
            probationaryLock.writeLock().unlock();
        }
    }

    private void evictFromProbationary() {
        Node<K, V> victim = probationaryTail.prev;
        if (victim != probationaryHead) {
            nodeMap.remove(victim.key);
            removeNodeFromList(victim);
            probationarySize--;
        }
    }

    private void evictFromProtected() {
        Node<K, V> victim = protectedTail.prev;
        if (victim != protectedHead) {
            nodeMap.remove(victim.key);
            removeNodeFromList(victim);
            protectedSize--;
        }
    }

    private void removeFromProtected(Node<K, V> node) {
        protectedLock.writeLock().lock();
        try {
            removeNodeFromList(node);
            protectedSize--;
        } finally {
            protectedLock.writeLock().unlock();
        }
    }

    private void removeFromProbationary(Node<K, V> node) {
        probationaryLock.writeLock().lock();
        try {
            removeNodeFromList(node);
            probationarySize--;
        } finally {
            probationaryLock.writeLock().unlock();
        }
    }

    private void addToHeadProtected(Node<K, V> node) {
        node.next = protectedHead.next;
        node.prev = protectedHead;
        protectedHead.next.prev = node;
        protectedHead.next = node;
    }

    private void addToHeadProbationary(Node<K, V> node) {
        node.next = probationaryHead.next;
        node.prev = probationaryHead;
        probationaryHead.next.prev = node;
        probationaryHead.next = node;
    }

    private void removeNodeFromList(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private boolean shouldPromote(K key, CacheEntry<V> entry) {
        // Simple promotion strategy: promote if accessed more than once
        // In a real implementation, this would use the frequency sketch
        return true; // For now, always promote on access
    }

    /**
     * Segment type enumeration.
     */
    private enum Segment {
        PROTECTED,
        PROBATIONARY
    }

    /**
     * Doubly linked list node for LRU ordering.
     *
     * @param <K> key type
     * @param <V> value type
     */
    private static class Node<K, V> {
        final K key;
        final CacheEntry<V> entry;
        Node<K, V> prev;
        Node<K, V> next;
        Segment segment;

        Node(K key, CacheEntry<V> entry) {
            this.key = key;
            this.entry = entry;
        }
    }
}
