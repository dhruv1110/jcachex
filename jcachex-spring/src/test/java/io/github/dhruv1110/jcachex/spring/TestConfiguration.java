package io.github.dhruv1110.jcachex.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Test configuration for Spring Boot integration tests.
 * This minimal configuration enables JCacheX auto-configuration and Spring
 * caching.
 */
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class
})
@EnableCaching
public class TestConfiguration {
    // This class serves as the main configuration for test contexts
    // The auto-configuration will be discovered and applied automatically
}
