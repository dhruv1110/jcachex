package io.github.dhruv1110.jcachex.spring.distributed;

import io.github.dhruv1110.jcachex.CacheFactory;
import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.NetworkProtocol;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXAutoConfiguration;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.github.dhruv1110.jcachex.spring.distributed.NodeDiscoveryFactory;

/**
 * Auto-configuration for JCacheX distributed cache integration.
 *
 * <p>
 * This configuration provides seamless integration between JCacheX distributed
 * caching
 * and Spring Boot applications, enabling automatic cluster formation, node
 * discovery,
 * and distributed cache management.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 * <li><strong>Automatic Cluster Formation:</strong> Configures distributed
 * caches based on properties</li>
 * <li><strong>Multiple Consistency Models:</strong> Supports strong, eventual,
 * and session consistency</li>
 * <li><strong>Environment-Aware:</strong> Adapts to different deployment
 * environments</li>
 * <li><strong>Network Protocol Support:</strong> TCP, UDP, HTTP, and WebSocket
 * protocols</li>
 * <li><strong>Replication Configuration:</strong> Configurable replication
 * factors and strategies</li>
 * </ul>
 *
 * <h2>Configuration Properties:</h2>
 *
 * <pre>{@code
 * jcachex:
 *   distributed:
 *     enabled: true
 *     clusterName: "my-cluster"
 *     nodes:
 *       - "cache-1:8080"
 *       - "cache-2:8080"
 *       - "cache-3:8080"
 *     replicationFactor: 2
 *     consistencyLevel: EVENTUAL
 *     networkProtocol: TCP
 *     serialization: KRYO
 *     compression: LZ4
 *     encryption: true
 *   caches:
 *     users:
 *       distributed:
 *         enabled: true
 *         replicationFactor: 3
 *         consistencyLevel: STRONG
 * }</pre>
 *
 * <h2>Usage Examples:</h2>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Service
 *     public class UserService {
 *         @Autowired
 *         private JCacheXDistributedCacheManager cacheManager;
 *
 *         public User getUser(String id) {
 *             DistributedCache<String, User> cache = cacheManager.getDistributedCache("users");
 *             return cache.get(id);
 *         }
 *
 *         public void invalidateUserGlobally(String id) {
 *             DistributedCache<String, User> cache = cacheManager.getDistributedCache("users");
 *             cache.invalidateGlobally(id);
 *         }
 *     }
 * }
 * </pre>
 *
 * @see JCacheXAutoConfiguration
 * @see JCacheXDistributedCacheManager
 * @see JCacheXProperties
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass({
        DistributedCache.class,
        NetworkProtocol.class
})
@ConditionalOnProperty(prefix = "jcachex.distributed", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(JCacheXProperties.class)
@AutoConfigureAfter(JCacheXAutoConfiguration.class)
public class JCacheXDistributedAutoConfiguration {

    private final JCacheXProperties properties;

    public JCacheXDistributedAutoConfiguration(JCacheXProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates distributed cache manager for managing distributed caches.
     *
     * @return configured distributed cache manager
     */
    @Bean
    @ConditionalOnMissingBean(JCacheXDistributedCacheManager.class)
    public JCacheXDistributedCacheManager distributedCacheManager() {
        return new JCacheXDistributedCacheManager(properties);
    }

    /**
     * Creates network protocol configuration for distributed communication.
     *
     * @return configured network protocol
     */
    @Bean
    @ConditionalOnMissingBean(NetworkProtocol.class)
    public NetworkProtocol networkProtocol() {
        JCacheXProperties.NetworkConfig networkConfig = properties.getDefaultConfig().getNetwork();

        NetworkProtocol.ProtocolType protocolType = NetworkProtocol.ProtocolType.valueOf(
                networkConfig.getProtocol().toUpperCase());

        NetworkProtocol.SerializationType serializationType = NetworkProtocol.SerializationType.valueOf(
                networkConfig.getSerialization().toUpperCase());

        NetworkProtocol.CompressionType compressionType = NetworkProtocol.CompressionType.valueOf(
                networkConfig.getCompression().toUpperCase());

        NetworkProtocol.ProtocolBuilder builder;

        // Create appropriate builder based on protocol type
        switch (protocolType) {
            case TCP:
                builder = NetworkProtocol.tcp();
                break;
            case UDP:
                builder = NetworkProtocol.udp();
                break;
            case HTTP:
                builder = NetworkProtocol.http();
                break;
            case WEBSOCKET:
                builder = NetworkProtocol.websocket();
                break;
            default:
                builder = NetworkProtocol.tcp(); // Default to TCP
        }

        return builder
                .serialization(serializationType)
                .compression(compressionType)
                .encryption(networkConfig.getEncryption())
                .port(networkConfig.getPort())
                .build();
    }

    /**
     * Creates node discovery factory for creating various node discovery
     * strategies.
     *
     * @return configured node discovery factory
     */
    @Bean
    @ConditionalOnMissingBean(NodeDiscoveryFactory.class)
    public NodeDiscoveryFactory nodeDiscoveryFactory() {
        return new NodeDiscoveryFactory();
    }

    /**
     * Creates distributed cache factory for programmatic cache creation.
     *
     * @param nodeDiscoveryFactory the node discovery factory
     * @return configured distributed cache factory
     */
    @Bean
    @ConditionalOnMissingBean(JCacheXDistributedCacheFactory.class)
    public JCacheXDistributedCacheFactory distributedCacheFactory(NodeDiscoveryFactory nodeDiscoveryFactory) {
        return new JCacheXDistributedCacheFactory(properties, nodeDiscoveryFactory);
    }

    // TODO: Implement JCacheXClusterTopologyMonitor for cluster health monitoring
    /**
     * Creates cluster topology monitor for monitoring cluster health.
     *
     * @return cluster topology monitor
     */
    /*
     * @Bean
     *
     * @ConditionalOnMissingBean(JCacheXClusterTopologyMonitor.class)
     * public JCacheXClusterTopologyMonitor clusterTopologyMonitor() {
     * return new JCacheXClusterTopologyMonitor(properties);
     * }
     */

    // TODO: Implement JCacheXAdaptiveCacheManager for adaptive cache management
    /**
     * Creates adaptive cache manager that switches between local and distributed
     * based on environment conditions.
     *
     * @return adaptive cache manager
     */
    /*
     * @Bean
     *
     * @ConditionalOnProperty(prefix = "jcachex.adaptive", name = "enabled",
     * havingValue = "true")
     *
     * @ConditionalOnMissingBean(JCacheXAdaptiveCacheManager.class)
     * public JCacheXAdaptiveCacheManager adaptiveCacheManager() {
     * return new JCacheXAdaptiveCacheManager(properties);
     * }
     */
}
