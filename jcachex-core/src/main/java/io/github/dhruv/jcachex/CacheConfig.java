package io.github.dhruv.jcachex;

import io.github.dhruv.jcachex.eviction.EvictionStrategy;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;

/**
 * Configuration options for a cache.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class CacheConfig<K, V> {
    private final Long maximumSize;
    private final Long maximumWeight;
    private final BiFunction<K, V, Long> weigher;
    private final Duration expireAfterWrite;
    private final Duration expireAfterAccess;
    private final EvictionStrategy<K, V> evictionStrategy;
    private final boolean weakKeys;
    private final boolean weakValues;
    private final boolean softValues;
    private final Function<K, V> loader;
    private final Function<K, CompletableFuture<V>> asyncLoader;
    private final Duration refreshAfterWrite;
    private final boolean recordStats;
    private final int initialCapacity;
    private final int concurrencyLevel;
    private final String directory;
    private final Set<CacheEventListener<K, V>> listeners;

    private CacheConfig(Builder<K, V> builder) {
        this.maximumSize = builder.maximumSize;
        this.maximumWeight = builder.maximumWeight;
        this.weigher = builder.weigher;
        this.expireAfterWrite = builder.expireAfterWrite;
        this.expireAfterAccess = builder.expireAfterAccess;
        this.evictionStrategy = builder.evictionStrategy;
        this.weakKeys = builder.weakKeys;
        this.weakValues = builder.weakValues;
        this.softValues = builder.softValues;
        this.loader = builder.loader;
        this.asyncLoader = builder.asyncLoader;
        this.refreshAfterWrite = builder.refreshAfterWrite;
        this.recordStats = builder.recordStats;
        this.initialCapacity = builder.initialCapacity;
        this.concurrencyLevel = builder.concurrencyLevel;
        this.directory = builder.directory;
        this.listeners = new HashSet<>(builder.listeners);
    }

    public Long getMaximumSize() {
        return maximumSize;
    }

    public Long getMaximumWeight() {
        return maximumWeight;
    }

    public BiFunction<K, V, Long> getWeigher() {
        return weigher;
    }

    public Duration getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public Duration getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public EvictionStrategy<K, V> getEvictionStrategy() {
        return evictionStrategy;
    }

    public boolean isWeakKeys() {
        return weakKeys;
    }

    public boolean isWeakValues() {
        return weakValues;
    }

    public boolean isSoftValues() {
        return softValues;
    }

    public Function<K, V> getLoader() {
        return loader;
    }

    public Function<K, CompletableFuture<V>> getAsyncLoader() {
        return asyncLoader;
    }

    public Duration getRefreshAfterWrite() {
        return refreshAfterWrite;
    }

    public boolean isRecordStats() {
        return recordStats;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public int getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public String getDirectory() {
        return directory;
    }

    public Set<CacheEventListener<K, V>> getListeners() {
        return new HashSet<>(listeners);
    }

    public static <K, V> Builder<K, V> newBuilder() {
        return new Builder<>();
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    public static class Builder<K, V> {
        private Long maximumSize;
        private Long maximumWeight;
        private BiFunction<K, V, Long> weigher;
        private Duration expireAfterWrite;
        private Duration expireAfterAccess;
        private EvictionStrategy<K, V> evictionStrategy;
        private boolean weakKeys;
        private boolean weakValues;
        private boolean softValues;
        private Function<K, V> loader;
        private Function<K, CompletableFuture<V>> asyncLoader;
        private Duration refreshAfterWrite;
        private boolean recordStats = true;
        private int initialCapacity = 16;
        private int concurrencyLevel = 16;
        private String directory;
        private Set<CacheEventListener<K, V>> listeners = new HashSet<>();

        public Builder<K, V> maximumSize(Long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public Builder<K, V> maximumWeight(Long maximumWeight) {
            this.maximumWeight = maximumWeight;
            return this;
        }

        public Builder<K, V> weigher(BiFunction<K, V, Long> weigher) {
            this.weigher = weigher;
            return this;
        }

        public Builder<K, V> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        public Builder<K, V> expireAfterWrite(long duration, TimeUnit unit) {
            this.expireAfterWrite = Duration.ofNanos(unit.toNanos(duration));
            return this;
        }

        public Builder<K, V> expireAfterAccess(Duration duration) {
            this.expireAfterAccess = duration;
            return this;
        }

        public Builder<K, V> expireAfterAccess(long duration, TimeUnit unit) {
            this.expireAfterAccess = Duration.ofNanos(unit.toNanos(duration));
            return this;
        }

        public Builder<K, V> evictionStrategy(EvictionStrategy<K, V> evictionStrategy) {
            this.evictionStrategy = evictionStrategy;
            return this;
        }

        public Builder<K, V> weakKeys(boolean weakKeys) {
            this.weakKeys = weakKeys;
            return this;
        }

        public Builder<K, V> weakValues(boolean weakValues) {
            this.weakValues = weakValues;
            return this;
        }

        public Builder<K, V> softValues(boolean softValues) {
            this.softValues = softValues;
            return this;
        }

        public Builder<K, V> loader(Function<K, V> loader) {
            this.loader = loader;
            return this;
        }

        public Builder<K, V> asyncLoader(Function<K, CompletableFuture<V>> asyncLoader) {
            this.asyncLoader = asyncLoader;
            return this;
        }

        public Builder<K, V> refreshAfterWrite(Duration duration) {
            this.refreshAfterWrite = duration;
            return this;
        }

        public Builder<K, V> recordStats(boolean recordStats) {
            this.recordStats = recordStats;
            return this;
        }

        public Builder<K, V> initialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
            return this;
        }

        public Builder<K, V> concurrencyLevel(int concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
            return this;
        }

        public Builder<K, V> directory(String directory) {
            this.directory = directory;
            return this;
        }

        public Builder<K, V> addListener(CacheEventListener<K, V> listener) {
            this.listeners.add(listener);
            return this;
        }

        public CacheConfig<K, V> build() {
            if (maximumSize != null && maximumSize < 1){
                throw new IllegalArgumentException("Maximum size must be greater than 0");
            }
            if (maximumWeight != null && maximumWeight < 1) {
                throw new IllegalArgumentException("Maximum weight must be greater than 0");
            }
            if (expireAfterWrite != null && expireAfterWrite.isNegative()) {
                throw new IllegalArgumentException("Expire after write duration must be non-negative");
            }
            return new CacheConfig<>(this);
        }
    }
}
