package io.github.dpflux.jcachex;

/**
 * Interface for cache event listeners.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public interface CacheEventListener<K, V> {
    /**
     * Called when a value is added to the cache.
     *
     * @param key   the key of the added value
     * @param value the added value
     */
    void onPut(K key, V value);

    /**
     * Called when a value is removed from the cache.
     *
     * @param key   the key of the removed value
     * @param value the removed value
     */
    void onRemove(K key, V value);

    /**
     * Called when a value is evicted from the cache.
     *
     * @param key    the key of the evicted value
     * @param value  the evicted value
     * @param reason the reason for eviction
     */
    void onEvict(K key, V value, EvictionReason reason);

    /**
     * Called when a value expires from the cache.
     *
     * @param key   the key of the expired value
     * @param value the expired value
     */
    void onExpire(K key, V value);

    /**
     * Called when a value is loaded into the cache.
     *
     * @param key   the key of the loaded value
     * @param value the loaded value
     */
    void onLoad(K key, V value);

    /**
     * Called when a value fails to load into the cache.
     *
     * @param key   the key of the value that failed to load
     * @param error the error that occurred
     */
    void onLoadError(K key, Throwable error);

    /**
     * Called when the cache is cleared.
     */
    void onClear();

    /**
     * Creates a new CacheEventListener that does nothing.
     */
    static <K, V> CacheEventListener<K, V> noOp() {
        return new CacheEventListener<K, V>() {
            @Override
            public void onPut(K key, V value) {
            }

            @Override
            public void onRemove(K key, V value) {
            }

            @Override
            public void onEvict(K key, V value, EvictionReason reason) {
            }

            @Override
            public void onExpire(K key, V value) {
            }

            @Override
            public void onLoad(K key, V value) {
            }

            @Override
            public void onLoadError(K key, Throwable error) {
            }

            @Override
            public void onClear() {
            }
        };
    }
}
