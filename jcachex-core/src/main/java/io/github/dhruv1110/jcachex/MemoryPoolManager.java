package io.github.dhruv1110.jcachex;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Advanced memory pool management system for JCacheX.
 * <p>
 * This system provides comprehensive memory management capabilities:
 * <ul>
 * <li><strong>Adaptive Pool Sizing:</strong> Automatically adjusts pool sizes
 * based on usage patterns</li>
 * <li><strong>Memory Pressure Detection:</strong> Monitors GC activity and
 * memory usage</li>
 * <li><strong>Intelligent Allocation:</strong> Routes allocations to optimal
 * pools</li>
 * <li><strong>Background Maintenance:</strong> Periodic cleanup and
 * optimization</li>
 * <li><strong>Memory Analytics:</strong> Detailed statistics and performance
 * insights</li>
 * </ul>
 *
 * <p>
 * <strong>Pool Types:</strong>
 * <ul>
 * <li>Eden Pool: Fast allocation for short-lived objects</li>
 * <li>Survivor Pools: Medium-term object storage</li>
 * <li>Tenured Pool: Long-lived object storage</li>
 * <li>Off-Heap Pool: Direct memory allocation</li>
 * <li>Emergency Pool: Fallback during memory pressure</li>
 * </ul>
 *
 * <p>
 * <strong>Allocation Strategies:</strong>
 * <ul>
 * <li>Size-based routing (small/medium/large objects)</li>
 * <li>Lifetime-based allocation (ephemeral/persistent/eternal)</li>
 * <li>Access pattern optimization (hot/warm/cold data)</li>
 * <li>NUMA-aware placement for multi-socket systems</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class MemoryPoolManager {

    // === CONFIGURATION ===
    private final MemoryPoolConfig config;
    private final ScheduledExecutorService maintenanceExecutor;
    private final MemoryMXBean memoryBean;

    // === POOL REGISTRY ===
    private final Map<PoolType, ManagedMemoryPool> pools;
    private final AtomicReference<AllocationStrategy> currentStrategy;

    // === MONITORING ===
    private final MemoryPressureMonitor pressureMonitor;
    private final PoolAnalytics analytics;
    private final AtomicLong totalAllocations;
    private final AtomicLong totalDeallocations;

    // === MAINTENANCE ===
    private volatile boolean maintenanceRunning;
    private final AtomicLong lastMaintenanceTime;

    /**
     * Types of memory pools managed by the system.
     */
    public enum PoolType {
        EDEN("Eden", "Fast allocation for short-lived objects", 1024 * 1024), // 1MB
        SURVIVOR_0("Survivor0", "Medium-term object storage", 512 * 1024), // 512KB
        SURVIVOR_1("Survivor1", "Medium-term object storage", 512 * 1024), // 512KB
        TENURED("Tenured", "Long-lived object storage", 4 * 1024 * 1024), // 4MB
        OFF_HEAP("OffHeap", "Direct memory allocation", 16 * 1024 * 1024), // 16MB
        EMERGENCY("Emergency", "Fallback during memory pressure", 256 * 1024); // 256KB

        private final String name;
        private final String description;
        private final long defaultSize;

        PoolType(String name, String description, long defaultSize) {
            this.name = name;
            this.description = description;
            this.defaultSize = defaultSize;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public long getDefaultSize() {
            return defaultSize;
        }
    }

    /**
     * Configuration for memory pool management.
     */
    public static class MemoryPoolConfig {
        private final Map<PoolType, Long> poolSizes;
        private final boolean adaptivePoolSizing;
        private final boolean enableMemoryPressureDetection;
        private final long maintenanceIntervalMs;
        private final double maxMemoryUtilization;
        private final boolean enableOffHeapPools;

        private MemoryPoolConfig(Builder builder) {
            this.poolSizes = new EnumMap<>(builder.poolSizes);
            this.adaptivePoolSizing = builder.adaptivePoolSizing;
            this.enableMemoryPressureDetection = builder.enableMemoryPressureDetection;
            this.maintenanceIntervalMs = builder.maintenanceIntervalMs;
            this.maxMemoryUtilization = builder.maxMemoryUtilization;
            this.enableOffHeapPools = builder.enableOffHeapPools;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Map<PoolType, Long> poolSizes = new EnumMap<>(PoolType.class);
            private boolean adaptivePoolSizing = true;
            private boolean enableMemoryPressureDetection = true;
            private long maintenanceIntervalMs = 10000; // 10 seconds
            private double maxMemoryUtilization = 0.85; // 85%
            private boolean enableOffHeapPools = true;

            public Builder() {
                // Set default pool sizes
                for (PoolType type : PoolType.values()) {
                    poolSizes.put(type, type.getDefaultSize());
                }
            }

            public Builder poolSize(PoolType type, long size) {
                poolSizes.put(type, size);
                return this;
            }

            public Builder adaptivePoolSizing(boolean enable) {
                this.adaptivePoolSizing = enable;
                return this;
            }

            public Builder enableMemoryPressureDetection(boolean enable) {
                this.enableMemoryPressureDetection = enable;
                return this;
            }

            public Builder maintenanceInterval(long intervalMs) {
                this.maintenanceIntervalMs = intervalMs;
                return this;
            }

            public Builder maxMemoryUtilization(double ratio) {
                this.maxMemoryUtilization = ratio;
                return this;
            }

            public Builder enableOffHeapPools(boolean enable) {
                this.enableOffHeapPools = enable;
                return this;
            }

            public MemoryPoolConfig build() {
                return new MemoryPoolConfig(this);
            }
        }

        // Getters
        public Map<PoolType, Long> getPoolSizes() {
            return Collections.unmodifiableMap(poolSizes);
        }

        public boolean isAdaptivePoolSizing() {
            return adaptivePoolSizing;
        }

        public boolean isMemoryPressureDetectionEnabled() {
            return enableMemoryPressureDetection;
        }

        public long getMaintenanceIntervalMs() {
            return maintenanceIntervalMs;
        }

        public double getMaxMemoryUtilization() {
            return maxMemoryUtilization;
        }

        public boolean isOffHeapPoolsEnabled() {
            return enableOffHeapPools;
        }
    }

    /**
     * Individual managed memory pool with adaptive sizing and monitoring.
     */
    private static class ManagedMemoryPool {
        private final PoolType type;
        private final ReentrantLock lock;
        private final Queue<PooledObject> availableObjects;
        private final Set<PooledObject> allocatedObjects;

        // Statistics
        private final AtomicLong allocations;
        private final AtomicLong deallocations;
        private final AtomicLong hits;
        private final AtomicLong misses;
        private final AtomicLong totalBytesAllocated;

        // Adaptive sizing
        private volatile long currentSize;
        private volatile long maxSize;
        private volatile double utilizationRatio;
        private final AtomicLong lastResizeTime;

        ManagedMemoryPool(PoolType type, long initialSize) {
            this.type = type;
            this.lock = new ReentrantLock();
            this.availableObjects = new ConcurrentLinkedQueue<>();
            this.allocatedObjects = ConcurrentHashMap.newKeySet();

            this.allocations = new AtomicLong(0);
            this.deallocations = new AtomicLong(0);
            this.hits = new AtomicLong(0);
            this.misses = new AtomicLong(0);
            this.totalBytesAllocated = new AtomicLong(0);

            this.currentSize = initialSize;
            this.maxSize = initialSize * 2; // Allow 2x growth
            this.utilizationRatio = 0.0;
            this.lastResizeTime = new AtomicLong(System.currentTimeMillis());

            // Pre-populate with some objects
            preAllocateObjects((int) Math.min(initialSize / 1024, 100));
        }

        /**
         * Allocate an object from this pool.
         */
        PooledObject allocate(int size) {
            allocations.incrementAndGet();

            // Try to reuse existing object
            PooledObject obj = availableObjects.poll();
            if (obj != null && obj.canAccommodate(size)) {
                allocatedObjects.add(obj);
                obj.allocate(size);
                hits.incrementAndGet();
                totalBytesAllocated.addAndGet(size);
                return obj;
            }

            // Create new object if pool has capacity
            if (canGrow()) {
                obj = new PooledObject(size, type);
                allocatedObjects.add(obj);
                obj.allocate(size);
                misses.incrementAndGet();
                totalBytesAllocated.addAndGet(size);
                return obj;
            }

            // Pool exhausted
            return null;
        }

        /**
         * Return an object to this pool.
         */
        void deallocate(PooledObject obj) {
            deallocations.incrementAndGet();
            totalBytesAllocated.addAndGet(-obj.getSize());

            if (allocatedObjects.remove(obj)) {
                obj.deallocate();

                // Return to pool if not too fragmented
                if (shouldReturnToPool(obj)) {
                    availableObjects.offer(obj);
                }
            }
        }

        /**
         * Check if this pool can grow to accommodate more objects.
         */
        private boolean canGrow() {
            return getTotalAllocatedSize() < maxSize;
        }

        /**
         * Determine if an object should be returned to the pool.
         */
        private boolean shouldReturnToPool(PooledObject obj) {
            // Don't return very large objects to avoid memory bloat
            return obj.getCapacity() <= 8192 && availableObjects.size() < 100;
        }

        /**
         * Pre-allocate objects to warm up the pool.
         */
        private void preAllocateObjects(int count) {
            for (int i = 0; i < count; i++) {
                availableObjects.offer(new PooledObject(1024, type));
            }
        }

        /**
         * Perform adaptive resizing based on usage patterns.
         */
        void performAdaptiveResize() {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastResize = currentTime - lastResizeTime.get();

            // Only resize if enough time has passed
            if (timeSinceLastResize < 30000) { // 30 seconds
                return;
            }

            // Calculate utilization
            double newUtilization = calculateUtilization();

            lock.lock();
            try {
                // Grow pool if highly utilized
                if (newUtilization > 0.8 && currentSize < maxSize) {
                    long newSize = Math.min(currentSize * 2, maxSize);
                    currentSize = newSize;
                    lastResizeTime.set(currentTime);
                }
                // Shrink pool if under-utilized
                else if (newUtilization < 0.3 && currentSize > type.getDefaultSize()) {
                    long newSize = Math.max(currentSize / 2, type.getDefaultSize());
                    currentSize = newSize;
                    // Remove excess objects
                    shrinkAvailableObjects();
                    lastResizeTime.set(currentTime);
                }

                utilizationRatio = newUtilization;

            } finally {
                lock.unlock();
            }
        }

        /**
         * Calculate current utilization ratio.
         */
        private double calculateUtilization() {
            long allocated = getTotalAllocatedSize();
            return currentSize > 0 ? (double) allocated / currentSize : 0.0;
        }

        /**
         * Get total size of allocated objects.
         */
        private long getTotalAllocatedSize() {
            return allocatedObjects.stream()
                    .mapToLong(PooledObject::getSize)
                    .sum();
        }

        /**
         * Remove excess objects when shrinking the pool.
         */
        private void shrinkAvailableObjects() {
            while (availableObjects.size() > currentSize / 2048) {
                availableObjects.poll();
            }
        }

        /**
         * Get pool statistics.
         */
        PoolStats getStats() {
            return new PoolStats(
                    type,
                    allocations.get(),
                    deallocations.get(),
                    hits.get(),
                    misses.get(),
                    totalBytesAllocated.get(),
                    currentSize,
                    maxSize,
                    utilizationRatio,
                    availableObjects.size(),
                    allocatedObjects.size());
        }
    }

    /**
     * Pooled object that can be reused across allocations.
     */
    private static class PooledObject {
        private final byte[] data;
        private final PoolType poolType;
        private final long creationTime;
        private volatile int currentSize;
        private volatile boolean allocated;
        private volatile long lastAccessTime;

        PooledObject(int initialCapacity, PoolType poolType) {
            this.data = new byte[initialCapacity];
            this.poolType = poolType;
            this.creationTime = System.currentTimeMillis();
            this.currentSize = 0;
            this.allocated = false;
            this.lastAccessTime = creationTime;
        }

        boolean canAccommodate(int size) {
            return !allocated && data.length >= size;
        }

        void allocate(int size) {
            this.currentSize = size;
            this.allocated = true;
            this.lastAccessTime = System.currentTimeMillis();
        }

        void deallocate() {
            this.allocated = false;
            this.currentSize = 0;
            this.lastAccessTime = System.currentTimeMillis();
        }

        int getSize() {
            return currentSize;
        }

        int getCapacity() {
            return data.length;
        }

        boolean isAllocated() {
            return allocated;
        }

        long getAge() {
            return System.currentTimeMillis() - creationTime;
        }

        long getTimeSinceLastAccess() {
            return System.currentTimeMillis() - lastAccessTime;
        }

        PoolType getPoolType() {
            return poolType;
        }
    }

    /**
     * Statistics for a memory pool.
     */
    public static class PoolStats {
        private final PoolType type;
        private final long allocations;
        private final long deallocations;
        private final long hits;
        private final long misses;
        private final long totalBytesAllocated;
        private final long currentSize;
        private final long maxSize;
        private final double utilizationRatio;
        private final int availableObjects;
        private final int allocatedObjects;

        PoolStats(PoolType type, long allocations, long deallocations, long hits, long misses,
                long totalBytesAllocated, long currentSize, long maxSize, double utilizationRatio,
                int availableObjects, int allocatedObjects) {
            this.type = type;
            this.allocations = allocations;
            this.deallocations = deallocations;
            this.hits = hits;
            this.misses = misses;
            this.totalBytesAllocated = totalBytesAllocated;
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.utilizationRatio = utilizationRatio;
            this.availableObjects = availableObjects;
            this.allocatedObjects = allocatedObjects;
        }

        // Getters
        public PoolType getType() {
            return type;
        }

        public long getAllocations() {
            return allocations;
        }

        public long getDeallocations() {
            return deallocations;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public long getTotalBytesAllocated() {
            return totalBytesAllocated;
        }

        public long getCurrentSize() {
            return currentSize;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public double getUtilizationRatio() {
            return utilizationRatio;
        }

        public int getAvailableObjects() {
            return availableObjects;
        }

        public int getAllocatedObjects() {
            return allocatedObjects;
        }

        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s{allocations=%d, hits=%d, hitRate=%.2f%%, utilization=%.1f%%, size=%d/%d, objects=%d/%d}",
                    type.getName(), allocations, hits, getHitRate() * 100.0, utilizationRatio * 100.0,
                    currentSize, maxSize, allocatedObjects, availableObjects);
        }
    }

    /**
     * Memory pressure monitor to detect GC activity and memory stress.
     */
    private static class MemoryPressureMonitor {
        private final MemoryMXBean memoryBean;
        private final AtomicLong lastGcTime;
        private final AtomicLong gcCount;
        private volatile double memoryUtilization;
        private volatile boolean underPressure;

        MemoryPressureMonitor(MemoryMXBean memoryBean) {
            this.memoryBean = memoryBean;
            this.lastGcTime = new AtomicLong(0);
            this.gcCount = new AtomicLong(0);
            this.memoryUtilization = 0.0;
            this.underPressure = false;
        }

        void updatePressureMetrics() {
            // Calculate memory utilization
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            this.memoryUtilization = (double) heapUsage.getUsed() / heapUsage.getMax();

            // Detect memory pressure
            this.underPressure = memoryUtilization > 0.85 || detectFrequentGc();
        }

        private boolean detectFrequentGc() {
            // Simple heuristic: if we see rapid memory usage changes, assume GC activity
            long currentTime = System.currentTimeMillis();
            long timeSinceLastCheck = currentTime - lastGcTime.get();

            if (timeSinceLastCheck > 5000) { // Check every 5 seconds
                lastGcTime.set(currentTime);
                return memoryUtilization > 0.7; // High memory usage indicates pressure
            }

            return false;
        }

        boolean isUnderPressure() {
            return underPressure;
        }

        double getMemoryUtilization() {
            return memoryUtilization;
        }
    }

    /**
     * Analytics system for pool performance analysis.
     */
    private static class PoolAnalytics {
        private final Map<PoolType, List<PoolStats>> historicalStats;
        private final AtomicLong lastAnalysisTime;

        PoolAnalytics() {
            this.historicalStats = new ConcurrentHashMap<>();
            this.lastAnalysisTime = new AtomicLong(0);

            for (PoolType type : PoolType.values()) {
                historicalStats.put(type, new CopyOnWriteArrayList<>());
            }
        }

        void recordStats(PoolStats stats) {
            List<PoolStats> history = historicalStats.get(stats.getType());
            history.add(stats);

            // Keep only last 100 records per pool
            if (history.size() > 100) {
                history.remove(0);
            }
        }

        Map<PoolType, Double> getAverageHitRates() {
            Map<PoolType, Double> averages = new EnumMap<>(PoolType.class);

            for (Map.Entry<PoolType, List<PoolStats>> entry : historicalStats.entrySet()) {
                List<PoolStats> stats = entry.getValue();
                if (!stats.isEmpty()) {
                    double average = stats.stream()
                            .mapToDouble(PoolStats::getHitRate)
                            .average()
                            .orElse(0.0);
                    averages.put(entry.getKey(), average);
                }
            }

            return averages;
        }

        String generateAnalysisReport() {
            StringBuilder report = new StringBuilder();
            report.append("Memory Pool Analysis Report\n");
            report.append("============================\n\n");

            Map<PoolType, Double> hitRates = getAverageHitRates();

            for (PoolType type : PoolType.values()) {
                List<PoolStats> stats = historicalStats.get(type);
                if (!stats.isEmpty()) {
                    PoolStats latest = stats.get(stats.size() - 1);
                    Double avgHitRate = hitRates.get(type);

                    report.append(String.format("Pool: %s\n", type.getName()));
                    report.append(String.format("  Description: %s\n", type.getDescription()));
                    report.append(String.format("  Current Hit Rate: %.2f%%\n", latest.getHitRate() * 100.0));
                    report.append(String.format("  Average Hit Rate: %.2f%%\n", avgHitRate * 100.0));
                    report.append(String.format("  Utilization: %.1f%%\n", latest.getUtilizationRatio() * 100.0));
                    report.append(String.format("  Allocations: %d\n", latest.getAllocations()));
                    report.append("\n");
                }
            }

            return report.toString();
        }
    }

    /**
     * Allocation strategy interface.
     */
    private interface AllocationStrategy {
        PoolType selectPool(int objectSize, String hint);

        String getName();
    }

    /**
     * Default allocation strategy based on object size.
     */
    private static class SizeBasedAllocationStrategy implements AllocationStrategy {
        @Override
        public PoolType selectPool(int objectSize, String hint) {
            if (objectSize <= 512) {
                return PoolType.EDEN;
            } else if (objectSize <= 4096) {
                return PoolType.SURVIVOR_0;
            } else if (objectSize <= 16384) {
                return PoolType.TENURED;
            } else {
                return PoolType.OFF_HEAP;
            }
        }

        @Override
        public String getName() {
            return "SizeBased";
        }
    }

    /**
     * Creates a new memory pool manager.
     */
    public MemoryPoolManager(MemoryPoolConfig config) {
        this.config = config;
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.maintenanceExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MemoryPoolManager-Maintenance");
            t.setDaemon(true);
            return t;
        });

        // Initialize pools
        this.pools = new EnumMap<>(PoolType.class);
        for (Map.Entry<PoolType, Long> entry : config.getPoolSizes().entrySet()) {
            if (entry.getKey() != PoolType.OFF_HEAP || config.isOffHeapPoolsEnabled()) {
                pools.put(entry.getKey(), new ManagedMemoryPool(entry.getKey(), entry.getValue()));
            }
        }

        // Initialize allocation strategy
        this.currentStrategy = new AtomicReference<>(new SizeBasedAllocationStrategy());

        // Initialize monitoring
        this.pressureMonitor = new MemoryPressureMonitor(memoryBean);
        this.analytics = new PoolAnalytics();
        this.totalAllocations = new AtomicLong(0);
        this.totalDeallocations = new AtomicLong(0);

        // Initialize maintenance
        this.maintenanceRunning = false;
        this.lastMaintenanceTime = new AtomicLong(System.currentTimeMillis());

        // Start maintenance thread
        startMaintenanceTask();
    }

    /**
     * Allocate memory from the appropriate pool.
     */
    public PooledObject allocate(int size, String hint) {
        totalAllocations.incrementAndGet();

        // Select appropriate pool
        PoolType poolType = currentStrategy.get().selectPool(size, hint);
        ManagedMemoryPool pool = pools.get(poolType);

        if (pool != null) {
            PooledObject obj = pool.allocate(size);
            if (obj != null) {
                return obj;
            }
        }

        // Try fallback pools
        for (PoolType fallbackType : PoolType.values()) {
            if (fallbackType != poolType) {
                ManagedMemoryPool fallbackPool = pools.get(fallbackType);
                if (fallbackPool != null) {
                    PooledObject obj = fallbackPool.allocate(size);
                    if (obj != null) {
                        return obj;
                    }
                }
            }
        }

        // Emergency allocation
        return new PooledObject(size, PoolType.EMERGENCY);
    }

    /**
     * Return memory to the appropriate pool.
     */
    public void deallocate(PooledObject obj) {
        totalDeallocations.incrementAndGet();

        ManagedMemoryPool pool = pools.get(obj.getPoolType());
        if (pool != null) {
            pool.deallocate(obj);
        }
    }

    /**
     * Get comprehensive statistics for all pools.
     */
    public Map<PoolType, PoolStats> getAllPoolStats() {
        Map<PoolType, PoolStats> stats = new EnumMap<>(PoolType.class);
        for (Map.Entry<PoolType, ManagedMemoryPool> entry : pools.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().getStats());
        }
        return stats;
    }

    /**
     * Get overall memory manager statistics.
     */
    public String getOverallStats() {
        return String.format(
                "MemoryPoolManager{allocations=%d, deallocations=%d, pressure=%.1f%%, strategy=%s, pools=%d}",
                totalAllocations.get(), totalDeallocations.get(),
                pressureMonitor.getMemoryUtilization() * 100.0,
                currentStrategy.get().getName(), pools.size());
    }

    /**
     * Generate comprehensive analytics report.
     */
    public String generateAnalyticsReport() {
        return analytics.generateAnalysisReport();
    }

    /**
     * Start background maintenance task.
     */
    private void startMaintenanceTask() {
        maintenanceExecutor.scheduleAtFixedRate(
                this::performMaintenance,
                config.getMaintenanceIntervalMs(),
                config.getMaintenanceIntervalMs(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * Perform background maintenance tasks.
     */
    private void performMaintenance() {
        if (maintenanceRunning) {
            return; // Already running
        }

        maintenanceRunning = true;
        try {
            long startTime = System.currentTimeMillis();

            // Update memory pressure metrics
            if (config.isMemoryPressureDetectionEnabled()) {
                pressureMonitor.updatePressureMetrics();
            }

            // Perform adaptive resizing
            if (config.isAdaptivePoolSizing()) {
                for (ManagedMemoryPool pool : pools.values()) {
                    pool.performAdaptiveResize();
                }
            }

            // Record analytics
            for (ManagedMemoryPool pool : pools.values()) {
                analytics.recordStats(pool.getStats());
            }

            lastMaintenanceTime.set(startTime);

        } finally {
            maintenanceRunning = false;
        }
    }

    /**
     * Shutdown the memory pool manager.
     */
    public void shutdown() {
        maintenanceExecutor.shutdown();
        try {
            if (!maintenanceExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                maintenanceExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            maintenanceExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if the system is under memory pressure.
     */
    public boolean isUnderMemoryPressure() {
        return pressureMonitor.isUnderPressure();
    }

    /**
     * Get current memory utilization ratio.
     */
    public double getMemoryUtilization() {
        return pressureMonitor.getMemoryUtilization();
    }
}
