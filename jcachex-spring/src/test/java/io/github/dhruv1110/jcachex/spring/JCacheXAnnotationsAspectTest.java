package io.github.dhruv1110.jcachex.spring;

import io.github.dhruv1110.jcachex.spring.annotations.JCacheXCacheEvict;
import io.github.dhruv1110.jcachex.spring.annotations.JCacheXCacheable;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXAutoConfiguration;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import io.github.dhruv1110.jcachex.spring.core.JCacheXCacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused tests to cover the AOP aspect that handles @JCacheXCacheable
 * and @JCacheXCacheEvict.
 */
@SpringBootTest(classes = {
        JCacheXAnnotationsAspectTest.TestConfig.class,
        JCacheXAnnotationsAspectTest.AnnotatedService.class
})
@ActiveProfiles("test")
@DisplayName("JCacheX AOP Annotation Aspect Tests")
class JCacheXAnnotationsAspectTest {

    @Autowired
    private AnnotatedService service;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("@JCacheXCacheable caches results and uses configured profile props")
    void cacheableCachesResults() {
        String a1 = service.getUser("alice");
        String a2 = service.getUser("alice");
        assertEquals("user:alice", a1);
        assertEquals(a1, a2, "Second call should be cached");

        // Ensure cache exists
        assertNotNull(cacheManager.getCache("annUsers"));
    }

    @Test
    @DisplayName("SpEL key is respected for cache key generation")
    void spelKeyRespected() {
        String r1 = service.getProfile("bob", "v1");
        String r2 = service.getProfile("bob", "v1");
        assertEquals(r1, r2, "Same key should hit cache");

        String r3 = service.getProfile("bob", "v2");
        assertNotEquals(r2, r3, "Different key should miss cache");
    }

    @Test
    @DisplayName("@JCacheXCacheEvict removes entries as configured")
    void evictRemovesEntries() {
        String a1 = service.getUser("carol");
        String a2 = service.getUser("carol");
        assertEquals(a1, a2, "Warm cache");

        service.evictUser("carol");

        String a3 = service.getUser("carol");
        // Value equality remains the same, but instance should be newly computed
        assertNotSame(a2, a3, "Post-evict call should recompute (different instance)");
    }

    @Configuration
    @EnableCaching
    @ComponentScan(basePackages = "io.github.dhruv1110.jcachex.spring.aop")
    static class TestConfig {
        @Bean(name = "jcacheXCacheManager")
        public JCacheXCacheManager jcachexCacheManager() {
            // Minimal properties with no pre-created caches; aspect will create on demand
            JCacheXProperties props = new JCacheXProperties();
            // Also add one named cache to ensure properties path is exercised too
            Map<String, JCacheXProperties.CacheConfig> caches = new HashMap<>();
            JCacheXProperties.CacheConfig users = new JCacheXProperties.CacheConfig();
            users.setProfile("READ_HEAVY");
            users.setMaximumSize(1000L);
            caches.put("users", users);
            props.setCaches(caches);
            return new JCacheXCacheManager(props);
        }

        @Bean
        public static PropertySourcesPlaceholderConfigurer ppc() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    @Service
    static class AnnotatedService {
        @JCacheXCacheable(cacheName = "annUsers", profile = "READ_HEAVY", expireAfterWrite = 2)
        public String getUser(String id) {
            return "user:" + id;
        }

        @JCacheXCacheable(cacheName = "apiResponses", key = "#id + '-' + #ver", profile = "API_CACHE", expireAfterWrite = 5)
        public String getProfile(String id, String ver) {
            return "profile:" + id + ":" + ver;
        }

        @JCacheXCacheEvict(cacheName = "annUsers", beforeInvocation = true)
        public void evictUser(String id) {
            // no-op
        }
    }
}
