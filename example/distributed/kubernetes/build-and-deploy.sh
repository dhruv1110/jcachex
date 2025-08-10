#!/bin/bash

set -e

echo "Building JCacheX Kubernetes Example..."

# Navigate to project root
cd "$(dirname "$0")/../../.."

# Build the application
echo "Building the application..."
./gradlew :example:distributed:kubernetes:bootJar

# Build Docker image
echo "Building Docker image..."
cd example/distributed/kubernetes
docker build -t jcachex-kubernetes-example:latest .

# Load image into kind cluster (if using kind)
if command -v kind &> /dev/null; then
    echo "Loading image into kind cluster..."
    kind load docker-image jcachex-kubernetes-example:latest
fi

# Deploy to Kubernetes
echo "Deploying to Kubernetes..."
kubectl apply -f k8s/deployment.yaml

# Wait for deployment
echo "Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/jcachex-kubernetes-example

# Show status
echo "Deployment status:"
kubectl get pods -l app=jcachex-kubernetes-example
kubectl get services -l app=jcachex-kubernetes-example

echo ""
echo "Application deployed successfully!"
echo "Access the application at: http://localhost:30080/api/health"
echo "To test the cache API:"
echo "  curl -X POST http://localhost:30080/api/cache/test -H 'Content-Type: application/json' -d '{\"value\":\"hello\"}'"
echo "  curl http://localhost:30080/api/cache/test"
echo "  curl http://localhost:30080/api/cache/stats"
