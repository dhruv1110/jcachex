package io.github.dhruv1110.jcachex.concurrent;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A striped ring buffer for lock-free access recording in cache operations.
 * <p>
 * This implementation provides high-throughput access recording by using
 * multiple ring buffers (stripes) to reduce contention. Each thread maps
 * to a specific stripe based on its hash, distributing the load evenly.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Lock-Free Writes:</strong> Uses CAS operations for buffer
 * updates</li>
 * <li><strong>Striped Architecture:</strong> Multiple buffers reduce
 * contention</li>
 * <li><strong>Overflow Handling:</strong> Graceful degradation when buffers are
 * full</li>
 * <li><strong>Memory Efficient:</strong> Compact representation of buffer
 * entries</li>
 * </ul>
 *
 * @param <E> the type of elements stored in the buffer
 * @since 1.0.0
 */
public class RingBuffer<E> {

    // Ring buffer configuration (instance-scoped for adaptive sizing)
    private final int bufferSize; // power of two
    private final int bufferMask;

    // Ring buffer for storing elements
    private final AtomicReferenceArray<E> buffer;
    private final AtomicLong writeSequence;
    private final AtomicLong readSequence;

    // Drain coordination
    private final AtomicReference<DrainStatus> drainStatus;

    /**
     * Drain status for coordinating buffer maintenance.
     */
    public enum DrainStatus {
        IDLE,
        PROCESSING,
        REQUIRED
    }

    /**
     * Creates a new ring buffer.
     */
    public RingBuffer() {
        // Adaptive default: cores * 32, rounded up to next power of two, min 64
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        int target = Math.max(64, cores * 32);
        int pow2 = 1;
        while (pow2 < target) {
            pow2 <<= 1;
        }
        this.bufferSize = pow2;
        this.bufferMask = this.bufferSize - 1;

        this.buffer = new AtomicReferenceArray<>(this.bufferSize);
        this.writeSequence = new AtomicLong(0);
        this.readSequence = new AtomicLong(0);
        this.drainStatus = new AtomicReference<>(DrainStatus.IDLE);
    }

    /**
     * Attempts to add an element to the buffer.
     *
     * @param element the element to add
     * @return true if the element was added, false if the buffer is full
     */
    public boolean offer(E element) {
        if (element == null) {
            return false;
        }

        long currentWrite = writeSequence.get();
        long currentRead = readSequence.get();

        // Check if buffer is full
        if (currentWrite - currentRead >= bufferSize) {
            // Buffer is full, signal drain needed
            drainStatus.compareAndSet(DrainStatus.IDLE, DrainStatus.REQUIRED);
            return false;
        }

        // Try to reserve a slot
        if (writeSequence.compareAndSet(currentWrite, currentWrite + 1)) {
            int index = (int) (currentWrite & bufferMask);
            buffer.set(index, element);
            return true;
        }

        return false; // Another thread got the slot
    }

    /**
     * Drains elements from the buffer and processes them.
     *
     * @param processor the function to process each element
     * @return the number of elements processed
     */
    public int drain(java.util.function.Consumer<E> processor) {
        if (!drainStatus.compareAndSet(DrainStatus.REQUIRED, DrainStatus.PROCESSING) &&
                !drainStatus.compareAndSet(DrainStatus.IDLE, DrainStatus.PROCESSING)) {
            return 0; // Another thread is already draining
        }

        try {
            long currentRead = readSequence.get();
            long currentWrite = writeSequence.get();

            int processed = 0;
            while (currentRead < currentWrite && processed < bufferSize) {
                int index = (int) (currentRead & bufferMask);
                E element = buffer.get(index);

                if (element != null) {
                    processor.accept(element);
                    buffer.set(index, null); // Clear slot
                    processed++;
                }

                currentRead++;
            }

            // Update read sequence
            readSequence.set(currentRead);

            return processed;
        } finally {
            drainStatus.set(DrainStatus.IDLE);
        }
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return the number of elements in the buffer
     */
    public int size() {
        long write = writeSequence.get();
        long read = readSequence.get();
        return Math.max(0, (int) (write - read));
    }

    /**
     * Checks if the buffer is empty.
     *
     * @return true if the buffer is empty
     */
    public boolean isEmpty() {
        return writeSequence.get() == readSequence.get();
    }

    /**
     * Checks if the buffer is full.
     *
     * @return true if the buffer is full
     */
    public boolean isFull() {
        return size() >= bufferSize;
    }

    /**
     * Gets the current drain status.
     *
     * @return the drain status
     */
    public DrainStatus getDrainStatus() {
        return drainStatus.get();
    }

    /**
     * Clears all elements from the buffer.
     */
    public void clear() {
        for (int i = 0; i < bufferSize; i++) {
            buffer.set(i, null);
        }
        readSequence.set(writeSequence.get());
        drainStatus.set(DrainStatus.IDLE);
    }
}
