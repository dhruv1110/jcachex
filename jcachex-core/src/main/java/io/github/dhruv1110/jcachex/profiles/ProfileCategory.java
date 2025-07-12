package io.github.dhruv1110.jcachex.profiles;

/**
 * Enum representing different categories of cache profiles.
 *
 * <p>
 * Categories help organize profiles by their intended use cases and complexity
 * levels,
 * making it easier for users to find appropriate profiles for their needs.
 * </p>
 *
 * @since 1.0.0
 */
public enum ProfileCategory {

    /**
     * Core profiles that cover 80% of use cases.
     * These are well-tested, stable profiles suitable for most applications.
     */
    CORE("Core", "Essential profiles for common use cases", 1),

    /**
     * Specialized profiles for specific scenarios.
     * These profiles are optimized for particular workload patterns.
     */
    SPECIALIZED("Specialized", "Profiles optimized for specific scenarios", 2),

    /**
     * Advanced profiles for cutting-edge requirements.
     * These profiles use experimental features and latest optimizations.
     */
    ADVANCED("Advanced", "Cutting-edge profiles with experimental features", 3),

    /**
     * Custom profiles created by users.
     * These are not built-in profiles but user-defined configurations.
     */
    CUSTOM("Custom", "User-defined custom profiles", 4);

    private final String displayName;
    private final String description;
    private final int sortOrder;

    ProfileCategory(String displayName, String description, int sortOrder) {
        this.displayName = displayName;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    /**
     * Gets the human-readable display name for this category.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the description of this category.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the sort order for this category (lower numbers first).
     *
     * @return the sort order
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Checks if this is a core category.
     *
     * @return true if this is the CORE category
     */
    public boolean isCore() {
        return this == CORE;
    }

    /**
     * Checks if this is an advanced category.
     *
     * @return true if this is the ADVANCED category
     */
    public boolean isAdvanced() {
        return this == ADVANCED;
    }
}
