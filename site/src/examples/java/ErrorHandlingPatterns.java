import io.github.dhruv1110.jcachex.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.function.Function;

/**
 * Comprehensive Error Handling and Resilience Patterns
 * Demonstrates production-ready error handling with circuit breakers,
 * fallback strategies, graceful degradation, and monitoring integration
 */
public class ErrorHandlingPatterns {
    private static final Logger log = Logger.getLogger(ErrorHandlingPatterns.class.getName());

    // Simulated external services
    private static final UserService userService = new UserService();
    private static final NotificationService notificationService = new NotificationService();
    private static final MetricsService metricsService = new MetricsService();

    public static void main(String[] args) {
        System.out.println("🛡️  Error Handling & Resilience Patterns Demo");
        System.out.println("=============================================");

        // 1. Basic error handling with retry
        demonstrateBasicErrorHandling();

        // 2. Circuit breaker pattern
        demonstrateCircuitBreakerPattern();

        // 3. Fallback strategies
        demonstrateFallbackStrategies();

        // 4. Graceful degradation
        demonstrateGracefulDegradation();

        // 5. Monitoring and alerting
        demonstrateMonitoringIntegration();

        System.out.println("\n✅ All error handling patterns demonstrated successfully!");
    }

    // 1. Basic Error Handling with Retry Logic
    private static void demonstrateBasicErrorHandling() {
        System.out.println("\n1. Basic Error Handling with Retry");
        System.out.println("==================================");

        Cache<String, UserProfile> cache = JCacheXBuilder.forReadHeavyWorkload()
                .name("users-with-retry")
                .maximumSize(10000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats(true)
                .build();

        String userId = "user123";
        UserProfile user = loadWithRetry(cache, userId, 3);

        if (user != null) {
            System.out.println("✅ Successfully loaded user: " + user.getName());
        } else {
            System.out.println("❌ Failed to load user after retries");
        }
    }

    /**
     * Retry logic with exponential backoff
     */
    private static UserProfile loadWithRetry(Cache<String, UserProfile> cache, String userId, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return cache.get(userId, () -> {
                    log.info("Attempting to load user {} (attempt {})", userId, attempt);
                    return userService.loadUser(userId);
                });
            } catch (Exception e) {
                log.warning("Attempt {} failed: {}", attempt, e.getMessage());

                if (attempt == maxRetries) {
                    log.error("All retry attempts exhausted for user {}", userId);
                    metricsService.recordFailure("user_load", userId);
                    return null;
                }

                // Exponential backoff
                try {
                    Thread.sleep(1000 * (long) Math.pow(2, attempt - 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    // 2. Circuit Breaker Pattern
    private static void demonstrateCircuitBreakerPattern() {
        System.out.println("\n2. Circuit Breaker Pattern");
        System.out.println("==========================");

        ResilientCacheService service = new ResilientCacheService();

        // Simulate service calls that might fail
        for (int i = 1; i <= 10; i++) {
            try {
                UserProfile user = service.getUserWithCircuitBreaker("user" + i);
                System.out.println("✅ Request " + i + ": " + user.getName());
            } catch (Exception e) {
                System.out.println("❌ Request " + i + ": " + e.getMessage());
            }
        }

        // Show circuit breaker state
        System.out.println("Circuit breaker state: " + service.getCircuitBreakerState());
    }

    // 3. Fallback Strategies
    private static void demonstrateFallbackStrategies() {
        System.out.println("\n3. Fallback Strategies");
        System.out.println("======================");

        FallbackCacheService service = new FallbackCacheService();

        // Test different fallback scenarios
        String[] userIds = { "user1", "user2", "unavailable-user", "timeout-user" };

        for (String userId : userIds) {
            UserProfile user = service.getUserWithFallback(userId);
            System.out.println("User " + userId + ": " + user.getName() + " (Source: " + user.getSource() + ")");
        }
    }

    // 4. Graceful Degradation
    private static void demonstrateGracefulDegradation() {
        System.out.println("\n4. Graceful Degradation");
        System.out.println("=======================");

        GracefulDegradationService service = new GracefulDegradationService();

        // Simulate various service states
        System.out.println("Normal operation:");
        service.processUserRequest("user1");

        System.out.println("\nDegraded operation (notifications disabled):");
        service.simulateNotificationFailure();
        service.processUserRequest("user2");

        System.out.println("\nMinimal operation (only essential services):");
        service.simulateMultipleFailures();
        service.processUserRequest("user3");
    }

    // 5. Monitoring and Alerting Integration
    private static void demonstrateMonitoringIntegration() {
        System.out.println("\n5. Monitoring & Alerting Integration");
        System.out.println("====================================");

        MonitoringIntegratedCache service = new MonitoringIntegratedCache();

        // Generate some traffic to trigger monitoring
        for (int i = 0; i < 20; i++) {
            service.getDataWithMonitoring("data" + (i % 5));
        }

        // Show monitoring results
        service.printMonitoringStats();
    }
}

// Circuit Breaker Implementation
class ResilientCacheService {
    private final Cache<String, UserProfile> cache;
    private final CircuitBreaker circuitBreaker;

    public ResilientCacheService() {
        this.cache = JCacheXBuilder.forReadHeavyWorkload()
                .name("resilient-users")
                .maximumSize(5000L)
                .expireAfterWrite(Duration.ofMinutes(15))
                .build();

        this.circuitBreaker = new CircuitBreaker(
                5, // failure threshold
                Duration.ofSeconds(30), // timeout
                Duration.ofSeconds(60) // recovery timeout
        );
    }

    public UserProfile getUserWithCircuitBreaker(String userId) {
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            throw new ServiceUnavailableException("Circuit breaker is OPEN");
        }

        try {
            UserProfile user = cache.get(userId, () -> {
                if (circuitBreaker.getState() == CircuitBreaker.State.HALF_OPEN) {
                    log.info("Circuit breaker in HALF_OPEN state, testing service");
                }

                return userService.loadUser(userId);
            });

            circuitBreaker.recordSuccess();
            return user;

        } catch (Exception e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    public CircuitBreaker.State getCircuitBreakerState() {
        return circuitBreaker.getState();
    }
}

// Fallback Strategies Implementation
class FallbackCacheService {
    private final Cache<String, UserProfile> primaryCache;
    private final Cache<String, UserProfile> fallbackCache;

    public FallbackCacheService() {
        this.primaryCache = JCacheXBuilder.forReadHeavyWorkload()
                .name("primary-users")
                .maximumSize(10000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .build();

        this.fallbackCache = JCacheXBuilder.forMemoryConstrainedEnvironment()
                .name("fallback-users")
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofHours(2))
                .build();
    }

    public UserProfile getUserWithFallback(String userId) {
        try {
            // Try primary cache first
            return primaryCache.get(userId, () -> {
                UserProfile user = userService.loadUser(userId);
                // Store in fallback cache for future use
                fallbackCache.put(userId, user);
                return user;
            });

        } catch (ServiceUnavailableException e) {
            log.warning("Primary service unavailable for {}, trying fallback", userId);
            return getFallbackUser(userId);

        } catch (TimeoutException e) {
            log.warning("Primary service timeout for {}, trying fallback", userId);
            return getFallbackUser(userId);

        } catch (Exception e) {
            log.error("Unexpected error for {}: {}", userId, e.getMessage());
            return getFallbackUser(userId);
        }
    }

    private UserProfile getFallbackUser(String userId) {
        // Try fallback cache
        UserProfile fallbackUser = fallbackCache.getIfPresent(userId);
        if (fallbackUser != null) {
            fallbackUser.setSource("fallback-cache");
            return fallbackUser;
        }

        // Last resort: return default user
        return createDefaultUser(userId);
    }

    private UserProfile createDefaultUser(String userId) {
        UserProfile defaultUser = new UserProfile(userId, "Unknown User", "unknown@example.com");
        defaultUser.setSource("default");
        return defaultUser;
    }
}

// Graceful Degradation Implementation
class GracefulDegradationService {
    private final Cache<String, UserProfile> cache;
    private boolean notificationServiceAvailable = true;
    private boolean analyticsServiceAvailable = true;
    private boolean recommendationServiceAvailable = true;

    public GracefulDegradationService() {
        this.cache = JCacheXBuilder.forReadHeavyWorkload()
                .name("graceful-users")
                .maximumSize(5000L)
                .expireAfterWrite(Duration.ofMinutes(20))
                .build();
    }

    public void processUserRequest(String userId) {
        try {
            // Essential: User data (always required)
            UserProfile user = cache.get(userId, () -> userService.loadUser(userId));
            System.out.println("✅ Core: User data loaded for " + user.getName());

            // Optional: Notifications (graceful degradation)
            if (notificationServiceAvailable) {
                try {
                    notificationService.sendWelcomeNotification(userId);
                    System.out.println("✅ Optional: Welcome notification sent");
                } catch (Exception e) {
                    log.warning("Notification service failed, continuing without notifications");
                    notificationServiceAvailable = false;
                }
            } else {
                System.out.println("⚠️  Optional: Notifications disabled (service unavailable)");
            }

            // Optional: Analytics tracking (graceful degradation)
            if (analyticsServiceAvailable) {
                try {
                    trackUserActivity(userId);
                    System.out.println("✅ Optional: User activity tracked");
                } catch (Exception e) {
                    log.warning("Analytics service failed, continuing without tracking");
                    analyticsServiceAvailable = false;
                }
            } else {
                System.out.println("⚠️  Optional: Analytics disabled (service unavailable)");
            }

            // Optional: Recommendations (graceful degradation)
            if (recommendationServiceAvailable) {
                try {
                    loadRecommendations(userId);
                    System.out.println("✅ Optional: Recommendations loaded");
                } catch (Exception e) {
                    log.warning("Recommendation service failed, continuing without recommendations");
                    recommendationServiceAvailable = false;
                }
            } else {
                System.out.println("⚠️  Optional: Recommendations disabled (service unavailable)");
            }

        } catch (Exception e) {
            log.error("Critical error processing user request: {}", e.getMessage());
            throw new RuntimeException("Essential service failed", e);
        }
    }

    public void simulateNotificationFailure() {
        this.notificationServiceAvailable = false;
    }

    public void simulateMultipleFailures() {
        this.notificationServiceAvailable = false;
        this.analyticsServiceAvailable = false;
        this.recommendationServiceAvailable = false;
    }

    private void trackUserActivity(String userId) {
        // Simulate analytics tracking
        if (Math.random() < 0.1) { // 10% failure rate
            throw new RuntimeException("Analytics service unavailable");
        }
    }

    private void loadRecommendations(String userId) {
        // Simulate recommendation loading
        if (Math.random() < 0.15) { // 15% failure rate
            throw new RuntimeException("Recommendation service unavailable");
        }
    }
}

// Monitoring Integration Implementation
class MonitoringIntegratedCache {
    private final Cache<String, String> cache;
    private final MetricsService metrics;

    public MonitoringIntegratedCache() {
        this.cache = JCacheXBuilder.forReadHeavyWorkload()
                .name("monitored-cache")
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats(true)
                .evictionListener(this::onEviction)
                .build();

        this.metrics = new MetricsService();
    }

    public String getDataWithMonitoring(String key) {
        long startTime = System.nanoTime();

        try {
            String data = cache.get(key, () -> {
                // Simulate data loading
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
                return "Data for " + key;
            });

            long endTime = System.nanoTime();
            metrics.recordSuccess("cache_get", endTime - startTime);

            return data;

        } catch (Exception e) {
            long endTime = System.nanoTime();
            metrics.recordFailure("cache_get", endTime - startTime);

            // Send alert for repeated failures
            if (metrics.getFailureRate("cache_get") > 0.1) { // 10% failure rate threshold
                sendAlert("High cache failure rate detected: " + metrics.getFailureRate("cache_get"));
            }

            throw e;
        }
    }

    private void onEviction(String key, String value, RemovalCause cause) {
        metrics.recordEviction("cache_eviction", cause.name());

        // Alert on unexpected evictions
        if (cause == RemovalCause.SIZE && metrics.getEvictionRate() > 0.2) {
            sendAlert("High eviction rate detected, consider increasing cache size");
        }
    }

    private void sendAlert(String message) {
        log.warning("ALERT: " + message);
        // In production: send to alerting system (PagerDuty, Slack, etc.)
    }

    public void printMonitoringStats() {
        CacheStats stats = cache.stats();
        System.out.println("Cache Statistics:");
        System.out.printf("  Hit Rate: %.2f%%\n", stats.hitRate() * 100);
        System.out.printf("  Miss Rate: %.2f%%\n", stats.missRate() * 100);
        System.out.printf("  Eviction Count: %d\n", stats.evictionCount());
        System.out.printf("  Average Load Time: %.2fms\n", stats.averageLoadPenalty() / 1_000_000.0);

        System.out.println("\nApplication Metrics:");
        System.out.printf("  Success Rate: %.2f%%\n", (1 - metrics.getFailureRate("cache_get")) * 100);
        System.out.printf("  Failure Rate: %.2f%%\n", metrics.getFailureRate("cache_get") * 100);
        System.out.printf("  Eviction Rate: %.2f%%\n", metrics.getEvictionRate() * 100);
    }
}

// Supporting classes and utilities
class UserProfile {
    private final String id;
    private final String name;
    private final String email;
    private String source = "primary";

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}

class UserService {
    public UserProfile loadUser(String userId) {
        // Simulate network latency
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate failures
        if (userId.contains("unavailable")) {
            throw new ServiceUnavailableException("User service temporarily unavailable");
        }
        if (userId.contains("timeout")) {
            throw new TimeoutException("User service timeout");
        }

        return new UserProfile(userId, "User " + userId, userId + "@example.com");
    }
}

class NotificationService {
    public void sendWelcomeNotification(String userId) {
        // Simulate notification sending
        if (Math.random() < 0.2) { // 20% failure rate
            throw new RuntimeException("Notification service failed");
        }
    }
}

class MetricsService {
    private int successCount = 0;
    private int failureCount = 0;
    private int evictionCount = 0;

    public void recordSuccess(String operation, long duration) {
        successCount++;
        // In production: send to metrics system
    }

    public void recordFailure(String operation, long duration) {
        failureCount++;
        // In production: send to metrics system
    }

    public void recordEviction(String operation, String cause) {
        evictionCount++;
        // In production: send to metrics system
    }

    public double getFailureRate(String operation) {
        int total = successCount + failureCount;
        return total > 0 ? (double) failureCount / total : 0.0;
    }

    public double getEvictionRate() {
        int total = successCount + failureCount;
        return total > 0 ? (double) evictionCount / total : 0.0;
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

// Circuit Breaker Implementation
class CircuitBreaker {
    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private final int failureThreshold;
    private final Duration timeout;
    private final Duration recoveryTimeout;

    private State state = State.CLOSED;
    private int failureCount = 0;
    private Instant lastFailureTime;

    public CircuitBreaker(int failureThreshold, Duration timeout, Duration recoveryTimeout) {
        this.failureThreshold = failureThreshold;
        this.timeout = timeout;
        this.recoveryTimeout = recoveryTimeout;
    }

    public void recordSuccess() {
        this.failureCount = 0;
        this.state = State.CLOSED;
    }

    public void recordFailure() {
        this.failureCount++;
        this.lastFailureTime = Instant.now();

        if (failureCount >= failureThreshold) {
            this.state = State.OPEN;
        }
    }

    public State getState() {
        if (state == State.OPEN && lastFailureTime != null) {
            if (Duration.between(lastFailureTime, Instant.now()).compareTo(recoveryTimeout) > 0) {
                state = State.HALF_OPEN;
            }
        }
        return state;
    }
}
