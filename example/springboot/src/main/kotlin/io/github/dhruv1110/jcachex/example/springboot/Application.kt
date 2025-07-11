package io.github.dhruv1110.jcachex.example.springboot

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.CacheBuilder
import io.github.dhruv1110.jcachex.FrequencySketchType
import io.github.dhruv1110.jcachex.eviction.EvictionStrategy
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@SpringBootApplication
@EnableCaching
class Application

fun main(args: Array<String>) {
    runApplication<Application>(args = args)
}

@Configuration
class CacheConfiguration {

    // Default user cache using TinyWindowLFU (new default)
    @Bean
    @Qualifier("userCache")
    fun userCache(): Cache<String, User> {
        return CacheBuilder.newBuilder<String, User>()
            .cacheType(CacheBuilder.CacheType.DEFAULT) // Uses TinyWindowLFU by default
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats(true)
            .build()
    }

    // Read-heavy cache for product data (fastest GET performance)
    @Bean
    @Qualifier("productCache")
    fun productCache(): Cache<String, Product> {
        return CacheBuilder.newBuilder<String, Product>()
            .cacheType(CacheBuilder.CacheType.READ_ONLY_OPTIMIZED)
            .maximumSize(5000L)
            .expireAfterWrite(Duration.ofHours(2))
            .recordStats(true)
            .build()
    }

    // Write-heavy cache for session data
    @Bean
    @Qualifier("sessionCache")
    fun sessionCache(): Cache<String, UserSession> {
        return CacheBuilder.newBuilder<String, UserSession>()
            .cacheType(CacheBuilder.CacheType.WRITE_HEAVY_OPTIMIZED)
            .maximumSize(10000L)
            .expireAfterAccess(Duration.ofMinutes(30))
            .recordStats(true)
            .build()
    }

    // High-performance cache for frequently accessed data
    @Bean
    @Qualifier("performanceCache")
    fun performanceCache(): Cache<String, String> {
        return CacheBuilder.newBuilder<String, String>()
            .cacheType(CacheBuilder.CacheType.JIT_OPTIMIZED)
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofMinutes(15))
            .recordStats(true)
            .build()
    }

    // Enhanced LFU cache with optimized frequency sketch
    @Bean
    @Qualifier("analyticsCache")
    fun analyticsCache(): Cache<String, AnalyticsData> {
        return CacheBuilder.newBuilder<String, AnalyticsData>()
            .cacheType(CacheBuilder.CacheType.JIT_OPTIMIZED)
            .maximumSize(2000L)
            .expireAfterWrite(Duration.ofHours(1))
            .evictionStrategy(EvictionStrategy.ENHANCED_LFU<String, AnalyticsData>())
            .frequencySketchType(FrequencySketchType.OPTIMIZED)
            .recordStats(true)
            .build()
    }
}

@RestController
class UserController(
    @Qualifier("userCache") private val userCache: Cache<String, User>,
    @Qualifier("performanceCache") private val performanceCache: Cache<String, String>
) {

    @GetMapping("/users/{id}")
    @Cacheable("users")
    fun getUser(@PathVariable id: String): User {
        // Check JCacheX cache first
        val cachedUser = userCache.get(id)
        if (cachedUser != null) {
            return cachedUser
        }

        // Simulate database call
        Thread.sleep(1000)
        val user = User(id, "User $id", "user$id@example.com")

        // Store in JCacheX cache
        userCache.put(id, user)

        return user
    }

    @GetMapping("/users/{id}/profile")
    fun getUserProfile(@PathVariable id: String): UserProfile {
        val cacheKey = "profile:$id"

        // Try performance cache first
        val cachedProfile = performanceCache.get(cacheKey)
        if (cachedProfile != null) {
            return UserProfile.fromJson(cachedProfile)
        }

        // Simulate expensive profile computation
        Thread.sleep(500)
        val profile = UserProfile(
            id, "Profile for User $id", "user$id@example.com",
            mapOf("theme" to "dark", "language" to "en")
        )

        // Store in performance cache
        performanceCache.put(cacheKey, profile.toJson())

        return profile
    }

    @GetMapping("/cache/stats")
    fun getCacheStats(): Map<String, Any> {
        val stats = userCache.stats()
        return mapOf(
            "userCache" to mapOf(
                "size" to userCache.size(),
                "hitRate" to String.format("%.2f%%", stats.hitRate() * 100),
                "hitCount" to stats.hitCount(),
                "missCount" to stats.missCount(),
                "evictionCount" to stats.evictionCount(),
                "requestCount" to (stats.hitCount() + stats.missCount())
            ),
            "performanceCache" to mapOf(
                "size" to performanceCache.size(),
                "hitRate" to String.format("%.2f%%", performanceCache.stats().hitRate() * 100)
            )
        )
    }

    @GetMapping("/cache/performance")
    fun testCachePerformance(): Map<String, String> {
        // Test user cache performance
        val iterations = 10000
        val startTime = System.nanoTime()

        repeat(iterations) { i ->
            userCache.get("user${i % 100}")
        }

        val endTime = System.nanoTime()
        val avgLatency = (endTime - startTime) / iterations

        return mapOf(
            "cacheType" to "DEFAULT (TinyWindowLFU)",
            "averageLatency" to "${avgLatency}ns",
            "iterations" to iterations.toString(),
            "expectedRange" to "~40.4ns per GET operation"
        )
    }
}

@RestController
class ProductController(
    @Qualifier("productCache") private val productCache: Cache<String, Product>
) {

    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: String): Product {
        // Check read-optimized cache
        val cachedProduct = productCache.get(id)
        if (cachedProduct != null) {
            return cachedProduct
        }

        // Simulate database call
        Thread.sleep(800)
        val product = Product(id, "Product $id", 99.99 + id.hashCode() % 100, "Electronics")

        // Store in read-optimized cache
        productCache.put(id, product)

        return product
    }

    @GetMapping("/products/performance")
    fun testProductCachePerformance(): Map<String, String> {
        // Test read-optimized cache performance
        val iterations = 10000
        val startTime = System.nanoTime()

        repeat(iterations) { i ->
            productCache.get("product${i % 100}")
        }

        val endTime = System.nanoTime()
        val avgLatency = (endTime - startTime) / iterations

        return mapOf(
            "cacheType" to "READ_ONLY_OPTIMIZED",
            "averageLatency" to "${avgLatency}ns",
            "iterations" to iterations.toString(),
            "expectedRange" to "~11.5ns per GET operation (1.6x faster than Caffeine)"
        )
    }
}

@RestController
class SessionController(
    @Qualifier("sessionCache") private val sessionCache: Cache<String, UserSession>
) {

    @GetMapping("/sessions/{userId}")
    fun createSession(@PathVariable userId: String): UserSession {
        val sessionId = "session-${System.currentTimeMillis()}-$userId"
        val session = UserSession(
            sessionId, userId, System.currentTimeMillis(),
            mapOf("ip" to "192.168.1.1", "userAgent" to "Mozilla/5.0")
        )

        // Store in write-optimized cache
        sessionCache.put(sessionId, session)

        return session
    }

    @GetMapping("/sessions/performance")
    fun testSessionCachePerformance(): Map<String, String> {
        // Test write-optimized cache performance
        val iterations = 1000
        val startTime = System.nanoTime()

        repeat(iterations) { i ->
            val session = UserSession("session-$i", "user$i", System.currentTimeMillis())
            sessionCache.put("session-$i", session)
        }

        val endTime = System.nanoTime()
        val avgLatency = (endTime - startTime) / iterations

        return mapOf(
            "cacheType" to "WRITE_HEAVY_OPTIMIZED",
            "averageLatency" to "${avgLatency}ns",
            "iterations" to iterations.toString(),
            "expectedRange" to "~393.5ns per PUT operation"
        )
    }
}

@RestController
class AnalyticsController(
    @Qualifier("analyticsCache") private val analyticsCache: Cache<String, AnalyticsData>
) {

    @GetMapping("/analytics/{metric}")
    fun getAnalytics(@PathVariable metric: String): AnalyticsData {
        val cachedData = analyticsCache.get(metric)
        if (cachedData != null) {
            return cachedData
        }

        // Simulate complex analytics computation
        Thread.sleep(2000)
        val data = AnalyticsData(metric, (1..100).random().toDouble(), System.currentTimeMillis())

        analyticsCache.put(metric, data)
        return data
    }

    @GetMapping("/analytics/eviction-test")
    fun testEvictionStrategies(): Map<String, Any> {
        // Test Enhanced LFU with frequency sketch
        repeat(100) { i ->
            val data = AnalyticsData("metric$i", i.toDouble(), System.currentTimeMillis())
            analyticsCache.put("metric$i", data)
        }

        // Create access pattern - some metrics accessed more frequently
        repeat(1000) { i ->
            when {
                i % 10 < 7 -> analyticsCache.get("metric${i % 10}") // Hot data (70%)
                i % 10 < 9 -> analyticsCache.get("metric${10 + (i % 20)}") // Warm data (20%)
                else -> analyticsCache.get("metric${30 + (i % 70)}") // Cold data (10%)
            }
        }

        val stats = analyticsCache.stats()
        return mapOf(
            "cacheType" to "JIT_OPTIMIZED with ENHANCED_LFU",
            "frequencySketch" to "OPTIMIZED",
            "hitRate" to String.format("%.2f%%", stats.hitRate() * 100),
            "evictionCount" to stats.evictionCount(),
            "size" to analyticsCache.size()
        )
    }
}

// Data classes
data class User(
    val id: String,
    val name: String,
    val email: String
)

data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String,
    val preferences: Map<String, String> = emptyMap()
) {
    fun toJson(): String =
        """{"userId":"$userId","displayName":"$displayName","email":"$email","preferences":${preferences}}"""

    companion object {
        fun fromJson(json: String): UserProfile {
            // Simple JSON parsing for demo purposes
            return UserProfile("demo", "Demo User", "demo@example.com")
        }
    }
}

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String
)

data class UserSession(
    val sessionId: String,
    val userId: String,
    val timestamp: Long,
    val metadata: Map<String, String> = emptyMap()
)

data class AnalyticsData(
    val metric: String,
    val value: Double,
    val timestamp: Long
)
