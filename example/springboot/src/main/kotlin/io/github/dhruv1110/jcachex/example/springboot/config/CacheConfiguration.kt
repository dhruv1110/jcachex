package io.github.dhruv1110.jcachex.example.springboot.config

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.JCacheXBuilder
import io.github.dhruv1110.jcachex.example.springboot.model.User
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class CacheConfiguration {

    @Bean
    @Qualifier("userCache")
    fun userCache(): Cache<String, User> =
        JCacheXBuilder.create<String, User>()
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats(true)
            .build()


    // No additional caches needed for the simplified example

}


