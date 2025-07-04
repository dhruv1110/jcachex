package io.github.dhruv1110.jcachex.example.java;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEventListener;
import io.github.dhruv1110.jcachex.CacheStats;
import io.github.dhruv1110.jcachex.DefaultCache;
import io.github.dhruv1110.jcachex.EvictionReason;
import io.github.dhruv1110.jcachex.eviction.LRUEvictionStrategy;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        // Create a cache with LRU eviction and event listener
        CacheConfig<String, String> config = CacheConfig.<String, String>newBuilder()
                .maximumSize(100L)
                .expireAfterWrite(Duration.ofMinutes(5))
                .evictionStrategy(new LRUEvictionStrategy<>())
                .recordStats(true)
                .addListener(new CacheEventListener<String, String>() {
                    @Override
                    public void onPut(String key, String value) {
                        System.out.println("Put: " + key + " = " + value);
                    }

                    @Override
                    public void onRemove(String key, String value) {
                        System.out.println("Remove: " + key + " = " + value);
                    }

                    @Override
                    public void onEvict(String key, String value, EvictionReason reason) {
                        System.out.println("Evict: " + key + " = " + value + " (" + reason + ")");
                    }

                    @Override
                    public void onExpire(String key, String value) {
                        System.out.println("Expire: " + key + " = " + value);
                    }

                    @Override
                    public void onLoad(String key, String value) {
                        System.out.println("Load: " + key + " = " + value);
                    }

                    @Override
                    public void onLoadError(String key, Throwable error) {
                        System.out.println("LoadError: " + key + " = " + error.getMessage());
                    }

                    @Override
                    public void onClear() {
                        System.out.println("Cache cleared");
                    }
                })
                .build();

        Cache<String, String> cache = new DefaultCache<>(config);

        // Synchronous operations
        cache.put("key1", "value1");
        String value = cache.get("key1");
        System.out.println("Value for key1: " + value);

        // Asynchronous operations
        CompletableFuture<String> future = cache.getAsync("key1");
        future.thenAccept(v -> System.out.println("Async value for key1: " + v));

        // More operations
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // Cache stats
        CacheStats stats = cache.stats();
        System.out.println("Cache stats: " + stats.toString());
        System.out.println("Hit rate: " + stats.hitRate());
        System.out.println("Cache size: " + cache.size());

        // Remove a key
        cache.remove("key2");

        // Clear cache
        cache.clear();

        // Show final stats
        System.out.println("Final cache size: " + cache.size());
    }
}
