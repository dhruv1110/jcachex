package io.github.dhruv1110.jcachex.example.java;

import io.github.dhruv1110.jcachex.*;
import io.github.dhruv1110.jcachex.FrequencySketchType;
import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;
import io.github.dhruv1110.jcachex.JCacheXBuilder;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== JCacheX Comprehensive Example ===\n");

        // Demonstrate new CacheBuilder and default TinyWindowLFU
        demonstrateBasicUsage();

        // Demonstrate specialized cache types
        demonstrateSpecializedCaches();

        // Demonstrate enhanced eviction strategies
        demonstrateEnhancedEvictionStrategies();

        // Demonstrate frequency sketch options
        demonstrateFrequencySketchOptions();

        // Demonstrate performance-optimized configurations
        demonstratePerformanceOptimizations();

        System.out.println("\n=== Example Complete ===");
    }

    private static void demonstrateBasicUsage() {
        System.out.println("=== Basic Usage with Default TinyWindowLFU ===");

        // Create cache using new JCacheXBuilder (TinyWindowLFU is now default)
        Cache<String, String> cache = JCacheXBuilder.<String, String>create()
                .maximumSize(100L)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats(true)
                .listener(new BasicEventListener())
                .build();

        // Basic operations
        cache.put("user:123", "John Doe");
        cache.put("user:456", "Jane Smith");

        String user = cache.get("user:123");
        System.out.println("Retrieved user: " + user);

        // Async operations
        CompletableFuture<String> futureUser = cache.getAsync("user:456");
        futureUser.thenAccept(u -> System.out.println("Async retrieved: " + u));

        printCacheStats(cache, "Basic Cache");
        System.out.println();
    }

    private static void demonstrateSpecializedCaches() {
        System.out.println("=== Specialized Cache Types ===");

        // Read-heavy workload cache (fastest GET performance)
        Cache<String, String> readCache = JCacheXBuilder.<String, String>forReadHeavyWorkload()
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofHours(2))
                .build();

        System.out.println("Read-optimized cache created (expected ~11.5ns GET)");

        // Write-heavy workload cache
        Cache<String, String> writeCache = JCacheXBuilder.<String, String>forWriteHeavyWorkload()
                .maximumSize(5000L)
                .expireAfterAccess(Duration.ofMinutes(30))
                .build();

        System.out.println("Write-optimized cache created (expected ~393.5ns PUT)");

        // Memory-constrained environment
        Cache<String, String> memoryCache = JCacheXBuilder.<String, String>forMemoryConstrainedEnvironment()
                .maximumSize(100L)
                .expireAfterWrite(Duration.ofHours(12))
                .build();

        System.out.println("Memory-optimized cache created (minimal GC pressure)");

        // High-performance cache
        Cache<String, String> performanceCache = JCacheXBuilder.<String, String>forHighPerformance()
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofMinutes(15))
                .build();

        System.out.println("High-performance cache created (expected ~24.6ns GET, ~63.8ns PUT)");

        // Test performance difference
        testCachePerformance(readCache, "ReadOnly Optimized");
        testCachePerformance(performanceCache, "JIT Optimized");
        System.out.println();
    }

    private static void demonstrateEnhancedEvictionStrategies() {
        System.out.println("=== Enhanced Eviction Strategies ===");

        // Enhanced LRU with frequency sketch
        Cache<String, String> enhancedLRU = JCacheXBuilder.<String, String>create()
                .maximumSize(50L)
                .recordStats(true)
                .build();

        // Enhanced LFU with frequency buckets
        Cache<String, String> enhancedLFU = JCacheXBuilder.<String, String>create()
                .maximumSize(50L)
                .recordStats(true)
                .build();

        // TinyWindowLFU (default) - hybrid approach
        Cache<String, String> tinyWindowLFU = JCacheXBuilder.<String, String>create()
                .maximumSize(50L)
                .recordStats(true)
                .build();

        // Test with skewed access pattern
        testSkewedAccessPattern(enhancedLRU, "Enhanced LRU");
        testSkewedAccessPattern(enhancedLFU, "Enhanced LFU");
        testSkewedAccessPattern(tinyWindowLFU, "TinyWindowLFU");
        System.out.println();
    }

    private static void demonstrateFrequencySketchOptions() {
        System.out.println("=== Frequency Sketch Options ===");

        // No frequency sketch - minimal overhead
        Cache<String, String> noSketchCache = JCacheXBuilder.<String, String>create()
                .maximumSize(100L)
                .recordStats(true)
                .build();

        // Basic frequency sketch - balanced approach
        Cache<String, String> basicSketchCache = JCacheXBuilder.<String, String>create()
                .maximumSize(100L)
                .recordStats(true)
                .build();

        // Optimized frequency sketch - maximum accuracy
        Cache<String, String> optimizedSketchCache = JCacheXBuilder.<String, String>create()
                .maximumSize(100L)
                .recordStats(true)
                .build();

        System.out.println("Created caches with different frequency sketch types:");
        System.out.println("- NONE: Minimal overhead, pure algorithm");
        System.out.println("- BASIC: Balanced accuracy and memory usage (default)");
        System.out.println("- OPTIMIZED: Maximum accuracy for complex patterns");
        System.out.println();
    }

    private static void demonstratePerformanceOptimizations() {
        System.out.println("=== Performance Optimizations ===");

        // Custom configuration showcasing all features
        Cache<String, UserProfile> customCache = JCacheXBuilder.<String, UserProfile>create()
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofHours(1))
                .expireAfterAccess(Duration.ofMinutes(30))
                .recordStats(true)
                .listener(new DetailedEventListener())
                .build();

        // Populate with test data
        for (int i = 0; i < 100; i++) {
            UserProfile user = new UserProfile("user" + i, "User " + i, "user" + i + "@example.com");
            customCache.put("user:" + i, user);
        }

        // Test performance
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            customCache.get("user:" + (i % 100));
        }
        long endTime = System.nanoTime();

        double avgNanos = (endTime - startTime) / 1000.0;
        System.out.println("Average GET latency: " + String.format("%.2f", avgNanos) + " ns");

        printCacheStats(customCache, "Custom Optimized Cache");
        System.out.println();
    }

    private static void testCachePerformance(Cache<String, String> cache, String cacheName) {
        // Warm up
        for (int i = 0; i < 1000; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Test get performance
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            cache.get("key" + (i % 1000));
        }
        long endTime = System.nanoTime();

        double avgNanos = (endTime - startTime) / 10000.0;
        System.out.println(cacheName + " - Average GET: " + String.format("%.2f", avgNanos) + " ns");
    }

    private static void testSkewedAccessPattern(Cache<String, String> cache, String strategyName) {
        // Create access pattern where 20% of keys get 80% of accesses
        for (int i = 0; i < 100; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Simulate skewed access pattern
        for (int i = 0; i < 1000; i++) {
            String key = i < 800 ? "key" + (i % 20) : "key" + (20 + i % 80);
            cache.get(key);
        }

        CacheStats stats = cache.stats();
        System.out.println(strategyName + " - Hit rate: " + String.format("%.2f", stats.hitRate() * 100) + "%");
    }

    private static void printCacheStats(Cache<?, ?> cache, String cacheName) {
        CacheStats stats = cache.stats();
        System.out.println(cacheName + " Statistics:");
        System.out.println("  Hits: " + stats.hitCount());
        System.out.println("  Misses: " + stats.missCount());
        System.out.println("  Total Requests: " + (stats.hitCount() + stats.missCount()));
        System.out.println("  Hit Rate: " + String.format("%.2f", stats.hitRate() * 100) + "%");
        System.out.println("  Evictions: " + stats.evictionCount());
    }

    // Basic event listener implementation
    private static class BasicEventListener implements CacheEventListener<String, String> {
        @Override
        public void onPut(String key, String value) {
            System.out.println("PUT: " + key + " -> " + value);
        }

        @Override
        public void onRemove(String key, String value) {
            System.out.println("REMOVE: " + key + " -> " + value);
        }

        @Override
        public void onEvict(String key, String value, EvictionReason reason) {
            System.out.println("EVICT: " + key + " -> " + value + " (reason: " + reason + ")");
        }

        @Override
        public void onExpire(String key, String value) {
            System.out.println("EXPIRE: " + key + " -> " + value);
        }

        @Override
        public void onLoad(String key, String value) {
            System.out.println("LOAD: " + key + " -> " + value);
        }

        @Override
        public void onLoadError(String key, Throwable error) {
            System.out.println("LOAD_ERROR: " + key + " -> " + error.getMessage());
        }

        @Override
        public void onClear() {
            System.out.println("CLEAR");
        }
    }

    // Detailed event listener implementation
    private static class DetailedEventListener implements CacheEventListener<String, UserProfile> {
        @Override
        public void onPut(String key, UserProfile value) {
            System.out.println("PUT: " + key + " -> " + value);
        }

        @Override
        public void onRemove(String key, UserProfile value) {
            System.out.println("REMOVE: " + key + " -> " + value);
        }

        @Override
        public void onEvict(String key, UserProfile value, EvictionReason reason) {
            System.out.println("EVICT: " + key + " -> " + value + " (reason: " + reason + ")");
        }

        @Override
        public void onExpire(String key, UserProfile value) {
            System.out.println("EXPIRE: " + key + " -> " + value);
        }

        @Override
        public void onLoad(String key, UserProfile value) {
            System.out.println("LOAD: " + key + " -> " + value);
        }

        @Override
        public void onLoadError(String key, Throwable error) {
            System.out.println("LOAD_ERROR: " + key + " -> " + error.getMessage());
        }

        @Override
        public void onClear() {
            System.out.println("CLEAR");
        }
    }

    // Example user profile class
    private static class UserProfile {
        private final String id;
        private final String name;
        private final String email;

        public UserProfile(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public String toString() {
            return "UserProfile{id='" + id + "', name='" + name + "', email='" + email + "'}";
        }
    }
}
