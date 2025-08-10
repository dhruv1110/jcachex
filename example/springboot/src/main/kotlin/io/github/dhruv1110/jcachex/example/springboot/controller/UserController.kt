package io.github.dhruv1110.jcachex.example.springboot.controller

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.example.springboot.model.User
import io.github.dhruv1110.jcachex.example.springboot.model.UserProfile
import io.github.dhruv1110.jcachex.spring.annotations.JCacheXCacheable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    @Qualifier("userCache") private val userCache: Cache<String, User>
) {

    @GetMapping("/users/{id}")
    @Cacheable("users")
    fun getUser(@PathVariable id: String): User {
        Thread.sleep(1000)
        return User(id, "User $id", "user$id@example.com")
    }

    @GetMapping("/users/{id}/profile")
    @JCacheXCacheable(cacheName = "apiResponses", profile = "API_CACHE", expireAfterWrite = 300)
    fun getUserProfile(@PathVariable id: String): UserProfile {
        Thread.sleep(500)
        val profile = UserProfile(
            id, "Profile for User $id", "user$id@example.com",
            mapOf("theme" to "dark", "language" to "en")
        )
        return profile
    }

    @GetMapping("/users/{id}/bean")
    fun getUserViaBean(@PathVariable id: String): User {
        userCache.get(id)?.let { return it }
        Thread.sleep(800)
        val user = User(id, "User $id", "user$id@example.com")
        userCache.put(id, user)
        return user
    }
}


