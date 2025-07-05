package io.github.dhruv1110.jcachex.warming;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Result of a cache warming operation.
 * <p>
 * This class provides comprehensive information about the warming process,
 * including success/failure status, timing information, and any errors
 * that occurred during warming.
 * </p>
 *
 * <h3>Usage Example:</h3>
 *
 * <pre>{@code
 * WarmingResult result = cachingStrategy.warmCache(cache, loader).join();
 *
 * if (result.isSuccess()) {
 *     System.out.println("Warmed " + result.getSuccessCount() + " entries in " +
 *             result.getDuration().toMillis() + "ms");
 * } else {
 *     System.err.println("Warming failed: " + result.getFailureReason());
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class WarmingResult {

    private final boolean success;
    private final int successCount;
    private final int failureCount;
    private final Duration duration;
    private final Instant startTime;
    private final Instant endTime;
    private final Optional<String> failureReason;
    private final List<String> warnings;

    private WarmingResult(Builder builder) {
        this.success = builder.success;
        this.successCount = builder.successCount;
        this.failureCount = builder.failureCount;
        this.duration = builder.duration;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.failureReason = Optional.ofNullable(builder.failureReason);
        this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
    }

    /**
     * Returns true if the warming operation was successful.
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the number of successfully warmed entries.
     *
     * @return success count
     */
    public int getSuccessCount() {
        return successCount;
    }

    /**
     * Returns the number of failed warming attempts.
     *
     * @return failure count
     */
    public int getFailureCount() {
        return failureCount;
    }

    /**
     * Returns the total duration of the warming operation.
     *
     * @return warming duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Returns the start time of the warming operation.
     *
     * @return start time
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Returns the end time of the warming operation.
     *
     * @return end time
     */
    public Instant getEndTime() {
        return endTime;
    }

    /**
     * Returns the failure reason if warming failed.
     *
     * @return failure reason if present
     */
    public Optional<String> getFailureReason() {
        return failureReason;
    }

    /**
     * Returns any warnings generated during warming.
     *
     * @return list of warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Creates a new builder for WarmingResult.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a successful warming result.
     *
     * @param successCount number of successfully warmed entries
     * @param duration     warming duration
     * @return successful warming result
     */
    public static WarmingResult success(int successCount, Duration duration) {
        return builder()
                .success(true)
                .successCount(successCount)
                .duration(duration)
                .build();
    }

    /**
     * Creates a failed warming result.
     *
     * @param failureReason reason for failure
     * @param duration      warming duration
     * @return failed warming result
     */
    public static WarmingResult failure(String failureReason, Duration duration) {
        return builder()
                .success(false)
                .failureReason(failureReason)
                .duration(duration)
                .build();
    }

    /**
     * Builder for WarmingResult.
     */
    public static class Builder {
        private boolean success = false;
        private int successCount = 0;
        private int failureCount = 0;
        private Duration duration = Duration.ZERO;
        private Instant startTime = Instant.now();
        private Instant endTime = Instant.now();
        private String failureReason;
        private List<String> warnings = Collections.emptyList();

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder successCount(int successCount) {
            this.successCount = successCount;
            return this;
        }

        public Builder failureCount(int failureCount) {
            this.failureCount = failureCount;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public WarmingResult build() {
            return new WarmingResult(this);
        }
    }
}
