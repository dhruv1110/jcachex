# JCacheX Spring Boot Example

This example validates Spring integration in three minimal ways:

- Properties-based cache via `application.yml` (named cache: `users`)
- Bean-based cache via `@Bean` (`userCache`)
- Annotation-based caching via Spring `@Cacheable` and JCacheX `@JCacheXCacheable`

## Run

```bash
./gradlew :example:springboot:bootRun
```

Server starts on `http://localhost:8080`.

## Endpoints to test

- Users
  - `GET /users/{id}` (uses Spring `@Cacheable("users")` with properties-based cache)
  - `GET /users/{id}/profile` (uses `@JCacheXCacheable` with `API_CACHE` profile)
- Diagnostics
  - `GET /cache/stats` — aggregated cache stats

## Properties-driven caches

`src/main/resources/application.yml` configures named caches (auto-created by properties):

```yaml
jcachex:
  enabled: true
  autoCreateCaches: true
  default:
    maximumSize: 1000
    expireAfterSeconds: 600
  caches:
    users:
      profile: READ_HEAVY
      maximumSize: 1000
      expireAfterSeconds: 300
```

These are auto-created by `JCacheXAutoConfiguration` and managed by `JCacheXCacheManager`.

## Bean-defined caches

`config/CacheConfiguration.kt` defines additional caches as beans using `JCacheXBuilder`.

## Annotation-based caching

Controllers use Spring's `@Cacheable("users")` and the library’s cache manager routes the calls through JCacheX.

We also demonstrate JCacheX-specific annotation:

```kotlin
@JCacheXCacheable(cacheName = "apiResponses", profile = "API_CACHE", expireAfterWrite = 300)
fun getUserProfile(id: String): UserProfile { ... }
```
Ensure `spring-boot-starter-aop` is on the classpath (already added in this example).

## Notes

- Default `recordStats` is off in core; the example opts in via `recordStats(true)` for demo.
- Boot 2.7.x is used in this example; the library supports Boot 2 and 3 auto-configuration.
