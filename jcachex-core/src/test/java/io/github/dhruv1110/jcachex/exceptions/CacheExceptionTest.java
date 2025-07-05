package io.github.dhruv1110.jcachex.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the CacheException hierarchy.
 */
class CacheExceptionTest {

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
    @DisplayName("CacheConfigurationException invalid maximum size")
    void testInvalidMaximumSize() {
        CacheConfigurationException exception = CacheConfigurationException.invalidMaximumSize(-1);

        assertTrue(exception.getMessage().contains("Invalid maximum cache size"));
        assertEquals(CacheException.ErrorType.CONFIGURATION, exception.getErrorType());
        assertFalse(exception.isRetryable());
    }

    @Test
    @DisplayName("CacheOperationException simple constructor")
    void testCacheOperationExceptionSimple() {
        CacheOperationException exception = new CacheOperationException("Operation failed");

        assertEquals("Operation failed", exception.getMessage());
        assertEquals(CacheException.ErrorType.OPERATION, exception.getErrorType());
        assertTrue(exception.isRetryable());
    }

    @Test
    @DisplayName("CacheOperationException with factory methods")
    void testCacheOperationFactoryMethods() {
        // Test invalidKey factory method
        CacheOperationException invalidKeyException = CacheOperationException.invalidKey("testKey");
        assertTrue(invalidKeyException.getMessage().contains("Invalid cache key"));
        assertFalse(invalidKeyException.isRetryable());

        // Test evictionFailure factory method
        RuntimeException cause = new RuntimeException("Eviction error");
        CacheOperationException evictionException = CacheOperationException.evictionFailure(cause);
        assertTrue(evictionException.getMessage().contains("Cache eviction failed"));
        assertTrue(evictionException.isRetryable());
        assertEquals(cause, evictionException.getCause());
    }

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
}
