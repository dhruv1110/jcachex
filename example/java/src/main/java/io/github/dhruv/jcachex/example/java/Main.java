package io.github.dhruv.jcachex.example.java;

import io.github.dhruv.jcachex.Cache;
import io.github.dhruv.jcachex.CacheBuilder;
import io.github.dhruv.jcachex.CacheConfig;
import io.github.dhruv.jcachex.CacheEventListener;
import io.github.dhruv.jcachex.CacheStats;
import io.github.dhruv.jcachex.EvictionReason;
import io.github.dhruv.jcachex.EvictionStrategy;
import io.github.dhruv.jcachex.LRUEvictionStrategy;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        // Create a cache with LRU eviction
        Cache<String, String> cache = CacheBuilder.<String, String>newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(5))
            .evictionStrategy(new LRUEvictionStrategy<>())
            .build();

        // Synchronous operations
        cache.put("key1", "value1");
        String value = cache.get("key1");
        System.out.println("Value for key1: " + value);

        // Asynchronous operations
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CompletableFuture<String> future = cache.getAsync("key1", executor);
        future.thenAccept(v -> System.out.println("Async value for key1: " + v));

        // Cache stats
        CacheStats stats = cache.stats();
        System.out.println("Cache stats: " + stats);

        // Cache event listener
        cache.addListener(new CacheEventListener<String, String>() {
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
        });

        // Cleanup
        executor.shutdown();
        cache.clear();
    }
}
