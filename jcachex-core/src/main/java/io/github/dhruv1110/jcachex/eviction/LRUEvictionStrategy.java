package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Least Recently Used (LRU) eviction strategy implementation.
 * <p>
 * This strategy maintains the order in which cache entries are accessed and
 * evicts
 * the entry that was accessed least recently when space is needed. LRU is one
 * of
 * the most commonly used eviction strategies and works well for most caching
 * scenarios
 * where recently accessed data is more likely to be accessed again.
 * </p>
 *
 * <h3>Algorithm Details:</h3>
 * <ul>
 * <li>Each entry access updates its access order timestamp</li>
 * <li>When eviction is needed, the entry with the oldest access timestamp is
 * selected</li>
 * <li>Never-accessed entries have the highest eviction priority (timestamp
 * 0)</li>
 * <li>Thread-safe implementation using concurrent data structures</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * 
 * <pre>{@code
 * // Basic LRU cache configuration
 * CacheConfig<String, String> config = CacheConfig.<String, String>builder()
 *         .maximumSize(1000L)
 *         .evictionStrategy(new LRUEvictionStrategy<>())
 *         .build();
 * Cache<String, String> cache = new DefaultCache<>(config);
 *
 * // LRU behavior demonstration
 * cache.put("A", "value1"); // A is most recent
 * cache.put("B", "value2"); // B is most recent, A is older
 * cache.put("C", "value3"); // C is most recent, B is older, A is oldest
 * cache.get("A"); // A becomes most recent again
 * // Now eviction order would be: B (oldest), C, A (most recent)
 * }</pre>
 *
 * <h3>Best Practices:</h3>
 * <ul>
 * <li><strong>General Purpose:</strong> Excellent default choice for most
 * caching scenarios</li>
 * <li><strong>Temporal Locality:</strong> Works best when recently accessed
 * data is likely to be accessed again</li>
 * <li><strong>Memory Efficiency:</strong> Good balance between performance and
 * memory usage</li>
 * <li><strong>Predictable Behavior:</strong> Easy to understand and debug cache
 * behavior</li>
 * </ul>
 *
 * <h3>Performance Characteristics:</h3>
 * <ul>
 * <li><strong>Update Operation:</strong> O(1) - constant time access order
 * update</li>
 * <li><strong>Eviction Selection:</strong> O(n) - linear scan to find least
 * recently used</li>
 * <li><strong>Memory Overhead:</strong> One long per cache entry for access
 * tracking</li>
 * <li><strong>Thread Safety:</strong> Fully thread-safe with minimal
 * contention</li>
 * </ul>
 *
 * <h3>Comparison with Other Strategies:</h3>
 * 
 * <pre>{@code
 * // LRU - Good general purpose strategy
 * new LRUEvictionStrategy<String, String>()
 *
 * // LFU - Better for data with clear frequency patterns
 * new LFUEvictionStrategy<String, String>()
 *
 * // FIFO - Simpler but less intelligent than LRU
 * new FIFOEvictionStrategy<String, String>()
 * }</pre>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 * @see EvictionStrategy
 * @see LFUEvictionStrategy
 * @see FIFOEvictionStrategy
 * @since 1.0.0
 */
public class LRUEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, Long> accessOrder = new ConcurrentHashMap<>();
    private final AtomicLong accessCounter = new AtomicLong(0);

    @Override
    public void update(K key, CacheEntry<V> entry) {
        accessOrder.put(key, accessCounter.incrementAndGet());
    }

    @Override
    public void remove(K key) {
        accessOrder.remove(key);
    }

    @Override
    public void clear() {
        accessOrder.clear();
        accessCounter.set(0); // Reset counter when clearing
    }

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        return entries.entrySet().stream()
                .min((e1, e2) -> Long.compare(
                        accessOrder.getOrDefault(e1.getKey(), 0L), // Never-accessed entries get 0 (highest eviction
                                                                   // priority)
                        accessOrder.getOrDefault(e2.getKey(), 0L)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
