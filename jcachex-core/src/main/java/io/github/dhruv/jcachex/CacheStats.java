package io.github.dhruv.jcachex;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Objects;

/**
 * Statistics for a cache.
 */
public class CacheStats {
    private final AtomicLong hitCount;
    private final AtomicLong missCount;
    private final AtomicLong evictionCount;
    private final AtomicLong loadCount;
    private final AtomicLong loadFailureCount;
    private final AtomicLong totalLoadTime;

    public CacheStats() {
        this.hitCount = new AtomicLong(0);
        this.missCount = new AtomicLong(0);
        this.evictionCount = new AtomicLong(0);
        this.loadCount = new AtomicLong(0);
        this.loadFailureCount = new AtomicLong(0);
        this.totalLoadTime = new AtomicLong(0);
    }

    public CacheStats(AtomicLong hitCount, AtomicLong missCount, AtomicLong evictionCount,
            AtomicLong loadCount, AtomicLong loadFailureCount, AtomicLong totalLoadTime) {
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.evictionCount = evictionCount;
        this.loadCount = loadCount;
        this.loadFailureCount = loadFailureCount;
        this.totalLoadTime = totalLoadTime;
    }

    public long hitCount() {
        return hitCount.get();
    }

    public long missCount() {
        return missCount.get();
    }

    public long evictionCount() {
        return evictionCount.get();
    }

    public long loadCount() {
        return loadCount.get();
    }

    public long loadFailureCount() {
        return loadFailureCount.get();
    }

    public long totalLoadTime() {
        return totalLoadTime.get();
    }

    public double hitRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0L ? 0.0 : (double) hitCount.get() / total;
    }

    public double missRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0L ? 0.0 : (double) missCount.get() / total;
    }

    public double averageLoadTime() {
        long loads = loadCount.get();
        return loads == 0L ? 0.0 : (double) totalLoadTime.get() / loads;
    }

    public void recordHit() {
        hitCount.incrementAndGet();
    }

    public void recordMiss() {
        missCount.incrementAndGet();
    }

    public void recordEviction() {
        evictionCount.incrementAndGet();
    }

    public void recordLoad(long loadTime) {
        loadCount.incrementAndGet();
        totalLoadTime.addAndGet(loadTime);
    }

    public void recordLoadFailure() {
        loadFailureCount.incrementAndGet();
    }

    public CacheStats snapshot() {
        return new CacheStats(
                new AtomicLong(hitCount.get()),
                new AtomicLong(missCount.get()),
                new AtomicLong(evictionCount.get()),
                new AtomicLong(loadCount.get()),
                new AtomicLong(loadFailureCount.get()),
                new AtomicLong(totalLoadTime.get()));
    }

    public CacheStats reset() {
        return new CacheStats();
    }

    public static CacheStats empty() {
        return new CacheStats();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CacheStats that = (CacheStats) o;
        return Objects.equals(hitCount.get(), that.hitCount.get()) &&
                Objects.equals(missCount.get(), that.missCount.get()) &&
                Objects.equals(evictionCount.get(), that.evictionCount.get()) &&
                Objects.equals(loadCount.get(), that.loadCount.get()) &&
                Objects.equals(loadFailureCount.get(), that.loadFailureCount.get()) &&
                Objects.equals(totalLoadTime.get(), that.totalLoadTime.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(hitCount.get(), missCount.get(), evictionCount.get(),
                loadCount.get(), loadFailureCount.get(), totalLoadTime.get());
    }

    @Override
    public String toString() {
        return "CacheStats{" +
                "hitCount=" + hitCount.get() +
                ", missCount=" + missCount.get() +
                ", evictionCount=" + evictionCount.get() +
                ", loadCount=" + loadCount.get() +
                ", loadFailureCount=" + loadFailureCount.get() +
                ", totalLoadTime=" + totalLoadTime.get() +
                '}';
    }
}
