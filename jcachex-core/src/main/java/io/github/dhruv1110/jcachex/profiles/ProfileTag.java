package io.github.dhruv1110.jcachex.profiles;

/**
 * Enum containing common tags used for cache profiles.
 *
 * <p>
 * Tags help with profile discovery, filtering, and categorization.
 * They provide semantic meaning about the characteristics and use cases
 * of each profile.
 * </p>
 *
 * @since 1.0.0
 */
public enum ProfileTag {

    // General characteristics
    GENERAL("general", "General-purpose, suitable for most use cases"),
    BALANCED("balanced", "Balanced performance characteristics"),
    DEFAULT("default", "Default or fallback option"),

    // Performance characteristics
    PERFORMANCE("performance", "High performance optimization"),
    THROUGHPUT("throughput", "High throughput optimization"),
    LOW_LATENCY("low-latency", "Optimized for minimal latency"),
    ULTRA_LOW_LATENCY("ultra-low-latency", "Ultra-low latency optimization"),

    // Workload patterns
    READ_HEAVY("read-heavy", "Optimized for read-intensive workloads"),
    WRITE_HEAVY("write-heavy", "Optimized for write-intensive workloads"),
    MIXED("mixed", "Suitable for mixed read/write workloads"),
    CACHE_FRIENDLY("cache-friendly", "Optimized for high cache hit rates"),

    // Memory characteristics
    MEMORY_EFFICIENT("memory-efficient", "Minimal memory usage"),
    MEMORY_CONSTRAINED("memory-constrained", "Suitable for constrained environments"),
    ZERO_COPY("zero-copy", "Minimal memory allocation and copying"),
    DIRECT_MEMORY("direct-memory", "Uses direct memory buffers"),

    // Concurrency
    ASYNC("async", "Supports asynchronous operations"),
    HIGH_CONCURRENCY("high-concurrency", "Optimized for high concurrent access"),
    THREAD_SAFE("thread-safe", "Thread-safe implementation"),
    LOCK_FREE("lock-free", "Lock-free implementation"),

    // Specific use cases
    SESSION("session", "Optimized for session storage"),
    API("api", "Suitable for API response caching"),
    EXTERNAL("external", "For external service responses"),
    COMPUTATION("computation", "For expensive computation results"),
    TEMPORAL("temporal", "Time-based access patterns"),

    // Expiration and TTL
    TTL("ttl", "Supports time-to-live expiration"),
    EXPIRATION("expiration", "Time-based expiration"),

    // Network and distribution
    NETWORK("network", "Network-aware caching"),
    NETWORK_AWARE("network-aware", "Optimized for network latency"),
    DISTRIBUTED("distributed", "Distributed caching support"),
    CLUSTER("cluster", "Cluster environment optimization"),
    CONSISTENCY("consistency", "Provides consistency guarantees"),

    // Advanced features
    MACHINE_LEARNING("machine-learning", "Machine learning optimizations"),
    PREDICTIVE("predictive", "Predictive caching capabilities"),
    ADAPTIVE("adaptive", "Adaptive behavior based on usage patterns"),
    INTELLIGENT("intelligent", "Intelligent optimization algorithms"),

    // Hardware optimizations
    HARDWARE("hardware", "Hardware-specific optimizations"),
    CPU_OPTIMIZED("cpu-optimized", "CPU-specific optimizations"),
    SIMD("simd", "SIMD instruction optimizations"),
    PARALLEL("parallel", "Parallel processing optimization"),

    // Special characteristics
    HFT("hft", "High-frequency trading optimized"),
    EXPERIMENTAL("experimental", "Experimental features"),
    ENTERPRISE("enterprise", "Enterprise-grade features"),
    PRODUCTION("production", "Production-ready"),
    DEVELOPMENT("development", "Development and testing");

    private final String value;
    private final String description;

    ProfileTag(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Gets the string value of this tag.
     *
     * @return the tag value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the description of this tag.
     *
     * @return the tag description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Finds a ProfileTag by its string value.
     *
     * @param value the string value to search for
     * @return the matching ProfileTag, or null if not found
     */
    public static ProfileTag fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (ProfileTag tag : values()) {
            if (tag.value.equals(value)) {
                return tag;
            }
        }

        return null;
    }

    /**
     * Converts an array of ProfileTags to an array of their string values.
     *
     * @param tags the tags to convert
     * @return array of string values
     */
    public static String[] toStringArray(ProfileTag... tags) {
        String[] result = new String[tags.length];
        for (int i = 0; i < tags.length; i++) {
            result[i] = tags[i].value;
        }
        return result;
    }

    @Override
    public String toString() {
        return value;
    }
}
