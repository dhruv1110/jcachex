import io.github.dhruv1110.jcachex.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Microservices API Gateway with Intelligent Caching
 * Demonstrates how to implement high-performance API response caching
 * with circuit breaker patterns and intelligent TTL strategies
 */
public class MicroservicesApiGateway {
    private static final Logger log = Logger.getLogger(MicroservicesApiGateway.class.getName());

    // Multi-layer caching strategy for different data types
    private final Cache<String, ApiResponse> responseCache;
    private final Cache<String, UserProfile> userProfileCache;
    private final Cache<String, ServiceHealth> healthCache;

    // Simulated downstream services
    private final UserService userService = new UserService();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    public MicroservicesApiGateway() {
        // API response cache with short TTL for dynamic content
        this.responseCache = JCacheXBuilder.forApiResponseCaching()
                .name("api-responses")
                .maximumSize(50000L)
                .expireAfterWrite(Duration.ofMinutes(5)) // Short TTL for API responses
                .expireAfterAccess(Duration.ofMinutes(10)) // Keep frequently accessed longer
                .recordStats(true)
                .evictionListener(this::onResponseEviction)
                .build();

        // User profile cache with longer TTL (user data changes less frequently)
        this.userProfileCache = JCacheXBuilder.forReadHeavyWorkload()
                .name("user-profiles")
                .maximumSize(100000L)
                .expireAfterWrite(Duration.ofHours(1))
                .expireAfterAccess(Duration.ofHours(2))
                .recordStats(true)
                .build();

        // Service health cache for monitoring
        this.healthCache = JCacheXBuilder.forWriteHeavyWorkload()
                .name("service-health")
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofSeconds(30))
                .recordStats(true)
                .build();
    }

    /**
     * Gateway endpoint: GET /api/users/{id}
     * Demonstrates intelligent caching with fallback strategies
     */
    public ApiResponse getUser(String userId) {
        String cacheKey = "user:" + userId;

        return responseCache.get(cacheKey, () -> {
            try {
                // Check service health first
                if (!isServiceHealthy("user-service")) {
                    return getStaleUserResponse(userId);
                }

                // Call downstream service
                UserProfile user = userService.fetchUser(userId);

                // Cache the user profile separately for other endpoints
                userProfileCache.put(userId, user);

                // Return API response with metadata
                return new ApiResponse(
                        user,
                        Instant.now(),
                        "user-service",
                        CacheStatus.MISS);

            } catch (ServiceUnavailableException e) {
                log.warning("User service unavailable for user " + userId);
                return getStaleUserResponse(userId);

            } catch (TimeoutException e) {
                log.error("Timeout fetching user " + userId, e);
                throw new CacheLoadException("User service timeout", e);
            }
        });
    }

    /**
     * Gateway endpoint: GET /api/products/{id}
     * Demonstrates circuit breaker pattern with caching
     */
    public ApiResponse getProduct(String productId) {
        String cacheKey = "product:" + productId;

        return responseCache.get(cacheKey, () -> {
            // Circuit breaker check
            if (!isServiceHealthy("product-service")) {
                throw new ServiceUnavailableException("Product service circuit breaker open");
            }

            try {
                Product product = productService.fetchProduct(productId);

                return new ApiResponse(
                        product,
                        Instant.now(),
                        "product-service",
                        CacheStatus.MISS);

            } catch (Exception e) {
                // Update service health
                updateServiceHealth("product-service", false);
                throw e;
            }
        });
    }

    /**
     * Gateway endpoint: GET /api/orders/{id}
     * Demonstrates conditional caching based on data sensitivity
     */
    public ApiResponse getOrder(String orderId, String userId) {
        // Orders are user-specific and sensitive - include user in cache key
        String cacheKey = "order:" + orderId + ":" + userId;

        return responseCache.get(cacheKey, () -> {
            // Verify user has access to this order
            if (!orderService.canUserAccessOrder(userId, orderId)) {
                throw new UnauthorizedException("User cannot access order");
            }

            Order order = orderService.fetchOrder(orderId);

            return new ApiResponse(
                    order,
                    Instant.now(),
                    "order-service",
                    CacheStatus.MISS);
        });
    }

    /**
     * Asynchronous cache warming for popular endpoints
     */
    public CompletableFuture<Void> warmCache(String[] popularUserIds) {
        return CompletableFuture.runAsync(() -> {
            log.info("Starting cache warming for {} users", popularUserIds.length);

            for (String userId : popularUserIds) {
                try {
                    // Pre-load user data
                    getUser(userId);

                    // Small delay to avoid overwhelming downstream services
                    Thread.sleep(10);

                } catch (Exception e) {
                    log.warning("Failed to warm cache for user " + userId + ": " + e.getMessage());
                }
            }

            log.info("Cache warming completed");
        });
    }

    /**
     * Circuit breaker health check
     */
    private boolean isServiceHealthy(String serviceName) {
        ServiceHealth health = healthCache.getIfPresent(serviceName);
        if (health == null) {
            // Assume healthy if no recent data
            updateServiceHealth(serviceName, true);
            return true;
        }
        return health.isHealthy();
    }

    /**
     * Update service health status
     */
    private void updateServiceHealth(String serviceName, boolean isHealthy) {
        ServiceHealth health = new ServiceHealth(serviceName, isHealthy, Instant.now());
        healthCache.put(serviceName, health);

        if (!isHealthy) {
            log.warning("Service {} marked as unhealthy", serviceName);
        }
    }

    /**
     * Fallback to stale data when service is unavailable
     */
    private ApiResponse getStaleUserResponse(String userId) {
        UserProfile staleUser = userProfileCache.getIfPresent(userId);
        if (staleUser != null) {
            log.info("Returning stale user data for {}", userId);
            return new ApiResponse(
                    staleUser,
                    Instant.now(),
                    "user-service",
                    CacheStatus.STALE);
        }

        // Return default response as last resort
        return new ApiResponse(
                new UserProfile(userId, "Unknown User", "unknown@example.com"),
                Instant.now(),
                "user-service",
                CacheStatus.DEFAULT);
    }

    /**
     * Cache eviction listener for monitoring
     */
    private void onResponseEviction(String key, ApiResponse response, RemovalCause cause) {
        log.info("API response evicted: {} (from {}) due to {}",
                key, response.getServiceName(), cause);

        // In production, send metrics to monitoring system
        // Examples: Prometheus, Grafana, CloudWatch
    }

    /**
     * Get comprehensive gateway statistics
     */
    public void printGatewayStats() {
        System.out.println("🌐 API Gateway Cache Statistics");
        System.out.println("================================");

        // Response cache stats
        CacheStats responseStats = responseCache.stats();
        System.out.printf("Response Cache - Size: %d, Hit Rate: %.2f%%, Avg Load: %.2fms%n",
                responseCache.size(),
                responseStats.hitRate() * 100,
                responseStats.averageLoadPenalty() / 1_000_000.0);

        // User profile cache stats
        CacheStats userStats = userProfileCache.stats();
        System.out.printf("User Profile Cache - Size: %d, Hit Rate: %.2f%%%n",
                userProfileCache.size(), userStats.hitRate() * 100);

        // Health cache stats
        CacheStats healthStats = healthCache.stats();
        System.out.printf("Health Cache - Size: %d, Hit Rate: %.2f%%%n",
                healthCache.size(), healthStats.hitRate() * 100);

        // Service health status
        System.out.println("\n🏥 Service Health Status:");
        String[] services = { "user-service", "product-service", "order-service" };
        for (String service : services) {
            ServiceHealth health = healthCache.getIfPresent(service);
            String status = health != null ? (health.isHealthy() ? "✅ Healthy" : "❌ Unhealthy") : "❓ Unknown";
            System.out.printf("  %s: %s%n", service, status);
        }
    }

    // Demo method
    public static void main(String[] args) throws InterruptedException {
        MicroservicesApiGateway gateway = new MicroservicesApiGateway();

        System.out.println("🚪 Microservices API Gateway Demo");
        System.out.println("===================================");

        // 1. Fresh requests (cache misses)
        System.out.println("\n1. Fresh API requests:");
        ApiResponse userResponse = gateway.getUser("user123");
        System.out.println("   User response: " + userResponse.getData());

        ApiResponse productResponse = gateway.getProduct("prod456");
        System.out.println("   Product response: " + productResponse.getData());

        // 2. Repeated requests (cache hits)
        System.out.println("\n2. Repeated requests (cache hits):");
        gateway.getUser("user123");
        gateway.getProduct("prod456");
        System.out.println("   Requests served from cache");

        // 3. Cache warming
        System.out.println("\n3. Cache warming:");
        String[] popularUsers = { "user1", "user2", "user3", "user4", "user5" };
        gateway.warmCache(popularUsers).get();
        System.out.println("   Cache warming completed");

        // 4. Simulate service failure
        System.out.println("\n4. Service failure simulation:");
        gateway.updateServiceHealth("user-service", false);
        ApiResponse staleResponse = gateway.getUser("user123");
        System.out.println("   Stale response status: " + staleResponse.getCacheStatus());

        // 5. Show gateway statistics
        System.out.println("\n5. Gateway performance:");
        gateway.printGatewayStats();

        System.out.println("\n✅ API Gateway demo completed!");
    }
}

// Supporting classes
class ApiResponse {
    private final Object data;
    private final Instant timestamp;
    private final String serviceName;
    private final CacheStatus cacheStatus;

    public ApiResponse(Object data, Instant timestamp, String serviceName, CacheStatus cacheStatus) {
        this.data = data;
        this.timestamp = timestamp;
        this.serviceName = serviceName;
        this.cacheStatus = cacheStatus;
    }

    public Object getData() {
        return data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getServiceName() {
        return serviceName;
    }

    public CacheStatus getCacheStatus() {
        return cacheStatus;
    }
}

enum CacheStatus {
    HIT, MISS, STALE, DEFAULT
}

class UserProfile {
    private final String id;
    private final String name;
    private final String email;

    public UserProfile(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return String.format("UserProfile{id='%s', name='%s'}", id, name);
    }
}

class Product {
    private final String id;
    private final String name;
    private final double price;

    public Product(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%.2f}", id, name, price);
    }
}

class Order {
    private final String id;
    private final String userId;
    private final double total;

    public Order(String id, String userId, double total) {
        this.id = id;
        this.userId = userId;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public double getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return String.format("Order{id='%s', userId='%s', total=%.2f}", id, userId, total);
    }
}

class ServiceHealth {
    private final String serviceName;
    private final boolean healthy;
    private final Instant lastCheck;

    public ServiceHealth(String serviceName, boolean healthy, Instant lastCheck) {
        this.serviceName = serviceName;
        this.healthy = healthy;
        this.lastCheck = lastCheck;
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public Instant getLastCheck() {
        return lastCheck;
    }
}

// Simulated downstream services
class UserService {
    public UserProfile fetchUser(String userId) throws ServiceUnavailableException {
        simulateNetworkLatency();

        if (ThreadLocalRandom.current().nextDouble() < 0.05) { // 5% failure rate
            throw new ServiceUnavailableException("User service temporarily unavailable");
        }

        return new UserProfile(userId, "User " + userId, userId + "@example.com");
    }

    private void simulateNetworkLatency() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class ProductService {
    public Product fetchProduct(String productId) throws ServiceUnavailableException {
        simulateNetworkLatency();

        if (ThreadLocalRandom.current().nextDouble() < 0.03) { // 3% failure rate
            throw new ServiceUnavailableException("Product service temporarily unavailable");
        }

        return new Product(productId, "Product " + productId, 99.99);
    }

    private void simulateNetworkLatency() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(15, 75));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class OrderService {
    public Order fetchOrder(String orderId) throws ServiceUnavailableException {
        simulateNetworkLatency();
        return new Order(orderId, "user123", 299.99);
    }

    public boolean canUserAccessOrder(String userId, String orderId) {
        // Simplified authorization check
        return userId != null && orderId != null;
    }

    private void simulateNetworkLatency() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(20, 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// Custom exceptions
class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}

class TimeoutException extends RuntimeException {
    public TimeoutException(String message) {
        super(message);
    }
}

class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

class CacheLoadException extends RuntimeException {
    public CacheLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
