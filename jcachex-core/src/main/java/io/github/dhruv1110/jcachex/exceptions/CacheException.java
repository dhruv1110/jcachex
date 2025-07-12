package io.github.dhruv1110.jcachex.exceptions;

/**
 * Base exception class for all JCacheX-related exceptions.
 * <p>
 * This is the root exception class for all cache-related errors in JCacheX.
 * It provides a common base for all cache exceptions and includes utilities
 * for error classification and handling.
 * </p>
 *
 * <h3>Exception Hierarchy:</h3>
 *
 * <pre>
 * CacheException
 * ├── CacheConfigurationException
 * ├── CacheOperationException
 * │   ├── CacheWriteException
 * │   ├── CacheReadException
 * │   └── CacheEvictionException
 * ├── CacheLoadingException
 * ├── CacheCapacityException
 * └── CacheTimeoutException
 * </pre>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * try {
 *     cache.put(key, value);
 * } catch (CacheException e) {
 *     // Handle all cache-related exceptions
 *     logger.error("Cache operation failed: " + e.getMessage(), e);
 *     // Implement fallback logic
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class CacheException extends RuntimeException {

    private static final long serialVersionUID = 1001L;

    /**
     * Error classification for handling different types of cache errors.
     */
    public enum ErrorType {
        /** Configuration or setup errors */
        CONFIGURATION,
        /** Runtime operation errors */
        OPERATION,
        /** Resource capacity errors */
        CAPACITY,
        /** Timeout or performance errors */
        TIMEOUT,
        /** Data loading errors */
        LOADING,
        /** Unknown or unclassified errors */
        UNKNOWN
    }

    private final ErrorType errorType;
    private final String errorCode;
    private final boolean retryable;

    /**
     * Constructs a new cache exception with the specified detail message.
     *
     * @param message the detail message
     */
    public CacheException(String message) {
        super(message);
        this.errorType = ErrorType.UNKNOWN;
        this.errorCode = null;
        this.retryable = false;
    }

    /**
     * Constructs a new cache exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public CacheException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.UNKNOWN;
        this.errorCode = null;
        this.retryable = false;
    }

    /**
     * Constructs a new cache exception with the specified detail message,
     * error type, and error code.
     *
     * @param message   the detail message
     * @param errorType the type of error
     * @param errorCode the specific error code
     * @param retryable whether this operation can be retried
     */
    public CacheException(String message, ErrorType errorType, String errorCode, boolean retryable) {
        super(message);
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    /**
     * Constructs a new cache exception with the specified detail message,
     * cause, error type, and error code.
     *
     * @param message   the detail message
     * @param cause     the cause
     * @param errorType the type of error
     * @param errorCode the specific error code
     * @param retryable whether this operation can be retried
     */
    public CacheException(String message, Throwable cause, ErrorType errorType, String errorCode, boolean retryable) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    /**
     * Returns the error type classification.
     *
     * @return the error type
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Returns the specific error code, if available.
     *
     * @return the error code, or null if not specified
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns whether this operation can be retried.
     *
     * @return true if the operation can be retried, false otherwise
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Returns a comprehensive string representation of this exception.
     *
     * @return a string representation including error type and code
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(": ").append(getMessage());
        if (errorCode != null) {
            sb.append(" [").append(errorCode).append("]");
        }
        sb.append(" (").append(errorType).append(")");
        if (retryable) {
            sb.append(" [RETRYABLE]");
        }
        return sb.toString();
    }
}
