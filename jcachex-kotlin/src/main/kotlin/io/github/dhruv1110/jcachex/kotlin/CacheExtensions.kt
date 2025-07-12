@file:Suppress("TooManyFunctions")

package io.github.dhruv1110.jcachex.kotlin

/**
 * JCacheX Kotlin Extensions - Index File
 *
 * This file serves as an index for all JCacheX Kotlin extensions.
 * It re-exports all extension functions from the separate modules for backward compatibility.
 *
 * ## Extension Modules:
 * - **DSLTypes**: Shared DSL classes and types for configuration
 * - **CollectionExtensions**: Collection-like operations (forEach, keysList, valuesList, etc.)
 *                             and bulk operations (putAll, removeAll, retainAll, removeIf)
 * - **OperatorExtensions**: Operator overloading (set, contains, plusAssign, etc.)
 * - **CoroutineExtensions**: Coroutine support and async operations
 * - **StatisticsExtensions**: Statistics formatting and utilities
 * - **SafeExtensions**: Safe operations with Result types
 * - **DSLExtensions**: Configuration DSL builders and cache creation
 * - **ProfileExtensions**: Profile-based cache creation
 * - **UtilityExtensions**: Utility functions (batch, measureTime, etc.)
 *
 * ## Usage:
 * ```kotlin
 * import io.github.dhruv1110.jcachex.kotlin.*
 *
 * // Use any extension function
 * val cache = createUnifiedCache<String, String> { maximumSize(100) }
 * cache["key"] = "value"
 * val value = cache.getOrDefault("key", "default")
 * ```
 *
 * ## Backward Compatibility:
 * All existing extension functions are still available when importing this package.
 * The logical separation improves maintainability while preserving the public API.
 *
 * ## Implementation Notes:
 * Extension functions are automatically imported when using:
 * import io.github.dhruv1110.jcachex.kotlin.*
 *
 * All extension functions are defined in separate files:
 * - DSLTypes.kt - Shared DSL classes (CacheConfigBuilder, UnifiedCacheBuilderScope, etc.)
 * - CollectionExtensions.kt - Collection-like operations and bulk operations
 * - OperatorExtensions.kt - Operator overloading
 * - CoroutineExtensions.kt - Coroutine support
 * - StatisticsExtensions.kt - Statistics utilities
 * - SafeExtensions.kt - Safe operations
 * - DSLExtensions.kt - DSL functions and cache creation
 * - ProfileExtensions.kt - Profile-based creation
 * - UtilityExtensions.kt - Utility functions
 */
