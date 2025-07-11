package io.github.dhruv1110.jcachex;

/**
 * Enumeration of frequency sketch types available for eviction strategies.
 * <p>
 * Frequency sketches are used to track access patterns efficiently in cache
 * eviction strategies like LRU, LFU, and adaptive eviction. Different types
 * provide different trade-offs between memory usage, accuracy, and performance.
 * </p>
 *
 * @since 1.0.0
 */
public enum FrequencySketchType {
    /**
     * No frequency sketch - simple counting or no frequency tracking.
     * <p>
     * <strong>Use Case:</strong> When memory is extremely limited or when
     * frequency tracking is not needed.
     * </p>
     * <ul>
     * <li><strong>Memory:</strong> Minimal - no frequency data structures</li>
     * <li><strong>Accuracy:</strong> Basic - no frequency information</li>
     * <li><strong>Performance:</strong> Fastest - no frequency operations</li>
     * </ul>
     */
    NONE,

    /**
     * Basic frequency sketch using CountMinSketch with standard parameters.
     * <p>
     * <strong>Use Case:</strong> General-purpose frequency tracking with
     * balanced memory usage and accuracy.
     * </p>
     * <ul>
     * <li><strong>Memory:</strong> Moderate - uses standard CountMinSketch</li>
     * <li><strong>Accuracy:</strong> Good - suitable for most workloads</li>
     * <li><strong>Performance:</strong> Fast - efficient hash operations</li>
     * </ul>
     */
    BASIC,

    /**
     * Optimized frequency sketch with advanced features and optimizations.
     * <p>
     * <strong>Use Case:</strong> High-performance scenarios where accuracy
     * and memory efficiency are both important.
     * </p>
     * <ul>
     * <li><strong>Memory:</strong> Optimized - uses advanced data structures</li>
     * <li><strong>Accuracy:</strong> Excellent - includes doorkeeper and aging</li>
     * <li><strong>Performance:</strong> Very fast - highly optimized
     * operations</li>
     * </ul>
     */
    OPTIMIZED
}
