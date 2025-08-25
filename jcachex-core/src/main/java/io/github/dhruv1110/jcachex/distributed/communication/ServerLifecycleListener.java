package io.github.dhruv1110.jcachex.distributed.communication;

/**
 * Listener interface for server lifecycle events.
 * <p>
 * This interface allows components to be notified when servers start and stop,
 * enabling event-driven testing and monitoring without relying on timeouts.
 * </p>
 *
 * @since 1.0.0
 */
public interface ServerLifecycleListener {

    /**
     * Called when a server has successfully started and is ready to accept
     * connections.
     *
     * @param serverId unique identifier for the server
     * @param port     the port the server is listening on
     */
    void onServerStarted(String serverId, int port);

    /**
     * Called when a server has successfully stopped and is no longer accepting
     * connections.
     *
     * @param serverId unique identifier for the server
     * @param port     the port the server was listening on
     */
    void onServerStopped(String serverId, int port);

    /**
     * Called when a server fails to start.
     *
     * @param serverId unique identifier for the server
     * @param port     the port the server attempted to listen on
     * @param error    the error that occurred
     */
    void onServerStartFailed(String serverId, int port, Throwable error);

    /**
     * Called when a server fails to stop gracefully.
     *
     * @param serverId unique identifier for the server
     * @param port     the port the server was listening on
     * @param error    the error that occurred
     */
    void onServerStopFailed(String serverId, int port, Throwable error);
}
