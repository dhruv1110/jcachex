package io.github.dhruv1110.jcachex.exceptions;

/**
 * Exception thrown when there are configuration errors in cache setup.
 * <p>
 * This exception indicates problems with cache configuration such as:
 * </p>
 * <ul>
 * <li>Invalid cache size limits</li>
 * <li>Incompatible configuration combinations</li>
 * <li>Missing required configuration parameters</li>
 * <li>Invalid eviction strategy settings</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * 
 * <pre>{@code
 * try {
 *     CacheConfig<String, String> config = CacheConfig.<String, String>builder()
 *             .maximumSize(-1L) // Invalid size
 *             .build();
 * } catch (CacheConfigurationException e) {
 *     // Handle configuration error
 *     logger.error("Invalid cache configuration: " + e.getMessage());
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class CacheConfigurationException extends CacheException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new cache configuration exception with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public CacheConfigurationException(String message) {
        super(message, ErrorType.CONFIGURATION, "CONFIG_ERROR", false);
    }

    /**
     * Constructs a new cache configuration exception with the specified detail
     * message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public CacheConfigurationException(String message, Throwable cause) {
        super(message, cause, ErrorType.CONFIGURATION, "CONFIG_ERROR", false);
    }

    /**
     * Constructs a new cache configuration exception with a specific error code.
     *
     * @param message   the detail message
     * @param errorCode the specific configuration error code
     */
    public CacheConfigurationException(String message, String errorCode) {
        super(message, ErrorType.CONFIGURATION, errorCode, false);
    }

    /**
     * Creates a configuration exception for invalid maximum size.
     *
     * @param size the invalid size value
     * @return a new CacheConfigurationException
     */
    public static CacheConfigurationException invalidMaximumSize(long size) {
        return new CacheConfigurationException(
                "Invalid maximum cache size: " + size + ". Size must be positive.",
                "INVALID_MAX_SIZE");
    }

    /**
     * Creates a configuration exception for invalid maximum weight.
     *
     * @param weight the invalid weight value
     * @return a new CacheConfigurationException
     */
    public static CacheConfigurationException invalidMaximumWeight(long weight) {
        return new CacheConfigurationException(
                "Invalid maximum cache weight: " + weight + ". Weight must be positive.",
                "INVALID_MAX_WEIGHT");
    }

    /**
     * Creates a configuration exception for missing weigher function.
     *
     * @return a new CacheConfigurationException
     */
    public static CacheConfigurationException missingWeigher() {
        return new CacheConfigurationException(
                "Weight-based cache configuration requires a weigher function.",
                "MISSING_WEIGHER");
    }

    /**
     * Creates a configuration exception for conflicting settings.
     *
     * @param setting1 the first conflicting setting
     * @param setting2 the second conflicting setting
     * @return a new CacheConfigurationException
     */
    public static CacheConfigurationException conflictingSettings(String setting1, String setting2) {
        return new CacheConfigurationException(
                "Conflicting cache configuration: " + setting1 + " cannot be used with " + setting2,
                "CONFLICTING_SETTINGS");
    }
}
