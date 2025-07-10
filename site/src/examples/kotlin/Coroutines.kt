import io.github.dhruv1110.jcachex.kotlin.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class AsyncCacheService {

    private val cache = cache<String, String> {
        maxSize = 1000
        expireAfterWrite = 30.minutes
        recordStats = true
    }

    // Suspend function for async loading
    suspend fun getValueAsync(key: String): String {
        return cache[key] ?: loadValueAsync(key).also { value ->
            cache[key] = value
        }
    }

    // Parallel loading with coroutines
    suspend fun loadMultipleAsync(keys: List<String>): Map<String, String> {
        return coroutineScope {
            keys.map { key ->
                async { key to getValueAsync(key) }
            }.awaitAll().toMap()
        }
    }

    // Flow-based cache events
    fun cacheEvents(): Flow<CacheEvent> = flow {
        // Simulate cache events
        while (currentCoroutineContext().isActive) {
            delay(1000)
            emit(CacheEvent.HitRate(cache.stats().hitRate()))
        }
    }

    // Structured concurrency for cache operations
    suspend fun manageCacheLifecycle() = coroutineScope {
        // Launch cache warming
        val warmupJob = launch {
            warmCacheAsync()
        }

        // Launch cache cleanup
        val cleanupJob = launch {
            scheduleCleanup()
        }

        // Launch metrics collection
        val metricsJob = launch {
            collectMetrics()
        }

        // All jobs will be cancelled if parent scope is cancelled
        joinAll(warmupJob, cleanupJob, metricsJob)
    }

    // Cancellation-aware cache warming
    private suspend fun warmCacheAsync() {
        val keys = (1..1000).map { "key-$it" }

        keys.chunked(100).forEach { chunk ->
            // Check for cancellation
            ensureActive()

            // Process chunk in parallel
            coroutineScope {
                chunk.map { key ->
                    async {
                        val value = "warm-$key"
                        cache[key] = value
                    }
                }.awaitAll()
            }

            // Yield to other coroutines
            yield()
        }
    }

    // Periodic cache cleanup
    private suspend fun scheduleCleanup() {
        while (currentCoroutineContext().isActive) {
            delay(5.minutes)

            // Cleanup expired entries
            withContext(Dispatchers.Default) {
                cache.cleanUp()
            }
        }
    }

    // Metrics collection with Flow
    private suspend fun collectMetrics() {
        cacheEvents()
            .sample(30.seconds)
            .collect { event ->
                when (event) {
                    is CacheEvent.HitRate -> {
                        println("Cache hit rate: ${event.rate}")
                        if (event.rate < 0.7) {
                            println("WARNING: Low cache hit rate!")
                        }
                    }
                }
            }
    }

    // Timeout-aware cache operations
    suspend fun getWithTimeout(key: String, timeout: kotlin.time.Duration): String? {
        return try {
            withTimeout(timeout) {
                getValueAsync(key)
            }
        } catch (e: TimeoutCancellationException) {
            null
        }
    }

    // Channel-based cache updates
    suspend fun processUpdates(updates: ReceiveChannel<Pair<String, String>>) {
        for ((key, value) in updates) {
            cache[key] = value
            yield() // Cooperative cancellation
        }
    }

    private suspend fun loadValueAsync(key: String): String {
        // Simulate async loading
        delay(50)
        return "async-loaded-$key"
    }
}

sealed class CacheEvent {
    data class HitRate(val rate: Double) : CacheEvent()
    data class SizeChange(val size: Long) : CacheEvent()
}
