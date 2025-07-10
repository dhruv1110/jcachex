import io.github.dhruv1110.jcachex.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Duration;
import java.util.Map;

public class ThreadSafeCacheExample {
    private final Cache<String, String> cache;
    private final AtomicInteger hitCount = new AtomicInteger(0);
    private final AtomicInteger missCount = new AtomicInteger(0);

    public ThreadSafeCacheExample() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats(true)
                .build();

        this.cache = new DefaultCache<>(config);
    }

    // Thread-safe cache operations
    public String getOrLoad(String key) {
        String value = cache.get(key);
        if (value != null) {
            hitCount.incrementAndGet();
            return value;
        }

        // Atomic put-if-absent operation
        missCount.incrementAndGet();
        return cache.computeIfAbsent(key, k -> {
            // Simulate expensive computation
            return "computed-" + k + "-" + Thread.currentThread().getName();
        });
    }

    // Concurrent cache stress test
    public void stressTest(int numThreads, int operationsPerThread) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "key-" + (j % 100); // Overlapping keys
                        String value = getOrLoad(key);

                        // Simulate some work
                        Thread.sleep(1);

                        // Update cache
                        cache.put(key, "updated-" + threadId + "-" + j);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Print results
        System.out.println("Stress test completed:");
        System.out.println("Cache size: " + cache.size());
        System.out.println("Hit count: " + hitCount.get());
        System.out.println("Miss count: " + missCount.get());
        System.out.println("Cache stats: " + cache.stats());
    }

    // Thread-safe bulk operations
    public void bulkUpdate(Map<String, String> updates) {
        // Use parallel streams for concurrent updates
        updates.entrySet().parallelStream().forEach(entry -> {
            cache.put(entry.getKey(), entry.getValue());
        });
    }

    // Atomic increment operation
    public int atomicIncrement(String counterKey) {
        return Integer.parseInt(cache.compute(counterKey, (key, value) -> {
            int currentValue = value != null ? Integer.parseInt(value) : 0;
            return String.valueOf(currentValue + 1);
        }));
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadSafeCacheExample example = new ThreadSafeCacheExample();

        // Run stress test
        example.stressTest(10, 1000);

        // Test atomic operations
        for (int i = 0; i < 100; i++) {
            int value = example.atomicIncrement("counter");
            System.out.println("Counter value: " + value);
        }
    }
}
