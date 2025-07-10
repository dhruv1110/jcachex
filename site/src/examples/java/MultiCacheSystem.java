import io.github.dhruv1110.jcachex.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.time.Duration;
import java.util.*;

public class MultiCacheManager {
    private final ConcurrentMap<String, Cache<?, ?>> cacheRegistry;
    private final CacheConfig<?> defaultConfig;

    public MultiCacheManager() {
        this.cacheRegistry = new ConcurrentHashMap<>();
        this.defaultConfig = CacheConfig.builder()
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats(true)
                .build();
    }

    // Create specialized caches for different domains
    public void initializeCaches() {
        // User cache - frequently accessed, longer TTL
        Cache<String, User> userCache = createCache(
                "users",
                CacheConfig.<String, User>builder()
                        .maximumSize(5000L)
                        .expireAfterWrite(Duration.ofHours(4))
                        .evictionStrategy(EvictionStrategy.LRU)
                        .recordStats(true)
                        .build());

        // Session cache - short TTL, high volume
        Cache<String, Session> sessionCache = createCache(
                "sessions",
                CacheConfig.<String, Session>builder()
                        .maximumSize(10000L)
                        .expireAfterWrite(Duration.ofMinutes(30))
                        .expireAfterAccess(Duration.ofMinutes(15))
                        .evictionStrategy(EvictionStrategy.LRU)
                        .recordStats(true)
                        .build());

        // Product cache - large objects, weight-based eviction
        Cache<String, Product> productCache = createCache(
                "products",
                CacheConfig.<String, Product>builder()
                        .maximumSize(2000L)
                        .maximumWeight(50 * 1024 * 1024) // 50MB
                        .expireAfterWrite(Duration.ofHours(12))
                        .evictionStrategy(EvictionStrategy.LRU)
                        .weigher((key, value) -> key.length() + estimateProductSize(value))
                        .recordStats(true)
                        .build());

        // Configuration cache - rarely changes, very long TTL
        Cache<String, String> configCache = createCache(
                "config",
                CacheConfig.<String, String>builder()
                        .maximumSize(500L)
                        .expireAfterWrite(Duration.ofDays(1))
                        .recordStats(true)
                        .build());
    }

    @SuppressWarnings("unchecked")
    private <K, V> Cache<K, V> createCache(String name, CacheConfig<K, V> config) {
        Cache<K, V> cache = new DefaultCache<>(config);
        cacheRegistry.put(name, cache);
        return cache;
    }

    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name) {
        return (Cache<K, V>) cacheRegistry.get(name);
    }

    // Coordinated cache operations
    public void invalidateUser(String userId) {
        // Invalidate across multiple caches
        Cache<String, User> userCache = getCache("users");
        Cache<String, Session> sessionCache = getCache("sessions");

        userCache.invalidate(userId);

        // Invalidate all sessions for this user
        sessionCache.asMap().entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            return session.getUserId().equals(userId);
        });
    }

    // Cache warming strategy
    public void warmCaches() {
        // Warm user cache with VIP users
        Cache<String, User> userCache = getCache("users");
        List<User> vipUsers = loadVipUsers(); // Load from database
        vipUsers.forEach(user -> userCache.put(user.getId(), user));

        // Warm product cache with featured products
        Cache<String, Product> productCache = getCache("products");
        List<Product> featuredProducts = loadFeaturedProducts();
        featuredProducts.forEach(product -> productCache.put(product.getId(), product));

        // Warm config cache
        Cache<String, String> configCache = getCache("config");
        Map<String, String> configs = loadConfigurations();
        configCache.putAll(configs);
    }

    // Unified cache statistics
    public CacheSystemStats getSystemStats() {
        Map<String, CacheStats> allStats = new HashMap<>();
        long totalSize = 0;
        double totalHitRate = 0;

        for (Map.Entry<String, Cache<?, ?>> entry : cacheRegistry.entrySet()) {
            String cacheName = entry.getKey();
            Cache<?, ?> cache = entry.getValue();
            CacheStats stats = cache.stats();

            allStats.put(cacheName, stats);
            totalSize += cache.size();
            totalHitRate += stats.hitRate();
        }

        return new CacheSystemStats(
                allStats,
                totalSize,
                totalHitRate / cacheRegistry.size());
    }

    // Graceful shutdown
    public void shutdown() {
        cacheRegistry.values().forEach(cache -> {
            // Perform cleanup if needed
            cache.invalidateAll();
        });
        cacheRegistry.clear();
    }

    private int estimateProductSize(Product product) {
        // Estimate memory footprint
        return product.getName().length() * 2 +
                product.getDescription().length() * 2 +
                100; // Base object size
    }

    private List<User> loadVipUsers() {
        // Load VIP users from database
        return Arrays.asList(
                new User("vip1", "VIP User 1", "vip1@example.com"),
                new User("vip2", "VIP User 2", "vip2@example.com"));
    }

    private List<Product> loadFeaturedProducts() {
        // Load featured products from database
        return Arrays.asList(
                new Product("featured1", "Featured Product 1", "Description 1"),
                new Product("featured2", "Featured Product 2", "Description 2"));
    }

    private Map<String, String> loadConfigurations() {
        // Load system configurations
        return Map.of(
                "app.name", "JCacheX Example",
                "app.version", "1.0.0",
                "feature.enabled", "true");
    }
}
