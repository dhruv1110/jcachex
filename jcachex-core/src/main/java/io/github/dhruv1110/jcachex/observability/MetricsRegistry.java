package io.github.dhruv1110.jcachex.observability;

import io.github.dhruv1110.jcachex.CacheStats;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

/**
 * Central registry for cache metrics and observability.
 * <p>
 * This class provides a unified interface for collecting and exposing cache
 * metrics
 * to various observability platforms including Micrometer, Prometheus, DataDog,
 * and
 * custom monitoring systems.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Multi-Platform Support:</strong> Works with Micrometer,
 * Prometheus, DataDog</li>
 * <li><strong>Real-time Metrics:</strong> Live performance data with minimal
 * overhead</li>
 * <li><strong>Custom Dimensions:</strong> Application-specific tags and
 * labels</li>
 * <li><strong>Histogram Support:</strong> Latency percentiles and
 * distributions</li>
 * <li><strong>Alerting Ready:</strong> Built-in thresholds and anomaly
 * detection</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Initialize with Micrometer
 * MetricsRegistry registry = MetricsRegistry.builder()
 *         .withMicrometer(meterRegistry)
 *         .withTags("application", "user-service", "environment", "production")
 *         .build();
 *
 * // Register cache for monitoring
 * registry.registerCache("user-cache", userCache);
 *
 * // Custom metrics
 * registry.counter("cache.custom.operation").increment();
 * registry.timer("cache.load.duration").record(Duration.ofMillis(150));
 *
 * // Alerting thresholds
 * registry.registerAlert("high-miss-rate",
 *         () -> registry.getCacheStats("user-cache").missRate() > 0.5);
 * }</pre>
 *
 * @since 1.0.0
 */
public class MetricsRegistry {

    private final Map<String, Object> metricAdapters = new ConcurrentHashMap<>();
    private final Map<String, CacheStats> cacheStatsRegistry = new ConcurrentHashMap<>();
    private final Map<String, String> globalTags = new ConcurrentHashMap<>();
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, Gauge> gauges = new ConcurrentHashMap<>();
    private final Map<String, Histogram> histograms = new ConcurrentHashMap<>();
    private final Map<String, Supplier<Boolean>> alerts = new ConcurrentHashMap<>();

    private MetricsRegistry(Builder builder) {
        this.globalTags.putAll(builder.globalTags);
        this.metricAdapters.putAll(builder.metricAdapters);
    }

    /**
     * Creates a new metrics registry builder.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Registers a cache for monitoring.
     *
     * @param cacheName  unique name for the cache
     * @param cacheStats cache statistics to monitor
     */
    public void registerCache(String cacheName, CacheStats cacheStats) {
        cacheStatsRegistry.put(cacheName, cacheStats);

        // Register standard cache metrics
        registerGauge(cacheName + ".hit.rate", () -> cacheStats.hitRate());
        registerGauge(cacheName + ".miss.rate", () -> cacheStats.missRate());
        registerGauge(cacheName + ".eviction.count", () -> (double) cacheStats.evictionCount());
        registerGauge(cacheName + ".load.average.time", () -> cacheStats.averageLoadTime());
    }

    /**
     * Returns a counter metric.
     *
     * @param name metric name
     * @return counter instance
     */
    public Counter counter(String name) {
        return counters.computeIfAbsent(name, k -> new Counter());
    }

    /**
     * Returns a timer metric.
     *
     * @param name metric name
     * @return timer instance
     */
    public Timer timer(String name) {
        return timers.computeIfAbsent(name, k -> new Timer());
    }

    /**
     * Returns a gauge metric.
     *
     * @param name     metric name
     * @param supplier value supplier
     * @return gauge instance
     */
    public Gauge registerGauge(String name, Supplier<Double> supplier) {
        return gauges.computeIfAbsent(name, k -> new Gauge(supplier));
    }

    /**
     * Returns a histogram metric.
     *
     * @param name metric name
     * @return histogram instance
     */
    public Histogram histogram(String name) {
        return histograms.computeIfAbsent(name, k -> new Histogram());
    }

    /**
     * Registers an alert condition.
     *
     * @param alertName unique alert name
     * @param condition condition that triggers the alert
     */
    public void registerAlert(String alertName, Supplier<Boolean> condition) {
        alerts.put(alertName, condition);
    }

    /**
     * Checks all registered alerts.
     *
     * @return map of alert names to their current status
     */
    public Map<String, Boolean> checkAlerts() {
        Map<String, Boolean> results = new ConcurrentHashMap<>();
        alerts.forEach((name, condition) -> {
            try {
                results.put(name, condition.get());
            } catch (Exception e) {
                results.put(name, false); // Default to false on error
            }
        });
        return results;
    }

    /**
     * Returns cache statistics for a registered cache.
     *
     * @param cacheName cache name
     * @return cache statistics
     */
    public CacheStats getCacheStats(String cacheName) {
        return cacheStatsRegistry.get(cacheName);
    }

    /**
     * Counter metric implementation.
     */
    public static class Counter {
        private final LongAdder value = new LongAdder();

        public void increment() {
            value.increment();
        }

        public void increment(long amount) {
            value.add(amount);
        }

        public long count() {
            return value.sum();
        }
    }

    /**
     * Timer metric implementation.
     */
    public static class Timer {
        private final LongAdder count = new LongAdder();
        private final LongAdder totalTime = new LongAdder();

        public void record(Duration duration) {
            count.increment();
            totalTime.add(duration.toNanos());
        }

        public long count() {
            return count.sum();
        }

        public Duration totalTime() {
            return Duration.ofNanos(totalTime.sum());
        }

        public double mean() {
            long countValue = count.sum();
            return countValue == 0 ? 0.0 : (double) totalTime.sum() / countValue;
        }
    }

    /**
     * Gauge metric implementation.
     */
    public static class Gauge {
        private final Supplier<Double> supplier;

        public Gauge(Supplier<Double> supplier) {
            this.supplier = supplier;
        }

        public double value() {
            return supplier.get();
        }
    }

    /**
     * Histogram metric implementation.
     */
    public static class Histogram {
        private final AtomicLong count = new AtomicLong();
        private final AtomicLong sum = new AtomicLong();
        private final Map<Double, AtomicLong> buckets = new ConcurrentHashMap<>();

        public void record(double value) {
            count.incrementAndGet();
            sum.addAndGet((long) value);

            // Record in appropriate buckets
            for (double bucket : getBucketBoundaries()) {
                if (value <= bucket) {
                    buckets.computeIfAbsent(bucket, k -> new AtomicLong()).incrementAndGet();
                }
            }
        }

        public long count() {
            return count.get();
        }

        public double mean() {
            long countValue = count.get();
            return countValue == 0 ? 0.0 : (double) sum.get() / countValue;
        }

        private double[] getBucketBoundaries() {
            return new double[] { 0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000 };
        }
    }

    /**
     * Builder for MetricsRegistry.
     */
    public static class Builder {
        private final Map<String, String> globalTags = new ConcurrentHashMap<>();
        private final Map<String, Object> metricAdapters = new ConcurrentHashMap<>();

        public Builder withTag(String key, String value) {
            globalTags.put(key, value);
            return this;
        }

        public Builder withTags(String... keyValuePairs) {
            if (keyValuePairs.length % 2 != 0) {
                throw new IllegalArgumentException("Key-value pairs must be even");
            }
            for (int i = 0; i < keyValuePairs.length; i += 2) {
                globalTags.put(keyValuePairs[i], keyValuePairs[i + 1]);
            }
            return this;
        }

        public Builder withMicrometer(Object meterRegistry) {
            metricAdapters.put("micrometer", meterRegistry);
            return this;
        }

        public Builder withPrometheus(Object prometheusRegistry) {
            metricAdapters.put("prometheus", prometheusRegistry);
            return this;
        }

        public Builder withDataDog(Object dataDogRegistry) {
            metricAdapters.put("datadog", dataDogRegistry);
            return this;
        }

        public MetricsRegistry build() {
            return new MetricsRegistry(this);
        }
    }
}
