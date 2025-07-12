package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext

/**
 * Coroutine extensions for cache operations.
 *
 * These extensions provide coroutine support for async cache operations
 * and integration with Kotlin's structured concurrency.
 */

/**
 * Asynchronously gets the value for the given key as a Deferred.
 */
fun <K, V> Cache<K, V>.getDeferred(
    key: K,
    scope: CoroutineScope,
): Deferred<V?> = scope.async { getAsync(key).await() }

/**
 * Asynchronously puts a value for the given key as a Deferred.
 */
fun <K, V> Cache<K, V>.putDeferred(
    key: K,
    value: V,
    scope: CoroutineScope,
): Deferred<Unit> = scope.async { put(key, value) }

/**
 * Asynchronously removes the value for the given key as a Deferred.
 */
fun <K, V> Cache<K, V>.removeDeferred(
    key: K,
    scope: CoroutineScope,
): Deferred<V?> = scope.async { remove(key) }

/**
 * Asynchronously clears the cache as a Deferred.
 */
fun <K, V> Cache<K, V>.clearDeferred(scope: CoroutineScope): Deferred<Unit> = scope.async { clear() }

/**
 * Suspending version of getOrPut using the cache's async loader if available.
 */
suspend fun <K, V> Cache<K, V>.getOrPutAsync(
    key: K,
    compute: suspend (K) -> V,
): V =
    withContext(Dispatchers.IO) {
        get(key) ?: compute(key).also { put(key, it) }
    }
