package io.github.dhruv1110.jcachex.internal.util;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheStats;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Utility class that extracts common patterns found across cache
 * implementations.
 *
 * <p>
 * This class has been updated to delegate to {@link CacheCommonOperations}
 * to eliminate duplication. It remains for backward compatibility.
 * </p>
 *
 * @deprecated Use {@link CacheCommonOperations} directly for new code.
 * @since 1.0.0
 */
@Deprecated
public final class CacheImplementationUtils {

    private CacheImplementationUtils() {
        // Utility class - no instances
    }

    // ===== DELEGATING METHODS TO CacheCommonOperations =====

    /**
     * @deprecated Use
     *             {@link CacheCommonOperations#createStats(AtomicLong, AtomicLong)}
     */
    @Deprecated
    public static CacheStats createStandardStats(AtomicLong hitCount, AtomicLong missCount) {
        return CacheCommonOperations.createStats(hitCount, missCount);
    }

    /**
     * @deprecated Use
     *             {@link CacheCommonOperations#createComprehensiveStats(AtomicLong, AtomicLong, AtomicLong, AtomicLong, AtomicLong)}
     */
    @Deprecated
    public static CacheStats createComprehensiveStats(AtomicLong hitCount, AtomicLong missCount,
            AtomicLong loadCount, AtomicLong loadTime,
            AtomicLong evictionCount) {
        return CacheCommonOperations.createComprehensiveStats(hitCount, missCount, loadCount, loadTime, evictionCount);
    }

    /**
     * @deprecated Use {@link CacheCommonOperations#createConfig(long, boolean)}
     */
    @Deprecated
    public static <K, V> CacheConfig<K, V> createStandardConfig(long maximumSize, boolean statsEnabled) {
        return CacheCommonOperations.createConfig(maximumSize, statsEnabled);
    }

    /**
     * @deprecated Use
     *             {@link CacheCommonOperations#createKeysView(ConcurrentHashMap)}
     */
    @Deprecated
    public static <K, V> Set<K> createKeysView(ConcurrentHashMap<K, ? extends Object> data) {
        return CacheCommonOperations.createKeysView(data);
    }

    /**
     * @deprecated Use
     *             {@link CacheCommonOperations#createValuesView(ConcurrentHashMap, Function)}
     */
    @Deprecated
    public static <K, V, E> Collection<V> createValuesView(ConcurrentHashMap<K, E> data,
            Function<E, V> valueExtractor) {
        return CacheCommonOperations.createValuesView(data, valueExtractor);
    }

    /**
     * @deprecated Use
     *             {@link CacheCommonOperations#createEntriesView(ConcurrentHashMap, Function)}
     */
    @Deprecated
    public static <K, V, E> Set<Map.Entry<K, V>> createEntriesView(ConcurrentHashMap<K, E> data,
            Function<E, V> valueExtractor) {
        return CacheCommonOperations.createEntriesView(data, valueExtractor);
    }

    /**
     * @deprecated Use {@link CacheCommonOperations#isValidKey(Object)}
     */
    @Deprecated
    public static boolean isValidKey(Object key) {
        return CacheCommonOperations.isValidKey(key);
    }

    /**
     * @deprecated Use {@link CacheCommonOperations#isValidValue(Object)}
     */
    @Deprecated
    public static boolean isValidValue(Object value) {
        return CacheCommonOperations.isValidValue(value);
    }

    /**
     * @deprecated Use
     *             {@link CacheCommonOperations#containsKey(Object, ConcurrentHashMap)}
     */
    @Deprecated
    public static <K> boolean containsKey(K key, ConcurrentHashMap<K, ?> data) {
        return CacheCommonOperations.containsKey(key, data);
    }

    /**
     * @deprecated Use {@link CacheCommonOperations.ImmutableMapEntry}
     */
    @Deprecated
    public static <K, V> Map.Entry<K, V> createMapEntry(K key, V value) {
        return CacheCommonOperations.createMapEntry(key, value);
    }
}
