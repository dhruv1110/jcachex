package io.github.dhruv1110.jcachex.benchmarks;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.dhruv1110.jcachex.Cache;
import org.cache2k.Cache2kBuilder;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.openjdk.jmh.annotations.*;

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

    // Realistic benchmarking configuration for eviction testing
    protected static final int CACHE_SIZE = 10_000; // 10K cache capacity
    protected static final int OPERATIONS_COUNT = 50_000; // 50K working set (5x cache size - forces evictions)
    protected static final int WARMUP_SET_SIZE = 5_000; // Pre-populate only 50% of cache

    // Test data
    protected String[] keys;
    protected String[] values;

    // JCacheX Cache Profiles - All 12 profiles
    // Core Profiles (5)
    protected Cache<String, String> jcacheXDefault;
    protected Cache<String, String> jcacheXReadHeavy;
    protected Cache<String, String> jcacheXWriteHeavy;
    protected Cache<String, String> jcacheXMemoryEfficient;
    protected Cache<String, String> jcacheXHighPerformance;

    // Specialized Profiles (3)
    protected Cache<String, String> jcacheXSessionCache;
    protected Cache<String, String> jcacheXApiCache;
    protected Cache<String, String> jcacheXComputeCache;

    // Advanced Profiles (4)
    protected Cache<String, String> jcacheXMlOptimized;
    protected Cache<String, String> jcacheXZeroCopy;
    protected Cache<String, String> jcacheXHardwareOptimized;

    // Industry-leading cache implementations
    protected com.github.benmanes.caffeine.cache.Cache<String, String> caffeineCache;
    protected org.cache2k.Cache<String, String> cache2kCache;
    protected org.ehcache.Cache<String, String> ehcacheCache;
    protected javax.cache.Cache<String, String> jcacheCache;
    protected ConcurrentHashMap<String, String> concurrentMap;
    // Flags to control which caches to setup (for optimization)
    protected boolean setupJCacheXProfiles = true;
    protected boolean setupCaffeine = true;
    protected boolean setupCache2k = true;
    protected boolean setupEhcache = true;
    protected boolean setupJCache = true;
    protected boolean setupConcurrentMap = true;
    private CacheManager ehcacheManager;
    private javax.cache.CacheManager jcacheManager;

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
        if (setupJCacheXProfiles) {
            setupJCacheXProfiles();
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

    private void setupJCacheXProfiles() {
        try {
            // Core Profiles (5)
            jcacheXDefault = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.DEFAULT)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

            jcacheXReadHeavy = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.READ_HEAVY)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

            jcacheXWriteHeavy = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.WRITE_HEAVY)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

            jcacheXMemoryEfficient = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.MEMORY_EFFICIENT)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

            jcacheXHighPerformance = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.HIGH_PERFORMANCE)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

            // Specialized Profiles (3)
            jcacheXSessionCache = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.SESSION_CACHE)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

            jcacheXApiCache = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.API_CACHE)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

            jcacheXComputeCache = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.COMPUTE_CACHE)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

            // Advanced Profiles (4)
            jcacheXMlOptimized = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.ML_OPTIMIZED)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

            jcacheXZeroCopy = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.ZERO_COPY)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

            jcacheXHardwareOptimized = io.github.dhruv1110.jcachex.JCacheXBuilder
                    .<String, String>fromProfile(io.github.dhruv1110.jcachex.profiles.ProfileName.HARDWARE_OPTIMIZED)
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();

        } catch (Exception e) {
            System.err.println("JCacheX profiles setup failed: " + e.getMessage());
            setupJCacheXProfiles = false;
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
            // Clean up JCacheX profiles
            if (jcacheXDefault != null)
                jcacheXDefault.clear();
            if (jcacheXReadHeavy != null)
                jcacheXReadHeavy.clear();
            if (jcacheXWriteHeavy != null)
                jcacheXWriteHeavy.clear();
            if (jcacheXMemoryEfficient != null)
                jcacheXMemoryEfficient.clear();
            if (jcacheXHighPerformance != null)
                jcacheXHighPerformance.clear();
            if (jcacheXSessionCache != null)
                jcacheXSessionCache.clear();
            if (jcacheXApiCache != null)
                jcacheXApiCache.clear();
            if (jcacheXComputeCache != null)
                jcacheXComputeCache.clear();
            if (jcacheXMlOptimized != null)
                jcacheXMlOptimized.clear();
            if (jcacheXZeroCopy != null)
                jcacheXZeroCopy.clear();
            if (jcacheXHardwareOptimized != null)
                jcacheXHardwareOptimized.clear();
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

    // Realistic access patterns for production-like benchmarking

    /**
     * Get a hot key (80/20 rule - 20% of keys get 80% of access)
     * Forces cache pressure and eviction testing
     */
    protected String getHotKey(int index) {
        // 80% chance to access hot keys (first 20% of key space)
        int hotKeySpace = OPERATIONS_COUNT / 5; // 20% of total keys
        if (index % 5 < 4) { // 80% probability
            return keys[(index % hotKeySpace) % keys.length];
        } else { // 20% probability - access cold keys
            int coldStart = hotKeySpace;
            return keys[coldStart + (index % (OPERATIONS_COUNT - hotKeySpace)) % keys.length];
        }
    }

    /**
     * Get a key that may not be in cache (forces cache misses and evictions)
     * Simulates real production scenarios where working set > cache capacity
     */
    protected String getEvictionTestKey(int index) {
        // Access keys from entire OPERATIONS_COUNT range (5x cache size)
        // This guarantees evictions will occur
        return "key" + (index % OPERATIONS_COUNT);
    }

    /**
     * Get a value for eviction testing
     */
    protected String getEvictionTestValue(int index) {
        return "value" + (index % OPERATIONS_COUNT) + "_" + System.nanoTime();
    }
}
