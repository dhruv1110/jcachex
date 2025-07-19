#!/bin/bash

# JCacheX Distributed Cache Cluster Startup Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}\n"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}📝 $1${NC}"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to check if ports are available
check_ports() {
    local ports=(8080 8081 8082 8083 8084 8085)
    local busy_ports=()

    for port in "${ports[@]}"; do
        if netstat -ln 2>/dev/null | grep -q ":$port " || lsof -i :$port >/dev/null 2>&1; then
            busy_ports+=($port)
        fi
    done

    if [ ${#busy_ports[@]} -gt 0 ]; then
        print_error "The following ports are already in use: ${busy_ports[*]}"
        echo "Please stop any services using these ports and try again."
        exit 1
    fi
    print_success "All required ports are available"
}

# Function to build and start the cluster
start_cluster() {
    print_header "Building and Starting JCacheX Distributed Cache Cluster"

    print_info "Building Docker images... (this may take a few minutes)"

    # Try standard build first
    if docker-compose build --no-cache; then
        print_success "Docker images built successfully"
    else
        print_error "Standard build failed. Trying with platform specification..."

        # Try with platform specification for Apple Silicon compatibility
        if docker-compose build --no-cache --platform linux/amd64; then
            print_success "Docker images built successfully with platform specification"
        else
            print_error "Failed to build Docker images"
            echo "Try running: docker system prune -a && docker-compose build --no-cache"
            exit 1
        fi
    fi

    print_info "Starting the 3-node cluster..."
    if docker-compose up -d; then
        print_success "Cluster started successfully"
    else
        print_error "Failed to start cluster"
        echo "Check logs with: docker-compose logs"
        exit 1
    fi
}

# Function to wait for all nodes to be healthy
wait_for_cluster() {
    print_header "Waiting for Cluster to be Ready"

    local nodes=("http://localhost:8080" "http://localhost:8082" "http://localhost:8084")
    local max_wait=180  # 3 minutes
    local wait_time=0

    for node in "${nodes[@]}"; do
        print_info "Waiting for $node to be healthy..."

        while [ $wait_time -lt $max_wait ]; do
            if curl -s "$node/actuator/health" | grep -q '"status":"UP"' 2>/dev/null; then
                print_success "$node is healthy"
                break
            fi

            sleep 5
            wait_time=$((wait_time + 5))
            echo -n "."
        done

        if [ $wait_time -ge $max_wait ]; then
            print_error "$node failed to become healthy within $max_wait seconds"
            echo "Check logs with: docker-compose logs"
            exit 1
        fi
    done

    print_success "All nodes are healthy and ready!"
}

# Function to show cluster status
show_cluster_status() {
    print_header "Cluster Status"

    echo "Node 1: http://localhost:8080 (Cache: 8081)"
    echo "Node 2: http://localhost:8082 (Cache: 8083)"
    echo "Node 3: http://localhost:8084 (Cache: 8085)"
    echo

    print_info "Testing cluster connectivity..."
    for port in 8080 8082 8084; do
        if curl -s "http://localhost:$port/cache/stats" > /dev/null; then
            print_success "Node on port $port is responding"
        else
            print_error "Node on port $port is not responding"
        fi
    done
}

# Function to show usage instructions
show_usage() {
    print_header "Quick Test Commands"

    echo "1. Store data on Node 1:"
    echo "   curl -X PUT http://localhost:8080/cache/test:123 -H 'Content-Type: application/json' -d '\"Hello World\"'"
    echo
    echo "2. Retrieve from Node 2 (should be replicated):"
    echo "   curl http://localhost:8082/cache/test:123"
    echo
    echo "3. Retrieve from Node 3 (should be replicated):"
    echo "   curl http://localhost:8084/cache/test:123"
    echo
    echo "4. Run comprehensive tests:"
    echo "   ./test-distributed-cache.sh"
    echo
    echo "5. Watch replication logs:"
    echo "   docker-compose logs -f | grep 'TCP-Replication\\|Replication'"
    echo
    echo "6. Stop the cluster:"
    echo "   docker-compose down"
}

# Main function
main() {
    echo -e "${BLUE}"
    echo "🚀 JCacheX Distributed Cache Cluster"
    echo "===================================="
    echo -e "${NC}"

    check_docker
    check_ports
    start_cluster
    wait_for_cluster
    show_cluster_status
    show_usage

    print_header "Cluster Ready!"
    print_success "JCacheX distributed cache cluster is now running! 🎉"
    echo -e "\n${YELLOW}💡 Tip: Use 'docker-compose logs -f' to watch real-time logs${NC}"
}

# Handle Ctrl+C
trap 'echo -e "\n${RED}❌ Startup interrupted${NC}"; exit 1' INT

# Run main function
main "$@"
