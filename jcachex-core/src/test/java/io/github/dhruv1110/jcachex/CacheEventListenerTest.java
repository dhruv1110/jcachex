package io.github.dhruv1110.jcachex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CacheEventListener interface.
 */
class CacheEventListenerTest {

    @Nested
    @DisplayName("NoOp Listener Tests")
    class NoOpListenerTests {

        @Test
        @DisplayName("NoOp listener factory method creates working listener")
        void testNoOpListenerCreation() {
            CacheEventListener<String, String> noOpListener = CacheEventListener.noOp();

            assertNotNull(noOpListener, "NoOp listener should not be null");

            // Verify all methods can be called without exceptions
            assertDoesNotThrow(() -> {
                noOpListener.onPut("key", "value");
                noOpListener.onRemove("key", "value");
                noOpListener.onEvict("key", "value", EvictionReason.SIZE);
                noOpListener.onExpire("key", "value");
                noOpListener.onLoad("key", "value");
                noOpListener.onLoadError("key", new RuntimeException("test"));
                noOpListener.onClear();
            }, "NoOp listener methods should not throw exceptions");
        }

        @Test
        @DisplayName("NoOp listener with different generic types")
        void testNoOpListenerWithDifferentTypes() {
            CacheEventListener<Integer, String> intStringListener = CacheEventListener.noOp();
            CacheEventListener<String, Integer> stringIntListener = CacheEventListener.noOp();

            assertNotNull(intStringListener);
            assertNotNull(stringIntListener);

            // Test with different types
            assertDoesNotThrow(() -> {
                intStringListener.onPut(1, "value");
                intStringListener.onRemove(2, "value2");
                intStringListener.onEvict(3, "value3", EvictionReason.EXPIRED);
                intStringListener.onExpire(4, "value4");
                intStringListener.onLoad(5, "value5");
                intStringListener.onLoadError(6, new RuntimeException("test"));
                intStringListener.onClear();
            });

            assertDoesNotThrow(() -> {
                stringIntListener.onPut("key", 1);
                stringIntListener.onRemove("key2", 2);
                stringIntListener.onEvict("key3", 3, EvictionReason.WEIGHT);
                stringIntListener.onExpire("key4", 4);
                stringIntListener.onLoad("key5", 5);
                stringIntListener.onLoadError("key6", new RuntimeException("test"));
                stringIntListener.onClear();
            });
        }

        @Test
        @DisplayName("NoOp listener with null parameters")
        void testNoOpListenerWithNullParameters() {
            CacheEventListener<String, String> noOpListener = CacheEventListener.noOp();

            // Should handle null parameters gracefully
            assertDoesNotThrow(() -> {
                noOpListener.onPut(null, null);
                noOpListener.onRemove(null, null);
                noOpListener.onEvict(null, null, null);
                noOpListener.onExpire(null, null);
                noOpListener.onLoad(null, null);
                noOpListener.onLoadError(null, null);
                noOpListener.onClear();
            }, "NoOp listener should handle null parameters gracefully");
        }

        @Test
        @DisplayName("NoOp listener with all eviction reasons")
        void testNoOpListenerWithAllEvictionReasons() {
            CacheEventListener<String, String> noOpListener = CacheEventListener.noOp();

            // Test with all possible eviction reasons
            for (EvictionReason reason : EvictionReason.values()) {
                assertDoesNotThrow(() -> {
                    noOpListener.onEvict("key", "value", reason);
                }, "NoOp listener should handle eviction reason: " + reason);
            }
        }

        @Test
        @DisplayName("Multiple NoOp listeners are independent")
        void testMultipleNoOpListeners() {
            CacheEventListener<String, String> listener1 = CacheEventListener.noOp();
            CacheEventListener<String, String> listener2 = CacheEventListener.noOp();

            assertNotSame(listener1, listener2, "Each noOp() call should return a new instance");

            // Both should work independently
            assertDoesNotThrow(() -> {
                listener1.onPut("key1", "value1");
                listener2.onPut("key2", "value2");
            });
        }
    }

    @Nested
    @DisplayName("Listener Integration Tests")
    class ListenerIntegrationTests {

        @Test
        @DisplayName("NoOp listener works with cache configuration")
        void testNoOpListenerWithCacheConfiguration() {
            CacheEventListener<String, String> noOpListener = CacheEventListener.noOp();

            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize(10L)
                    .addListener(noOpListener)
                    .build();

            assertNotNull(config);
            assertTrue(config.getListeners().contains(noOpListener));

            // Should be able to create cache with noOp listener
            assertDoesNotThrow(() -> {
                try (DefaultCache<String, String> cache = new DefaultCache<>(config)) {
                    cache.put("key", "value");
                    cache.get("key");
                    cache.remove("key");
                    cache.clear();
                }
            });
        }

        @Test
        @DisplayName("NoOp listener mixed with functional listeners")
        void testNoOpListenerMixedWithFunctionalListeners() {
            CacheEventListener<String, String> noOpListener = CacheEventListener.noOp();

            // Create a simple functional listener that tracks calls
            final int[] callCount = { 0 };
            CacheEventListener<String, String> functionalListener = new CacheEventListener<String, String>() {
                @Override
                public void onPut(String key, String value) {
                    callCount[0]++;
                }

                @Override
                public void onRemove(String key, String value) {
                    callCount[0]++;
                }

                @Override
                public void onEvict(String key, String value, EvictionReason reason) {
                    callCount[0]++;
                }

                @Override
                public void onExpire(String key, String value) {
                    callCount[0]++;
                }

                @Override
                public void onLoad(String key, String value) {
                    callCount[0]++;
                }

                @Override
                public void onLoadError(String key, Throwable error) {
                    callCount[0]++;
                }

                @Override
                public void onClear() {
                    callCount[0]++;
                }
            };

            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize(10L)
                    .addListener(noOpListener)
                    .addListener(functionalListener)
                    .build();

            try (DefaultCache<String, String> cache = new DefaultCache<>(config)) {
                cache.put("key", "value");
                cache.remove("key");
                cache.clear();

                // Functional listener should have been called, noOp listener should not
                // interfere
                assertEquals(3, callCount[0], "Functional listener should have been called 3 times");
            }
        }
    }
}
