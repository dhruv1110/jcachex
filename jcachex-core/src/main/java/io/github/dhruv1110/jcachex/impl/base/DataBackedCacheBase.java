package io.github.dhruv1110.jcachex.impl.base;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.internal.util.ConfigurationProvider;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Abstract base class for cache implementations that store data in a
 * ConcurrentHashMap.
 *
 * This class eliminates code duplication by providing common implementations
 * for:
 * - Collection view methods (keys, values, entries)
 * - Data storage management
 * - Entry value extraction patterns
 * - Common data access patterns
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @param <E> the type of entries stored in the data map
 */
public abstract class DataBackedCacheBase<K, V, E> extends AbstractCacheBase<K, V> {

    // Core data storage - shared across most implementations
    protected final ConcurrentHashMap<K, E> data;

    /**
     * Constructor for data-backed cache implementations.
     *
     * @param config the cache configuration
     */
    protected DataBackedCacheBase(CacheConfig<K, V> config) {
        super(config);

        // Initialize with optimal capacity using utility
        int initialCapacity = ConfigurationProvider.getSafeInitialCapacity(config);
        int capacity = ConfigurationProvider.calculateOptimalCapacity(maximumSize);
        int concurrency = ConfigurationProvider.calculateOptimalConcurrency();
        this.data = new ConcurrentHashMap<>(capacity, 0.75f, concurrency);
    }

    /**
     * Common implementation of keys() method.
     * Returns the key set from the underlying data map.
     */
    @Override
    public final Set<K> keys() {
        return data.keySet();
    }

    /**
     * Common implementation of values() method with filtering.
     * Subclasses provide the value extraction and filtering logic.
     */
    @Override
    public final Collection<V> values() {
        return data.values().stream()
                .map(this::extractValue)
                .filter(this::isValidValue)
                .collect(Collectors.toList());
    }

    /**
     * Common implementation of entries() method with filtering.
     * Subclasses provide the value extraction and filtering logic.
     */
    @Override
    public final Set<Map.Entry<K, V>> entries() {
        return data.entrySet().stream()
                .filter(e -> isValidEntry(e.getValue()))
                .map(e -> createMapEntry(e.getKey(), extractValue(e.getValue())))
                .collect(Collectors.toSet());
    }

    /**
     * Common implementation of size() method.
     * Can be overridden by subclasses that track size differently.
     */
    @Override
    public long size() {
        return data.size();
    }

    /**
     * Common implementation of containsKey with entry validation.
     */
    @Override
    protected boolean doContainsKey(K key) {
        E entry = data.get(key);
        return entry != null && isValidEntry(entry);
    }

    /**
     * Common clear implementation.
     */
    @Override
    protected void doClear() {
        beforeClear();
        data.clear();
        afterClear();
    }

    // Abstract methods for entry-specific operations

    /**
     * Extract the value from an entry.
     *
     * @param entry the entry
     * @return the extracted value
     */
    protected abstract V extractValue(E entry);

    /**
     * Check if an entry is valid (not expired, not corrupted, etc.).
     *
     * @param entry the entry to validate
     * @return true if the entry is valid
     */
    protected abstract boolean isValidEntry(E entry);

    /**
     * Check if an extracted value should be included in collection views.
     * Default implementation includes null values.
     *
     * @param value the extracted value
     * @return true if the value should be included
     */
    protected boolean isValidValue(V value) {
        return true; // Allow null values to be included
    }

    /**
     * Create a Map.Entry for the entries() collection view.
     * Uses a simple immutable entry by default.
     *
     * @param key   the key
     * @param value the value
     * @return a Map.Entry
     */
    protected Map.Entry<K, V> createMapEntry(K key, V value) {
        return new ImmutableMapEntry<>(key, value);
    }

    // Hook methods for subclass customization

    /**
     * Called before clearing the data map.
     * Subclasses can override to perform cleanup operations.
     */
    protected void beforeClear() {
        // Default: no operation
    }

    /**
     * Called after clearing the data map.
     * Subclasses can override to reset additional state.
     */
    protected void afterClear() {
        // Default: no operation
    }

    // Utility methods

    /**
     * Get direct access to the underlying data map.
     * Should be used carefully and only when necessary.
     *
     * @return the underlying data map
     */
    protected final ConcurrentHashMap<K, E> getDataMap() {
        return data;
    }

    /**
     * Immutable Map.Entry implementation for collection views.
     */
    private static final class ImmutableMapEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private final V value;

        ImmutableMapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("Entry is immutable");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            return java.util.Objects.equals(key, entry.getKey()) &&
                    java.util.Objects.equals(value, entry.getValue());
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}
