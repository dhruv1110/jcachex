package io.github.dhruv1110.jcachex.example.distributed.staticnode.controller;

import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * REST Controller for cache operations.
 * Provides endpoints to test distributed cache functionality.
 */
@RestController
@RequestMapping("/cache")
public class CacheController {

    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

    @Autowired
    private DistributedCache<String, Object> distributedCache;

    @Value("${spring.application.name:node}")
    private String nodeName;

    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * Get a value from the cache
     */
    @GetMapping("/{key}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String key) {
        logger.info("🔍 [{}:{}] GET request for key: {}", nodeName, serverPort, key);

        try {
            Object value = distributedCache.get(key);

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("key", key);
            response.put("value", value);
            response.put("found", value != null);

            if (value != null) {
                logger.info("✅ [{}:{}] Found value for key '{}': {}", nodeName, serverPort, key, value);
            } else {
                logger.info("❌ [{}:{}] No value found for key '{}'", nodeName, serverPort, key);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("💥 [{}:{}] Error getting key '{}': {}", nodeName, serverPort, key, e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("key", key);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Put a value in the cache
     */
    @PutMapping("/{key}")
    public ResponseEntity<Map<String, Object>> put(@PathVariable String key, @RequestBody Object value) {
        logger.info("📝 [{}:{}] PUT request for key '{}' with value: {}", nodeName, serverPort, key, value);

        try {
            distributedCache.put(key, value);

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("key", key);
            response.put("value", value);
            response.put("action", "stored");
            response.put("timestamp", System.currentTimeMillis());

            logger.info("✅ [{}:{}] Successfully stored key '{}' -> '{}'", nodeName, serverPort, key, value);
            logger.info("🔄 [{}:{}] Cache put operation will replicate to cluster nodes", nodeName, serverPort);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("💥 [{}:{}] Error storing key '{}': {}", nodeName, serverPort, key, e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("key", key);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Delete a value from the cache
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String key) {
        logger.info("🗑️ [{}:{}] DELETE request for key: {}", nodeName, serverPort, key);

        try {
            Object oldValue = distributedCache.remove(key);

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("key", key);
            response.put("oldValue", oldValue);
            response.put("action", "deleted");
            response.put("timestamp", System.currentTimeMillis());

            logger.info("✅ [{}:{}] Successfully deleted key '{}', old value: {}", nodeName, serverPort, key, oldValue);
            logger.info("🔄 [{}:{}] Cache delete operation will replicate to cluster nodes", nodeName, serverPort);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("💥 [{}:{}] Error deleting key '{}': {}", nodeName, serverPort, key, e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("key", key);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Clear all values from the cache
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clear() {
        logger.info("🧹 [{}:{}] CLEAR request - clearing entire cache", nodeName, serverPort);

        try {
            long sizeBefore = distributedCache.size();
            distributedCache.clear();

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("action", "cleared");
            response.put("itemsCleared", sizeBefore);
            response.put("timestamp", System.currentTimeMillis());

            logger.info("✅ [{}:{}] Successfully cleared cache, {} items removed", nodeName, serverPort, sizeBefore);
            logger.info("🔄 [{}:{}] Cache clear operation will replicate to cluster nodes", nodeName, serverPort);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("💥 [{}:{}] Error clearing cache: {}", nodeName, serverPort, e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get all keys in the cache
     */
    @GetMapping("/keys")
    public ResponseEntity<Map<String, Object>> getAllKeys() {
        logger.info("🔑 [{}:{}] GET ALL KEYS request", nodeName, serverPort);

        try {
            Set<String> keys = distributedCache.keys();

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("keys", keys);
            response.put("count", keys.size());
            response.put("timestamp", System.currentTimeMillis());

            logger.info("✅ [{}:{}] Found {} keys in cache", nodeName, serverPort, keys.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("💥 [{}:{}] Error getting all keys: {}", nodeName, serverPort, e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        logger.info("📊 [{}:{}] STATS request", nodeName, serverPort);

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("cacheSize", distributedCache.size());
            response.put("stats", distributedCache.stats());
            response.put("clusterTopology", distributedCache.getClusterTopology());
            response.put("nodeStatuses", distributedCache.getNodeStatuses());
            response.put("distributedMetrics", distributedCache.getDistributedMetrics());
            response.put("timestamp", System.currentTimeMillis());

            logger.info("✅ [{}:{}] Cache stats retrieved successfully", nodeName, serverPort);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("💥 [{}:{}] Error getting cache stats: {}", nodeName, serverPort, e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("node", nodeName + ":" + serverPort);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
