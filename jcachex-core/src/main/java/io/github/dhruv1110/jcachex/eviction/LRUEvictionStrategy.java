package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Least Recently Used (LRU) eviction strategy implementation.
 * <p>
 * This strategy maintains the order in which cache entries are accessed and
 * evicts the entry that was accessed least recently when space is needed.
 * This implementation uses a doubly-linked list for O(1) access order updates
 * and O(1) eviction candidate selection.
 * </p>
 *
 * <h3>Algorithm Details:</h3>
 * <ul>
 * <li>Uses a doubly-linked list to maintain access order</li>
 * <li>HashMap for O(1) node lookup by key</li>
 * <li>Head of list = most recently used, tail = least recently used</li>
 * <li>All operations (update, select candidate) are O(1)</li>
 * <li>Thread-safe implementation using read-write locks</li>
 * </ul>
 *
 * <h3>Performance Characteristics:</h3>
 * <ul>
 * <li><strong>Update Operation:</strong> O(1) - constant time access order
 * update</li>
 * <li><strong>Eviction Selection:</strong> O(1) - constant time candidate
 * selection</li>
 * <li><strong>Memory Overhead:</strong> Two pointers per cache entry for linked
 * list</li>
 * <li><strong>Thread Safety:</strong> Read-write lock for optimal concurrent
 * performance</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 * @see EvictionStrategy
 * @since 1.0.0
 */
public class LRUEvictionStrategy<K, V> implements EvictionStrategy<K, V> {

    /**
     * Node in the doubly-linked list representing an entry's position in access
     * order
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

    // Read-write lock for thread safety
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public LRUEvictionStrategy() {
        // Initialize empty doubly-linked list
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        writeLock.lock();
        try {
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
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        readLock.lock();
        try {
            // Return the least recently used key (tail of the list)
            AccessNode<K> lru = tail.prev;
            if (lru != head) {
                return lru.key;
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Add node to the head of the list (most recently used position)
     */
    private void addToHead(AccessNode<K> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * Remove a node from the list
     */
    private void removeNode(AccessNode<K> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /**
     * Move an existing node to the head (most recently used position)
     */
    private void moveToHead(AccessNode<K> node) {
        removeNode(node);
        addToHead(node);
    }
}
