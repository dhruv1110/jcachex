package io.github.dhruv1110.jcachex.profiles;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Centralized registry for all cache profiles. This eliminates the need for
 * manual
 * updates in multiple files when adding new cache types.
 *
 * <p>
 * Profiles automatically register themselves when created, and this registry
 * provides centralized access for all consumers.
 * </p>
 */
public final class ProfileRegistry {

    private static final Map<String, CacheProfile<Object, Object>> profiles = new ConcurrentHashMap<>();
    private static final Map<String, ProfileMetadata> metadata = new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;

    private ProfileRegistry() {
        // Utility class
    }

    /**
     * Registers a cache profile with the registry.
     *
     * @param profile  the profile to register
     * @param metadata additional metadata about the profile
     */
    public static synchronized void register(CacheProfile<Object, Object> profile, ProfileMetadata metadata) {
        if (profile == null || profile.getName() == null) {
            throw new IllegalArgumentException("Profile and profile name cannot be null");
        }

        String name = profile.getName();
        if (profiles.containsKey(name)) {
            throw new IllegalStateException("Profile already registered: " + name);
        }

        profiles.put(name, profile);
        ProfileRegistry.metadata.put(name, metadata != null ? metadata : ProfileMetadata.DEFAULT);
    }

    /**
     * Gets a profile by name.
     *
     * @param name the profile name
     * @return the profile, or null if not found
     */
    public static CacheProfile<Object, Object> getProfile(String name) {
        ensureInitialized();
        return profiles.get(name);
    }

    /**
     * Gets all registered profiles.
     *
     * @return unmodifiable collection of all profiles
     */
    public static Collection<CacheProfile<Object, Object>> getAllProfiles() {
        ensureInitialized();
        return Collections.unmodifiableCollection(profiles.values());
    }

    /**
     * Gets all profile names.
     *
     * @return unmodifiable set of all profile names
     */
    public static Set<String> getAllProfileNames() {
        ensureInitialized();
        return Collections.unmodifiableSet(profiles.keySet());
    }

    /**
     * Gets metadata for a profile.
     *
     * @param profileName the profile name
     * @return the metadata, or default metadata if not found
     */
    public static ProfileMetadata getMetadata(String profileName) {
        ensureInitialized();
        return metadata.getOrDefault(profileName, ProfileMetadata.DEFAULT);
    }

    /**
     * Finds profiles suitable for the given workload characteristics.
     *
     * @param workload the workload characteristics
     * @return list of suitable profiles, ordered by suitability
     */
    public static List<CacheProfile<Object, Object>> findSuitableProfiles(WorkloadCharacteristics workload) {
        ensureInitialized();

        return profiles.values().stream()
                .filter(profile -> profile.isSuitableFor(workload))
                .sorted((p1, p2) -> {
                    ProfileMetadata m1 = metadata.get(p1.getName());
                    ProfileMetadata m2 = metadata.get(p2.getName());
                    return Integer.compare(m2.getPriority(), m1.getPriority()); // Higher priority first
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Creates a cache instance using the specified profile.
     *
     * @param profileName the profile name
     * @param config      the cache configuration
     * @return the cache instance
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Cache<K, V> createCache(String profileName, CacheConfig<K, V> config) {
        CacheProfile<Object, Object> profile = getProfile(profileName);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown profile: " + profileName);
        }

        ProfileMetadata meta = getMetadata(profileName);
        return (Cache<K, V>) meta.getCacheFactory().apply(config);
    }

    /**
     * Checks if a profile is registered.
     *
     * @param name the profile name
     * @return true if registered
     */
    public static boolean isRegistered(String name) {
        ensureInitialized();
        return profiles.containsKey(name);
    }

    /**
     * Gets the default profile.
     *
     * @return the default profile
     */
    public static CacheProfile<Object, Object> getDefaultProfile() {
        return getProfile("DEFAULT");
    }

    /**
     * Initializes the registry with default profiles if not already initialized.
     */
    private static synchronized void ensureInitialized() {
        if (!initialized) {
            // This will trigger the static initialization of CacheProfilesV3
            // which will register all default profiles
            try {
                Class.forName("io.github.dhruv1110.jcachex.profiles.CacheProfilesV3");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to initialize default profiles", e);
            }
            initialized = true;
        }
    }

    /**
     * For testing purposes - clears all profiles.
     */
    static synchronized void clearForTesting() {
        profiles.clear();
        metadata.clear();
        initialized = false;
    }

    /**
     * Metadata about a cache profile.
     */
    public static final class ProfileMetadata {
        public static final ProfileMetadata DEFAULT = new ProfileMetadata.Builder().build();

        private final String description;
        private final String category;
        private final int priority;
        private final Set<String> tags;
        private final Function<CacheConfig<?, ?>, Cache<?, ?>> cacheFactory;

        private ProfileMetadata(Builder builder) {
            this.description = builder.description;
            this.category = builder.category;
            this.priority = builder.priority;
            this.tags = Collections.unmodifiableSet(new HashSet<>(builder.tags));
            this.cacheFactory = builder.cacheFactory;
        }

        public String getDescription() {
            return description;
        }

        public String getCategory() {
            return category;
        }

        public int getPriority() {
            return priority;
        }

        public Set<String> getTags() {
            return tags;
        }

        public Function<CacheConfig<?, ?>, Cache<?, ?>> getCacheFactory() {
            return cacheFactory;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String description = "";
            private String category = "General";
            private int priority = 0;
            private Set<String> tags = new HashSet<>();
            private Function<CacheConfig<?, ?>, Cache<?, ?>> cacheFactory;

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder category(String category) {
                this.category = category;
                return this;
            }

            public Builder priority(int priority) {
                this.priority = priority;
                return this;
            }

            public Builder tags(String... tags) {
                this.tags.addAll(Arrays.asList(tags));
                return this;
            }

            public Builder cacheFactory(Function<CacheConfig<?, ?>, Cache<?, ?>> factory) {
                this.cacheFactory = factory;
                return this;
            }

            public ProfileMetadata build() {
                return new ProfileMetadata(this);
            }
        }
    }
}
