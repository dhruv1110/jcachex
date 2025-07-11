package io.github.dhruv1110.jcachex.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * High-performance batch processor for cache operations that reduces contention
 * by batching operations and processing them in chunks.
 * <p>
 * This implementation provides:
 * <ul>
 * <li><strong>Batch Aggregation:</strong> Collects operations before
 * processing</li>
 * <li><strong>Dynamic Batching:</strong> Adjusts batch size based on load</li>
 * <li><strong>Backpressure Handling:</strong> Prevents overwhelming the
 * system</li>
 * <li><strong>Thread Safety:</strong> Lock-free operations for high
 * concurrency</li>
 * <li><strong>Monitoring:</strong> Comprehensive metrics for performance
 * analysis</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of operations being batched
 * @since 1.0.0
 */
public class BatchProcessor<T> {

    // Configuration constants
    private static final int DEFAULT_BATCH_SIZE = 64;
    private static final int DEFAULT_MAX_BATCH_SIZE = 1024;
    private static final int DEFAULT_MIN_BATCH_SIZE = 8;
    private static final long DEFAULT_BATCH_TIMEOUT_NANOS = 1_000_000L; // 1ms
    private static final int DEFAULT_QUEUE_CAPACITY = 8192;

    // Dynamic batching parameters
    private static final double BATCH_SIZE_INCREASE_FACTOR = 1.25;
    private static final double BATCH_SIZE_DECREASE_FACTOR = 0.8;
    private static final int PERFORMANCE_SAMPLE_SIZE = 100;

    // Core components
    private final BlockingQueue<Operation<T>> operationQueue;
    private final Consumer<List<T>> batchProcessor;
    private final int maxBatchSize;
    private final int minBatchSize;
    private final long batchTimeoutNanos;

    // Dynamic state
    private volatile int currentBatchSize;
    private volatile boolean isProcessing;
    private final AtomicBoolean shutdownRequested;
    private final AtomicInteger pendingOperations;

    // Performance tracking
    private final AtomicLong totalBatchesProcessed;
    private final AtomicLong totalOperationsProcessed;
    private final AtomicLong totalProcessingTimeNanos;
    private final AtomicLong adaptationCounter;
    private volatile long lastProcessingTimeNanos;

    /**
     * Creates a new batch processor with default configuration.
     *
     * @param batchProcessor the function to process batches of operations
     */
    public BatchProcessor(Consumer<List<T>> batchProcessor) {
        this(batchProcessor, DEFAULT_BATCH_SIZE, DEFAULT_MAX_BATCH_SIZE,
                DEFAULT_MIN_BATCH_SIZE, DEFAULT_BATCH_TIMEOUT_NANOS, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * Creates a new batch processor with custom configuration.
     *
     * @param batchProcessor    the function to process batches of operations
     * @param initialBatchSize  the initial batch size
     * @param maxBatchSize      the maximum batch size
     * @param minBatchSize      the minimum batch size
     * @param batchTimeoutNanos the maximum time to wait for a batch in nanoseconds
     * @param queueCapacity     the capacity of the operation queue
     */
    public BatchProcessor(Consumer<List<T>> batchProcessor, int initialBatchSize,
            int maxBatchSize, int minBatchSize, long batchTimeoutNanos,
            int queueCapacity) {
        this.batchProcessor = batchProcessor;
        this.currentBatchSize = initialBatchSize;
        this.maxBatchSize = maxBatchSize;
        this.minBatchSize = minBatchSize;
        this.batchTimeoutNanos = batchTimeoutNanos;

        this.operationQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.shutdownRequested = new AtomicBoolean(false);
        this.pendingOperations = new AtomicInteger(0);

        // Initialize metrics
        this.totalBatchesProcessed = new AtomicLong(0);
        this.totalOperationsProcessed = new AtomicLong(0);
        this.totalProcessingTimeNanos = new AtomicLong(0);
        this.adaptationCounter = new AtomicLong(0);
        this.lastProcessingTimeNanos = 0;
        this.isProcessing = false;
    }

    /**
     * Submits an operation for batch processing.
     *
     * @param operation the operation to process
     * @return CompletableFuture that completes when the operation is processed
     * @throws IllegalStateException if the processor is shut down
     */
    public CompletableFuture<Void> submit(T operation) {
        if (shutdownRequested.get()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("BatchProcessor is shut down"));
            return future;
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        Operation<T> op = new Operation<>(operation, future);

        if (!operationQueue.offer(op)) {
            // Queue is full, handle backpressure
            future.completeExceptionally(new IllegalStateException("Operation queue is full"));
            return future;
        }

        pendingOperations.incrementAndGet();

        // Trigger processing if batch size threshold is met
        if (!isProcessing && pendingOperations.get() >= currentBatchSize) {
            triggerProcessing();
        }

        return future;
    }

    /**
     * Forces processing of all pending operations.
     *
     * @return CompletableFuture that completes when all pending operations are
     *         processed
     */
    public CompletableFuture<Void> flush() {
        if (pendingOperations.get() == 0) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(this::processPendingOperations);
    }

    /**
     * Initiates shutdown of the batch processor.
     *
     * @return CompletableFuture that completes when shutdown is complete
     */
    public CompletableFuture<Void> shutdown() {
        shutdownRequested.set(true);

        return CompletableFuture.runAsync(() -> {
            // Process any remaining operations
            processPendingOperations();

            // Cancel any remaining operations
            Operation<T> op;
            while ((op = operationQueue.poll()) != null) {
                op.future.cancel(false);
            }
        });
    }

    /**
     * Returns the current number of pending operations.
     *
     * @return the number of pending operations
     */
    public int getPendingOperationCount() {
        return pendingOperations.get();
    }

    /**
     * Returns the current batch size.
     *
     * @return the current batch size
     */
    public int getCurrentBatchSize() {
        return currentBatchSize;
    }

    /**
     * Returns performance metrics for the batch processor.
     *
     * @return performance metrics
     */
    public BatchProcessorMetrics getMetrics() {
        long totalBatches = totalBatchesProcessed.get();
        long totalOps = totalOperationsProcessed.get();
        long totalTimeNanos = totalProcessingTimeNanos.get();

        double averageBatchSize = totalBatches > 0 ? (double) totalOps / totalBatches : 0.0;
        double averageProcessingTimeNanos = totalBatches > 0 ? (double) totalTimeNanos / totalBatches : 0.0;
        double throughputOpsPerSecond = totalTimeNanos > 0 ? (double) totalOps / (totalTimeNanos / 1_000_000_000.0)
                : 0.0;

        return new BatchProcessorMetrics(
                totalBatches,
                totalOps,
                averageBatchSize,
                averageProcessingTimeNanos,
                throughputOpsPerSecond,
                currentBatchSize,
                pendingOperations.get(),
                operationQueue.remainingCapacity());
    }

    // Private helper methods

    private void triggerProcessing() {
        if (isProcessing) {
            return;
        }

        synchronized (this) {
            if (isProcessing) {
                return;
            }
            isProcessing = true;
        }

        CompletableFuture.runAsync(this::processPendingOperations)
                .whenComplete((result, throwable) -> {
                    synchronized (this) {
                        isProcessing = false;
                    }

                    // Check if more processing is needed
                    if (pendingOperations.get() >= currentBatchSize && !shutdownRequested.get()) {
                        triggerProcessing();
                    }
                });
    }

    private void processPendingOperations() {
        while (pendingOperations.get() > 0 && !shutdownRequested.get()) {
            List<Operation<T>> batch = collectBatch();
            if (batch.isEmpty()) {
                break;
            }

            processBatch(batch);
        }
    }

    private List<Operation<T>> collectBatch() {
        List<Operation<T>> batch = new ArrayList<>(currentBatchSize);
        long deadline = System.nanoTime() + batchTimeoutNanos;

        // Collect operations up to batch size or timeout
        for (int i = 0; i < currentBatchSize && System.nanoTime() < deadline; i++) {
            Operation<T> op = operationQueue.poll();
            if (op == null) {
                break;
            }
            batch.add(op);
            pendingOperations.decrementAndGet();
        }

        return batch;
    }

    private void processBatch(List<Operation<T>> batch) {
        if (batch.isEmpty()) {
            return;
        }

        long startTime = System.nanoTime();

        try {
            // Extract operations for processing
            List<T> operations = new ArrayList<>(batch.size());
            for (Operation<T> op : batch) {
                operations.add(op.operation);
            }

            // Process the batch
            batchProcessor.accept(operations);

            // Complete all futures successfully
            for (Operation<T> op : batch) {
                op.future.complete(null);
            }

        } catch (Exception e) {
            // Complete all futures with the exception
            for (Operation<T> op : batch) {
                op.future.completeExceptionally(e);
            }
        } finally {
            // Update metrics
            long processingTime = System.nanoTime() - startTime;
            lastProcessingTimeNanos = processingTime;
            totalBatchesProcessed.incrementAndGet();
            totalOperationsProcessed.addAndGet(batch.size());
            totalProcessingTimeNanos.addAndGet(processingTime);

            // Adapt batch size based on performance
            adaptBatchSize(batch.size(), processingTime);
        }
    }

    private void adaptBatchSize(int batchSize, long processingTimeNanos) {
        // Only adapt every N batches to avoid over-optimization
        if (adaptationCounter.incrementAndGet() % PERFORMANCE_SAMPLE_SIZE != 0) {
            return;
        }

        // Calculate throughput metrics
        long totalBatches = totalBatchesProcessed.get();
        if (totalBatches < 2) {
            return; // Not enough data to adapt
        }

        double avgProcessingTime = (double) totalProcessingTimeNanos.get() / totalBatches;
        double currentThroughput = batchSize / (processingTimeNanos / 1_000_000_000.0);

        // Increase batch size if processing is efficient
        if (processingTimeNanos < avgProcessingTime * 0.9 && currentBatchSize < maxBatchSize) {
            currentBatchSize = Math.min(maxBatchSize,
                    (int) (currentBatchSize * BATCH_SIZE_INCREASE_FACTOR));
        }
        // Decrease batch size if processing is slow
        else if (processingTimeNanos > avgProcessingTime * 1.1 && currentBatchSize > minBatchSize) {
            currentBatchSize = Math.max(minBatchSize,
                    (int) (currentBatchSize * BATCH_SIZE_DECREASE_FACTOR));
        }
    }

    /**
     * Internal operation wrapper with completion future.
     */
    private static class Operation<T> {
        final T operation;
        final CompletableFuture<Void> future;

        Operation(T operation, CompletableFuture<Void> future) {
            this.operation = operation;
            this.future = future;
        }
    }

    /**
     * Performance metrics for the batch processor.
     */
    public static class BatchProcessorMetrics {
        public final long totalBatchesProcessed;
        public final long totalOperationsProcessed;
        public final double averageBatchSize;
        public final double averageProcessingTimeNanos;
        public final double throughputOpsPerSecond;
        public final int currentBatchSize;
        public final int pendingOperations;
        public final int queueRemainingCapacity;

        public BatchProcessorMetrics(long totalBatchesProcessed, long totalOperationsProcessed,
                double averageBatchSize, double averageProcessingTimeNanos,
                double throughputOpsPerSecond, int currentBatchSize,
                int pendingOperations, int queueRemainingCapacity) {
            this.totalBatchesProcessed = totalBatchesProcessed;
            this.totalOperationsProcessed = totalOperationsProcessed;
            this.averageBatchSize = averageBatchSize;
            this.averageProcessingTimeNanos = averageProcessingTimeNanos;
            this.throughputOpsPerSecond = throughputOpsPerSecond;
            this.currentBatchSize = currentBatchSize;
            this.pendingOperations = pendingOperations;
            this.queueRemainingCapacity = queueRemainingCapacity;
        }

        @Override
        public String toString() {
            return String.format(
                    "BatchProcessorMetrics{batches=%d, operations=%d, avgBatchSize=%.2f, " +
                            "avgProcessingTime=%.2fms, throughput=%.2f ops/sec, currentBatchSize=%d, " +
                            "pending=%d, queueCapacity=%d}",
                    totalBatchesProcessed, totalOperationsProcessed, averageBatchSize,
                    averageProcessingTimeNanos / 1_000_000.0, throughputOpsPerSecond,
                    currentBatchSize, pendingOperations, queueRemainingCapacity);
        }
    }
}
