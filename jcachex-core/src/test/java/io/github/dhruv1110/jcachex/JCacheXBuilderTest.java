package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.exceptions.CacheConfigurationException;
import io.github.dhruv1110.jcachex.profiles.ProfileName;
import io.github.dhruv1110.jcachex.profiles.WorkloadCharacteristics;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for JCacheXBuilder with maximum coverage.
 */
@ExtendWith(MockitoExtension.class)
class JCacheXBuilderTest {

    @Mock
    private CacheEventListener<String, String> mockListener;

    @Mock
    private Function<String, String> mockLoader;

    @Mock
    private Function<String, CompletableFuture<String>> mockAsyncLoader;

    @Mock
    private BiFunction<String, String, Long> mockWeigher;

    @Test
    void testFromProfileWithValidProfileName() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY);
        assertNotNull(builder);
        Cache<String, String> cache = builder.name("test-cache").maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testFromProfileWithInvalidProfileName() {
        assertThrows(IllegalArgumentException.class, () -> {
            JCacheXBuilder.fromProfile(ProfileName.valueOf("NON_EXISTENT"));
        });
    }

    @Test
    void testWithSmartDefaults() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.withSmartDefaults();
        assertNotNull(builder);

        WorkloadCharacteristics characteristics = WorkloadCharacteristics.builder()
                .readToWriteRatio(8.0)
                .accessPattern(WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
                .build();

        Cache<String, String> cache = builder
                .workloadCharacteristics(characteristics)
                .maximumSize(100)
                .build();
        assertNotNull(cache);
    }

    @Test
    void testCreate() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testForReadHeavyWorkload() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.forReadHeavyWorkload();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testForWriteHeavyWorkload() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.forWriteHeavyWorkload();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testForMemoryConstrainedEnvironment() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.forMemoryConstrainedEnvironment();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testForHighPerformance() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.forHighPerformance();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testForSessionStorage() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.forSessionStorage();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testForApiResponseCaching() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.forApiResponseCaching();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testForComputationCaching() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.forComputationCaching();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testForMachineLearning() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.forMachineLearning();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testForUltraLowLatency() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.forUltraLowLatency();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testForHardwareOptimization() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.forHardwareOptimization();
        assertNotNull(builder);
        Cache<String, String> cache = builder.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testName() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();
        JCacheXBuilder<String, String> result = builder.name("test-cache");
        assertSame(builder, result);
        Cache<String, String> cache = result.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testWorkloadCharacteristics() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.withSmartDefaults();
        WorkloadCharacteristics characteristics = WorkloadCharacteristics.builder()
                .readToWriteRatio(5.0)
                .accessPattern(WorkloadCharacteristics.AccessPattern.RANDOM)
                .build();

        JCacheXBuilder<String, String> result = builder.workloadCharacteristics(characteristics);
        assertSame(builder, result);
        Cache<String, String> cache = result.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testMaximumSize() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();
        JCacheXBuilder<String, String> result = builder.maximumSize(1000L);
        assertSame(builder, result);
        Cache<String, String> cache = result.build();
        assertNotNull(cache);
    }

    @Test
    void testMaximumWeight() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create().weigher(mockWeigher);
        JCacheXBuilder<String, String> result = builder.maximumWeight(500L);
        assertSame(builder, result);
        Cache<String, String> cache = result.build();
        assertNotNull(cache);
    }

    @Test
    void testExpireAfterWrite() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();
        Duration duration = Duration.ofMinutes(30);
        JCacheXBuilder<String, String> result = builder.expireAfterWrite(duration);
        assertSame(builder, result);
        Cache<String, String> cache = result.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testExpireAfterAccess() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();
        Duration duration = Duration.ofHours(1);
        JCacheXBuilder<String, String> result = builder.expireAfterAccess(duration);
        assertSame(builder, result);
        Cache<String, String> cache = result.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testRefreshAfterWrite() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();
        Duration duration = Duration.ofMinutes(15);
        JCacheXBuilder<String, String> result = builder.refreshAfterWrite(duration);
        assertSame(builder, result);
        Cache<String, String> cache = result.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testRecordStatsWithBoolean() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();
        JCacheXBuilder<String, String> result = builder.recordStats(true);
        assertSame(builder, result);
        Cache<String, String> cache = result.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testRecordStatsWithoutBoolean() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();
        JCacheXBuilder<String, String> result = builder.recordStats();
        assertSame(builder, result);
        Cache<String, String> cache = result.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testListener() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();
        JCacheXBuilder<String, String> result = builder.listener(mockListener);
        assertSame(builder, result);
        Cache<String, String> cache = result.maximumSize(100).build();
        assertNotNull(cache);
    }

    @Test
    void testBuildWithMinimalConfiguration() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();
        Cache<String, String> cache = builder.build();
        assertNotNull(cache);
    }

    @Test
    void testBuildWithFullConfiguration() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>forReadHeavyWorkload()
                .name("test-cache")
                .maximumSize(1000L)
                .maximumWeight(500L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofHours(1))
                .refreshAfterWrite(Duration.ofMinutes(15))
                .recordStats(true);

        assertThrows(CacheConfigurationException.class, builder::build);
    }

    @Test
    void testBuildWithSmartDefaults() {
        WorkloadCharacteristics characteristics = WorkloadCharacteristics.builder()
                .readToWriteRatio(8.0)
                .accessPattern(WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
                .build();

        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>withSmartDefaults()
                .workloadCharacteristics(characteristics)
                .maximumSize(1000L)
                .name("smart-cache");

        Cache<String, String> cache = builder.build();
        assertNotNull(cache);
    }

    @Test
    void testBuildWithProfileAndOverrides() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>forHighPerformance()
                .maximumSize(500L)
                .recordStats(true)
                .name("high-perf-cache");

        Cache<String, String> cache = builder.build();
        assertNotNull(cache);
    }

    @Test
    void testMethodChaining() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .name("chained-cache")
                .maximumSize(100L)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats();

        Cache<String, String> cache = builder.build();
        assertNotNull(cache);
    }

    @Test
    void testComplexMethodChaining() {
        WorkloadCharacteristics characteristics = WorkloadCharacteristics.builder()
                .readToWriteRatio(6.0)
                .accessPattern(WorkloadCharacteristics.AccessPattern.SPATIAL_LOCALITY)
                .build();

        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>withSmartDefaults()
                .workloadCharacteristics(characteristics)
                .name("complex-cache")
                .maximumSize(2000L)
                .maximumWeight(1000L)
                .expireAfterWrite(Duration.ofHours(2))
                .expireAfterAccess(Duration.ofHours(1))
                .refreshAfterWrite(Duration.ofMinutes(30))
                .recordStats(true);

        assertThrows(CacheConfigurationException.class, builder::build);
    }

    @Test
    void testBuildWithNullName() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .name(null)
                .maximumSize(100L);

        Cache<String, String> cache = builder.build();
        assertNotNull(cache);
    }

    @Test
    void testBuildWithZeroMaximumSize() {
        assertThrows(CacheConfigurationException.class, () -> {
            JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                    .maximumSize(0L);

            builder.build();
        });
    }

    @Test
    void testBuildWithNegativeMaximumSize() {
        assertThrows(CacheConfigurationException.class, () -> {
            JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                    .maximumSize(-1L);

            builder.build();
        });
    }

    @Test
    void testBuildWithZeroMaximumWeight() {
        assertThrows(CacheConfigurationException.class, () -> {
            JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                    .maximumWeight(0L);

            builder.build();
        });
    }

    @Test
    void testBuildWithNegativeMaximumWeight() {
        assertThrows(CacheConfigurationException.class, () -> {
            JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                    .maximumWeight(-1L);

            builder.build();
        });
    }

    @Test
    void testBuildWithZeroDuration() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .expireAfterWrite(Duration.ZERO)
                .expireAfterAccess(Duration.ZERO)
                .refreshAfterWrite(Duration.ZERO)
                .maximumSize(100L);

        Cache<String, String> cache = builder.build();
        assertNotNull(cache);
    }

    @Test
    void testBuildWithNegativeDuration() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .expireAfterWrite(Duration.ofSeconds(-1))
                .maximumSize(100L);

        assertThrows(CacheConfigurationException.class, builder::build);
    }

    @Test
    void testGetConfigurationSummary() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>forReadHeavyWorkload()
                .name("test-cache");

        String summary = builder.getConfigurationSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("JCacheXBuilder Configuration"));
        assertTrue(summary.contains("test-cache"));
        assertTrue(summary.contains("READ_HEAVY"));
    }

    @Test
    void testGetConfigurationSummaryWithSmartDefaults() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>withSmartDefaults()
                .name("smart-cache");

        String summary = builder.getConfigurationSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("JCacheXBuilder Configuration"));
        assertTrue(summary.contains("smart-cache"));
        assertTrue(summary.contains("Smart Defaults: true"));
    }

    @Test
    void testGetConfigurationSummaryWithoutName() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.create();

        String summary = builder.getConfigurationSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("JCacheXBuilder Configuration"));
        assertTrue(summary.contains("Cache Name: Not set"));
    }

    @Test
    void testCacheOperationsAfterBuild() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>forReadHeavyWorkload()
                .name("test-cache")
                .maximumSize(100L);

        Cache<String, String> cache = builder.build();

        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));

        cache.put("key2", "value2");
        assertEquals("value2", cache.get("key2"));

        cache.remove("key1");
        assertNull(cache.get("key1"));
    }

    @Test
    void testCacheWithLoader() {
        when(mockLoader.apply("missing-key")).thenReturn("loaded-value");

        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .maximumSize(100L)
                .loader(mockLoader);

        Cache<String, String> cache = builder.build();

        String value = cache.get("missing-key");
        assertEquals("loaded-value", value);
        verify(mockLoader).apply("missing-key");
    }

    @Test
    void testCacheWithAsyncLoader() {
        when(mockAsyncLoader.apply("async-key")).thenReturn(CompletableFuture.completedFuture("async-value"));

        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .maximumSize(100L)
                .asyncLoader(mockAsyncLoader);

        Cache<String, String> cache = builder.build();

        CompletableFuture<String> future = cache.getAsync("async-key");
        assertNotNull(future);

        String value = future.join();
        assertEquals("async-value", value);
        verify(mockAsyncLoader).apply("async-key");
    }

    @Test
    @Disabled
    void testCacheWithWeigher() {
        when(mockWeigher.apply("key1", "value1")).thenReturn(10L);
        when(mockWeigher.apply("key2", "value2")).thenReturn(20L);

        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .maximumWeight(25L)
                .weigher(mockWeigher);

        Cache<String, String> cache = builder.build();

        cache.put("key1", "value1");
        cache.put("key2", "value2");

        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));

        verify(mockWeigher).apply("key1", "value1");
        verify(mockWeigher).apply("key2", "value2");
    }

    @Test
    void testCacheWithEventListener() {
        AtomicInteger eventCount = new AtomicInteger(0);
        CacheEventListener<String, String> listener = new CacheEventListener<String, String>() {
            @Override
            public void onPut(String key, String value) {
                eventCount.incrementAndGet();
            }

            @Override
            public void onRemove(String key, String value) {
                eventCount.incrementAndGet();
            }

            @Override
            public void onEvict(String key, String value, EvictionReason reason) {
                eventCount.incrementAndGet();
            }

            @Override
            public void onExpire(String key, String value) {
                eventCount.incrementAndGet();
            }

            @Override
            public void onLoad(String key, String value) {
                eventCount.incrementAndGet();
            }

            @Override
            public void onLoadError(String key, Throwable error) {
                eventCount.incrementAndGet();
            }

            @Override
            public void onClear() {
                eventCount.incrementAndGet();
            }
        };

        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .maximumSize(100L)
                .listener(listener);

        Cache<String, String> cache = builder.build();

        cache.put("key1", "value1");
        assertEquals(1, eventCount.get());

        cache.get("key1");
        assertEquals(1, eventCount.get());

        cache.remove("key1");
        assertEquals(2, eventCount.get());

        cache.clear();
        assertEquals(3, eventCount.get());
    }

    @Test
    void testBuildWithInvalidProfile() {
        assertThrows(IllegalArgumentException.class, () -> {
            JCacheXBuilder.fromProfile(ProfileName.valueOf("INVALID_PROFILE"));
        });
    }

    @Test
    void testBuildWithNullLoader() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .maximumSize(100L)
                .loader(null);

        Cache<String, String> cache = builder.build();
        assertNotNull(cache);
    }

    @Test
    void testBuildWithNullAsyncLoader() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .maximumSize(100L)
                .asyncLoader(null);

        Cache<String, String> cache = builder.build();
        assertNotNull(cache);
    }

    @Test
    void testBuildWithNullWeigher() {
        assertThrows(CacheConfigurationException.class, () -> {
            JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .maximumWeight(100L)
                .weigher(null);

            builder.build();
        });
    }

    @Test
    void testBuildWithNullListener() {
        JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                .maximumSize(100L)
                .listener(null);

        Cache<String, String> cache = builder.build();
        assertNotNull(cache);
    }

    @Test
    void testBuildPerformance() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>create()
                    .maximumSize(100L)
                    .name("perf-test-" + i);

            Cache<String, String> cache = builder.build();
            assertNotNull(cache);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 5000, "Build performance test took too long: " + duration + "ms");
    }

    @Test
    void testMethodChainingPerformance() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            JCacheXBuilder<String, String> builder = JCacheXBuilder.<String, String>forReadHeavyWorkload()
                    .name("chain-test-" + i)
                    .maximumSize(1000L)
                    .maximumWeight(500L)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .expireAfterAccess(Duration.ofHours(1))
                    .refreshAfterWrite(Duration.ofMinutes(15))
                    .recordStats(true);

            assertThrows(CacheConfigurationException.class, builder::build);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 2000, "Method chaining performance test took too long: " + duration + "ms");
    }
}
