package io.github.dhruv.jcachex.eviction;

import io.github.dhruv.jcachex.CacheEntry;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Time-based eviction strategy.
 * This strategy evicts entries based on their last access time.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class TimeBasedEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, Instant> lastAccessTime = new ConcurrentHashMap<>();
    private final Duration maxIdleTime;

    public TimeBasedEvictionStrategy(Duration maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        Instant now = Instant.now();
        return entries.entrySet().stream()
                .filter(e -> {
                    Instant lastAccess = lastAccessTime.getOrDefault(e.getKey(), Instant.EPOCH);
                    return Duration.between(lastAccess, now).compareTo(maxIdleTime) >= 0;
                })
                .max((e1, e2) -> lastAccessTime.getOrDefault(e1.getKey(), Instant.EPOCH)
                        .compareTo(lastAccessTime.getOrDefault(e2.getKey(), Instant.EPOCH)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        lastAccessTime.put(key, Instant.now());
    }

    @Override
    public void remove(K key) {
        lastAccessTime.remove(key);
    }

    @Override
    public void clear() {
        lastAccessTime.clear();
    }
}
