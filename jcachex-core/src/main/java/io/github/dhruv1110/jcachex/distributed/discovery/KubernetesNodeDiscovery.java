package io.github.dhruv1110.jcachex.distributed.discovery;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Kubernetes-based node discovery implementation using the official Kubernetes
 * Java client.
 * <p>
 * This implementation uses the official Kubernetes Java client library to
 * discover cache nodes running
 * in the same cluster. It supports both in-cluster service account
 * authentication and external kubeconfig.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Official Client:</strong> Uses the official Kubernetes Java
 * client library</li>
 * <li><strong>Service Account Support:</strong> Automatically uses in-cluster
 * service account</li>
 * <li><strong>Kubeconfig Support:</strong> Can use external kubeconfig for
 * development</li>
 * <li><strong>Label Selectors:</strong> Filter pods/services by labels</li>
 * <li><strong>Namespace Aware:</strong> Discover nodes in specific
 * namespaces</li>
 * <li><strong>Health Monitoring:</strong> Integrates with Kubernetes readiness
 * probes</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // In-cluster discovery using service account
 * NodeDiscovery k8sDiscovery = NodeDiscovery.kubernetes()
 *         .namespace("default")
 *         .serviceName("jcachex-cluster")
 *         .labelSelector("app=jcachex,version=1.0")
 *         .build();
 *
 * // External discovery using kubeconfig
 * NodeDiscovery k8sDiscovery = NodeDiscovery.kubernetes()
 *         .kubeConfigPath("/path/to/kubeconfig")
 *         .namespace("jcachex")
 *         .serviceName("cache-service")
 *         .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public class KubernetesNodeDiscovery implements NodeDiscovery {
    private static final Logger logger = Logger.getLogger(KubernetesNodeDiscovery.class.getName());
    private static final int DEFAULT_CACHE_PORT = 8081;

    private final KubernetesDiscoveryBuilder config;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Set<NodeDiscoveryListener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<String, DiscoveredNode> currentNodes = new ConcurrentHashMap<>();

    // Statistics
    private final AtomicLong totalDiscoveries = new AtomicLong(0);
    private final AtomicLong successfulDiscoveries = new AtomicLong(0);
    private final AtomicLong failedDiscoveries = new AtomicLong(0);
    private final AtomicLong totalDiscoveryTime = new AtomicLong(0);

    // Kubernetes Java client components
    private ApiClient apiClient;
    private CoreV1Api coreV1Api;
    private String actualNamespace;

    public KubernetesNodeDiscovery(KubernetesDiscoveryBuilder config) {
        this.config = config;
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "k8s-discovery-scheduler");
            t.setDaemon(true);
            return t;
        });

        initializeKubernetesClient();
    }

    private void initializeKubernetesClient() {
        try {
            this.apiClient = Config.defaultClient();

            // Set the global configuration
            Configuration.setDefaultApiClient(apiClient);

            // Initialize the Core V1 API
            this.coreV1Api = new CoreV1Api(apiClient);

            // Set the actual namespace
            this.actualNamespace = config.namespace != null ? config.namespace : "default";

            logger.info("Kubernetes client initialized successfully for namespace: " + actualNamespace);

        } catch (IOException e) {
            logger.severe("Failed to initialize Kubernetes client: " + e.getMessage());
            throw new RuntimeException("Kubernetes discovery initialization failed", e);
        }
    }

    @Override
    public CompletableFuture<Void> start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting Kubernetes node discovery...");

            // Start periodic discovery
            scheduler.scheduleAtFixedRate(
                    this::performDiscovery,
                    0,
                    config.discoveryInterval.getSeconds(),
                    TimeUnit.SECONDS);

            // Start health monitoring
            scheduler.scheduleAtFixedRate(
                    this::performHealthCheck,
                    config.healthCheckInterval.getSeconds(),
                    config.healthCheckInterval.getSeconds(),
                    TimeUnit.SECONDS);

            logger.info("Kubernetes node discovery started successfully");
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping Kubernetes node discovery...");

            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }

            logger.info("Kubernetes node discovery stopped");
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Set<DiscoveredNode>> discoverNodes() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            totalDiscoveries.incrementAndGet();

            try {
                Set<DiscoveredNode> nodes = performKubernetesDiscovery();
                successfulDiscoveries.incrementAndGet();
                totalDiscoveryTime.addAndGet(System.currentTimeMillis() - startTime);
                return nodes;
            } catch (Exception e) {
                failedDiscoveries.incrementAndGet();
                logger.severe("Failed to discover nodes: " + e.getMessage());
                throw new RuntimeException("Node discovery failed", e);
            }
        });
    }

    private Set<DiscoveredNode> performKubernetesDiscovery() {
        Set<DiscoveredNode> discoveredNodes = new HashSet<>();

        try {
            // Discover pods with matching labels
            Set<DiscoveredNode> podNodes = discoverPodsAsNodes();
            discoveredNodes.addAll(podNodes);

            // Optionally discover services
            // if (config.serviceName != null) {
            // Set<DiscoveredNode> serviceNodes = discoverServiceEndpoints();
            // if (serviceNodes.isEmpty()) {
            // logger.info("No service endpoints found for service " + config.serviceName +
            // " in namespace " + actualNamespace);
            // } else {
            // logger.info("Discovered " + serviceNodes.size() + " service endpoints for
            // service " + config.serviceName + " in namespace " + actualNamespace);
            // logger.info("Service nodes: " + serviceNodes);
            // }
            // discoveredNodes.addAll(serviceNodes);
            // }

            // logger.info("Discovered " + discoveredNodes.size() + " nodes in namespace " +
            // actualNamespace);
            // logger.info("Discovered nodes: " + discoveredNodes);

        } catch (Exception e) {
            logger.severe("Failed to perform Kubernetes discovery: " + e.getMessage());
            throw new RuntimeException("Kubernetes discovery failed", e);
        }

        return discoveredNodes;
    }

    private Set<DiscoveredNode> discoverPodsAsNodes() {
        Set<DiscoveredNode> nodes = new HashSet<>();

        try {
            V1PodList podList;

            if (config.labelSelector != null) {
                // List pods with label selector
                podList = coreV1Api.listNamespacedPod(
                        actualNamespace,
                        null, // pretty
                        null, // allowWatchBookmarks
                        null, // _continue
                        null, // fieldSelector
                        config.labelSelector, // labelSelector
                        null, // limit
                        null, // resourceVersion
                        null, // resourceVersionMatch
                        null, // timeoutSeconds
                        null // watch
                );
            } else {
                // List all pods in namespace
                podList = coreV1Api.listNamespacedPod(
                        actualNamespace,
                        null, // pretty
                        null, // allowWatchBookmarks
                        null, // _continue
                        null, // fieldSelector
                        null, // labelSelector
                        null, // limit
                        null, // resourceVersion
                        null, // resourceVersionMatch
                        null, // timeoutSeconds
                        null // watch
                );
            }

            if (podList.getItems() != null) {
                for (V1Pod pod : podList.getItems()) {
                    DiscoveredNode node = createNodeFromPod(pod);
                    if (node != null) {
                        nodes.add(node);
                    }
                }
            }

        } catch (ApiException e) {
            logger.severe("Failed to discover pods: " + e.getResponseBody());
        }

        return nodes;
    }

    // private Set<DiscoveredNode> discoverServiceEndpoints() {
    // Set<DiscoveredNode> nodes = new HashSet<>();

    // try {
    // V1Endpoints endpoints = coreV1Api.readNamespacedEndpoints(
    // config.serviceName,
    // actualNamespace,
    // null // pretty
    // );

    // if (endpoints.getSubsets() != null) {
    // for (V1EndpointSubset subset : endpoints.getSubsets()) {
    // if (subset.getAddresses() != null) {
    // for (V1EndpointAddress address : subset.getAddresses()) {
    // DiscoveredNode node = createNodeFromEndpointAddress(address);
    // if (node != null) {
    // nodes.add(node);
    // }
    // }
    // }
    // }
    // }

    // } catch (ApiException e) {
    // logger.warning(
    // "Failed to discover service endpoints for " + config.serviceName + ": " +
    // e.getResponseBody());
    // }

    // return nodes;
    // }

    private DiscoveredNode createNodeFromPod(V1Pod pod) {
        try {
            V1ObjectMeta metadata = pod.getMetadata();
            V1PodStatus status = pod.getStatus();

            if (metadata == null || status == null) {
                return null;
            }

            String podName = metadata.getName();
            String podIP = status.getPodIP();
            String phase = status.getPhase();

            if (podIP == null || podIP.isEmpty()) {
                return null; // Skip pods without IP
            }

            // Determine health based on pod phase and conditions
            NodeHealth health = determinePodHealth(pod);

            Map<String, String> nodeMetadata = new HashMap<>();
            nodeMetadata.put("podName", podName);
            nodeMetadata.put("namespace", actualNamespace);
            nodeMetadata.put("phase", phase);
            nodeMetadata.put("source", "kubernetes-pod");

            if (metadata.getLabels() != null) {
                for (Map.Entry<String, String> entry : metadata.getLabels().entrySet()) {
                    nodeMetadata.put("label." + entry.getKey(), entry.getValue());
                }
            }

            return new DiscoveredNode(
                    podIP, // Use IP as node ID for reliable TCP connections
                    podIP,
                    DEFAULT_CACHE_PORT,
                    health,
                    Instant.now(),
                    nodeMetadata);

        } catch (Exception e) {
            logger.warning("Failed to create node from pod: " + e.getMessage());
            return null;
        }
    }

    private NodeHealth determinePodHealth(V1Pod pod) {
        V1PodStatus status = pod.getStatus();
        if (status == null) {
            return NodeHealth.UNKNOWN;
        }

        String phase = status.getPhase();
        if (!"Running".equals(phase)) {
            return NodeHealth.UNHEALTHY;
        }

        // Check readiness conditions
        if (status.getConditions() != null) {
            for (V1PodCondition condition : status.getConditions()) {
                if ("Ready".equals(condition.getType())) {
                    return "True".equals(condition.getStatus()) ? NodeHealth.HEALTHY : NodeHealth.UNHEALTHY;
                }
            }
        }

        return NodeHealth.HEALTHY;
    }

    // private DiscoveredNode createNodeFromEndpointAddress(V1EndpointAddress
    // address) {
    // try {
    // String ip = address.getIp();
    // String hostname = address.getHostname();

    // if (ip == null || ip.isEmpty()) {
    // return null;
    // }

    // String nodeId = hostname != null ? hostname : ip;
    // Map<String, String> nodeMetadata = new HashMap<>();
    // nodeMetadata.put("namespace", actualNamespace);
    // // nodeMetadata.put("serviceName", config.serviceName);
    // nodeMetadata.put("source", "kubernetes-endpoint");

    // return new DiscoveredNode(
    // nodeId,
    // ip,
    // DEFAULT_CACHE_PORT,
    // NodeHealth.HEALTHY,
    // Instant.now(),
    // nodeMetadata);

    // } catch (Exception e) {
    // logger.warning("Failed to create node from endpoint address: " +
    // e.getMessage());
    // return null;
    // }
    // }

    private void performDiscovery() {
        if (!running.get())
            return;

        try {
            Set<DiscoveredNode> newNodes = performKubernetesDiscovery();
            updateCurrentNodes(newNodes);
        } catch (Exception e) {
            logger.severe("Periodic discovery failed: " + e.getMessage());
        }
    }

    private void updateCurrentNodes(Set<DiscoveredNode> newNodes) {
        Set<String> newNodeIds = new HashSet<>();

        // Process new/updated nodes
        for (DiscoveredNode node : newNodes) {
            newNodeIds.add(node.getNodeId());

            DiscoveredNode existing = currentNodes.get(node.getNodeId());
            if (existing == null) {
                // New node discovered
                currentNodes.put(node.getNodeId(), node);
                notifyNodeDiscovered(node);
            } else if (!existing.getHealth().equals(node.getHealth())) {
                // Health status changed
                currentNodes.put(node.getNodeId(), node);
                notifyNodeHealthChanged(node.getNodeId(), existing.getHealth(), node.getHealth());
            }
        }

        // Process removed nodes
        Set<String> removedNodes = new HashSet<>(currentNodes.keySet());
        removedNodes.removeAll(newNodeIds);

        for (String nodeId : removedNodes) {
            currentNodes.remove(nodeId);
            notifyNodeLost(nodeId);
        }
    }

    private void performHealthCheck() {
        if (!running.get())
            return;

        // Re-check health of all current nodes by performing a fresh discovery
        CompletableFuture.runAsync(() -> {
            try {
                Set<DiscoveredNode> refreshedNodes = performKubernetesDiscovery();
                updateCurrentNodes(refreshedNodes);
            } catch (Exception e) {
                logger.warning("Health check discovery failed: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> registerNode(DiscoveredNode node) {
        // In Kubernetes, nodes are registered via pod/service creation
        // This is typically handled by the deployment system
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> unregisterNode(String nodeId) {
        // In Kubernetes, nodes are unregistered via pod/service deletion
        // This is typically handled by the deployment system
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<NodeHealth> checkNodeHealth(String nodeId) {
        return CompletableFuture.supplyAsync(() -> {
            DiscoveredNode node = currentNodes.get(nodeId);
            if (node == null) {
                return NodeHealth.UNKNOWN;
            }

            // Check if node was recently seen
            Duration timeSinceLastSeen = Duration.between(node.getLastSeen(), Instant.now());
            if (timeSinceLastSeen.compareTo(config.healthCheckInterval.multipliedBy(2)) > 0) {
                return NodeHealth.UNHEALTHY;
            }

            return node.getHealth();
        });
    }

    @Override
    public void addNodeDiscoveryListener(NodeDiscoveryListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeNodeDiscoveryListener(NodeDiscoveryListener listener) {
        listeners.remove(listener);
    }

    private void notifyNodeDiscovered(DiscoveredNode node) {
        for (NodeDiscoveryListener listener : listeners) {
            try {
                listener.onNodeDiscovered(node);
            } catch (Exception e) {
                logger.warning("Listener notification failed: " + e.getMessage());
            }
        }
    }

    private void notifyNodeLost(String nodeId) {
        for (NodeDiscoveryListener listener : listeners) {
            try {
                listener.onNodeLost(nodeId);
            } catch (Exception e) {
                logger.warning("Listener notification failed: " + e.getMessage());
            }
        }
    }

    private void notifyNodeHealthChanged(String nodeId, NodeHealth oldHealth, NodeHealth newHealth) {
        for (NodeDiscoveryListener listener : listeners) {
            try {
                listener.onNodeHealthChanged(nodeId, oldHealth, newHealth);
            } catch (Exception e) {
                logger.warning("Listener notification failed: " + e.getMessage());
            }
        }
    }

    @Override
    public DiscoveryType getDiscoveryType() {
        return DiscoveryType.KUBERNETES;
    }

    @Override
    public DiscoveryStats getDiscoveryStats() {
        long totalTime = totalDiscoveryTime.get();
        long totalDisc = totalDiscoveries.get();
        long averageTime = totalDisc > 0 ? totalTime / totalDisc : 0;

        return new DiscoveryStats(
                totalDisc,
                successfulDiscoveries.get(),
                failedDiscoveries.get(),
                averageTime,
                currentNodes.size(),
                currentNodes.size());
    }
}
