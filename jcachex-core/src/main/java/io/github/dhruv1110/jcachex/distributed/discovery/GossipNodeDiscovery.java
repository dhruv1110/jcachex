package io.github.dhruv1110.jcachex.distributed.discovery;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Gossip protocol-based node discovery implementation.
 * <p>
 * This implementation uses a gossip protocol for peer-to-peer node discovery.
 * Nodes periodically exchange information about known nodes with their
 * neighbors,
 * allowing the cluster to maintain a eventually consistent view of all nodes
 * without requiring a central registry.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Decentralized:</strong> No single point of failure</li>
 * <li><strong>Self-Healing:</strong> Automatically adapts to node failures</li>
 * <li><strong>Scalable:</strong> Gossip overhead grows logarithmically</li>
 * <li><strong>Eventual Consistency:</strong> All nodes eventually learn about
 * all other nodes</li>
 * <li><strong>Configurable:</strong> Adjustable gossip interval and fanout</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Basic gossip discovery with seed nodes
 * NodeDiscovery gossipDiscovery = NodeDiscovery.gossip()
 *         .seedNodes("node1:8080", "node2:8080")
 *         .gossipInterval(Duration.ofSeconds(5))
 *         .gossipFanout(3)
 *         .build();
 *
 * // Advanced gossip configuration
 * NodeDiscovery gossipDiscovery = NodeDiscovery.gossip()
 *         .seedNodes("node1:8080", "node2:8080", "node3:8080")
 *         .gossipInterval(Duration.ofSeconds(10))
 *         .gossipFanout(2)
 *         .nodeTimeout(Duration.ofMinutes(2))
 *         .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public class GossipNodeDiscovery implements NodeDiscovery {
    private static final Logger logger = Logger.getLogger(GossipNodeDiscovery.class.getName());

    private static final int DEFAULT_GOSSIP_PORT = 8081;
    private static final int SOCKET_TIMEOUT = 5000; // 5 seconds
    private static final String GOSSIP_MESSAGE_TYPE_HEARTBEAT = "HEARTBEAT";
    private static final String GOSSIP_MESSAGE_TYPE_NODE_LIST = "NODE_LIST";
    private static final String GOSSIP_MESSAGE_TYPE_JOIN = "JOIN";
    private static final String GOSSIP_MESSAGE_TYPE_LEAVE = "LEAVE";

    private final GossipDiscoveryBuilder config;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService gossipExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Set<NodeDiscoveryListener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<String, DiscoveredNode> currentNodes = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Statistics
    private final AtomicLong totalDiscoveries = new AtomicLong(0);
    private final AtomicLong successfulDiscoveries = new AtomicLong(0);
    private final AtomicLong failedDiscoveries = new AtomicLong(0);
    private final AtomicLong totalDiscoveryTime = new AtomicLong(0);

    // Gossip state
    private ServerSocket gossipServerSocket;
    private String localNodeId;
    private String localAddress;
    private int gossipPort;
    private DiscoveredNode localNode;

    public GossipNodeDiscovery(GossipDiscoveryBuilder config) {
        this.config = config;
        this.scheduler = Executors.newScheduledThreadPool(3, r -> {
            Thread t = new Thread(r, "gossip-scheduler");
            t.setDaemon(true);
            return t;
        });
        this.gossipExecutor = Executors.newFixedThreadPool(5, r -> {
            Thread t = new Thread(r, "gossip-executor");
            t.setDaemon(true);
            return t;
        });

        initializeLocalNode();
    }

    private void initializeLocalNode() {
        try {
            // Determine local address
            this.localAddress = InetAddress.getLocalHost().getHostAddress();
            this.gossipPort = findAvailablePort();
            this.localNodeId = generateNodeId(localAddress, gossipPort);

            // Create local node representation
            Map<String, String> metadata = new HashMap<>();
            metadata.put("gossipPort", String.valueOf(gossipPort));
            metadata.put("source", "gossip-local");
            metadata.put("startTime", String.valueOf(System.currentTimeMillis()));

            this.localNode = new DiscoveredNode(
                    localNodeId,
                    localAddress,
                    DEFAULT_GOSSIP_PORT, // Cache port
                    NodeHealth.HEALTHY,
                    Instant.now(),
                    metadata);

            logger.info("Initialized local gossip node: " + localNodeId + " at " + localAddress + ":" + gossipPort);

        } catch (Exception e) {
            logger.severe("Failed to initialize local node: " + e.getMessage());
            throw new RuntimeException("Gossip node initialization failed", e);
        }
    }

    private int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            logger.warning("Failed to find available port, using default: " + DEFAULT_GOSSIP_PORT);
            return DEFAULT_GOSSIP_PORT;
        }
    }

    private String generateNodeId(String address, int port) {
        return "gossip-" + address + "-" + port + "-" + System.currentTimeMillis();
    }

    @Override
    public CompletableFuture<Void> start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting gossip node discovery...");

            try {
                // Start gossip server
                startGossipServer();

                // Join the cluster with seed nodes
                joinCluster();

                // Start periodic gossip
                scheduler.scheduleAtFixedRate(
                        this::performGossip,
                        config.gossipInterval.getSeconds(),
                        config.gossipInterval.getSeconds(),
                        TimeUnit.SECONDS);

                // Start health monitoring
                scheduler.scheduleAtFixedRate(
                        this::performHealthCheck,
                        config.healthCheckInterval.getSeconds(),
                        config.healthCheckInterval.getSeconds(),
                        TimeUnit.SECONDS);

                // Start node timeout checker
                scheduler.scheduleAtFixedRate(
                        this::checkNodeTimeouts,
                        config.nodeTimeout.getSeconds() / 2,
                        config.nodeTimeout.getSeconds() / 2,
                        TimeUnit.SECONDS);

                logger.info("Gossip node discovery started successfully");

            } catch (Exception e) {
                running.set(false);
                logger.severe("Failed to start gossip discovery: " + e.getMessage());
                throw new RuntimeException("Gossip discovery startup failed", e);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    private void startGossipServer() throws IOException {
        gossipServerSocket = new ServerSocket(gossipPort);

        // Start accepting gossip connections
        Thread serverThread = new Thread(() -> {
            while (running.get() && !gossipServerSocket.isClosed()) {
                try {
                    Socket clientSocket = gossipServerSocket.accept();
                    gossipExecutor.submit(() -> handleGossipConnection(clientSocket));
                } catch (IOException e) {
                    if (running.get()) {
                        logger.warning("Error accepting gossip connection: " + e.getMessage());
                    }
                }
            }
        }, "gossip-server");

        serverThread.setDaemon(true);
        serverThread.start();

        logger.info("Gossip server started on port: " + gossipPort);
    }

    private void joinCluster() {
        // Add local node to current nodes
        currentNodes.put(localNodeId, localNode);

        // Contact seed nodes to join the cluster
        for (String seedNodeAddress : config.seedNodes) {
            try {
                String[] parts = seedNodeAddress.split(":");
                String address = parts[0];
                int port = parts.length > 1 ? Integer.parseInt(parts[1]) : DEFAULT_GOSSIP_PORT;

                if (!address.equals(localAddress) || port != gossipPort) {
                    sendJoinMessage(address, port);
                }
            } catch (Exception e) {
                logger.warning("Failed to contact seed node " + seedNodeAddress + ": " + e.getMessage());
            }
        }
    }

    private void sendJoinMessage(String address, int port) {
        GossipMessage joinMessage = new GossipMessage(
                GOSSIP_MESSAGE_TYPE_JOIN,
                localNodeId,
                Collections.singletonList(localNode));

        sendGossipMessage(address, port, joinMessage);
    }

    @Override
    public CompletableFuture<Void> stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping gossip node discovery...");

            try {
                // Send leave messages to known nodes
                sendLeaveMessages();

                // Stop gossip server
                if (gossipServerSocket != null && !gossipServerSocket.isClosed()) {
                    gossipServerSocket.close();
                }

                // Shutdown executors
                scheduler.shutdown();
                gossipExecutor.shutdown();

                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                    if (!gossipExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        gossipExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    gossipExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }

                logger.info("Gossip node discovery stopped");

            } catch (Exception e) {
                logger.severe("Error stopping gossip discovery: " + e.getMessage());
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    private void sendLeaveMessages() {
        GossipMessage leaveMessage = new GossipMessage(
                GOSSIP_MESSAGE_TYPE_LEAVE,
                localNodeId,
                Collections.singletonList(localNode));

        for (DiscoveredNode node : currentNodes.values()) {
            if (!node.getNodeId().equals(localNodeId)) {
                try {
                    int gossipPort = Integer.parseInt(node.getMetadata().getOrDefault("gossipPort", "8081"));
                    sendGossipMessage(node.getAddress(), gossipPort, leaveMessage);
                } catch (Exception e) {
                    logger.warning("Failed to send leave message to " + node.getNodeId() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public CompletableFuture<Set<DiscoveredNode>> discoverNodes() {
        return CompletableFuture.supplyAsync(() -> {
            totalDiscoveries.incrementAndGet();

            try {
                Set<DiscoveredNode> nodes = new HashSet<>(currentNodes.values());
                successfulDiscoveries.incrementAndGet();
                return nodes;
            } catch (Exception e) {
                failedDiscoveries.incrementAndGet();
                throw new RuntimeException("Node discovery failed", e);
            }
        });
    }

    private void performGossip() {
        if (!running.get())
            return;

        try {
            // Select random nodes to gossip with
            List<DiscoveredNode> availableNodes = new ArrayList<>(currentNodes.values());
            availableNodes.removeIf(node -> node.getNodeId().equals(localNodeId));

            if (availableNodes.isEmpty()) {
                logger.fine("No nodes available for gossip");
                return;
            }

            // Randomly select nodes to gossip with (fanout)
            int fanout = Math.min(config.gossipFanout, availableNodes.size());
            Collections.shuffle(availableNodes, random);

            for (int i = 0; i < fanout; i++) {
                DiscoveredNode targetNode = availableNodes.get(i);
                gossipWithNode(targetNode);
            }

        } catch (Exception e) {
            logger.warning("Error during gossip: " + e.getMessage());
        }
    }

    private void gossipWithNode(DiscoveredNode targetNode) {
        try {
            // Create gossip message with known nodes
            List<DiscoveredNode> knownNodes = new ArrayList<>(currentNodes.values());
            GossipMessage message = new GossipMessage(
                    GOSSIP_MESSAGE_TYPE_NODE_LIST,
                    localNodeId,
                    knownNodes);

            int gossipPort = Integer.parseInt(targetNode.getMetadata().getOrDefault("gossipPort", "8081"));
            sendGossipMessage(targetNode.getAddress(), gossipPort, message);

        } catch (Exception e) {
            logger.warning("Failed to gossip with node " + targetNode.getNodeId() + ": " + e.getMessage());
        }
    }

    private void sendGossipMessage(String address, int port, GossipMessage message) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, port), SOCKET_TIMEOUT);
            socket.setSoTimeout(SOCKET_TIMEOUT);

            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(message);
                out.flush();
            }

        } catch (Exception e) {
            logger.fine("Failed to send gossip message to " + address + ":" + port + ": " + e.getMessage());
        }
    }

    private void handleGossipConnection(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            GossipMessage message = (GossipMessage) in.readObject();
            processGossipMessage(message);

        } catch (Exception e) {
            logger.warning("Error handling gossip connection: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.fine("Error closing gossip connection: " + e.getMessage());
            }
        }
    }

    private void processGossipMessage(GossipMessage message) {
        String messageType = message.getMessageType();
        String senderId = message.getSenderId();
        List<DiscoveredNode> nodes = message.getNodes();

        logger.fine("Processing gossip message: " + messageType + " from " + senderId);

        switch (messageType) {
            case GOSSIP_MESSAGE_TYPE_JOIN:
                handleJoinMessage(nodes);
                break;
            case GOSSIP_MESSAGE_TYPE_LEAVE:
                handleLeaveMessage(nodes);
                break;
            case GOSSIP_MESSAGE_TYPE_NODE_LIST:
                handleNodeListMessage(nodes);
                break;
            case GOSSIP_MESSAGE_TYPE_HEARTBEAT:
                handleHeartbeatMessage(nodes);
                break;
            default:
                logger.warning("Unknown gossip message type: " + messageType);
        }
    }

    private void handleJoinMessage(List<DiscoveredNode> nodes) {
        for (DiscoveredNode node : nodes) {
            if (!currentNodes.containsKey(node.getNodeId())) {
                currentNodes.put(node.getNodeId(), node);
                notifyNodeDiscovered(node);
                logger.info("Node joined the cluster: " + node.getNodeId());
            }
        }
    }

    private void handleLeaveMessage(List<DiscoveredNode> nodes) {
        for (DiscoveredNode node : nodes) {
            if (currentNodes.containsKey(node.getNodeId())) {
                currentNodes.remove(node.getNodeId());
                notifyNodeLost(node.getNodeId());
                logger.info("Node left the cluster: " + node.getNodeId());
            }
        }
    }

    private void handleNodeListMessage(List<DiscoveredNode> nodes) {
        for (DiscoveredNode node : nodes) {
            if (!node.getNodeId().equals(localNodeId)) {
                DiscoveredNode existing = currentNodes.get(node.getNodeId());
                if (existing == null) {
                    // New node discovered
                    currentNodes.put(node.getNodeId(), node);
                    notifyNodeDiscovered(node);
                    logger.info("Discovered new node through gossip: " + node.getNodeId());
                } else {
                    // Update existing node with newer information
                    if (node.getLastSeen().isAfter(existing.getLastSeen())) {
                        currentNodes.put(node.getNodeId(), node);
                        if (!existing.getHealth().equals(node.getHealth())) {
                            notifyNodeHealthChanged(node.getNodeId(), existing.getHealth(), node.getHealth());
                        }
                    }
                }
            }
        }
    }

    private void handleHeartbeatMessage(List<DiscoveredNode> nodes) {
        // Update last seen time for nodes
        for (DiscoveredNode node : nodes) {
            if (currentNodes.containsKey(node.getNodeId())) {
                DiscoveredNode updated = new DiscoveredNode(
                        node.getNodeId(),
                        node.getAddress(),
                        node.getPort(),
                        NodeHealth.HEALTHY,
                        Instant.now(),
                        node.getMetadata());
                currentNodes.put(node.getNodeId(), updated);
            }
        }
    }

    private void performHealthCheck() {
        if (!running.get())
            return;

        // Send heartbeat to all known nodes
        for (DiscoveredNode node : currentNodes.values()) {
            if (!node.getNodeId().equals(localNodeId)) {
                try {
                    GossipMessage heartbeat = new GossipMessage(
                            GOSSIP_MESSAGE_TYPE_HEARTBEAT,
                            localNodeId,
                            Collections.singletonList(localNode));

                    int gossipPort = Integer.parseInt(node.getMetadata().getOrDefault("gossipPort", "8081"));
                    sendGossipMessage(node.getAddress(), gossipPort, heartbeat);

                } catch (Exception e) {
                    logger.fine("Failed to send heartbeat to " + node.getNodeId() + ": " + e.getMessage());
                }
            }
        }
    }

    private void checkNodeTimeouts() {
        if (!running.get())
            return;

        Instant now = Instant.now();
        List<String> timedOutNodes = new ArrayList<>();

        for (DiscoveredNode node : currentNodes.values()) {
            if (!node.getNodeId().equals(localNodeId)) {
                Duration timeSinceLastSeen = Duration.between(node.getLastSeen(), now);
                if (timeSinceLastSeen.compareTo(config.nodeTimeout) > 0) {
                    timedOutNodes.add(node.getNodeId());
                }
            }
        }

        for (String nodeId : timedOutNodes) {
            currentNodes.remove(nodeId);
            notifyNodeLost(nodeId);
            logger.info("Node timed out and removed: " + nodeId);
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

            // Check if node has been seen recently
            Duration timeSinceLastSeen = Duration.between(node.getLastSeen(), Instant.now());
            if (timeSinceLastSeen.compareTo(config.nodeTimeout) > 0) {
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
        return DiscoveryType.GOSSIP;
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

    /**
     * Gossip message for node communication.
     */
    private static class GossipMessage implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String messageType;
        private final String senderId;
        private final List<DiscoveredNode> nodes;
        private final long timestamp;

        public GossipMessage(String messageType, String senderId, List<DiscoveredNode> nodes) {
            this.messageType = messageType;
            this.senderId = senderId;
            this.nodes = nodes;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessageType() {
            return messageType;
        }

        public String getSenderId() {
            return senderId;
        }

        public List<DiscoveredNode> getNodes() {
            return nodes;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
