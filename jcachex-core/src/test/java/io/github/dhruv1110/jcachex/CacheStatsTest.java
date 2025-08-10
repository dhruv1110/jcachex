package io.github.dhruv1110.jcachex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class CacheStatsTest {
    private CacheStats stats;

    @BeforeEach
    void setUp() {
        stats = new CacheStats();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        @Test
        @DisplayName("Default constructor should initialize all counters to zero")
        void defaultConstructorShouldInitializeCountersToZero() {
            assertEquals(0L, stats.hitCount());
            assertEquals(0L, stats.missCount());
            assertEquals(0L, stats.evictionCount());
            assertEquals(0L, stats.loadCount());
            assertEquals(0L, stats.loadFailureCount());
            assertEquals(0L, stats.totalLoadTime());
        }

        @Test
        @DisplayName("Custom constructor should initialize with provided values")
        void customConstructorShouldInitializeWithProvidedValues() {
            AtomicLong hitCount = new AtomicLong(10);
            AtomicLong missCount = new AtomicLong(5);
            AtomicLong evictionCount = new AtomicLong(2);
            AtomicLong loadCount = new AtomicLong(8);
            AtomicLong loadFailureCount = new AtomicLong(1);
            AtomicLong totalLoadTime = new AtomicLong(1000);

            CacheStats customStats = new CacheStats(hitCount, missCount, evictionCount,
                    loadCount, loadFailureCount, totalLoadTime);

            assertEquals(10L, customStats.hitCount());
            assertEquals(5L, customStats.missCount());
            assertEquals(2L, customStats.evictionCount());
            assertEquals(8L, customStats.loadCount());
            assertEquals(1L, customStats.loadFailureCount());
            assertEquals(1000L, customStats.totalLoadTime());
        }
    }

    @Nested
    @DisplayName("AtomicLong Getter Tests")
    class AtomicLongGetterTests {

        @Test
        @DisplayName("getHitCount should return AtomicLong with correct value")
        void getHitCountShouldReturnAtomicLongWithCorrectValue() {
            AtomicLong hitCount = stats.getHitCount();
            assertNotNull(hitCount);
            assertEquals(0L, hitCount.get());

            stats.recordHit();
            assertEquals(1L, hitCount.get());

            stats.recordHit();
            assertEquals(2L, hitCount.get());
        }

        @Test
        @DisplayName("getMissCount should return AtomicLong with correct value")
        void getMissCountShouldReturnAtomicLongWithCorrectValue() {
            AtomicLong missCount = stats.getMissCount();
            assertNotNull(missCount);
            assertEquals(0L, missCount.get());

            stats.recordMiss();
            assertEquals(1L, missCount.get());

            stats.recordMiss();
            assertEquals(2L, missCount.get());
        }

        @Test
        @DisplayName("getEvictionCount should return AtomicLong with correct value")
        void getEvictionCountShouldReturnAtomicLongWithCorrectValue() {
            AtomicLong evictionCount = stats.getEvictionCount();
            assertNotNull(evictionCount);
            assertEquals(0L, evictionCount.get());

            stats.recordEviction();
            assertEquals(1L, evictionCount.get());

            stats.recordEviction();
            assertEquals(2L, evictionCount.get());
        }

        @Test
        @DisplayName("getLoadCount should return AtomicLong with correct value")
        void getLoadCountShouldReturnAtomicLongWithCorrectValue() {
            AtomicLong loadCount = stats.getLoadCount();
            assertNotNull(loadCount);
            assertEquals(0L, loadCount.get());

            stats.recordLoad(100);
            assertEquals(1L, loadCount.get());

            stats.recordLoad(200);
            assertEquals(2L, loadCount.get());
        }

        @Test
        @DisplayName("getLoadFailureCount should return AtomicLong with correct value")
        void getLoadFailureCountShouldReturnAtomicLongWithCorrectValue() {
            AtomicLong loadFailureCount = stats.getLoadFailureCount();
            assertNotNull(loadFailureCount);
            assertEquals(0L, loadFailureCount.get());

            stats.recordLoadFailure();
            assertEquals(1L, loadFailureCount.get());

            stats.recordLoadFailure();
            assertEquals(2L, loadFailureCount.get());
        }

        @Test
        @DisplayName("getTotalLoadTime should return AtomicLong with correct value")
        void getTotalLoadTimeShouldReturnAtomicLongWithCorrectValue() {
            AtomicLong totalLoadTime = stats.getTotalLoadTime();
            assertNotNull(totalLoadTime);
            assertEquals(0L, totalLoadTime.get());

            stats.recordLoad(100);
            assertEquals(100L, totalLoadTime.get());

            stats.recordLoad(200);
            assertEquals(300L, totalLoadTime.get());
        }

        @Test
        @DisplayName("AtomicLong getters should return same instance across calls")
        void atomicLongGettersShouldReturnSameInstanceAcrossCalls() {
            AtomicLong hitCount1 = stats.getHitCount();
            AtomicLong hitCount2 = stats.getHitCount();
            assertSame(hitCount1, hitCount2, "getHitCount should return same AtomicLong instance");

            AtomicLong missCount1 = stats.getMissCount();
            AtomicLong missCount2 = stats.getMissCount();
            assertSame(missCount1, missCount2, "getMissCount should return same AtomicLong instance");

            AtomicLong evictionCount1 = stats.getEvictionCount();
            AtomicLong evictionCount2 = stats.getEvictionCount();
            assertSame(evictionCount1, evictionCount2, "getEvictionCount should return same AtomicLong instance");

            AtomicLong loadCount1 = stats.getLoadCount();
            AtomicLong loadCount2 = stats.getLoadCount();
            assertSame(loadCount1, loadCount2, "getLoadCount should return same AtomicLong instance");

            AtomicLong loadFailureCount1 = stats.getLoadFailureCount();
            AtomicLong loadFailureCount2 = stats.getLoadFailureCount();
            assertSame(loadFailureCount1, loadFailureCount2,
                    "getLoadFailureCount should return same AtomicLong instance");

            AtomicLong totalLoadTime1 = stats.getTotalLoadTime();
            AtomicLong totalLoadTime2 = stats.getTotalLoadTime();
            assertSame(totalLoadTime1, totalLoadTime2, "getTotalLoadTime should return same AtomicLong instance");
        }

        @Test
        @DisplayName("AtomicLong getters should reflect changes made directly to returned objects")
        void atomicLongGettersShouldReflectDirectChanges() {
            AtomicLong hitCount = stats.getHitCount();
            hitCount.set(42);
            assertEquals(42L, stats.hitCount());

            AtomicLong missCount = stats.getMissCount();
            missCount.incrementAndGet();
            assertEquals(1L, stats.missCount());

            AtomicLong evictionCount = stats.getEvictionCount();
            evictionCount.addAndGet(5);
            assertEquals(5L, stats.evictionCount());

            AtomicLong loadCount = stats.getLoadCount();
            loadCount.set(10);
            assertEquals(10L, stats.loadCount());

            AtomicLong loadFailureCount = stats.getLoadFailureCount();
            loadFailureCount.incrementAndGet();
            assertEquals(1L, stats.loadFailureCount());

            AtomicLong totalLoadTime = stats.getTotalLoadTime();
            totalLoadTime.set(1000);
            assertEquals(1000L, stats.totalLoadTime());
        }
    }

    @Nested
    @DisplayName("Counter Recording Tests")
    class CounterRecordingTests {
        @Test
        @DisplayName("recordHit should increment hit count")
        void recordHitShouldIncrementHitCount() {
            stats.recordHit();
            assertEquals(1L, stats.hitCount());

            stats.recordHit();
            assertEquals(2L, stats.hitCount());
        }

        @Test
        @DisplayName("recordMiss should increment miss count")
        void recordMissShouldIncrementMissCount() {
            stats.recordMiss();
            assertEquals(1L, stats.missCount());

            stats.recordMiss();
            assertEquals(2L, stats.missCount());
        }

        @Test
        @DisplayName("recordEviction should increment eviction count")
        void recordEvictionShouldIncrementEvictionCount() {
            stats.recordEviction();
            assertEquals(1L, stats.evictionCount());

            stats.recordEviction();
            assertEquals(2L, stats.evictionCount());
        }

        @Test
        @DisplayName("recordLoad should increment load count and add to total load time")
        void recordLoadShouldIncrementLoadCountAndAddToTotalLoadTime() {
            stats.recordLoad(100);
            assertEquals(1L, stats.loadCount());
            assertEquals(100L, stats.totalLoadTime());

            stats.recordLoad(200);
            assertEquals(2L, stats.loadCount());
            assertEquals(300L, stats.totalLoadTime());
        }

        @Test
        @DisplayName("recordLoadFailure should increment load failure count")
        void recordLoadFailureShouldIncrementLoadFailureCount() {
            stats.recordLoadFailure();
            assertEquals(1L, stats.loadFailureCount());

            stats.recordLoadFailure();
            assertEquals(2L, stats.loadFailureCount());
        }
    }

    @Nested
    @DisplayName("Rate Calculation Tests")
    class RateCalculationTests {
        @Test
        @DisplayName("hitRate should return 0.0 when no hits or misses")
        void hitRateShouldReturnZeroWhenNoHitsOrMisses() {
            assertEquals(0.0, stats.hitRate());
        }

        @Test
        @DisplayName("hitRate should calculate correct rate with hits and misses")
        void hitRateShouldCalculateCorrectRate() {
            stats.recordHit();
            stats.recordHit();
            stats.recordMiss();

            assertEquals(2.0 / 3.0, stats.hitRate());
        }

        @Test
        @DisplayName("missRate should return 0.0 when no hits or misses")
        void missRateShouldReturnZeroWhenNoHitsOrMisses() {
            assertEquals(0.0, stats.missRate());
        }

        @Test
        @DisplayName("missRate should calculate correct rate with hits and misses")
        void missRateShouldCalculateCorrectRate() {
            stats.recordHit();
            stats.recordMiss();
            stats.recordMiss();

            assertEquals(2.0 / 3.0, stats.missRate());
        }

        @Test
        @DisplayName("averageLoadTime should return 0.0 when no loads")
        void averageLoadTimeShouldReturnZeroWhenNoLoads() {
            assertEquals(0.0, stats.averageLoadTime());
        }

        @Test
        @DisplayName("averageLoadTime should calculate correct average")
        void averageLoadTimeShouldCalculateCorrectAverage() {
            stats.recordLoad(100);
            stats.recordLoad(200);
            stats.recordLoad(300);

            assertEquals(200.0, stats.averageLoadTime());
        }
    }

    @Nested
    @DisplayName("Snapshot and Reset Tests")
    class SnapshotAndResetTests {
        @Test
        @DisplayName("snapshot should create independent copy of stats")
        void snapshotShouldCreateIndependentCopy() {
            stats.recordHit();
            stats.recordMiss();
            stats.recordEviction();
            stats.recordLoad(100);
            stats.recordLoadFailure();

            CacheStats snapshot = stats.snapshot();

            // Modify original stats
            stats.recordHit();
            stats.recordMiss();

            // Snapshot should remain unchanged
            assertEquals(1L, snapshot.hitCount());
            assertEquals(1L, snapshot.missCount());
            assertEquals(1L, snapshot.evictionCount());
            assertEquals(1L, snapshot.loadCount());
            assertEquals(1L, snapshot.loadFailureCount());
            assertEquals(100L, snapshot.totalLoadTime());
        }

        @Test
        @DisplayName("reset should clear all counters")
        void resetShouldClearAllCounters() {
            stats.recordHit();
            stats.recordMiss();
            stats.recordEviction();
            stats.recordLoad(100);
            stats.recordLoadFailure();

            CacheStats resetStats = stats.reset();

            assertEquals(0L, resetStats.hitCount());
            assertEquals(0L, resetStats.missCount());
            assertEquals(0L, resetStats.evictionCount());
            assertEquals(0L, resetStats.loadCount());
            assertEquals(0L, resetStats.loadFailureCount());
            assertEquals(0L, resetStats.totalLoadTime());

            // Original stats should also be reset
            assertEquals(0L, stats.hitCount());
            assertEquals(0L, stats.missCount());
            assertEquals(0L, stats.evictionCount());
            assertEquals(0L, stats.loadCount());
            assertEquals(0L, stats.loadFailureCount());
            assertEquals(0L, stats.totalLoadTime());
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {
        @Test
        @DisplayName("empty should create new instance with zero counters")
        void emptyShouldCreateNewInstanceWithZeroCounters() {
            CacheStats emptyStats = CacheStats.empty();

            assertEquals(0L, emptyStats.hitCount());
            assertEquals(0L, emptyStats.missCount());
            assertEquals(0L, emptyStats.evictionCount());
            assertEquals(0L, emptyStats.loadCount());
            assertEquals(0L, emptyStats.loadFailureCount());
            assertEquals(0L, emptyStats.totalLoadTime());
        }
    }

    @Nested
    @DisplayName("Object Method Tests")
    class ObjectMethodTests {
        @Test
        @DisplayName("equals should correctly compare stats")
        void equalsShouldCorrectlyCompareStats() {
            CacheStats stats1 = new CacheStats();
            CacheStats stats2 = new CacheStats();

            assertTrue(stats1.equals(stats2));

            stats1.recordHit();
            assertFalse(stats1.equals(stats2));

            stats2.recordHit();
            assertTrue(stats1.equals(stats2));

            assertFalse(stats1.equals(null));
            assertFalse(stats1.equals(new Object()));
        }

        @Test
        @DisplayName("hashCode should be consistent with equals")
        void hashCodeShouldBeConsistentWithEquals() {
            CacheStats stats1 = new CacheStats();
            CacheStats stats2 = new CacheStats();

            assertEquals(stats1.hashCode(), stats2.hashCode());

            stats1.recordHit();
            assertNotEquals(stats1.hashCode(), stats2.hashCode());

            stats2.recordHit();
            assertEquals(stats1.hashCode(), stats2.hashCode());
        }

        @Test
        @DisplayName("toString should include all counters")
        void toStringShouldIncludeAllCounters() {
            stats.recordHit();
            stats.recordMiss();
            stats.recordEviction();
            stats.recordLoad(100);
            stats.recordLoadFailure();

            String str = stats.toString();

            assertTrue(str.contains("hitCount=1"));
            assertTrue(str.contains("missCount=1"));
            assertTrue(str.contains("evictionCount=1"));
            assertTrue(str.contains("loadCount=1"));
            assertTrue(str.contains("loadFailureCount=1"));
            assertTrue(str.contains("totalLoadTime=100"));
        }
    }
}
