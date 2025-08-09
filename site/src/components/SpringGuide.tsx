import React from 'react';
import { useVersion, useTabState } from '../hooks';
import { SPRING_USAGE } from '../constants';
import { Section, Grid, FeatureCard, InstallationGuide, Badge } from './common';
import { MetaTags, Breadcrumbs } from './SEO';
import CodeTabs from './CodeTabs';
import { CodeTab, Feature, Resource } from '../types';
import Layout from './Layout';
import { Container, Typography, Box } from '@mui/material';
import {
    Settings as SettingsIcon,
    Build as BuildIcon,
    Check as CheckIcon,
    Info as InfoIcon,
    Rocket as RocketIcon,
    Analytics as AnalyticsIcon,
    Monitor as MonitorIcon,
    Security as SecurityIcon,
    Api as ApiIcon,
    Extension as ExtensionIcon,
} from '@mui/icons-material';


const SpringGuide: React.FC = () => {
    const { version } = useVersion();
    const { activeTab, setActiveTab } = useTabState('maven');

    // Default SEO data for Spring guide
    const seoData = {
        title: 'Spring Boot Integration Guide - JCacheX',
        description: 'Complete guide to integrating JCacheX with Spring Boot applications. Auto-configuration, profile-based caching, annotations, health checks, and monitoring.',
        keywords: ['Spring Boot cache', 'JCacheX Spring', 'Spring cache integration', 'Spring Boot starter', 'cache annotations', 'cache profiles'],
        canonical: 'https://dhruv1110.github.io/jcachex/spring'
    };

    const setupTabs: CodeTab[] = [
        {
            id: 'maven',
            label: 'Maven',
            language: 'xml',
            code: `<dependencies>
    <!-- JCacheX Spring Boot Integration -->
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-spring</artifactId>
        <version>${version}</version>
    </dependency>

    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot Starter Actuator (Optional) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>`
        },
        {
            id: 'gradle',
            label: 'Gradle',
            language: 'gradle',
            code: `dependencies {
    // JCacheX Spring Boot Integration
    implementation 'io.github.dhruv1110:jcachex-spring:${version}'

    // Spring Boot Starter Web
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // Spring Boot Starter Actuator (Optional)
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}`
        }
    ];

    const configTabs: CodeTab[] = [
        {
            id: 'yaml',
            label: 'application.yml',
            language: 'yaml',
            code: `# JCacheX Spring Boot Configuration
jcachex:
  enabled: true
  autoCreateCaches: true
  default:
    maximumSize: 1000
    expireAfterSeconds: 600
    enableStatistics: true
  caches:
    users:
      profile: READ_HEAVY
      maximumSize: 1000
      expireAfterSeconds: 300

# Spring Boot Actuator Configuration (optional)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches
  endpoint:
    health:
      show-details: always`
        },
        {
            id: 'properties',
            label: 'application.properties',
            language: 'properties',
            code: `# JCacheX Spring Boot Configuration
jcachex.enabled=true
jcachex.autoCreateCaches=true
jcachex.default.maximumSize=1000
jcachex.default.expireAfterSeconds=600
jcachex.default.enableStatistics=true

jcachex.caches.users.profile=READ_HEAVY
jcachex.caches.users.maximumSize=1000
jcachex.caches.users.expireAfterSeconds=300

# Spring Boot Actuator (optional)
management.endpoints.web.exposure.include=health,info,metrics,caches
management.endpoint.health.show-details=always

# Application
spring.application.name=jcachex-spring-demo`
        }
    ];

    const annotationTabs: CodeTab[] = [
        {
            id: 'service',
            label: 'Service Layer',
            language: 'java',
            code: `@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    // Profile-based caching for read-heavy operations
    @JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
    public User findById(Long id) {
        log.info("Loading user from database: {}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    // High-performance caching for frequently accessed data
    @JCacheXCacheable(
        cacheName = "userProfiles",
        profile = "HIGH_PERFORMANCE",
        key = "#userId"
    )
    public UserProfile getUserProfile(String userId) {
        log.info("Building user profile for: {}", userId);
        return buildUserProfile(userId);
    }

    // Write-heavy caching for frequently updated data
    @JCacheXCacheable(
        cacheName = "userPreferences",
        profile = "WRITE_HEAVY",
        condition = "#result != null"
    )
    public UserPreferences getUserPreferences(Long userId) {
        log.info("Loading user preferences: {}", userId);
        return userRepository.findPreferencesById(userId);
    }

    // Cache eviction with profile support
    @JCacheXCacheEvict(cacheName = "users", profile = "READ_HEAVY")
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = findById(id);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return userRepository.save(user);
    }

    // Clear multiple caches with different profiles
    @JCacheXCacheEvict(
        cacheName = {"users", "userProfiles", "userPreferences"},
        allEntries = true
    )
    public void clearAllUserCaches() {
        log.info("Clearing all user-related caches");
    }
}`
        },
        {
            id: 'controller',
            label: 'REST Controller',
            language: 'java',
            code: `@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable String id) {
        UserProfile profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}/preferences")
    public ResponseEntity<UserPreferences> getUserPreferences(@PathVariable Long id) {
        UserPreferences preferences = userService.getUserPreferences(id);
        return ResponseEntity.ok(preferences);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        User user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/cache")
    public ResponseEntity<Void> clearCache() {
        userService.clearAllUserCaches();
        return ResponseEntity.ok().build();
    }
}`
        },
        {
            id: 'config',
            label: 'Java Configuration',
            language: 'java',
            code: `@Configuration
@EnableCaching
public class CacheConfiguration {

    // Example of a bean-defined cache alongside properties-created caches
    @Bean("userCache")
    public Cache<String, User> userCache() {
        return JCacheXBuilder.create()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats(true)
            .build();
    }
}`
        }
    ];

    const monitoringTabs: CodeTab[] = [
        {
            id: 'health',
            label: 'Health Indicators',
            language: 'java',
            code: `@Component
public class CacheHealthIndicator implements HealthIndicator {

    @Autowired
    private JCacheXCacheManager cacheManager;

    @Override
    public Health health() {
        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            Health.Builder builder = Health.up();

            builder.withDetail("cacheCount", cacheNames.size());

            for (String cacheName : cacheNames) {
                org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
                if (springCache instanceof JCacheXSpringCache) {
                    JCacheXSpringCache jcacheXCache = (JCacheXSpringCache) springCache;
                    CacheStats stats = jcacheXCache.getStats();

                    if (stats != null) {
                        Map<String, Object> cacheDetails = Map.of(
                            "size", jcacheXCache.getNativeCache().size(),
                            "hitRate", Math.round(stats.hitRate() * 100) + "%",
                            "hitCount", stats.hitCount(),
                            "missCount", stats.missCount()
                        );
                        builder.withDetail(cacheName, cacheDetails);
                    }
                }
            }

            return builder.build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}`
        },
        {
            id: 'metrics',
            label: 'Micrometer Metrics',
            language: 'java',
            code: `@Component
public class CacheMetrics implements MeterBinder {

    @Autowired
    private JCacheXCacheManager cacheManager;

    @Override
    public void bindTo(MeterRegistry registry) {
        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);

            if (springCache instanceof JCacheXSpringCache) {
                bindCacheMetrics(registry, cacheName, (JCacheXSpringCache) springCache);
            }
        }
    }

    private void bindCacheMetrics(MeterRegistry registry, String cacheName,
                                  JCacheXSpringCache cache) {

        Tags tags = Tags.of("cache", cacheName);

        // Cache size
        Gauge.builder("cache.size")
            .tags(tags)
            .register(registry, cache, c -> c.getNativeCache().size());

        // Hit rate
        Gauge.builder("cache.hit_rate")
            .tags(tags)
            .register(registry, cache, c -> {
                CacheStats stats = c.getStats();
                return stats != null ? stats.hitRate() : 0.0;
            });

        // Miss rate
        Gauge.builder("cache.miss_rate")
            .tags(tags)
            .register(registry, cache, c -> {
                CacheStats stats = c.getStats();
                return stats != null ? stats.missRate() : 0.0;
            });
    }
}`
        }
    ];

    const annotations: Feature[] = [
        {
            icon: '🎯',
            title: '@JCacheXCacheable',
            description: 'Cache method results with profile-based optimization',
            details: ['Profile support', 'Custom TTL', 'Conditional caching', 'Key generation']
        },
        {
            icon: '🗑️',
            title: '@JCacheXCacheEvict',
            description: 'Remove entries from cache with profile awareness',
            details: ['Profile-based eviction', 'Single entry eviction', 'Clear all entries', 'Conditional eviction']
        },
        {
            icon: '🔄',
            title: '@JCacheXCachePut',
            description: 'Update cache entries with profile configuration',
            details: ['Profile support', 'Force cache update', 'Custom value expression', 'Conditional updates']
        },
        {
            icon: '📦',
            title: '@JCacheXCaching',
            description: 'Combine multiple cache operations with profiles',
            details: ['Multiple profiles', 'Complex caching scenarios', 'Batch operations', 'Performance optimization']
        }
    ];

    const features: Feature[] = [
        {
            icon: '🚀',
            title: 'Profile-Based Auto-Configuration',
            description: 'Zero-configuration setup with intelligent profiles',
            details: ['READ_HEAVY optimization', 'WRITE_HEAVY optimization', 'HIGH_PERFORMANCE tuning', 'Environment-based config']
        },
        {
            icon: '⚙️',
            title: 'Flexible Configuration',
            description: 'Comprehensive configuration options via profiles',
            details: ['Per-cache profiles', 'Global profile defaults', 'Runtime profile switching', 'Custom profile creation']
        },
        {
            icon: '📊',
            title: 'Enhanced Actuator Integration',
            description: 'Built-in health checks and metrics with profile insights',
            details: ['Profile-specific metrics', 'Health indicators', 'Cache statistics', 'Performance monitoring']
        }
    ];

    const resources: Resource[] = [
        {
            title: 'Getting Started',
            description: 'Basic usage and configuration guide',
            icon: '📖',
            href: '/getting-started',
            badge: 'primary'
        },
        {
            title: 'Examples',
            description: 'Comprehensive examples and use cases',
            icon: '💡',
            href: '/examples',
            badge: 'success'
        },
        {
            title: 'API Documentation',
            description: 'Complete API reference and JavaDoc',
            icon: '📚',
            href: 'https://javadoc.io/doc/io.github.dhruv1110/jcachex-spring',
            badge: 'info'
        }
    ];

    const navigationItems = [
        {
            id: 'spring-setup',
            title: 'Setup',
            icon: <SettingsIcon />,
            children: [
                { id: 'maven', title: 'Installation', icon: <BuildIcon /> },
                { id: 'enable-caching', title: 'Enable Caching', icon: <SettingsIcon /> },
                { id: 'service-caching', title: 'Add Caching to Services', icon: <CheckIcon /> }
            ]
        },
        {
            id: 'spring-usage',
            title: 'Usage',
            icon: <ApiIcon />,
            children: [
                { id: 'spring-annotations', title: 'Annotations', icon: <ExtensionIcon /> }
            ]
        },
        {
            id: 'spring-monitoring',
            title: 'Monitoring',
            icon: <MonitorIcon />,
            children: [
                { id: 'health-endpoint', title: 'Health', icon: <AnalyticsIcon /> },
                { id: 'metrics-endpoint', title: 'Metrics', icon: <MonitorIcon /> },
                { id: 'management-controller', title: 'Management API', icon: <SecurityIcon /> }
            ]
        },
        {
            id: 'spring-examples',
            title: 'Examples',
            icon: <InfoIcon />,
            children: [
                { id: 'rest-controller', title: 'REST API', icon: <BuildIcon /> },
                { id: 'security-service', title: 'Security/Session', icon: <ExtensionIcon /> }
            ]
        }
    ];

    const sidebarConfig = {
        title: "Spring Boot Guide",
        navigationItems: navigationItems,
        expandedByDefault: true
    };

    return (
        <Layout sidebarConfig={sidebarConfig}>
            <Container
                maxWidth={false}
                sx={{
                    py: 4,
                    px: { xs: 2, sm: 3, md: 0 }, // Remove horizontal padding on desktop since Layout handles sidebar offset
                    pr: { xs: 2, sm: 3, md: 4 }, // Keep right padding on desktop
                    pl: { xs: 2, sm: 3, md: 0 }, // Remove left padding on desktop
                    ml: { xs: 0, md: 0 }, // No extra margin on mobile
                    mt: { xs: 1, md: 0 }, // Small top margin on mobile to avoid FAB overlap
                    minHeight: { xs: 'calc(100vh - 80px)', md: 'auto' }, // Ensure full height on mobile
                }}
            >
                <MetaTags seo={seoData} />
                <Breadcrumbs items={[
                    { label: 'Home', path: '/' },
                    { label: 'Spring Boot', path: '/spring', current: true }
                ]} />

                {/* Header */}
                <Section padding="lg" centered className="jcx-surface">
                    <div className="spring-header">
                        <Typography variant="h2" component="h1" gutterBottom sx={{ fontWeight: 700, mb: 2 }}>
                            Spring Boot Integration
                        </Typography>
                        <Typography variant="h6" color="text.secondary" sx={{ mb: 4 }}>
                            Complete guide to integrating JCacheX with Spring Boot applications.
                            From zero to production-ready caching in minutes.
                        </Typography>
                    </div>
                </Section>

                {/* Why JCacheX + Spring Boot */}
                <Section padding="lg" className="jcx-surface">
                    <div className="intro-section">
                        <h2 className="section-title">Why JCacheX + Spring Boot?</h2>
                        <p className="section-description">
                            Spring Boot provides excellent caching abstractions, but most cache providers are either
                            too simple for production or too complex to configure. JCacheX bridges this gap with
                            enterprise-grade features and zero-configuration setup.
                        </p>

                        <Grid columns={3} gap="lg">
                            <FeatureCard
                                icon="🚀"
                                title="Zero Configuration"
                                description="Works out of the box with sensible defaults"
                                details={['Auto-detection', 'Smart defaults', 'No XML configuration']}
                            />
                            <FeatureCard
                                icon="📊"
                                title="Production Ready"
                                description="Built-in monitoring and management"
                                details={['Health checks', 'Metrics export', 'JMX support']}
                            />
                            <FeatureCard
                                icon="⚡"
                                title="High Performance"
                                description="Optimized for Spring Boot workloads"
                                details={['Async support', 'Thread-safe', 'Low latency']}
                            />
                        </Grid>
                    </div>
                </Section>

                {/* Step-by-Step Setup */}
                <Section padding="lg" className="jcx-surface">
                    <div className="setup-guide">
                        <Box id="maven" />
                        <h2 className="section-title">📋 Step-by-Step Setup</h2>
                        <p className="section-description">
                            Follow these steps to add enterprise-grade caching to your Spring Boot application.
                            Each step builds on the previous one with clear explanations.
                        </p>

                        <div className="setup-steps">
                            <div className="step">
                                <div className="step-header">
                                    <span className="step-number">1</span>
                                    <h3 className="step-title">Add JCacheX Spring Boot Starter</h3>
                                </div>
                                <div className="step-content">
                                    <p>
                                        The Spring Boot starter automatically configures JCacheX and integrates
                                        it with Spring's caching abstraction. No additional configuration needed!
                                    </p>
                                    <CodeTabs tabs={[
                                        {
                                            id: 'maven-setup',
                                            label: 'Maven',
                                            language: 'xml',
                                            code: `<dependencies>
    <!-- JCacheX Spring Integration -->
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-spring</artifactId>
        <version>${version}</version>
    </dependency>

    <!-- Spring Boot Web (if building web app) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot Actuator (for monitoring) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>`
                                        },
                                        {
                                            id: 'gradle-setup',
                                            label: 'Gradle',
                                            language: 'groovy',
                                            code: `dependencies {
    // JCacheX Spring Integration
    implementation 'io.github.dhruv1110:jcachex-spring:${version}'

    // Spring Boot Web (if building web app)
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // Spring Boot Actuator (for monitoring)
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}`
                                        }
                                    ]} />
                                    <div className="step-explanation">
                                        <h4>💡 What you get with the integration:</h4>
                                        <ul>
                                            <li><strong>Auto-configuration</strong>: JCacheX integrates with Spring’s caching abstraction</li>
                                            <li><strong>Spring integration</strong>: Works with @Cacheable, @CacheEvict, etc.</li>
                                            <li><strong>Health checks</strong>: Automatic health indicators for monitoring</li>
                                            <li><strong>Metrics</strong>: Cache statistics exposed to Micrometer/Actuator</li>
                                        </ul>
                                    </div>
                                </div>
                            </div>

                            <div className="step">
                                <div className="step-header">
                                    <span className="step-number">2</span>
                                    <h3 className="step-title"><span id="enable-caching">Enable Caching in Your Application</span></h3>
                                </div>
                                <div className="step-content">
                                    <p>
                                        Add @EnableCaching to your main application class to activate Spring's caching features.
                                        JCacheX will automatically be used as the cache provider.
                                    </p>
                                    <CodeTabs tabs={[
                                        {
                                            id: 'enable-caching',
                                            label: 'Application Class',
                                            language: 'java',
                                            code: `package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching  // This activates Spring's caching support
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

// That's it! JCacheX is now active and ready to use`
                                        }
                                    ]} />
                                    <div className="step-explanation">
                                        <h4>🔍 What @EnableCaching does:</h4>
                                        <ul>
                                            <li><strong>Activates annotations</strong>: @Cacheable, @CacheEvict, @CachePut now work</li>
                                            <li><strong>Proxy creation</strong>: Spring creates proxies for cached methods</li>
                                            <li><strong>Cache manager setup</strong>: JCacheX cache manager is automatically configured</li>
                                        </ul>
                                    </div>
                                </div>
                            </div>

                            <div className="step">
                                <div className="step-header">
                                    <span className="step-number">3</span>
                                    <h3 className="step-title"><span id="service-caching">Add Caching to Your Services</span></h3>
                                </div>
                                <div className="step-content">
                                    <p>
                                        Now you can add caching to any method using Spring's annotations.
                                        Let's start with a simple example that every developer can understand.
                                    </p>
                                    <CodeTabs tabs={[
                                        {
                                            id: 'service-caching',
                                            label: 'User Service with Caching',
                                            language: 'java',
                                            code: `@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Cache user lookups for 30 minutes
    @Cacheable(value = "users", key = "#userId")
    public User findById(Long userId) {
        log.info("Loading user from database: {}", userId);

        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    // Cache expensive search operations for 15 minutes
    @Cacheable(value = "userSearches", key = "#query.toLowerCase()")
    public List<User> searchUsers(String query) {
        log.info("Executing user search: {}", query);

        // Simulate expensive database operation
        return userRepository.findByNameContainingIgnoreCase(query);
    }

    // Remove user from cache when updated
    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) {
        log.info("Updating user and evicting from cache: {}", user.getId());
        return userRepository.save(user);
    }

    // Clear all user caches when doing bulk operations
    @CacheEvict(value = {"users", "userSearches"}, allEntries = true)
    public void deleteAllUsers() {
        log.info("Deleting all users and clearing caches");
        userRepository.deleteAll();
    }

    // Update cache instead of evicting (useful for frequently accessed data)
    @CachePut(value = "users", key = "#result.id")
    public User createUser(CreateUserRequest request) {
        User user = new User(request.getName(), request.getEmail());
        User saved = userRepository.save(user);
        log.info("Created and cached new user: {}", saved.getId());
        return saved;
    }
}`
                                        }
                                    ]} />
                                    <div className="step-explanation">
                                        <h4>🎯 Annotation Explained:</h4>
                                        <ul>
                                            <li><strong>@Cacheable</strong>: Stores method result in cache, returns cached value on subsequent calls</li>
                                            <li><strong>@CacheEvict</strong>: Removes entries from cache when data changes</li>
                                            <li><strong>@CachePut</strong>: Always executes method and updates cache with result</li>
                                            <li><strong>key = "#userId"</strong>: Uses method parameter as cache key</li>
                                            <li><strong>allEntries = true</strong>: Clears entire cache, not just one entry</li>
                                        </ul>
                                    </div>
                                </div>
                            </div>

                            <div className="step">
                                <div className="step-header">
                                    <span className="step-number">4</span>
                                    <h3 className="step-title">Configure Cache Settings (Optional)</h3>
                                </div>
                                <div className="step-content">
                                    <p>
                                        JCacheX works great with zero configuration, but you can customize settings
                                        for optimal performance in your specific use case.
                                    </p>
                                    <CodeTabs tabs={[
                                        {
                                            id: 'application-yml',
                                            label: 'application.yml',
                                            language: 'yaml',
                                            code: `# JCacheX Configuration
jcachex:
  # Enable JCacheX (default: true)
  enabled: true

  # Enable statistics collection (default: true)
  enable-statistics: true

  # Enable JMX management (default: true)
  enable-jmx: true

  # Default cache configuration (applied to all caches)
  default:
    maximum-size: 1000
    expire-after-write-seconds: 1800  # 30 minutes
    expire-after-access-seconds: 900  # 15 minutes
    eviction-strategy: LRU
    record-stats: true

  # Named cache configurations (override defaults)
  caches:
    users:
      maximum-size: 5000
      expire-after-write-seconds: 3600  # 1 hour
      eviction-strategy: LFU
      record-stats: true

    products:
      maximum-size: 10000
      expire-after-write-seconds: 7200  # 2 hours
      eviction-strategy: LRU

    sessions:
      maximum-size: 50000
      expire-after-access-seconds: 1800  # 30 minutes
      eviction-strategy: FIFO

# Spring Boot Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches,jcachex
  endpoint:
    health:
      show-details: always
    caches:
      enabled: true`
                                        },
                                        {
                                            id: 'application-properties',
                                            label: 'application.properties',
                                            language: 'properties',
                                            code: `# JCacheX Configuration
jcachex.enabled=true
jcachex.auto-create-caches=true
jcachex.enable-statistics=true
jcachex.enable-jmx=true

# Default cache configuration
jcachex.default.maximum-size=1000
jcachex.default.expire-after-write-seconds=1800
jcachex.default.expire-after-access-seconds=900
jcachex.default.eviction-strategy=LRU
jcachex.default.record-stats=true

# Named cache configurations
jcachex.caches.users.maximum-size=5000
jcachex.caches.users.expire-after-write-seconds=3600
jcachex.caches.users.eviction-strategy=LFU
jcachex.caches.users.record-stats=true

jcachex.caches.products.maximum-size=10000
jcachex.caches.products.expire-after-write-seconds=7200
jcachex.caches.products.eviction-strategy=LRU

# Spring Boot Actuator
management.endpoints.web.exposure.include=health,info,metrics,caches,jcachex
management.endpoint.health.show-details=always
management.endpoint.caches.enabled=true

# Application
spring.application.name=jcachex-spring-demo
spring.cache.type=jcachex`
                                        }
                                    ]} />
                                </div>
                            </div>
                        </div>
                    </div>
                </Section>

                {/* Annotations Section */}
                <Section padding="lg" className="jcx-surface">
                    <Box id="spring-annotations" />
                    <div className="annotations-guide">
                        <h2 className="section-title">🧩 Annotations</h2>
                        <p className="section-description">
                            Use annotations to declaratively cache method results and manage cache entries.
                        </p>
                        <CodeTabs tabs={annotationTabs} />
                    </div>
                </Section>

                {/* Real-World Spring Boot Examples */}
                <Section padding="lg" className="jcx-surface">
                    <div className="examples-section">
                        <h2 className="section-title">🏗️ Real-World Spring Boot Examples</h2>
                        <p className="section-description">
                            See how JCacheX solves common Spring Boot performance challenges with
                            production-ready code you can use immediately.
                        </p>

                        <div className="example-categories">
                            <div className="example-category">
                                <h3 id="rest-controller">📊 REST API with Database Caching</h3>
                                <p>Complete example of a REST controller with optimized database caching:</p>
                                <CodeTabs tabs={[
                                    {
                                        id: 'rest-controller',
                                        label: 'Product Controller',
                                        language: 'java',
                                        code: `@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(ProductDto.from(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Product> products = productService.searchProducts(query, page, size);
        List<ProductDto> dtos = products.stream()
            .map(ProductDto::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ProductDto.from(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        Product product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ProductDto.from(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Cache individual products for 2 hours
    @Cacheable(value = "products", key = "#id")
    public Product findById(Long id) {
        log.info("Loading product from database: {}", id);
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    // Cache search results for 30 minutes
    @Cacheable(value = "productSearches",
               key = "#query + '_' + #page + '_' + #size")
    public List<Product> searchProducts(String query, int page, int size) {
        log.info("Executing product search: {} (page: {}, size: {})", query, page, size);

        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByNameContainingIgnoreCase(query, pageable)
            .getContent();
    }

    // Update cache when creating new product
    @CachePut(value = "products", key = "#result.id")
    public Product createProduct(CreateProductRequest request) {
        Product product = new Product(request.getName(),
                                    request.getDescription(),
                                    request.getPrice());
        Product saved = productRepository.save(product);
        log.info("Created and cached new product: {}", saved.getId());

        // Clear search caches as new product affects search results
        cacheManager.getCache("productSearches").clear();

        return saved;
    }

    // Evict and update cache when product changes
    @CacheEvict(value = "products", key = "#id")
    @CacheEvict(value = "productSearches", allEntries = true)
    public Product updateProduct(Long id, UpdateProductRequest request) {
        Product product = findById(id);  // This will use cache
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        Product saved = productRepository.save(product);
        log.info("Updated product and evicted from cache: {}", id);

        return saved;
    }

    // Clear all related caches when deleting
    @CacheEvict(value = {"products", "productSearches"}, allEntries = true)
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        log.info("Deleted product and cleared caches: {}", id);
    }
}`
                                    }
                                ]} />
                            </div>

                            <div className="example-category">
                                <h3 id="security-service">🔐 Security & Session Caching</h3>
                                <p>Optimize authentication and session management with intelligent caching:</p>
                                <CodeTabs tabs={[
                                    {
                                        id: 'security-service',
                                        label: 'Authentication Service',
                                        language: 'java',
                                        code: `@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthenticationService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    // Cache user details for 15 minutes
    @Cacheable(value = "userDetails", key = "#username")
    public UserDetails loadUserByUsername(String username) {
        log.info("Loading user details from database: {}", username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return UserPrincipal.create(user);
    }

    // Cache authentication results for 5 minutes (failed attempts)
    @Cacheable(value = "authAttempts", key = "#username", condition = "#result == false")
    public boolean authenticate(String username, String password) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            boolean matches = passwordEncoder.matches(password, userDetails.getPassword());

            if (!matches) {
                log.warn("Failed authentication attempt for user: {}", username);
            }

            return matches;
        } catch (UsernameNotFoundException e) {
            log.warn("Authentication failed - user not found: {}", username);
            return false;
        }
    }

    // Cache valid tokens for their lifetime
    @Cacheable(value = "validTokens", key = "#token")
    public boolean isTokenValid(String token) {
        try {
            return tokenProvider.validateToken(token);
        } catch (Exception e) {
            log.debug("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    // Clear caches when user is updated
    @CacheEvict(value = {"userDetails", "authAttempts"}, key = "#username")
    public void invalidateUserCaches(String username) {
        log.info("Cleared authentication caches for user: {}", username);
    }

    // Clear token cache when user logs out
    @CacheEvict(value = "validTokens", key = "#token")
    public void logout(String token) {
        log.info("Token invalidated and removed from cache");
    }
}

@Component
public class SessionCacheService {

    // Cache active sessions for 30 minutes
    @Cacheable(value = "userSessions", key = "#sessionId")
    public UserSession getSession(String sessionId) {
        // Load session from database or external store
        return sessionRepository.findById(sessionId).orElse(null);
    }

    // Update session cache on activity
    @CachePut(value = "userSessions", key = "#sessionId")
    public UserSession updateSessionActivity(String sessionId) {
        UserSession session = getSession(sessionId);
        if (session != null) {
            session.setLastActivity(Instant.now());
            sessionRepository.save(session);
        }
        return session;
    }

    // Remove session from cache on logout
    @CacheEvict(value = "userSessions", key = "#sessionId")
    public void invalidateSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}`
                                    }
                                ]} />
                            </div>
                        </div>
                    </div>
                </Section>

                {/* Monitoring & Management */}
                <Section padding="lg" className="jcx-surface">
                    <div className="monitoring-section">
                        <h2 className="section-title">📊 Monitoring & Management</h2>
                        <p className="section-description">
                            JCacheX provides comprehensive monitoring through Spring Boot Actuator.
                            Monitor cache performance and health in production.
                        </p>

                        <div className="monitoring-examples">
                            <div className="monitoring-category">
                                <h3 id="health-endpoint">Health Checks</h3>
                                <p>Automatic health indicators show cache status:</p>
                                <CodeTabs tabs={[
                                    {
                                        id: 'health-endpoint',
                                        label: 'GET /actuator/health',
                                        language: 'json',
                                        code: `{
  "status": "UP",
  "components": {
    "jcachex": {
      "status": "UP",
      "details": {
        "caches": {
          "users": {
            "status": "UP",
            "size": 234,
            "hitRate": 0.89,
            "missRate": 0.11
          },
          "products": {
            "status": "UP",
            "size": 1567,
            "hitRate": 0.76,
            "missRate": 0.24
          }
        }
      }
    }
  }
}`
                                    }
                                ]} />
                            </div>

                            <div className="monitoring-category">
                                <h3 id="metrics-endpoint">Cache Metrics</h3>
                                <p>Detailed performance metrics via Micrometer:</p>
                                <CodeTabs tabs={[
                                    {
                                        id: 'metrics-endpoint',
                                        label: 'GET /actuator/metrics/cache.gets',
                                        language: 'json',
                                        code: `{
  "name": "cache.gets",
  "description": "Cache gets",
  "baseUnit": "operations",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 15420
    }
  ],
  "availableTags": [
    {
      "tag": "cache",
      "values": ["users", "products", "userSearches"]
    },
    {
      "tag": "result",
      "values": ["hit", "miss"]
    }
  ]
}`
                                    }
                                ]} />
                            </div>

                            <div className="monitoring-category">
                                <h3 id="management-controller">Custom Management Endpoint</h3>
                                <p>Create custom endpoints for cache management:</p>
                                <CodeTabs tabs={[
                                    {
                                        id: 'management-controller',
                                        label: 'Cache Management Controller',
                                        language: 'java',
                                        code: `@RestController
@RequestMapping("/actuator/jcachex")
@ConditionalOnProperty(name = "management.endpoint.jcachex.enabled", havingValue = "true")
public class JCacheXManagementController {

    private final CacheManager cacheManager;

    public JCacheXManagementController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping
    public Map<String, Object> getCacheOverview() {
        Map<String, Object> overview = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof JCacheXCache) {
                JCacheXCache jcacheXCache = (JCacheXCache) cache;
                CacheStats stats = jcacheXCache.getNativeCache().stats();

                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("size", jcacheXCache.getNativeCache().size());
                cacheInfo.put("hitCount", stats.hitCount());
                cacheInfo.put("missCount", stats.missCount());
                cacheInfo.put("hitRate", stats.hitRate());
                cacheInfo.put("averageLoadTime", stats.averageLoadTime());
                cacheInfo.put("evictionCount", stats.evictionCount());

                overview.put(cacheName, cacheInfo);
            }
        });

        return overview;
    }

    @PostMapping("/{cacheName}/clear")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok("Cache '" + cacheName + "' cleared successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/clear-all")
    public ResponseEntity<String> clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        return ResponseEntity.ok("All caches cleared successfully");
    }

    @GetMapping("/{cacheName}/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache instanceof JCacheXCache) {
            JCacheXCache jcacheXCache = (JCacheXCache) cache;
            CacheStats stats = jcacheXCache.getNativeCache().stats();

            Map<String, Object> statsMap = new HashMap<>();
            statsMap.put("hitCount", stats.hitCount());
            statsMap.put("missCount", stats.missCount());
            statsMap.put("hitRate", stats.hitRate());
            statsMap.put("missRate", stats.missRate());
            statsMap.put("loadCount", stats.loadCount());
            statsMap.put("averageLoadTime", stats.averageLoadTime());
            statsMap.put("evictionCount", stats.evictionCount());

            return ResponseEntity.ok(statsMap);
        }
        return ResponseEntity.notFound().build();
    }
}`
                                    }
                                ]} />
                            </div>
                        </div>
                    </div>
                </Section>

                {/* Best Practices for Spring Boot */}
                <Section padding="lg" className="jcx-surface">
                    <div className="best-practices">
                        <h2 className="section-title">🎯 Spring Boot Best Practices</h2>

                        <Grid columns={2} gap="lg">
                            <div className="practices-section">
                                <h3>✅ Spring Boot Specific Do's</h3>
                                <ul className="practices-list">
                                    <li><strong>Use profiles for different environments</strong> - Dev/staging/prod configs</li>
                                    <li><strong>Enable Actuator endpoints</strong> - Monitor cache health and metrics</li>
                                    <li><strong>Use conditional caching</strong> - Cache based on user roles or conditions</li>
                                    <li><strong>Implement cache warming</strong> - Pre-populate caches on startup</li>
                                    <li><strong>Use async caching</strong> - For non-blocking cache operations</li>
                                    <li><strong>Configure proper timeouts</strong> - For distributed scenarios</li>
                                    <li><strong>Use SpEL expressions</strong> - For dynamic cache keys and conditions</li>
                                </ul>
                            </div>

                            <div className="practices-section">
                                <h3>❌ Spring Boot Specific Don'ts</h3>
                                <ul className="practices-list">
                                    <li><strong>Don't cache in @Transactional methods</strong> - Can cause data consistency issues</li>
                                    <li><strong>Don't use caching with @Async</strong> - Proxy issues can occur</li>
                                    <li><strong>Don't cache large objects</strong> - Can cause memory pressure</li>
                                    <li><strong>Don't forget error handling</strong> - Cache exceptions should be handled</li>
                                    <li><strong>Don't cache user-specific data globally</strong> - Security risk</li>
                                    <li><strong>Don't cache everything</strong> - Only cache expensive operations</li>
                                    <li><strong>Don't ignore cache statistics</strong> - Monitor performance regularly</li>
                                </ul>
                            </div>
                        </Grid>
                    </div>
                </Section>

                {/* Production Deployment */}
                <Section padding="lg" centered className="jcx-surface">
                    <div className="deployment-section">
                        <h2 className="deployment-title">🚀 Ready for Production?</h2>
                        <p className="deployment-subtitle">
                            Your Spring Boot application now has enterprise-grade caching!
                            Deploy with confidence using these final tips.
                        </p>

                        <div className="deployment-checklist">
                            <h3>Pre-Production Checklist</h3>
                            <ul>
                                <li>✅ Cache hit rates &gt; 80% for frequently accessed data</li>
                                <li>✅ Memory usage under 70% of allocated heap</li>
                                <li>✅ Health checks passing consistently</li>
                                <li>✅ Metrics being collected and monitored</li>
                                <li>✅ Cache invalidation strategies tested</li>
                                <li>✅ Load testing completed with caching enabled</li>
                            </ul>
                        </div>

                        <div className="next-steps-buttons">
                            <a href="/examples" className="btn btn-primary">
                                More Examples
                            </a>
                            <a href="/faq" className="btn btn-secondary">
                                Common Questions
                            </a>
                            <a href="https://github.com/dhruv1110/JCacheX/tree/main/examples/springboot" className="btn btn-secondary">
                                Complete Example on GitHub
                            </a>
                        </div>
                    </div>
                </Section>
            </Container>
        </Layout>
    );
};

export default SpringGuide;
