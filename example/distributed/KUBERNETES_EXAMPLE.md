# JCacheX Kubernetes Discovery Example

This guide explains how to run the JCacheX Kubernetes discovery example in different environments, from simple simulation to production deployment.

## 🚀 Quick Start (No Kubernetes Required)

The easiest way to understand the example is to run it in **simulation mode**:

```bash
# Clone the repository
git clone https://github.com/dhruv1110/JCacheX.git
cd JCacheX

# Build the project
./gradlew build

# Run in simulation mode (default)
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample

# Or explicitly enable simulation
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=true
```

**Output:**
```
🚀 JCacheX Kubernetes Discovery Example
=========================================
📋 Running in SIMULATION mode (no real Kubernetes required)
   💡 To use real Kubernetes: --simulation.mode=false
   📚 See class documentation for setup instructions
🧪 Setting up simulation mode...
   ✅ Simulation configured - no real Kubernetes needed!

1. 🔍 Creating Kubernetes-discovered cache
2. 🏗️ Basic cache operations
   👤 Retrieved user: John Doe (john@example.com)
3. 🏢 Cluster information
   🏢 Cluster: kubernetes-cluster
   💚 Healthy nodes: 3
   📊 Total nodes: 3
4. 🔍 Discovery status
   🧪 Simulation mode: Mock discovery responses
   📍 Simulated nodes: [pod-1, pod-2, pod-3]
   ❤️  All simulated nodes healthy
7. 🧪 Simulation scenarios
   🧪 Simulating Kubernetes scenarios...
   ➕ Simulated: New pod deployed (jcachex-pod-4)
   🔄 Simulated: Service discovery updated endpoints
   ⚖️ Simulated: Load balancer reconfigured
   🔄 Simulated: Pod scaling event (3 -> 5 replicas)
   ⚠️  Simulated: Pod restart (jcachex-pod-2)
   ✅ Simulated: All pods healthy and ready
```

## 📝 What This Example Demonstrates

### ✅ In Simulation Mode
- Mock Kubernetes API responses
- Simulated pod endpoints and health checks
- Distributed cache operations without real infrastructure
- Understanding of the discovery flow

### ✅ In Real Kubernetes Mode
- Actual Kubernetes service discovery
- Real pod endpoint detection
- Service account authentication
- Label selector-based filtering
- Dynamic cluster membership changes
- Health monitoring integration

## 🛠️ Development Setup Options

### Option 1: Local Kubernetes (Recommended)

#### A. Using Minikube
```bash
# Install minikube
brew install minikube  # macOS
# or
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube  # Linux

# Start minikube
minikube start

# Enable required addons
minikube addons enable metrics-server
minikube addons enable dashboard

# Deploy JCacheX
kubectl apply -f example/distributed/k8s-resources.yaml

# Wait for pods to be ready
kubectl wait --for=condition=ready pod -l app=jcachex -n jcachex --timeout=300s

# Run the example
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=false

# Optional: View dashboard
minikube dashboard
```

#### B. Using Kind (Kubernetes in Docker)
```bash
# Install kind
brew install kind  # macOS
# or
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64
chmod +x ./kind && sudo mv ./kind /usr/local/bin/kind  # Linux

# Create cluster
kind create cluster --name jcachex-test

# Deploy JCacheX
kubectl apply -f example/distributed/k8s-resources.yaml

# Wait for pods
kubectl wait --for=condition=ready pod -l app=jcachex -n jcachex --timeout=300s

# Run example
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=false
```

#### C. Using K3s (Lightweight Kubernetes)
```bash
# Install k3s
curl -sfL https://get.k3s.io | sh -

# Set kubeconfig
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
sudo chmod 644 /etc/rancher/k3s/k3s.yaml

# Deploy JCacheX
kubectl apply -f example/distributed/k8s-resources.yaml

# Wait for pods
kubectl wait --for=condition=ready pod -l app=jcachex -n jcachex --timeout=300s

# Run example
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=false
```

### Option 2: Cloud Kubernetes

#### A. AWS EKS
```bash
# Install AWS CLI and eksctl
brew install aws-cli eksctl  # macOS

# Create cluster
eksctl create cluster --name jcachex-cluster --region us-west-2

# Deploy JCacheX
kubectl apply -f example/distributed/k8s-resources.yaml

# Run example
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=false
```

#### B. Google GKE
```bash
# Install gcloud CLI
brew install google-cloud-sdk  # macOS

# Create cluster
gcloud container clusters create jcachex-cluster --zone us-central1-a

# Get credentials
gcloud container clusters get-credentials jcachex-cluster --zone us-central1-a

# Deploy JCacheX
kubectl apply -f example/distributed/k8s-resources.yaml

# Run example
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=false
```

#### C. Azure AKS
```bash
# Install Azure CLI
brew install azure-cli  # macOS

# Create resource group
az group create --name jcachex-rg --location eastus

# Create cluster
az aks create --resource-group jcachex-rg --name jcachex-cluster --node-count 3

# Get credentials
az aks get-credentials --resource-group jcachex-rg --name jcachex-cluster

# Deploy JCacheX
kubectl apply -f example/distributed/k8s-resources.yaml

# Run example
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=false
```

## 🧪 Testing Different Scenarios

### 1. Basic Simulation Test
```bash
# Run simulation mode
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=true
```

### 2. Local Kubernetes Test
```bash
# Start minikube
minikube start

# Apply resources
kubectl apply -f example/distributed/k8s-resources.yaml

# Check deployment
kubectl get pods -n jcachex

# Run example
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=false
```

### 3. Scaling Test
```bash
# Scale up
kubectl scale deployment jcachex-cluster --replicas=5 -n jcachex

# Watch scaling
kubectl get pods -n jcachex -w

# Run example (it will detect new pods)
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=false
```

### 4. Pod Restart Test
```bash
# Delete a pod (triggers restart)
kubectl delete pod -l app=jcachex -n jcachex --timeout=0 --force

# Watch recovery
kubectl get pods -n jcachex -w

# Run example (it will detect the change)
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=false
```

### 5. Multiple Namespace Test
```bash
# Create staging namespace
kubectl create namespace jcachex-staging

# Deploy to staging
kubectl apply -f example/distributed/k8s-resources.yaml -n jcachex-staging

# Run example with staging namespace
java -cp example/distributed/build/classes/java/main io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample --simulation.mode=false --kubernetes.namespace=jcachex-staging
```

## 🔧 Configuration Options

### Environment Variables
```bash
# Set simulation mode
export SIMULATION_MODE=true

# Set Kubernetes namespace
export KUBERNETES_NAMESPACE=jcachex

# Set service name
export KUBERNETES_SERVICE_NAME=jcachex-cluster

# Set label selector
export KUBERNETES_LABEL_SELECTOR=app=jcachex,component=cache
```

### System Properties
```bash
# Run with custom configuration
java -Dsimulation.mode=false \
     -Dkubernetes.namespace=jcachex \
     -Dkubernetes.serviceName=jcachex-cluster \
     -Dkubernetes.labelSelector=app=jcachex,component=cache \
     -cp example/distributed/build/classes/java/main \
     io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample
```

### Application Configuration
```yaml
# application.yml
jcachex:
  distributed:
    clusterName: kubernetes-cluster
    replicationFactor: 2
    consistencyLevel: EVENTUAL
    nodeDiscovery:
      type: KUBERNETES
      discoveryIntervalSeconds: 30
      healthCheckIntervalSeconds: 10
      kubernetes:
        namespace: jcachex
        serviceName: jcachex-cluster
        labelSelector: app=jcachex,component=cache
        useServiceAccount: true
        kubeConfigPath: ~/.kube/config  # For local development

simulation:
  mode: true  # Set to false for real K8s
```

## 🐛 Troubleshooting

### Common Issues

#### 1. "No Kubernetes cluster found"
```bash
# Check kubectl configuration
kubectl cluster-info

# Check current context
kubectl config current-context

# List available contexts
kubectl config get-contexts

# Switch context if needed
kubectl config use-context your-context
```

#### 2. "Service account permissions denied"
```bash
# Check service account permissions
kubectl auth can-i get pods --as=system:serviceaccount:jcachex:jcachex-service-account -n jcachex

# Check role binding
kubectl get rolebinding jcachex-discovery-binding -n jcachex -o yaml

# Recreate RBAC resources
kubectl apply -f example/distributed/k8s-resources.yaml
```

#### 3. "Pods not ready"
```bash
# Check pod status
kubectl get pods -n jcachex

# Check pod logs
kubectl logs -f deployment/jcachex-cluster -n jcachex

# Describe pod for events
kubectl describe pod -l app=jcachex -n jcachex
```

#### 4. "Connection refused"
```bash
# Check service endpoints
kubectl get endpoints jcachex-cluster -n jcachex

# Check service
kubectl get service jcachex-cluster -n jcachex

# Port forward for testing
kubectl port-forward service/jcachex-cluster 8080:8080 -n jcachex
```

### Debug Commands

```bash
# View all resources
kubectl get all -n jcachex

# Check events
kubectl get events -n jcachex --sort-by=.metadata.creationTimestamp

# Check logs with timestamps
kubectl logs -f deployment/jcachex-cluster -n jcachex --timestamps=true

# Check resource usage
kubectl top pods -n jcachex

# Check network policies
kubectl get networkpolicies -n jcachex
```

### Enable Debug Logging

```bash
# Run with debug logging
java -Dlogging.level.io.github.dhruv1110.jcachex=DEBUG \
     -cp example/distributed/build/classes/java/main \
     io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample
```

## 📊 Monitoring

### Health Checks
```bash
# Check application health
kubectl port-forward service/jcachex-cluster 8080:8080 -n jcachex &
curl http://localhost:8080/actuator/health

# Check discovery health
curl http://localhost:8080/actuator/health/kubernetes-discovery
```

### Metrics
```bash
# View Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# View discovery metrics
curl http://localhost:8080/actuator/metrics/jcachex.discovery
```

### Logs
```bash
# View structured logs
kubectl logs -f deployment/jcachex-cluster -n jcachex | jq .

# Follow logs from all pods
kubectl logs -f -l app=jcachex -n jcachex --prefix=true
```

## 🔄 CI/CD Integration

### GitHub Actions Example
```yaml
# .github/workflows/k8s-test.yml
name: Kubernetes Test
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3

    - name: Setup Java
      uses: actions/setup-java@v3
      with:
        java-version: '11'

    - name: Build
      run: ./gradlew build

    - name: Test Simulation
      run: |
        java -cp example/distributed/build/classes/java/main \
             io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample \
             --simulation.mode=true

    - name: Setup Kind
      uses: helm/kind-action@v1.4.0
      with:
        cluster_name: jcachex-test

    - name: Test Real Kubernetes
      run: |
        kubectl apply -f example/distributed/k8s-resources.yaml
        kubectl wait --for=condition=ready pod -l app=jcachex -n jcachex --timeout=300s
        java -cp example/distributed/build/classes/java/main \
             io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample \
             --simulation.mode=false
```

## 🎯 Next Steps

### 1. Try Other Discovery Methods
- [Consul Discovery Example](ConsulDiscoveryExample.java)
- [Gossip Discovery Example](GossipDiscoveryExample.java)

### 2. Production Deployment
- Configure resource limits and requests
- Set up monitoring and alerting
- Implement backup and disaster recovery
- Configure network policies

### 3. Advanced Features
- Multi-cluster federation
- Cross-region replication
- Custom health checks
- Performance tuning

## 📚 Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [JCacheX Architecture Guide](../../docs/ARCHITECTURE.md)
- [Spring Boot Kubernetes Integration](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [Minikube Documentation](https://minikube.sigs.k8s.io/docs/)
- [Kind Documentation](https://kind.sigs.k8s.io/docs/)

## 🤝 Contributing

Found an issue or want to improve the example?

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

For questions, open an issue in the [GitHub repository](https://github.com/dhruv1110/JCacheX/issues).
