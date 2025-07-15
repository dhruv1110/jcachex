import io.github.dhruv1110.jcachex.kotlin.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.delay

data class User(
    val id: String,
    val name: String,
    val email: String,
    val preferences: Map<String, String> = emptyMap()
)

class UserCacheService {

    // Modern DSL-style cache configuration with convenience methods
    private val userCache = createReadHeavyCache<String, User> {
        name("users")
        maximumSize(1000L)
        expireAfterWrite(java.time.Duration.ofHours(2))
        expireAfterAccess(java.time.Duration.ofMinutes(30))
        recordStats(true)
    }

    // Alternative: Use specialized cache types for specific workloads
    private val readHeavyCache = createReadHeavyCache<String, User> {
        name("read-heavy-users")
        maximumSize(5000L)
        expireAfterWrite(java.time.Duration.ofHours(4))
        recordStats(true)
    }

    private val writeHeavyCache = createWriteHeavyCache<String, UserSession> {
        name("write-heavy-sessions")
        maximumSize(10000L)
        expireAfterAccess(java.time.Duration.ofMinutes(30))
        recordStats(true)
    }

    private val performanceCache = createHighPerformanceCache<String, User> {
        name("high-performance-users")
        maximumSize(1000L)
        expireAfterWrite(java.time.Duration.ofHours(1))
        recordStats(true)
    }

    // Operator overloading for intuitive access
    suspend fun getUser(userId: String): User? {
        return userCache[userId] ?: loadUserFromDatabase(userId)?.also { user ->
            userCache[userId] = user
        }
    }

    // Extension functions for common operations
    suspend fun cacheUser(user: User) {
        userCache[user.id] = user
    }

    fun removeUser(userId: String) {
        userCache -= userId  // Operator overloading for removal
    }

    // Batch operations with extension functions
    suspend fun cacheUsers(users: List<User>) {
        users.forEach { user ->
            userCache[user.id] = user
        }
    }

    // Cache warming with extension functions
    suspend fun warmCache() {
        val popularUsers = loadPopularUsers()
        popularUsers.forEach { user ->
            userCache[user.id] = user
        }
    }

    // Statistics with extension properties
    fun getCacheStats(): String {
        return buildString {
            with(userCache.stats()) {
                append("Hit Rate: ${(hitRate() * 100).toInt()}%\n")
                append("Size: ${userCache.size()}\n")
                append("Evictions: ${evictionCount()}\n")
                append("Avg Load Time: ${averageLoadTime()}ms")
            }
        }
    }

    // Type-safe cache operations
    inline fun <reified T> getCacheValue(key: String): T? {
        return userCache[key] as? T
    }

    // Extension function for conditional caching
    suspend fun cacheIfValid(user: User): Boolean {
        return if (user.email.isNotBlank()) {
            userCache[user.id] = user
            true
        } else {
            false
        }
    }

    private suspend fun loadUserFromDatabase(userId: String): User? {
        // Simulate database access
        delay(50)
        return User(
            id = userId,
            name = "User $userId",
            email = "user$userId@example.com",
            preferences = mapOf("theme" to "dark", "language" to "en")
        )
    }

    private suspend fun loadPopularUsers(): List<User> {
        // Simulate loading popular users
        delay(100)
        return (1..10).map { id ->
            User(
                id = "popular-$id",
                name = "Popular User $id",
                email = "popular$id@example.com"
            )
        }
    }
}
