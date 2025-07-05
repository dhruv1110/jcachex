package io.github.dhruv1110.jcachex.resilience;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the RetryPolicy class.
 */
class RetryPolicyTest {

    private RetryPolicy retryPolicy;

    @BeforeEach
    void setUp() {
        retryPolicy = RetryPolicy.builder()
                .maxAttempts(3)
                .initialDelay(Duration.ofMillis(10))
                .build();
    }

    @Test
    @DisplayName("Default retry policy")
    void testDefaultRetryPolicy() {
        RetryPolicy defaultPolicy = RetryPolicy.defaultPolicy();

        assertNotNull(defaultPolicy);
        // Default policy should work with basic operations
        AtomicInteger counter = new AtomicInteger(0);
        assertDoesNotThrow(() -> {
            defaultPolicy.execute(() -> {
                counter.incrementAndGet();
                return "success";
            });
        });

        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Successful operation on first attempt")
    void testSuccessfulOperationFirstAttempt() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        String result = retryPolicy.execute(() -> {
            counter.incrementAndGet();
            return "success";
        });

        assertEquals("success", result);
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Successful operation after retries")
    void testSuccessfulOperationAfterRetries() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        String result = retryPolicy.execute(() -> {
            if (counter.incrementAndGet() < 3) {
                throw new RuntimeException("Temporary failure");
            }
            return "success";
        });

        assertEquals("success", result);
        assertEquals(3, counter.get());
    }

    @Test
    @DisplayName("Operation fails after max attempts")
    void testOperationFailsAfterMaxAttempts() {
        AtomicInteger counter = new AtomicInteger(0);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            retryPolicy.execute(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("Persistent failure");
            });
        });

        assertEquals("Persistent failure", exception.getMessage());
        assertEquals(3, counter.get()); // Should attempt 3 times
    }

    @Test
    @DisplayName("Runnable execution with retries")
    void testRunnableExecution() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        retryPolicy.execute(() -> {
            if (counter.incrementAndGet() < 2) {
                throw new RuntimeException("Temporary failure");
            }
        });

        assertEquals(2, counter.get());
    }

    @Test
    @DisplayName("Builder with custom parameters")
    void testBuilderWithCustomParameters() {
        RetryPolicy customPolicy = RetryPolicy.builder()
                .maxAttempts(5)
                .initialDelay(Duration.ofMillis(50))
                .maxDelay(Duration.ofSeconds(10))
                .backoffMultiplier(1.5)
                .jitterFactor(0.2)
                .build();

        assertNotNull(customPolicy);

        AtomicInteger counter = new AtomicInteger(0);
        assertThrows(RuntimeException.class, () -> {
            customPolicy.execute(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("Always fails");
            });
        });

        assertEquals(5, counter.get()); // Should attempt 5 times
    }

    @Test
    @DisplayName("Retry on specific exception types")
    void testRetryOnSpecificExceptions() {
        RetryPolicy specificPolicy = RetryPolicy.builder()
                .maxAttempts(3)
                .retryOnException(IllegalArgumentException.class, IllegalStateException.class)
                .build();

        AtomicInteger counter = new AtomicInteger(0);

        // Should retry on IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            specificPolicy.execute(() -> {
                counter.incrementAndGet();
                throw new IllegalArgumentException("Retryable exception");
            });
        });
        assertEquals(3, counter.get());

        // Should not retry on RuntimeException
        counter.set(0);
        assertThrows(RuntimeException.class, () -> {
            specificPolicy.execute(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("Non-retryable exception");
            });
        });
        assertEquals(1, counter.get()); // Should only attempt once
    }

    @Test
    @DisplayName("Custom retry predicate")
    void testCustomRetryPredicate() {
        RetryPolicy customPredicatePolicy = RetryPolicy.builder()
                .maxAttempts(3)
                .retryOn(throwable -> throwable.getMessage().contains("retry"))
                .build();

        AtomicInteger counter = new AtomicInteger(0);

        // Should retry when message contains "retry"
        assertThrows(RuntimeException.class, () -> {
            customPredicatePolicy.execute(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("Please retry this operation");
            });
        });
        assertEquals(3, counter.get());

        // Should not retry when message doesn't contain "retry"
        counter.set(0);
        assertThrows(RuntimeException.class, () -> {
            customPredicatePolicy.execute(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("Fatal error");
            });
        });
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Custom delay function")
    void testCustomDelayFunction() {
        RetryPolicy customDelayPolicy = RetryPolicy.builder()
                .maxAttempts(3)
                .delayFunction(attempt -> Duration.ofMillis(attempt * 100))
                .build();

        AtomicInteger counter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        assertThrows(RuntimeException.class, () -> {
            customDelayPolicy.execute(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("Always fails");
            });
        });

        long endTime = System.currentTimeMillis();
        assertEquals(3, counter.get());

        // Should have some delay (at least 100ms + 200ms = 300ms for 2 retries)
        assertTrue((endTime - startTime) >= 250);
    }

    @Test
    @DisplayName("Builder validation")
    void testBuilderValidation() {
        // Test invalid max attempts
        assertThrows(IllegalArgumentException.class, () -> {
            RetryPolicy.builder().maxAttempts(0).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            RetryPolicy.builder().maxAttempts(-1).build();
        });

        // Test invalid initial delay
        assertThrows(IllegalArgumentException.class, () -> {
            RetryPolicy.builder().initialDelay(Duration.ofMillis(-1)).build();
        });

        // Test invalid max delay
        assertThrows(IllegalArgumentException.class, () -> {
            RetryPolicy.builder().maxDelay(Duration.ofMillis(-1)).build();
        });

        // Test invalid backoff multiplier
        assertThrows(IllegalArgumentException.class, () -> {
            RetryPolicy.builder().backoffMultiplier(0.5).build();
        });

        // Test invalid jitter factor
        assertThrows(IllegalArgumentException.class, () -> {
            RetryPolicy.builder().jitterFactor(-0.1).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            RetryPolicy.builder().jitterFactor(1.5).build();
        });
    }

    @Test
    @DisplayName("Interrupted execution")
    void testInterruptedExecution() {
        RetryPolicy longDelayPolicy = RetryPolicy.builder()
                .maxAttempts(3)
                .initialDelay(Duration.ofSeconds(5))
                .build();

        Thread.currentThread().interrupt();

        assertThrows(RuntimeException.class, () -> {
            longDelayPolicy.execute(() -> {
                throw new RuntimeException("Test exception");
            });
        });

        // Clear interrupted flag
        Thread.interrupted();
    }

    @Test
    @DisplayName("Zero jitter factor")
    void testZeroJitterFactor() {
        RetryPolicy noJitterPolicy = RetryPolicy.builder()
                .maxAttempts(2)
                .jitterFactor(0.0)
                .build();

        AtomicInteger counter = new AtomicInteger(0);

        assertThrows(RuntimeException.class, () -> {
            noJitterPolicy.execute(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("Test exception");
            });
        });

        assertEquals(2, counter.get());
    }

    @Test
    @DisplayName("Edge case with single attempt")
    void testSingleAttempt() {
        RetryPolicy singleAttemptPolicy = RetryPolicy.builder()
                .maxAttempts(1)
                .build();

        AtomicInteger counter = new AtomicInteger(0);

        assertThrows(RuntimeException.class, () -> {
            singleAttemptPolicy.execute(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("Single failure");
            });
        });

        assertEquals(1, counter.get());
    }
}
