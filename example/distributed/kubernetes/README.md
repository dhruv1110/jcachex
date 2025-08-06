# JCacheX Kubernetes Example

A minimal Spring Boot application demonstrating JCacheX usage in a Kubernetes environment.

## Prerequisites

- Java 11 or higher
- Docker
- Kubernetes cluster (kind, minikube, or any local cluster)
- kubectl configured to access your cluster

## Quick Start

### 1. Build and Deploy

```bash
# Make the script executable
chmod +x build-and-deploy.sh

# Build and deploy to Kubernetes
./build-and-deploy.sh
```

### 2. Test the Application

```bash
# Health check
curl http://localhost:30080/api/health

# Put data in cache
curl -X POST http://localhost:30080/api/cache/mykey \
  -H 'Content-Type: application/json' \
  -d '{"value":"Hello Kubernetes"}'

# Get data from cache
curl http://localhost:30080/api/cache/mykey

# Check cache statistics
curl http://localhost:30080/api/cache/stats

# Delete from cache
curl -X DELETE http://localhost:30080/api/cache/mykey
```

## Manual Steps

### Build Application

```bash
# From project root
./gradlew :example:distributed:kubernetes:bootJar
```

### Build Docker Image

```bash
cd example/distributed/kubernetes
docker build -t jcachex-kubernetes-example:latest .

# For kind clusters
kind load docker-image jcachex-kubernetes-example:latest
```

### Deploy to Kubernetes

```bash
kubectl apply -f k8s/deployment.yaml
```

### Check Deployment

```bash
kubectl get pods -l app=jcachex-kubernetes-example
kubectl get services -l app=jcachex-kubernetes-example
```

## API Endpoints

- `GET /api/health` - Health check
- `POST /api/cache/{key}` - Store value in cache
- `GET /api/cache/{key}` - Retrieve value from cache
- `GET /api/cache/stats` - Get cache statistics
- `DELETE /api/cache/{key}` - Remove key from cache

## Architecture

This example demonstrates:
- JCacheX integration with Spring Boot
- Containerized deployment with Docker
- Kubernetes deployment with health checks
- Basic cache operations via REST API
- Cache statistics monitoring

## Configuration

The application is configured with:
- 2 replicas for high availability
- Resource limits and requests
- Health checks (liveness and readiness probes)
- NodePort service for easy local access

## Cleanup

```bash
kubectl delete -f k8s/deployment.yaml
```
