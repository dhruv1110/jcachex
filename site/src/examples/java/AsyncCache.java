import io.github.dhruv1110.jcachex.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AsyncCacheExample {
    private final Cache<String, String> cache;
    private final ExecutorService asyncExecutor;

    public AsyncCacheExample() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats(true)
                .build();

        this.cache = new DefaultCache<>(config);
        this.asyncExecutor = Executors.newFixedThreadPool(4);
    }

    // Asynchronous cache loading
    public CompletableFuture<String> getValueAsync(String key) {
        String cachedValue = cache.get(key);
        if (cachedValue != null) {
            return CompletableFuture.completedFuture(cachedValue);
        }

        // Load asynchronously if not in cache
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate expensive operation
                Thread.sleep(100);
                String value = "loaded-" + key;
                cache.put(key, value);
                return value;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }, asyncExecutor);
    }

    // Bulk asynchronous loading
    public CompletableFuture<Map<String, String>> getMultipleAsync(List<String> keys) {
        Map<String, String> results = new HashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String key : keys) {
            CompletableFuture<Void> future = getValueAsync(key)
                    .thenAccept(value -> results.put(key, value));
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> results);
    }

    // Non-blocking cache refresh
    public CompletableFuture<Void> refreshCacheAsync(String key) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Simulate loading fresh data
                Thread.sleep(200);
                String freshValue = "refreshed-" + key + "-" + System.currentTimeMillis();
                cache.put(key, freshValue);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }, asyncExecutor);
    }

    // Asynchronous cache warming
    public CompletableFuture<Void> warmCacheAsync(List<String> keys) {
        List<CompletableFuture<Void>> warmupTasks = keys.stream()
                .map(key -> CompletableFuture.runAsync(() -> {
                    String value = "warmed-" + key;
                    cache.put(key, value);
                }, asyncExecutor))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(warmupTasks.toArray(new CompletableFuture[0]));
    }

    public void shutdown() {
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
