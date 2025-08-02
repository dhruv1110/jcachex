package io.github.dhruv1110.jcachex.example.distributed.kubernetes.controller;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.distributed.DistributedCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class CacheController {

    private final Logger logger = LoggerFactory.getLogger(CacheController.class);

    private final DistributedCache<String, String> distributedCache;

    public CacheController(@Qualifier("distributedCache") DistributedCache<String, String> distributedCache) {
        this.distributedCache = distributedCache;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "JCacheX Kubernetes Example");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }

    @PostMapping("/cache/{key}")
    public Map<String, String> putCache(@PathVariable String key, @RequestBody Map<String, String> payload) {
        String value = payload.get("value");
        distributedCache.put(key, value);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Cached successfully");
        response.put("key", key);
        response.put("value", value);
        logger.info("Cached successfully");
        return response;
    }

    @GetMapping("/cache/{key}")
    public Map<String, String> getCache(@PathVariable String key) {
        String value = distributedCache.get(key);

        Map<String, String> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value != null ? value : "Not found");
        response.put("hit", String.valueOf(value != null));
        logger.info("Retrieved successfully");
        return response;
    }

    @GetMapping("/cache/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> response = new HashMap<>();

        // Get cluster topology and node statuses
        try {
            var topology = distributedCache.getClusterTopology();
            var nodeStatuses = distributedCache.getNodeStatuses();
            var perNodeStats = distributedCache.getPerNodeStats();

            response.put("clusterSize", topology.getNodes().size());
            response.put("healthyNodes", topology.getHealthyNodeCount());
            response.put("activeNodes", nodeStatuses.size());
            response.put("nodeStatuses", nodeStatuses);
            response.put("perNodeStats", perNodeStats);
        } catch (Exception e) {
            response.put("error", "Unable to retrieve distributed cache stats: " + e.getMessage());
        }

        logger.info("Cache stats retrieved successfully");
        return response;
    }

    @DeleteMapping("/cache/{key}")
    public Map<String, String> deleteCache(@PathVariable String key) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> removed = distributedCache.invalidateGlobally(key);
        removed.get();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Key removed globally");
        response.put("key", key);
        logger.info("Cache deleted successfully");
        return response;
    }
}
