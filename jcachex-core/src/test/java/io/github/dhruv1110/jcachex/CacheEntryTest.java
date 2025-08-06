package io.github.dhruv1110.jcachex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CacheEntry class.
 */
class CacheEntryTest {

    @Nested
    @DisplayName("CacheEntry Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("CacheEntry constructor with value, weight, and expiration")
        void testConstructorWithAllParameters() {
            String value = "test_value";
            long weight = 42L;
            Instant expiration = Instant.now().plus(1, ChronoUnit.HOURS);

            CacheEntry<String> entry = new CacheEntry<>(value, weight, expiration);

            assertEquals(value, entry.getValue());
            assertEquals(weight, entry.getWeight());
            assertEquals(expiration, entry.getExpirationTime());
            assertEquals(0L, entry.getAccessCount());
            assertNotNull(entry.getLastAccessTime());
            assertNotNull(entry.getCreationTime());
        }

        @Test
        @DisplayName("CacheEntry constructor with null expiration")
        void testConstructorWithNullExpiration() {
            String value = "test_value";
            long weight = 10L;

            CacheEntry<String> entry = new CacheEntry<>(value, weight, null);

            assertEquals(value, entry.getValue());
            assertEquals(weight, entry.getWeight());
            assertNull(entry.getExpirationTime());
            assertEquals(0L, entry.getAccessCount());
            assertNotNull(entry.getLastAccessTime());
            assertNotNull(entry.getCreationTime());
        }

        @Test
        @DisplayName("CacheEntry constructor with null value")
        void testConstructorWithNullValue() {
            long weight = 5L;
            Instant expiration = Instant.now().plus(30, ChronoUnit.MINUTES);

            CacheEntry<String> entry = new CacheEntry<>(null, weight, expiration);

            assertNull(entry.getValue());
            assertEquals(weight, entry.getWeight());
            assertEquals(expiration, entry.getExpirationTime());
            assertEquals(0L, entry.getAccessCount());
            assertNotNull(entry.getLastAccessTime());
            assertNotNull(entry.getCreationTime());
        }

        @Test
        @DisplayName("CacheEntry constructor with zero weight")
        void testConstructorWithZeroWeight() {
            String value = "zero_weight_value";
            long weight = 0L;

            CacheEntry<String> entry = new CacheEntry<>(value, weight, null);

            assertEquals(value, entry.getValue());
            assertEquals(0L, entry.getWeight());
            assertNull(entry.getExpirationTime());
        }

        @Test
        @DisplayName("CacheEntry constructor with negative weight")
        void testConstructorWithNegativeWeight() {
            String value = "negative_weight_value";
            long weight = -10L;

            CacheEntry<String> entry = new CacheEntry<>(value, weight, null);

            assertEquals(value, entry.getValue());
            assertEquals(-10L, entry.getWeight());
        }
    }

    @Nested
    @DisplayName("CacheEntry Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getValue returns correct value")
        void testGetValue() {
            String stringValue = "string_value";
            CacheEntry<String> stringEntry = new CacheEntry<>(stringValue, 1L, null);
            assertEquals(stringValue, stringEntry.getValue());

            Integer intValue = 42;
            CacheEntry<Integer> intEntry = new CacheEntry<>(intValue, 1L, null);
            assertEquals(intValue, intEntry.getValue());

            Object complexValue = new Object();
            CacheEntry<Object> objectEntry = new CacheEntry<>(complexValue, 1L, null);
            assertEquals(complexValue, objectEntry.getValue());
        }

        @Test
        @DisplayName("getWeight returns correct weight")
        void testGetWeight() {
            CacheEntry<String> smallEntry = new CacheEntry<>("small", 1L, null);
            assertEquals(1L, smallEntry.getWeight());

            CacheEntry<String> largeEntry = new CacheEntry<>("large", 1000L, null);
            assertEquals(1000L, largeEntry.getWeight());

            CacheEntry<String> zeroEntry = new CacheEntry<>("zero", 0L, null);
            assertEquals(0L, zeroEntry.getWeight());
        }

        @Test
        @DisplayName("getExpirationTime returns correct expiration")
        void testGetExpirationTime() {
            Instant futureTime = Instant.now().plus(1, ChronoUnit.HOURS);
            CacheEntry<String> expiringEntry = new CacheEntry<>("expiring", 1L, futureTime);
            assertEquals(futureTime, expiringEntry.getExpirationTime());

            CacheEntry<String> nonExpiringEntry = new CacheEntry<>("non_expiring", 1L, null);
            assertNull(nonExpiringEntry.getExpirationTime());
        }

        @Test
        @DisplayName("getAccessCount returns correct count")
        void testGetAccessCount() {
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);

            // Initial access count should be 0
            assertEquals(0L, entry.getAccessCount());
        }

        @Test
        @DisplayName("getLastAccessTime returns valid time")
        void testGetLastAccessTime() {
            Instant beforeCreation = Instant.now().minus(1, ChronoUnit.SECONDS);
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);
            Instant afterCreation = Instant.now().plus(1, ChronoUnit.SECONDS);

            Instant lastAccessTime = entry.getLastAccessTime();
            assertNotNull(lastAccessTime);
            assertTrue(lastAccessTime.isAfter(beforeCreation));
            assertTrue(lastAccessTime.isBefore(afterCreation));
        }

        @Test
        @DisplayName("getCreationTime returns valid time")
        void testGetCreationTime() {
            Instant beforeCreation = Instant.now().minus(1, ChronoUnit.SECONDS);
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);
            Instant afterCreation = Instant.now().plus(1, ChronoUnit.SECONDS);

            Instant creationTime = entry.getCreationTime();
            assertNotNull(creationTime);
            assertTrue(creationTime.isAfter(beforeCreation));
            assertTrue(creationTime.isBefore(afterCreation));
        }

        @Test
        @DisplayName("getCreationTime is stable across calls")
        void testGetCreationTimeStability() throws InterruptedException {
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);

            Instant firstCall = entry.getCreationTime();
            Thread.sleep(10); // Small delay
            Instant secondCall = entry.getCreationTime();

            assertEquals(firstCall, secondCall, "Creation time should not change between calls");
        }
    }

    @Nested
    @DisplayName("CacheEntry Access Count Tests")
    class AccessCountTests {

        @Test
        @DisplayName("incrementAccessCount increments count correctly")
        void testIncrementAccessCount() {
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);

            assertEquals(0L, entry.getAccessCount());

            entry.incrementAccessCount();
            assertEquals(1L, entry.getAccessCount());

            entry.incrementAccessCount();
            assertEquals(2L, entry.getAccessCount());

            entry.incrementAccessCount();
            assertEquals(3L, entry.getAccessCount());
        }

        @Test
        @DisplayName("incrementAccessCount updates last access time")
        void testIncrementAccessCountUpdatesLastAccessTime() throws InterruptedException {
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);

            Instant initialLastAccess = entry.getLastAccessTime();
            Thread.sleep(10); // Small delay to ensure time difference

            entry.incrementAccessCount();
            Instant updatedLastAccess = entry.getLastAccessTime();

            assertTrue(updatedLastAccess.isAfter(initialLastAccess),
                    "Last access time should be updated after incrementing access count");
        }

        @Test
        @DisplayName("incrementAccessCount is thread-safe")
        void testIncrementAccessCountThreadSafety() throws InterruptedException {
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);
            int threadCount = 10;
            int incrementsPerThread = 100;

            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        entry.incrementAccessCount();
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertEquals(threadCount * incrementsPerThread, entry.getAccessCount(),
                    "Access count should be exactly threadCount * incrementsPerThread");
        }
    }

    @Nested
    @DisplayName("CacheEntry Expiration Tests")
    class ExpirationTests {

        @Test
        @DisplayName("isExpired returns false for null expiration")
        void testIsExpiredWithNullExpiration() {
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);
            assertFalse(entry.isExpired(), "Entry with null expiration should never be expired");
        }

        @Test
        @DisplayName("isExpired returns false for future expiration")
        void testIsExpiredWithFutureExpiration() {
            Instant futureTime = Instant.now().plus(1, ChronoUnit.HOURS);
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, futureTime);
            assertFalse(entry.isExpired(), "Entry with future expiration should not be expired");
        }

        @Test
        @DisplayName("isExpired returns true for past expiration")
        void testIsExpiredWithPastExpiration() {
            Instant pastTime = Instant.now().minus(1, ChronoUnit.HOURS);
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, pastTime);
            assertTrue(entry.isExpired(), "Entry with past expiration should be expired");
        }

        @Test
        @DisplayName("isExpired transitions correctly over time")
        void testIsExpiredTransition() throws InterruptedException {
            // Create entry that expires in 50ms
            Instant nearFutureTime = Instant.now().plus(50, ChronoUnit.MILLIS);
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, nearFutureTime);

            assertFalse(entry.isExpired(), "Entry should not be expired initially");

            Thread.sleep(100); // Wait for expiration

            assertTrue(entry.isExpired(), "Entry should be expired after waiting");
        }

        @Test
        @DisplayName("isExpired handles edge case of exact expiration time")
        void testIsExpiredEdgeCase() {
            // This test is inherently racy, but we can try to get close to the edge
            Instant exactTime = Instant.now().plus(1, ChronoUnit.MILLIS);
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, exactTime);

            // Entry might or might not be expired depending on timing
            // The important thing is that the method doesn't throw an exception
            assertDoesNotThrow(() -> entry.isExpired(),
                    "isExpired should not throw exception for edge case timing");
        }
    }

    @Nested
    @DisplayName("CacheEntry Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("CacheEntry with different generic types")
        void testDifferentGenericTypes() {
            CacheEntry<Integer> intEntry = new CacheEntry<>(42, 1L, null);
            assertEquals(Integer.valueOf(42), intEntry.getValue());

            CacheEntry<Boolean> boolEntry = new CacheEntry<>(true, 1L, null);
            assertEquals(Boolean.TRUE, boolEntry.getValue());

            CacheEntry<Double> doubleEntry = new CacheEntry<>(3.14, 1L, null);
            assertEquals(Double.valueOf(3.14), doubleEntry.getValue());
        }

        @Test
        @DisplayName("CacheEntry with large weight values")
        void testLargeWeightValues() {
            long maxWeight = Long.MAX_VALUE;
            CacheEntry<String> entry = new CacheEntry<>("test", maxWeight, null);
            assertEquals(maxWeight, entry.getWeight());

            long minWeight = Long.MIN_VALUE;
            CacheEntry<String> minEntry = new CacheEntry<>("test", minWeight, null);
            assertEquals(minWeight, minEntry.getWeight());
        }

        @Test
        @DisplayName("CacheEntry access count overflow")
        void testAccessCountOverflow() {
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);

            // Test behavior near Long.MAX_VALUE (this would take too long to actually
            // reach)
            // We'll test the mechanism works correctly for large increments
            for (int i = 0; i < 1000; i++) {
                entry.incrementAccessCount();
            }
            assertEquals(1000L, entry.getAccessCount());
        }

        @Test
        @DisplayName("CacheEntry with extreme expiration times")
        void testExtremeExpirationTimes() {
            // Very far in the future
            Instant farFuture = Instant.MAX;
            CacheEntry<String> farFutureEntry = new CacheEntry<>("test", 1L, farFuture);
            assertFalse(farFutureEntry.isExpired());

            // Very far in the past
            Instant farPast = Instant.MIN;
            CacheEntry<String> farPastEntry = new CacheEntry<>("test", 1L, farPast);
            assertTrue(farPastEntry.isExpired());
        }

        @Test
        @DisplayName("CacheEntry with complex objects")
        void testComplexObjects() {
            class ComplexObject {
                private final String name;
                private final int value;

                ComplexObject(String name, int value) {
                    this.name = name;
                    this.value = value;
                }

                @Override
                public boolean equals(Object obj) {
                    if (this == obj)
                        return true;
                    if (obj == null || getClass() != obj.getClass())
                        return false;
                    ComplexObject that = (ComplexObject) obj;
                    return value == that.value && name.equals(that.name);
                }
            }

            ComplexObject complexObj = new ComplexObject("test", 42);
            CacheEntry<ComplexObject> entry = new CacheEntry<>(complexObj, 100L, null);

            assertEquals(complexObj, entry.getValue());
            assertEquals(100L, entry.getWeight());
        }
    }

    @Nested
    @DisplayName("CacheEntry Time Behavior Tests")
    class TimeBehaviorTests {

        @Test
        @DisplayName("Last access time changes with increments")
        void testLastAccessTimeChanges() throws InterruptedException {
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);

            Instant initialLastAccess = entry.getLastAccessTime();
            Thread.sleep(10);

            entry.incrementAccessCount();
            Instant afterFirstIncrement = entry.getLastAccessTime();

            Thread.sleep(10);
            entry.incrementAccessCount();
            Instant afterSecondIncrement = entry.getLastAccessTime();

            assertTrue(afterFirstIncrement.isAfter(initialLastAccess));
            assertTrue(afterSecondIncrement.isAfter(afterFirstIncrement));
        }

        @Test
        @DisplayName("Creation time remains constant")
        void testCreationTimeConstancy() throws InterruptedException {
            CacheEntry<String> entry = new CacheEntry<>("test", 1L, null);

            Instant initialCreationTime = entry.getCreationTime();
            Thread.sleep(10);

            entry.incrementAccessCount();
            Thread.sleep(10);
            entry.incrementAccessCount();

            Instant finalCreationTime = entry.getCreationTime();
            assertEquals(initialCreationTime, finalCreationTime,
                    "Creation time should never change");
        }
    }
}
