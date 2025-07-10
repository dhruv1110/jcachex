package io.github.dhruv1110.jcachex.benchmarks;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.DefaultCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.cache2k.Cache2kBuilder;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.openjdk.jmh.annotations.*;

import javax.cache.CacheException;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Base benchmark class that sets up different cache implementations for
 * comparison.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Benchmark)
public abstract class BaseBenchmark {

    protected static final int CACHE_SIZE = 10000;
    protected static final int OPERATIONS_COUNT = 1000;

    // Test data
    protected String[] keys;
    protected String[] values;

    // Cache implementations
    protected Cache<String, String> jcacheXCache;
    protected com.github.benmanes.caffeine.cache.Cache<String, String> caffeineCache;
    protected org.cache2k.Cache<String, String> cache2kCache;
    protected org.ehcache.Cache<String, String> ehcacheCache;
    protected javax.cache.Cache<String, String> jcacheCache;
    protected ConcurrentHashMap<String, String> concurrentMap;

    private CacheManager ehcacheManager;
    private javax.cache.CacheManager jcacheManager;

    // Flags to control which caches to setup (for optimization)
    protected boolean setupJCacheX = true;
    protected boolean setupCaffeine = true;
    protected boolean setupCache2k = true;
    protected boolean setupEhcache = true;
    protected boolean setupJCache = true;
    protected boolean setupConcurrentMap = true;

    @Setup(Level.Trial)
    public void setupTrial() {
        // Generate test data
        keys = new String[OPERATIONS_COUNT];
        values = new String[OPERATIONS_COUNT];

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            keys[i] = "key" + i;
            values[i] = "value" + i + "_" + System.nanoTime(); // Make values unique
        }

        // Setup cache implementations based on flags
        if (setupJCacheX) {
            setupJCacheX();
        }
        if (setupCaffeine) {
            setupCaffeine();
        }
        if (setupCache2k) {
            setupCache2k();
        }
        if (setupEhcache) {
            setupEhcache();
        }
        if (setupJCache) {
            setupJCache();
        }
        if (setupConcurrentMap) {
            setupConcurrentMap();
        }
    }

    private void setupJCacheX() {
        try {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize((long) CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false) // Disable stats for benchmarking
                    .build();
            jcacheXCache = new DefaultCache<>(config);
        } catch (Exception e) {
            System.err.println("JCacheX setup failed: " + e.getMessage());
            setupJCacheX = false;
        }
    }

    private void setupCaffeine() {
        try {
            caffeineCache = Caffeine.newBuilder()
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .build(); // Removed recordStats() to avoid overhead
        } catch (Exception e) {
            System.err.println("Caffeine setup failed: " + e.getMessage());
            setupCaffeine = false;
        }
    }

    private void setupCache2k() {
        try {
            cache2kCache = Cache2kBuilder.of(String.class, String.class)
                    .name("cache2k-benchmark-" + System.currentTimeMillis())
                    .entryCapacity(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .build();
        } catch (Exception e) {
            System.err.println("Cache2k setup failed: " + e.getMessage());
            setupCache2k = false;
        }
    }

    private void setupEhcache() {
        try {
            ehcacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
            ehcacheManager.init();

            String cacheName = "ehcache-benchmark-" + System.currentTimeMillis();
            ehcacheCache = ehcacheManager.createCache(cacheName,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(
                            String.class, String.class,
                            ResourcePoolsBuilder.heap(CACHE_SIZE))
                            .build());
        } catch (Exception e) {
            System.err.println("EHCache setup failed: " + e.getMessage());
            setupEhcache = false;
        }
    }

    private void setupJCache() {
        try {
            // Try to get a specific provider first to avoid conflicts
            CachingProvider provider = null;

            // Try EHCache provider first
            try {
                provider = Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");
            } catch (Exception e) {
                // Fall back to Cache2k provider
                try {
                    provider = Caching.getCachingProvider("org.cache2k.jcache.provider.JCacheProvider");
                } catch (Exception e2) {
                    // Fall back to default provider
                    provider = Caching.getCachingProvider();
                }
            }

            if (provider != null) {
                jcacheManager = provider.getCacheManager();

                MutableConfiguration<String, String> config = new MutableConfiguration<String, String>()
                        .setTypes(String.class, String.class)
                        .setStoreByValue(false)
                        .setStatisticsEnabled(false)
                        .setManagementEnabled(false);

                String cacheName = "jcache-benchmark-" + System.currentTimeMillis();
                jcacheCache = jcacheManager.createCache(cacheName, config);
            }
        } catch (Exception e) {
            System.err.println("JCache setup failed: " + e.getMessage());
            setupJCache = false;
        }
    }

    private void setupConcurrentMap() {
        try {
            concurrentMap = new ConcurrentHashMap<>(CACHE_SIZE);
        } catch (Exception e) {
            System.err.println("ConcurrentMap setup failed: " + e.getMessage());
            setupConcurrentMap = false;
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        // Cleanup resources
        try {
            if (jcacheXCache != null) {
                jcacheXCache.clear();
            }
            if (caffeineCache != null) {
                caffeineCache.invalidateAll();
            }
            if (cache2kCache != null) {
                cache2kCache.close();
            }
            if (ehcacheManager != null) {
                ehcacheManager.close();
            }
            if (jcacheManager != null && jcacheCache != null) {
                String cacheName = null;
                // Try to get cache name for cleanup
                try {
                    jcacheManager.destroyCache(jcacheCache.getName());
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
                jcacheManager.close();
            }
            if (concurrentMap != null) {
                concurrentMap.clear();
            }
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }

    // Helper methods for subclasses
    protected String getRandomKey(int index) {
        return keys[index % keys.length];
    }

    protected String getRandomValue(int index) {
        return values[index % values.length];
    }

    protected String getSequentialKey(int index) {
        return keys[index];
    }

    protected String getSequentialValue(int index) {
        return values[index];
    }
}
