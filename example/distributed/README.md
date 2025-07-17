# JCacheX Distributed Cache Examples

This directory contains comprehensive examples demonstrating JCacheX's distributed caching capabilities, including auto-discovery mechanisms for various environments.

## Overview

JCacheX provides seamless scaling from local to distributed caching with multiple discovery strategies:

- **Kubernetes Discovery**: Service-based discovery for containerized environments
- **Consul Discovery**: HashiCorp Consul integration for microservices
- **Gossip Protocol**: Peer-to-peer discovery without central coordination
- **Static Configuration**: Manual node configuration

## Discovery Examples

### 1. Kubernetes Discovery Example

**File**: `KubernetesDiscoveryExample.java`

Demonstrates automatic node discovery in Kubernetes environments using service accounts and label selectors.

```bash
# Prerequisites
kubectl apply -f k8s-resources.yaml

# Run the example
java -jar kubernetes-discovery-example.jar
```

**Key Features**:
- Service account-based authentication
- Label selector filtering
- Pod endpoint discovery
- Health check integration
- Auto-scaling support

### 2. Consul Discovery Example

**File**: `ConsulDiscoveryExample.java`

Shows integration with HashiCorp Consul for service discovery in microservices architectures.

```bash
# Start Consul
docker run -d -p 8500:8500 consul:latest agent -dev -client=0.0.0.0

# Run the example
java -jar consul-discovery-example.jar
```

**Key Features**:
- Service registration and discovery
- Health check integration
- Multi-datacenter support
- ACL token support
- TTL-based health updates

### 3. Gossip Protocol Example

**File**: `GossipDiscoveryExample.java`

Demonstrates peer-to-peer discovery using gossip protocol for environments without central service registries.

```bash
# Start multiple nodes
java -jar gossip-discovery-example.jar --server.port=8080 --node.id=node1 &
java -jar gossip-discovery-example.jar --server.port=8081 --node.id=node2 &
java -jar gossip-discovery-example.jar --server.port=8082 --node.id=node3 &
```

**Key Features**:
- Decentralized discovery
- Network partition tolerance
- Self-healing clusters
- Seed node bootstrapping
- Eventual consistency

## Configuration Reference

### Kubernetes Discovery

```yaml
jcachex:
  distributed:
    nodeDiscovery:
      type: KUBERNETES
      discoveryIntervalSeconds: 30
      healthCheckIntervalSeconds: 10
      kubernetes:
        namespace: jcachex
        serviceName: jcachex-cluster
        labelSelector: app=jcachex,component=cache
        useServiceAccount: true
        kubeConfigPath: /path/to/kubeconfig  # Optional
```

### Consul Discovery

```yaml
jcachex:
  distributed:
    nodeDiscovery:
      type: CONSUL
      discoveryIntervalSeconds: 30
      healthCheckIntervalSeconds: 10
      consul:
        consulHost: localhost:8500
        serviceName: jcachex-cluster
        datacenter: dc1
        enableAcl: false
        token: your-consul-token  # Optional
```

### Gossip Protocol

```yaml
jcachex:
  distributed:
    nodeDiscovery:
      type: GOSSIP
      discoveryIntervalSeconds: 30
      healthCheckIntervalSeconds: 10
      gossip:
        seedNodes:
          - node1:8080
          - node2:8080
          - node3:8080
        gossipIntervalSeconds: 5
        gossipFanout: 3
        nodeTimeoutSeconds: 60
```

## Deployment Scenarios

### Docker Compose

```yaml
version: '3.8'
services:
  consul:
    image: consul:latest
    ports:
      - "8500:8500"
    command: agent -dev -client=0.0.0.0

  jcachex-node1:
    image: jcachex:latest
    depends_on:
      - consul
    environment:
      - CONSUL_HOST=consul:8500
      - NODE_ID=node1
    ports:
      - "8080:8080"

  jcachex-node2:
    image: jcachex:latest
    depends_on:
      - consul
    environment:
      - CONSUL_HOST=consul:8500
      - NODE_ID=node2
    ports:
      - "8081:8080"
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jcachex-cluster
  namespace: jcachex
spec:
  replicas: 3
  selector:
    matchLabels:
      app: jcachex
      component: cache
  template:
    metadata:
      labels:
        app: jcachex
        component: cache
    spec:
      serviceAccountName: jcachex-service-account
      containers:
      - name: jcachex
        image: jcachex:latest
        ports:
        - containerPort: 8080
          name: cache
        env:
        - name: DISCOVERY_TYPE
          value: "KUBERNETES"
        - name: KUBERNETES_NAMESPACE
          value: "jcachex"
        - name: SERVICE_NAME
          value: "jcachex-cluster"
```

## Running the Examples

### Prerequisites

1. **Java 11+**: Required for running the examples
2. **Maven/Gradle**: For building the examples
3. **Docker**: For running infrastructure components
4. **Kubernetes**: For Kubernetes discovery example

### Building

```bash
# Build all examples
./gradlew :example:distributed:build

# Build specific example
./gradlew :example:distributed:compileJava
```

### Running

#### Docker Compose Setup (Recommended for Testing)

For easy testing with a complete Kubernetes environment:

```bash
# One-command setup with k3s
./start-k8s-demo.sh

# With log following
./start-k8s-demo.sh --logs

# Cleanup
./start-k8s-demo.sh --cleanup
```

This setup includes:
- Complete k3s Kubernetes cluster
- JCacheX applications with real discovery
- Automatic resource deployment
- Health checks and monitoring

See `DOCKER_COMPOSE_SETUP.md` for detailed documentation.

#### Manual Execution

```bash
# Kubernetes example
java -cp target/classes io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample

# Consul example
java -cp target/classes io.github.dhruv1110.jcachex.example.distributed.ConsulDiscoveryExample

# Gossip example
java -cp target/classes io.github.dhruv1110.jcachex.example.distributed.GossipDiscoveryExample
```

## Architecture Overview

### Discovery Flow

1. **Initialization**: Cache factory creates discovery service based on configuration
2. **Bootstrap**: Initial nodes are discovered through configured mechanism
3. **Registration**: Local node registers with discovery service
4. **Monitoring**: Continuous health checks and node status updates
5. **Adaptation**: Dynamic cluster membership changes handled automatically

### Integration Points

- **Spring Boot**: Auto-configuration and property binding
- **Kubernetes**: Service account authentication and pod discovery
- **Consul**: Service registration and health checks
- **Gossip**: Peer-to-peer message exchange

## Monitoring and Observability

### Health Checks

Each discovery mechanism provides health check endpoints:

```bash
# Kubernetes
curl http://localhost:8080/actuator/health/kubernetes-discovery

# Consul
curl http://localhost:8080/actuator/health/consul-discovery

# Gossip
curl http://localhost:8080/actuator/health/gossip-discovery
```

### Metrics

Discovery metrics are exposed through:

```bash
# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# JMX beans
jconsole localhost:8080
```

## Troubleshooting

### Common Issues

1. **Service Account Permissions**: Ensure Kubernetes service account has proper RBAC
2. **Network Connectivity**: Verify nodes can communicate on configured ports
3. **Consul Connectivity**: Check Consul agent availability and ACL tokens
4. **Gossip Seed Nodes**: Ensure at least one seed node is reachable

### Debug Logging

```yaml
logging:
  level:
    io.github.dhruv1110.jcachex.distributed.discovery: DEBUG
```

## Best Practices

1. **Production Readiness**: Use appropriate discovery mechanism for your environment
2. **Health Checks**: Configure meaningful health check intervals
3. **Security**: Use TLS and authentication where available
4. **Monitoring**: Set up alerting for cluster health
5. **Scaling**: Design for dynamic cluster membership changes

## Contributing

To add new discovery examples:

1. Create a new example class in the `distributed` package
2. Follow the existing pattern with comprehensive documentation
3. Include configuration examples and deployment scenarios
4. Add integration tests
5. Update this README with your example

## Related Documentation

- [JCacheX Core Documentation](../../docs/ARCHITECTURE.md)
- [Spring Integration Guide](../../jcachex-spring/README.md)
- [Kubernetes Deployment Guide](../../docs/KUBERNETES.md)
- [Consul Integration Guide](../../docs/CONSUL.md)
