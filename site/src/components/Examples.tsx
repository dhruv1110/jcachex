import React, { useState } from 'react';
import { Section, Grid, FeatureCard, Badge } from './common';
import { MetaTags } from './SEO';
import CodeTabs from './CodeTabs';
import type { CodeTab } from '../types';

// Define examples with better organization and explanations
const BASIC_EXAMPLES: CodeTab[] = [
    {
        id: 'simple-cache',
        label: 'Basic Cache Implementation',
        language: 'java',
        code: `// Basic cache configuration and usage
import io.github.dhruv1110.jcachex.*;
import java.time.Duration;

public class BasicCacheExample {
    public static void main(String[] args) {
        // Configure cache with capacity and expiration
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
            .maximumSize(100L)  // Maximum 100 cached items
            .expireAfterWrite(Duration.ofMinutes(10))  // Items expire after 10 minutes
            .recordStats(true)  // Enable performance metrics
            .build();

        // Create cache instance
        Cache<String, String> cache = new DefaultCache<>(config);

        // Basic cache operations
        cache.put("user:123", "Alice Johnson");
        String cachedValue = cache.get("user:123");

        System.out.println("Retrieved: " + cachedValue);
        System.out.println("Cache size: " + cache.size());

        // Check cache statistics
        CacheStats stats = cache.stats();
        System.out.println("Hit rate: " + stats.hitRate());
    }
}`
    },
    {
        id: 'object-caching',
        label: 'Object Serialization',
        language: 'java',
        code: `// Caching complex objects with proper serialization
public class ObjectCacheExample {

    // Domain object for caching
    public static class UserProfile {
        private final String userId;
        private final String name;
        private final String email;
        private final Set<String> roles;

        public UserProfile(String userId, String name, String email, Set<String> roles) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.roles = Collections.unmodifiableSet(roles);
        }

        // Getters and proper equals/hashCode implementation
        public String getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public Set<String> getRoles() { return roles; }
    }

    public static void main(String[] args) {
        // Configure cache for user profiles
        CacheConfig<String, UserProfile> config = CacheConfig.<String, UserProfile>builder()
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofHours(1))
            .evictionStrategy(EvictionStrategy.LRU)
            .recordStats(true)
            .build();

        Cache<String, UserProfile> userCache = new DefaultCache<>(config);

        // Cache user profiles
        Set<String> adminRoles = Set.of("ADMIN", "USER");
        UserProfile admin = new UserProfile("123", "Alice Johnson", "alice@company.com", adminRoles);

        userCache.put(admin.getUserId(), admin);

        // Retrieve and validate
        UserProfile cached = userCache.get("123");
        if (cached != null) {
            System.out.println("User: " + cached.getName() + " (" + cached.getEmail() + ")");
            System.out.println("Roles: " + cached.getRoles());
        }
    }
}`
    },
    {
        id: 'kotlin-implementation',
        label: 'Kotlin Integration',
        language: 'kotlin',
        code: `// JCacheX with Kotlin idiomatic patterns
import io.github.dhruv1110.jcachex.kotlin.*
import kotlin.time.Duration.Companion.minutes

data class Product(
    val id: String,
    val name: String,
    val price: BigDecimal,
    val category: String
)

class ProductCacheService {

    // Cache configuration using Kotlin DSL
    private val productCache = cache<String, Product> {
        maxSize = 500
        expireAfterWrite = 30.minutes
        evictionStrategy = EvictionStrategy.LRU
        recordStats = true
    }

    // Cache operations with Kotlin idioms
    suspend fun getProduct(productId: String): Product? {
        return productCache[productId] ?: loadFromDatabase(productId)?.also { product ->
            productCache[productId] = product
        }
    }

    suspend fun updateProduct(product: Product) {
        saveToDatabase(product)
        productCache[product.id] = product // Update cache
    }

    fun evictProduct(productId: String) {
        productCache.invalidate(productId)
    }

    // Simulated database operations
    private suspend fun loadFromDatabase(productId: String): Product? {
        // Database loading logic
        return Product(productId, "Sample Product", BigDecimal("99.99"), "Electronics")
    }

    private suspend fun saveToDatabase(product: Product) {
        // Database persistence logic
    }

    // Cache performance monitoring
    fun getCacheMetrics(): String {
        val stats = productCache.stats()
        val hitRate = (stats.hitRate() * 100).toString().take(5)
        return "Hit Rate: $hitRate% | " +
               "Size: \${productCache.size()} | " +
               "Evictions: \${stats.evictionCount()}"
    }
}`
    }
];

const DATABASE_EXAMPLES: CodeTab[] = [
    {
        id: 'repository-pattern',
        label: 'Repository Pattern Integration',
        language: 'java',
        code: `// Cache-aside pattern with repository layer
@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;
    private final Cache<String, Product> productCache;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // Configure cache for database entities
        CacheConfig<String, Product> config = CacheConfig.<String, Product>builder()
            .maximumSize(10000L)
            .expireAfterWrite(Duration.ofHours(2))
            .evictionStrategy(EvictionStrategy.LRU)
            .recordStats(true)
            .eventListener(new LoggingCacheEventListener<>())
            .build();

        this.productCache = new DefaultCache<>(config);
    }

    public Optional<Product> findById(String productId) {
        // Check cache first
        Product cached = productCache.get(productId);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Load from database
        try {
            Product product = jdbcTemplate.queryForObject(
                "SELECT id, name, price, category FROM products WHERE id = ?",
                new Object[]{productId},
                (rs, rowNum) -> new Product(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getBigDecimal("price"),
                    rs.getString("category")
                )
            );

            // Cache the result
            if (product != null) {
                productCache.put(productId, product);
            }

            return Optional.ofNullable(product);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Product save(Product product) {
        // Save to database
        jdbcTemplate.update(
            "INSERT INTO products (id, name, price, category) VALUES (?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE name = ?, price = ?, category = ?",
            product.getId(), product.getName(), product.getPrice(), product.getCategory(),
            product.getName(), product.getPrice(), product.getCategory()
        );

        // Update cache
        productCache.put(product.getId(), product);

        return product;
    }

    public void deleteById(String productId) {
        jdbcTemplate.update("DELETE FROM products WHERE id = ?", productId);
        productCache.invalidate(productId);
    }

    // Bulk operations with cache optimization
    public List<Product> findByCategory(String category) {
        return jdbcTemplate.query(
            "SELECT id, name, price, category FROM products WHERE category = ?",
            new Object[]{category},
            (rs, rowNum) -> {
                Product product = new Product(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getBigDecimal("price"),
                    rs.getString("category")
                );

                // Cache individual products
                productCache.put(product.getId(), product);
                return product;
            }
        );
    }
}`
    },
    {
        id: 'spring-annotations',
        label: 'Spring Boot Annotations',
        language: 'java',
        code: `// Declarative caching with Spring Boot integration
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Cache method results with automatic key generation
    @JCacheXCacheable(
        cacheName = "users",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES,
        maximumSize = 1000
    )
    public User findUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    // Custom cache key with SpEL expression
    @JCacheXCacheable(
        cacheName = "usersByEmail",
        key = "#email.toLowerCase()",
        expireAfterWrite = 15,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    // Conditional caching based on method parameters
    @JCacheXCacheable(
        cacheName = "activeUsers",
        condition = "#includeInactive == false",
        expireAfterWrite = 10,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public List<User> findUsers(boolean includeInactive) {
        return includeInactive ?
            userRepository.findAll() :
            userRepository.findByActiveTrue();
    }

    // Cache eviction on data modification
    @JCacheXCacheEvict(cacheName = "users")
    @JCacheXCacheEvict(cacheName = "usersByEmail", key = "#user.email.toLowerCase()")
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // Multiple cache eviction
    @JCacheXCacheEvict(cacheName = "users")
    @JCacheXCacheEvict(cacheName = "usersByEmail")
    @JCacheXCacheEvict(cacheName = "activeUsers")
    @Transactional
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    // Cache warming strategy
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCaches() {
        // Pre-load frequently accessed data
        List<User> activeUsers = userRepository.findTop100ByActiveOrderByLastLoginDesc(true);
        // These calls will populate the cache
        activeUsers.forEach(user -> findUserById(user.getId()));
    }
}`
    }
];

const API_EXAMPLES: CodeTab[] = [
    {
        id: 'external-api-caching',
        label: 'External API Integration',
        language: 'java',
        code: `// Caching external API responses with fallback strategies
@Service
public class ExternalApiService {

    private final RestTemplate restTemplate;
    private final Cache<String, ApiResponse> responseCache;
    private final Cache<String, ApiResponse> staleCache;

    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        // Primary cache with shorter TTL
        CacheConfig<String, ApiResponse> primaryConfig = CacheConfig.<String, ApiResponse>builder()
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofMinutes(15))
            .recordStats(true)
            .build();
        this.responseCache = new DefaultCache<>(primaryConfig);

        // Stale cache with longer TTL for fallback
        CacheConfig<String, ApiResponse> staleConfig = CacheConfig.<String, ApiResponse>builder()
            .maximumSize(500L)
            .expireAfterWrite(Duration.ofHours(24))
            .build();
        this.staleCache = new DefaultCache<>(staleConfig);
    }

    public ApiResponse fetchData(String endpoint, Map<String, String> parameters) {
        String cacheKey = buildCacheKey(endpoint, parameters);

        // Try primary cache first
        ApiResponse cached = responseCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            // Make external API call
            String url = buildUrl(endpoint, parameters);
            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);

            if (response != null && response.isSuccessful()) {
                // Cache successful responses
                responseCache.put(cacheKey, response);
                staleCache.put(cacheKey, response); // Also store in stale cache
                return response;
            }

        } catch (RestClientException e) {
            // API call failed - try stale cache as fallback
            ApiResponse stale = staleCache.get(cacheKey);
            if (stale != null) {
                // Return stale data with warning
                return stale.withStaleWarning();
            }

            throw new ExternalApiException("API call failed and no cached data available", e);
        }

        throw new ExternalApiException("API returned unsuccessful response");
    }

    // Bulk API calls with intelligent caching
    public List<ApiResponse> fetchMultiple(List<String> endpoints) {
        Map<String, ApiResponse> results = new HashMap<>();
        List<String> uncachedEndpoints = new ArrayList<>();

        // Check cache for all endpoints
        for (String endpoint : endpoints) {
            String cacheKey = buildCacheKey(endpoint, Collections.emptyMap());
            ApiResponse cached = responseCache.get(cacheKey);

            if (cached != null) {
                results.put(endpoint, cached);
            } else {
                uncachedEndpoints.add(endpoint);
            }
        }

        // Batch fetch uncached endpoints
        if (!uncachedEndpoints.isEmpty()) {
            try {
                List<ApiResponse> responses = batchApiCall(uncachedEndpoints);
                for (int i = 0; i < uncachedEndpoints.size(); i++) {
                    String endpoint = uncachedEndpoints.get(i);
                    ApiResponse response = responses.get(i);

                    if (response.isSuccessful()) {
                        String cacheKey = buildCacheKey(endpoint, Collections.emptyMap());
                        responseCache.put(cacheKey, response);
                        staleCache.put(cacheKey, response);
                    }

                    results.put(endpoint, response);
                }
            } catch (RestClientException e) {
                // Handle batch failure
                throw new ExternalApiException("Batch API call failed", e);
            }
        }

        return endpoints.stream()
            .map(results::get)
            .collect(Collectors.toList());
    }

    private String buildCacheKey(String endpoint, Map<String, String> parameters) {
        return endpoint + ":" + parameters.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }

    private String buildUrl(String endpoint, Map<String, String> parameters) {
        // URL building logic
        return endpoint + "?" + parameters.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
    }

    private List<ApiResponse> batchApiCall(List<String> endpoints) {
        // Batch API call implementation
        return endpoints.stream()
            .map(endpoint -> restTemplate.getForObject(endpoint, ApiResponse.class))
            .collect(Collectors.toList());
    }
}`
    }
];

const MONITORING_EXAMPLES: CodeTab[] = [
    {
        id: 'production-monitoring',
        label: 'Production Monitoring',
        language: 'java',
        code: `// Comprehensive cache monitoring and alerting
@Component
public class CacheMonitoringService {

    private final MeterRegistry meterRegistry;
    private final List<Cache<?, ?>> monitoredCaches;
    private final ScheduledExecutorService scheduler;

    public CacheMonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.monitoredCaches = new CopyOnWriteArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(2);

        // Schedule periodic health checks
        startHealthCheckMonitoring();
        startPerformanceMetricsCollection();
    }

    public void registerCache(String name, Cache<?, ?> cache) {
        monitoredCaches.add(cache);

        // Create Micrometer gauges for cache metrics
        Gauge.builder("cache.size")
            .tag("cache", name)
            .register(meterRegistry, cache, Cache::size);

        Gauge.builder("cache.hit_rate")
            .tag("cache", name)
            .register(meterRegistry, cache, c -> c.stats().hitRate());

        Gauge.builder("cache.miss_rate")
            .tag("cache", name)
            .register(meterRegistry, cache, c -> c.stats().missRate());

        Gauge.builder("cache.eviction_count")
            .tag("cache", name)
            .register(meterRegistry, cache, c -> c.stats().evictionCount());
    }

    private void startHealthCheckMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            for (Cache<?, ?> cache : monitoredCaches) {
                CacheStats stats = cache.stats();
                CacheHealthStatus health = evaluateCacheHealth(stats);

                // Publish health metrics
                meterRegistry.gauge("cache.health_score",
                    Tags.of("cache", getCacheName(cache)),
                    health.getScore());

                // Trigger alerts for unhealthy caches
                if (health.getScore() < 0.7) {
                    sendAlert(getCacheName(cache), health);
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void startPerformanceMetricsCollection() {
        scheduler.scheduleAtFixedRate(() -> {
            for (Cache<?, ?> cache : monitoredCaches) {
                String cacheName = getCacheName(cache);
                CacheStats stats = cache.stats();

                // Log detailed performance metrics
                MDC.put("cache.name", cacheName);
                MDC.put("cache.size", String.valueOf(cache.size()));
                MDC.put("cache.hit_rate", String.valueOf(stats.hitRate()));
                MDC.put("cache.average_load_time", String.valueOf(stats.averageLoadTime()));

                if (stats.hitRate() < 0.5) {
                    // Log warning for poor hit rates
                    log.warn("Cache {} has low hit rate: {:.2f}%",
                        cacheName, stats.hitRate() * 100);
                }

                MDC.clear();
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    public CacheHealthReport generateHealthReport() {
        Map<String, CacheMetrics> cacheMetrics = new HashMap<>();

        for (Cache<?, ?> cache : monitoredCaches) {
            String name = getCacheName(cache);
            CacheStats stats = cache.stats();

            CacheMetrics metrics = CacheMetrics.builder()
                .name(name)
                .size(cache.size())
                .hitRate(stats.hitRate())
                .missRate(stats.missRate())
                .evictionCount(stats.evictionCount())
                .averageLoadTime(stats.averageLoadTime())
                .requestCount(stats.requestCount())
                .healthScore(evaluateCacheHealth(stats).getScore())
                .build();

            cacheMetrics.put(name, metrics);
        }

        return new CacheHealthReport(cacheMetrics, Instant.now());
    }

    private CacheHealthStatus evaluateCacheHealth(CacheStats stats) {
        double score = 1.0;
        List<String> issues = new ArrayList<>();

        // Hit rate assessment
        if (stats.hitRate() < 0.5) {
            score -= 0.3;
            issues.add("Low hit rate: " + Math.round(stats.hitRate() * 100) + "%");
        } else if (stats.hitRate() < 0.7) {
            score -= 0.1;
        }

        // Load time assessment
        if (stats.averageLoadTime() > 100) {
            score -= 0.2;
            issues.add("High average load time: " + stats.averageLoadTime() + "ms");
        }

        // Eviction rate assessment
        long totalRequests = stats.requestCount();
        if (totalRequests > 0 && (stats.evictionCount() / (double) totalRequests) > 0.1) {
            score -= 0.2;
            issues.add("High eviction rate: " +
                Math.round((stats.evictionCount() / (double) totalRequests) * 100) + "%");
        }

        return new CacheHealthStatus(Math.max(0, score), issues);
    }

    private void sendAlert(String cacheName, CacheHealthStatus health) {
        // Integration with alerting system (PagerDuty, Slack, etc.)
        log.error("Cache health alert for {}: score={:.2f}, issues={}",
            cacheName, health.getScore(), health.getIssues());
    }

    private String getCacheName(Cache<?, ?> cache) {
        // Extract cache name from cache instance
        return cache.getClass().getSimpleName() + "@" +
            Integer.toHexString(cache.hashCode());
    }

    @PreDestroy
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
];

const ExamplesPage: React.FC = () => {
    const [activeSection, setActiveSection] = useState<string>('basic');

    // Default SEO data for examples page
    const seoData = {
        title: 'JCacheX Examples and Code Samples',
        description: 'Comprehensive examples and code samples for JCacheX. Learn async operations, Spring Boot integration, distributed caching, and advanced patterns.',
        keywords: ['JCacheX examples', 'cache examples', 'Java cache tutorial', 'Spring cache examples', 'async cache'],
        canonical: 'https://dhruv1110.github.io/jcachex/examples'
    };

    const sections = [
        { id: 'basic', label: 'Core Implementation', description: 'Fundamental caching patterns' },
        { id: 'database', label: 'Database Integration', description: 'Repository and ORM patterns' },
        { id: 'api', label: 'API Caching', description: 'External service integration' },
        { id: 'monitoring', label: 'Production Monitoring', description: 'Observability and alerting' }
    ];

    const getSectionExamples = (sectionId: string) => {
        switch (sectionId) {
            case 'basic': return BASIC_EXAMPLES;
            case 'database': return DATABASE_EXAMPLES;
            case 'api': return API_EXAMPLES;
            case 'monitoring': return MONITORING_EXAMPLES;
            default: return BASIC_EXAMPLES;
        }
    };

    const getSectionInfo = (sectionId: string) => {
        switch (sectionId) {
            case 'basic':
                return {
                    title: 'Core Implementation Patterns',
                    description: 'Essential caching patterns and configuration strategies for production applications.',
                    keyPoints: [
                        'Cache configuration and lifecycle management',
                        'Object serialization and type safety',
                        'Performance monitoring and statistics',
                        'Memory management and eviction policies'
                    ],
                    useCases: 'Session management, user preferences, configuration data, frequently accessed entities'
                };
            case 'database':
                return {
                    title: 'Database Integration Strategies',
                    description: 'Production-ready patterns for caching database queries and managing data consistency.',
                    keyPoints: [
                        'Cache-aside pattern implementation',
                        'Repository layer integration',
                        'Declarative caching with annotations',
                        'Bulk operations and cache warming'
                    ],
                    useCases: 'User profiles, product catalogs, reference data, complex query results'
                };
            case 'api':
                return {
                    title: 'External API Integration',
                    description: 'Strategies for caching external service responses with fault tolerance and fallback mechanisms.',
                    keyPoints: [
                        'Multi-tier cache architecture',
                        'Fallback and stale data strategies',
                        'Batch API call optimization',
                        'Circuit breaker pattern integration'
                    ],
                    useCases: 'Weather data, payment processing, geolocation services, third-party content'
                };
            case 'monitoring':
                return {
                    title: 'Production Monitoring',
                    description: 'Comprehensive monitoring, alerting, and performance optimization for production environments.',
                    keyPoints: [
                        'Real-time performance metrics',
                        'Health checks and alerting',
                        'Integration with monitoring systems',
                        'Automated performance optimization'
                    ],
                    useCases: 'Production deployment, capacity planning, performance tuning, incident response'
                };
            default:
                return {
                    title: 'Core Implementation Patterns',
                    description: 'Essential caching patterns and configuration strategies for production applications.',
                    keyPoints: [
                        'Cache configuration and lifecycle management',
                        'Object serialization and type safety',
                        'Performance monitoring and statistics',
                        'Memory management and eviction policies'
                    ],
                    useCases: 'Session management, user preferences, configuration data, frequently accessed entities'
                };
        }
    };

    const currentSection = getSectionInfo(activeSection);

    return (
        <div className="examples-page">
            <MetaTags seo={seoData} />

            {/* Header */}
            <Section background="gradient" padding="lg" centered>
                <div className="examples-header">
                    <h1 className="examples-title">Implementation Examples</h1>
                    <p className="examples-subtitle">
                        Production-ready code examples and integration patterns for enterprise applications.
                        Comprehensive implementations covering core functionality through advanced monitoring.
                    </p>
                </div>
            </Section>

            {/* Examples Navigation */}
            <Section background="dark" padding="lg">
                <nav className="examples-nav">
                    <ul className="examples-nav-list">
                        {sections.map((section) => (
                            <li key={section.id} className="examples-nav-item">
                                <button
                                    className={`examples-nav-link ${activeSection === section.id ? 'active' : ''}`}
                                    onClick={() => setActiveSection(section.id)}
                                >
                                    <div className="nav-content">
                                        <span className="nav-label">{section.label}</span>
                                        <span className="nav-description">{section.description}</span>
                                    </div>
                                </button>
                            </li>
                        ))}
                    </ul>
                </nav>
            </Section>

            {/* Current Section Content */}
            <Section background="primary" padding="lg">
                <div className="examples-section">
                    <div className="examples-section-header">
                        <h2>{currentSection.title}</h2>
                        <p>{currentSection.description}</p>
                    </div>

                    <Grid>
                        <div className="example-category">
                            <div className="category-header">
                                <h3>Key Implementation Points</h3>
                                <ul>
                                    {currentSection.keyPoints.map((point, index) => (
                                        <li key={index}>{point}</li>
                                    ))}
                                </ul>

                                <h4>Common Use Cases</h4>
                                <p>{currentSection.useCases}</p>
                            </div>

                            <CodeTabs tabs={getSectionExamples(activeSection)} />
                        </div>
                    </Grid>
                </div>
            </Section>

            {/* Next Steps */}
            <Section background="dark" padding="lg">
                <div className="next-steps-cta">
                    <h3>Ready to implement?</h3>
                    <p>
                        Continue your implementation with comprehensive documentation and framework-specific guides.
                    </p>
                    <div className="cta-buttons">
                        <a href="/getting-started" className="btn btn-primary">
                            View Documentation
                        </a>
                        <a href="/spring" className="btn btn-secondary">
                            Spring Guide
                        </a>
                        <a href="/faq" className="btn btn-outline">
                            FAQ
                        </a>
                    </div>
                </div>
            </Section>
        </div>
    );
};

export default ExamplesPage;
