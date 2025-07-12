package io.github.dhruv1110.jcachex.profiles;

import java.time.Duration;

/**
 * Constants used throughout the cache profile system.
 *
 * <p>
 * This class centralizes commonly used values to eliminate duplication
 * and provide a single source of truth for configuration values.
 * </p>
 *
 * @since 1.0.0
 */
public final class ProfileConstants {

    private ProfileConstants() {
        // Utility class - no instances
    }

    // ===== PRIORITY CONSTANTS =====

    /**
     * Default priority for profiles that should be used as fallbacks.
     */
    public static final int PRIORITY_DEFAULT = 0;

    /**
     * Low priority for less preferred profiles.
     */
    public static final int PRIORITY_LOW = 3;

    /**
     * Normal priority for standard profiles.
     */
    public static final int PRIORITY_NORMAL = 5;

    /**
     * High priority for preferred profiles.
     */
    public static final int PRIORITY_HIGH = 8;

    /**
     * Very high priority for specialized profiles.
     */
    public static final int PRIORITY_VERY_HIGH = 10;

    // ===== CACHE SIZE CONSTANTS =====

    /**
     * Small cache size for memory-constrained environments.
     */
    public static final long SIZE_SMALL = 100L;

    /**
     * Medium cache size for general use.
     */
    public static final long SIZE_MEDIUM = 1000L;

    /**
     * Large cache size for high-capacity scenarios.
     */
    public static final long SIZE_LARGE = 10000L;

    /**
     * Extra large cache size for enterprise scenarios.
     */
    public static final long SIZE_XLARGE = 50000L;

    // ===== INITIAL CAPACITY CONSTANTS =====

    /**
     * Small initial capacity for memory efficiency.
     */
    public static final int CAPACITY_SMALL = 16;

    /**
     * Medium initial capacity for balanced scenarios.
     */
    public static final int CAPACITY_MEDIUM = 32;

    /**
     * Large initial capacity for high-throughput scenarios.
     */
    public static final int CAPACITY_LARGE = 64;

    /**
     * Extra large initial capacity for enterprise scenarios.
     */
    public static final int CAPACITY_XLARGE = 128;

    // ===== EXPIRATION CONSTANTS =====

    /**
     * Short expiration time for temporary data (5 minutes).
     */
    public static final Duration EXPIRATION_SHORT = Duration.ofMinutes(5);

    /**
     * Medium expiration time for API responses (15 minutes).
     */
    public static final Duration EXPIRATION_MEDIUM = Duration.ofMinutes(15);

    /**
     * Long expiration time for session data (30 minutes).
     */
    public static final Duration EXPIRATION_LONG = Duration.ofMinutes(30);

    /**
     * Extra long expiration time for computation results (2 hours).
     */
    public static final Duration EXPIRATION_XLONG = Duration.ofHours(2);

    // ===== LOGGING CONSTANTS =====

    /**
     * Logger name for profile-related operations.
     */
    public static final String LOGGER_NAME = "io.github.dhruv1110.jcachex.profiles";

    /**
     * Log message for profile registration.
     */
    public static final String LOG_PROFILE_REGISTERED = "Cache profile registered: {} (category: {}, priority: {})";

    /**
     * Log message for profile creation.
     */
    public static final String LOG_PROFILE_CREATED = "Cache profile created: {} with implementation: {}";

    /**
     * Log message for profile initialization.
     */
    public static final String LOG_PROFILE_INIT_START = "Initializing cache profile system...";

    /**
     * Log message for profile initialization completion.
     */
    public static final String LOG_PROFILE_INIT_COMPLETE = "Cache profile system initialized with {} profiles";

    /**
     * Log message for profile discovery.
     */
    public static final String LOG_PROFILE_DISCOVERY = "Found {} suitable profiles for workload characteristics: {}";

    // ===== VALIDATION CONSTANTS =====

    /**
     * Maximum allowed priority value.
     */
    public static final int MAX_PRIORITY = 100;

    /**
     * Minimum allowed priority value.
     */
    public static final int MIN_PRIORITY = 0;

    /**
     * Maximum profile name length.
     */
    public static final int MAX_NAME_LENGTH = 50;

    /**
     * Maximum description length.
     */
    public static final int MAX_DESCRIPTION_LENGTH = 200;

    /**
     * Maximum number of tags per profile.
     */
    public static final int MAX_TAGS_PER_PROFILE = 10;

    // ===== CATEGORY DISPLAY NAMES =====

    /**
     * Display name for core category.
     */
    public static final String CATEGORY_CORE_DISPLAY = "Core Profiles";

    /**
     * Display name for specialized category.
     */
    public static final String CATEGORY_SPECIALIZED_DISPLAY = "Specialized Profiles";

    /**
     * Display name for advanced category.
     */
    public static final String CATEGORY_ADVANCED_DISPLAY = "Advanced Profiles";

    /**
     * Display name for custom category.
     */
    public static final String CATEGORY_CUSTOM_DISPLAY = "Custom Profiles";
}
