// Basic usage examples with minimal code strings
export const BASIC_USAGE_JAVA = `// The simplest possible start
Cache<String, User> cache = JCacheX.create().build();

// Basic operations
cache.put("user123", new User("Alice", "alice@example.com"));
User user = cache.get("user123");
System.out.println("✅ Retrieved: " + user.getName());`;

export const BASIC_USAGE_KOTLIN = `// Kotlin DSL version
val cache = jcacheX<String, User> {
    profile = Profile.READ_HEAVY
    name = "users"
    maximumSize = 10000
}

// Operator overloading
cache["user123"] = User("Alice", "alice@example.com")
val user = cache["user123"]
println("✅ Retrieved: \${user?.name}")`;

export const SPRING_USAGE = `@Service
public class UserService {
    @JCacheXCacheable(value = "users", profile = "READ_HEAVY")
    public User findUserById(String id) {
        return userRepository.findById(id);
    }
}`;

import type { CodeTab } from '../types';

export const MIGRATION_EXAMPLES: CodeTab[] = [
    {
        id: 'caffeine-migration',
        label: 'From Caffeine',
        language: 'java',
        code: `// Before: Caffeine
Cache<String, User> cache = Caffeine.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(Duration.ofMinutes(5))
    .build();

// After: JCacheX
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .maximumSize(10000L)
    .expireAfterWrite(Duration.ofMinutes(5))
    .build();`
    },
    {
        id: 'redis-migration',
        label: 'From Redis',
        language: 'java',
        code: `// Before: Redis
RedisTemplate<String, User> redis = new RedisTemplate<>();
redis.opsForValue().set("user123", user, Duration.ofMinutes(5));
User user = redis.opsForValue().get("user123");

// After: JCacheX
Cache<String, User> cache = JCacheXBuilder.forApiResponseCaching()
    .expireAfterWrite(Duration.ofMinutes(5))
    .build();
cache.put("user123", user);
User user = cache.get("user123");`
    }
];
