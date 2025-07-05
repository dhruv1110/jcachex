package io.github.dhruv1110.jcachex.monitoring;

import io.github.dhruv1110.jcachex.CacheStats;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive health monitoring for cache instances.
 * <p>
 * This class provides enterprise-grade health checks and monitoring
 * capabilities:
 * </p>
 * <ul>
 * <li>Performance threshold monitoring</li>
 * <li>Memory usage tracking</li>
 * <li>Error rate monitoring</li>
 * <li>Health status reporting</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * 
 * <pre>{@code
 * // Configure health monitoring
 * CacheHealth health = CacheHealth.builder()
 *         .hitRateThreshold(0.8)
 *         .maxResponseTime(Duration.ofMillis(100))
 *         .errorRateThreshold(0.05)
 *         .build();
 *
 * // Register cache for monitoring
 * health.registerCache("userCache", cache);
 *
 * // Check health status
 * HealthStatus status = health.getOverallHealth();
 * if (status.isHealthy()) {
 *     // System is healthy
 * } else {
 *     // Handle health issues
 *     handleHealthIssues(status.getIssues());
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class CacheHealth {

    /**
     * Health status enumeration.
     */
    public enum Status {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }

    /**
     * Health check result.
     */
    public static class HealthStatus {
        private final Status status;
        private final String message;
        private final List<String> issues;
        private final Map<String, Object> metrics;
        private final Instant checkTime;

        public HealthStatus(Status status, String message, List<String> issues, Map<String, Object> metrics) {
            this.status = status;
            this.message = message;
            this.issues = new ArrayList<>(issues);
            this.metrics = new ConcurrentHashMap<>(metrics);
            this.checkTime = Instant.now();
        }

        public Status getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public List<String> getIssues() {
            return new ArrayList<>(issues);
        }

        public Map<String, Object> getMetrics() {
            return new ConcurrentHashMap<>(metrics);
        }

        public Instant getCheckTime() {
            return checkTime;
        }

        public boolean isHealthy() {
            return status == Status.HEALTHY;
        }

        public boolean isDegraded() {
            return status == Status.DEGRADED;
        }

        public boolean isUnhealthy() {
            return status == Status.UNHEALTHY;
        }
    }

    private final double hitRateThreshold;
    private final Duration maxResponseTime;
    private final double errorRateThreshold;
    private final long maxMemoryUsage;
    private final Map<String, CacheMonitor> monitors;

    private CacheHealth(Builder builder) {
        this.hitRateThreshold = builder.hitRateThreshold;
        this.maxResponseTime = builder.maxResponseTime;
        this.errorRateThreshold = builder.errorRateThreshold;
        this.maxMemoryUsage = builder.maxMemoryUsage;
        this.monitors = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new health monitoring builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Registers a cache for health monitoring.
     *
     * @param name  the cache name
     * @param cache the cache instance to monitor
     */
    public void registerCache(String name, Object cache) {
        monitors.put(name, new CacheMonitor(name, cache));
    }

    /**
     * Unregisters a cache from health monitoring.
     *
     * @param name the cache name
     */
    public void unregisterCache(String name) {
        monitors.remove(name);
    }

    /**
     * Gets the overall health status of all monitored caches.
     *
     * @return the overall health status
     */
    public HealthStatus getOverallHealth() {
        List<String> issues = new ArrayList<>();
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        Status overallStatus = Status.HEALTHY;

        if (monitors.isEmpty()) {
            return new HealthStatus(Status.UNKNOWN, "No caches registered for monitoring", issues, metrics);
        }

        for (CacheMonitor monitor : monitors.values()) {
            HealthStatus cacheHealth = monitor.checkHealth(this);

            // Aggregate issues
            issues.addAll(cacheHealth.getIssues());

            // Aggregate metrics
            String prefix = monitor.name + ".";
            cacheHealth.getMetrics().forEach((key, value) -> metrics.put(prefix + key, value));

            // Determine worst status
            if (cacheHealth.getStatus() == Status.UNHEALTHY) {
                overallStatus = Status.UNHEALTHY;
            } else if (cacheHealth.getStatus() == Status.DEGRADED && overallStatus != Status.UNHEALTHY) {
                overallStatus = Status.DEGRADED;
            }
        }

        String message = generateOverallMessage(overallStatus, monitors.size());
        return new HealthStatus(overallStatus, message, issues, metrics);
    }

    /**
     * Gets the health status of a specific cache.
     *
     * @param cacheName the cache name
     * @return the cache health status, or null if not found
     */
    public HealthStatus getCacheHealth(String cacheName) {
        CacheMonitor monitor = monitors.get(cacheName);
        if (monitor == null) {
            return null;
        }
        return monitor.checkHealth(this);
    }

    /**
     * Gets health metrics for all registered caches.
     *
     * @return a map of cache names to their metrics
     */
    public Map<String, Map<String, Object>> getAllMetrics() {
        Map<String, Map<String, Object>> allMetrics = new ConcurrentHashMap<>();
        for (Map.Entry<String, CacheMonitor> entry : monitors.entrySet()) {
            HealthStatus health = entry.getValue().checkHealth(this);
            allMetrics.put(entry.getKey(), health.getMetrics());
        }
        return allMetrics;
    }

    private String generateOverallMessage(Status status, int cacheCount) {
        switch (status) {
            case HEALTHY:
                return "All " + cacheCount + " cache(s) are healthy";
            case DEGRADED:
                return "Some cache(s) are experiencing performance issues";
            case UNHEALTHY:
                return "One or more cache(s) are unhealthy";
            default:
                return "Cache health status unknown";
        }
    }

    /**
     * Internal cache monitor for individual cache instances.
     */
    private static class CacheMonitor {
        private final String name;
        private final Object cache;
        private volatile long lastCheckTime = 0;
        private volatile CacheStats lastStats = null;

        public CacheMonitor(String name, Object cache) {
            this.name = name;
            this.cache = cache;
        }

        public HealthStatus checkHealth(CacheHealth config) {
            List<String> issues = new ArrayList<>();
            Map<String, Object> metrics = new ConcurrentHashMap<>();
            Status status = Status.HEALTHY;

            try {
                // Get current stats (using reflection for compatibility)
                CacheStats stats = getCacheStats();
                if (stats != null) {
                    // Check hit rate
                    double hitRate = stats.hitRate();
                    metrics.put("hitRate", hitRate);
                    if (hitRate < config.hitRateThreshold) {
                        issues.add("Hit rate (" + String.format("%.2f", hitRate) +
                                ") below threshold (" + config.hitRateThreshold + ")");
                        status = Status.DEGRADED;
                    }

                    // Check error rate (approximated by load failures)
                    long totalRequests = stats.hitCount() + stats.missCount();
                    double errorRate = totalRequests > 0 ? (double) stats.loadFailureCount() / totalRequests : 0.0;
                    metrics.put("errorRate", errorRate);
                    if (errorRate > config.errorRateThreshold) {
                        issues.add("Error rate (" + String.format("%.2f", errorRate) +
                                ") above threshold (" + config.errorRateThreshold + ")");
                        status = Status.UNHEALTHY;
                    }

                    // Add other metrics
                    metrics.put("hitCount", stats.hitCount());
                    metrics.put("missCount", stats.missCount());
                    metrics.put("evictionCount", stats.evictionCount());
                    metrics.put("loadCount", stats.loadCount());
                    metrics.put("averageLoadTime", stats.averageLoadTime());
                }

                // Check cache size (using reflection for compatibility)
                Long cacheSize = getCacheSize();
                if (cacheSize != null) {
                    metrics.put("size", cacheSize);
                }

                lastStats = stats;
                lastCheckTime = System.currentTimeMillis();

            } catch (Exception e) {
                issues.add("Health check failed: " + e.getMessage());
                status = Status.UNKNOWN;
                metrics.put("error", e.getMessage());
            }

            String message = "Cache '" + name + "' status: " + status;
            return new HealthStatus(status, message, issues, metrics);
        }

        private CacheStats getCacheStats() {
            try {
                // Use reflection to call stats() method
                return (CacheStats) cache.getClass().getMethod("stats").invoke(cache);
            } catch (Exception e) {
                return null;
            }
        }

        private Long getCacheSize() {
            try {
                // Use reflection to call size() method
                Object size = cache.getClass().getMethod("size").invoke(cache);
                return size instanceof Long ? (Long) size : Long.valueOf(size.toString());
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Builder for creating cache health monitors.
     */
    public static class Builder {
        private double hitRateThreshold = 0.8;
        private Duration maxResponseTime = Duration.ofMillis(100);
        private double errorRateThreshold = 0.05;
        private long maxMemoryUsage = Long.MAX_VALUE;

        /**
         * Sets the minimum acceptable hit rate.
         *
         * @param hitRateThreshold the hit rate threshold (0.0 to 1.0)
         * @return this builder
         */
        public Builder hitRateThreshold(double hitRateThreshold) {
            if (hitRateThreshold < 0.0 || hitRateThreshold > 1.0) {
                throw new IllegalArgumentException("Hit rate threshold must be between 0.0 and 1.0");
            }
            this.hitRateThreshold = hitRateThreshold;
            return this;
        }

        /**
         * Sets the maximum acceptable response time.
         *
         * @param maxResponseTime the maximum response time
         * @return this builder
         */
        public Builder maxResponseTime(Duration maxResponseTime) {
            this.maxResponseTime = maxResponseTime;
            return this;
        }

        /**
         * Sets the maximum acceptable error rate.
         *
         * @param errorRateThreshold the error rate threshold (0.0 to 1.0)
         * @return this builder
         */
        public Builder errorRateThreshold(double errorRateThreshold) {
            if (errorRateThreshold < 0.0 || errorRateThreshold > 1.0) {
                throw new IllegalArgumentException("Error rate threshold must be between 0.0 and 1.0");
            }
            this.errorRateThreshold = errorRateThreshold;
            return this;
        }

        /**
         * Sets the maximum acceptable memory usage.
         *
         * @param maxMemoryUsage the maximum memory usage in bytes
         * @return this builder
         */
        public Builder maxMemoryUsage(long maxMemoryUsage) {
            this.maxMemoryUsage = maxMemoryUsage;
            return this;
        }

        /**
         * Builds the cache health monitor.
         *
         * @return a new cache health monitor
         */
        public CacheHealth build() {
            return new CacheHealth(this);
        }
    }
}
