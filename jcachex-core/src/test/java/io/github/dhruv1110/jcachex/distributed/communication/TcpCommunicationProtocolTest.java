package io.github.dhruv1110.jcachex.distributed.communication;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.impl.DefaultCache;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for TcpCommunicationProtocol.
 * Tests TCP-specific functionality including networking, concurrency, and error
 * handling.
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TcpCommunicationProtocolTest {

    @Mock
    private Cache<String, String> mockCache;

    private TcpCommunicationProtocol<String, String> tcpProtocol;
    private TcpCommunicationProtocol<String, String> serverProtocol;
    private TcpCommunicationProtocol<String, String> clientProtocol;

    private int testPort;
    private int serverPort;
    private int clientPort;

    @BeforeEach
    void setUp() throws IOException {
        // Find available ports for testing
        testPort = findAvailablePort();
        serverPort = findAvailablePort();
        clientPort = findAvailablePort();

        // Create basic TCP protocol for general tests
        tcpProtocol = TcpCommunicationProtocol.<String, String>builder()
                .port(testPort)
                .timeout(2000)
                .maxConnections(50)
                .bufferSize(4096)
                .build();
        tcpProtocol.setLocalCache(mockCache);

        // Create server and client protocols for integration tests
        serverProtocol = TcpCommunicationProtocol.<String, String>builder()
                .port(serverPort)
                .timeout(1000)
                .maxConnections(10)
                .build();

        clientProtocol = TcpCommunicationProtocol.<String, String>builder()
                .port(clientPort)
                .timeout(1000)
                .maxConnections(10)
                .build();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        // Clean up all protocols
        if (tcpProtocol != null && tcpProtocol.isRunning()) {
            tcpProtocol.stopServer().join();
        }
        if (serverProtocol != null && serverProtocol.isRunning()) {
            serverProtocol.stopServer().join();
        }
        if (clientProtocol != null && clientProtocol.isRunning()) {
            clientProtocol.stopServer().join();
        }

        // Give time for cleanup
        Thread.sleep(100);
    }

    private int findAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    // ============= Configuration and Builder Tests =============

    @Test
    @Order(1)
    void testBuilderWithDefaultValues() {
        TcpCommunicationProtocol<String, String> protocol = TcpCommunicationProtocol.<String, String>builder()
                .build();

        assertNotNull(protocol);
        assertEquals(CommunicationProtocol.ProtocolType.TCP, protocol.getProtocolType());
        CommunicationProtocol.ProtocolConfig config = protocol.getConfig();
        assertEquals(8081, config.getPort()); // Default port
        assertEquals(5000, config.getTimeoutMs()); // Default timeout
        assertEquals(100, config.getMaxConnections()); // Default max connections
        assertEquals(8192, config.getBufferSize()); // Default buffer size
    }

    @Test
    @Order(2)
    void testBuilderWithCustomValues() {
        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("custom", "value");

        TcpCommunicationProtocol<String, String> protocol = TcpCommunicationProtocol.<String, String>builder()
                .port(9090)
                .timeout(3000)
                .maxConnections(200)
                .bufferSize(16384)
                .additionalProperties(additionalProps)
                .build();

        CommunicationProtocol.ProtocolConfig config = protocol.getConfig();
        assertEquals(9090, config.getPort());
        assertEquals(3000, config.getTimeoutMs());
        assertEquals(200, config.getMaxConnections());
        assertEquals(16384, config.getBufferSize());
        assertEquals("value", config.getAdditionalProperties().get("custom"));
    }

    @Test
    @Order(3)
    void testProtocolType() {
        assertEquals(CommunicationProtocol.ProtocolType.TCP, tcpProtocol.getProtocolType());
    }

    @Test
    @Order(4)
    void testInitialState() {
        assertFalse(tcpProtocol.isRunning());
        assertNotNull(tcpProtocol.getConfig());
        assertNotNull(tcpProtocol.getMetrics());
    }

    // ============= Lifecycle Tests =============

    @Test
    @Order(5)
    void testServerStartAndStop() throws Exception {
        assertFalse(tcpProtocol.isRunning());

        // Start server
        CompletableFuture<Void> startFuture = tcpProtocol.startServer();
        assertNotNull(startFuture);

        // Wait for server to start
        Thread.sleep(100);
        assertTrue(tcpProtocol.isRunning());

        // Stop server
        CompletableFuture<Void> stopFuture = tcpProtocol.stopServer();
        assertNotNull(stopFuture);
        stopFuture.get(2, TimeUnit.SECONDS);

        assertFalse(tcpProtocol.isRunning());
    }

    @Test
    @Order(6)
    void testMultipleStartStopCycles() throws Exception {
        for (int i = 0; i < 3; i++) {
            // Start
            tcpProtocol.startServer();
            Thread.sleep(50);
            assertTrue(tcpProtocol.isRunning());

            // Stop
            tcpProtocol.stopServer().get(1, TimeUnit.SECONDS);
            assertFalse(tcpProtocol.isRunning());
        }
    }

    // ============= Communication Tests =============

    @Test
    @Order(7)
    void testSendPutWithMockCache() throws Exception {
        // Setup cache mock
        doNothing().when(mockCache).put(anyString(), anyString());

        // Start server
        serverProtocol.setLocalCache(mockCache);
        serverProtocol.startServer();
        Thread.sleep(100);

        // Send PUT request
        String nodeAddress = "localhost:" + serverPort;
        CompletableFuture<CommunicationProtocol.CommunicationResult<Void>> future = clientProtocol.sendPut(nodeAddress,
                "key1", "value1");

        CommunicationProtocol.CommunicationResult<Void> result = future.get(2, TimeUnit.SECONDS);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getLatency());
        assertTrue(result.getLatency().toMillis() >= 0);
    }

    @Test
    @Order(8)
    void testSendGetWithMockCache() throws Exception {
        // Setup cache mock
        when(mockCache.get("key1")).thenReturn("value1");

        // Start server
        serverProtocol.setLocalCache(mockCache);
        serverProtocol.startServer();
        Thread.sleep(100);

        // Send GET request
        String nodeAddress = "localhost:" + serverPort;
        CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = clientProtocol
                .sendGet(nodeAddress, "key1");

        CommunicationProtocol.CommunicationResult<String> result = future.get(2, TimeUnit.SECONDS);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("value1", result.getResult());
        assertNotNull(result.getLatency());
    }

    @Test
    @Order(9)
    void testSendRemoveWithMockCache() throws Exception {
        // Setup cache mock
        when(mockCache.remove("key1")).thenReturn("value1");

        // Start server
        serverProtocol.setLocalCache(mockCache);
        serverProtocol.startServer();
        Thread.sleep(100);

        // Send REMOVE request
        String nodeAddress = "localhost:" + serverPort;
        CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = clientProtocol
                .sendRemove(nodeAddress, "key1");

        CommunicationProtocol.CommunicationResult<String> result = future.get(2, TimeUnit.SECONDS);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("value1", result.getResult());
    }

    @Test
    @Order(10)
    void testSendHealthCheck() throws Exception {
        // Start server
        serverProtocol.setLocalCache(mockCache);
        serverProtocol.startServer();
        Thread.sleep(100);

        // Send health check
        String nodeAddress = "localhost:" + serverPort;
        CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = clientProtocol
                .sendHealthCheck(nodeAddress);

        CommunicationProtocol.CommunicationResult<String> result = future.get(2, TimeUnit.SECONDS);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("OK", result.getResult());
    }

    // ============= Error Handling Tests =============

    @Test
    @Order(11)
    void testConnectionFailure() throws Exception {
        // Try to connect to non-existent server
        String nodeAddress = "localhost:99999";
        CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = clientProtocol
                .sendGet(nodeAddress, "key1");

        CommunicationProtocol.CommunicationResult<String> result = future.get(3, TimeUnit.SECONDS);
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Connection failed"));
    }

    @Test
    @Order(12)
    void testTimeoutHandling() throws Exception {
        // Create protocol with very short timeout
        TcpCommunicationProtocol<String, String> timeoutProtocol = TcpCommunicationProtocol.<String, String>builder()
                .port(findAvailablePort())
                .timeout(100) // Very short timeout
                .build();

        try {
            String nodeAddress = "192.0.2.1:12345"; // Non-routable address
            CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = timeoutProtocol
                    .sendGet(nodeAddress, "key1");

            CommunicationProtocol.CommunicationResult<String> result = future.get(3, TimeUnit.SECONDS);
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertNotNull(result.getErrorMessage());
        } finally {
            if (timeoutProtocol.isRunning()) {
                timeoutProtocol.stopServer().join();
            }
        }
    }

    @Test
    @Order(13)
    void testInvalidNodeAddress() throws Exception {
        // Test with invalid address format
        String invalidAddress = "invalid-address-format";
        CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = clientProtocol
                .sendGet(invalidAddress, "key1");

        CommunicationProtocol.CommunicationResult<String> result = future.get(2, TimeUnit.SECONDS);
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    // ============= Concurrent Operations Tests =============

    @Test
    @Order(14)
    void testConcurrentPutOperations() throws Exception {
        // Setup
        serverProtocol.setLocalCache(mockCache);
        serverProtocol.startServer();
        Thread.sleep(100);

        String nodeAddress = "localhost:" + serverPort;
        int numOperations = 10;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<CompletableFuture<CommunicationProtocol.CommunicationResult<Void>>> futures = new ArrayList<>();

        try {
            // Submit concurrent operations
            for (int i = 0; i < numOperations; i++) {
                final int index = i;
                CompletableFuture<CommunicationProtocol.CommunicationResult<Void>> future = CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return clientProtocol.sendPut(nodeAddress, "key" + index, "value" + index)
                                        .get(2, TimeUnit.SECONDS);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }, executor);
                futures.add(future);
            }

            // Wait for all operations to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(5, TimeUnit.SECONDS);

            // Verify all operations succeeded
            for (CompletableFuture<CommunicationProtocol.CommunicationResult<Void>> future : futures) {
                CommunicationProtocol.CommunicationResult<Void> result = future.get();
                assertTrue(result.isSuccess(), "Operation should succeed");
            }

        } finally {
            executor.shutdown();
        }
    }

    @Test
    @Order(15)
    void testConcurrentServerConnections() throws Exception {
        // Setup cache with real implementation for this test
        Cache<String, String> realCache = new DefaultCache<>(CacheConfig.<String, String>builder().build());
        serverProtocol.setLocalCache(realCache);
        serverProtocol.startServer();
        Thread.sleep(100);

        String nodeAddress = "localhost:" + serverPort;
        int numClients = 5;
        int operationsPerClient = 3;
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        AtomicInteger successCount = new AtomicInteger(0);

        try {
            List<CompletableFuture<Void>> clientFutures = new ArrayList<>();

            for (int clientId = 0; clientId < numClients; clientId++) {
                final int id = clientId;
                CompletableFuture<Void> clientFuture = CompletableFuture.runAsync(() -> {
                    try {
                        for (int op = 0; op < operationsPerClient; op++) {
                            String key = "client" + id + "_key" + op;
                            String value = "client" + id + "_value" + op;

                            // PUT operation
                            CommunicationProtocol.CommunicationResult<Void> putResult = clientProtocol
                                    .sendPut(nodeAddress, key, value)
                                    .get(2, TimeUnit.SECONDS);
                            if (putResult.isSuccess()) {
                                successCount.incrementAndGet();
                            }

                            // GET operation
                            CommunicationProtocol.CommunicationResult<String> getResult = clientProtocol
                                    .sendGet(nodeAddress, key)
                                    .get(2, TimeUnit.SECONDS);
                            if (getResult.isSuccess() && value.equals(getResult.getResult())) {
                                successCount.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, executor);

                clientFutures.add(clientFuture);
            }

            // Wait for all clients to complete
            CompletableFuture.allOf(clientFutures.toArray(new CompletableFuture[0]))
                    .get(10, TimeUnit.SECONDS);

            // Verify most operations succeeded (allow for some failures due to timing)
            int expectedOperations = numClients * operationsPerClient * 2; // PUT + GET
            assertTrue(successCount.get() >= expectedOperations * 0.8,
                    "At least 80% of operations should succeed. Got: " + successCount.get() + "/" + expectedOperations);

        } finally {
            executor.shutdown();
        }
    }

    // ============= Broadcast Operations Tests =============

    @Test
    @Order(16)
    void testBroadcastInvalidation() throws Exception {
        // Start multiple servers
        List<TcpCommunicationProtocol<String, String>> servers = new ArrayList<>();
        List<String> nodeAddresses = new ArrayList<>();

        try {
            // Setup mock for remove operation (invalidation uses sendRemove)
            when(mockCache.remove("test-key")).thenReturn("test-value");

            for (int i = 0; i < 3; i++) {
                int port = findAvailablePort();
                TcpCommunicationProtocol<String, String> server = TcpCommunicationProtocol.<String, String>builder()
                        .port(port)
                        .timeout(1000)
                        .build();
                server.setLocalCache(mockCache);
                server.startServer();
                Thread.sleep(50);

                servers.add(server);
                nodeAddresses.add("localhost:" + port);
            }

            // Broadcast invalidation
            CompletableFuture<Map<String, CommunicationProtocol.CommunicationResult<Void>>> future = clientProtocol
                    .broadcastInvalidation(nodeAddresses, "test-key");

            Map<String, CommunicationProtocol.CommunicationResult<Void>> results = future.get(3, TimeUnit.SECONDS);

            assertNotNull(results);
            assertEquals(3, results.size());

            for (CommunicationProtocol.CommunicationResult<Void> result : results.values()) {
                assertTrue(result.isSuccess());
            }

        } finally {
            // Cleanup servers
            for (TcpCommunicationProtocol<String, String> server : servers) {
                if (server.isRunning()) {
                    server.stopServer().join();
                }
            }
        }
    }

    // ============= Metrics Tests =============

    @Test
    @Order(17)
    void testMetricsTracking() throws Exception {
        // Start server
        serverProtocol.setLocalCache(mockCache);
        serverProtocol.startServer();
        Thread.sleep(100);

        // Get initial metrics
        Map<String, Object> initialMetrics = clientProtocol.getMetrics();
        long initialSent = (Long) initialMetrics.get("messagesSent");

        // Perform operations
        String nodeAddress = "localhost:" + serverPort;
        clientProtocol.sendPut(nodeAddress, "key1", "value1").get(2, TimeUnit.SECONDS);
        clientProtocol.sendGet(nodeAddress, "key1").get(2, TimeUnit.SECONDS);

        // Check updated metrics
        Map<String, Object> finalMetrics = clientProtocol.getMetrics();
        long finalSent = (Long) finalMetrics.get("messagesSent");

        assertTrue(finalSent > initialSent);
        assertEquals("TCP", finalMetrics.get("protocolType"));
        assertTrue(finalMetrics.containsKey("port"));
        assertTrue(finalMetrics.containsKey("messagesReceived"));
        assertTrue(finalMetrics.containsKey("connectionFailures"));
    }

    // ============= Integration Tests =============

    @Test
    @Order(18)
    void testFullCommunicationWorkflow() throws Exception {
        // Setup real cache for integration test
        Cache<String, String> serverCache = new DefaultCache<>(CacheConfig.<String, String>builder().build());
        serverProtocol.setLocalCache(serverCache);
        serverProtocol.startServer();
        Thread.sleep(100);

        String nodeAddress = "localhost:" + serverPort;

        // Test full workflow: PUT -> GET -> REMOVE -> GET (should return null)

        // 1. PUT
        CommunicationProtocol.CommunicationResult<Void> putResult = clientProtocol
                .sendPut(nodeAddress, "workflow-key", "workflow-value")
                .get(2, TimeUnit.SECONDS);
        assertTrue(putResult.isSuccess());

        // 2. GET (should return the value)
        CommunicationProtocol.CommunicationResult<String> getResult1 = clientProtocol
                .sendGet(nodeAddress, "workflow-key")
                .get(2, TimeUnit.SECONDS);
        assertTrue(getResult1.isSuccess());
        assertEquals("workflow-value", getResult1.getResult());

        // 3. REMOVE
        CommunicationProtocol.CommunicationResult<String> removeResult = clientProtocol
                .sendRemove(nodeAddress, "workflow-key")
                .get(2, TimeUnit.SECONDS);
        assertTrue(removeResult.isSuccess());
        assertEquals("workflow-value", removeResult.getResult());

        // 4. GET (should return null)
        CommunicationProtocol.CommunicationResult<String> getResult2 = clientProtocol
                .sendGet(nodeAddress, "workflow-key")
                .get(2, TimeUnit.SECONDS);
        assertTrue(getResult2.isSuccess());
        assertNull(getResult2.getResult());
    }
}
