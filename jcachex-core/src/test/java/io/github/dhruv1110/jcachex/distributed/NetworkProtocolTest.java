package io.github.dhruv1110.jcachex.distributed;

import io.github.dhruv1110.jcachex.distributed.NetworkProtocol.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for NetworkProtocol interface and implementations.
 */
class NetworkProtocolTest {

    private NetworkProtocol tcpProtocol;
    private NetworkProtocol httpProtocol;
    private NetworkProtocol udpProtocol;
    private NetworkProtocol websocketProtocol;

    @BeforeEach
    void setUp() {
        tcpProtocol = NetworkProtocol.tcp()
                .serialization(SerializationType.JAVA)
                .compression(CompressionType.GZIP)
                .encryption(true)
                .port(8080)
                .build();

        httpProtocol = NetworkProtocol.http()
                .serialization(SerializationType.JSON)
                .compression(CompressionType.NONE)
                .port(8081)
                .build();

        udpProtocol = NetworkProtocol.udp()
                .serialization(SerializationType.KRYO)
                .compression(CompressionType.LZ4)
                .port(8082)
                .build();

        websocketProtocol = NetworkProtocol.websocket()
                .serialization(SerializationType.PROTOBUF)
                .compression(CompressionType.SNAPPY)
                .port(8083)
                .build();
    }

    @Nested
    @DisplayName("Protocol Factory Tests")
    class ProtocolFactoryTests {

        @Test
        @DisplayName("TCP protocol factory creates valid protocol")
        void testTcpProtocolFactory() {
            NetworkProtocol protocol = NetworkProtocol.tcp().build();
            assertNotNull(protocol);
        }

        @Test
        @DisplayName("HTTP protocol factory creates valid protocol")
        void testHttpProtocolFactory() {
            NetworkProtocol protocol = NetworkProtocol.http().build();
            assertNotNull(protocol);
        }

        @Test
        @DisplayName("UDP protocol factory creates valid protocol")
        void testUdpProtocolFactory() {
            NetworkProtocol protocol = NetworkProtocol.udp().build();
            assertNotNull(protocol);
        }

        @Test
        @DisplayName("WebSocket protocol factory creates valid protocol")
        void testWebSocketProtocolFactory() {
            NetworkProtocol protocol = NetworkProtocol.websocket().build();
            assertNotNull(protocol);
        }
    }

    @Nested
    @DisplayName("Protocol Builder Tests")
    class ProtocolBuilderTests {

        @Test
        @DisplayName("Builder with all configuration options")
        void testBuilderWithAllOptions() {
            NetworkProtocol protocol = NetworkProtocol.tcp()
                    .serialization(SerializationType.JAVA)
                    .compression(CompressionType.GZIP)
                    .encryption(true)
                    .port(9090)
                    .maxConnections(200)
                    .connectionTimeout(10000)
                    .bufferSize(16384)
                    .build();

            assertNotNull(protocol);
        }

        @Test
        @DisplayName("Builder with minimal configuration")
        void testBuilderWithMinimalOptions() {
            NetworkProtocol protocol = NetworkProtocol.tcp().build();
            assertNotNull(protocol);
        }

        @Test
        @DisplayName("Builder method chaining")
        void testBuilderMethodChaining() {
            NetworkProtocol protocol = NetworkProtocol.tcp()
                    .serialization(SerializationType.JSON)
                    .compression(CompressionType.LZ4)
                    .encryption(false)
                    .port(8080)
                    .build();

            assertNotNull(protocol);
        }
    }

    @Nested
    @DisplayName("Cache Operation Tests")
    class CacheOperationTests {

        @Test
        @DisplayName("PutOperation creation and properties")
        void testPutOperation() {
            String operationId = "put-123";
            String sourceNodeId = "node-1";
            String key = "test-key";
            String value = "test-value";
            DistributedCache.ConsistencyLevel consistency = DistributedCache.ConsistencyLevel.STRONG;

            PutOperation operation = new PutOperation(operationId, sourceNodeId, key, value, consistency);

            assertEquals(operationId, operation.getOperationId());
            assertEquals(sourceNodeId, operation.getSourceNodeId());
            assertEquals(key, operation.getKey());
            assertEquals(value, operation.getValue());
            assertEquals(consistency, operation.getConsistencyLevel());
            assertEquals(OperationType.PUT, operation.getType());
            assertNotNull(operation.getTimestamp());
        }

        @Test
        @DisplayName("GetOperation creation and properties")
        void testGetOperation() {
            String operationId = "get-456";
            String sourceNodeId = "node-2";
            String key = "test-key";
            DistributedCache.ConsistencyLevel consistency = DistributedCache.ConsistencyLevel.EVENTUAL;

            GetOperation operation = new GetOperation(operationId, sourceNodeId, key, consistency);

            assertEquals(operationId, operation.getOperationId());
            assertEquals(sourceNodeId, operation.getSourceNodeId());
            assertEquals(key, operation.getKey());
            assertEquals(consistency, operation.getConsistencyLevel());
            assertEquals(OperationType.GET, operation.getType());
            assertNotNull(operation.getTimestamp());
        }

        @Test
        @DisplayName("InvalidateOperation creation and properties")
        void testInvalidateOperation() {
            String operationId = "invalidate-789";
            String sourceNodeId = "node-3";
            Collection<Object> keys = Arrays.asList("key1", "key2", "key3");

            InvalidateOperation operation = new InvalidateOperation(operationId, sourceNodeId, keys);

            assertEquals(operationId, operation.getOperationId());
            assertEquals(sourceNodeId, operation.getSourceNodeId());
            assertEquals(keys, operation.getKeys());
            assertEquals(OperationType.INVALIDATE, operation.getType());
            assertNotNull(operation.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Operation Response Tests")
    class OperationResponseTests {

        @Test
        @DisplayName("Successful operation response")
        void testSuccessfulResponse() {
            String operationId = "test-123";
            String result = "success-result";

            OperationResponse response = OperationResponse.success(operationId, result);

            assertEquals(operationId, response.getOperationId());
            assertTrue(response.isSuccess());
            assertEquals(result, response.getResult());
            assertNull(response.getErrorMessage());
            assertNotNull(response.getTimestamp());
        }

        @Test
        @DisplayName("Failed operation response")
        void testFailedResponse() {
            String operationId = "test-456";
            String errorMessage = "Operation failed";

            OperationResponse response = OperationResponse.failure(operationId, errorMessage);

            assertEquals(operationId, response.getOperationId());
            assertFalse(response.isSuccess());
            assertNull(response.getResult());
            assertEquals(errorMessage, response.getErrorMessage());
            assertNotNull(response.getTimestamp());
        }

        @Test
        @DisplayName("Full constructor response")
        void testFullConstructorResponse() {
            String operationId = "test-789";
            boolean success = true;
            String result = "test-result";
            String errorMessage = null;

            OperationResponse response = new OperationResponse(operationId, success, result, errorMessage);

            assertEquals(operationId, response.getOperationId());
            assertEquals(success, response.isSuccess());
            assertEquals(result, response.getResult());
            assertEquals(errorMessage, response.getErrorMessage());
            assertNotNull(response.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Network Statistics Tests")
    class NetworkStatisticsTests {

        @Test
        @DisplayName("NetworkStats creation and properties")
        void testNetworkStats() {
            long messagesSent = 100L;
            long messagesReceived = 95L;
            long bytesTransmitted = 10240L;
            long bytesReceived = 9728L;
            double averageLatency = 15.5;
            long connectionFailures = 3L;

            NetworkStats stats = new NetworkStats(messagesSent, messagesReceived, bytesTransmitted,
                    bytesReceived, averageLatency, connectionFailures);

            assertEquals(messagesSent, stats.getMessagesSent());
            assertEquals(messagesReceived, stats.getMessagesReceived());
            assertEquals(bytesTransmitted, stats.getBytesTransmitted());
            assertEquals(bytesReceived, stats.getBytesReceived());
            assertEquals(averageLatency, stats.getAverageLatency());
            assertEquals(connectionFailures, stats.getConnectionFailures());
            assertTrue(stats.getThroughput() > 0);
        }

        @Test
        @DisplayName("NetworkStats throughput calculation")
        void testThroughputCalculation() {
            long bytesTransmitted = 1024 * 1024; // 1 MB
            long bytesReceived = 1024 * 1024; // 1 MB

            NetworkStats stats = new NetworkStats(100, 95, bytesTransmitted, bytesReceived, 10.0, 0);

            assertEquals(2.0, stats.getThroughput(), 0.001); // 2 MB/s
        }

        @Test
        @DisplayName("NetworkStats with zero values")
        void testNetworkStatsWithZeroValues() {
            NetworkStats stats = new NetworkStats(0, 0, 0, 0, 0.0, 0);

            assertEquals(0L, stats.getMessagesSent());
            assertEquals(0L, stats.getMessagesReceived());
            assertEquals(0L, stats.getBytesTransmitted());
            assertEquals(0L, stats.getBytesReceived());
            assertEquals(0.0, stats.getAverageLatency());
            assertEquals(0L, stats.getConnectionFailures());
            assertEquals(0.0, stats.getThroughput());
        }
    }

    @Nested
    @DisplayName("Protocol Communication Tests")
    class ProtocolCommunicationTests {

        @Test
        @DisplayName("Send operation to single node")
        void testSendOperation() throws ExecutionException, InterruptedException, TimeoutException {
            String nodeId = "target-node";
            PutOperation operation = new PutOperation("put-123", "source-node", "key1", "value1",
                    DistributedCache.ConsistencyLevel.STRONG);

            CompletableFuture<OperationResponse> future = tcpProtocol.send(nodeId, operation);
            OperationResponse response = future.get(5, TimeUnit.SECONDS);

            assertNotNull(response);
            assertEquals(operation.getOperationId(), response.getOperationId());
        }

        @Test
        @DisplayName("Broadcast operation to multiple nodes")
        void testBroadcastOperation() throws ExecutionException, InterruptedException, TimeoutException {
            Collection<String> nodeIds = Arrays.asList("node-1", "node-2", "node-3");
            InvalidateOperation operation = new InvalidateOperation("invalidate-456", "source-node",
                    Arrays.asList("key1", "key2"));

            CompletableFuture<Map<String, OperationResponse>> future = tcpProtocol.broadcast(nodeIds, operation);
            Map<String, OperationResponse> responses = future.get(5, TimeUnit.SECONDS);

            assertNotNull(responses);
            assertEquals(nodeIds.size(), responses.size());
            for (String nodeId : nodeIds) {
                assertTrue(responses.containsKey(nodeId));
                assertNotNull(responses.get(nodeId));
            }
        }

        @Test
        @DisplayName("Operation handler registration")
        void testOperationHandlerRegistration() {
            OperationHandler handler = operation -> CompletableFuture.completedFuture(
                    OperationResponse.success(operation.getOperationId(), "handled"));

            assertDoesNotThrow(() -> tcpProtocol.registerHandler(handler));
        }
    }

    @Nested
    @DisplayName("Protocol Lifecycle Tests")
    class ProtocolLifecycleTests {

        @Test
        @DisplayName("Protocol start and stop")
        void testProtocolLifecycle() throws ExecutionException, InterruptedException, TimeoutException {
            CompletableFuture<Void> startFuture = tcpProtocol.start();
            startFuture.get(5, TimeUnit.SECONDS);

            CompletableFuture<Void> stopFuture = tcpProtocol.stop();
            stopFuture.get(5, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("Multiple protocol instances")
        void testMultipleProtocolInstances() {
            NetworkProtocol protocol1 = NetworkProtocol.tcp().port(8080).build();
            NetworkProtocol protocol2 = NetworkProtocol.tcp().port(8081).build();
            NetworkProtocol protocol3 = NetworkProtocol.http().port(8082).build();

            assertNotNull(protocol1);
            assertNotNull(protocol2);
            assertNotNull(protocol3);
            assertNotSame(protocol1, protocol2);
            assertNotSame(protocol1, protocol3);
        }

        @Test
        @DisplayName("Protocol statistics retrieval")
        void testProtocolStatistics() {
            NetworkStats stats = tcpProtocol.getNetworkStats();
            assertNotNull(stats);
            assertTrue(stats.getMessagesSent() >= 0);
            assertTrue(stats.getMessagesReceived() >= 0);
            assertTrue(stats.getBytesTransmitted() >= 0);
            assertTrue(stats.getBytesReceived() >= 0);
            assertTrue(stats.getConnectionFailures() >= 0);
        }
    }

    @Nested
    @DisplayName("Serialization and Compression Tests")
    class SerializationCompressionTests {

        @Test
        @DisplayName("All serialization types are supported")
        void testSerializationTypes() {
            for (SerializationType type : SerializationType.values()) {
                assertDoesNotThrow(() -> {
                    NetworkProtocol protocol = NetworkProtocol.tcp()
                            .serialization(type)
                            .build();
                    assertNotNull(protocol);
                });
            }
        }

        @Test
        @DisplayName("All compression types are supported")
        void testCompressionTypes() {
            for (CompressionType type : CompressionType.values()) {
                assertDoesNotThrow(() -> {
                    NetworkProtocol protocol = NetworkProtocol.tcp()
                            .compression(type)
                            .build();
                    assertNotNull(protocol);
                });
            }
        }

        @Test
        @DisplayName("All protocol types are supported")
        void testProtocolTypes() {
            assertDoesNotThrow(() -> {
                NetworkProtocol tcp = NetworkProtocol.tcp().build();
                NetworkProtocol udp = NetworkProtocol.udp().build();
                NetworkProtocol http = NetworkProtocol.http().build();
                NetworkProtocol ws = NetworkProtocol.websocket().build();

                assertNotNull(tcp);
                assertNotNull(udp);
                assertNotNull(http);
                assertNotNull(ws);
            });
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Send operation with null node ID")
        void testSendWithNullNodeId() {
            PutOperation operation = new PutOperation("put-123", "source", "key", "value",
                    DistributedCache.ConsistencyLevel.STRONG);

            assertDoesNotThrow(() -> {
                CompletableFuture<OperationResponse> future = tcpProtocol.send(null, operation);
                assertNotNull(future);
            });
        }

        @Test
        @DisplayName("Send operation with null operation")
        void testSendWithNullOperation() {
            assertDoesNotThrow(() -> {
                CompletableFuture<OperationResponse> future = tcpProtocol.send("node-1", null);
                assertNotNull(future);
            });
        }

        @Test
        @DisplayName("Broadcast with empty node list")
        void testBroadcastWithEmptyNodes() {
            PutOperation operation = new PutOperation("put-123", "source", "key", "value",
                    DistributedCache.ConsistencyLevel.STRONG);

            assertDoesNotThrow(() -> {
                CompletableFuture<Map<String, OperationResponse>> future = tcpProtocol.broadcast(Arrays.asList(),
                        operation);
                assertNotNull(future);
            });
        }

        @Test
        @DisplayName("Register null operation handler")
        void testRegisterNullHandler() {
            assertDoesNotThrow(() -> tcpProtocol.registerHandler(null));
        }
    }

    @Nested
    @DisplayName("Operation Types and Enums Tests")
    class OperationTypesTests {

        @Test
        @DisplayName("All operation types are defined")
        void testOperationTypes() {
            OperationType[] types = OperationType.values();
            assertTrue(types.length > 0);

            // Verify expected types exist
            boolean foundPut = false, foundGet = false, foundRemove = false;
            for (OperationType type : types) {
                if (type == OperationType.PUT)
                    foundPut = true;
                if (type == OperationType.GET)
                    foundGet = true;
                if (type == OperationType.INVALIDATE)
                    foundRemove = true;
            }

            assertTrue(foundPut);
            assertTrue(foundGet);
            assertTrue(foundRemove);
        }

        @Test
        @DisplayName("All protocol types are defined")
        void testProtocolTypesEnum() {
            ProtocolType[] types = ProtocolType.values();
            assertTrue(types.length >= 4); // TCP, UDP, HTTP, WEBSOCKET
        }

        @Test
        @DisplayName("All serialization types are defined")
        void testSerializationTypesEnum() {
            SerializationType[] types = SerializationType.values();
            assertTrue(types.length >= 5); // JAVA, JSON, AVRO, PROTOBUF, KRYO
        }

        @Test
        @DisplayName("All compression types are defined")
        void testCompressionTypesEnum() {
            CompressionType[] types = CompressionType.values();
            assertTrue(types.length >= 4); // NONE, GZIP, LZ4, SNAPPY
        }
    }
}
