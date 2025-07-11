package io.github.dhruv1110.jcachex.timing;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Hierarchical timer wheel for efficient scheduling of time-based operations.
 * <p>
 * This implementation provides O(1) insertion and deletion of timers by using
 * multiple wheels operating at different time granularities:
 * <ul>
 * <li><strong>Wheel 0:</strong> 1-second granularity (256 slots)</li>
 * <li><strong>Wheel 1:</strong> 4-minute granularity (64 slots)</li>
 * <li><strong>Wheel 2:</strong> 4-hour granularity (64 slots)</li>
 * <li><strong>Wheel 3:</strong> 10-day granularity (64 slots)</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of tasks being scheduled
 * @since 1.0.0
 */
public class HierarchicalTimerWheel<T> {

    // Wheel configuration
    private static final int WHEEL_COUNT = 4;
    private static final int[] WHEEL_SIZES = { 256, 64, 64, 64 };
    private static final long[] WHEEL_GRANULARITIES = { 1000L, 256000L, 16384000L, 1073741824L }; // milliseconds

    // Timing configuration
    private static final long TICK_DURATION_MS = 100L; // 100ms tick interval
    private static final int MAX_PENDING_TASKS = 10000;

    // Timer wheels - each wheel operates at different granularity
    private final TimerWheel[] wheels;

    // Current time tracking
    private final AtomicLong currentTimeMs;
    private final AtomicLong tickCounter;

    // Task execution
    private final Consumer<T> taskExecutor;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentLinkedQueue<ScheduledTask<T>> pendingTasks;

    // Performance tracking
    private final AtomicLong totalTasksScheduled;
    private final AtomicLong totalTasksExecuted;
    private final AtomicLong totalTicksProcessed;

    // State management
    private final AtomicReference<State> state;
    private volatile boolean isRunning;

    private enum State {
        CREATED, STARTED, STOPPED
    }

    /**
     * Creates a new hierarchical timer wheel.
     *
     * @param taskExecutor the function to execute scheduled tasks
     */
    public HierarchicalTimerWheel(Consumer<T> taskExecutor) {
        this.taskExecutor = taskExecutor;
        this.currentTimeMs = new AtomicLong(System.currentTimeMillis());
        this.tickCounter = new AtomicLong(0);
        this.pendingTasks = new ConcurrentLinkedQueue<>();

        // Initialize wheels
        this.wheels = new TimerWheel[WHEEL_COUNT];
        for (int i = 0; i < WHEEL_COUNT; i++) {
            this.wheels[i] = new TimerWheel(WHEEL_SIZES[i], WHEEL_GRANULARITIES[i]);
        }

        // Initialize scheduler
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "jcachex-timer-wheel");
            thread.setDaemon(true);
            return thread;
        });

        // Initialize metrics
        this.totalTasksScheduled = new AtomicLong(0);
        this.totalTasksExecuted = new AtomicLong(0);
        this.totalTicksProcessed = new AtomicLong(0);

        this.state = new AtomicReference<>(State.CREATED);
        this.isRunning = false;
    }

    /**
     * Starts the timer wheel.
     *
     * @return CompletableFuture that completes when the timer wheel is started
     */
    public CompletableFuture<Void> start() {
        if (!state.compareAndSet(State.CREATED, State.STARTED)) {
            return CompletableFuture.completedFuture(null);
        }

        isRunning = true;

        // Schedule periodic tick processing
        scheduler.scheduleAtFixedRate(this::processTick, 0, TICK_DURATION_MS, TimeUnit.MILLISECONDS);

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Stops the timer wheel.
     *
     * @return CompletableFuture that completes when the timer wheel is stopped
     */
    public CompletableFuture<Void> stop() {
        if (!state.compareAndSet(State.STARTED, State.STOPPED)) {
            return CompletableFuture.completedFuture(null);
        }

        isRunning = false;

        return CompletableFuture.runAsync(() -> {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Schedules a task to be executed after the specified delay.
     *
     * @param task  the task to execute
     * @param delay the delay before execution
     * @return a scheduled task handle
     */
    public ScheduledTask<T> schedule(T task, Duration delay) {
        return schedule(task, delay.toMillis());
    }

    /**
     * Schedules a task to be executed after the specified delay.
     *
     * @param task    the task to execute
     * @param delayMs the delay in milliseconds
     * @return a scheduled task handle
     */
    public ScheduledTask<T> schedule(T task, long delayMs) {
        if (!isRunning) {
            throw new IllegalStateException("Timer wheel is not running");
        }

        if (pendingTasks.size() >= MAX_PENDING_TASKS) {
            throw new IllegalStateException("Too many pending tasks");
        }

        long executeTime = currentTimeMs.get() + delayMs;
        ScheduledTask<T> scheduledTask = new ScheduledTask<>(task, executeTime);

        // Add to appropriate wheel
        insertTask(scheduledTask);

        totalTasksScheduled.incrementAndGet();
        return scheduledTask;
    }

    /**
     * Cancels a scheduled task.
     *
     * @param scheduledTask the task to cancel
     * @return true if the task was cancelled, false if it was already executed or
     *         cancelled
     */
    public boolean cancel(ScheduledTask<T> scheduledTask) {
        return scheduledTask.cancel();
    }

    /**
     * Returns the current time as tracked by the timer wheel.
     *
     * @return the current time in milliseconds
     */
    public long getCurrentTime() {
        return currentTimeMs.get();
    }

    /**
     * Returns performance metrics for the timer wheel.
     *
     * @return performance metrics
     */
    public TimerWheelMetrics getMetrics() {
        int totalPendingTasks = 0;
        for (TimerWheel wheel : wheels) {
            totalPendingTasks += wheel.getTaskCount();
        }
        totalPendingTasks += pendingTasks.size();

        return new TimerWheelMetrics(
                totalTasksScheduled.get(),
                totalTasksExecuted.get(),
                totalTicksProcessed.get(),
                totalPendingTasks,
                tickCounter.get(),
                currentTimeMs.get(),
                isRunning);
    }

    // Private helper methods

    private void processTick() {
        if (!isRunning) {
            return;
        }

        long startTime = System.nanoTime();

        try {
            // Update current time
            currentTimeMs.set(System.currentTimeMillis());
            long tick = tickCounter.incrementAndGet();

            // Process wheel 0 (most frequent)
            processWheel(0, tick);

            // Process higher-level wheels at appropriate intervals
            for (int i = 1; i < WHEEL_COUNT; i++) {
                if (tick % getWheelInterval(i) == 0) {
                    processWheel(i, tick);
                }
            }

            // Execute ready tasks
            executeReadyTasks();

            totalTicksProcessed.incrementAndGet();

        } catch (Exception e) {
            // Log error but continue processing
            System.err.println("Error processing timer wheel tick: " + e.getMessage());
        }
    }

    private void processWheel(int wheelIndex, long tick) {
        TimerWheel wheel = wheels[wheelIndex];
        int slot = (int) (tick % wheel.size);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<ScheduledTask<T>> tasks = (List) wheel.getTasks(slot);
        if (tasks != null && !tasks.isEmpty()) {
            for (ScheduledTask<T> task : tasks) {
                if (!task.isCancelled()) {
                    long timeRemaining = task.executeTime - currentTimeMs.get();

                    if (timeRemaining <= 0) {
                        // Task is ready to execute
                        pendingTasks.offer(task);
                    } else if (wheelIndex > 0) {
                        // Move to lower-level wheel
                        insertTaskIntoWheel(task, wheelIndex - 1);
                    }
                }
            }
            tasks.clear();
        }
    }

    private void executeReadyTasks() {
        ScheduledTask<T> task;
        int executed = 0;

        // Execute ready tasks (limit to avoid blocking)
        while ((task = pendingTasks.poll()) != null && executed < 100) {
            if (!task.isCancelled() && task.executeTime <= currentTimeMs.get()) {
                try {
                    taskExecutor.accept(task.task);
                    task.markExecuted();
                    totalTasksExecuted.incrementAndGet();
                    executed++;
                } catch (Exception e) {
                    // Log error but continue
                    System.err.println("Error executing task: " + e.getMessage());
                }
            }
        }
    }

    private void insertTask(ScheduledTask<T> task) {
        long delay = task.executeTime - currentTimeMs.get();

        // Find appropriate wheel based on delay
        for (int i = 0; i < WHEEL_COUNT; i++) {
            if (delay < WHEEL_GRANULARITIES[i] * WHEEL_SIZES[i]) {
                insertTaskIntoWheel(task, i);
                return;
            }
        }

        // If delay is too large, put in the highest wheel
        insertTaskIntoWheel(task, WHEEL_COUNT - 1);
    }

    private void insertTaskIntoWheel(ScheduledTask<T> task, int wheelIndex) {
        TimerWheel wheel = wheels[wheelIndex];
        long delay = task.executeTime - currentTimeMs.get();
        long granularity = WHEEL_GRANULARITIES[wheelIndex];

        int slot = (int) ((delay / granularity + tickCounter.get()) % wheel.size);
        wheel.addTask(slot, task);
    }

    private long getWheelInterval(int wheelIndex) {
        // How often to process each wheel level
        return WHEEL_SIZES[wheelIndex - 1];
    }

    /**
     * Individual timer wheel operating at a specific granularity.
     */
    private static class TimerWheel {
        private final int size;
        private final long granularity;
        private final List<ScheduledTask<?>>[] slots;
        private final AtomicLong taskCount;

        @SuppressWarnings("unchecked")
        public TimerWheel(int size, long granularity) {
            this.size = size;
            this.granularity = granularity;
            this.slots = new List[size];
            this.taskCount = new AtomicLong(0);

            for (int i = 0; i < size; i++) {
                slots[i] = new ArrayList<>();
            }
        }

        public void addTask(int slot, ScheduledTask<?> task) {
            synchronized (slots[slot]) {
                slots[slot].add(task);
                taskCount.incrementAndGet();
            }
        }

        public List<ScheduledTask<?>> getTasks(int slot) {
            synchronized (slots[slot]) {
                List<ScheduledTask<?>> tasks = new ArrayList<>(slots[slot]);
                int removedCount = slots[slot].size();
                slots[slot].clear();
                taskCount.addAndGet(-removedCount);
                return tasks;
            }
        }

        public long getTaskCount() {
            return taskCount.get();
        }
    }

    /**
     * A scheduled task with execution time and cancellation support.
     */
    public static class ScheduledTask<T> {
        private final T task;
        private final long executeTime;
        private volatile boolean cancelled;
        private volatile boolean executed;

        public ScheduledTask(T task, long executeTime) {
            this.task = task;
            this.executeTime = executeTime;
            this.cancelled = false;
            this.executed = false;
        }

        public T getTask() {
            return task;
        }

        public long getExecuteTime() {
            return executeTime;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public boolean isExecuted() {
            return executed;
        }

        public boolean cancel() {
            if (executed) {
                return false;
            }
            cancelled = true;
            return true;
        }

        void markExecuted() {
            executed = true;
        }

        @Override
        public String toString() {
            return String.format("ScheduledTask{task=%s, executeTime=%d, cancelled=%b, executed=%b}",
                    task, executeTime, cancelled, executed);
        }
    }

    /**
     * Performance metrics for the timer wheel.
     */
    public static class TimerWheelMetrics {
        public final long totalTasksScheduled;
        public final long totalTasksExecuted;
        public final long totalTicksProcessed;
        public final int pendingTasks;
        public final long currentTick;
        public final long currentTime;
        public final boolean isRunning;

        public TimerWheelMetrics(long totalTasksScheduled, long totalTasksExecuted,
                long totalTicksProcessed, int pendingTasks,
                long currentTick, long currentTime, boolean isRunning) {
            this.totalTasksScheduled = totalTasksScheduled;
            this.totalTasksExecuted = totalTasksExecuted;
            this.totalTicksProcessed = totalTicksProcessed;
            this.pendingTasks = pendingTasks;
            this.currentTick = currentTick;
            this.currentTime = currentTime;
            this.isRunning = isRunning;
        }

        @Override
        public String toString() {
            return String.format(
                    "TimerWheelMetrics{scheduled=%d, executed=%d, ticks=%d, pending=%d, " +
                            "currentTick=%d, currentTime=%d, running=%b}",
                    totalTasksScheduled, totalTasksExecuted, totalTicksProcessed,
                    pendingTasks, currentTick, currentTime, isRunning);
        }
    }
}
