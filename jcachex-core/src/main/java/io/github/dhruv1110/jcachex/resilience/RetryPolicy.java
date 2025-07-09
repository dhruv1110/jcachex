package io.github.dhruv1110.jcachex.resilience;

import io.github.dhruv1110.jcachex.exceptions.CacheException;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Configurable retry policy for cache operations.
 * <p>
 * This class provides production-grade retry mechanisms with:
 * </p>
 * <ul>
 * <li>Configurable retry attempts and delays</li>
 * <li>Exponential backoff with jitter</li>
 * <li>Selective retry based on exception types</li>
 * <li>Circuit breaker integration</li>
 * <li>Configurable random number generator for jitter</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Basic retry policy
 * RetryPolicy retryPolicy = RetryPolicy.builder()
 *         .maxAttempts(3)
 *         .initialDelay(Duration.ofMillis(100))
 *         .maxDelay(Duration.ofSeconds(1))
 *         .retryOnException(CacheException.class)
 *         .build();
 *
 * // Execute with retry
 * String result = retryPolicy.execute(() -> {
 *     return cache.get(key);
 * });
 *
 * // Async execution with retry
 * CompletableFuture<String> future = retryPolicy.executeAsync(() -> {
 *     return cache.getAsync(key);
 * });
 *
 * // With secure random for security-sensitive environments
 * RetryPolicy secureRetryPolicy = RetryPolicy.builder()
 *         .maxAttempts(3)
 *         .secureRandom(true) // Uses SecureRandom instead of ThreadLocalRandom
 *         .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public class RetryPolicy {

    private final int maxAttempts;
    private final Duration initialDelay;
    private final Duration maxDelay;
    private final double backoffMultiplier;
    private final double jitterFactor;
    private final Predicate<Throwable> retryPredicate;
    private final Function<Integer, Duration> delayFunction;
    private final Supplier<Double> randomSupplier;

    private RetryPolicy(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialDelay = builder.initialDelay;
        this.maxDelay = builder.maxDelay;
        this.backoffMultiplier = builder.backoffMultiplier;
        this.jitterFactor = builder.jitterFactor;
        this.retryPredicate = builder.retryPredicate;
        this.delayFunction = builder.delayFunction;
        this.randomSupplier = builder.randomSupplier;
    }

    /**
     * Creates a new retry policy builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a default retry policy with reasonable defaults.
     *
     * @return a default retry policy
     */
    public static RetryPolicy defaultPolicy() {
        return builder()
                .maxAttempts(3)
                .initialDelay(Duration.ofMillis(100))
                .maxDelay(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Executes the given supplier with retry logic.
     *
     * @param <T>      the return type
     * @param supplier the operation to execute
     * @return the result of the operation
     * @throws Exception if all retry attempts fail
     */
    public <T> T execute(Supplier<T> supplier) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return supplier.get();
            } catch (Exception e) {
                lastException = e;

                // Don't retry if this is the last attempt
                if (attempt == maxAttempts) {
                    break;
                }

                // Don't retry if exception is not retryable
                if (!shouldRetry(e)) {
                    break;
                }

                // Calculate delay for next attempt
                Duration delay = calculateDelay(attempt);

                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }

        throw lastException;
    }

    /**
     * Executes the given runnable with retry logic.
     *
     * @param runnable the operation to execute
     * @throws Exception if all retry attempts fail
     */
    public void execute(Runnable runnable) throws Exception {
        execute(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Determines if an exception should trigger a retry.
     *
     * @param exception the exception to check
     * @return true if the operation should be retried
     */
    private boolean shouldRetry(Throwable exception) {
        // Check if exception is a retryable cache exception
        if (exception instanceof CacheException) {
            CacheException ce = (CacheException) exception;
            if (!ce.isRetryable()) {
                return false;
            }
        }

        return retryPredicate.test(exception);
    }

    /**
     * Calculates the delay for the next retry attempt.
     *
     * @param attempt the current attempt number (1-based)
     * @return the delay duration
     */
    private Duration calculateDelay(int attempt) {
        if (delayFunction != null) {
            return delayFunction.apply(attempt);
        }

        // Exponential backoff with jitter
        long baseDelay = initialDelay.toMillis();
        long exponentialDelay = (long) (baseDelay * Math.pow(backoffMultiplier, attempt - 1));

        // Apply maximum delay limit
        long clampedDelay = Math.min(exponentialDelay, maxDelay.toMillis());

        // Apply jitter to avoid thundering herd
        if (jitterFactor > 0) {
            // Use configurable random supplier for jitter calculation
            double randomValue = randomSupplier.get();
            double jitter = (randomValue * 2 - 1) * jitterFactor; // Convert [0,1] to [-jitterFactor, jitterFactor]
            clampedDelay = (long) (clampedDelay * (1 + jitter));
        }

        return Duration.ofMillis(Math.max(0, clampedDelay));
    }

    /**
     * Builder class for creating retry policies.
     */
    public static class Builder {
        private int maxAttempts = 3;
        private Duration initialDelay = Duration.ofMillis(100);
        private Duration maxDelay = Duration.ofSeconds(30);
        private double backoffMultiplier = 2.0;
        private double jitterFactor = 0.1;
        private Predicate<Throwable> retryPredicate = throwable -> true;
        private Function<Integer, Duration> delayFunction = null;
        private Supplier<Double> randomSupplier = () -> ThreadLocalRandom.current().nextDouble();

        /**
         * Sets the maximum number of retry attempts.
         *
         * @param maxAttempts the maximum attempts (must be greater than 0)
         * @return this builder
         */
        public Builder maxAttempts(int maxAttempts) {
            if (maxAttempts <= 0) {
                throw new IllegalArgumentException("Max attempts must be positive");
            }
            this.maxAttempts = maxAttempts;
            return this;
        }

        /**
         * Sets the initial delay between retries.
         *
         * @param initialDelay the initial delay
         * @return this builder
         */
        public Builder initialDelay(Duration initialDelay) {
            if (initialDelay.isNegative()) {
                throw new IllegalArgumentException("Initial delay cannot be negative");
            }
            this.initialDelay = initialDelay;
            return this;
        }

        /**
         * Sets the maximum delay between retries.
         *
         * @param maxDelay the maximum delay
         * @return this builder
         */
        public Builder maxDelay(Duration maxDelay) {
            if (maxDelay.isNegative()) {
                throw new IllegalArgumentException("Max delay cannot be negative");
            }
            this.maxDelay = maxDelay;
            return this;
        }

        /**
         * Sets the backoff multiplier for exponential backoff.
         *
         * @param backoffMultiplier the backoff multiplier (must be greater than or
         *                          equal to 1.0)
         * @return this builder
         */
        public Builder backoffMultiplier(double backoffMultiplier) {
            if (backoffMultiplier < 1.0) {
                throw new IllegalArgumentException("Backoff multiplier must be greater than or equal to 1.0");
            }
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        /**
         * Sets the jitter factor to randomize delays.
         *
         * @param jitterFactor the jitter factor (0.0 to 1.0)
         * @return this builder
         */
        public Builder jitterFactor(double jitterFactor) {
            if (jitterFactor < 0.0 || jitterFactor > 1.0) {
                throw new IllegalArgumentException("Jitter factor must be between 0.0 and 1.0");
            }
            this.jitterFactor = jitterFactor;
            return this;
        }

        /**
         * Sets a predicate to determine which exceptions should trigger retries.
         *
         * @param retryPredicate the retry predicate
         * @return this builder
         */
        public Builder retryOn(Predicate<Throwable> retryPredicate) {
            this.retryPredicate = retryPredicate;
            return this;
        }

        /**
         * Configures retry only for specific exception types.
         *
         * @param exceptionTypes the exception types to retry on
         * @return this builder
         */
        @SafeVarargs
        public final Builder retryOnException(Class<? extends Throwable>... exceptionTypes) {
            this.retryPredicate = throwable -> {
                for (Class<? extends Throwable> type : exceptionTypes) {
                    if (type.isAssignableFrom(throwable.getClass())) {
                        return true;
                    }
                }
                return false;
            };
            return this;
        }

        /**
         * Sets a custom delay function.
         *
         * @param delayFunction function that takes attempt number and returns delay
         * @return this builder
         */
        public Builder delayFunction(Function<Integer, Duration> delayFunction) {
            this.delayFunction = delayFunction;
            return this;
        }

        /**
         * Configures the retry policy to use SecureRandom for jitter calculation.
         * <p>
         * This is useful in security-sensitive environments where predictable
         * random numbers could be a concern. Note that SecureRandom is slower
         * than ThreadLocalRandom, so only use this when security is a priority.
         * </p>
         *
         * @param useSecureRandom true to use SecureRandom, false to use
         *                        ThreadLocalRandom (default)
         * @return this builder
         */
        public Builder secureRandom(boolean useSecureRandom) {
            if (useSecureRandom) {
                SecureRandom secureRandom = new SecureRandom();
                this.randomSupplier = secureRandom::nextDouble;
            } else {
                this.randomSupplier = () -> ThreadLocalRandom.current().nextDouble();
            }
            return this;
        }

        /**
         * Sets a custom random number supplier for jitter calculation.
         * <p>
         * This allows for complete control over the random number generation
         * used in jitter calculations. The supplier should return values
         * between 0.0 (inclusive) and 1.0 (exclusive).
         * </p>
         *
         * @param randomSupplier the random number supplier
         * @return this builder
         */
        public Builder randomSupplier(Supplier<Double> randomSupplier) {
            this.randomSupplier = randomSupplier;
            return this;
        }

        /**
         * Builds the retry policy.
         *
         * @return a new retry policy instance
         */
        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }
}
