package io.github.dhruv1110.jcachex.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the CacheException hierarchy.
 */
class CacheExceptionTest {

    @Nested
    @DisplayName("CacheException Base Class Tests")
    class CacheExceptionTests {

        @Test
        @DisplayName("CacheException constructor with message only")
        void testCacheExceptionSimpleConstructor() {
            CacheException exception = new CacheException("Test message");

            assertEquals("Test message", exception.getMessage());
            assertEquals(CacheException.ErrorType.UNKNOWN, exception.getErrorType());
            assertFalse(exception.isRetryable());
            assertNull(exception.getErrorCode());
        }

        @Test
        @DisplayName("CacheException with cause")
        void testCacheExceptionWithCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            CacheException exception = new CacheException("Test message", cause);

            assertEquals("Test message", exception.getMessage());
            assertEquals(CacheException.ErrorType.UNKNOWN, exception.getErrorType());
            assertFalse(exception.isRetryable());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("CacheException with full parameters")
        void testCacheExceptionFullConstructor() {
            CacheException exception = new CacheException(
                    "Test message",
                    CacheException.ErrorType.OPERATION,
                    "TEST_CODE",
                    true);

            assertEquals("Test message", exception.getMessage());
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertTrue(exception.isRetryable());
            assertEquals("TEST_CODE", exception.getErrorCode());
        }

        @Test
        @DisplayName("CacheException with cause and full parameters")
        void testCacheExceptionFullConstructorWithCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            CacheException exception = new CacheException(
                    "Test message",
                    cause,
                    CacheException.ErrorType.CONFIGURATION,
                    "CONFIG_CODE",
                    false);

            assertEquals("Test message", exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertEquals(CacheException.ErrorType.CONFIGURATION, exception.getErrorType());
            assertEquals("CONFIG_CODE", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Exception toString includes error details")
        void testToString() {
            CacheException exception = new CacheException(
                    "Test message",
                    CacheException.ErrorType.OPERATION,
                    "TEST_CODE",
                    true);

            String toString = exception.toString();
            assertTrue(toString.contains("Test message"));
            assertTrue(toString.contains("OPERATION"));
            assertTrue(toString.contains("TEST_CODE"));
            assertTrue(toString.contains("RETRYABLE"));
        }

        @Test
        @DisplayName("Exception toString without error code")
        void testToStringWithoutErrorCode() {
            CacheException exception = new CacheException(
                    "Test message",
                    CacheException.ErrorType.OPERATION,
                    null,
                    false);

            String toString = exception.toString();
            assertTrue(toString.contains("Test message"));
            assertTrue(toString.contains("OPERATION"));
            assertFalse(toString.contains("RETRYABLE"));
            assertFalse(toString.contains("[null]"));
        }
    }

    @Nested
    @DisplayName("CacheConfigurationException Tests")
    class CacheConfigurationExceptionTests {

        @Test
        @DisplayName("Simple constructor")
        void testSimpleConstructor() {
            CacheConfigurationException exception = new CacheConfigurationException("Config error");

            assertEquals("Config error", exception.getMessage());
            assertEquals(CacheException.ErrorType.CONFIGURATION, exception.getErrorType());
            assertEquals("CONFIG_ERROR", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Constructor with cause")
        void testConstructorWithCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            CacheConfigurationException exception = new CacheConfigurationException("Config error", cause);

            assertEquals("Config error", exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertEquals(CacheException.ErrorType.CONFIGURATION, exception.getErrorType());
            assertEquals("CONFIG_ERROR", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Constructor with custom error code")
        void testConstructorWithErrorCode() {
            CacheConfigurationException exception = new CacheConfigurationException("Config error", "CUSTOM_CODE");

            assertEquals("Config error", exception.getMessage());
            assertEquals(CacheException.ErrorType.CONFIGURATION, exception.getErrorType());
            assertEquals("CUSTOM_CODE", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Invalid maximum size factory method")
        void testInvalidMaximumSize() {
            CacheConfigurationException exception = CacheConfigurationException.invalidMaximumSize(-1);

            assertTrue(exception.getMessage().contains("Invalid maximum cache size"));
            assertTrue(exception.getMessage().contains("-1"));
            assertEquals(CacheException.ErrorType.CONFIGURATION, exception.getErrorType());
            assertEquals("INVALID_MAX_SIZE", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Invalid maximum weight factory method")
        void testInvalidMaximumWeight() {
            CacheConfigurationException exception = CacheConfigurationException.invalidMaximumWeight(-5);

            assertTrue(exception.getMessage().contains("Invalid maximum cache weight"));
            assertTrue(exception.getMessage().contains("-5"));
            assertEquals(CacheException.ErrorType.CONFIGURATION, exception.getErrorType());
            assertEquals("INVALID_MAX_WEIGHT", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Missing weigher factory method")
        void testMissingWeigher() {
            CacheConfigurationException exception = CacheConfigurationException.missingWeigher();

            assertTrue(exception.getMessage().contains("Weight-based cache configuration requires a weigher function"));
            assertEquals(CacheException.ErrorType.CONFIGURATION, exception.getErrorType());
            assertEquals("MISSING_WEIGHER", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Conflicting settings factory method")
        void testConflictingSettings() {
            CacheConfigurationException exception = CacheConfigurationException.conflictingSettings("weakKeys",
                    "softValues");

            assertTrue(exception.getMessage().contains("Conflicting cache configuration"));
            assertTrue(exception.getMessage().contains("weakKeys"));
            assertTrue(exception.getMessage().contains("softValues"));
            assertEquals(CacheException.ErrorType.CONFIGURATION, exception.getErrorType());
            assertEquals("CONFLICTING_SETTINGS", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }
    }

    @Nested
    @DisplayName("CacheOperationException Tests")
    class CacheOperationExceptionTests {

        @Test
        @DisplayName("Simple constructor")
        void testCacheOperationExceptionSimple() {
            CacheOperationException exception = new CacheOperationException("Operation failed");

            assertEquals("Operation failed", exception.getMessage());
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertEquals("OPERATION_ERROR", exception.getErrorCode());
            assertTrue(exception.isRetryable());
        }

        @Test
        @DisplayName("Constructor with cause")
        void testConstructorWithCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            CacheOperationException exception = new CacheOperationException("Operation failed", cause);

            assertEquals("Operation failed", exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertEquals("OPERATION_ERROR", exception.getErrorCode());
            assertTrue(exception.isRetryable());
        }

        @Test
        @DisplayName("Constructor with error code and retryable flag")
        void testConstructorWithErrorCodeAndRetryable() {
            CacheOperationException exception = new CacheOperationException("Operation failed", "CUSTOM_CODE", false);

            assertEquals("Operation failed", exception.getMessage());
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertEquals("CUSTOM_CODE", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Full constructor with cause")
        void testFullConstructorWithCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            CacheOperationException exception = new CacheOperationException("Operation failed", cause, "FULL_CODE",
                    true);

            assertEquals("Operation failed", exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertEquals("FULL_CODE", exception.getErrorCode());
            assertTrue(exception.isRetryable());
        }

        @Test
        @DisplayName("Invalid key factory method")
        void testInvalidKey() {
            CacheOperationException exception = CacheOperationException.invalidKey("testKey");

            assertTrue(exception.getMessage().contains("Invalid cache key"));
            assertTrue(exception.getMessage().contains("testKey"));
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertEquals("INVALID_KEY", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Invalid value factory method")
        void testInvalidValue() {
            CacheOperationException exception = CacheOperationException.invalidValue("testValue");

            assertTrue(exception.getMessage().contains("Invalid cache value"));
            assertTrue(exception.getMessage().contains("testValue"));
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertEquals("INVALID_VALUE", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Concurrent modification factory method")
        void testConcurrentModification() {
            CacheOperationException exception = CacheOperationException.concurrentModification("key123");

            assertTrue(exception.getMessage().contains("Concurrent modification detected"));
            assertTrue(exception.getMessage().contains("key123"));
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertEquals("CONCURRENT_MODIFICATION", exception.getErrorCode());
            assertTrue(exception.isRetryable());
        }

        @Test
        @DisplayName("Invalid cache state factory method")
        void testInvalidCacheState() {
            CacheOperationException exception = CacheOperationException.invalidCacheState("CLOSED");

            assertTrue(exception.getMessage().contains("Invalid cache state"));
            assertTrue(exception.getMessage().contains("CLOSED"));
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertEquals("INVALID_STATE", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }

        @Test
        @DisplayName("Eviction failure factory method")
        void testEvictionFailure() {
            RuntimeException cause = new RuntimeException("Eviction error");
            CacheOperationException exception = CacheOperationException.evictionFailure(cause);

            assertTrue(exception.getMessage().contains("Cache eviction failed"));
            assertTrue(exception.getMessage().contains("Eviction error"));
            assertEquals(cause, exception.getCause());
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertEquals("EVICTION_FAILURE", exception.getErrorCode());
            assertTrue(exception.isRetryable());
        }

        @Test
        @DisplayName("Serialization failure factory method")
        void testSerializationFailure() {
            RuntimeException cause = new RuntimeException("Serialization error");
            CacheOperationException exception = CacheOperationException.serializationFailure("key123", cause);

            assertTrue(exception.getMessage().contains("Serialization failed"));
            assertTrue(exception.getMessage().contains("key123"));
            assertTrue(exception.getMessage().contains("Serialization error"));
            assertEquals(cause, exception.getCause());
            assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
            assertEquals("SERIALIZATION_FAILURE", exception.getErrorCode());
            assertFalse(exception.isRetryable());
        }
    }

    @Nested
    @DisplayName("Exception Hierarchy Tests")
    class ExceptionHierarchyTests {

        @Test
        @DisplayName("Exception hierarchy")
        void testExceptionHierarchy() {
            CacheConfigurationException configException = CacheConfigurationException.invalidMaximumSize(-1);
            CacheOperationException operationException = new CacheOperationException("test");

            assertInstanceOf(CacheException.class, configException);
            assertInstanceOf(RuntimeException.class, configException);
            assertInstanceOf(CacheException.class, operationException);
            assertInstanceOf(RuntimeException.class, operationException);
        }

        @Test
        @DisplayName("Error type classifications")
        void testErrorTypes() {
            assertEquals(6, CacheException.ErrorType.values().length);

            // Test all error types exist
            assertNotNull(CacheException.ErrorType.CONFIGURATION);
            assertNotNull(CacheException.ErrorType.OPERATION);
            assertNotNull(CacheException.ErrorType.CAPACITY);
            assertNotNull(CacheException.ErrorType.TIMEOUT);
            assertNotNull(CacheException.ErrorType.LOADING);
            assertNotNull(CacheException.ErrorType.UNKNOWN);
        }
    }
}
