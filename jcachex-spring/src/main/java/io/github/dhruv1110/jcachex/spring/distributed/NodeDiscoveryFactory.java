package io.github.dhruv1110.jcachex.spring.distributed;

import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.logging.Logger;

/**
 * Factory service for creating NodeDiscovery instances based on Spring
 * configuration.
 * <p>
 * This factory supports creating various types of node discovery mechanisms:
 * - Kubernetes service discovery
 * - Consul service discovery
 * - Gossip protocol
 * - Static node configuration
 * </p>
 *
 * <h3>Configuration Examples:</h3>
 *
 * <pre>{@code
 * # Kubernetes discovery
 * jcachex:
 *   distributed:
 *     nodeDiscovery:
 *       type: KUBERNETES
 *       kubernetes:
 *         namespace: jcachex
 *         serviceName: jcachex-cluster
 *         labelSelector: app=jcachex
 *
 * # Consul discovery
 * jcachex:
 *   distributed:
 *     nodeDiscovery:
 *       type: CONSUL
 *       consul:
 *         consulHost: localhost:8500
 *         serviceName: jcachex-cluster
 *         datacenter: dc1
 *
 * # Gossip protocol
 * jcachex:
 *   distributed:
 *     nodeDiscovery:
 *       type: GOSSIP
 *       gossip:
 *         seedNodes:
 *           - node1:8080
 *           - node2:8080
 *         gossipIntervalSeconds: 5
 * }</pre>
 *
 * @since 1.0.0
 */
@Service
public class NodeDiscoveryFactory {
    private static final Logger logger = Logger.getLogger(NodeDiscoveryFactory.class.getName());

    /**
     * Creates a NodeDiscovery instance based on the provided configuration.
     *
     * @param config the node discovery configuration
     * @return configured NodeDiscovery instance
     * @throws IllegalArgumentException if the discovery type is not supported
     */
    public NodeDiscovery createNodeDiscovery(JCacheXProperties.NodeDiscoveryConfig config) {
        if (config == null) {
            logger.info("No node discovery configuration provided, using static discovery");
            return null;
        }

        String type = config.getType();
        if (type == null) {
            type = "STATIC";
        }

        logger.info("Creating node discovery of type: " + type);

        switch (type.toUpperCase()) {
            case "KUBERNETES":
                return createKubernetesDiscovery(config);
            case "CONSUL":
                return createConsulDiscovery(config);
            case "GOSSIP":
                return createGossipDiscovery(config);
            case "STATIC":
                return null; // Static discovery doesn't need a NodeDiscovery instance
            default:
                throw new IllegalArgumentException("Unsupported node discovery type: " + type);
        }
    }

    /**
     * Creates a Kubernetes-based node discovery instance.
     *
     * @param config the node discovery configuration
     * @return configured Kubernetes node discovery
     */
    private NodeDiscovery createKubernetesDiscovery(JCacheXProperties.NodeDiscoveryConfig config) {
        JCacheXProperties.KubernetesDiscoveryConfig k8sConfig = config.getKubernetes();

        NodeDiscovery.KubernetesDiscoveryBuilder builder = NodeDiscovery.kubernetes()
                .serviceName(k8sConfig.getServiceName())
                .namespace(k8sConfig.getNamespace())
                .useServiceAccount(k8sConfig.getUseServiceAccount())
                .discoveryInterval(Duration.ofSeconds(config.getDiscoveryIntervalSeconds()))
                .healthCheckInterval(Duration.ofSeconds(config.getHealthCheckIntervalSeconds()))
                .maxRetries(config.getMaxRetries())
                .connectionTimeout(Duration.ofSeconds(config.getConnectionTimeoutSeconds()));

        if (k8sConfig.getLabelSelector() != null) {
            builder.labelSelector(k8sConfig.getLabelSelector());
        }

        if (k8sConfig.getKubeConfigPath() != null) {
            builder.kubeConfigPath(k8sConfig.getKubeConfigPath());
        }

        NodeDiscovery discovery = builder.build();
        logger.info("Created Kubernetes node discovery for namespace: " + k8sConfig.getNamespace());
        return discovery;
    }

    /**
     * Creates a Consul-based node discovery instance.
     *
     * @param config the node discovery configuration
     * @return configured Consul node discovery
     */
    private NodeDiscovery createConsulDiscovery(JCacheXProperties.NodeDiscoveryConfig config) {
        JCacheXProperties.ConsulDiscoveryConfig consulConfig = config.getConsul();

        NodeDiscovery.ConsulDiscoveryBuilder builder = NodeDiscovery.consul()
                .serviceName(consulConfig.getServiceName())
                .consulHost(consulConfig.getConsulHost())
                .datacenter(consulConfig.getDatacenter())
                .enableAcl(consulConfig.getEnableAcl())
                .discoveryInterval(Duration.ofSeconds(config.getDiscoveryIntervalSeconds()))
                .healthCheckInterval(Duration.ofSeconds(config.getHealthCheckIntervalSeconds()))
                .maxRetries(config.getMaxRetries())
                .connectionTimeout(Duration.ofSeconds(config.getConnectionTimeoutSeconds()));

        if (consulConfig.getToken() != null) {
            builder.token(consulConfig.getToken());
        }

        NodeDiscovery discovery = builder.build();
        logger.info("Created Consul node discovery for service: " + consulConfig.getServiceName());
        return discovery;
    }

    /**
     * Creates a Gossip protocol-based node discovery instance.
     *
     * @param config the node discovery configuration
     * @return configured Gossip node discovery
     */
    private NodeDiscovery createGossipDiscovery(JCacheXProperties.NodeDiscoveryConfig config) {
        JCacheXProperties.GossipDiscoveryConfig gossipConfig = config.getGossip();

        NodeDiscovery.GossipDiscoveryBuilder builder = NodeDiscovery.gossip()
                .seedNodes(gossipConfig.getSeedNodes())
                .gossipInterval(Duration.ofSeconds(gossipConfig.getGossipIntervalSeconds()))
                .gossipFanout(gossipConfig.getGossipFanout())
                .nodeTimeout(Duration.ofSeconds(gossipConfig.getNodeTimeoutSeconds()))
                .discoveryInterval(Duration.ofSeconds(config.getDiscoveryIntervalSeconds()))
                .healthCheckInterval(Duration.ofSeconds(config.getHealthCheckIntervalSeconds()))
                .maxRetries(config.getMaxRetries())
                .connectionTimeout(Duration.ofSeconds(config.getConnectionTimeoutSeconds()));

        NodeDiscovery discovery = builder.build();
        logger.info("Created Gossip node discovery with " + gossipConfig.getSeedNodes().size() + " seed nodes");
        return discovery;
    }

    /**
     * Checks if the given discovery type is supported.
     *
     * @param type the discovery type to check
     * @return true if supported, false otherwise
     */
    public boolean isDiscoveryTypeSupported(String type) {
        if (type == null) {
            return false;
        }

        switch (type.toUpperCase()) {
            case "KUBERNETES":
            case "CONSUL":
            case "GOSSIP":
            case "STATIC":
                return true;
            default:
                return false;
        }
    }

    /**
     * Gets the default discovery type.
     *
     * @return the default discovery type
     */
    public String getDefaultDiscoveryType() {
        return "STATIC";
    }
}
