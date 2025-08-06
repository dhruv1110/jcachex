package io.github.dhruv1110.jcachex.resilience;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Circuit breaker implementation for cache operations.
 * <p>
 * This circuit breaker provides production-grade resilience by automatically
 * failing fast when a cache operation is consistently failing, allowing the
 * system to recover gracefully and avoid cascading failures.
 * </p>
 *
 * <h3>Circuit Breaker States:</h3>
 * <ul>
 * <li><strong>CLOSED:</strong> Normal operation, all requests pass through</li>
 * <li><strong>OPEN:</strong> Failures exceeded threshold, all requests fail
 * fast</li>
 * <li><strong>HALF_OPEN:</strong> Testing if service has recovered</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Basic circuit breaker configuration
 * CircuitBreaker circuitBreaker = CircuitBreaker.builder()
 *         .failureThreshold(5)
 *         .recoveryTimeout(Duration.ofSeconds(30))
 *         .successThreshold(3)
 *         .build();
 *
 * // Protect cache operations
 * String value = circuitBreaker.execute(() -> {
 *     return expensiveServiceCall(key);
 * });
 *
 * // Handle circuit breaker state
 * if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
 *     // Use fallback mechanism
 *     return getCachedFallback(key);
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class CircuitBreaker {

    /**
     * Circuit breaker states.
     */
    public enum State {
        CLOSED, // Normal operation
        OPEN, // Failing fast
        HALF_OPEN // Testing recovery
    }

    private final int failureThreshold;
    private final Duration recoveryTimeout;
    private final int successThreshold;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);
    private final AtomicLong totalSuccesses = new AtomicLong(0);

    private CircuitBreaker(Builder builder) {
        this.failureThreshold = builder.failureThreshold;
        this.recoveryTimeout = builder.recoveryTimeout;
        this.successThreshold = builder.successThreshold;
    }

    /**
     * Creates a new circuit breaker builder.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Executes the given supplier with circuit breaker protection.
     *
     * @param <T>      the return type
     * @param supplier the operation to execute
     * @return the result of the operation
     * @throws CircuitBreakerException if circuit is open
     * @throws Exception               if the operation fails
     */
    public <T> T execute(Supplier<T> supplier) throws Exception {
        totalRequests.incrementAndGet();

        State currentState = state.get();

        // Check if circuit is open
        if (currentState == State.OPEN) {
            if (shouldAttemptRecovery()) {
                // Transition to half-open
                if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                    successCount.set(0);
                }
            } else {
                throw new CircuitBreakerException("Circuit breaker is OPEN");
            }
        }

        try {
            T result = supplier.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    /**
     * Executes the given runnable with circuit breaker protection.
     *
     * @param runnable the operation to execute
     * @throws CircuitBreakerException if circuit is open
     * @throws Exception               if the operation fails
     */
    public void execute(Runnable runnable) throws Exception {
        execute(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Returns the current state of the circuit breaker.
     *
     * @return current state
     */
    public State getState() {
        return state.get();
    }

    /**
     * Returns the current failure count.
     *
     * @return failure count
     */
    public int getFailureCount() {
        return failureCount.get();
    }

    /**
     * Returns the current success count (used in half-open state).
     *
     * @return success count
     */
    public int getSuccessCount() {
        return successCount.get();
    }

    /**
     * Returns the failure rate as a percentage.
     *
     * @return failure rate (0.0 to 1.0)
     */
    public double getFailureRate() {
        long total = totalRequests.get();
        return total == 0 ? 0.0 : (double) totalFailures.get() / total;
    }

    /**
     * Returns comprehensive circuit breaker metrics.
     *
     * @return circuit breaker metrics
     */
    public CircuitBreakerMetrics getMetrics() {
        return new CircuitBreakerMetrics(
                state.get(),
                failureCount.get(),
                successCount.get(),
                totalRequests.get(),
                totalFailures.get(),
                totalSuccesses.get(),
                getFailureRate());
    }

    /**
     * Manually opens the circuit breaker.
     */
    public void open() {
        state.set(State.OPEN);
        lastFailureTime.set(System.currentTimeMillis());
    }

    /**
     * Manually closes the circuit breaker.
     */
    public void close() {
        state.set(State.CLOSED);
        failureCount.set(0);
        successCount.set(0);
    }

    /**
     * Resets all circuit breaker statistics.
     */
    public void reset() {
        close();
        totalRequests.set(0);
        totalFailures.set(0);
        totalSuccesses.set(0);
        lastFailureTime.set(0);
    }

    private void onSuccess() {
        State currentState = state.get();
        totalSuccesses.incrementAndGet();

        if (currentState == State.HALF_OPEN) {
            int currentSuccessCount = successCount.incrementAndGet();
            if (currentSuccessCount >= successThreshold) {
                // Transition to closed
                state.set(State.CLOSED);
                failureCount.set(0);
                successCount.set(0);
            }
        } else if (currentState == State.CLOSED) {
            // Reset failure count on success
            failureCount.set(0);
        }
    }

    private void onFailure() {
        State currentState = state.get();
        totalFailures.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());

        if (currentState == State.HALF_OPEN) {
            // Go back to open on any failure in half-open state
            state.set(State.OPEN);
            successCount.set(0);
        } else if (currentState == State.CLOSED) {
            int currentFailureCount = failureCount.incrementAndGet();
            if (currentFailureCount >= failureThreshold) {
                // Transition to open
                state.set(State.OPEN);
            }
        }
    }

    private boolean shouldAttemptRecovery() {
        return System.currentTimeMillis() - lastFailureTime.get() >= recoveryTimeout.toMillis();
    }

    /**
     * Circuit breaker metrics container.
     */
    public static class CircuitBreakerMetrics {
        private final State state;
        private final int failureCount;
        private final int successCount;
        private final long totalRequests;
        private final long totalFailures;
        private final long totalSuccesses;
        private final double failureRate;

        public CircuitBreakerMetrics(State state, int failureCount, int successCount,
                long totalRequests, long totalFailures, long totalSuccesses,
                double failureRate) {
            this.state = state;
            this.failureCount = failureCount;
            this.successCount = successCount;
            this.totalRequests = totalRequests;
            this.totalFailures = totalFailures;
            this.totalSuccesses = totalSuccesses;
            this.failureRate = failureRate;
        }

        public State getState() {
            return state;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getTotalFailures() {
            return totalFailures;
        }

        public long getTotalSuccesses() {
            return totalSuccesses;
        }

        public double getFailureRate() {
            return failureRate;
        }
    }

    /**
     * Exception thrown when circuit breaker is open.
     */
    public static class CircuitBreakerException extends RuntimeException {
        public CircuitBreakerException(String message) {
            super(message);
        }
    }

    /**
     * Builder for CircuitBreaker.
     */
    public static class Builder {
        private int failureThreshold = 5;
        private Duration recoveryTimeout = Duration.ofSeconds(30);
        private int successThreshold = 3;

        public Builder failureThreshold(int failureThreshold) {
            if (failureThreshold <= 0) {
                throw new IllegalArgumentException("Failure threshold must be positive");
            }
            this.failureThreshold = failureThreshold;
            return this;
        }

        public Builder recoveryTimeout(Duration recoveryTimeout) {
            if (recoveryTimeout.isNegative()) {
                throw new IllegalArgumentException("Recovery timeout cannot be negative");
            }
            this.recoveryTimeout = recoveryTimeout;
            return this;
        }

        public Builder successThreshold(int successThreshold) {
            if (successThreshold <= 0) {
                throw new IllegalArgumentException("Success threshold must be positive");
            }
            this.successThreshold = successThreshold;
            return this;
        }

        public CircuitBreaker build() {
            return new CircuitBreaker(this);
        }
    }
}
