package io.github.dhruv1110.jcachex.example.distributed.staticnode.config;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.KubernetesDistributedCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for setting up the distributed cache with static node
 * discovery.
 */
@Configuration
public class CacheConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfiguration.class);

    @Value("${cache.cluster.name:static-cluster}")
    private String clusterName;

    @Value("${cache.replication.port:8081}")
    private int replicationPort;

    @Value("${cache.nodes:node1:8081,node2:8081,node3:8081}")
    private String[] nodeAddresses;

    @Value("${cache.replication.factor:2}")
    private int replicationFactor;

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public DistributedCache<String, Object> distributedCache() {
        logger.info("🚀 [CacheConfig] Initializing distributed cache...");
        logger.info("🔧 [CacheConfig] Cluster Name: {}", clusterName);
        logger.info("🔧 [CacheConfig] Replication Port: {}", replicationPort);
        logger.info("🔧 [CacheConfig] Server Port: {}", serverPort);
        logger.info("🔧 [CacheConfig] Node Addresses: {}", Arrays.toString(nodeAddresses));
        logger.info("🔧 [CacheConfig] Replication Factor: {}", replicationFactor);

        // Create cache configuration
        CacheConfig<String, Object> cacheConfig = CacheConfig.<String, Object>builder()
                .maximumSize(10000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .build();

        // Build distributed cache with static node discovery
        DistributedCache<String, Object> cache = new KubernetesDistributedCache.Builder<String, Object>()
                .clusterName(clusterName)
                .maxMemoryMB(512) // 512 MB memory limit
                .tcpPort(replicationPort)
                .consistencyLevel(DistributedCache.ConsistencyLevel.EVENTUAL)
                .networkTimeout(Duration.ofSeconds(5))
                .enableReadRepair(true)
                .cacheConfig(cacheConfig)
                .build();

        logger.info("✅ [CacheConfig] Distributed cache initialized successfully!");

        return cache;
    }
}
