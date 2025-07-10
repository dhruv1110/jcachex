import React from 'react';
import {
    Container,
    Typography,
    Box,
    Card,
    CardContent,
    List,
    ListItem,
    ListItemText,
    Chip,
    Alert,
    Button,
    Stack,
    useTheme,
    useMediaQuery
} from '@mui/material';
import {
    Code as CodeIcon,
    Coffee as JavaIcon,
    Extension as ExtensionIcon,
    Speed as SpeedIcon,
    Settings as SettingsIcon,
    Memory as MemoryIcon,
    Security as SecurityIcon,
    Sync as SyncIcon,
    Dashboard as DashboardIcon,
    Storage as StorageIcon,
    Timeline as TimelineIcon,
    Api as ApiIcon,
    Build as BuildIcon,
    Analytics as AnalyticsIcon,
    Architecture as ArchitectureIcon,
    Timer as TimerIcon,
    GitHub as GitHubIcon,
    Link as LinkIcon,
    PlayArrow as PlayArrowIcon,
    Computer as ComputerIcon,
    CloudSync as CloudSyncIcon,
} from '@mui/icons-material';
import Layout from './Layout';
import CodeTabs from './CodeTabs';
import { MetaTags, Breadcrumbs } from './SEO';
import type { CodeTab } from '../types';

const Examples: React.FC = () => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    // Define navigation items for the sidebar
    const navigationItems = [
        {
            id: 'java-examples',
            title: 'Java Examples',
            icon: <JavaIcon />,
            children: [
                { id: 'basic-cache', title: 'Basic Cache', icon: <CodeIcon /> },
                { id: 'complex-cache-configurations', title: 'Complex Cache Configurations', icon: <SettingsIcon /> },
                { id: 'non-blocking-caching', title: 'Non-blocking Caching', icon: <SyncIcon /> },
                { id: 'thread-safety', title: 'Thread Safety', icon: <SecurityIcon /> },
                { id: 'multi-cache-system', title: 'Multi Cache System', icon: <ArchitectureIcon /> },
                { id: 'observability', title: 'Observability', icon: <AnalyticsIcon /> },
            ],
        },
        {
            id: 'kotlin-examples',
            title: 'Kotlin Examples',
            icon: <ExtensionIcon />,
            children: [
                { id: 'extensions-usage', title: 'Extensions Usage', icon: <ExtensionIcon /> },
                { id: 'coroutines-support', title: 'Coroutines Support', icon: <CloudSyncIcon /> },
            ],
        },
        {
            id: 'springboot-examples',
            title: 'Spring Boot Examples',
            icon: <SpeedIcon />,
            children: [
                { id: 'spring-jpa', title: 'Spring JPA', icon: <StorageIcon /> },
                { id: 'rest-api', title: 'Rest API', icon: <ApiIcon /> },
                { id: 'heavy-computation-process', title: 'Heavy Computation Process', icon: <ComputerIcon /> },
            ],
        },
    ];

    const sidebarConfig = {
        title: "Examples",
        navigationItems: navigationItems,
        expandedByDefault: true
    };

    // SEO data
    const seoData = {
        title: 'JCacheX Examples and Code Samples',
        description: 'Comprehensive examples and code samples for JCacheX. Learn Java, Kotlin, and Spring Boot integration patterns.',
        keywords: ['JCacheX examples', 'cache examples', 'Java cache tutorial', 'Spring cache examples', 'Kotlin cache'],
        canonical: 'https://dhruv1110.github.io/jcachex/examples'
    };

    return (
        <Layout sidebarConfig={sidebarConfig}>
            <MetaTags seo={seoData} />
            <Breadcrumbs items={[
                { label: 'Home', path: '/' },
                { label: 'Examples', path: '/examples', current: true }
            ]} />

            <Container
                maxWidth={false}
                sx={{
                    py: 4,
                    px: { xs: 2, sm: 3, md: 0 },
                    pr: { xs: 2, sm: 3, md: 4 },
                    pl: { xs: 2, sm: 3, md: 0 },
                    ml: { xs: 0, md: 0 },
                    mt: { xs: 1, md: 0 },
                    minHeight: { xs: 'calc(100vh - 80px)', md: 'auto' },
                }}
            >
                {/* Header */}
                <Box sx={{ textAlign: 'center', mb: 6 }}>
                    <Typography variant="h2" component="h1" gutterBottom sx={{ fontWeight: 700, mb: 2 }}>
                        JCacheX Examples
                    </Typography>
                    <Typography variant="h5" color="text.secondary" sx={{ mb: 4 }}>
                        Comprehensive code examples and integration patterns for production applications
                    </Typography>
                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr 1fr' },
                        gap: 2,
                        justifyItems: 'center',
                        maxWidth: '600px',
                        mx: 'auto'
                    }}>
                        <Chip
                            icon={<JavaIcon />}
                            label="Java Examples"
                            color="primary"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<ExtensionIcon />}
                            label="Kotlin Extensions"
                            color="secondary"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<SpeedIcon />}
                            label="Spring Boot"
                            color="success"
                            sx={{ px: 2, py: 1 }}
                        />
                    </Box>
                </Box>

                {/* Java Examples Section */}
                <Box id="java-examples" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <JavaIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Java Examples
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
                        Comprehensive Java examples covering basic usage to advanced enterprise patterns.
                    </Typography>
                </Box>

                {/* Basic Cache */}
                <Box id="basic-cache" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <CodeIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Basic Cache
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Essential cache setup and usage patterns for getting started with JCacheX.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                When to use Basic Cache:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Simple key-value caching for frequently accessed data" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="User session management and preferences" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Configuration data and reference lookups" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Caching expensive computation results" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'basic-usage',
                            label: 'Basic Usage',
                            language: 'java',
                            code: `import io.github.dhruv1110.jcachex.*;
import java.time.Duration;

public class BasicCacheExample {
    public static void main(String[] args) {
        // Create a basic cache configuration
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
            .maximumSize(1000L)  // Maximum 1000 cached items
            .expireAfterWrite(Duration.ofMinutes(30))  // Items expire after 30 minutes
            .recordStats(true)   // Enable performance metrics
            .build();

        // Create cache instance
        Cache<String, String> cache = new DefaultCache<>(config);

        // Basic cache operations
        cache.put("user:123", "John Doe");
        cache.put("user:456", "Jane Smith");

        // Retrieve values
        String user1 = cache.get("user:123");  // Returns "John Doe"
        String user2 = cache.get("user:789");  // Returns null (not found)

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
}`
                        },
                        {
                            id: 'object-caching',
                            label: 'Object Caching',
                            language: 'java',
                            code: `import io.github.dhruv1110.jcachex.*;
import java.time.Duration;
import java.time.LocalDateTime;

    // Domain object for caching
public class UserProfile {
        private final String userId;
        private final String name;
        private final String email;
    private final LocalDateTime lastLogin;

    public UserProfile(String userId, String name, String email, LocalDateTime lastLogin) {
            this.userId = userId;
            this.name = name;
            this.email = email;
        this.lastLogin = lastLogin;
        }

    // Getters
        public String getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    public LocalDateTime getLastLogin() { return lastLogin; }

    @Override
    public String toString() {
        return String.format("UserProfile{userId='%s', name='%s', email='%s', lastLogin=%s}",
                           userId, name, email, lastLogin);
    }
}

public class ObjectCacheExample {
    public static void main(String[] args) {
        // Configure cache for user profiles
        CacheConfig<String, UserProfile> config = CacheConfig.<String, UserProfile>builder()
            .maximumSize(500L)
            .expireAfterWrite(Duration.ofHours(4))
            .evictionStrategy(EvictionStrategy.LRU)
            .recordStats(true)
            .build();

        Cache<String, UserProfile> userCache = new DefaultCache<>(config);

        // Create and cache user profiles
        UserProfile user1 = new UserProfile("123", "Alice Johnson", "alice@example.com", LocalDateTime.now());
        UserProfile user2 = new UserProfile("456", "Bob Wilson", "bob@example.com", LocalDateTime.now().minusHours(2));

        userCache.put(user1.getUserId(), user1);
        userCache.put(user2.getUserId(), user2);

        // Retrieve cached objects
        UserProfile cachedUser = userCache.get("123");
        if (cachedUser != null) {
            System.out.println("Retrieved user: " + cachedUser);
        }

        // Batch operations
        Map<String, UserProfile> batch = Map.of(
            "789", new UserProfile("789", "Charlie Brown", "charlie@example.com", LocalDateTime.now()),
            "101", new UserProfile("101", "Diana Prince", "diana@example.com", LocalDateTime.now().minusMinutes(30))
        );

        userCache.putAll(batch);

        // Performance monitoring
        CacheStats stats = userCache.stats();
        System.out.println("Cache performance:");
        System.out.println("  Requests: " + stats.requestCount());
        System.out.println("  Hits: " + stats.hitCount());
        System.out.println("  Misses: " + stats.missCount());
        System.out.println("  Hit rate: " + String.format("%.2f%%", stats.hitRate() * 100));
    }
}`
                        }
                    ]} />
                </Box>

                {/* Complex Cache Configurations */}
                <Box id="complex-cache-configurations" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <SettingsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Complex Cache Configurations
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Advanced configuration patterns for production environments with custom eviction policies and listeners.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Advanced Configuration Features:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Custom eviction strategies and policies" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Cache event listeners for monitoring" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Weight-based memory management" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Composite eviction strategies" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'advanced-config',
                            label: 'Advanced Configuration',
                            language: 'java',
                            code: `import io.github.dhruv1110.jcachex.*;
import io.github.dhruv1110.jcachex.eviction.*;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// Custom cache event listener for monitoring
public class ProductionCacheListener<K, V> implements CacheEventListener<K, V> {
    private final Logger logger = LoggerFactory.getLogger(ProductionCacheListener.class);

    @Override
    public void onEviction(K key, V value, EvictionReason reason) {
        logger.info("Cache eviction: key={}, reason={}", key, reason);

        // Send metrics to monitoring system
        if (reason == EvictionReason.SIZE) {
            incrementMetric("cache.eviction.size");
        } else if (reason == EvictionReason.TIME) {
            incrementMetric("cache.eviction.time");
        }
    }

    @Override
    public void onRemoval(K key, V value, EvictionReason reason) {
        logger.debug("Cache removal: key={}, reason={}", key, reason);
    }

    private void incrementMetric(String metric) {
        // Integration with metrics system (e.g., Micrometer, Prometheus)
        // MetricsRegistry.counter(metric).increment();
    }
}

// Custom weigher for memory-based eviction
public class ProductWeigher implements Weigher<String, Product> {
    @Override
    public int weigh(String key, Product value) {
        // Calculate memory footprint
        int keyWeight = key.length() * 2; // String overhead
        int valueWeight = value.getName().length() * 2 +
                         value.getDescription().length() * 2 +
                         100; // Object overhead
        return keyWeight + valueWeight;
    }
}

public class ComplexCacheConfigExample {
    public static void main(String[] args) {
        // Custom executor for cache operations
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        // Composite eviction strategy
        CompositeEvictionStrategy<String, Product> evictionStrategy =
            CompositeEvictionStrategy.<String, Product>builder()
                .addStrategy(new LRUEvictionStrategy<>())
                .addStrategy(new WeightBasedEvictionStrategy<>(new ProductWeigher()))
                .addStrategy(new IdleTimeEvictionStrategy<>(Duration.ofHours(2)))
                .build();

        // Advanced cache configuration
        CacheConfig<String, Product> config = CacheConfig.<String, Product>builder()
            .maximumSize(10000L)
            .maximumWeight(1024 * 1024) // 1MB max weight
            .expireAfterWrite(Duration.ofHours(6))
            .expireAfterAccess(Duration.ofHours(2))
            .evictionStrategy(evictionStrategy)
            .eventListener(new ProductionCacheListener<>())
            .recordStats(true)
            .statsRecordingExecutor(executor)
            .cleanupExecutor(executor)
            .build();

        Cache<String, Product> productCache = new DefaultCache<>(config);

        // Cache warming with bulk operations
        warmupCache(productCache);

        // Monitor cache health
        monitorCacheHealth(productCache);

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down cache...");
            CacheStats finalStats = productCache.stats();
            System.out.println("Final cache statistics:");
            System.out.println("  Total requests: " + finalStats.requestCount());
            System.out.println("  Hit rate: " + String.format("%.2f%%", finalStats.hitRate() * 100));
            System.out.println("  Eviction count: " + finalStats.evictionCount());

            executor.shutdown();
        }));
    }

    private static void warmupCache(Cache<String, Product> cache) {
        // Simulate cache warming from database
        for (int i = 1; i <= 1000; i++) {
            String productId = "product-" + i;
            Product product = new Product(productId, "Product " + i, "Description for product " + i);
            cache.put(productId, product);
        }
        System.out.println("Cache warmed up with " + cache.size() + " products");
    }

    private static void monitorCacheHealth(Cache<String, Product> cache) {
        // Scheduled health check
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        monitor.scheduleAtFixedRate(() -> {
            CacheStats stats = cache.stats();
            double hitRate = stats.hitRate();

            System.out.println("Cache health check:");
            System.out.println("  Size: " + cache.size());
            System.out.println("  Hit rate: " + String.format("%.2f%%", hitRate * 100));

            // Alert if hit rate is too low
            if (hitRate < 0.8) {
                System.out.println("WARNING: Cache hit rate is below 80%");
            }
        }, 0, 60, TimeUnit.SECONDS);
    }
}`
                        }
                    ]} />
                </Box>

                {/* Non-blocking Caching */}
                <Box id="non-blocking-caching" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <SyncIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Non-blocking Caching
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Asynchronous caching patterns that don't block application threads during cache operations.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Non-blocking Benefits:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Improved application responsiveness" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Better resource utilization" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Reduced thread contention" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Graceful degradation under load" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'async-cache-operations',
                            label: 'Async Cache Operations',
                            language: 'java',
                            code: `import io.github.dhruv1110.jcachex.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
}`
                        }
                    ]} />
                </Box>

                {/* Thread Safety */}
                <Box id="thread-safety" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Thread Safety
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Ensuring cache operations are thread-safe in concurrent environments.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Thread Safety Considerations:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Concurrent read/write operations" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Atomic cache operations" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Lock-free data structures" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Consistent state management" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'thread-safe-cache',
                            label: 'Thread-Safe Cache',
                            language: 'java',
                            code: `import io.github.dhruv1110.jcachex.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
        return cache.compute(counterKey, (key, value) -> {
            int currentValue = value != null ? Integer.parseInt(value) : 0;
            return String.valueOf(currentValue + 1);
        });
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
}`
                        }
                    ]} />
                </Box>

                {/* Multi Cache System */}
                <Box id="multi-cache-system" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <ArchitectureIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Multi Cache System
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Managing multiple cache instances with different configurations and purposes.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Multi-Cache Architecture:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="L1 (Memory) and L2 (Disk) cache layers" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Domain-specific cache partitioning" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Cache coordination and consistency" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Unified cache management" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'multi-cache-manager',
                            label: 'Multi-Cache Manager',
                            language: 'java',
                            code: `import io.github.dhruv1110.jcachex.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
                .build()
        );

        // Session cache - short TTL, high volume
        Cache<String, Session> sessionCache = createCache(
            "sessions",
            CacheConfig.<String, Session>builder()
                .maximumSize(10000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(15))
                .evictionStrategy(EvictionStrategy.LRU)
                .recordStats(true)
                .build()
        );

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
                .build()
        );

        // Configuration cache - rarely changes, very long TTL
        Cache<String, String> configCache = createCache(
            "config",
            CacheConfig.<String, String>builder()
                .maximumSize(500L)
                .expireAfterWrite(Duration.ofDays(1))
                .recordStats(true)
                .build()
        );
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
            totalHitRate / cacheRegistry.size()
        );
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
            new User("vip2", "VIP User 2", "vip2@example.com")
        );
    }

    private List<Product> loadFeaturedProducts() {
        // Load featured products from database
        return Arrays.asList(
            new Product("featured1", "Featured Product 1", "Description 1"),
            new Product("featured2", "Featured Product 2", "Description 2")
        );
    }

    private Map<String, String> loadConfigurations() {
        // Load system configurations
        return Map.of(
            "app.name", "JCacheX Example",
            "app.version", "1.0.0",
            "feature.enabled", "true"
        );
    }
}`
                        }
                    ]} />
                </Box>

                {/* Observability */}
                <Box id="observability" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <AnalyticsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Observability
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Monitoring, metrics, and observability patterns for production cache systems.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Observability Features:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Real-time metrics collection" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Performance dashboards" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Alerting and notifications" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Distributed tracing" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'cache-observability',
                            label: 'Cache Observability',
                            language: 'java',
                            code: `import io.github.dhruv1110.jcachex.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ObservableCacheService {
    private final Cache<String, String> cache;
    private final MeterRegistry meterRegistry;
    private final Timer cacheLoadTimer;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final ScheduledExecutorService scheduler;

    public ObservableCacheService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Configure cache with observability
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats(true)
            .eventListener(new ObservabilityCacheEventListener())
            .build();

        this.cache = new DefaultCache<>(config);

        // Initialize metrics
        this.cacheLoadTimer = Timer.builder("cache.load.time")
            .description("Time taken to load cache values")
            .register(meterRegistry);

        this.cacheHitCounter = Counter.builder("cache.hits")
            .description("Number of cache hits")
            .register(meterRegistry);

        this.cacheMissCounter = Counter.builder("cache.misses")
            .description("Number of cache misses")
            .register(meterRegistry);

        // Register cache size gauge
        Gauge.builder("cache.size")
            .description("Current cache size")
            .register(meterRegistry, cache, Cache::size);

        // Register hit rate gauge
        Gauge.builder("cache.hit_rate")
            .description("Cache hit rate")
            .register(meterRegistry, cache, c -> c.stats().hitRate());

        // Start periodic metrics collection
        startMetricsCollection();
    }

    public String getValue(String key) {
        String value = cache.get(key);

        if (value != null) {
            cacheHitCounter.increment();
            return value;
        }

        cacheMissCounter.increment();

        // Load with timing
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            value = loadValue(key);
            cache.put(key, value);
            return value;
        } finally {
            sample.stop(cacheLoadTimer);
        }
    }

    private String loadValue(String key) {
        // Simulate expensive operation
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return "loaded-" + key;
    }

    private void startMetricsCollection() {
        scheduler.scheduleAtFixedRate(() -> {
                CacheStats stats = cache.stats();

            // Custom metrics
            meterRegistry.gauge("cache.eviction_count", stats.evictionCount());
            meterRegistry.gauge("cache.load_exception_count", stats.loadExceptionCount());
            meterRegistry.gauge("cache.average_load_time", stats.averageLoadTime());

            // Log detailed metrics
            logCacheMetrics(stats);

            // Health checks
            performHealthChecks(stats);

        }, 0, 30, TimeUnit.SECONDS);
    }

    private void logCacheMetrics(CacheStats stats) {
        MDC.put("cache.size", String.valueOf(cache.size()));
        MDC.put("cache.hit_rate", String.format("%.2f", stats.hitRate()));
        MDC.put("cache.miss_rate", String.format("%.2f", stats.missRate()));
        MDC.put("cache.eviction_count", String.valueOf(stats.evictionCount()));

        logger.info("Cache metrics snapshot");

                MDC.clear();
            }

    private void performHealthChecks(CacheStats stats) {
        // Alert on low hit rate
        if (stats.hitRate() < 0.7) {
            alertService.sendAlert(
                "Cache hit rate is low: " + String.format("%.2f%%", stats.hitRate() * 100),
                AlertLevel.WARNING
            );
        }

        // Alert on high eviction rate
        long totalRequests = stats.requestCount();
        if (totalRequests > 0) {
            double evictionRate = (double) stats.evictionCount() / totalRequests;
            if (evictionRate > 0.1) {
                alertService.sendAlert(
                    "High cache eviction rate: " + String.format("%.2f%%", evictionRate * 100),
                    AlertLevel.WARNING
                );
            }
        }

        // Alert on slow load times
        if (stats.averageLoadTime() > 500) {
            alertService.sendAlert(
                "Slow cache load time: " + stats.averageLoadTime() + "ms",
                AlertLevel.WARNING
            );
        }
    }

    public CacheHealthReport generateHealthReport() {
            CacheStats stats = cache.stats();

        return CacheHealthReport.builder()
            .cacheSize(cache.size())
                .hitRate(stats.hitRate())
                .missRate(stats.missRate())
                .evictionCount(stats.evictionCount())
                .averageLoadTime(stats.averageLoadTime())
                .requestCount(stats.requestCount())
            .healthScore(calculateHealthScore(stats))
            .timestamp(Instant.now())
                .build();
        }

    private double calculateHealthScore(CacheStats stats) {
        double score = 1.0;

        // Penalize low hit rate
        if (stats.hitRate() < 0.5) {
            score -= 0.4;
        } else if (stats.hitRate() < 0.7) {
            score -= 0.2;
        }

        // Penalize slow load times
        if (stats.averageLoadTime() > 200) {
            score -= 0.2;
        }

        // Penalize high eviction rate
        long totalRequests = stats.requestCount();
        if (totalRequests > 0) {
            double evictionRate = (double) stats.evictionCount() / totalRequests;
            if (evictionRate > 0.05) {
                score -= 0.3;
            }
        }

        return Math.max(0, score);
    }

    private class ObservabilityCacheEventListener implements CacheEventListener<String, String> {
        @Override
        public void onEviction(String key, String value, EvictionReason reason) {
            meterRegistry.counter("cache.evictions", "reason", reason.toString()).increment();

            if (reason == EvictionReason.SIZE) {
                logger.warn("Cache size-based eviction for key: {}", key);
            }
        }

        @Override
        public void onRemoval(String key, String value, EvictionReason reason) {
            meterRegistry.counter("cache.removals", "reason", reason.toString()).increment();
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}`
                        }
                    ]} />
                </Box>

                {/* Kotlin Examples Section */}
                <Box id="kotlin-examples" sx={{ mb: 8, mt: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <ExtensionIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Kotlin Examples
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
                        Kotlin-specific examples showcasing extensions and coroutines support.
                    </Typography>
                </Box>

                {/* Extensions Usage */}
                <Box id="extensions-usage" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <ExtensionIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Extensions Usage
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Idiomatic Kotlin extensions for JCacheX providing a more fluent API.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Kotlin Extensions Benefits:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="DSL-style configuration" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Operator overloading for intuitive access" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Extension functions for common operations" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Type-safe builders" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'kotlin-extensions',
                            label: 'Kotlin Extensions',
                            language: 'kotlin',
                            code: `import io.github.dhruv1110.jcachex.kotlin.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.hours

data class User(
    val id: String,
    val name: String,
    val email: String,
    val preferences: Map<String, String> = emptyMap()
)

class UserCacheService {

    // DSL-style cache configuration
    private val userCache = cache<String, User> {
        maxSize = 1000
        expireAfterWrite = 2.hours
        expireAfterAccess = 30.minutes
        evictionStrategy = EvictionStrategy.LRU
        recordStats = true
    }

    // Operator overloading for intuitive access
    suspend fun getUser(userId: String): User? {
        return userCache[userId] ?: loadUserFromDatabase(userId)?.also { user ->
            userCache[userId] = user
        }
    }

    // Extension functions for common operations
    suspend fun cacheUser(user: User) {
        userCache[user.id] = user
    }

    fun removeUser(userId: String) {
        userCache -= userId  // Operator overloading for removal
    }

    // Batch operations with extension functions
    suspend fun cacheUsers(users: List<User>) {
        users.forEach { user ->
            userCache[user.id] = user
        }
    }

    // Cache warming with extension functions
    suspend fun warmCache() {
        val popularUsers = loadPopularUsers()
        popularUsers.forEach { user ->
            userCache[user.id] = user
        }
    }

    // Statistics with extension properties
    fun getCacheStats(): String {
        return buildString {
            with(userCache.stats()) {
                append("Hit Rate: \${(hitRate() * 100).toInt()}%\\n")
                append("Size: \${userCache.size()}\\n")
                append("Evictions: \${evictionCount()}\\n")
                append("Avg Load Time: \${averageLoadTime()}ms")
            }
        }
    }

    // Type-safe cache operations
    inline fun <reified T> getCacheValue(key: String): T? {
        return userCache[key] as? T
    }

    // Extension function for conditional caching
    suspend fun cacheIfValid(user: User): Boolean {
        return if (user.email.isNotBlank()) {
            userCache[user.id] = user
            true
        } else {
            false
        }
    }

    private suspend fun loadUserFromDatabase(userId: String): User? {
        // Simulate database access
        delay(50)
        return User(
            id = userId,
            name = "User $userId",
            email = "user$userId@example.com",
            preferences = mapOf("theme" to "dark", "language" to "en")
        )
    }

    private suspend fun loadPopularUsers(): List<User> {
        // Simulate loading popular users
        delay(100)
        return (1..10).map { id ->
            User(
                id = "popular-$id",
                name = "Popular User $id",
                email = "popular$id@example.com"
            )
        }
    }
}`
                        }
                    ]} />
                </Box>

                {/* Coroutines Support */}
                <Box id="coroutines-support" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <CloudSyncIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Coroutines Support
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Seamless integration with Kotlin coroutines for asynchronous caching operations.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Coroutines Integration:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Suspend functions for non-blocking operations" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Flow-based cache events" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Structured concurrency support" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Cancellation-aware operations" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'coroutines-cache',
                            label: 'Coroutines Cache',
                            language: 'kotlin',
                            code: `import io.github.dhruv1110.jcachex.kotlin.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.minutes

class AsyncCacheService {

    private val cache = cache<String, String> {
        maxSize = 1000
        expireAfterWrite = 30.minutes
        recordStats = true
    }

    // Suspend function for async loading
    suspend fun getValueAsync(key: String): String {
        return cache[key] ?: loadValueAsync(key).also { value ->
            cache[key] = value
        }
    }

    // Parallel loading with coroutines
    suspend fun loadMultipleAsync(keys: List<String>): Map<String, String> {
        return coroutineScope {
            keys.map { key ->
                async { key to getValueAsync(key) }
            }.awaitAll().toMap()
        }
    }

    // Flow-based cache events
    fun cacheEvents(): Flow<CacheEvent> = flow {
        // Simulate cache events
        while (currentCoroutineContext().isActive) {
            delay(1000)
            emit(CacheEvent.HitRate(cache.stats().hitRate()))
        }
    }

    // Structured concurrency for cache operations
    suspend fun manageCacheLifecycle() = coroutineScope {
        // Launch cache warming
        val warmupJob = launch {
            warmCacheAsync()
        }

        // Launch cache cleanup
        val cleanupJob = launch {
            scheduleCleanup()
        }

        // Launch metrics collection
        val metricsJob = launch {
            collectMetrics()
        }

        // All jobs will be cancelled if parent scope is cancelled
        joinAll(warmupJob, cleanupJob, metricsJob)
    }

    // Cancellation-aware cache warming
    private suspend fun warmCacheAsync() {
        val keys = (1..1000).map { "key-$it" }

        keys.chunked(100).forEach { chunk ->
            // Check for cancellation
            ensureActive()

            // Process chunk in parallel
            coroutineScope {
                chunk.map { key ->
                    async {
                        val value = "warm-$key"
                        cache[key] = value
                    }
                }.awaitAll()
            }

            // Yield to other coroutines
            yield()
        }
    }

    // Periodic cache cleanup
    private suspend fun scheduleCleanup() {
        while (currentCoroutineContext().isActive) {
            delay(5.minutes)

            // Cleanup expired entries
            withContext(Dispatchers.Default) {
                cache.cleanUp()
            }
        }
    }

    // Metrics collection with Flow
    private suspend fun collectMetrics() {
        cacheEvents()
            .sample(30.seconds)
            .collect { event ->
                when (event) {
                    is CacheEvent.HitRate -> {
                        println("Cache hit rate: \${event.rate}")
                        if (event.rate < 0.7) {
                            println("WARNING: Low cache hit rate!")
                        }
                    }
                }
            }
    }

    // Timeout-aware cache operations
    suspend fun getWithTimeout(key: String, timeout: kotlin.time.Duration): String? {
        return try {
            withTimeout(timeout) {
                getValueAsync(key)
            }
        } catch (e: TimeoutCancellationException) {
            null
        }
    }

    // Channel-based cache updates
    suspend fun processUpdates(updates: ReceiveChannel<Pair<String, String>>) {
        for ((key, value) in updates) {
            cache[key] = value
            yield() // Cooperative cancellation
        }
    }

    private suspend fun loadValueAsync(key: String): String {
        // Simulate async loading
        delay(50)
        return "async-loaded-$key"
    }
}

sealed class CacheEvent {
    data class HitRate(val rate: Double) : CacheEvent()
    data class SizeChange(val size: Long) : CacheEvent()
}`
                        }
                    ]} />
                </Box>

                {/* Spring Boot Examples Section */}
                <Box id="springboot-examples" sx={{ mb: 8, mt: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <SpeedIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Spring Boot Examples
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
                        Spring Boot integration examples for enterprise applications.
                    </Typography>
                </Box>

                {/* Spring JPA */}
                <Box id="spring-jpa" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <StorageIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Spring JPA
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Integrating JCacheX with Spring Data JPA for efficient database caching.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Spring JPA Integration:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Repository-level caching" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Query result caching" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Entity-level cache management" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Transactional cache operations" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'spring-jpa-cache',
                            label: 'Spring JPA Cache',
                            language: 'java',
                            code: `import io.github.dhruv1110.jcachex.spring.annotation.JCacheXCacheable;
import io.github.dhruv1110.jcachex.spring.annotation.JCacheXCacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private boolean active;

    // Constructors, getters, setters
}

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @JCacheXCacheable(
        cacheName = "usersByEmail",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    Optional<User> findByEmail(String email);

    @JCacheXCacheable(
        cacheName = "activeUsers",
        expireAfterWrite = 15,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    @JCacheXCacheable(
        cacheName = "usersByName",
        key = "#name",
        expireAfterWrite = 20,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    List<User> findByNameContaining(String name);
}

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @JCacheXCacheable(
        cacheName = "users",
        expireAfterWrite = 60,
        expireAfterWriteUnit = TimeUnit.MINUTES,
        maximumSize = 10000
    )
    public User findById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    @JCacheXCacheable(
        cacheName = "userProfiles",
        key = "#userId",
        condition = "#includeDetails == true",
        expireAfterWrite = 45,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public UserProfile getUserProfile(String userId, boolean includeDetails) {
        User user = findById(userId);

        if (includeDetails) {
            return buildDetailedProfile(user);
        } else {
            return buildBasicProfile(user);
        }
    }

    @JCacheXCacheEvict(cacheName = "users")
    @JCacheXCacheEvict(cacheName = "usersByEmail", key = "#user.email")
    @JCacheXCacheEvict(cacheName = "activeUsers", condition = "#user.active")
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @JCacheXCacheEvict(cacheName = "users")
    @JCacheXCacheEvict(cacheName = "usersByEmail")
    @JCacheXCacheEvict(cacheName = "activeUsers")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    // Bulk operations with cache management
    @JCacheXCacheEvict(cacheName = "activeUsers")
    public void activateUsers(List<String> userIds) {
        userIds.forEach(id -> {
            User user = findById(id);
            user.setActive(true);
            userRepository.save(user);
        });
    }

    private UserProfile buildDetailedProfile(User user) {
        return UserProfile.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .active(user.isActive())
            .permissions(loadUserPermissions(user.getId()))
            .preferences(loadUserPreferences(user.getId()))
            .build();
    }

    private UserProfile buildBasicProfile(User user) {
        return UserProfile.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .active(user.isActive())
            .build();
    }

    private List<String> loadUserPermissions(String userId) {
        // Load user permissions from database
        return Arrays.asList("READ", "WRITE");
    }

    private Map<String, String> loadUserPreferences(String userId) {
        // Load user preferences from database
        return Map.of("theme", "dark", "language", "en");
    }
}`
                        }
                    ]} />
                </Box>

                {/* Rest API */}
                <Box id="rest-api" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <ApiIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Rest API
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Caching REST API responses and external service calls in Spring Boot.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                REST API Caching:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="HTTP response caching" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="External API call caching" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Request-level cache control" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Cache invalidation strategies" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'rest-api-cache',
                            label: 'REST API Cache',
                            language: 'java',
                            code: `import io.github.dhruv1110.jcachex.spring.annotation.JCacheXCacheable;
import io.github.dhruv1110.jcachex.spring.annotation.JCacheXCacheEvict;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.CacheControl;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final ExternalApiService externalApiService;

    public UserController(UserService userService, ExternalApiService externalApiService) {
        this.userService = userService;
        this.externalApiService = externalApiService;
    }

    @GetMapping("/users/{id}")
    @JCacheXCacheable(
        cacheName = "userResponses",
        key = "#id",
        expireAfterWrite = 10,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public ResponseEntity<User> getUser(@PathVariable String id) {
        User user = userService.findById(id);

        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
            .body(user);
    }

    @GetMapping("/users")
    @JCacheXCacheable(
        cacheName = "userListResponses",
        key = "#page + '-' + #size + '-' + #sort",
        expireAfterWrite = 5,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {

        List<User> users = userService.findUsers(page, size, sort);

        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
            .body(users);
    }

    @PostMapping("/users")
    @JCacheXCacheEvict(cacheName = "userListResponses")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/users/{id}")
    @JCacheXCacheEvict(cacheName = "userResponses", key = "#id")
    @JCacheXCacheEvict(cacheName = "userListResponses")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    @JCacheXCacheEvict(cacheName = "userResponses", key = "#id")
    @JCacheXCacheEvict(cacheName = "userListResponses")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}/profile")
    @JCacheXCacheable(
        cacheName = "userProfiles",
        key = "#id + '-' + #includeDetails",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public ResponseEntity<UserProfile> getUserProfile(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        UserProfile profile = userService.getUserProfile(id, includeDetails);

        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
            .body(profile);
    }
}

@Service
public class ExternalApiService {

    private final RestTemplate restTemplate;

    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @JCacheXCacheable(
        cacheName = "externalApiResponses",
        key = "#endpoint + '-' + #params.toString()",
        expireAfterWrite = 15,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public ApiResponse callExternalApi(String endpoint, Map<String, String> params) {
        String url = buildUrl(endpoint, params);

        try {
            return restTemplate.getForObject(url, ApiResponse.class);
        } catch (Exception e) {
            throw new ExternalApiException("Failed to call external API: " + endpoint, e);
        }
    }

    @JCacheXCacheable(
        cacheName = "weatherData",
        key = "#city + '-' + #country",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public WeatherData getWeatherData(String city, String country) {
        String endpoint = "https://api.weather.com/v1/current";
        Map<String, String> params = Map.of(
            "city", city,
            "country", country,
            "apikey", "your-api-key"
        );

        ApiResponse response = callExternalApi(endpoint, params);
        return parseWeatherData(response);
    }

    private String buildUrl(String endpoint, Map<String, String> params) {
        StringBuilder url = new StringBuilder(endpoint);
        if (!params.isEmpty()) {
            url.append("?");
            url.append(params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&")));
        }
        return url.toString();
    }

    private WeatherData parseWeatherData(ApiResponse response) {
        // Parse weather data from API response
        return new WeatherData(
            response.getTemperature(),
            response.getHumidity(),
            response.getDescription()
        );
    }
}`
                        }
                    ]} />
                </Box>

                {/* Heavy Computation Process */}
                <Box id="heavy-computation-process" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <ComputerIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Heavy Computation Process
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Caching results of expensive computations and long-running processes.
                    </Typography>

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Heavy Computation Caching:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Mathematical computations" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Data processing results" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Report generation" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Machine learning predictions" />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'heavy-computation-cache',
                            label: 'Heavy Computation Cache',
                            language: 'java',
                            code: `import io.github.dhruv1110.jcachex.spring.annotation.JCacheXCacheable;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class ComputationService {

    @JCacheXCacheable(
        cacheName = "fibonacciResults",
        key = "#n",
        expireAfterWrite = 60,
        expireAfterWriteUnit = TimeUnit.MINUTES,
        maximumSize = 1000
    )
    public BigInteger fibonacci(int n) {
        if (n <= 1) {
            return BigInteger.valueOf(n);
        }

        BigInteger prev = BigInteger.ZERO;
        BigInteger curr = BigInteger.ONE;

        for (int i = 2; i <= n; i++) {
            BigInteger next = prev.add(curr);
            prev = curr;
            curr = next;
        }

        return curr;
    }

    @JCacheXCacheable(
        cacheName = "primeFactors",
        key = "#number",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public List<Long> getPrimeFactors(long number) {
        List<Long> factors = new ArrayList<>();

        for (long i = 2; i * i <= number; i++) {
            while (number % i == 0) {
                factors.add(i);
                number /= i;
            }
        }

        if (number > 1) {
            factors.add(number);
        }

        return factors;
    }

    @JCacheXCacheable(
        cacheName = "reportData",
        key = "#startDate + '-' + #endDate + '-' + #reportType",
        expireAfterWrite = 2,
        expireAfterWriteUnit = TimeUnit.HOURS
    )
    public ReportData generateReport(LocalDate startDate, LocalDate endDate, String reportType) {
        // Simulate heavy report generation
        return switch (reportType) {
            case "SALES" -> generateSalesReport(startDate, endDate);
            case "ANALYTICS" -> generateAnalyticsReport(startDate, endDate);
            case "PERFORMANCE" -> generatePerformanceReport(startDate, endDate);
            default -> throw new IllegalArgumentException("Unknown report type: " + reportType);
        };
    }

    @Async
    @JCacheXCacheable(
        cacheName = "asyncComputations",
        key = "#taskId",
        expireAfterWrite = 45,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public CompletableFuture<ComputationResult> performAsyncComputation(String taskId, ComputationParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate heavy computation
                Thread.sleep(5000);

                double result = switch (params.getType()) {
                    case "MONTE_CARLO" -> performMonteCarloSimulation(params);
                    case "MATRIX_OPERATIONS" -> performMatrixOperations(params);
                    case "DATA_ANALYSIS" -> performDataAnalysis(params);
                    default -> 0.0;
                };

                return new ComputationResult(taskId, result, LocalDateTime.now());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Computation interrupted", e);
            }
        });
    }

    @JCacheXCacheable(
        cacheName = "machineLearningPredictions",
        key = "#modelId + '-' + #inputData.hashCode()",
        expireAfterWrite = 15,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public PredictionResult predict(String modelId, InputData inputData) {
        // Simulate ML model prediction
        try {
            Thread.sleep(2000); // Simulate model inference time

            double prediction = performPrediction(modelId, inputData);
            double confidence = calculateConfidence(prediction, inputData);

            return new PredictionResult(
                modelId,
                prediction,
                confidence,
                LocalDateTime.now()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Prediction interrupted", e);
        }
    }

    private ReportData generateSalesReport(LocalDate startDate, LocalDate endDate) {
        // Simulate heavy sales report generation
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return new ReportData(
            "SALES",
            Map.of(
                "totalSales", BigDecimal.valueOf(1000000),
                "totalOrders", BigDecimal.valueOf(5000),
                "averageOrderValue", BigDecimal.valueOf(200)
            ),
            startDate,
            endDate
        );
    }

    private ReportData generateAnalyticsReport(LocalDate startDate, LocalDate endDate) {
        // Simulate heavy analytics report generation
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return new ReportData(
            "ANALYTICS",
            Map.of(
                "pageViews", BigDecimal.valueOf(500000),
                "uniqueVisitors", BigDecimal.valueOf(25000),
                "bounceRate", BigDecimal.valueOf(0.35)
            ),
            startDate,
            endDate
        );
    }

    private ReportData generatePerformanceReport(LocalDate startDate, LocalDate endDate) {
        // Simulate heavy performance report generation
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return new ReportData(
            "PERFORMANCE",
            Map.of(
                "averageResponseTime", BigDecimal.valueOf(150),
                "errorRate", BigDecimal.valueOf(0.02),
                "throughput", BigDecimal.valueOf(1000)
            ),
            startDate,
            endDate
        );
    }

    private double performMonteCarloSimulation(ComputationParameters params) {
        // Simulate Monte Carlo simulation
        Random random = new Random();
        int iterations = params.getIterations();
        int insideCircle = 0;

        for (int i = 0; i < iterations; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();

            if (x * x + y * y <= 1) {
                insideCircle++;
            }
        }

        return 4.0 * insideCircle / iterations; // Approximation of π
    }

    private double performMatrixOperations(ComputationParameters params) {
        // Simulate matrix operations
        int size = params.getMatrixSize();
        double[][] matrix = new double[size][size];

        // Initialize matrix with random values
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextDouble();
            }
        }

        // Perform matrix operations (determinant calculation)
        return calculateDeterminant(matrix);
    }

    private double performDataAnalysis(ComputationParameters params) {
        // Simulate data analysis
        List<Double> data = generateRandomData(params.getDataSize());

        // Calculate statistics
        double mean = data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = data.stream()
            .mapToDouble(x -> Math.pow(x - mean, 2))
            .average()
            .orElse(0.0);

        return Math.sqrt(variance); // Standard deviation
    }

    private double performPrediction(String modelId, InputData inputData) {
        // Simulate ML prediction
        return inputData.getFeatures().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0) * 1.5; // Simple prediction logic
    }

    private double calculateConfidence(double prediction, InputData inputData) {
        // Simulate confidence calculation
        return Math.min(0.95, 0.7 + Math.random() * 0.25);
    }

    private double calculateDeterminant(double[][] matrix) {
        // Simple determinant calculation for demonstration
        int n = matrix.length;
        if (n == 1) return matrix[0][0];
        if (n == 2) return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

        double det = 0;
        for (int i = 0; i < n; i++) {
            det += Math.pow(-1, i) * matrix[0][i] * calculateDeterminant(getSubMatrix(matrix, 0, i));
        }
        return det;
    }

    private double[][] getSubMatrix(double[][] matrix, int excludeRow, int excludeCol) {
        int n = matrix.length;
        double[][] subMatrix = new double[n - 1][n - 1];
        int row = 0;

        for (int i = 0; i < n; i++) {
            if (i == excludeRow) continue;
            int col = 0;
            for (int j = 0; j < n; j++) {
                if (j == excludeCol) continue;
                subMatrix[row][col] = matrix[i][j];
                col++;
            }
            row++;
        }
        return subMatrix;
    }

    private List<Double> generateRandomData(int size) {
        Random random = new Random();
        return IntStream.range(0, size)
            .mapToDouble(i -> random.nextGaussian())
            .boxed()
            .collect(Collectors.toList());
    }
}`
                        }
                    ]} />
                </Box>

                {/* Footer */}
                <Box sx={{ textAlign: 'center', mt: 8, pt: 4, borderTop: 1, borderColor: 'divider' }}>
                    <Typography variant="body2" color="text.secondary">
                        JCacheX Examples - Production-ready caching patterns for Java, Kotlin, and Spring Boot
                    </Typography>
                    <Box sx={{ mt: 2 }}>
                        <Button
                            startIcon={<GitHubIcon />}
                            href="https://github.com/dhruv1110/JCacheX"
                            target="_blank"
                            variant="outlined"
                            sx={{ mr: 2 }}
                        >
                            GitHub
                        </Button>
                        <Button
                            startIcon={<LinkIcon />}
                            href="/docs"
                            variant="outlined"
                        >
                            Documentation
                        </Button>
                    </Box>
                </Box>
            </Container>
        </Layout>
    );
};

export default Examples;
