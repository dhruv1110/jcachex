import io.github.dhruv1110.jcachex.*;
import java.time.Duration;
import java.time.LocalDateTime;

// Domain object for caching
public class UserProfile {
    private final String userId;
    private final String name;
    private final String email;
    private final LocalDateTime lastLogin;

    public UserProfile(String userId, String name, String email, LocalDateTime lastLogin) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.lastLogin = lastLogin;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    @Override
    public String toString() {
        return String.format("UserProfile{userId='%s', name='%s', email='%s', lastLogin=%s}",
                userId, name, email, lastLogin);
    }
}

public class ObjectCacheExample {
    public static void main(String[] args) {
        // Configure cache for user profiles
        CacheConfig<String, UserProfile> config = CacheConfig.<String, UserProfile>builder()
                .maximumSize(500L)
                .expireAfterWrite(Duration.ofHours(4))
                .evictionStrategy(EvictionStrategy.LRU)
                .recordStats(true)
                .build();

        Cache<String, UserProfile> userCache = new DefaultCache<>(config);

        // Create and cache user profiles
        UserProfile user1 = new UserProfile("123", "Alice Johnson", "alice@example.com", LocalDateTime.now());
        UserProfile user2 = new UserProfile("456", "Bob Wilson", "bob@example.com", LocalDateTime.now().minusHours(2));

        userCache.put(user1.getUserId(), user1);
        userCache.put(user2.getUserId(), user2);

        // Retrieve cached objects
        UserProfile cachedUser = userCache.get("123");
        if (cachedUser != null) {
            System.out.println("Retrieved user: " + cachedUser);
        }

        // Batch operations
        Map<String, UserProfile> batch = Map.of(
                "789", new UserProfile("789", "Charlie Brown", "charlie@example.com", LocalDateTime.now()),
                "101",
                new UserProfile("101", "Diana Prince", "diana@example.com", LocalDateTime.now().minusMinutes(30)));

        userCache.putAll(batch);

        // Performance monitoring
        CacheStats stats = userCache.stats();
        System.out.println("Cache performance:");
        System.out.println("  Requests: " + stats.requestCount());
        System.out.println("  Hits: " + stats.hitCount());
        System.out.println("  Misses: " + stats.missCount());
        System.out.println("  Hit rate: " + String.format("%.2f%%", stats.hitRate() * 100));
    }
}
