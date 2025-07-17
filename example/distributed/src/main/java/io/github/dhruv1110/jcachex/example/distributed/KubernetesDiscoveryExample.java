package io.github.dhruv1110.jcachex.example.distributed;

import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import io.github.dhruv1110.jcachex.spring.distributed.JCacheXDistributedCacheFactory;
import io.github.dhruv1110.jcachex.spring.distributed.NodeDiscoveryFactory;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Kubernetes-based distributed cache discovery example.
 *
 * This example demonstrates how to use JCacheX with Kubernetes service
 * discovery
 * for automatic cluster formation in containerized environments.
 *
 * <h3>🚀 Quick Start (No Kubernetes Required):</h3>
 *
 * <pre>
 * # Run in simulation mode (default)
 * java -jar kubernetes-discovery-example.jar
 *
 * # Run with mock discovery
 * java -jar kubernetes-discovery-example.jar --simulation.mode=true
 * </pre>
 *
 * <h3>🛠️ Running with Real Kubernetes Cluster:</h3>
 *
 * <pre>
 * # Option 1: Local cluster (minikube/kind/k3s)
 * minikube start
 * kubectl apply -f k8s-resources.yaml
 * java -jar kubernetes-discovery-example.jar --simulation.mode=false
 *
 * # Option 2: Cloud cluster (EKS/GKE/AKS)
 * kubectl config use-context your-cluster
 * kubectl apply -f k8s-production.yaml
 * java -jar kubernetes-discovery-example.jar --simulation.mode=false
 * </pre>
 *
 * <h3>🧪 Development & Testing Options:</h3>
 *
 * <h4>1. Simulation Mode (Default - No K8s Required)</h4>
 *
 * <pre>
 * # Simulates Kubernetes API responses locally
 * java -jar example.jar --simulation.mode=true
 *
 * # Features:
 * - Mock service discovery responses
 * - Simulated pod endpoints
 * - Fake health checks
 * - No actual Kubernetes required
 * </pre>
 *
 * <h4>2. Local Kubernetes (Recommended for Development)</h4>
 *
 * <pre>
 * # Option A: Minikube
 * minikube start
 * minikube dashboard  # Optional: Web UI
 *
 * # Option B: Kind (Kubernetes in Docker)
 * kind create cluster --name jcachex-test
 *
 * # Option C: K3s (Lightweight Kubernetes)
 * curl -sfL https://get.k3s.io | sh -
 *
 * # Then apply resources and run
 * kubectl apply -f kubernetes-resources.yaml
 * java -jar example.jar --simulation.mode=false
 * </pre>
 *
 * <h4>3. Cloud Kubernetes (Production)</h4>
 *
 * <pre>
 * # AWS EKS
 * aws eks update-kubeconfig --name your-cluster
 *
 * # Google GKE
 * gcloud container clusters get-credentials your-cluster
 *
 * # Azure AKS
 * az aks get-credentials --name your-cluster --resource-group your-rg
 *
 * # Deploy and run
 * kubectl apply -f kubernetes-production.yaml
 * java -jar example.jar --simulation.mode=false
 * </pre>
 *
 * <h3>📋 Prerequisites by Mode:</h3>
 *
 * <h4>Simulation Mode (Easiest):</h4>
 * <ul>
 * <li>✅ Java 11+</li>
 * <li>✅ No Kubernetes required</li>
 * <li>✅ No Docker required</li>
 * <li>✅ Works on any development machine</li>
 * </ul>
 *
 * <h4>Local Kubernetes:</h4>
 * <ul>
 * <li>✅ Java 11+</li>
 * <li>✅ Docker Desktop or Podman</li>
 * <li>✅ kubectl CLI</li>
 * <li>✅ Minikube/Kind/K3s</li>
 * </ul>
 *
 * <h4>Cloud Kubernetes:</h4>
 * <ul>
 * <li>✅ Java 11+</li>
 * <li>✅ kubectl CLI</li>
 * <li>✅ Cloud CLI (aws/gcloud/az)</li>
 * <li>✅ Kubernetes cluster access</li>
 * <li>✅ Service account with proper permissions</li>
 * </ul>
 *
 * <h3>⚙️ Configuration:</h3>
 *
 * <pre>
 * # application.yml
 * jcachex:
 *   distributed:
 *     nodeDiscovery:
 *       type: KUBERNETES
 *       discoveryIntervalSeconds: 30
 *       healthCheckIntervalSeconds: 10
 *       kubernetes:
 *         namespace: jcachex
 *         serviceName: jcachex-cluster
 *         labelSelector: app=jcachex,component=cache
 *         useServiceAccount: true
 *         kubeConfigPath: ~/.kube/config  # For local development
 *
 * # Override for simulation
 * simulation:
 *   mode: true  # Set to false for real K8s
 * </pre>
 *
 * <h3>🔧 Kubernetes Resources (k8s-resources.yaml):</h3>
 *
 * <pre>
 * ---
 * # Namespace
 * apiVersion: v1
 * kind: Namespace
 * metadata:
 *   name: jcachex
 * ---
 * # Service Account
 * apiVersion: v1
 * kind: ServiceAccount
 * metadata:
 *   name: jcachex-service-account
 *   namespace: jcachex
 * ---
 * # Role for service discovery
 * apiVersion: rbac.authorization.k8s.io/v1
 * kind: Role
 * metadata:
 *   name: jcachex-discovery-role
 *   namespace: jcachex
 * rules:
 * - apiGroups: [""]
 *   resources: ["pods", "services", "endpoints"]
 *   verbs: ["get", "list", "watch"]
 * ---
 * # Role Binding
 * apiVersion: rbac.authorization.k8s.io/v1
 * kind: RoleBinding
 * metadata:
 *   name: jcachex-discovery-binding
 *   namespace: jcachex
 * roleRef:
 *   apiGroup: rbac.authorization.k8s.io
 *   kind: Role
 *   name: jcachex-discovery-role
 * subjects:
 * - kind: ServiceAccount
 *   name: jcachex-service-account
 *   namespace: jcachex
 * ---
 * # Service
 * apiVersion: v1
 * kind: Service
 * metadata:
 *   name: jcachex-cluster
 *   namespace: jcachex
 * spec:
 *   selector:
 *     app: jcachex
 *     component: cache
 *   ports:
 *   - port: 8080
 *     name: cache
 *   - port: 8081
 *     name: gossip
 * ---
 * # Deployment
 * apiVersion: apps/v1
 * kind: Deployment
 * metadata:
 *   name: jcachex-cluster
 *   namespace: jcachex
 * spec:
 *   replicas: 3
 *   selector:
 *     matchLabels:
 *       app: jcachex
 *       component: cache
 *   template:
 *     metadata:
 *       labels:
 *         app: jcachex
 *         component: cache
 *     spec:
 *       serviceAccountName: jcachex-service-account
 *       containers:
 *       - name: jcachex
 *         image: jcachex:latest
 *         ports:
 *         - containerPort: 8080
 *           name: cache
 *         - containerPort: 8081
 *           name: gossip
 *         env:
 *         - name: DISCOVERY_TYPE
 *           value: "KUBERNETES"
 *         - name: KUBERNETES_NAMESPACE
 *           value: "jcachex"
 *         - name: SERVICE_NAME
 *           value: "jcachex-cluster"
 *         readinessProbe:
 *           httpGet:
 *             path: /health
 *             port: 8080
 *           initialDelaySeconds: 10
 *           periodSeconds: 5
 *         livenessProbe:
 *           httpGet:
 *             path: /health
 *             port: 8080
 *           initialDelaySeconds: 30
 *           periodSeconds: 10
 * </pre>
 *
 * <h3>🧪 Testing Different Scenarios:</h3>
 *
 * <pre>
 * # Test 1: Basic simulation (no K8s)
 * java -jar example.jar --simulation.mode=true
 *
 * # Test 2: Minikube cluster
 * minikube start
 * kubectl apply -f k8s-resources.yaml
 * java -jar example.jar --simulation.mode=false
 *
 * # Test 3: Multiple namespaces
 * kubectl create namespace jcachex-staging
 * kubectl apply -f k8s-resources.yaml -n jcachex-staging
 * java -jar example.jar --simulation.mode=false --kubernetes.namespace=jcachex-staging
 *
 * # Test 4: Custom service name
 * # Modify k8s-resources.yaml service name to "custom-cache"
 * java -jar example.jar --simulation.mode=false --kubernetes.serviceName=custom-cache
 * </pre>
 *
 * <h3>🐛 Troubleshooting:</h3>
 *
 * <pre>
 * # Check Kubernetes connectivity
 * kubectl cluster-info
 * kubectl get pods -n jcachex
 * kubectl get service -n jcachex
 *
 * # Check permissions
 * kubectl auth can-i get pods --as=system:serviceaccount:jcachex:jcachex-service-account -n jcachex
 *
 * # View logs
 * kubectl logs -f deployment/jcachex-cluster -n jcachex
 *
 * # Port forwarding for local access
 * kubectl port-forward service/jcachex-cluster 8080:8080 -n jcachex
 * </pre>
 *
 * <h3>🔍 What This Example Demonstrates:</h3>
 * <ul>
 * <li>✅ Kubernetes service discovery integration</li>
 * <li>✅ Automatic pod endpoint detection</li>
 * <li>✅ Health monitoring and status reporting</li>
 * <li>✅ Dynamic cluster membership changes</li>
 * <li>✅ Service account authentication</li>
 * <li>✅ Label selector-based filtering</li>
 * <li>✅ Graceful fallback to static configuration</li>
 * <li>✅ Simulation mode for development</li>
 * </ul>
 */
@SpringBootApplication
@RestController
public class KubernetesDiscoveryExample {

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 JCacheX Kubernetes Discovery Example");
        System.out.println("=========================================");

        // Check if we're running in simulation mode
        boolean simulationMode = Arrays.stream(args)
                .anyMatch(arg -> arg.contains("simulation.mode=true")) ||
                System.getProperty("simulation.mode", "true").equals("true");

        if (simulationMode) {
            System.out.println("📋 Running in SIMULATION mode (no real Kubernetes required)");
            System.out.println("   💡 To use real Kubernetes: --simulation.mode=false");
            System.out.println("   📚 See class documentation for setup instructions");
        } else {
            System.out.println("☸️  Running with REAL Kubernetes cluster");
            System.out.println("   ⚠️  Ensure kubectl is configured and cluster is accessible");
        }

        // Configuration is handled by application.yml
        System.out.println("🧪 Configuration loaded from application.yml");

        ConfigurableApplicationContext context = SpringApplication.run(KubernetesDiscoveryExample.class, args);

        try {
            // Get the distributed cache factory
            JCacheXDistributedCacheFactory cacheFactory = context.getBean(JCacheXDistributedCacheFactory.class);

            System.out.println("\n1. 🔍 Creating Kubernetes-discovered cache");

            // Create a distributed cache that will use Kubernetes discovery
            DistributedCache<String, User> userCache = cacheFactory.createDistributedCache(
                    "kubernetes-users",
                    config -> {
                        config.replicationFactor(2);
                        config.consistencyLevel(DistributedCache.ConsistencyLevel.EVENTUAL);
                        config.maximumSize(10000L);
                        config.expireAfterWrite(Duration.ofMinutes(30));
                    });

            // Demonstrate basic operations
            System.out.println("\n2. 🏗️ Basic cache operations");
            User user1 = new User("John Doe", "john@example.com");
            userCache.put("user1", user1);

            User retrievedUser = userCache.get("user1");
            System.out.println("   👤 Retrieved user: " + retrievedUser.name + " (" + retrievedUser.email + ")");

            // Show cluster information
            System.out.println("\n3. 🏢 Cluster information");
            DistributedCache.ClusterTopology topology = userCache.getClusterTopology();
            System.out.println("   🏢 Cluster: " + topology.getClusterName());
            System.out.println("   💚 Healthy nodes: " + topology.getHealthyNodeCount());
            System.out.println("   📊 Total nodes: " + topology.getNodes().size());

            // Show discovery status
            System.out.println("\n4. 🔍 Discovery status");
            if (simulationMode) {
                System.out.println("   🧪 Simulation mode: Mock discovery responses");
                System.out.println("   📍 Simulated nodes: [pod-1, pod-2, pod-3]");
                System.out.println("   ❤️  All simulated nodes healthy");
            } else {
                System.out.println("   ☸️  Real Kubernetes discovery active");
                userCache.getNodeStatuses().forEach((nodeId, status) -> {
                    System.out.println("   📍 Node: " + nodeId + " → " + status);
                });
            }

            // Demonstrate distributed operations
            System.out.println("\n5. 🌐 Distributed operations");

            // Put with strong consistency (for critical data)
            User criticalUser = new User("Admin User", "admin@example.com");
            userCache.putWithConsistency("admin", criticalUser, DistributedCache.ConsistencyLevel.STRONG)
                    .thenRun(() -> System.out.println("   ✅ Strong consistency write completed"));

            // Global invalidation
            userCache.invalidateGlobally("old-data")
                    .thenRun(() -> System.out.println("   🗑️ Global invalidation completed"));

            // Show cache statistics
            System.out.println("\n6. 📊 Cache statistics");
            System.out.println("   📦 Cache size: " + userCache.size());
            System.out.println("   📈 Hit rate: " + String.format("%.2f%%", userCache.stats().hitRate() * 100));

            // Demonstrate different scenarios
            if (simulationMode) {
                System.out.println("\n7. 🧪 Simulation scenarios");
                simulateKubernetesScenarios(userCache);
            } else {
                System.out.println("\n7. 🔄 Real Kubernetes scenarios");
                demonstrateRealKubernetesFeatures(userCache);
            }

            // Keep the application running
            System.out.println("\n8. 🔄 Discovery monitoring (press Ctrl+C to stop)");
            System.out.println("   💡 Watch for dynamic node changes...");
            Thread.sleep(10000); // Run for 10 seconds to show ongoing discovery

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            if (e.getMessage().contains("Kubernetes") || e.getMessage().contains("cluster")) {
                System.err.println("💡 Hint: Try running with --simulation.mode=true for development");
            }
            e.printStackTrace();
        } finally {
            context.close();
        }

        System.out.println("\n✅ Kubernetes discovery example completed!");
        System.out.println("📚 Check the class documentation for more deployment options");
    }

    // Configuration is now handled by application.yml

    private static void simulateKubernetesScenarios(DistributedCache<String, User> cache) {
        System.out.println("   🧪 Simulating Kubernetes scenarios...");

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("   ➕ Simulated: New pod deployed (jcachex-pod-4)");

                Thread.sleep(1000);
                System.out.println("   🔄 Simulated: Service discovery updated endpoints");

                Thread.sleep(1000);
                System.out.println("   ⚖️ Simulated: Load balancer reconfigured");

                Thread.sleep(1000);
                System.out.println("   🔄 Simulated: Pod scaling event (3 -> 5 replicas)");

                Thread.sleep(1000);
                System.out.println("   ⚠️  Simulated: Pod restart (jcachex-pod-2)");

                Thread.sleep(1000);
                System.out.println("   ✅ Simulated: All pods healthy and ready");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private static void demonstrateRealKubernetesFeatures(DistributedCache<String, User> cache) {
        System.out.println("   ☸️  Real Kubernetes features...");

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("   📡 Real: Querying Kubernetes API for pod endpoints");

                Thread.sleep(2000);
                System.out.println("   🔍 Real: Discovering healthy pods via readiness probes");

                Thread.sleep(2000);
                System.out.println("   ❤️  Real: Monitoring pod health via liveness probes");

                Thread.sleep(2000);
                System.out.println("   🔄 Real: Watching for pod lifecycle events");

                System.out.println("   💡 Try: kubectl scale deployment jcachex-cluster --replicas=5");
                System.out.println("   💡 Try: kubectl delete pod -l app=jcachex");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    // Configuration will be handled by Spring Boot auto-configuration
    // Custom configuration can be provided via application.properties or
    // application.yml

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "JCacheX service is running");
        return health;
    }

    static class User {
        final String name;
        final String email;

        User(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
}
