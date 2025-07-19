package io.github.dhruv1110.jcachex.profiles;

/**
 * Enum containing all predefined cache profile names.
 *
 * <p>
 * This enum centralizes all profile names, eliminating string duplication
 * and providing compile-time type safety when working with profiles.
 * </p>
 *
 * @since 1.0.0
 */
public enum ProfileName {

    // Core Profiles
    DEFAULT("DEFAULT", "General-purpose cache with balanced performance", ProfileCategory.CORE),
    READ_HEAVY("READ_HEAVY", "Optimized for read-intensive workloads (80%+ reads)", ProfileCategory.CORE),
    WRITE_HEAVY("WRITE_HEAVY", "Optimized for write-intensive workloads (50%+ writes)", ProfileCategory.CORE),
    MEMORY_EFFICIENT("MEMORY_EFFICIENT", "Minimizes memory usage for constrained environments", ProfileCategory.CORE),
    HIGH_PERFORMANCE("HIGH_PERFORMANCE", "Maximum throughput optimization", ProfileCategory.CORE),

    // Specialized Profiles
    SESSION_CACHE("SESSION_CACHE", "Optimized for user session storage with time-based expiration",
            ProfileCategory.SPECIALIZED),
    API_CACHE("API_CACHE", "Optimized for API response caching with short TTL", ProfileCategory.SPECIALIZED),
    COMPUTE_CACHE("COMPUTE_CACHE", "Optimized for expensive computation results", ProfileCategory.SPECIALIZED),

    // Advanced Profiles
    ML_OPTIMIZED("ML_OPTIMIZED", "Machine learning optimized cache with predictive capabilities",
            ProfileCategory.ADVANCED),
    ZERO_COPY("ZERO_COPY", "Zero-copy optimized cache for minimal memory allocation", ProfileCategory.ADVANCED),
    HARDWARE_OPTIMIZED("HARDWARE_OPTIMIZED", "Hardware-optimized cache leveraging CPU-specific features",
            ProfileCategory.ADVANCED),
    KUBERNETES_DISTRIBUTED("KUBERNETES_DISTRIBUTED", "Kubernetes distributed cache optimized for cluster environments",
            ProfileCategory.ADVANCED);

    private final String value;
    private final String description;
    private final ProfileCategory category;

    ProfileName(String value, String description, ProfileCategory category) {
        this.value = value;
        this.description = description;
        this.category = category;
    }

    /**
     * Gets the string value of this profile name.
     *
     * @return the profile name as a string
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the description of this profile.
     *
     * @return the profile description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the category this profile belongs to.
     *
     * @return the profile category
     */
    public ProfileCategory getCategory() {
        return category;
    }

    /**
     * Checks if this is a core profile.
     *
     * @return true if this profile is in the CORE category
     */
    public boolean isCore() {
        return category == ProfileCategory.CORE;
    }

    /**
     * Checks if this is an advanced profile.
     *
     * @return true if this profile is in the ADVANCED category
     */
    public boolean isAdvanced() {
        return category == ProfileCategory.ADVANCED;
    }

    /**
     * Finds a ProfileName by its string value.
     *
     * @param value the string value to search for
     * @return the matching ProfileName, or null if not found
     */
    public static ProfileName fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (ProfileName profileName : values()) {
            if (profileName.value.equals(value)) {
                return profileName;
            }
        }

        return null;
    }

    /**
     * Gets all core profile names.
     *
     * @return array of core profiles
     */
    public static ProfileName[] getCoreProfiles() {
        return new ProfileName[] { DEFAULT, READ_HEAVY, WRITE_HEAVY, MEMORY_EFFICIENT, HIGH_PERFORMANCE };
    }

    /**
     * Gets all specialized profile names.
     *
     * @return array of specialized profiles
     */
    public static ProfileName[] getSpecializedProfiles() {
        return new ProfileName[] { SESSION_CACHE, API_CACHE, COMPUTE_CACHE };
    }

    /**
     * Gets all advanced profile names.
     *
     * @return array of advanced profiles
     */
    public static ProfileName[] getAdvancedProfiles() {
        return new ProfileName[] { ML_OPTIMIZED, ZERO_COPY, HARDWARE_OPTIMIZED, KUBERNETES_DISTRIBUTED };
    }

    @Override
    public String toString() {
        return value;
    }
}
