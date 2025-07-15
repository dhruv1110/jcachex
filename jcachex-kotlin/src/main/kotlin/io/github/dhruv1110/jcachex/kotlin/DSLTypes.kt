@file:Suppress("TooManyFunctions", "unused")
@file:JvmName("DSLTypes")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.CacheConfig
import io.github.dhruv1110.jcachex.FrequencySketchType
import io.github.dhruv1110.jcachex.JCacheXBuilder
import io.github.dhruv1110.jcachex.profiles.WorkloadCharacteristics

/*
 * DSL types for cache configuration.
 *
 * This file contains shared DSL classes that are used by multiple extension modules.
 */

/**
 * DSL wrapper for CacheConfig.Builder providing a Kotlin-idiomatic configuration experience.
 */
@Suppress("TooManyFunctions")
class CacheConfigBuilder<K, V> {
    private val builder = CacheConfig.builder<K, V>()

    fun maximumSize(size: Long) = apply { builder.maximumSize(size) }

    fun maximumWeight(weight: Long) = apply { builder.maximumWeight(weight) }

    fun expireAfterWrite(duration: java.time.Duration) = apply { builder.expireAfterWrite(duration) }

    fun expireAfterAccess(duration: java.time.Duration) = apply { builder.expireAfterAccess(duration) }

    fun refreshAfterWrite(duration: java.time.Duration) = apply { builder.refreshAfterWrite(duration) }

    fun loader(loader: (K) -> V) = apply { builder.loader(loader) }

    fun asyncLoader(loader: (K) -> java.util.concurrent.CompletableFuture<V>) = apply { builder.asyncLoader(loader) }

    fun weigher(weigher: (K, V) -> Long) = apply { builder.weigher(weigher) }

    fun evictionStrategy(strategy: io.github.dhruv1110.jcachex.eviction.EvictionStrategy<K, V>) =
        apply { builder.evictionStrategy(strategy) }

    fun recordStats(enable: Boolean = true) = apply { builder.recordStats(enable) }

    fun initialCapacity(capacity: Int) = apply { builder.initialCapacity(capacity) }

    fun concurrencyLevel(level: Int) = apply { builder.concurrencyLevel(level) }

    fun weakKeys(enable: Boolean = true) = apply { builder.weakKeys(enable) }

    fun weakValues(enable: Boolean = true) = apply { builder.weakValues(enable) }

    fun softValues(enable: Boolean = true) = apply { builder.softValues(enable) }

    fun directory(dir: String) = apply { builder.directory(dir) }

    fun listener(listener: io.github.dhruv1110.jcachex.CacheEventListener<K, V>) =
        apply { builder.addListener(listener) }

    fun frequencySketchType(sketchType: FrequencySketchType) = apply { builder.frequencySketchType(sketchType) }

    fun build(): CacheConfig<K, V> = builder.build()
}

/**
 * DSL scope for JCacheXBuilder providing a Kotlin-idiomatic configuration experience.
 */
@Suppress("TooManyFunctions")
class JCacheXBuilderScope<K, V>(private val builder: JCacheXBuilder<K, V>) {
    fun name(name: String) = apply { builder.name(name) }

    fun maximumSize(size: Long) = apply { builder.maximumSize(size) }

    fun maximumWeight(weight: Long) = apply { builder.maximumWeight(weight) }

    fun expireAfterWrite(duration: java.time.Duration) = apply { builder.expireAfterWrite(duration) }

    fun expireAfterAccess(duration: java.time.Duration) = apply { builder.expireAfterAccess(duration) }

    fun refreshAfterWrite(duration: java.time.Duration) = apply { builder.refreshAfterWrite(duration) }

    fun loader(loader: (K) -> V) = apply { builder.loader(loader) }

    fun asyncLoader(loader: (K) -> java.util.concurrent.CompletableFuture<V>) = apply { builder.asyncLoader(loader) }

    fun weigher(weigher: (K, V) -> Long) = apply { builder.weigher(weigher) }

    fun recordStats(enable: Boolean = true) = apply { builder.recordStats(enable) }

    fun listener(listener: io.github.dhruv1110.jcachex.CacheEventListener<K, V>) = apply { builder.listener(listener) }

    fun workloadCharacteristics(characteristics: WorkloadCharacteristics) =
        apply { builder.workloadCharacteristics(characteristics) }

    fun workloadCharacteristics(configure: WorkloadCharacteristicsScope.() -> Unit) =
        apply {
            val scope = WorkloadCharacteristicsScope()
            scope.configure()
            builder.workloadCharacteristics(scope.build())
        }
}

/**
 * DSL scope for WorkloadCharacteristics.
 */
class WorkloadCharacteristicsScope {
    private val builder = WorkloadCharacteristics.builder()

    fun readToWriteRatio(ratio: Double) = apply { builder.readToWriteRatio(ratio) }

    fun accessPattern(pattern: WorkloadCharacteristics.AccessPattern) = apply { builder.accessPattern(pattern) }

    fun memoryConstraint(constraint: WorkloadCharacteristics.MemoryConstraint) =
        apply { builder.memoryConstraint(constraint) }

    fun concurrencyLevel(level: WorkloadCharacteristics.ConcurrencyLevel) = apply { builder.concurrencyLevel(level) }

    fun requiresConsistency(required: Boolean) = apply { builder.requiresConsistency(required) }

    fun requiresAsyncOperations(required: Boolean) = apply { builder.requiresAsyncOperations(required) }

    fun expectedSize(size: Long) = apply { builder.expectedSize(size) }

    fun hitRateExpectation(rate: Double) = apply { builder.hitRateExpectation(rate) }

    fun build(): WorkloadCharacteristics = builder.build()
}

// ===== LEGACY SUPPORT =====

/**
 * @deprecated Use JCacheXBuilderScope instead
 */
@Deprecated(
    "Use JCacheXBuilderScope instead",
    ReplaceWith("JCacheXBuilderScope<K, V>", "io.github.dhruv1110.jcachex.kotlin.JCacheXBuilderScope"),
)
typealias UnifiedCacheBuilderScope<K, V> = JCacheXBuilderScope<K, V>
