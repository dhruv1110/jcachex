package io.github.dhruv.jcachex.example.springboot

import io.github.dhruv.jcachex.spring.EnableJCacheX
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableJCacheX
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@RestController
class UserController {
    @GetMapping("/users/{id}")
    @Cacheable("users")
    fun getUser(@PathVariable id: String): User {
        // Simulate database call
        Thread.sleep(1000)
        return User(id, "User $id")
    }
}

data class User(
    val id: String,
    val name: String
)
