package io.github.dhruv1110.jcachex.exceptions;

/**
 * Exception thrown when cache operations fail at runtime.
 * <p>
 * This exception indicates problems during cache operations such as:
 * </p>
 * <ul>
 * <li>Read/Write operation failures</li>
 * <li>Eviction strategy errors</li>
 * <li>Concurrency conflicts</li>
 * <li>Resource exhaustion</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * 
 * <pre>{@code
 * try {
 *     cache.put(key, value);
 * } catch (CacheOperationException e) {
 *     if (e.isRetryable()) {
 *         // Implement retry logic
 *         retryOperation();
 *     } else {
 *         // Handle permanent failure
 *         logger.error("Cache operation failed permanently: " + e.getMessage());
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class CacheOperationException extends CacheException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new cache operation exception with the specified detail message.
     *
     * @param message the detail message
     */
    public CacheOperationException(String message) {
        super(message, ErrorType.OPERATION, "OPERATION_ERROR", true);
    }

    /**
     * Constructs a new cache operation exception with the specified detail message
     * and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public CacheOperationException(String message, Throwable cause) {
        super(message, cause, ErrorType.OPERATION, "OPERATION_ERROR", true);
    }

    /**
     * Constructs a new cache operation exception with specific error code and
     * retryable flag.
     *
     * @param message   the detail message
     * @param errorCode the specific operation error code
     * @param retryable whether this operation can be retried
     */
    public CacheOperationException(String message, String errorCode, boolean retryable) {
        super(message, ErrorType.OPERATION, errorCode, retryable);
    }

    /**
     * Constructs a new cache operation exception with cause, error code and
     * retryable flag.
     *
     * @param message   the detail message
     * @param cause     the cause
     * @param errorCode the specific operation error code
     * @param retryable whether this operation can be retried
     */
    public CacheOperationException(String message, Throwable cause, String errorCode, boolean retryable) {
        super(message, cause, ErrorType.OPERATION, errorCode, retryable);
    }

    /**
     * Creates an operation exception for key validation failures.
     *
     * @param key the invalid key
     * @return a new CacheOperationException
     */
    public static CacheOperationException invalidKey(Object key) {
        return new CacheOperationException(
                "Invalid cache key: " + key + ". Key cannot be null.",
                "INVALID_KEY",
                false);
    }

    /**
     * Creates an operation exception for value validation failures.
     *
     * @param value the invalid value
     * @return a new CacheOperationException
     */
    public static CacheOperationException invalidValue(Object value) {
        return new CacheOperationException(
                "Invalid cache value: " + value + ". Value cannot be null.",
                "INVALID_VALUE",
                false);
    }

    /**
     * Creates an operation exception for concurrent modification conflicts.
     *
     * @param key the key involved in the conflict
     * @return a new CacheOperationException
     */
    public static CacheOperationException concurrentModification(Object key) {
        return new CacheOperationException(
                "Concurrent modification detected for key: " + key,
                "CONCURRENT_MODIFICATION",
                true);
    }

    /**
     * Creates an operation exception for cache state errors.
     *
     * @param state the invalid state
     * @return a new CacheOperationException
     */
    public static CacheOperationException invalidCacheState(String state) {
        return new CacheOperationException(
                "Invalid cache state: " + state + ". Cache may be closed or corrupted.",
                "INVALID_STATE",
                false);
    }

    /**
     * Creates an operation exception for eviction failures.
     *
     * @param cause the underlying cause
     * @return a new CacheOperationException
     */
    public static CacheOperationException evictionFailure(Throwable cause) {
        return new CacheOperationException(
                "Cache eviction failed: " + cause.getMessage(),
                cause,
                "EVICTION_FAILURE",
                true);
    }

    /**
     * Creates an operation exception for serialization failures.
     *
     * @param key   the key being serialized
     * @param cause the underlying cause
     * @return a new CacheOperationException
     */
    public static CacheOperationException serializationFailure(Object key, Throwable cause) {
        return new CacheOperationException(
                "Serialization failed for key: " + key + ". " + cause.getMessage(),
                cause,
                "SERIALIZATION_FAILURE",
                false);
    }
}
