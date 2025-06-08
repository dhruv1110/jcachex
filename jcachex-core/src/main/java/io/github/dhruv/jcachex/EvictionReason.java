package io.github.dhruv.jcachex;

/**
 * Reasons for cache eviction.
 */
public enum EvictionReason {
    /**
     * The entry was evicted because the cache size limit was reached.
     */
    SIZE,

    /**
     * The entry was evicted because the cache weight limit was reached.
     */
    WEIGHT,

    /**
     * The entry was evicted because it expired.
     */
    EXPIRED,

    /**
     * The entry was evicted because it was explicitly removed.
     */
    EXPLICIT,

    /**
     * The entry was evicted because the cache was cleared.
     */
    CLEARED,

    /**
     * The entry was evicted for an unknown reason.
     */
    UNKNOWN
}
