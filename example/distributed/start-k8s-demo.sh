#!/bin/bash

# JCacheX Kubernetes Discovery Demo Startup Script
# This script sets up a complete Kubernetes environment with k3s and JCacheX

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to wait for container to be healthy
wait_for_container() {
    local container_name=$1
    local max_wait=120
    local wait_time=0

    print_info "Waiting for container '$container_name' to be ready..."

    while [ $wait_time -lt $max_wait ]; do
        if docker ps --filter "name=$container_name" --filter "status=running" --format "{{.Names}}" | grep -q "$container_name"; then
            print_success "Container '$container_name' is ready!"
            return 0
        fi
        sleep 2
        wait_time=$((wait_time + 2))
        echo -n "."
    done

    print_error "Container '$container_name' failed to start within ${max_wait}s"
    return 1
}

# Function to cleanup
cleanup() {
    print_info "Cleaning up containers and volumes..."
    docker-compose down -v --remove-orphans 2>/dev/null || true
    docker volume prune -f 2>/dev/null || true
    print_success "Cleanup completed!"
}

# Function to check system requirements
check_requirements() {
    print_info "Checking system requirements..."

    if ! command_exists docker; then
        print_error "Docker is not installed. Please install Docker Desktop."
        exit 1
    fi

    if ! command_exists docker-compose; then
        print_error "Docker Compose is not installed. Please install Docker Compose."
        exit 1
    fi

    # Check if Docker is running
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker Desktop."
        exit 1
    fi

    print_success "System requirements check passed!"
}

# Main function
main() {
    echo "🚀 JCacheX Kubernetes Discovery Demo"
    echo "===================================="

    # Parse command line arguments
    SKIP_BUILD=false
    CLEANUP_ONLY=false
    FOLLOW_LOGS=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-build)
                SKIP_BUILD=true
                shift
                ;;
            --cleanup)
                CLEANUP_ONLY=true
                shift
                ;;
            --logs)
                FOLLOW_LOGS=true
                shift
                ;;
            --help)
                echo "Usage: $0 [OPTIONS]"
                echo "Options:"
                echo "  --skip-build    Skip building the Docker image"
                echo "  --cleanup       Only cleanup containers and volumes"
                echo "  --logs          Follow container logs after startup"
                echo "  --help          Show this help message"
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                exit 1
                ;;
        esac
    done

    # If cleanup only, do cleanup and exit
    if [ "$CLEANUP_ONLY" = true ]; then
        cleanup
        exit 0
    fi

    # Check requirements
    check_requirements

    # Create directories
    mkdir -p k3s-config

    # Cleanup first
    cleanup

    print_info "Starting JCacheX Kubernetes Discovery Demo..."

    # Start the Docker Compose setup
    if [ "$SKIP_BUILD" = true ]; then
        print_info "Skipping build step..."
        docker-compose up -d k3s-server
    else
        print_info "Building and starting containers..."
        print_info "This may take several minutes for the first build..."
        if ! docker-compose up -d --build; then
            print_error "Build failed. Try running with --no-cache:"
            print_error "docker-compose build --no-cache jcachex-app-builder"
            exit 1
        fi
    fi

    # Wait for k3s to be ready
    wait_for_container "k3s-server"

    # Wait a bit more for k3s to fully initialize
    print_info "Waiting for k3s to fully initialize..."
    sleep 10

        # Show k3s cluster info
    print_info "Getting k3s cluster information..."
    docker exec k3s-server kubectl get nodes -o wide || true

    # Wait for deployment to be ready
    print_info "Waiting for JCacheX deployment to be ready..."
    sleep 5

    # Show deployment status
    print_info "Checking deployment status..."
    docker exec k3s-server kubectl get pods -n jcachex-demo -o wide || true
    docker exec k3s-server kubectl get services -n jcachex-demo || true

    # Show logs from the client
    print_info "JCacheX client logs:"
    docker-compose logs jcachex-client

        print_success "JCacheX Kubernetes Discovery Demo is running!"

    echo ""
    echo "🎯 Quick Commands:"
    echo "  📊 View cluster status:    docker exec k3s-server kubectl get pods -n jcachex-demo"
    echo "  📋 View services:          docker exec k3s-server kubectl get services -n jcachex-demo"
    echo "  📄 View logs:              docker-compose logs jcachex-client"
    echo "  🔄 Scale deployment:       docker exec k3s-server kubectl scale deployment jcachex-app --replicas=5 -n jcachex-demo"
    echo "  🧹 Cleanup:               $0 --cleanup"
    echo ""
    echo "🎉 Setup completed successfully! The demo is ready to use."
    echo "💡 Access the services at: http://localhost:30080"
    echo "📖 See DOCKER_COMPOSE_SETUP.md for detailed documentation"
    echo ""

    # Follow logs if requested
    if [ "$FOLLOW_LOGS" = true ]; then
        print_info "Following container logs (Press Ctrl+C to exit)..."
        docker-compose logs -f
    fi
}

# Handle interrupts
trap cleanup INT TERM

# Run main function
main "$@"
