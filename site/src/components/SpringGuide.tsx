import React from 'react';
import { useVersion, useTabState, useSEO } from '../hooks';
import { SPRING_USAGE } from '../constants';
import { Section, Grid, FeatureCard, InstallationGuide, Badge } from './common';
import { MetaTags } from './SEO';
import CodeTabs from './CodeTabs';
import { CodeTab, Feature, Resource } from '../types';
import './SpringGuide.css';

const SpringGuide: React.FC = () => {
    const { version } = useVersion();
    const { activeTab, setActiveTab } = useTabState('maven');
    const { getCurrentPageSEO } = useSEO();
    const seoData = getCurrentPageSEO();

    const setupTabs: CodeTab[] = [
        {
            id: 'maven',
            label: 'Maven',
            language: 'xml',
            code: `<dependencies>
    <!-- JCacheX Spring Boot Starter -->
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-spring-boot-starter</artifactId>
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
    // JCacheX Spring Boot Starter
    implementation 'io.github.dhruv1110:jcachex-spring-boot-starter:${version}'

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
            code: `# JCacheX Configuration
jcachex:
  enabled: true
  auto-create-caches: true
  enable-statistics: true
  enable-jmx: true

  # Default cache configuration
  default:
    maximum-size: 1000
    expire-after-write-seconds: 1800  # 30 minutes
    expire-after-access-seconds: 900  # 15 minutes
    eviction-strategy: LRU
    record-stats: true

  # Named cache configurations
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
      enabled: true

# Application Configuration
spring:
  application:
    name: jcachex-spring-demo
  cache:
    type: jcachex  # Use JCacheX as the cache provider`
        },
        {
            id: 'properties',
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

    // Basic caching with default configuration
    @JCacheXCacheable(cacheName = "users")
    public User findById(Long id) {
        log.info("Loading user from database: {}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    // Custom cache configuration
    @JCacheXCacheable(
        cacheName = "userProfiles",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public UserProfile getUserProfile(String userId) {
        log.info("Building user profile for: {}", userId);
        return buildUserProfile(userId);
    }

    // Cache eviction
    @JCacheXCacheEvict(cacheName = "users")
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = findById(id);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return userRepository.save(user);
    }

    // Clear all cache entries
    @JCacheXCacheEvict(cacheName = "users", allEntries = true)
    public void clearAllUsers() {
        log.info("Clearing all user caches");
        userRepository.deleteAll();
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

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        User user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/cache")
    public ResponseEntity<Void> clearCache() {
        userService.clearAllUsers();
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

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new JCacheXCacheManager();
    }

    @Bean
    public JCacheXCacheFactory cacheFactory() {
        return new JCacheXCacheFactory();
    }

    // Custom cache configuration
    @Bean("customCache")
    public Cache<String, Object> customCache() {
        CacheConfig<String, Object> config = CacheConfig.<String, Object>builder()
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofMinutes(30))
            .evictionStrategy(EvictionStrategy.LRU)
            .recordStats(true)
            .build();

        return new DefaultCache<>(config);
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
                            "hitRate", String.format("%.2f%%", stats.hitRate() * 100),
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
            description: 'Cache method results with flexible configuration',
            details: ['Custom TTL', 'Conditional caching', 'Key generation']
        },
        {
            icon: '🗑️',
            title: '@JCacheXCacheEvict',
            description: 'Remove entries from cache after method execution',
            details: ['Single entry eviction', 'Clear all entries', 'Conditional eviction']
        },
        {
            icon: '🔄',
            title: '@JCacheXCachePut',
            description: 'Update cache entries regardless of method outcome',
            details: ['Force cache update', 'Custom value expression', 'Conditional updates']
        },
        {
            icon: '📦',
            title: '@JCacheXCaching',
            description: 'Combine multiple cache operations in one annotation',
            details: ['Multiple cache operations', 'Complex caching scenarios', 'Batch operations']
        }
    ];

    const features: Feature[] = [
        {
            icon: '🚀',
            title: 'Auto-Configuration',
            description: 'Zero-configuration setup with sensible defaults',
            details: ['Automatic cache manager', 'Default cache settings', 'Environment-based config']
        },
        {
            icon: '⚙️',
            title: 'Flexible Configuration',
            description: 'Comprehensive configuration options via properties',
            details: ['Per-cache settings', 'Global defaults', 'Runtime configuration']
        },
        {
            icon: '📊',
            title: 'Actuator Integration',
            description: 'Built-in health checks and metrics endpoints',
            details: ['Health indicators', 'Cache statistics', 'Management endpoints']
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

    return (
        <div className="spring-guide">
            <MetaTags seo={seoData} />

            {/* Header */}
            <Section background="gradient" padding="lg" centered>
                <div className="header-content">
                    <h1 className="page-title">Spring Boot Integration</h1>
                    <p className="page-subtitle">
                        Complete guide to integrating JCacheX with Spring Boot applications
                    </p>
                </div>
            </Section>

            {/* Installation */}
            <Section padding="lg">
                <InstallationGuide
                    tabs={setupTabs}
                    title="1. Add Dependencies"
                    description="Add JCacheX Spring Boot starter to your project:"
                />
            </Section>

            {/* Configuration */}
            <Section
                background="light"
                padding="lg"
                title="2. Configuration"
                subtitle="Configure JCacheX using Spring Boot properties"
                centered
            >
                <div className="code-section">
                    <CodeTabs tabs={configTabs} />
                </div>
            </Section>

            {/* Annotations */}
            <Section
                padding="lg"
                title="3. Annotations"
                subtitle="Use JCacheX annotations to add caching to your methods"
                centered
            >
                <div className="code-section">
                    <CodeTabs tabs={annotationTabs} />
                </div>
            </Section>

            {/* Annotation Reference */}
            <Section
                background="light"
                padding="lg"
                title="Annotation Reference"
                subtitle="Available JCacheX annotations and their features"
                centered
            >
                <Grid columns={2} gap="lg">
                    {annotations.map((annotation, index) => (
                        <FeatureCard
                            key={index}
                            icon={annotation.icon}
                            title={annotation.title}
                            description={annotation.description}
                            details={annotation.details}
                            variant="horizontal"
                        />
                    ))}
                </Grid>
            </Section>

            {/* Monitoring */}
            <Section
                padding="lg"
                title="4. Monitoring & Observability"
                subtitle="Monitor cache performance with Spring Boot Actuator"
                centered
            >
                <div className="code-section">
                    <CodeTabs tabs={monitoringTabs} />
                </div>
            </Section>

            {/* Features */}
            <Section
                background="light"
                padding="lg"
                title="Spring Boot Features"
                subtitle="Key features of JCacheX Spring Boot integration"
                centered
            >
                <Grid columns={3} gap="lg">
                    {features.map((feature, index) => (
                        <FeatureCard
                            key={index}
                            icon={feature.icon}
                            title={feature.title}
                            description={feature.description}
                            details={feature.details}
                            variant="compact"
                        />
                    ))}
                </Grid>
            </Section>

            {/* Resources */}
            <Section
                padding="lg"
                title="Additional Resources"
                subtitle="Learn more about JCacheX and Spring Boot integration"
                centered
            >
                <Grid columns={3} gap="lg">
                    {resources.map((resource, index) => (
                        <div key={index} className="resource-card">
                            <div className="resource-icon">{resource.icon}</div>
                            <h3 className="resource-title">{resource.title}</h3>
                            <p className="resource-description">{resource.description}</p>
                            <div className="resource-action">
                                <Badge
                                    variant={resource.badge}
                                    href={resource.href}
                                    target={resource.href.startsWith('http') ? '_blank' : '_self'}
                                >
                                    {resource.href.startsWith('http') ? 'Open' : 'View'}
                                </Badge>
                            </div>
                        </div>
                    ))}
                </Grid>
            </Section>

            {/* Next Steps */}
            <Section background="gradient" padding="lg" centered>
                <div className="next-steps">
                    <h2 className="next-steps-title">Ready to build?</h2>
                    <p className="next-steps-subtitle">
                        Start using JCacheX in your Spring Boot application
                    </p>
                    <div className="next-steps-buttons">
                        <Badge variant="primary" size="large" href="/examples">
                            View Examples
                        </Badge>
                        <Badge variant="default" size="large" href="/getting-started">
                            Getting Started
                        </Badge>
                    </div>
                </div>
            </Section>
        </div>
    );
};

export default SpringGuide;
