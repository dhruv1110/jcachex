import io.github.dhruv1110.jcachex.*;
import java.time.Duration;

public class BasicCacheExample {
    public static void main(String[] args) {
        // Create a basic cache configuration
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize(1000L) // Maximum 1000 cached items
                .expireAfterWrite(Duration.ofMinutes(30)) // Items expire after 30 minutes
                .recordStats(true) // Enable performance metrics
                .build();

        // Create cache instance
        Cache<String, String> cache = new DefaultCache<>(config);

        // Basic cache operations
        cache.put("user:123", "John Doe");
        cache.put("user:456", "Jane Smith");

        // Retrieve values
        String user1 = cache.get("user:123"); // Returns "John Doe"
        String user2 = cache.get("user:789"); // Returns null (not found)

        // Check cache statistics
        CacheStats stats = cache.stats();
        System.out.println("Cache size: " + cache.size());
        System.out.println("Hit rate: " + String.format("%.2f%%", stats.hitRate() * 100));
        System.out.println("Miss rate: " + String.format("%.2f%%", stats.missRate() * 100));

        // Invalidate specific entries
        cache.invalidate("user:123");

        // Clear all entries
        cache.invalidateAll();
    }
}
