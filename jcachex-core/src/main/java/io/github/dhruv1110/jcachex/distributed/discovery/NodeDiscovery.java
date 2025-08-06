package io.github.dhruv1110.jcachex.distributed.discovery;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for discovering cache nodes in distributed environments.
 * <p>
 * This interface provides a pluggable mechanism for node discovery that can
 * work
 * with various service discovery systems including Kubernetes, Consul, etcd, or
 * custom gossip protocols.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Service Discovery Integration:</strong> Kubernetes, Consul, etcd,
 * Zookeeper</li>
 * <li><strong>Gossip Protocol:</strong> Peer-to-peer discovery without central
 * coordinator</li>
 * <li><strong>Seed Nodes:</strong> Bootstrap cluster formation with initial
 * nodes</li>
 * <li><strong>Health Monitoring:</strong> Continuous health checks and failure
 * detection</li>
 * <li><strong>Dynamic Scaling:</strong> Automatic handling of nodes
 * joining/leaving</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Kubernetes discovery
 * NodeDiscovery k8sDiscovery = NodeDiscovery.kubernetes()
 *         .namespace("default")
 *         .serviceName("jcachex-cluster")
 *         .labelSelector("app=jcachex")
 *         .build();
 *
 * // Consul discovery
 * NodeDiscovery consulDiscovery = NodeDiscovery.consul()
 *         .consulHost("localhost:8500")
 *         .serviceName("jcachex-cluster")
 *         .healthCheckInterval(Duration.ofSeconds(10))
 *         .build();
 *
 * // Gossip protocol
 * NodeDiscovery gossipDiscovery = NodeDiscovery.gossip()
 *         .seedNodes("node1:8080", "node2:8080")
 *         .gossipInterval(Duration.ofSeconds(5))
 *         .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public interface NodeDiscovery {

    /**
     * Discovery strategy types.
     */
    enum DiscoveryType {
        KUBERNETES
    }

    /**
     * Node health status.
     */
    enum NodeHealth {
        HEALTHY,
        UNHEALTHY,
        UNKNOWN
    }

    /**
     * Starts the node discovery service.
     *
     * @return CompletableFuture that completes when discovery is started
     */
    CompletableFuture<Void> start();

    /**
     * Stops the node discovery service.
     *
     * @return CompletableFuture that completes when discovery is stopped
     */
    CompletableFuture<Void> stop();

    /**
     * Discovers all available nodes in the cluster.
     *
     * @return CompletableFuture containing discovered nodes
     */
    CompletableFuture<Set<DiscoveredNode>> discoverNodes();

    /**
     * Checks the health of a specific node.
     *
     * @param nodeId the ID of the node to check
     * @return CompletableFuture containing the node's health status
     */
    CompletableFuture<NodeHealth> checkNodeHealth(String nodeId);

    /**
     * Adds a listener for node discovery events.
     *
     * @param listener the listener to add
     */
    void addNodeDiscoveryListener(NodeDiscoveryListener listener);

    /**
     * Removes a listener for node discovery events.
     *
     * @param listener the listener to remove
     */
    void removeNodeDiscoveryListener(NodeDiscoveryListener listener);

    /**
     * Gets the discovery type.
     *
     * @return the discovery type
     */
    DiscoveryType getDiscoveryType();

    /**
     * Gets discovery statistics.
     *
     * @return discovery statistics
     */
    DiscoveryStats getDiscoveryStats();

    /**
     * Creates a Kubernetes-based node discovery.
     *
     * @return Kubernetes discovery builder
     */
    static KubernetesDiscoveryBuilder kubernetes() {
        return new KubernetesDiscoveryBuilder();
    }

    /**
     * Information about a discovered node.
     */
    class DiscoveredNode {
        private final String nodeId;
        private final String address;
        private final int port;
        private final NodeHealth health;
        private final Instant lastSeen;
        private final Map<String, String> metadata;

        public DiscoveredNode(String nodeId, String address, int port, NodeHealth health,
                Instant lastSeen, Map<String, String> metadata) {
            this.nodeId = nodeId;
            this.address = address;
            this.port = port;
            this.health = health;
            this.lastSeen = lastSeen;
            this.metadata = metadata;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public NodeHealth getHealth() {
            return health;
        }

        public Instant getLastSeen() {
            return lastSeen;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public String getFullAddress() {
            return address + ":" + port;
        }

        @Override
        public String toString() {
            return "DiscoveredNode{" +
                    "nodeId='" + nodeId + '\'' +
                    ", address='" + address + '\'' +
                    ", port=" + port +
                    ", health=" + health +
                    ", lastSeen=" + lastSeen +
                    '}';
        }
    }

    /**
     * Listener for node discovery events.
     */
    interface NodeDiscoveryListener {
        /**
         * Called when a new node is discovered.
         *
         * @param node the discovered node
         */
        void onNodeDiscovered(DiscoveredNode node);

        /**
         * Called when a node is no longer available.
         *
         * @param nodeId the ID of the lost node
         */
        void onNodeLost(String nodeId);

        /**
         * Called when a node's health status changes.
         *
         * @param nodeId    the ID of the node
         * @param oldHealth the previous health status
         * @param newHealth the new health status
         */
        void onNodeHealthChanged(String nodeId, NodeHealth oldHealth, NodeHealth newHealth);
    }

    /**
     * Discovery statistics.
     */
    class DiscoveryStats {
        private final long totalDiscoveries;
        private final long successfulDiscoveries;
        private final long failedDiscoveries;
        private final long averageDiscoveryTime;
        private final long activeNodes;
        private final long totalNodesDiscovered;

        public DiscoveryStats(long totalDiscoveries, long successfulDiscoveries,
                long failedDiscoveries, long averageDiscoveryTime,
                long activeNodes, long totalNodesDiscovered) {
            this.totalDiscoveries = totalDiscoveries;
            this.successfulDiscoveries = successfulDiscoveries;
            this.failedDiscoveries = failedDiscoveries;
            this.averageDiscoveryTime = averageDiscoveryTime;
            this.activeNodes = activeNodes;
            this.totalNodesDiscovered = totalNodesDiscovered;
        }

        public long getTotalDiscoveries() {
            return totalDiscoveries;
        }

        public long getSuccessfulDiscoveries() {
            return successfulDiscoveries;
        }

        public long getFailedDiscoveries() {
            return failedDiscoveries;
        }

        public long getAverageDiscoveryTime() {
            return averageDiscoveryTime;
        }

        public long getActiveNodes() {
            return activeNodes;
        }

        public long getTotalNodesDiscovered() {
            return totalNodesDiscovered;
        }

        public double getSuccessRate() {
            return totalDiscoveries == 0 ? 1.0 : (double) successfulDiscoveries / totalDiscoveries;
        }
    }

    /**
     * Base builder for node discovery implementations.
     */
    abstract class BaseDiscoveryBuilder<T extends BaseDiscoveryBuilder<T>> {
        // protected String serviceName = "jcachex-cluster";
        protected Duration discoveryInterval = Duration.ofSeconds(30);
        protected Duration healthCheckInterval = Duration.ofSeconds(10);
        protected int maxRetries = 3;
        protected Duration connectionTimeout = Duration.ofSeconds(5);

        // @SuppressWarnings("unchecked")
        // public T serviceName(String serviceName) {
        // this.serviceName = serviceName;
        // return (T) this;
        // }

        @SuppressWarnings("unchecked")
        public T discoveryInterval(Duration discoveryInterval) {
            this.discoveryInterval = discoveryInterval;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T healthCheckInterval(Duration healthCheckInterval) {
            this.healthCheckInterval = healthCheckInterval;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T connectionTimeout(Duration connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return (T) this;
        }

        public abstract NodeDiscovery build();
    }

    /**
     * Builder for Kubernetes node discovery.
     */
    class KubernetesDiscoveryBuilder extends BaseDiscoveryBuilder<KubernetesDiscoveryBuilder> {
        protected String namespace = "default";
        protected String labelSelector;
        protected String kubeConfigPath;
        protected boolean useServiceAccount = true;

        public KubernetesDiscoveryBuilder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public KubernetesDiscoveryBuilder labelSelector(String labelSelector) {
            this.labelSelector = labelSelector;
            return this;
        }

        public KubernetesDiscoveryBuilder kubeConfigPath(String kubeConfigPath) {
            this.kubeConfigPath = kubeConfigPath;
            return this;
        }

        public KubernetesDiscoveryBuilder useServiceAccount(boolean useServiceAccount) {
            this.useServiceAccount = useServiceAccount;
            return this;
        }

        @Override
        public NodeDiscovery build() {
            return new KubernetesNodeDiscovery(this);
        }
    }

}
