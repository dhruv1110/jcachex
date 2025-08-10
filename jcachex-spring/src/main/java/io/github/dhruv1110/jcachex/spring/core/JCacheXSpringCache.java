package io.github.dhruv1110.jcachex.spring.core;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheStats;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Spring Cache adapter that bridges JCacheX with Spring's caching abstraction.
 *
 * This adapter allows JCacheX caches to be used seamlessly with Spring's
 * declarative caching support (@Cacheable, @CacheEvict, etc.) while providing
 * access to all JCacheX features and maintaining compatibility with Spring's
 * Cache interface.
 *
 * <h2>Key Features:</h2>
 * <ul>
 * <li><strong>Full JCacheX Integration</strong>: Direct access to underlying
 * JCacheX cache</li>
 * <li><strong>Spring Compatibility</strong>: Works with Spring's caching
 * annotations</li>
 * <li><strong>Null Value Support</strong>: Configurable handling of null
 * values</li>
 * <li><strong>Type Safety</strong>: Proper handling of generic types</li>
 * <li><strong>Thread Safety</strong>: Fully thread-safe for concurrent
 * access</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <pre>
 * {
 *     &#64;code
 *     // Direct usage (usually not needed)
 *     JCacheXSpringCache springCache = new JCacheXSpringCache("users", jcacheXCache, true);
 *
 *     // Access via cache manager
 *     &#64;Service
 *     public class UserService {
 *         @Autowired
 *         private CacheManager cacheManager;
 *
 *         public User getUser(String id) {
 *             Cache cache = cacheManager.getCache("users");
 *             return cache.get(id, User.class);
 *         }
 *
 *         // Access underlying JCacheX cache for advanced features
 *         public CompletableFuture<User> getUserAsync(String id) {
 *             JCacheXSpringCache springCache = (JCacheXSpringCache) cacheManager.getCache("users");
 *             return springCache.getNativeCache().getAsync(id);
 *         }
 *     }
 * }
 * </pre>
 *
 * @see Cache
 * @see org.springframework.cache.Cache
 * @see JCacheXCacheManager
 * @since 1.0.0
 */
public class JCacheXSpringCache extends org.springframework.cache.support.AbstractValueAdaptingCache {

    private final String name;
    private final Cache<Object, Object> nativeCache;
    private final LongAdder entryCount = new LongAdder();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true); // Fair locking

    /**
     * Creates a new JCacheXSpringCache instance.
     *
     * @param name            the cache name
     * @param nativeCache     the underlying JCacheX cache
     * @param allowNullValues whether to allow null values
     */
    public JCacheXSpringCache(String name, Cache<Object, Object> nativeCache, boolean allowNullValues) {
        super(allowNullValues);
        this.name = name;
        this.nativeCache = nativeCache;
        // Initialize entry count
        lock.writeLock().lock();
        try {
            entryCount.add(nativeCache.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Creates a new JCacheXSpringCache instance with null values allowed.
     *
     * @param name        the cache name
     * @param nativeCache the underlying JCacheX cache
     */
    public JCacheXSpringCache(String name, Cache<Object, Object> nativeCache) {
        this(name, nativeCache, true);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return nativeCache;
    }

    @Override
    protected Object lookup(Object key) {
        lock.readLock().lock();
        try {
            return nativeCache.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    @Nullable
    public <T> T get(Object key, Callable<T> valueLoader) {
        // First try with read lock
        lock.readLock().lock();
        try {
            Object value = nativeCache.get(key);
            if (value != null) {
                @SuppressWarnings("unchecked")
                T typedValue = (T) value;
                return typedValue;
            }
        } finally {
            lock.readLock().unlock();
        }

        // If not found, acquire write lock and try again
        lock.writeLock().lock();
        try {
            // Double-check under write lock
            Object value = nativeCache.get(key);
            if (value != null) {
                @SuppressWarnings("unchecked")
                T typedValue = (T) value;
                return typedValue;
            }

            try {
                T newValue = valueLoader.call();
                put(key, newValue);
                return newValue;
            } catch (Exception ex) {
                throw new ValueRetrievalException(key, valueLoader, ex);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        lock.writeLock().lock();
        try {
            Object storeValue = toStoreValue(value);
            Object oldValue = nativeCache.get(key);
            nativeCache.put(key, storeValue);
            if (oldValue == null) {
                entryCount.increment();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @Nullable
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        lock.writeLock().lock();
        try {
            Object existingValue = nativeCache.get(key);
            if (existingValue == null) {
                Object storeValue = toStoreValue(value);
                nativeCache.put(key, storeValue);
                entryCount.increment();
                return null;
            }
            return toValueWrapper(existingValue);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void evict(Object key) {
        evictBeforeInvocation(key);
    }

    /**
     * Evicts a key from the cache, ensuring it happens before method execution.
     * This is used by @CacheEvict with beforeInvocation=true.
     *
     * @param key the key to evict
     */
    public void evictBeforeInvocation(Object key) {
        lock.writeLock().lock();
        try {
            Object removed = nativeCache.remove(key);
            if (removed != null) {
                entryCount.decrement();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean evictIfPresent(Object key) {
        lock.writeLock().lock();
        try {
            Object removed = nativeCache.remove(key);
            if (removed != null) {
                entryCount.decrement();
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            nativeCache.clear();
            entryCount.reset();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets a value from the cache with the specified type.
     *
     * This method provides type-safe access to cached values, automatically
     * casting to the expected type.
     *
     * @param key  the cache key
     * @param type the expected value type
     * @param <T>  the value type
     * @return the cached value, or null if not found
     * @throws ClassCastException if the cached value cannot be cast to the expected
     *                            type
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(Object key, @Nullable Class<T> type) {
        lock.readLock().lock();
        try {
            Object value = nativeCache.get(key);
            if (value != null && type != null && !type.isInstance(value)) {
                throw new ClassCastException(
                        "Cached value for key '" + key + "' is not of expected type " + type.getName() +
                                " but " + value.getClass().getName());
            }
            return (T) value;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if the cache contains a value for the specified key.
     *
     * @param key the cache key
     * @return true if the cache contains a value for the key, false otherwise
     */
    public boolean containsKey(Object key) {
        lock.readLock().lock();
        try {
            return nativeCache.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the size of the cache.
     *
     * @return the number of entries in the cache
     */
    public long size() {
        lock.readLock().lock();
        try {
            return entryCount.sum();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets cache statistics if available.
     *
     * @return cache statistics, or null if statistics are not enabled or not
     *         supported
     */
    @Nullable
    public CacheStats getStats() {
        lock.readLock().lock();
        try {
            // All JCacheX cache implementations should support stats()
            return nativeCache.stats();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the underlying JCacheX cache instance.
     *
     * This provides direct access to JCacheX-specific features like async
     * operations,
     * detailed statistics, and advanced configuration options.
     *
     * @return the underlying JCacheX cache
     */
    public Cache<Object, Object> getJCacheXCache() {
        return nativeCache;
    }

    @Override
    public String toString() {
        return "JCacheXSpringCache{" +
                "name='" + name + '\'' +
                ", nativeCache=" + nativeCache +
                '}';
    }
}
