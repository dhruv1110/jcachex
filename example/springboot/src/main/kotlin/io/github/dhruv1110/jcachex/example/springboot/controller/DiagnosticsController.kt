package io.github.dhruv1110.jcachex.example.springboot.controller

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.example.springboot.model.User
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DiagnosticsController(
    @Qualifier("userCache") private val userCache: Cache<String, User>
) {
    companion object { private const val PERCENT = "%.2f%%" }

    @GetMapping("/cache/stats")
    fun getCacheStats(): Map<String, Any> {
        val userStats = userCache.stats()
        return mapOf(
            "userCache" to mapOf(
                "size" to userCache.size(),
                "hitRate" to String.format(PERCENT, userStats.hitRate() * 100),
                "hitCount" to userStats.hitCount(),
                "missCount" to userStats.missCount(),
                "evictionCount" to userStats.evictionCount(),
                "requestCount" to (userStats.hitCount() + userStats.missCount())
            )
        )
    }
}


