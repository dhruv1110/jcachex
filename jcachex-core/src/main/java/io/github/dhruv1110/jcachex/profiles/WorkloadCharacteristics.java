package io.github.dhruv1110.jcachex.profiles;

/**
 * Encapsulates workload characteristics for cache profiling and optimization.
 *
 * This class provides a way to describe the expected usage patterns of a cache,
 * allowing profiles to automatically select the most appropriate implementation
 * and configuration parameters.
 *
 * <h2>Usage Examples:</h2>
 * 
 * <pre>{@code
 * // High-frequency read operations
 * WorkloadCharacteristics readHeavy = WorkloadCharacteristics.builder()
 *         .readToWriteRatio(10.0)
 *         .accessPattern(AccessPattern.TEMPORAL_LOCALITY)
 *         .build();
 *
 * // Write-intensive workload
 * WorkloadCharacteristics writeHeavy = WorkloadCharacteristics.builder()
 *         .readToWriteRatio(0.5)
 *         .accessPattern(AccessPattern.RANDOM)
 *         .build();
 *
 * // Memory-constrained environment
 * WorkloadCharacteristics memoryConstrained = WorkloadCharacteristics.builder()
 *         .memoryConstraint(MemoryConstraint.VERY_LIMITED)
 *         .accessPattern(AccessPattern.SEQUENTIAL)
 *         .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public class WorkloadCharacteristics {

    /**
     * Enum representing different access patterns.
     */
    public enum AccessPattern {
        /** Sequential access pattern - good for prefetching */
        SEQUENTIAL,
        /** Random access pattern - requires fast lookup */
        RANDOM,
        /** Temporal locality - recent items accessed again soon */
        TEMPORAL_LOCALITY,
        /** Spatial locality - nearby items accessed together */
        SPATIAL_LOCALITY,
        /** Mixed access pattern */
        MIXED
    }

    /**
     * Enum representing memory constraints.
     */
    public enum MemoryConstraint {
        /** No memory constraints */
        NONE,
        /** Limited memory available */
        LIMITED,
        /** Very limited memory - optimize for minimal allocation */
        VERY_LIMITED,
        /** Constrained by GC pressure */
        GC_SENSITIVE
    }

    /**
     * Enum representing concurrency characteristics.
     */
    public enum ConcurrencyLevel {
        /** Single-threaded access */
        SINGLE_THREADED,
        /** Low concurrency - few threads */
        LOW,
        /** Medium concurrency - moderate thread count */
        MEDIUM,
        /** High concurrency - many concurrent threads */
        HIGH,
        /** Very high concurrency - extreme thread contention */
        VERY_HIGH
    }

    private final double readToWriteRatio;
    private final AccessPattern accessPattern;
    private final MemoryConstraint memoryConstraint;
    private final ConcurrencyLevel concurrencyLevel;
    private final boolean requiresConsistency;
    private final boolean requiresAsyncOperations;
    private final long expectedSize;
    private final double hitRateExpectation;

    private WorkloadCharacteristics(Builder builder) {
        this.readToWriteRatio = builder.readToWriteRatio;
        this.accessPattern = builder.accessPattern;
        this.memoryConstraint = builder.memoryConstraint;
        this.concurrencyLevel = builder.concurrencyLevel;
        this.requiresConsistency = builder.requiresConsistency;
        this.requiresAsyncOperations = builder.requiresAsyncOperations;
        this.expectedSize = builder.expectedSize;
        this.hitRateExpectation = builder.hitRateExpectation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getReadToWriteRatio() {
        return readToWriteRatio;
    }

    public AccessPattern getAccessPattern() {
        return accessPattern;
    }

    public MemoryConstraint getMemoryConstraint() {
        return memoryConstraint;
    }

    public ConcurrencyLevel getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public boolean isRequiresConsistency() {
        return requiresConsistency;
    }

    public boolean isRequiresAsyncOperations() {
        return requiresAsyncOperations;
    }

    public long getExpectedSize() {
        return expectedSize;
    }

    public double getHitRateExpectation() {
        return hitRateExpectation;
    }

    /**
     * Returns true if this workload is read-heavy (ratio > 5.0).
     */
    public boolean isReadHeavy() {
        return readToWriteRatio > 5.0;
    }

    /**
     * Returns true if this workload is write-heavy (ratio < 1.0).
     */
    public boolean isWriteHeavy() {
        return readToWriteRatio < 1.0;
    }

    /**
     * Returns true if this workload has memory constraints.
     */
    public boolean hasMemoryConstraints() {
        return memoryConstraint != MemoryConstraint.NONE;
    }

    /**
     * Returns true if this workload requires high concurrency support.
     */
    public boolean requiresHighConcurrency() {
        return concurrencyLevel == ConcurrencyLevel.HIGH ||
                concurrencyLevel == ConcurrencyLevel.VERY_HIGH;
    }

    /**
     * Builder for WorkloadCharacteristics.
     */
    public static class Builder {
        private double readToWriteRatio = 1.0;
        private AccessPattern accessPattern = AccessPattern.MIXED;
        private MemoryConstraint memoryConstraint = MemoryConstraint.NONE;
        private ConcurrencyLevel concurrencyLevel = ConcurrencyLevel.MEDIUM;
        private boolean requiresConsistency = false;
        private boolean requiresAsyncOperations = false;
        private long expectedSize = 1000L;
        private double hitRateExpectation = 0.8;

        public Builder readToWriteRatio(double ratio) {
            this.readToWriteRatio = ratio;
            return this;
        }

        public Builder accessPattern(AccessPattern pattern) {
            this.accessPattern = pattern;
            return this;
        }

        public Builder memoryConstraint(MemoryConstraint constraint) {
            this.memoryConstraint = constraint;
            return this;
        }

        public Builder concurrencyLevel(ConcurrencyLevel level) {
            this.concurrencyLevel = level;
            return this;
        }

        public Builder requiresConsistency(boolean required) {
            this.requiresConsistency = required;
            return this;
        }

        public Builder requiresAsyncOperations(boolean required) {
            this.requiresAsyncOperations = required;
            return this;
        }

        public Builder expectedSize(long size) {
            this.expectedSize = size;
            return this;
        }

        public Builder hitRateExpectation(double rate) {
            this.hitRateExpectation = rate;
            return this;
        }

        public WorkloadCharacteristics build() {
            return new WorkloadCharacteristics(this);
        }
    }
}
