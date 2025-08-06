package io.github.dhruv1110.jcachex.distributed.communication;

import io.github.dhruv1110.jcachex.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractCommunicationProtocolTest {

    @Mock
    private Cache<String, String> mockCache;

    private TestCommunicationProtocol protocol;

    @BeforeEach
    void setUp() {
        CommunicationProtocol.ProtocolConfig config = new CommunicationProtocol.ProtocolConfig(
                CommunicationProtocol.ProtocolType.TCP, 8080, 5000, 100, 8192, Collections.emptyMap());
        protocol = new TestCommunicationProtocol(config);
        protocol.setLocalCache(mockCache);
    }

    // ============= Configuration Tests =============

    @Test
    void testGetConfig() {
        CommunicationProtocol.ProtocolConfig config = protocol.getConfig();

        assertNotNull(config);
        assertEquals(CommunicationProtocol.ProtocolType.TCP, config.getProtocolType());
        assertEquals(8080, config.getPort());
        assertEquals(5000, config.getTimeoutMs());
    }

    @Test
    void testGetProtocolType() {
        assertEquals(CommunicationProtocol.ProtocolType.TCP, protocol.getProtocolType());
    }

    @Test
    void testIsRunning() {
        assertFalse(protocol.isRunning());

        protocol.startServer();
        assertTrue(protocol.isRunning());

        protocol.stopServer();
        assertFalse(protocol.isRunning());
    }

    // ============= Metrics Tests =============

    @Test
    void testGetMetrics() {
        Map<String, Object> metrics = protocol.getMetrics();

        assertNotNull(metrics);
        assertTrue(metrics.containsKey("messagesSent"));
        assertTrue(metrics.containsKey("messagesReceived"));
        assertTrue(metrics.containsKey("connectionFailures"));
        assertTrue(metrics.containsKey("protocolType"));
        assertTrue(metrics.containsKey("port"));

        assertEquals(0L, metrics.get("messagesSent"));
        assertEquals(0L, metrics.get("messagesReceived"));
        assertEquals(0L, metrics.get("connectionFailures"));
    }

    @Test
    void testMetricsUpdatedAfterOperations() throws Exception {
        // Simulate some operations
        protocol.sendPut("localhost:8080", "key1", "value1").get(1, TimeUnit.SECONDS);
        protocol.sendGet("localhost:8080", "key1").get(1, TimeUnit.SECONDS);

        Map<String, Object> metrics = protocol.getMetrics();
        assertTrue((Long) metrics.get("messagesSent") > 0);
    }

    // ============= Cache Operations Tests =============

    @Test
    void testSendPut() throws Exception {
        String nodeAddress = "localhost:8080";
        String key = "test-key";
        String value = "test-value";

        CompletableFuture<CommunicationProtocol.CommunicationResult<Void>> future = protocol.sendPut(nodeAddress, key,
                value);

        CommunicationProtocol.CommunicationResult<Void> result = future.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getResult());
        assertNotNull(result.getLatency());
    }

    @Test
    void testSendGet() throws Exception {
        String nodeAddress = "localhost:8080";
        String key = "test-key";

        CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = protocol.sendGet(nodeAddress,
                key);

        CommunicationProtocol.CommunicationResult<String> result = future.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getLatency());
    }

    @Test
    void testSendRemove() throws Exception {
        String nodeAddress = "localhost:8080";
        String key = "test-key";

        CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = protocol.sendRemove(nodeAddress,
                key);

        CommunicationProtocol.CommunicationResult<String> result = future.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getLatency());
    }

    @Test
    void testSendHealthCheck() throws Exception {
        String nodeAddress = "localhost:8080";

        CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = protocol
                .sendHealthCheck(nodeAddress);

        CommunicationProtocol.CommunicationResult<String> result = future.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("OK", result.getResult());
        assertNotNull(result.getLatency());
    }

    @Test
    void testRequestKeyMigration() throws Exception {
        String nodeAddress = "localhost:8080";
        Collection<String> keys = Arrays.asList("key1", "key2", "key3");

        CompletableFuture<CommunicationProtocol.CommunicationResult<Map<String, String>>> future = protocol
                .requestKeyMigration(nodeAddress, keys);

        CommunicationProtocol.CommunicationResult<Map<String, String>> result = future.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getLatency());
    }

    @Test
    void testRequestClusterInfo() throws Exception {
        String nodeAddress = "localhost:8080";

        CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = protocol
                .requestClusterInfo(nodeAddress);

        CommunicationProtocol.CommunicationResult<String> result = future.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Cluster Info", result.getResult());
        assertNotNull(result.getLatency());
    }

    // ============= Broadcast Operations Tests =============

    @Test
    void testBroadcastInvalidation() throws Exception {
        Collection<String> nodeAddresses = Arrays.asList("node1:8080", "node2:8080", "node3:8080");
        String key = "test-key";

        CompletableFuture<Map<String, CommunicationProtocol.CommunicationResult<Void>>> future = protocol
                .broadcastInvalidation(nodeAddresses, key);

        Map<String, CommunicationProtocol.CommunicationResult<Void>> results = future.get(10, TimeUnit.SECONDS);

        assertNotNull(results);
        assertEquals(3, results.size());

        for (String nodeAddress : nodeAddresses) {
            assertTrue(results.containsKey(nodeAddress));
            CommunicationProtocol.CommunicationResult<Void> result = results.get(nodeAddress);
            assertNotNull(result);
        }
    }

    @Test
    void testBroadcastClear() throws Exception {
        Collection<String> nodeAddresses = Arrays.asList("node1:8080", "node2:8080", "node3:8080");

        CompletableFuture<Map<String, CommunicationProtocol.CommunicationResult<Void>>> future = protocol
                .broadcastClear(nodeAddresses);

        Map<String, CommunicationProtocol.CommunicationResult<Void>> results = future.get(10, TimeUnit.SECONDS);

        assertNotNull(results);
        assertEquals(3, results.size());

        for (String nodeAddress : nodeAddresses) {
            assertTrue(results.containsKey(nodeAddress));
            CommunicationProtocol.CommunicationResult<Void> result = results.get(nodeAddress);
            assertNotNull(result);
        }
    }

    // ============= Error Handling Tests =============

    @Test
    void testCommunicationResultSuccess() {
        String result = "success-result";
        Duration latency = Duration.ofMillis(100);

        CommunicationProtocol.CommunicationResult<String> commResult = CommunicationProtocol.CommunicationResult
                .success(result, latency);

        assertTrue(commResult.isSuccess());
        assertEquals(result, commResult.getResult());
        assertEquals(latency, commResult.getLatency());
        assertNull(commResult.getError());
        assertNull(commResult.getErrorMessage());
    }

    @Test
    void testCommunicationResultFailure() {
        String errorMessage = "Test error";
        Exception error = new RuntimeException("Test exception");

        CommunicationProtocol.CommunicationResult<String> commResult = CommunicationProtocol.CommunicationResult
                .failure(errorMessage, error);

        assertFalse(commResult.isSuccess());
        assertNull(commResult.getResult());
        assertEquals(errorMessage, commResult.getErrorMessage());
        assertEquals(error, commResult.getError());
    }

    // ============= Test Implementation Class =============

    private static class TestCommunicationProtocol extends AbstractCommunicationProtocol<String, String> {

        public TestCommunicationProtocol(CommunicationProtocol.ProtocolConfig config) {
            super(config);
        }

        @Override
        public CommunicationProtocol.ProtocolType getProtocolType() {
            return CommunicationProtocol.ProtocolType.TCP;
        }

        @Override
        public CompletableFuture<Void> startServer() {
            running.set(true);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> stopServer() {
            running.set(false);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<CommunicationProtocol.CommunicationResult<String>> sendHealthCheck(
                String nodeAddress) {
            messagesSent.incrementAndGet();
            return CompletableFuture.completedFuture(
                    CommunicationProtocol.CommunicationResult.success("OK", Duration.ofMillis(10)));
        }

        @Override
        public CompletableFuture<CommunicationProtocol.CommunicationResult<Map<String, String>>> requestKeyMigration(
                String nodeAddress, Collection<String> keys) {
            messagesSent.incrementAndGet();
            Map<String, String> result = new HashMap<>();
            result.put("key1", "value1");
            result.put("key2", "value2");
            return CompletableFuture.completedFuture(
                    CommunicationProtocol.CommunicationResult.success(result, Duration.ofMillis(10)));
        }

        @Override
        public CompletableFuture<CommunicationProtocol.CommunicationResult<String>> requestClusterInfo(
                String nodeAddress) {
            messagesSent.incrementAndGet();
            return CompletableFuture.completedFuture(
                    CommunicationProtocol.CommunicationResult.success("Cluster Info", Duration.ofMillis(10)));
        }

        @Override
        protected CompletableFuture<CacheOperationResponse> sendRequest(String nodeAddress,
                CacheOperationRequest request) {
            messagesSent.incrementAndGet();

            // Simple mock response for other operations
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                CacheOperationResponse response = CacheOperationResponse.success(serializeObject("mock-result"));
                response.setLatency(Duration.ofMillis(10));
                return response;
            });
        }
    }
}
