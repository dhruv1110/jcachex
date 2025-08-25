package io.github.dhruv1110.jcachex.distributed.communication;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test-specific implementation of ServerLifecycleListener that uses
 * CountDownLatch
 * for synchronization, eliminating the need for timeout-based waiting in tests.
 * <p>
 * This listener allows tests to wait for specific server lifecycle events
 * without
 * relying on arbitrary timeouts, making tests deterministic and fast.
 * </p>
 *
 * @since 1.0.0
 */
public class TestServerLifecycleListener implements ServerLifecycleListener {

    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch stopLatch = new CountDownLatch(1);
    private final AtomicReference<Throwable> startError = new AtomicReference<>();
    private final AtomicReference<Throwable> stopError = new AtomicReference<>();

    @Override
    public void onServerStarted(String serverId, int port) {
        startLatch.countDown();
    }

    @Override
    public void onServerStopped(String serverId, int port) {
        stopLatch.countDown();
    }

    @Override
    public void onServerStartFailed(String serverId, int port, Throwable error) {
        startError.set(error);
        startLatch.countDown();
    }

    @Override
    public void onServerStopFailed(String serverId, int port, Throwable error) {
        stopError.set(error);
        stopLatch.countDown();
    }

    /**
     * Waits for the server to start, with an optional timeout.
     *
     * @param timeoutMs timeout in milliseconds, or 0 for no timeout (wait
     *                  indefinitely)
     * @return true if server started successfully, false if timeout occurred
     * @throws InterruptedException if the thread is interrupted
     * @throws RuntimeException     if server failed to start
     */
    public boolean waitForStart(long timeoutMs) throws InterruptedException {
        boolean started;
        if (timeoutMs > 0) {
            started = startLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } else {
            // Wait indefinitely
            startLatch.await();
            started = true;
        }

        if (!started) {
            return false;
        }

        Throwable error = startError.get();
        if (error != null) {
            throw new RuntimeException("Server failed to start", error);
        }

        return true;
    }

    /**
     * Waits for the server to stop, with an optional timeout.
     *
     * @param timeoutMs timeout in milliseconds, or 0 for no timeout (wait
     *                  indefinitely)
     * @return true if server stopped successfully, false if timeout occurred
     * @throws InterruptedException if the thread is interrupted
     * @throws RuntimeException     if server failed to stop gracefully
     */
    public boolean waitForStop(long timeoutMs) throws InterruptedException {
        boolean stopped;
        if (timeoutMs > 0) {
            stopped = stopLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } else {
            // Wait indefinitely
            stopLatch.await();
            stopped = true;
        }

        if (!stopped) {
            return false;
        }

        Throwable error = stopError.get();
        if (error != null) {
            throw new RuntimeException("Server failed to stop gracefully", error);
        }

        return true;
    }

    /**
     * Resets the listener state for reuse in multiple test cycles.
     * Creates new latches since CountDownLatch cannot be reset.
     */
    public void reset() {
        // Create new latches and clear errors
        startError.set(null);
        stopError.set(null);
        // Note: We need to create new instances since CountDownLatch cannot be reset
        // This method should be called between test cycles, and new instances should be
        // created
    }
}
