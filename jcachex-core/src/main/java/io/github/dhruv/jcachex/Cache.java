package io.github.dhruv.jcachex;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A thread-safe cache that provides a unified API for both Kotlin and Java
 * applications.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public interface Cache<K, V> {
    /**
     * Returns the value associated with the key in this cache, or null if there is
     * no cached value for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this cache
     *         contains no mapping for the key
     */
    V get(K key);

    /**
     * Associates the specified value with the specified key in this cache.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    void put(K key, V value);

    /**
     * Removes the mapping for a key from this cache if it is present.
     *
     * @param key key whose mapping is to be removed from the cache
     * @return the previous value associated with key, or null if there was no
     *         mapping for key
     */
    V remove(K key);

    /**
     * Removes all mappings from this cache.
     */
    void clear();

    /**
     * Returns the approximate number of entries in this cache.
     *
     * @return the number of entries in this cache
     */
    long size();

    /**
     * Returns true if this cache contains a mapping for the specified key.
     *
     * @param key key whose presence in this cache is to be tested
     * @return true if this cache contains a mapping for the specified key
     */
    boolean containsKey(K key);

    /**
     * Returns a view of all the keys in this cache.
     *
     * @return a set view of the keys contained in this cache
     */
    Set<K> keys();

    /**
     * Returns a view of all the values in this cache.
     *
     * @return a collection view of the values contained in this cache
     */
    Collection<V> values();

    /**
     * Returns a view of all the key-value pairs in this cache.
     *
     * @return a set view of the mappings contained in this cache
     */
    Set<Map.Entry<K, V>> entries();

    /**
     * Returns the cache statistics.
     *
     * @return the cache statistics
     */
    CacheStats stats();

    /**
     * Asynchronously returns the value associated with the key in this cache.
     *
     * @param key the key whose associated value is to be returned
     * @return a CompletableFuture that will be completed with the value to which
     *         the specified key is mapped,
     *         or null if this cache contains no mapping for the key
     */
    CompletableFuture<V> getAsync(K key);

    /**
     * Asynchronously associates the specified value with the specified key in this
     * cache.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return a CompletableFuture that will be completed when the operation is done
     */
    CompletableFuture<Void> putAsync(K key, V value);

    /**
     * Asynchronously removes the mapping for a key from this cache if it is
     * present.
     *
     * @param key key whose mapping is to be removed from the cache
     * @return a CompletableFuture that will be completed with the previous value
     *         associated with key,
     *         or null if there was no mapping for key
     */
    CompletableFuture<V> removeAsync(K key);

    /**
     * Asynchronously removes all mappings from this cache.
     *
     * @return a CompletableFuture that will be completed when the operation is done
     */
    CompletableFuture<Void> clearAsync();

    /**
     * Returns the cache configuration.
     *
     * @return the cache configuration
     */
    CacheConfig<K, V> config();
}
