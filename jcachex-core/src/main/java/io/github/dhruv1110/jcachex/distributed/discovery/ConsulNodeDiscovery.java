package io.github.dhruv1110.jcachex.distributed.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Consul-based node discovery implementation.
 * <p>
 * This implementation integrates with HashiCorp Consul for service discovery.
 * It registers the current node as a service and discovers other cache nodes
 * through Consul's service registry.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Service Registration:</strong> Automatically registers with
 * Consul</li>
 * <li><strong>Health Checks:</strong> Integrates with Consul health
 * checking</li>
 * <li><strong>Service Discovery:</strong> Discovers healthy service
 * instances</li>
 * <li><strong>Multi-Datacenter:</strong> Supports multiple Consul
 * datacenters</li>
 * <li><strong>ACL Support:</strong> Works with Consul ACL tokens</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Basic Consul discovery
 * NodeDiscovery consulDiscovery = NodeDiscovery.consul()
 *         .consulHost("localhost:8500")
 *         .serviceName("jcachex-cluster")
 *         .healthCheckInterval(Duration.ofSeconds(10))
 *         .build();
 *
 * // Advanced Consul configuration
 * NodeDiscovery consulDiscovery = NodeDiscovery.consul()
 *         .consulHost("consul.example.com:8500")
 *         .serviceName("jcachex-cluster")
 *         .datacenter("dc1")
 *         .token("your-consul-token")
 *         .enableAcl(true)
 *         .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public class ConsulNodeDiscovery implements NodeDiscovery {
    private static final Logger logger = Logger.getLogger(ConsulNodeDiscovery.class.getName());

    private static final int DEFAULT_CACHE_PORT = 8080;
    private static final int HTTP_OK = 200;
    private static final int HTTP_CREATED = 201;
    private static final String CONSUL_API_V1 = "/v1";
    private static final String HEALTH_CHECK_TTL = "30s";

    private final ConsulDiscoveryBuilder config;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Set<NodeDiscoveryListener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<String, DiscoveredNode> currentNodes = new ConcurrentHashMap<>();

    // Statistics
    private final AtomicLong totalDiscoveries = new AtomicLong(0);
    private final AtomicLong successfulDiscoveries = new AtomicLong(0);
    private final AtomicLong failedDiscoveries = new AtomicLong(0);
    private final AtomicLong totalDiscoveryTime = new AtomicLong(0);

    // Local node info
    private String localNodeId;
    private String localAddress;
    private int localPort;
    private String serviceId;

    public ConsulNodeDiscovery(ConsulDiscoveryBuilder config) {
        this.config = config;
        this.scheduler = Executors.newScheduledThreadPool(3, r -> {
            Thread t = new Thread(r, "consul-discovery-scheduler");
            t.setDaemon(true);
            return t;
        });

        initializeLocalNode();
    }

    private void initializeLocalNode() {
        try {
            this.localAddress = getLocalAddress();
            this.localPort = DEFAULT_CACHE_PORT;
            this.localNodeId = generateNodeId();
            this.serviceId = config.serviceName + "-" + localNodeId;

            logger.info("Initialized local Consul node: " + localNodeId + " at " + localAddress + ":" + localPort);

        } catch (Exception e) {
            logger.severe("Failed to initialize local node: " + e.getMessage());
            throw new RuntimeException("Consul node initialization failed", e);
        }
    }

    private String getLocalAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            logger.warning("Failed to get local address, using localhost: " + e.getMessage());
            return "localhost";
        }
    }

    private String generateNodeId() {
        return "consul-" + System.currentTimeMillis() + "-" + Math.abs(localAddress.hashCode());
    }

    @Override
    public CompletableFuture<Void> start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting Consul node discovery...");

            return CompletableFuture.runAsync(() -> {
                try {
                    // Register this node with Consul
                    registerWithConsul();

                    // Start periodic discovery
                    scheduler.scheduleAtFixedRate(
                            this::performDiscovery,
                            0,
                            config.discoveryInterval.getSeconds(),
                            TimeUnit.SECONDS);

                    // Start health check updates
                    scheduler.scheduleAtFixedRate(
                            this::updateHealthCheck,
                            config.healthCheckInterval.getSeconds(),
                            config.healthCheckInterval.getSeconds(),
                            TimeUnit.SECONDS);

                    logger.info("Consul node discovery started successfully");

                } catch (Exception e) {
                    running.set(false);
                    logger.severe("Failed to start Consul discovery: " + e.getMessage());
                    throw new RuntimeException("Consul discovery startup failed", e);
                }
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    private void registerWithConsul() {
        try {
            String registrationPayload = createServiceRegistrationPayload();
            String url = "http://" + config.consulHost + CONSUL_API_V1 + "/agent/service/register";

            HttpURLConnection connection = createConnection(url, "PUT");
            connection.setDoOutput(true);

            // Write registration payload
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(connection.getOutputStream())) {
                writer.write(registrationPayload);
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                logger.info("Successfully registered with Consul: " + serviceId);
            } else {
                throw new RuntimeException("Failed to register with Consul. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.severe("Failed to register with Consul: " + e.getMessage());
            throw new RuntimeException("Consul registration failed", e);
        }
    }

    private String createServiceRegistrationPayload() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"ID\":\"").append(serviceId).append("\",");
        json.append("\"Name\":\"").append(config.serviceName).append("\",");
        json.append("\"Tags\":[\"jcachex\",\"cache\"],");
        json.append("\"Address\":\"").append(localAddress).append("\",");
        json.append("\"Port\":").append(localPort).append(",");
        json.append("\"Check\":{");
        json.append("\"TTL\":\"").append(HEALTH_CHECK_TTL).append("\",");
        json.append("\"DeregisterCriticalServiceAfter\":\"60s\"");
        json.append("},");
        json.append("\"Meta\":{");
        json.append("\"nodeId\":\"").append(localNodeId).append("\",");
        json.append("\"startTime\":\"").append(System.currentTimeMillis()).append("\"");
        json.append("}");
        json.append("}");

        return json.toString();
    }

    private HttpURLConnection createConnection(String urlString, String method) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout((int) config.connectionTimeout.toMillis());
        connection.setReadTimeout((int) config.connectionTimeout.toMillis());

        if (config.enableAcl && config.token != null) {
            connection.setRequestProperty("X-Consul-Token", config.token);
        }

        return connection;
    }

    @Override
    public CompletableFuture<Void> stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping Consul node discovery...");

            return CompletableFuture.runAsync(() -> {
                try {
                    // Deregister from Consul
                    deregisterFromConsul();

                    // Stop scheduler
                    scheduler.shutdown();
                    try {
                        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                            scheduler.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        scheduler.shutdownNow();
                        Thread.currentThread().interrupt();
                    }

                    logger.info("Consul node discovery stopped");

                } catch (Exception e) {
                    logger.severe("Error stopping Consul discovery: " + e.getMessage());
                }
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    private void deregisterFromConsul() {
        try {
            String url = "http://" + config.consulHost + CONSUL_API_V1 + "/agent/service/deregister/" + serviceId;
            HttpURLConnection connection = createConnection(url, "PUT");

            int responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                logger.info("Successfully deregistered from Consul: " + serviceId);
            } else {
                logger.warning("Failed to deregister from Consul. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.warning("Failed to deregister from Consul: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Set<DiscoveredNode>> discoverNodes() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            totalDiscoveries.incrementAndGet();

            try {
                Set<DiscoveredNode> nodes = performConsulDiscovery();
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

    private Set<DiscoveredNode> performConsulDiscovery() {
        Set<DiscoveredNode> discoveredNodes = new HashSet<>();

        try {
            String url = "http://" + config.consulHost + CONSUL_API_V1 + "/health/service/" + config.serviceName;
            if (config.datacenter != null) {
                url += "?dc=" + config.datacenter;
            }

            HttpURLConnection connection = createConnection(url, "GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HTTP_OK) {
                String response = readResponse(connection);
                discoveredNodes.addAll(parseConsulResponse(response));
                logger.info("Discovered " + discoveredNodes.size() + " nodes from Consul");
            } else {
                logger.warning("Failed to query Consul services. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.severe("Failed to perform Consul discovery: " + e.getMessage());
            throw new RuntimeException("Consul discovery failed", e);
        }

        return discoveredNodes;
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private Set<DiscoveredNode> parseConsulResponse(String jsonResponse) {
        Set<DiscoveredNode> nodes = new HashSet<>();

        try {
            // Parse JSON response - simplified parsing
            // In production, use a proper JSON library like Jackson or Gson
            String[] services = extractJsonArrayItems(jsonResponse);

            for (String service : services) {
                try {
                    DiscoveredNode node = parseServiceToNode(service);
                    if (node != null) {
                        nodes.add(node);
                    }
                } catch (Exception e) {
                    logger.warning("Failed to parse service: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.severe("Failed to parse Consul response: " + e.getMessage());
        }

        return nodes;
    }

    private String[] extractJsonArrayItems(String json) {
        // Very basic JSON array parsing - extracts items between [ and ]
        int start = json.indexOf('[');
        int end = json.lastIndexOf(']');

        if (start != -1 && end != -1 && start < end) {
            String arrayContent = json.substring(start + 1, end);
            if (arrayContent.trim().isEmpty()) {
                return new String[0];
            }

            // Split by },{ to get individual items
            // This is very simplified - in production use proper JSON parsing
            List<String> items = new ArrayList<>();
            int depth = 0;
            StringBuilder current = new StringBuilder();

            for (char c : arrayContent.toCharArray()) {
                if (c == '{')
                    depth++;
                else if (c == '}')
                    depth--;

                current.append(c);

                if (depth == 0 && c == '}') {
                    items.add(current.toString());
                    current = new StringBuilder();
                    // Skip comma and whitespace
                    continue;
                }
            }

            if (current.length() > 0) {
                items.add(current.toString());
            }

            return items.toArray(new String[0]);
        }

        return new String[0];
    }

    private DiscoveredNode parseServiceToNode(String serviceJson) {
        try {
            // Extract service information
            String serviceId = extractJsonValue(serviceJson, "ServiceID");
            String serviceName = extractJsonValue(serviceJson, "ServiceName");
            String address = extractJsonValue(serviceJson, "ServiceAddress");
            String portStr = extractJsonValue(serviceJson, "ServicePort");
            String nodeId = extractJsonValue(serviceJson, "nodeId");

            if (address == null || portStr == null) {
                return null;
            }

            int port = Integer.parseInt(portStr);

            // Check if service is healthy
            NodeHealth health = parseHealthStatus(serviceJson);

            // Create metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("serviceId", serviceId);
            metadata.put("serviceName", serviceName);
            metadata.put("datacenter", config.datacenter);
            metadata.put("source", "consul");

            String finalNodeId = nodeId != null ? nodeId : serviceId;

            return new DiscoveredNode(
                    finalNodeId,
                    address,
                    port,
                    health,
                    Instant.now(),
                    metadata);

        } catch (Exception e) {
            logger.warning("Failed to parse service to node: " + e.getMessage());
            return null;
        }
    }

    private NodeHealth parseHealthStatus(String serviceJson) {
        try {
            // Look for health checks in the service response
            if (serviceJson.contains("\"Status\":\"passing\"")) {
                return NodeHealth.HEALTHY;
            } else if (serviceJson.contains("\"Status\":\"critical\"")) {
                return NodeHealth.UNHEALTHY;
            } else if (serviceJson.contains("\"Status\":\"warning\"")) {
                return NodeHealth.UNHEALTHY;
            }
        } catch (Exception e) {
            logger.fine("Failed to parse health status: " + e.getMessage());
        }

        return NodeHealth.UNKNOWN;
    }

    private String extractJsonValue(String json, String key) {
        // Very basic JSON value extraction
        String pattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
        Pattern p = Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);

        if (m.find()) {
            return m.group(1);
        }

        // Try numeric values
        pattern = "\"" + key + "\":\\s*([0-9]+)";
        p = Pattern.compile(pattern);
        m = p.matcher(json);

        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    private void performDiscovery() {
        if (!running.get())
            return;

        try {
            Set<DiscoveredNode> newNodes = performConsulDiscovery();
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

    private void updateHealthCheck() {
        if (!running.get())
            return;

        try {
            String url = "http://" + config.consulHost + CONSUL_API_V1 + "/agent/check/pass/service:" + serviceId;
            HttpURLConnection connection = createConnection(url, "PUT");

            int responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                logger.fine("Health check updated successfully");
            } else {
                logger.warning("Failed to update health check. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.warning("Failed to update health check: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Void> registerNode(DiscoveredNode node) {
        return CompletableFuture.runAsync(() -> {
            currentNodes.put(node.getNodeId(), node);
            notifyNodeDiscovered(node);
        });
    }

    @Override
    public CompletableFuture<Void> unregisterNode(String nodeId) {
        return CompletableFuture.runAsync(() -> {
            if (currentNodes.containsKey(nodeId)) {
                currentNodes.remove(nodeId);
                notifyNodeLost(nodeId);
            }
        });
    }

    @Override
    public CompletableFuture<NodeHealth> checkNodeHealth(String nodeId) {
        return CompletableFuture.supplyAsync(() -> {
            DiscoveredNode node = currentNodes.get(nodeId);
            if (node == null) {
                return NodeHealth.UNKNOWN;
            }

            // For Consul, we rely on Consul's health checks
            // Could also implement direct health checks here
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
        return DiscoveryType.CONSUL;
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
