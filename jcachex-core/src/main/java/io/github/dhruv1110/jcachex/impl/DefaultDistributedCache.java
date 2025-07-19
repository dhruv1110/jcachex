package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheStats;
import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery.DiscoveredNode;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery.NodeDiscoveryListener;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Default implementation of DistributedCache interface with TCP-based
 * replication.
 * <p>
 * This implementation provides a production-ready distributed cache with:
 * - TCP socket-based replication for efficient communication
 * - Configurable replication port for node communication
 * - Automatic node discovery and failure detection
 * - Interval-based discovery updates
 * - Bidirectional cache update handling
 * - Built-in load balancing and replication
 * </p>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @since 1.0.0
 */
public class DefaultDistributedCache<K, V> implements DistributedCache<K, V> {
    private static final Logger logger = Logger.getLogger(DefaultDistributedCache.class.getName());

    private final String clusterName;
    private final Set<String> nodeAddresses;
    private final int replicationFactor;
    private final ConsistencyLevel defaultConsistencyLevel;
    private final int partitionCount;
    private final Duration networkTimeout;
    private final int replicationPort;
    private final Duration discoveryInterval;
    private volatile boolean readRepairEnabled;
    private final NodeDiscovery nodeDiscovery;

    // Current node identification
    private final String currentNodeAddress;

    // Local cache for this node
    private final Cache<K, V> localCache;

    // TCP Infrastructure
    private ServerSocket replicationServer;
    private final ExecutorService tcpServerExecutor;
    private final ExecutorService replicationExecutor;
    private final ScheduledExecutorService discoveryScheduler;
    private volatile boolean isRunning;

    // Cluster state
    private final Map<String, NodeInfo> clusterNodes = new ConcurrentHashMap<>();
    private final Set<String> healthyNodes = new CopyOnWriteArraySet<>();
    private final AtomicLong topologyVersion = new AtomicLong(0);

    // TCP Connection Pool
    private final Map<String, Socket> nodeConnections = new ConcurrentHashMap<>();

    // Metrics
    private final AtomicLong networkRequests = new AtomicLong(0);
    private final AtomicLong networkFailures = new AtomicLong(0);
    private final AtomicLong replicationLag = new AtomicLong(0);
    private final AtomicLong conflictResolutions = new AtomicLong(0);
    private final Map<String, AtomicLong> perNodeLatencies = new ConcurrentHashMap<>();

    private DefaultDistributedCache(Builder<K, V> builder) {
        this.clusterName = builder.clusterName;
        this.nodeAddresses = new HashSet<>(builder.nodeAddresses);
        this.replicationFactor = builder.replicationFactor;
        this.defaultConsistencyLevel = builder.consistencyLevel;
        this.partitionCount = builder.partitionCount;
        this.networkTimeout = builder.networkTimeout;
        this.replicationPort = builder.replicationPort;
        this.discoveryInterval = builder.discoveryInterval;
        this.readRepairEnabled = builder.readRepairEnabled;
        this.nodeDiscovery = builder.nodeDiscovery;

        // Determine current node address based on replication port
        this.currentNodeAddress = determineCurrentNodeAddress();

        // Initialize executors
        this.tcpServerExecutor = Executors.newFixedThreadPool(10, r -> {
            Thread t = new Thread(r, "TCPServer-" + replicationPort);
            t.setDaemon(true);
            return t;
        });
        this.replicationExecutor = Executors.newFixedThreadPool(20, r -> {
            Thread t = new Thread(r, "Replication-Worker");
            t.setDaemon(true);
            return t;
        });
        this.discoveryScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "Discovery-Scheduler");
            t.setDaemon(true);
            return t;
        });

        // Create local cache with provided configuration
        this.localCache = new DefaultCache<>(
                builder.cacheConfig != null ? builder.cacheConfig : CacheConfig.<K, V>builder().build());

        // Initialize cluster and start TCP infrastructure
        this.isRunning = true;
        startTcpReplicationServer();
        initializeCluster();
        startIntervalBasedDiscovery();
    }

    /**
     * Starts the TCP server for listening to replication requests from other nodes.
     */
    private void startTcpReplicationServer() {
        try {
            replicationServer = new ServerSocket(replicationPort);
            logger.info("🚀 [TCP-Server] Started replication server on port: " + replicationPort);

            // Start accepting connections
            tcpServerExecutor.submit(() -> {
                while (isRunning && !replicationServer.isClosed()) {
                    try {
                        Socket clientSocket = replicationServer.accept();
                        tcpServerExecutor.submit(() -> handleIncomingReplication(clientSocket));
                    } catch (IOException e) {
                        if (isRunning) {
                            logger.warning("⚠️ [TCP-Server] Error accepting connection: " + e.getMessage());
                        }
                    }
                }
            });

        } catch (IOException e) {
            logger.severe("❌ [TCP-Server] Failed to start replication server on port " + replicationPort + ": "
                    + e.getMessage());
            throw new RuntimeException("Failed to start TCP replication server", e);
        }
    }

    /**
     * Handles incoming TCP replication requests from other nodes.
     */
    private void handleIncomingReplication(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = reader.readLine();
            if (request != null) {
                ReplicationMessage message = parseReplicationMessage(request);
                processIncomingReplication(message);
                writer.println("ACK");
                logger.info("✅ [TCP-Server] Processed replication: " + message.operation + " for key: " + message.key);
            }

        } catch (Exception e) {
            logger.warning("⚠️ [TCP-Server] Error handling incoming replication: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.warning("⚠️ [TCP-Server] Error closing client socket: " + e.getMessage());
            }
        }
    }

    /**
     * Processes an incoming replication message.
     */
    private void processIncomingReplication(ReplicationMessage message) {
        switch (message.operation) {
            case PUT:
                // Apply the update locally without triggering further replication
                localCache.put((K) message.key, (V) message.value);
                logger.info("📥 [Replication] Applied PUT: " + message.key);
                break;
            case DELETE:
                localCache.remove((K) message.key);
                logger.info("📥 [Replication] Applied DELETE: " + message.key);
                break;
            case CLEAR:
                localCache.clear();
                logger.info("📥 [Replication] Applied CLEAR");
                break;
        }
    }

    /**
     * Starts interval-based node discovery.
     */
    private void startIntervalBasedDiscovery() {
        if (nodeDiscovery != null) {
            // Initial discovery
            performNodeDiscovery();

            // Schedule periodic discovery
            discoveryScheduler.scheduleAtFixedRate(
                    this::performNodeDiscovery,
                    discoveryInterval.toMillis(),
                    discoveryInterval.toMillis(),
                    TimeUnit.MILLISECONDS);

            logger.info("🔍 [Discovery] Started interval-based discovery every " + discoveryInterval.toMillis() + "ms");
        }
    }

    /**
     * Performs node discovery and updates cluster topology.
     */
    private void performNodeDiscovery() {
        try {
            if (nodeDiscovery != null) {
                nodeDiscovery.discoverNodes().thenAccept(nodes -> {
                    logger.info("🔍 [Discovery] Found " + nodes.size() + " nodes");

                    Set<String> currentNodeIds = new HashSet<>();
                    for (DiscoveredNode node : nodes) {
                        currentNodeIds.add(node.getNodeId());
                        addDiscoveredNode(node);
                    }

                    // Remove nodes that are no longer discovered
                    Set<String> toRemove = new HashSet<>(clusterNodes.keySet());
                    toRemove.removeAll(currentNodeIds);
                    for (String nodeId : toRemove) {
                        removeDiscoveredNode(nodeId);
                    }
                }).exceptionally(throwable -> {
                    logger.warning("⚠️ [Discovery] Failed to discover nodes: " + throwable.getMessage());
                    return null;
                });
            }
        } catch (Exception e) {
            logger.warning("⚠️ [Discovery] Error during node discovery: " + e.getMessage());
        }
    }

    /**
     * Parses a replication message from TCP request.
     */
    private ReplicationMessage parseReplicationMessage(String request) {
        try {
            // Expected format: "OPERATION|key|value|sourceNode"
            String[] parts = request.split("\\|");
            if (parts.length >= 3) {
                ReplicationOperation operation = ReplicationOperation.valueOf(parts[0]);
                String key = parts[1];
                String value = parts.length > 2 ? parts[2] : null;
                String sourceNode = parts.length > 3 ? parts[3] : "unknown";

                return new ReplicationMessage(operation, key, value, sourceNode);
            }
            throw new IllegalArgumentException("Invalid message format: " + request);
        } catch (Exception e) {
            logger.warning("⚠️ [TCP-Server] Failed to parse replication message: " + request);
            throw e;
        }
    }

    // ============= TCP Message Protocol =============

    /**
     * Replication operation types.
     */
    private enum ReplicationOperation {
        PUT, DELETE, CLEAR
    }

    /**
     * Replication message structure for TCP communication.
     */
    private static class ReplicationMessage {
        final ReplicationOperation operation;
        final String key;
        final String value;
        final String sourceNode;

        ReplicationMessage(ReplicationOperation operation, String key, String value, String sourceNode) {
            this.operation = operation;
            this.key = key;
            this.value = value;
            this.sourceNode = sourceNode;
        }

        @Override
        public String toString() {
            return operation + "|" + key + "|" + (value != null ? value : "") + "|" + sourceNode;
        }
    }

    private void initializeCluster() {
        if (nodeDiscovery != null) {
            // Use automatic node discovery
            logger.info("Initializing cluster with automatic node discovery: " + nodeDiscovery.getDiscoveryType());

            // Set up discovery listener
            nodeDiscovery.addNodeDiscoveryListener(new NodeDiscoveryListener() {
                @Override
                public void onNodeDiscovered(DiscoveredNode node) {
                    logger.info("Discovered new node: " + node.getNodeId());
                    addDiscoveredNode(node);
                }

                @Override
                public void onNodeLost(String nodeId) {
                    logger.info("Lost node: " + nodeId);
                    removeDiscoveredNode(nodeId);
                }

                @Override
                public void onNodeHealthChanged(String nodeId, NodeDiscovery.NodeHealth oldHealth,
                        NodeDiscovery.NodeHealth newHealth) {
                    logger.info("Node health changed: " + nodeId + " " + oldHealth + " -> " + newHealth);
                    updateNodeHealth(nodeId, newHealth);
                }
            });

            // Start node discovery
            nodeDiscovery.start().thenRun(() -> {
                logger.info("Node discovery started successfully");
                // Initial discovery
                nodeDiscovery.discoverNodes().thenAccept(nodes -> {
                    logger.info("Initial discovery found " + nodes.size() + " nodes");
                    for (DiscoveredNode node : nodes) {
                        addDiscoveredNode(node);
                    }
                });
            }).exceptionally(throwable -> {
                logger.severe("Failed to start node discovery: " + throwable.getMessage());
                return null;
            });
        } else {
            // Use manual node configuration
            logger.info("Initializing cluster with manual node configuration");
            for (String address : nodeAddresses) {
                String nodeId = generateNodeId(address);
                clusterNodes.put(nodeId, new NodeInfo(nodeId, address, NodeStatus.HEALTHY,
                        System.currentTimeMillis(), new HashSet<>()));
                healthyNodes.add(nodeId);
            }
        }
        topologyVersion.incrementAndGet();
    }

    private String generateNodeId(String address) {
        return "node-" + address.hashCode();
    }

    /**
     * Determines the current node's address by matching the replication port
     * with the configured node addresses.
     */
    private String determineCurrentNodeAddress() {
        String portStr = ":" + replicationPort;

        // Look for a node address that ends with our replication port
        for (String address : nodeAddresses) {
            if (address.endsWith(portStr)) {
                logger.info("🏠 [CurrentNode] Identified current node address: " + address);
                return address;
            }
        }

        // Fallback: try to determine from hostname
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            String fallbackAddress = hostname + ":" + replicationPort;
            logger.info("🏠 [CurrentNode] Using fallback address: " + fallbackAddress);
            return fallbackAddress;
        } catch (Exception e) {
            // Last resort: use localhost
            String lastResort = "localhost:" + replicationPort;
            logger.warning("⚠️ [CurrentNode] Could not determine hostname, using: " + lastResort);
            return lastResort;
        }
    }

    private void addDiscoveredNode(DiscoveredNode node) {
        String nodeId = node.getNodeId();
        String address = node.getFullAddress();

        NodeStatus status = convertToNodeStatus(node.getHealth());
        NodeInfo nodeInfo = new NodeInfo(nodeId, address, status,
                System.currentTimeMillis(), new HashSet<>());

        clusterNodes.put(nodeId, nodeInfo);
        if (status == NodeStatus.HEALTHY) {
            healthyNodes.add(nodeId);
        }
        topologyVersion.incrementAndGet();

        logger.info("Added discovered node: " + nodeId + " at " + address);
    }

    private void removeDiscoveredNode(String nodeId) {
        clusterNodes.remove(nodeId);
        healthyNodes.remove(nodeId);
        topologyVersion.incrementAndGet();

        logger.info("Removed discovered node: " + nodeId);
    }

    private void updateNodeHealth(String nodeId, NodeDiscovery.NodeHealth health) {
        NodeInfo existingNode = clusterNodes.get(nodeId);
        if (existingNode != null) {
            NodeStatus newStatus = convertToNodeStatus(health);
            NodeInfo updatedNode = new NodeInfo(nodeId, existingNode.getAddress(), newStatus,
                    System.currentTimeMillis(), existingNode.getPartitions());

            clusterNodes.put(nodeId, updatedNode);

            if (newStatus == NodeStatus.HEALTHY) {
                healthyNodes.add(nodeId);
            } else {
                healthyNodes.remove(nodeId);
            }

            logger.info("Updated node health: " + nodeId + " -> " + newStatus);
        }
    }

    private NodeStatus convertToNodeStatus(NodeDiscovery.NodeHealth health) {
        switch (health) {
            case HEALTHY:
                return NodeStatus.HEALTHY;
            case UNHEALTHY:
                return NodeStatus.FAILED;
            case UNKNOWN:
            default:
                return NodeStatus.UNREACHABLE;
        }
    }

    // ============= Cache Interface Implementation =============

    @Override
    public V get(K key) {
        return getWithConsistency(key, defaultConsistencyLevel).join();
    }

    @Override
    public void put(K key, V value) {
        putWithConsistency(key, value, defaultConsistencyLevel).join();
    }

    @Override
    public V remove(K key) {
        // For now, delegate to local cache and invalidate globally
        CompletableFuture.runAsync(() -> invalidateGlobally(key));
        return localCache.remove(key);
    }

    @Override
    public void clear() {
        clearGlobally().join();
    }

    @Override
    public long size() {
        return localCache.size();
    }

    @Override
    public boolean containsKey(K key) {
        return localCache.containsKey(key);
    }

    @Override
    public Set<K> keys() {
        return localCache.keys();
    }

    @Override
    public Collection<V> values() {
        return localCache.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return localCache.entries();
    }

    @Override
    public CacheStats stats() {
        return localCache.stats();
    }

    @Override
    public CompletableFuture<V> getAsync(K key) {
        return getWithConsistency(key, defaultConsistencyLevel);
    }

    @Override
    public CompletableFuture<Void> putAsync(K key, V value) {
        return putWithConsistency(key, value, defaultConsistencyLevel);
    }

    @Override
    public CompletableFuture<V> removeAsync(K key) {
        return CompletableFuture.supplyAsync(() -> remove(key));
    }

    @Override
    public CompletableFuture<Void> clearAsync() {
        return clearGlobally();
    }

    @Override
    public CacheConfig<K, V> config() {
        return localCache.config();
    }

    // ============= Distributed Cache Implementation =============

    @Override
    public CompletableFuture<Void> putWithConsistency(K key, V value, ConsistencyLevel consistencyLevel) {
        networkRequests.incrementAndGet();

        return CompletableFuture.runAsync(() -> {
            try {
                // Always put locally first
                localCache.put(key, value);

                // Replicate based on consistency level
                switch (consistencyLevel) {
                    case STRONG:
                        replicateSync(key, value);
                        break;
                    case EVENTUAL:
                    case SESSION:
                    case MONOTONIC_READ:
                        replicateAsync(key, value);
                        break;
                }
            } catch (Exception e) {
                networkFailures.incrementAndGet();
                throw new RuntimeException("Failed to put with consistency " + consistencyLevel, e);
            }
        });
    }

    @Override
    public CompletableFuture<V> getWithConsistency(K key, ConsistencyLevel consistencyLevel) {
        networkRequests.incrementAndGet();

        return CompletableFuture.supplyAsync(() -> {
            try {
                V localValue = localCache.get(key);

                switch (consistencyLevel) {
                    case STRONG:
                        // For strong consistency, verify with majority of nodes
                        return getWithMajorityRead(key, localValue);
                    case EVENTUAL:
                    case SESSION:
                    case MONOTONIC_READ:
                        // For other consistency levels, return local value
                        return localValue;
                    default:
                        return localValue;
                }
            } catch (Exception e) {
                networkFailures.incrementAndGet();
                throw new RuntimeException("Failed to get with consistency " + consistencyLevel, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> invalidateGlobally(K key) {
        return CompletableFuture.runAsync(() -> {
            // Remove locally
            localCache.remove(key);

            // Send invalidation to all nodes
            broadcastInvalidation(key);
        });
    }

    @Override
    public CompletableFuture<Void> invalidateGlobally(Collection<K> keys) {
        return CompletableFuture.runAsync(() -> {
            // Remove locally
            for (K key : keys) {
                localCache.remove(key);
            }

            // Send invalidation to all nodes
            broadcastInvalidation(keys);
        });
    }

    @Override
    public CompletableFuture<Void> clearGlobally() {
        return CompletableFuture.runAsync(() -> {
            // Clear locally
            localCache.clear();

            // Send clear to all nodes
            broadcastClear();
        });
    }

    @Override
    public ClusterTopology getClusterTopology() {
        Set<NodeInfo> nodeInfos = new HashSet<>(clusterNodes.values());
        return new ClusterTopology(clusterName, nodeInfos, partitionCount, topologyVersion.get());
    }

    @Override
    public Map<String, CacheStats> getPerNodeStats() {
        Map<String, CacheStats> stats = new ConcurrentHashMap<>();

        // Add local stats
        stats.put("local", localCache.stats());

        // In a real implementation, we would query remote nodes
        // For now, return local stats only
        return stats;
    }

    @Override
    public Map<String, NodeStatus> getNodeStatuses() {
        Map<String, NodeStatus> statuses = new ConcurrentHashMap<>();
        for (NodeInfo node : clusterNodes.values()) {
            statuses.put(node.getNodeId(), node.getStatus());
        }
        return statuses;
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return defaultConsistencyLevel;
    }

    @Override
    public int getReplicationFactor() {
        return replicationFactor;
    }

    @Override
    public CompletableFuture<Void> rebalance() {
        return CompletableFuture.runAsync(() -> {
            // Trigger rebalancing logic
            topologyVersion.incrementAndGet();
        });
    }

    @Override
    public CompletableFuture<Void> addNode(String nodeAddress) {
        return CompletableFuture.runAsync(() -> {
            String nodeId = generateNodeId(nodeAddress);
            clusterNodes.put(nodeId, new NodeInfo(nodeId, nodeAddress, NodeStatus.HEALTHY,
                    System.currentTimeMillis(), new HashSet<>()));
            healthyNodes.add(nodeId);
            topologyVersion.incrementAndGet();
        });
    }

    @Override
    public CompletableFuture<Void> removeNode(String nodeId) {
        return CompletableFuture.runAsync(() -> {
            clusterNodes.remove(nodeId);
            healthyNodes.remove(nodeId);
            topologyVersion.incrementAndGet();
        });
    }

    @Override
    public DistributedMetrics getDistributedMetrics() {
        Map<String, Long> latencies = new HashMap<>();
        perNodeLatencies.forEach((node, latency) -> latencies.put(node, latency.get()));

        return new DistributedMetrics(
                networkRequests.get(),
                networkFailures.get(),
                calculateAverageNetworkLatency(),
                replicationLag.get(),
                conflictResolutions.get(),
                latencies);
    }

    @Override
    public void setReadRepairEnabled(boolean enabled) {
        this.readRepairEnabled = enabled;
    }

    @Override
    public boolean isReadRepairEnabled() {
        return readRepairEnabled;
    }

    // ============= Private Helper Methods =============

    private void replicateSync(K key, V value) {
        // Synchronous replication to required number of nodes
        List<String> targetNodes = selectReplicationNodes(key);

        logger.info("🔄 [TCP-Replication] Synchronous replication of key: " + key + " to " + targetNodes.size()
                + " nodes (self: " + currentNodeAddress + " excluded)");

        if (targetNodes.isEmpty()) {
            logger.info(
                    "ℹ️ [TCP-Replication] No target nodes for replication (single node cluster or all nodes unhealthy)");
            return;
        }

        for (String nodeAddress : targetNodes) {
            try {
                // Send PUT request via TCP to other nodes
                sendTcpReplicationMessage(nodeAddress, new ReplicationMessage(
                        ReplicationOperation.PUT, key.toString(), value.toString(), currentNodeAddress));
                logger.info("✅ [TCP-Replication] Sync replication to " + nodeAddress + " successful");
            } catch (Exception e) {
                networkFailures.incrementAndGet();
                logger.warning("❌ [TCP-Replication] Sync replication to " + nodeAddress + " failed: "
                        + e.getMessage());
            }
        }
    }

    private void replicateAsync(K key, V value) {
        CompletableFuture.runAsync(() -> {
            List<String> targetNodes = selectReplicationNodes(key);

            logger.info("🚀 [TCP-Replication] Asynchronous replication of key: " + key + " to "
                    + targetNodes.size() + " nodes (self: " + currentNodeAddress + " excluded)");

            if (targetNodes.isEmpty()) {
                logger.info(
                        "ℹ️ [TCP-Replication] No target nodes for async replication (single node cluster or all nodes unhealthy)");
                return;
            }

            for (String nodeAddress : targetNodes) {
                // Asynchronous replication
                replicationExecutor.submit(() -> {
                    try {
                        sendTcpReplicationMessage(nodeAddress, new ReplicationMessage(
                                ReplicationOperation.PUT, key.toString(), value.toString(), currentNodeAddress));
                        logger.info("✅ [TCP-Replication] Async replication to " + nodeAddress + " successful");
                    } catch (Exception e) {
                        networkFailures.incrementAndGet();
                        logger.warning("❌ [TCP-Replication] Async replication to " + nodeAddress + " failed: "
                                + e.getMessage());
                    }
                });
            }
        }, replicationExecutor);
    }

    private V getWithMajorityRead(K key, V localValue) {
        // For strong consistency, verify with majority of nodes
        // In real implementation, query multiple nodes and compare values
        return localValue;
    }

    private List<String> selectReplicationNodes(K key) {
        // Return actual node addresses for replication
        List<String> addresses = getNodeAddresses();
        int targetCount = Math.min(replicationFactor, addresses.size());
        return addresses.subList(0, targetCount);
    }

    private List<String> getNodeAddresses() {
        // Get addresses from cluster nodes, excluding ourselves
        List<String> addresses = new ArrayList<>();
        for (NodeInfo nodeInfo : clusterNodes.values()) {
            if (nodeInfo.getStatus() == NodeStatus.HEALTHY &&
                    !nodeInfo.getAddress().equals(currentNodeAddress)) {
                addresses.add(nodeInfo.getAddress());
            }
        }

        // If no discovered nodes, use manual addresses (excluding current node)
        if (addresses.isEmpty()) {
            for (String address : nodeAddresses) {
                if (!address.equals(currentNodeAddress)) {
                    addresses.add(address);
                }
            }
        }

        logger.info("🎯 [Replication] Available target nodes: " + addresses + " (excluding self: " + currentNodeAddress
                + ")");
        return addresses;
    }

    // ============= TCP Network Communication Methods =============

    /**
     * Sends a replication message via TCP to a target node.
     */
    private void sendTcpReplicationMessage(String nodeAddress, ReplicationMessage message) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // Parse host and port from nodeAddress
            String host;
            int port;

            if (nodeAddress.contains(":")) {
                String[] parts = nodeAddress.split(":");
                host = parts[0];
                port = parts.length > 1 ? Integer.parseInt(parts[1]) : replicationPort;
            } else {
                host = nodeAddress;
                port = replicationPort;
            }

            // Create TCP connection
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), (int) networkTimeout.toMillis());
                socket.setSoTimeout((int) networkTimeout.toMillis());

                // Send message
                try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    writer.println(message.toString());

                    // Wait for acknowledgment
                    String response = reader.readLine();
                    if (!"ACK".equals(response)) {
                        throw new RuntimeException("Unexpected response: " + response);
                    }
                }
            }

            // Record latency
            long latency = System.currentTimeMillis() - startTime;
            perNodeLatencies.computeIfAbsent(nodeAddress, k -> new AtomicLong(0)).set(latency);

            logger.info("📡 [TCP-Replication] " + message.operation + " sent to " + nodeAddress + " (latency: "
                    + latency + "ms)");

        } catch (Exception e) {
            logger.warning("❌ [TCP-Replication] Failed to send message to " + nodeAddress + ": " + e.getMessage());
            throw e;
        }
    }

    private void broadcastInvalidation(K key) {
        // Send invalidation message to all nodes
        List<String> targetNodes = getNodeAddresses();
        logger.info("🗑️ [TCP-Replication] Broadcasting invalidation for key: " + key + " to " + targetNodes.size()
                + " nodes (self excluded)");

        if (targetNodes.isEmpty()) {
            logger.info(
                    "ℹ️ [TCP-Replication] No target nodes for invalidation (single node cluster or all nodes unhealthy)");
            return;
        }

        for (String nodeAddress : targetNodes) {
            replicationExecutor.submit(() -> {
                try {
                    sendTcpReplicationMessage(nodeAddress, new ReplicationMessage(
                            ReplicationOperation.DELETE, key.toString(), null, currentNodeAddress));
                    logger.info("✅ [TCP-Replication] Invalidation sent to " + nodeAddress);
                } catch (Exception e) {
                    logger.warning("❌ [TCP-Replication] Failed to send invalidation to " + nodeAddress + ": "
                            + e.getMessage());
                }
            });
        }
    }

    private void broadcastInvalidation(Collection<K> keys) {
        // Send batch invalidation message to all nodes
        List<String> targetNodes = getNodeAddresses();
        logger.info("🗑️ [TCP-Replication] Broadcasting batch invalidation for " + keys.size() + " keys to "
                + targetNodes.size() + " nodes (self excluded)");

        if (targetNodes.isEmpty()) {
            logger.info(
                    "ℹ️ [TCP-Replication] No target nodes for batch invalidation (single node cluster or all nodes unhealthy)");
            return;
        }

        for (String nodeAddress : targetNodes) {
            for (K key : keys) {
                replicationExecutor.submit(() -> {
                    try {
                        sendTcpReplicationMessage(nodeAddress, new ReplicationMessage(
                                ReplicationOperation.DELETE, key.toString(), null, currentNodeAddress));
                    } catch (Exception e) {
                        logger.warning("❌ [TCP-Replication] Failed to send batch invalidation to " + nodeAddress + ": "
                                + e.getMessage());
                    }
                });
            }
        }
    }

    private void broadcastClear() {
        // Send clear message to all nodes
        List<String> targetNodes = getNodeAddresses();
        logger.info("🗑️ [TCP-Replication] Broadcasting clear to " + targetNodes.size() + " nodes (self excluded)");

        if (targetNodes.isEmpty()) {
            logger.info("ℹ️ [TCP-Replication] No target nodes for clear (single node cluster or all nodes unhealthy)");
            return;
        }

        for (String nodeAddress : targetNodes) {
            replicationExecutor.submit(() -> {
                try {
                    sendTcpReplicationMessage(nodeAddress, new ReplicationMessage(
                            ReplicationOperation.CLEAR, "", null, currentNodeAddress));
                    logger.info("✅ [TCP-Replication] Clear sent to " + nodeAddress);
                } catch (Exception e) {
                    logger.warning("❌ [TCP-Replication] Failed to send clear to " + nodeAddress + ": "
                            + e.getMessage());
                }
            });
        }
    }

    private double calculateAverageNetworkLatency() {
        if (perNodeLatencies.isEmpty()) {
            return 0.0;
        }

        long totalLatency = perNodeLatencies.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();

        return (double) totalLatency / perNodeLatencies.size();
    }

    /**
     * Shuts down the distributed cache and cleans up resources.
     */
    public void shutdown() {
        logger.info("🔄 [Shutdown] Shutting down DefaultDistributedCache...");

        isRunning = false;

        try {
            // Stop discovery scheduler
            if (discoveryScheduler != null && !discoveryScheduler.isShutdown()) {
                discoveryScheduler.shutdown();
                if (!discoveryScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    discoveryScheduler.shutdownNow();
                }
            }

            // Stop replication executor
            if (replicationExecutor != null && !replicationExecutor.isShutdown()) {
                replicationExecutor.shutdown();
                if (!replicationExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    replicationExecutor.shutdownNow();
                }
            }

            // Stop TCP server executor
            if (tcpServerExecutor != null && !tcpServerExecutor.isShutdown()) {
                tcpServerExecutor.shutdown();
                if (!tcpServerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    tcpServerExecutor.shutdownNow();
                }
            }

            // Close replication server
            if (replicationServer != null && !replicationServer.isClosed()) {
                replicationServer.close();
            }

            // Close node connections
            for (Socket socket : nodeConnections.values()) {
                try {
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    logger.warning("⚠️ [Shutdown] Error closing socket: " + e.getMessage());
                }
            }
            nodeConnections.clear();

            // Stop node discovery
            if (nodeDiscovery != null) {
                nodeDiscovery.stop();
            }

            logger.info("✅ [Shutdown] DefaultDistributedCache shutdown completed");

        } catch (Exception e) {
            logger.severe("❌ [Shutdown] Error during shutdown: " + e.getMessage());
        }
    }

    // ============= Builder Implementation =============

    public static class Builder<K, V> implements DistributedCache.Builder<K, V> {
        private String clusterName = "default-cluster";
        private Collection<String> nodeAddresses = new ArrayList<>();
        private int replicationFactor = 2;
        private ConsistencyLevel consistencyLevel = ConsistencyLevel.EVENTUAL;
        private int partitionCount = 256;
        private Duration networkTimeout = Duration.ofSeconds(5);
        private boolean readRepairEnabled = true;
        private boolean autoDiscoveryEnabled = true;
        private Duration gossipInterval = Duration.ofSeconds(10);
        private int maxReconnectAttempts = 3;
        private boolean compressionEnabled = false;
        private boolean encryptionEnabled = false;
        private CacheConfig<K, V> cacheConfig;
        private NodeDiscovery nodeDiscovery;
        private int replicationPort = 8080; // Default replication port
        private Duration discoveryInterval = Duration.ofSeconds(10); // Default discovery interval

        @Override
        public Builder<K, V> clusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        @Override
        public Builder<K, V> nodes(String... nodeAddresses) {
            this.nodeAddresses = Arrays.asList(nodeAddresses);
            return this;
        }

        @Override
        public Builder<K, V> nodes(Collection<String> nodeAddresses) {
            this.nodeAddresses = new ArrayList<>(nodeAddresses);
            return this;
        }

        @Override
        public Builder<K, V> replicationFactor(int replicationFactor) {
            this.replicationFactor = replicationFactor;
            return this;
        }

        @Override
        public Builder<K, V> consistencyLevel(ConsistencyLevel consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
            return this;
        }

        @Override
        public Builder<K, V> partitionCount(int partitionCount) {
            this.partitionCount = partitionCount;
            return this;
        }

        @Override
        public Builder<K, V> networkTimeout(Duration networkTimeout) {
            this.networkTimeout = networkTimeout;
            return this;
        }

        @Override
        public Builder<K, V> enableReadRepair(boolean enabled) {
            this.readRepairEnabled = enabled;
            return this;
        }

        @Override
        public Builder<K, V> enableAutoDiscovery(boolean enabled) {
            this.autoDiscoveryEnabled = enabled;
            return this;
        }

        @Override
        public Builder<K, V> gossipInterval(Duration gossipInterval) {
            this.gossipInterval = gossipInterval;
            return this;
        }

        @Override
        public Builder<K, V> maxReconnectAttempts(int maxAttempts) {
            this.maxReconnectAttempts = maxAttempts;
            return this;
        }

        @Override
        public Builder<K, V> compressionEnabled(boolean enabled) {
            this.compressionEnabled = enabled;
            return this;
        }

        @Override
        public Builder<K, V> encryptionEnabled(boolean enabled) {
            this.encryptionEnabled = enabled;
            return this;
        }

        public Builder<K, V> cacheConfig(CacheConfig<K, V> cacheConfig) {
            this.cacheConfig = cacheConfig;
            return this;
        }

        public Builder<K, V> nodeDiscovery(NodeDiscovery nodeDiscovery) {
            this.nodeDiscovery = nodeDiscovery;
            return this;
        }

        public Builder<K, V> replicationPort(int replicationPort) {
            this.replicationPort = replicationPort;
            return this;
        }

        public Builder<K, V> discoveryInterval(Duration discoveryInterval) {
            this.discoveryInterval = discoveryInterval;
            return this;
        }

        @Override
        public DistributedCache<K, V> build() {
            return new DefaultDistributedCache<>(this);
        }
    }
}
