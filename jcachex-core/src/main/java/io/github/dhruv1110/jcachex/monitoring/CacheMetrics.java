package io.github.dhruv1110.jcachex.monitoring;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * Advanced metrics collection for cache performance monitoring.
 * <p>
 * This class provides comprehensive metrics collection for production
 * monitoring:
 * </p>
 * <ul>
 * <li>Response time histograms</li>
 * <li>Throughput measurements</li>
 * <li>Error rate tracking</li>
 * <li>Custom metric registration</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * 
 * <pre>{@code
 * // Create metrics collector
 * CacheMetrics metrics = CacheMetrics.create("userCache");
 *
 * // Record operation timing
 * CacheMetrics.Timer timer = metrics.startTimer("get");
 * try {
 *     String value = cache.get(key);
 *     timer.recordSuccess();
 *     return value;
 * } catch (Exception e) {
 *     timer.recordError();
 *     throw e;
 * }
 *
 * // Export metrics for monitoring systems
 * Map<String, Object> allMetrics = metrics.exportMetrics();
 * }</pre>
 *
 * @since 1.0.0
 */
public class CacheMetrics {

    private final String cacheName;
    private final Map<String, OperationMetrics> operationMetrics;
    private final Map<String, Object> customMetrics;
    private final Instant creationTime;

    public CacheMetrics(String cacheName) {
        this.cacheName = cacheName;
        this.operationMetrics = new ConcurrentHashMap<>();
        this.customMetrics = new ConcurrentHashMap<>();
        this.creationTime = Instant.now();
    }

    /**
     * Creates a new metrics collector for the specified cache.
     *
     * @param cacheName the cache name
     * @return a new metrics collector
     */
    public static CacheMetrics create(String cacheName) {
        return new CacheMetrics(cacheName);
    }

    /**
     * Starts a timer for the specified operation.
     *
     * @param operation the operation name
     * @return a timer instance
     */
    public Timer startTimer(String operation) {
        OperationMetrics opMetrics = operationMetrics.computeIfAbsent(
                operation, k -> new OperationMetrics());
        return new Timer(opMetrics);
    }

    /**
     * Records a counter metric.
     *
     * @param name  the metric name
     * @param value the value to add
     */
    public void recordCounter(String name, long value) {
        customMetrics.compute(name, (k, v) -> {
            if (v instanceof LongAdder) {
                ((LongAdder) v).add(value);
                return v;
            } else {
                LongAdder adder = new LongAdder();
                adder.add(value);
                return adder;
            }
        });
    }

    /**
     * Records a gauge metric.
     *
     * @param name  the metric name
     * @param value the gauge value
     */
    public void recordGauge(String name, double value) {
        customMetrics.put(name, value);
    }

    /**
     * Records a histogram value.
     *
     * @param name  the metric name
     * @param value the histogram value
     */
    public void recordHistogram(String name, double value) {
        customMetrics.compute(name, (k, v) -> {
            if (v instanceof Histogram) {
                ((Histogram) v).record(value);
                return v;
            } else {
                Histogram histogram = new Histogram();
                histogram.record(value);
                return histogram;
            }
        });
    }

    /**
     * Exports all collected metrics.
     *
     * @return a map of metric names to their values
     */
    public Map<String, Object> exportMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();

        // Add cache metadata
        metrics.put("cache.name", cacheName);
        metrics.put("cache.uptime.seconds",
                Duration.between(creationTime, Instant.now()).getSeconds());

        // Add operation metrics
        for (Map.Entry<String, OperationMetrics> entry : operationMetrics.entrySet()) {
            String operation = entry.getKey();
            OperationMetrics opMetrics = entry.getValue();

            String prefix = "operation." + operation + ".";
            metrics.put(prefix + "count", opMetrics.getCount());
            metrics.put(prefix + "success.count", opMetrics.getSuccessCount());
            metrics.put(prefix + "error.count", opMetrics.getErrorCount());
            metrics.put(prefix + "success.rate", opMetrics.getSuccessRate());
            metrics.put(prefix + "average.duration.ms", opMetrics.getAverageDuration());
            metrics.put(prefix + "min.duration.ms", opMetrics.getMinDuration());
            metrics.put(prefix + "max.duration.ms", opMetrics.getMaxDuration());
        }

        // Add custom metrics
        for (Map.Entry<String, Object> entry : customMetrics.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof LongAdder) {
                metrics.put("custom." + name, ((LongAdder) value).sum());
            } else if (value instanceof DoubleAdder) {
                metrics.put("custom." + name, ((DoubleAdder) value).sum());
            } else if (value instanceof Histogram) {
                Histogram histogram = (Histogram) value;
                metrics.put("custom." + name + ".count", histogram.getCount());
                metrics.put("custom." + name + ".sum", histogram.getSum());
                metrics.put("custom." + name + ".average", histogram.getAverage());
                metrics.put("custom." + name + ".min", histogram.getMin());
                metrics.put("custom." + name + ".max", histogram.getMax());
            } else {
                metrics.put("custom." + name, value);
            }
        }

        return metrics;
    }

    /**
     * Resets all metrics.
     */
    public void reset() {
        operationMetrics.clear();
        customMetrics.clear();
    }

    /**
     * Gets the cache name.
     *
     * @return the cache name
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * Timer for measuring operation durations.
     */
    public static class Timer {
        private final OperationMetrics metrics;
        private final long startTime;
        private boolean completed = false;

        Timer(OperationMetrics metrics) {
            this.metrics = metrics;
            this.startTime = System.nanoTime();
        }

        /**
         * Records a successful operation completion.
         */
        public void recordSuccess() {
            if (completed)
                return;
            completed = true;
            long duration = System.nanoTime() - startTime;
            metrics.recordSuccess(duration);
        }

        /**
         * Records an error during operation.
         */
        public void recordError() {
            if (completed)
                return;
            completed = true;
            long duration = System.nanoTime() - startTime;
            metrics.recordError(duration);
        }

        /**
         * Auto-closes the timer as successful (for try-with-resources).
         */
        public void close() {
            recordSuccess();
        }
    }

    /**
     * Metrics for individual cache operations.
     */
    private static class OperationMetrics {
        private final LongAdder successCount = new LongAdder();
        private final LongAdder errorCount = new LongAdder();
        private final DoubleAdder totalDuration = new DoubleAdder();
        private final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxDuration = new AtomicLong(Long.MIN_VALUE);

        void recordSuccess(long durationNanos) {
            successCount.increment();
            recordDuration(durationNanos);
        }

        void recordError(long durationNanos) {
            errorCount.increment();
            recordDuration(durationNanos);
        }

        private void recordDuration(long durationNanos) {
            double durationMs = durationNanos / 1_000_000.0;
            totalDuration.add(durationMs);

            // Update min/max atomically
            minDuration.updateAndGet(current -> Math.min(current, durationNanos));
            maxDuration.updateAndGet(current -> Math.max(current, durationNanos));
        }

        long getCount() {
            return successCount.sum() + errorCount.sum();
        }

        long getSuccessCount() {
            return successCount.sum();
        }

        long getErrorCount() {
            return errorCount.sum();
        }

        double getSuccessRate() {
            long total = getCount();
            return total > 0 ? (double) getSuccessCount() / total : 0.0;
        }

        double getAverageDuration() {
            long count = getCount();
            return count > 0 ? totalDuration.sum() / count : 0.0;
        }

        double getMinDuration() {
            long min = minDuration.get();
            return min == Long.MAX_VALUE ? 0.0 : min / 1_000_000.0;
        }

        double getMaxDuration() {
            long max = maxDuration.get();
            return max == Long.MIN_VALUE ? 0.0 : max / 1_000_000.0;
        }
    }

    /**
     * Simple histogram implementation for value distribution tracking.
     */
    private static class Histogram {
        private final LongAdder count = new LongAdder();
        private final DoubleAdder sum = new DoubleAdder();
        private final AtomicLong minValue = new AtomicLong(Double.doubleToLongBits(Double.MAX_VALUE));
        private final AtomicLong maxValue = new AtomicLong(Double.doubleToLongBits(Double.MIN_VALUE));

        void record(double value) {
            count.increment();
            sum.add(value);

            long valueBits = Double.doubleToLongBits(value);
            minValue.updateAndGet(current -> {
                double currentValue = Double.longBitsToDouble(current);
                return value < currentValue ? valueBits : current;
            });
            maxValue.updateAndGet(current -> {
                double currentValue = Double.longBitsToDouble(current);
                return value > currentValue ? valueBits : current;
            });
        }

        long getCount() {
            return count.sum();
        }

        double getSum() {
            return sum.sum();
        }

        double getAverage() {
            long cnt = getCount();
            return cnt > 0 ? getSum() / cnt : 0.0;
        }

        double getMin() {
            long min = minValue.get();
            return min == Double.doubleToLongBits(Double.MAX_VALUE) ? 0.0 : Double.longBitsToDouble(min);
        }

        double getMax() {
            long max = maxValue.get();
            return max == Double.doubleToLongBits(Double.MIN_VALUE) ? 0.0 : Double.longBitsToDouble(max);
        }
    }
}
