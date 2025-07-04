# JCacheX Kotlin Example

This example showcases **JCacheX** with **Kotlin Coroutines** and demonstrates advanced session management patterns for modern web applications.

## 🎯 What This Example Shows

- **🔐 Session Management**: User sessions with automatic expiration
- **⚡ Kotlin Coroutines**: Suspend functions and async caching operations
- **🎨 Kotlin DSL**: Fluent cache configuration with type-safe builders
- **🛡️ Security Patterns**: Token validation and session cleanup
- **📱 Real-time Features**: WebSocket session tracking
- **🧪 Testing**: Coroutine-friendly testing patterns

## 🚀 Running the Example

```bash
# From the example/kotlin directory
./gradlew run

# Or with your IDE: Run Main.kt
```

## 📋 Key Features Demonstrated

### 1. **Session Service**
- **User Sessions**: 100k concurrent sessions supported
- **Auto-Expiration**: 30-minute idle timeout
- **Security**: Token-based authentication
- **Cleanup**: Automatic expired session removal

### 2. **Kotlin Extensions**
- **Operator Overloading**: `cache["key"] = value` syntax
- **Suspend Functions**: `getSuspend()`, `putSuspend()`
- **DSL Builders**: Type-safe cache configuration
- **Collection Operations**: `filterKeys()`, `mapValues()`

### 3. **Real-time Features**
- **WebSocket Sessions**: Live connection tracking
- **Presence System**: User online/offline status
- **Event Broadcasting**: Real-time notifications
- **Coroutine Flows**: Reactive session updates

### 4. **Advanced Patterns**
- **Batch Operations**: Bulk session operations
- **Conditional Updates**: `computeIfPresent()` patterns
- **Safe Nullability**: Null-safe cache operations
- **Performance Monitoring**: Coroutine-aware metrics

## 📖 Code Structure

```
src/main/kotlin/
├── Main.kt                            # Main demo application
├── session/
│   ├── SessionService.kt              # Core session management
│   ├── UserSession.kt                 # Session data model
│   ├── SessionManager.kt              # Advanced session operations
│   └── SecurityService.kt             # Authentication helpers
├── websocket/
│   ├── WebSocketSessionTracker.kt     # Real-time connection tracking
│   ├── PresenceService.kt             # User online/offline status
│   └── EventBroadcaster.kt            # Real-time notifications
├── extensions/
│   ├── CacheExtensions.kt             # Custom Kotlin extensions
│   ├── CoroutineHelpers.kt            # Async utilities
│   └── DSLBuilders.kt                 # Type-safe configuration DSL
└── testing/
    ├── SessionServiceTest.kt          # Coroutine testing examples
    └── MockWebSocketSession.kt        # Test utilities
```

## 🎓 Learning Path

1. **Start with Main.kt**: Overview of all Kotlin features
2. **Study SessionService.kt**: Learn coroutine caching patterns
3. **Explore DSL builders**: See type-safe configuration
4. **Check Extensions**: Understand operator overloading
5. **Review Tests**: Learn coroutine testing

## 🔧 Configuration Examples

### Session Cache with DSL
```kotlin
val sessionCache = cache<String, UserSession> {
    maximumSize = 100_000
    expireAfterAccess = 30.minutes
    evictionStrategy = LRUEvictionStrategy()
    enableStatistics = true

    // Custom weigher for memory management
    weigher { _, session -> session.estimatedSize() }

    // Event listeners
    onEviction { sessionId, session, reason ->
        logger.info("Session $sessionId evicted: $reason")
        cleanupUserResources(session.userId)
    }
}
```

### WebSocket Connection Tracking
```kotlin
class WebSocketSessionTracker {
    private val connections = cache<String, WebSocketConnection> {
        maximumSize = 50_000
        expireAfterWrite = 1.hours

        onRemoval { connectionId, connection, _ ->
            // Notify other users that this user went offline
            broadcastUserOffline(connection.userId)
        }
    }

    suspend fun trackConnection(connectionId: String, userId: String) {
        val connection = WebSocketConnection(userId, Instant.now())
        connections.putSuspend(connectionId, connection)

        // Notify other users that this user is online
        broadcastUserOnline(userId)
    }
}
```

## ⚡ Coroutine Integration Examples

### Suspend Function Caching
```kotlin
class UserService {
    private val userCache = cache<String, User> {
        maximumSize = 10_000
        expireAfterWrite = 15.minutes
    }

    suspend fun getUser(id: String): User {
        return userCache.getOrComputeSuspend(id) {
            // This lambda is a suspend function
            userRepository.findByIdSuspend(it)
        }
    }

    suspend fun updateUserPresence(userId: String) {
        userCache.computeIfPresentSuspend(userId) { _, user ->
            user.copy(lastSeen = Instant.now())
        }
    }
}
```

### Flow-based Cache Updates
```kotlin
class SessionEventStream {
    private val sessionCache = cache<String, UserSession>()

    fun sessionUpdates(): Flow<SessionEvent> = flow {
        // Watch for session changes and emit events
        sessionCache.asFlow()
            .filter { it.event == CacheEvent.UPDATED }
            .map { SessionEvent.Updated(it.key, it.newValue) }
            .collect { emit(it) }
    }
}
```

## 🧪 Testing with Coroutines

### Coroutine-Friendly Testing
```kotlin
@Test
fun `should manage sessions correctly`() = runTest {
    val sessionService = SessionService(fakeCache())

    // Create session
    val sessionId = sessionService.createSession(testUser)

    // Verify session exists
    val session = sessionService.getSession(sessionId)
    assertNotNull(session)
    assertEquals(testUser.id, session?.userId)

    // Test expiration
    advanceTimeBy(31.minutes)
    val expiredSession = sessionService.getSession(sessionId)
    assertNull(expiredSession)
}
```

## 📈 Expected Performance

This example demonstrates:
- **Session Creation**: 50k+ sessions/second
- **Lookup Performance**: < 1μs for cached sessions
- **Memory Efficiency**: ~64 bytes per session
- **Coroutine Overhead**: < 10% vs blocking operations
- **Cleanup Efficiency**: Automatic expired session removal

## 🎨 Kotlin-Specific Features

### Operator Overloading
```kotlin
// Natural syntax for cache operations
sessionCache["session123"] = userSession
val session = sessionCache["session123"]
val exists = "session123" in sessionCache

// Bulk operations
sessionCache += mapOf(
    "session1" to session1,
    "session2" to session2
)
```

### Type-Safe Extensions
```kotlin
// Collection-like operations with type safety
val activeSessions = sessionCache.filterValues { it.isActive }
val sessionSummary = sessionCache.mapValues { it.summary() }
val userSessions = sessionCache.filterKeys { it.startsWith("user_") }
```

### Null Safety
```kotlin
// Null-safe operations
val session = sessionCache.getOrNull("session123")
val safeUpdate = sessionCache.updateIfPresent("session123") { session ->
    session.copy(lastActivity = Instant.now())
}
```

## 🔗 Related Documentation

- [Main Documentation](../../README.md): Complete JCacheX guide
- [Java Example](../java/): Traditional Java patterns
- [Spring Boot Example](../springboot/): Annotation-based caching
- [Kotlin Extensions API](https://javadoc.io/doc/io.github.dhruv1110/jcachex-kotlin): Kotlin-specific documentation

---

💡 **Pro Tip**: Combine coroutines with caching for highly responsive applications. The suspend function support eliminates the need for callback-based async patterns.
