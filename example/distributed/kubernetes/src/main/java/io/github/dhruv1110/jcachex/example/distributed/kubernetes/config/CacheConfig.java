package io.github.dhruv1110.jcachex.example.distributed.kubernetes.config;

import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.communication.TcpCommunicationProtocol;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import io.github.dhruv1110.jcachex.distributed.impl.KubernetesDistributedCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    @Qualifier("distributedCache")
    public DistributedCache<String, String> distributedCache() {
        return KubernetesDistributedCache.<String, String>builder()
                .clusterName("jcachex-kubernetes-example")
                .nodeDiscovery(NodeDiscovery.kubernetes()
                        .namespace("default") // Use default namespace where pods are deployed
                        .build())
                .communicationProtocol(new TcpCommunicationProtocol.Builder<String, String>()
                        .port(8081) // Use different port from web server
                        .build())
                .cacheConfig(
                        io.github.dhruv1110.jcachex.CacheConfig.<String, String>builder()
                                .maximumSize(1000L)
                                .build())
                .build();
    }
}
