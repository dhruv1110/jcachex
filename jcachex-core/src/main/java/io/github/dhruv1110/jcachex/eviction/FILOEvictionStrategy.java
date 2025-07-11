package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Optimized First In, Last Out (FILO) eviction strategy with O(1) operations.
 * <p>
 * This implementation uses a doubly-linked list to maintain insertion order,
 * providing O(1) operations for both updates and eviction candidate selection.
 * FILO evicts the most recently inserted entry first (stack-like behavior).
 * </p>
 *
 * <h3>Performance Characteristics:</h3>
 * <ul>
 * <li><strong>Update Operation:</strong> O(1) - constant time insertion
 * tracking</li>
 * <li><strong>Eviction Selection:</strong> O(1) - constant time candidate
 * selection</li>
 * <li><strong>Memory Overhead:</strong> Two pointers per entry for linked
 * list</li>
 * <li><strong>Thread Safety:</strong> Read-write lock for optimal concurrent
 * performance</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class FILOEvictionStrategy<K, V> implements EvictionStrategy<K, V> {

    /**
     * Node in the doubly-linked list representing insertion order.
     */
    private static class InsertionNode<K> {
        K key;
        InsertionNode<K> prev;
        InsertionNode<K> next;

        InsertionNode(K key) {
            this.key = key;
        }
    }

    // Map from key to its insertion node
    private final ConcurrentHashMap<K, InsertionNode<K>> keyToNode = new ConcurrentHashMap<>();

    // Doubly-linked list sentinel nodes (head = newest, tail = oldest)
    private final InsertionNode<K> head = new InsertionNode<>(null);
    private final InsertionNode<K> tail = new InsertionNode<>(null);

    // Thread safety
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public FILOEvictionStrategy() {
        // Initialize empty doubly-linked list
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        readLock.lock();
        try {
            // Return the newest entry (head of the list) - FILO behavior
            InsertionNode<K> newest = head.next;
            return (newest != tail) ? newest.key : null;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        writeLock.lock();
        try {
            InsertionNode<K> node = keyToNode.get(key);

            if (node == null) {
                // New key - add to head (newest position)
                node = new InsertionNode<>(key);
                keyToNode.put(key, node);
                addToHead(node);
            }
            // For FILO, we don't update position on access or reinsert - only on first
            // insertion
            // The existing node stays in its original position to maintain insertion order
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        writeLock.lock();
        try {
            InsertionNode<K> node = keyToNode.remove(key);
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
            keyToNode.clear();
            head.next = tail;
            tail.prev = head;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Add node to the head of the list (newest position).
     */
    private void addToHead(InsertionNode<K> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * Remove a node from the list.
     */
    private void removeNode(InsertionNode<K> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
