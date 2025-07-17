# 🚀 JCacheX Kubernetes Discovery - Docker Compose Setup

This setup provides a complete Kubernetes environment with k3s and JCacheX for testing the Kubernetes discovery feature without needing a separate Kubernetes cluster.

## 🎯 Overview

The Docker Compose setup includes:
- **k3s Server**: Lightweight Kubernetes cluster
- **JCacheX Applications**: Multiple pods with distributed cache
- **Automatic Deployment**: Kubernetes resources auto-deployed
- **Service Discovery**: Real Kubernetes API-based node discovery

## 📋 Prerequisites

- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)
- At least 4GB RAM available for containers
- Ports 6443, 30080-30082 available
- Java 11+ toolchain (handled automatically by Docker)
- Compatible with both ARM64 (Apple Silicon) and x86_64 architectures

## 🚀 Quick Start

### 1. One-Command Setup
```bash
# Start everything with one command
./start-k8s-demo.sh

# With log following
./start-k8s-demo.sh --logs
```

### 2. Manual Setup
```bash
# Create directories
mkdir -p k3s-config

# Start the cluster
docker-compose up -d --build

# Check status
docker-compose ps

# View logs
docker-compose logs jcachex-client
```

### 3. Cleanup
```bash
# Stop and remove everything
./start-k8s-demo.sh --cleanup

# Or manually
docker-compose down -v
```

## 🏗️ Architecture

```
┌─────────────────────┐    ┌─────────────────────┐
│   k3s-server        │    │   jcachex-client    │
│   (Kubernetes API)  │────│   (Discovery Test)  │
│   Port: 6443        │    │                     │
└─────────────────────┘    └─────────────────────┘
           │
           ▼
┌─────────────────────┐
│   JCacheX Pods      │
│   (3 replicas)      │
│   Ports: 30080-82   │
└─────────────────────┘
```

## 📦 Docker Compose Services

### k3s-server
- **Image**: `rancher/k3s:v1.28.4-k3s1`
- **Purpose**: Kubernetes API server
- **Ports**: 6443 (API), 30080-30082 (NodePorts)
- **Volumes**: k3s config, kubeconfig output

### jcachex-app-builder
- **Purpose**: Builds JCacheX application image
- **Context**: Entire project root
- **Output**: `jcachex-k8s-example:latest`

### k3s-deploy
- **Purpose**: Deploys Kubernetes resources
- **Deploys**: Namespace, RBAC, Deployment, Service
- **Waits**: For deployment to be ready

### jcachex-client
- **Purpose**: Runs discovery test client
- **Environment**: Kubernetes configuration
- **Shows**: Discovery status and cluster info

## 🔧 Configuration

### Environment Variables
```bash
# Kubernetes Configuration
K8S_NAMESPACE=jcachex-demo
K8S_SERVICE_NAME=jcachex-service
K8S_LABEL_SELECTOR=app=jcachex,component=distributed-cache

# Application Configuration
SIMULATION_MODE=false
JAVA_OPTS=-Xmx512m -Xms256m
```

### Kubernetes Resources
- **Namespace**: `jcachex-demo`
- **Service Account**: `jcachex-service-account`
- **Deployment**: `jcachex-app` (3 replicas)
- **Service**: `jcachex-service` (NodePort)

## 🛠️ Usage Commands

### Basic Operations
```bash
# Check cluster status
docker exec k3s-server k3s kubectl get nodes

# View pods
docker exec k3s-server k3s kubectl get pods -n jcachex-demo

# View services
docker exec k3s-server k3s kubectl get services -n jcachex-demo

# View logs
docker exec k3s-server k3s kubectl logs -l app=jcachex -n jcachex-demo
```

### Kubernetes Operations
```bash
# Scale deployment
docker exec k3s-server k3s kubectl scale deployment jcachex-app --replicas=5 -n jcachex-demo

# Restart pods
docker exec k3s-server k3s kubectl rollout restart deployment jcachex-app -n jcachex-demo

# Delete a pod (tests auto-recovery)
docker exec k3s-server k3s kubectl delete pod -l app=jcachex -n jcachex-demo --force

# View events
docker exec k3s-server k3s kubectl get events -n jcachex-demo --sort-by=.metadata.creationTimestamp
```

### Service Discovery Testing
```bash
# Test service endpoints
docker exec k3s-server k3s kubectl get endpoints -n jcachex-demo

# Check service discovery
docker exec k3s-server k3s kubectl get services -n jcachex-demo -o wide

# Monitor pod changes
docker exec k3s-server k3s kubectl get pods -n jcachex-demo -w
```

## 🔍 Troubleshooting

### Common Issues

#### k3s Server Not Starting
```bash
# Check Docker resources
docker system df
docker system prune -f

# Check port conflicts
netstat -tlnp | grep :6443
```

#### Build Failures
```bash
# Java toolchain issues
docker system prune -f
docker-compose build --no-cache jcachex-app-builder

# Architecture-specific issues (ARM64/x86_64)
docker buildx create --use
docker buildx build --platform linux/amd64,linux/arm64 -f example/distributed/Dockerfile .

# Gradle build issues
./gradlew clean build --no-daemon
```

#### Deployment Not Ready
```bash
# Check deployment status
docker exec k3s-server k3s kubectl describe deployment jcachex-app -n jcachex-demo

# Check pod logs
docker exec k3s-server k3s kubectl logs -l app=jcachex -n jcachex-demo

# Check events
docker exec k3s-server k3s kubectl get events -n jcachex-demo
```

#### Discovery Not Working
```bash
# Check service account permissions
docker exec k3s-server k3s kubectl describe serviceaccount jcachex-service-account -n jcachex-demo

# Check cluster role binding
docker exec k3s-server k3s kubectl describe clusterrolebinding jcachex-cluster-role-binding

# Test API access
docker exec k3s-server k3s kubectl auth can-i get pods --as=system:serviceaccount:jcachex-demo:jcachex-service-account
```

### Log Analysis
```bash
# Container logs
docker-compose logs k3s-server
docker-compose logs jcachex-client

# Kubernetes logs
docker exec k3s-server k3s kubectl logs -l app=jcachex -n jcachex-demo --tail=100

# System logs
docker exec k3s-server journalctl -u k3s --no-pager --lines=50
```

## 📊 Monitoring

### Resource Usage
```bash
# Pod resource usage
docker exec k3s-server k3s kubectl top pods -n jcachex-demo

# Node resource usage
docker exec k3s-server k3s kubectl top nodes

# Container stats
docker stats
```

### Health Checks
```bash
# Pod health status
docker exec k3s-server k3s kubectl get pods -n jcachex-demo -o wide

# Service health
curl -f http://localhost:30080/health || echo "Service not healthy"

# Discovery health
docker exec k3s-server k3s kubectl get endpoints -n jcachex-demo
```

## 🧪 Testing Scenarios

### 1. Scale Testing
```bash
# Scale up
docker exec k3s-server k3s kubectl scale deployment jcachex-app --replicas=5 -n jcachex-demo

# Monitor discovery
docker-compose logs jcachex-client

# Scale down
docker exec k3s-server k3s kubectl scale deployment jcachex-app --replicas=2 -n jcachex-demo
```

### 2. Failure Recovery
```bash
# Delete random pod
docker exec k3s-server k3s kubectl delete pod -l app=jcachex -n jcachex-demo --force

# Watch recreation
docker exec k3s-server k3s kubectl get pods -n jcachex-demo -w
```

### 3. Service Discovery
```bash
# Add new pods
docker exec k3s-server k3s kubectl scale deployment jcachex-app --replicas=4 -n jcachex-demo

# Check endpoint updates
docker exec k3s-server k3s kubectl get endpoints jcachex-service -n jcachex-demo -o yaml
```

## 🔧 Advanced Configuration

### Custom Resources
```bash
# Apply custom configuration
kubectl apply -f - <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: jcachex-config
  namespace: jcachex-demo
data:
  discovery.interval: "15s"
  health.check.interval: "5s"
EOF
```

### Network Policies
```bash
# Network isolation
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: jcachex-network-policy
  namespace: jcachex-demo
spec:
  podSelector:
    matchLabels:
      app: jcachex
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: jcachex
    ports:
    - protocol: TCP
      port: 8080
    - protocol: TCP
      port: 8081
EOF
```

## 🎓 Learning Resources

### Understanding the Flow
1. **k3s starts** → Kubernetes API available
2. **Resources deployed** → Pods, Services, RBAC created
3. **Discovery active** → JCacheX queries Kubernetes API
4. **Cluster formed** → Nodes discover each other
5. **Cache distributed** → Data replicated across nodes

### Key Concepts
- **Service Discovery**: How pods find each other
- **NodePort Services**: External access to services
- **RBAC**: Role-based access control
- **Readiness Probes**: Health check mechanism
- **Endpoints**: Service-to-pod mapping

## 🚨 Important Notes

1. **Resource Requirements**: Ensure adequate RAM/CPU
2. **Port Conflicts**: Check for port 6443 conflicts
3. **Docker Resources**: Monitor Docker resource usage
4. **Cleanup**: Always cleanup after testing
5. **Persistence**: Data is ephemeral (lost on restart)

## 📚 Next Steps

1. **Modify Discovery Settings**: Edit `deployment.yaml`
2. **Add Monitoring**: Integrate Prometheus/Grafana
3. **Test Production**: Deploy to real Kubernetes
4. **Customize Networking**: Add ingress controllers
5. **Add Persistence**: Use persistent volumes

## 🤝 Support

For issues or questions:
1. Check logs: `docker-compose logs`
2. Verify resources: `docker exec k3s-server k3s kubectl get all -n jcachex-demo`
3. Review troubleshooting section above
4. Check JCacheX documentation

---

**Happy Testing!** 🎉
