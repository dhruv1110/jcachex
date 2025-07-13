import type { CodeTab } from '../types';

// Progressive Learning Examples for Hero Section
export const PROGRESSIVE_LEARNING_EXAMPLES: CodeTab[] = [
    {
        id: 'thirty-second',
        label: '30-Second Start',
        language: 'java',
        code: `// Single line cache creation - immediate success!
Cache<String, String> cache = JCacheX.create().build();

// Put and get in 3 lines
cache.put("user1", "Alice");
String user = cache.get("user1"); // Alice retrieved instantly

System.out.println("✅ Success! Retrieved: " + user);
// That's it! Production-ready with sensible defaults`
    },
    {
        id: 'five-minute',
        label: '5-Minute Power',
        language: 'java',
        code: `// Show the "magic" of profiles - ONE line gives optimal config
Cache<String, Product> cache = JCacheXBuilder
    .forReadHeavyWorkload()  // 11.5ns GET performance automatically!
    .name("products")
    .maximumSize(10000L)
    .build();

// Lightning-fast operations with zero tuning
cache.put("product42", new Product("Premium Widget", 99.99));
Product product = cache.get("product42");

System.out.println("⚡ Retrieved in ~11.5ns: " + product.getName());
System.out.println("🎯 Hit rate: " + cache.stats().hitRate() * 100 + "%");`
    },
    {
        id: 'production-ready',
        label: 'Production Ready',
        language: 'java',
        code: `// Complete production setup with monitoring
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(50000L)
    .expireAfterWrite(Duration.ofHours(2))
    .recordStats(true)
    .evictionListener(this::onEviction)
    .loadingCache(this::loadFromDatabase)
    .build();

// Automatic database loading with error handling
User user = cache.get("user123"); // Loads from DB if not cached

// Production monitoring
CacheStats stats = cache.stats();
System.out.printf("Hit rate: %.2f%%, Load time: %.2fms",
    stats.hitRate() * 100, stats.averageLoadPenalty() / 1_000_000.0);`
    }
];
