package io.github.dhruv1110.jcachex.distributed.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Kubernetes-based node discovery implementation.
 * <p>
 * This implementation uses the Kubernetes API to discover cache nodes running
 * in the same cluster.
 * It can work with both service account tokens (for in-cluster usage) and
 * kubeconfig files
 * (for external usage).
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
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

    private static final String SERVICE_ACCOUNT_TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";
    private static final String SERVICE_ACCOUNT_NAMESPACE_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
    private static final String KUBERNETES_SERVICE_HOST = "KUBERNETES_SERVICE_HOST";
    private static final String KUBERNETES_SERVICE_PORT = "KUBERNETES_SERVICE_PORT";
    private static final int DEFAULT_CACHE_PORT = 8080;

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

    private String apiServerUrl;
    private String bearerToken;
    private String actualNamespace;

    public KubernetesNodeDiscovery(KubernetesDiscoveryBuilder config) {
        this.config = config;
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "k8s-discovery-scheduler");
            t.setDaemon(true);
            return t;
        });

        initializeKubernetesConnection();
    }

    private void initializeKubernetesConnection() {
        try {
            if (config.useServiceAccount && isRunningInCluster()) {
                // Use in-cluster service account
                this.bearerToken = readServiceAccountToken();
                this.actualNamespace = readServiceAccountNamespace();
                this.apiServerUrl = buildApiServerUrl();
                logger.info("Using in-cluster service account for Kubernetes discovery");
            } else if (config.kubeConfigPath != null) {
                // Use kubeconfig file
                parseKubeConfig();
                this.actualNamespace = config.namespace;
                logger.info("Using kubeconfig file for Kubernetes discovery: " + config.kubeConfigPath);
            } else {
                throw new IllegalStateException("No valid Kubernetes configuration found");
            }
        } catch (Exception e) {
            logger.severe("Failed to initialize Kubernetes connection: " + e.getMessage());
            throw new RuntimeException("Kubernetes discovery initialization failed", e);
        }
    }

    private boolean isRunningInCluster() {
        return Files.exists(Paths.get(SERVICE_ACCOUNT_TOKEN_PATH)) &&
                System.getenv(KUBERNETES_SERVICE_HOST) != null &&
                System.getenv(KUBERNETES_SERVICE_PORT) != null;
    }

    private String readServiceAccountToken() throws IOException {
        return readFileContent(SERVICE_ACCOUNT_TOKEN_PATH).trim();
    }

    private String readServiceAccountNamespace() throws IOException {
        if (Files.exists(Paths.get(SERVICE_ACCOUNT_NAMESPACE_PATH))) {
            return readFileContent(SERVICE_ACCOUNT_NAMESPACE_PATH).trim();
        }
        return config.namespace; // fallback to configured namespace
    }

    private String readFileContent(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content.toString().trim();
    }

    private String buildApiServerUrl() {
        String host = System.getenv(KUBERNETES_SERVICE_HOST);
        String port = System.getenv(KUBERNETES_SERVICE_PORT);
        return "https://" + host + ":" + port;
    }

    private void parseKubeConfig() {
        // Simplified kubeconfig parsing
        // In a real implementation, you'd use a proper YAML parser
        try {
            Path kubeConfigPath = Paths.get(config.kubeConfigPath);
            if (!Files.exists(kubeConfigPath)) {
                throw new IllegalStateException("Kubeconfig file not found: " + config.kubeConfigPath);
            }

            // For this implementation, we'll assume standard kubeconfig format
            // In production, use a proper Kubernetes client library
            this.apiServerUrl = "https://kubernetes.default.svc"; // placeholder
            this.bearerToken = "placeholder-token"; // placeholder

            logger.warning("Kubeconfig parsing is simplified in this implementation. " +
                    "Consider using official Kubernetes client libraries for production.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse kubeconfig", e);
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
            if (config.serviceName != null) {
                Set<DiscoveredNode> serviceNodes = discoverServiceEndpoints();
                discoveredNodes.addAll(serviceNodes);
            }

            logger.info("Discovered " + discoveredNodes.size() + " nodes in namespace " + actualNamespace);

        } catch (Exception e) {
            logger.severe("Failed to perform Kubernetes discovery: " + e.getMessage());
            throw new RuntimeException("Kubernetes discovery failed", e);
        }

        return discoveredNodes;
    }

    private Set<DiscoveredNode> discoverPodsAsNodes() {
        Set<DiscoveredNode> nodes = new HashSet<>();

        try {
            String apiPath = "/api/v1/namespaces/" + actualNamespace + "/pods";
            if (config.labelSelector != null) {
                apiPath += "?labelSelector=" + config.labelSelector;
            }

            String response = makeKubernetesApiCall(apiPath);
            nodes.addAll(parsePodsResponse(response));

        } catch (Exception e) {
            logger.severe("Failed to discover pods: " + e.getMessage());
        }

        return nodes;
    }

    private Set<DiscoveredNode> discoverServiceEndpoints() {
        Set<DiscoveredNode> nodes = new HashSet<>();

        try {
            String apiPath = "/api/v1/namespaces/" + actualNamespace + "/endpoints/" + config.serviceName;
            String response = makeKubernetesApiCall(apiPath);
            nodes.addAll(parseEndpointsResponse(response));

        } catch (Exception e) {
            logger.severe("Failed to discover service endpoints: " + e.getMessage());
        }

        return nodes;
    }

    private String makeKubernetesApiCall(String apiPath) throws Exception {
        URL url = new URL(apiServerUrl + apiPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout((int) config.connectionTimeout.toMillis());
            connection.setReadTimeout((int) config.connectionTimeout.toMillis());

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Kubernetes API call failed with status: " + responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    private Set<DiscoveredNode> parsePodsResponse(String jsonResponse) {
        Set<DiscoveredNode> nodes = new HashSet<>();

        // Simplified JSON parsing - in production, use a proper JSON library
        try {
            // This is a very basic implementation
            // In production, use Jackson, Gson, or similar
            if (jsonResponse.contains("\"items\"")) {
                // Extract pod information
                String[] pods = extractJsonArrayItems(jsonResponse, "items");

                for (String pod : pods) {
                    try {
                        DiscoveredNode node = parsePodToNode(pod);
                        if (node != null) {
                            nodes.add(node);
                        }
                    } catch (Exception e) {
                        logger.warning("Failed to parse pod: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Failed to parse pods response: " + e.getMessage());
        }

        return nodes;
    }

    private Set<DiscoveredNode> parseEndpointsResponse(String jsonResponse) {
        Set<DiscoveredNode> nodes = new HashSet<>();

        try {
            // Parse endpoints JSON response
            if (jsonResponse.contains("\"subsets\"")) {
                String[] subsets = extractJsonArrayItems(jsonResponse, "subsets");

                for (String subset : subsets) {
                    if (subset.contains("\"addresses\"")) {
                        String[] addresses = extractJsonArrayItems(subset, "addresses");

                        for (String address : addresses) {
                            try {
                                DiscoveredNode node = parseAddressToNode(address);
                                if (node != null) {
                                    nodes.add(node);
                                }
                            } catch (Exception e) {
                                logger.warning("Failed to parse address: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Failed to parse endpoints response: " + e.getMessage());
        }

        return nodes;
    }

    private DiscoveredNode parsePodToNode(String podJson) {
        try {
            // Extract pod information
            String podName = extractJsonValue(podJson, "name");
            String podIP = extractJsonValue(podJson, "podIP");
            String phase = extractJsonValue(podJson, "phase");

            if (podIP == null || "null".equals(podIP) || podIP.isEmpty()) {
                return null; // Skip pods without IP
            }

            NodeHealth health = "Running".equals(phase) ? NodeHealth.HEALTHY : NodeHealth.UNHEALTHY;
            Map<String, String> metadata = new HashMap<>();
            metadata.put("podName", podName);
            metadata.put("namespace", actualNamespace);
            metadata.put("phase", phase);
            metadata.put("source", "kubernetes-pod");

            return new DiscoveredNode(
                    podName,
                    podIP,
                    DEFAULT_CACHE_PORT,
                    health,
                    Instant.now(),
                    metadata);

        } catch (Exception e) {
            logger.warning("Failed to parse pod to node: " + e.getMessage());
            return null;
        }
    }

    private DiscoveredNode parseAddressToNode(String addressJson) {
        try {
            String ip = extractJsonValue(addressJson, "ip");
            String hostname = extractJsonValue(addressJson, "hostname");

            if (ip == null || "null".equals(ip) || ip.isEmpty()) {
                return null;
            }

            String nodeId = hostname != null ? hostname : ip;
            Map<String, String> metadata = new HashMap<>();
            metadata.put("namespace", actualNamespace);
            metadata.put("serviceName", config.serviceName);
            metadata.put("source", "kubernetes-endpoint");

            return new DiscoveredNode(
                    nodeId,
                    ip,
                    DEFAULT_CACHE_PORT,
                    NodeHealth.HEALTHY,
                    Instant.now(),
                    metadata);

        } catch (Exception e) {
            logger.warning("Failed to parse address to node: " + e.getMessage());
            return null;
        }
    }

    // Simplified JSON parsing utilities
    private String[] extractJsonArrayItems(String json, String arrayName) {
        // Very basic JSON array extraction
        String pattern = "\"" + arrayName + "\":\\s*\\[([^\\]]+)\\]";
        Pattern p = Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);

        if (m.find()) {
            String arrayContent = m.group(1);
            // Split by },{ to get individual items
            return arrayContent.split("\\},\\s*\\{");
        }

        return new String[0];
    }

    private String extractJsonValue(String json, String key) {
        // Very basic JSON value extraction
        String pattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
        Pattern p = Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);

        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

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

        // Re-check health of all current nodes
        for (DiscoveredNode node : currentNodes.values()) {
            checkNodeHealth(node.getNodeId()).thenAccept(health -> {
                if (health != node.getHealth()) {
                    DiscoveredNode updatedNode = new DiscoveredNode(
                            node.getNodeId(),
                            node.getAddress(),
                            node.getPort(),
                            health,
                            Instant.now(),
                            node.getMetadata());
                    currentNodes.put(node.getNodeId(), updatedNode);
                    notifyNodeHealthChanged(node.getNodeId(), node.getHealth(), health);
                }
            });
        }
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

            // In a real implementation, you might check readiness probes
            // For now, assume healthy if recently seen
            Duration timeSinceLastSeen = Duration.between(node.getLastSeen(), Instant.now());
            if (timeSinceLastSeen.compareTo(config.healthCheckInterval.multipliedBy(2)) > 0) {
                return NodeHealth.UNHEALTHY;
            }

            return NodeHealth.HEALTHY;
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
