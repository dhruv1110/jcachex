package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheStats;
import io.github.dhruv1110.jcachex.internal.util.StatisticsProvider;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for StatisticsProvider utility methods.
 */
class StatisticsProviderTest {

    @Test
    void testCreateBasicStats() {
        // Given
        AtomicLong hitCount = new AtomicLong(100);
        AtomicLong missCount = new AtomicLong(50);

        // When
        CacheStats stats = StatisticsProvider.createBasicStats(hitCount, missCount);

        // Then
        assertNotNull(stats);
        assertEquals(100L, stats.getHitCount().get());
        assertEquals(50L, stats.getMissCount().get());
    }

    @Test
    void testCreateComprehensiveStats() {
        // Given
        AtomicLong hitCount = new AtomicLong(100);
        AtomicLong missCount = new AtomicLong(50);
        AtomicLong loadCount = new AtomicLong(60);
        AtomicLong loadTime = new AtomicLong(1000000L); // 1ms in nanoseconds
        AtomicLong evictionCount = new AtomicLong(10);

        // When
        CacheStats stats = StatisticsProvider.createComprehensiveStats(
                hitCount, missCount, loadCount, loadTime, evictionCount);

        // Then
        assertNotNull(stats);
        assertEquals(100L, stats.getHitCount().get());
        assertEquals(50L, stats.getMissCount().get());
        assertEquals(60L, stats.getLoadCount().get());
        assertEquals(1000000L, stats.getTotalLoadTime().get());
        assertEquals(10L, stats.getEvictionCount().get());
    }

    @Test
    void testCalculateHitRatio() {
        // Test normal case
        double hitRatio = StatisticsProvider.calculateHitRatio(100, 50);
        assertEquals(100.0 / 150.0, hitRatio, 0.001);

        // Test edge cases
        assertEquals(0.0, StatisticsProvider.calculateHitRatio(0, 0));
        assertEquals(1.0, StatisticsProvider.calculateHitRatio(100, 0));
        assertEquals(0.0, StatisticsProvider.calculateHitRatio(0, 100));
    }

    @Test
    void testCalculateMissRatio() {
        // Test normal case
        double missRatio = StatisticsProvider.calculateMissRatio(100, 50);
        // Miss ratio is calculated as missCount / (hitCount + missCount)
        // Miss ratio = 50 / (100 + 50) = 50 / 150 = 0.3333...
        assertEquals(50.0 / 150.0, missRatio, 0.001);

        // Test edge cases
        assertEquals(0.0, StatisticsProvider.calculateMissRatio(0, 0));
        assertEquals(0.0, StatisticsProvider.calculateMissRatio(100, 0));
        assertEquals(1.0, StatisticsProvider.calculateMissRatio(0, 100));
    }

    @Test
    void testCalculateAverageLoadTime() {
        // Test normal case
        double avgLoadTime = StatisticsProvider.calculateAverageLoadTime(1000000L, 10);
        assertEquals(100000.0, avgLoadTime, 0.001);

        // Test edge cases
        assertEquals(0.0, StatisticsProvider.calculateAverageLoadTime(0, 0));
        assertEquals(0.0, StatisticsProvider.calculateAverageLoadTime(1000000L, 0));
    }

    @Test
    void testRecordHit() {
        // Given
        AtomicLong hitCount = new AtomicLong(0);

        // When recording is enabled
        StatisticsProvider.recordHit(hitCount, true);
        assertEquals(1L, hitCount.get());

        // When recording is disabled
        StatisticsProvider.recordHit(hitCount, false);
        assertEquals(1L, hitCount.get()); // Should not change
    }

    @Test
    void testRecordMiss() {
        // Given
        AtomicLong missCount = new AtomicLong(0);

        // When recording is enabled
        StatisticsProvider.recordMiss(missCount, true);
        assertEquals(1L, missCount.get());

        // When recording is disabled
        StatisticsProvider.recordMiss(missCount, false);
        assertEquals(1L, missCount.get()); // Should not change
    }

    @Test
    void testRecordLoad() {
        // Given
        AtomicLong loadCount = new AtomicLong(0);
        AtomicLong loadTime = new AtomicLong(0);

        // When recording is enabled
        StatisticsProvider.recordLoad(loadCount, loadTime, 1000000L, true);
        assertEquals(1L, loadCount.get());
        assertEquals(1000000L, loadTime.get());

        // When recording is disabled
        StatisticsProvider.recordLoad(loadCount, loadTime, 1000000L, false);
        assertEquals(1L, loadCount.get()); // Should not change
        assertEquals(1000000L, loadTime.get()); // Should not change
    }

    @Test
    void testRecordEviction() {
        // Given
        AtomicLong evictionCount = new AtomicLong(0);

        // When recording is enabled
        StatisticsProvider.recordEviction(evictionCount, true);
        assertEquals(1L, evictionCount.get());

        // When recording is disabled
        StatisticsProvider.recordEviction(evictionCount, false);
        assertEquals(1L, evictionCount.get()); // Should not change
    }

    @Test
    void testResetCounters() {
        // Given
        AtomicLong counter1 = new AtomicLong(100);
        AtomicLong counter2 = new AtomicLong(200);
        AtomicLong counter3 = new AtomicLong(300);

        // When
        StatisticsProvider.resetCounters(counter1, counter2, counter3);

        // Then
        assertEquals(0L, counter1.get());
        assertEquals(0L, counter2.get());
        assertEquals(0L, counter3.get());
    }

    @Test
    void testFormatStatsSummary() {
        // Test normal case
        String summary = StatisticsProvider.formatStatsSummary(100, 50, 60, 10);
        assertTrue(summary.contains("requests=150"));
        assertTrue(summary.contains("hits=100"));
        assertTrue(summary.contains("misses=50"));
        assertTrue(summary.contains("loads=60"));
        assertTrue(summary.contains("evictions=10"));
        assertTrue(summary.contains("66.7%")); // Hit ratio

        // Test edge case with zero values
        String zeroSummary = StatisticsProvider.formatStatsSummary(0, 0, 0, 0);
        assertTrue(zeroSummary.contains("requests=0"));
        assertTrue(zeroSummary.contains("0.0%")); // Hit ratio
    }

    @Test
    void testFormatDetailedMetrics() {
        // Test normal case
        String metrics = StatisticsProvider.formatDetailedMetrics(100, 50, 60, 60000000L, 10, 500);
        assertTrue(metrics.contains("size=500"));
        assertTrue(metrics.contains("hits=100"));
        assertTrue(metrics.contains("misses=50"));
        assertTrue(metrics.contains("loads=60"));
        assertTrue(metrics.contains("evictions=10"));
        assertTrue(metrics.contains("hitRatio=0.667")); // Hit ratio
        assertTrue(metrics.contains("avgLoadTime=1.00ms")); // Average load time

        // Test edge case with zero values
        String zeroMetrics = StatisticsProvider.formatDetailedMetrics(0, 0, 0, 0, 0, 0);
        assertTrue(zeroMetrics.contains("size=0"));
        assertTrue(zeroMetrics.contains("hitRatio=0.000"));
        assertTrue(zeroMetrics.contains("avgLoadTime=0.00ms"));
    }

    @Test
    void testThreadSafety() {
        // Test concurrent access to statistics recording
        AtomicLong hitCount = new AtomicLong(0);
        AtomicLong missCount = new AtomicLong(0);
        AtomicLong loadCount = new AtomicLong(0);
        AtomicLong loadTime = new AtomicLong(0);
        AtomicLong evictionCount = new AtomicLong(0);

        // Simulate concurrent access
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    StatisticsProvider.recordHit(hitCount, true);
                    StatisticsProvider.recordMiss(missCount, true);
                    StatisticsProvider.recordLoad(loadCount, loadTime, 1000L, true);
                    StatisticsProvider.recordEviction(evictionCount, true);
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Verify correct counts
        assertEquals(1000L, hitCount.get());
        assertEquals(1000L, missCount.get());
        assertEquals(1000L, loadCount.get());
        assertEquals(1000000L, loadTime.get()); // 1000 * 1000L
        assertEquals(1000L, evictionCount.get());
    }

    @Test
    void testEdgeCasesForStatisticsCalculation() {
        // Test with very large numbers
        double hitRatio = StatisticsProvider.calculateHitRatio(Long.MAX_VALUE / 2, Long.MAX_VALUE / 2);
        assertEquals(0.5, hitRatio, 0.001);

        // Test with zero hits but non-zero misses
        double zeroHitRatio = StatisticsProvider.calculateHitRatio(0, 1000);
        assertEquals(0.0, zeroHitRatio);

        // Test with zero misses but non-zero hits
        double perfectHitRatio = StatisticsProvider.calculateHitRatio(1000, 0);
        assertEquals(1.0, perfectHitRatio);
    }
}
