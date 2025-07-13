import io.github.dhruv1110.jcachex.*;
import java.time.Duration;

public class BasicCacheExample {
    public static void main(String[] args) {
        // Create a basic cache using convenience methods (JCacheXBuilder)
        Cache<String, String> cache = JCacheXBuilder.forReadHeavyWorkload()
                .name("users")
                .maximumSize(1000L) // Maximum 1000 cached items
                .expireAfterWrite(Duration.ofMinutes(30)) // Items expire after 30 minutes
                .recordStats(true) // Enable performance metrics
                .build();

        // Basic cache operations
        cache.put("user:123", "John Doe");
        cache.put("user:456", "Jane Smith");

        // Retrieve values
        String user1 = cache.get("user:123"); // Returns "John Doe"
        String user2 = cache.get("user:789"); // Returns null (not found)

        System.out.println("Found user1: " + user1);
        System.out.println("Found user2: " + user2);

        // Check cache statistics
        CacheStats stats = cache.stats();
        System.out.println("Cache size: " + cache.size());
        System.out.println("Hit rate: " + String.format("%.2f%%", stats.hitRate() * 100));
        System.out.println("Miss rate: " + String.format("%.2f%%", stats.missRate() * 100));

        // Invalidate specific entries
        cache.invalidate("user:123");

        // Clear all entries
        cache.invalidateAll();

        // Demonstrate all convenience methods
        demonstrateConvenienceMethods();
    }

    private static void demonstrateConvenienceMethods() {
        System.out.println("\n=== JCacheX Convenience Methods ===");

        // Core profiles - one-liner cache creation
        Cache<String, String> readHeavy = JCacheXBuilder.forReadHeavyWorkload()
                .name("read-heavy").maximumSize(1000L).build();

        Cache<String, String> writeHeavy = JCacheXBuilder.forWriteHeavyWorkload()
                .name("write-heavy").maximumSize(1000L).build();

        Cache<String, String> memoryEfficient = JCacheXBuilder.forMemoryConstrainedEnvironment()
                .name("memory-efficient").maximumSize(100L).build();

        Cache<String, String> highPerformance = JCacheXBuilder.forHighPerformance()
                .name("high-performance").maximumSize(10000L).build();

        // Specialized profiles
        Cache<String, String> sessionCache = JCacheXBuilder.forSessionStorage()
                .name("sessions").build(); // Auto-configured TTL

        Cache<String, String> apiCache = JCacheXBuilder.forApiResponseCaching()
                .name("api-cache").build(); // Auto-configured TTL

        Cache<String, String> computeCache = JCacheXBuilder.forComputationCaching()
                .name("compute-cache").build(); // Long TTL for expensive computations

        // Advanced profiles
        Cache<String, String> mlCache = JCacheXBuilder.forMachineLearning()
                .name("ml-cache").maximumSize(1000L).build();

        Cache<String, String> ultraFast = JCacheXBuilder.forUltraLowLatency()
                .name("ultra-fast").maximumSize(100000L).build();

        Cache<String, String> hardwareOptimized = JCacheXBuilder.forHardwareOptimization()
                .name("hardware-optimized").maximumSize(1000L).build();

        Cache<String, String> distributed = JCacheXBuilder.forDistributedCaching()
                .name("distributed").maximumSize(5000L).build();

        System.out.println("Created 11 different cache types with convenience methods");
        System.out.println("Read-heavy cache size: " + readHeavy.size());
        System.out.println("Ultra-fast cache configured for: " + ultraFast.name());
    }
}
