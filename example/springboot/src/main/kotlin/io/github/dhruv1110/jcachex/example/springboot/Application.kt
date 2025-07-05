package io.github.dhruv1110.jcachex.example.springboot

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.CacheConfig
import io.github.dhruv1110.jcachex.DefaultCache
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

private const val CACHE_SIZE = 1000L
private const val EXPIRE_MINUTES = 30L
private const val DATABASE_DELAY = 1000L
private const val PROFILE_DELAY = 500L

@SpringBootApplication
@EnableCaching
class Application

fun main(args: Array<String>) {
    runApplication<Application>(args = args)
}

@Configuration
class CacheConfiguration {
    @Bean
    fun userCache(): Cache<String, User> {
        val config =
            CacheConfig
                .builder<String, User>()
                .maximumSize(CACHE_SIZE)
                .expireAfterWrite(Duration.ofMinutes(EXPIRE_MINUTES))
                .recordStats(true)
                .build()
        return DefaultCache(config)
    }
}

@RestController
class UserController(
    private val userCache: Cache<String, User>,
) {
    @GetMapping("/users/{id}")
    @Cacheable("users")
    fun getUser(
        @PathVariable id: String,
    ): User {
        // Check JCacheX cache first
        val cachedUser = userCache.get(id)
        if (cachedUser != null) {
            return cachedUser
        }

        // Simulate database call
        Thread.sleep(DATABASE_DELAY)
        val user = User(id, "User $id")

        // Store in JCacheX cache
        userCache.put(id, user)

        return user
    }

    @GetMapping("/users/{id}/profile")
    fun getUserProfile(
        @PathVariable id: String,
    ): UserProfile {
        // Simulate expensive profile computation
        Thread.sleep(PROFILE_DELAY)
        return UserProfile(id, "Profile for User $id", "user$id@example.com")
    }

    @GetMapping("/stats")
    fun getCacheStats(): Map<String, Any> {
        val stats = userCache.stats()
        return mapOf(
            "size" to userCache.size(),
            "hitRate" to stats.hitRate(),
            "hitCount" to stats.hitCount(),
            "missCount" to stats.missCount(),
        )
    }
}

data class User(
    val id: String,
    val name: String,
)

data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String,
)
