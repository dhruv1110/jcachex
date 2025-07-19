package io.github.dhruv1110.jcachex.example.distributed.staticnode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main application class for the Distributed Cache Static Example.
 *
 * This application demonstrates JCacheX distributed cache capabilities with:
 * - TCP-based replication between nodes
 * - REST API for cache operations
 * - Static node discovery configuration
 * - Real-time cache synchronization logging
 */
@SpringBootApplication
@EnableConfigurationProperties
public class DistributedCacheApplication {

    public static void main(String[] args) {
        System.setProperty("java.util.logging.manager", "org.slf4j.bridge.SLF4JBridgeHandler");
        SpringApplication.run(DistributedCacheApplication.class, args);
    }
}
