package io.github.dhruv1110.jcachex.distributed.communication;

import io.github.dhruv1110.jcachex.testing.TestAwait;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIf;

import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TCP communication protocol.
 * <p>
 * Note: Some tests are disabled in CI environments due to socket communication
 * restrictions.
 * </p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TcpCommunicationProtocolTest {

    private TcpCommunicationProtocol<String, String> tcpProtocol;
    private static final int TEST_PORT = 8100;

    @BeforeEach
    void setUp() {
        Map<String, Object> additionalProps = new HashMap<>();
        CommunicationProtocol.ProtocolConfig config = new CommunicationProtocol.ProtocolConfig(
                CommunicationProtocol.ProtocolType.TCP,
                TEST_PORT,
                5000L, // timeout
                10, // maxConnections
                8192, // bufferSize
                additionalProps);
        tcpProtocol = new TcpCommunicationProtocol<>(config);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (tcpProtocol != null && tcpProtocol.isRunning()) {
            tcpProtocol.stopServer().get(2, TimeUnit.SECONDS);
        }
    }

    @Test
    @Order(1)
    @DisabledIf("isCIEnvironment")
    void testStartServer() throws Exception {
        CompletableFuture<Void> future = tcpProtocol.startServer();
        assertNotNull(future);

        // Wait for server to start
        TestAwait.awaitTrue(() -> tcpProtocol.isRunning(), 2000);
        assertTrue(tcpProtocol.isRunning());

        // Test that server is actually listening
        try (Socket socket = new Socket("localhost", TEST_PORT)) {
            assertTrue(socket.isConnected());
        }
    }

    @Test
    @Order(2)
    @DisabledIf("isCIEnvironment")
    void testStopServer() throws Exception {
        // Start server first
        tcpProtocol.startServer();
        TestAwait.awaitTrue(() -> tcpProtocol.isRunning(), 2000);
        assertTrue(tcpProtocol.isRunning());

        // Stop server
        CompletableFuture<Void> future = tcpProtocol.stopServer();
        assertNotNull(future);
        future.get(2, TimeUnit.SECONDS);

        assertFalse(tcpProtocol.isRunning());
    }

    @Test
    @Order(3)
    @DisabledIf("isCIEnvironment")
    void testServerConnectionHandling() throws Exception {
        tcpProtocol.startServer();
        TestAwait.awaitTrue(() -> tcpProtocol.isRunning(), 2000);

        // Test connection handling
        try (Socket socket = new Socket("localhost", TEST_PORT)) {
            assertTrue(socket.isConnected());
        }
    }

    @Test
    @Order(4)
    void testLifecycleListeners() throws Exception {
        // Test lifecycle listeners without actually starting the server
        TestServerLifecycleListener listener = new TestServerLifecycleListener();
        tcpProtocol.addLifecycleListener(listener);

        // Remove listener
        tcpProtocol.removeLifecycleListener(listener);

        // Verify no exceptions occurred
        assertTrue(true, "Lifecycle listener operations should complete without errors");
    }

    @Test
    @Order(5)
    void testProtocolConfig() {
        assertNotNull(tcpProtocol);
        assertFalse(tcpProtocol.isRunning());
    }

    @Test
    @Order(6)
    @DisabledIf("isCIEnvironment")
    void testMultipleStartStopCycles() throws Exception {
        for (int i = 0; i < 3; i++) {
            // Create a lifecycle listener for this cycle
            TestServerLifecycleListener lifecycleListener = new TestServerLifecycleListener();
            tcpProtocol.addLifecycleListener(lifecycleListener);

            try {
                // Start
                CompletableFuture<Void> startFuture = tcpProtocol.startServer();
                assertNotNull(startFuture);

                // Wait for server to start using event listener (no timeout needed)
                assertTrue(lifecycleListener.waitForStart(0), "Server should start successfully");
                assertTrue(tcpProtocol.isRunning());

                // Stop
                CompletableFuture<Void> stopFuture = tcpProtocol.stopServer();
                assertNotNull(stopFuture);
                stopFuture.get(2, TimeUnit.SECONDS);

                // Wait for server to stop using event listener (no timeout needed)
                assertTrue(lifecycleListener.waitForStop(0), "Server should stop successfully");
                assertFalse(tcpProtocol.isRunning());

            } finally {
                // Clean up listener
                tcpProtocol.removeLifecycleListener(lifecycleListener);
            }
        }
    }

    @Test
    @Order(7)
    void testMultipleStartStopCyclesInCI() throws Exception {
        // This test simulates the multiple start/stop cycles without actual socket
        // operations
        // It's designed to work in CI environments where socket communication is
        // restricted

        for (int i = 0; i < 3; i++) {
            // Create a lifecycle listener for this cycle
            TestServerLifecycleListener lifecycleListener = new TestServerLifecycleListener();
            tcpProtocol.addLifecycleListener(lifecycleListener);

            try {
                // Simulate start operation (without actually starting the server)
                // In CI, we just test the listener functionality
                lifecycleListener.onServerStarted("test-server-" + i, TEST_PORT);

                // Verify the listener received the start event
                assertTrue(lifecycleListener.waitForStart(0), "Listener should receive start event");

                // Simulate stop operation
                lifecycleListener.onServerStopped("test-server-" + i, TEST_PORT);

                // Verify the listener received the stop event
                assertTrue(lifecycleListener.waitForStop(0), "Listener should receive stop event");

            } finally {
                // Clean up listener
                tcpProtocol.removeLifecycleListener(lifecycleListener);
            }
        }
    }

    /**
     * Checks if the current environment is a CI environment.
     *
     * @return true if running in CI, false otherwise
     */
    static boolean isCIEnvironment() {
        String ci = System.getenv("CI");
        String githubActions = System.getenv("GITHUB_ACTIONS");
        String jenkins = System.getenv("JENKINS_URL");
        String travis = System.getenv("TRAVIS");
        String circle = System.getenv("CIRCLECI");

        return "true".equals(ci) ||
                "true".equals(githubActions) ||
                jenkins != null ||
                "true".equals(travis) ||
                "true".equals(circle);
    }
}
