package io.github.dpflux.jcachex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

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
            AtomicLong hitCount = new AtomicLong(5);
            AtomicLong missCount = new AtomicLong(3);
            AtomicLong evictionCount = new AtomicLong(2);
            AtomicLong loadCount = new AtomicLong(4);
            AtomicLong loadFailureCount = new AtomicLong(1);
            AtomicLong totalLoadTime = new AtomicLong(100);

            CacheStats customStats = new CacheStats(
                    hitCount, missCount, evictionCount,
                    loadCount, loadFailureCount, totalLoadTime);

            assertEquals(5L, customStats.hitCount());
            assertEquals(3L, customStats.missCount());
            assertEquals(2L, customStats.evictionCount());
            assertEquals(4L, customStats.loadCount());
            assertEquals(1L, customStats.loadFailureCount());
            assertEquals(100L, customStats.totalLoadTime());
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
