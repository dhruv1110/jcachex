package io.github.dhruv1110.jcachex.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A collection of striped ring buffers for high-concurrency access recording.
 * <p>
 * This class manages multiple ring buffers, each assigned to specific threads
 * based on their hash. This design minimizes contention by ensuring that
 * threads rarely compete for the same buffer.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Dynamic Striping:</strong> Automatically expands stripes under
 * contention</li>
 * <li><strong>Thread Affinity:</strong> Maps threads to specific stripes</li>
 * <li><strong>Contention Detection:</strong> Monitors and responds to buffer
 * overflow</li>
 * <li><strong>Efficient Draining:</strong> Batch processing of all stripes</li>
 * </ul>
 *
 * @param <E> the type of elements stored in the buffers
 * @since 1.0.0
 */
public class StripedRingBuffer<E> {

    // Initial number of stripes (will grow as needed)
    private static final int INITIAL_STRIPES = 4;
    private static final int MAX_STRIPES = 64;

    private volatile RingBuffer<E>[] buffers;
    private volatile int stripeMask;
    private final AtomicLong contentionCounter;

    /**
     * Creates a new striped ring buffer.
     */
    @SuppressWarnings("unchecked")
    public StripedRingBuffer() {
        this.buffers = new RingBuffer[INITIAL_STRIPES];
        for (int i = 0; i < INITIAL_STRIPES; i++) {
            buffers[i] = new RingBuffer<>();
        }
        this.stripeMask = INITIAL_STRIPES - 1;
        this.contentionCounter = new AtomicLong(0);
    }

    /**
     * Records an access for the specified element.
     *
     * @param element the element being accessed
     * @return true if the access was recorded
     */
    public boolean recordAccess(E element) {
        if (element == null) {
            return false;
        }

        // Get stripe for current thread
        int stripe = getStripeForCurrentThread();
        RingBuffer<E> buffer = buffers[stripe];

        boolean success = buffer.offer(element);

        if (!success) {
            // Buffer is full, track contention
            contentionCounter.incrementAndGet();

            // Consider expanding stripes if contention is high
            considerExpansion();
        }

        return success;
    }

    /**
     * Drains all buffers and processes the elements.
     *
     * @param processor the function to process each element
     * @return the total number of elements processed
     */
    public int drainAll(java.util.function.Consumer<E> processor) {
        int totalProcessed = 0;
        RingBuffer<E>[] currentBuffers = buffers; // Snapshot

        for (RingBuffer<E> buffer : currentBuffers) {
            totalProcessed += buffer.drain(processor);
        }

        return totalProcessed;
    }

    /**
     * Gets the stripe index for the current thread.
     *
     * @return the stripe index
     */
    private int getStripeForCurrentThread() {
        // Use thread-specific hash to determine stripe
        Thread currentThread = Thread.currentThread();
        int hash = currentThread.hashCode();

        // Apply additional hashing to improve distribution
        hash ^= hash >>> 16;
        hash ^= hash >>> 8;

        return hash & stripeMask;
    }

    /**
     * Considers expanding the number of stripes based on contention.
     */
    @SuppressWarnings("unchecked")
    private void considerExpansion() {
        long contention = contentionCounter.get();
        int currentStripes = buffers.length;

        // Expand if we see significant contention and haven't reached max
        if (contention > currentStripes * 10 && currentStripes < MAX_STRIPES) {
            synchronized (this) {
                // Double-check under lock
                if (buffers.length == currentStripes && currentStripes < MAX_STRIPES) {
                    int newStripeCount = Math.min(currentStripes * 2, MAX_STRIPES);
                    RingBuffer<E>[] newBuffers = new RingBuffer[newStripeCount];

                    // Copy existing buffers
                    System.arraycopy(buffers, 0, newBuffers, 0, currentStripes);

                    // Initialize new buffers
                    for (int i = currentStripes; i < newStripeCount; i++) {
                        newBuffers[i] = new RingBuffer<>();
                    }

                    // Update references atomically
                    stripeMask = newStripeCount - 1;
                    buffers = newBuffers;

                    // Reset contention counter
                    contentionCounter.set(0);
                }
            }
        }
    }

    /**
     * Returns the current number of stripes.
     *
     * @return the stripe count
     */
    public int getStripeCount() {
        return buffers.length;
    }

    /**
     * Returns the current contention level.
     *
     * @return the contention counter value
     */
    public long getContentionLevel() {
        return contentionCounter.get();
    }

    /**
     * Checks if any buffer needs draining.
     *
     * @return true if draining is needed
     */
    public boolean needsDraining() {
        RingBuffer<E>[] currentBuffers = buffers; // Snapshot

        for (RingBuffer<E> buffer : currentBuffers) {
            if (buffer.getDrainStatus() == RingBuffer.DrainStatus.REQUIRED) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the total size across all buffers.
     *
     * @return the total number of elements
     */
    public int totalSize() {
        int total = 0;
        RingBuffer<E>[] currentBuffers = buffers; // Snapshot

        for (RingBuffer<E> buffer : currentBuffers) {
            total += buffer.size();
        }

        return total;
    }

    /**
     * Clears all buffers.
     */
    public void clear() {
        RingBuffer<E>[] currentBuffers = buffers; // Snapshot

        for (RingBuffer<E> buffer : currentBuffers) {
            buffer.clear();
        }

        contentionCounter.set(0);
    }
}
