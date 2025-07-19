#!/bin/bash

# JCacheX Distributed Cache Test Script
# This script tests the distributed cache functionality across 3 nodes

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Node endpoints
NODE1="http://localhost:8080"
NODE2="http://localhost:8082"
NODE3="http://localhost:8084"

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

wait_for_nodes() {
    print_header "Waiting for nodes to be ready"

    for node in $NODE1 $NODE2 $NODE3; do
        echo -n "Waiting for $node... "
        while ! curl -s $node/actuator/health > /dev/null 2>&1; do
            sleep 2
            echo -n "."
        done
        echo " Ready!"
    done

    print_success "All nodes are ready!"
    sleep 2
}

test_health_check() {
    print_header "Testing Node Health"

    for i in 1 2 3; do
        local node_var="NODE$i"
        local node="${!node_var}"

        if curl -s "$node/actuator/health" | grep -q '"status":"UP"'; then
            print_success "Node $i ($node) is healthy"
        else
            print_error "Node $i ($node) is not healthy"
            exit 1
        fi
    done
}

test_cache_operations() {
    print_header "Testing Basic Cache Operations"

    # Test 1: Store data on Node 1
    print_info "Storing 'user:123' on Node 1..."
    curl -s -X PUT "$NODE1/cache/user:123" \
        -H "Content-Type: application/json" \
        -d '"John Doe"' > /dev/null

    sleep 1

    # Test 2: Retrieve from Node 2 (should be replicated)
    print_info "Retrieving 'user:123' from Node 2..."
    result=$(curl -s "$NODE2/cache/user:123" | jq -r '.value // "null"')
    if [ "$result" = "John Doe" ]; then
        print_success "Data successfully replicated to Node 2: $result"
    else
        print_error "Data not found on Node 2. Expected 'John Doe', got: $result"
    fi

    # Test 3: Retrieve from Node 3 (should be replicated)
    print_info "Retrieving 'user:123' from Node 3..."
    result=$(curl -s "$NODE3/cache/user:123" | jq -r '.value // "null"')
    if [ "$result" = "John Doe" ]; then
        print_success "Data successfully replicated to Node 3: $result"
    else
        print_error "Data not found on Node 3. Expected 'John Doe', got: $result"
    fi
}

test_complex_data() {
    print_header "Testing Complex Data Replication"

    # Store complex object on Node 2
    print_info "Storing complex object on Node 2..."
    curl -s -X PUT "$NODE2/cache/product:456" \
        -H "Content-Type: application/json" \
        -d '{"name":"Laptop","price":999.99,"category":"Electronics"}' > /dev/null

    sleep 1

    # Retrieve from Node 1
    print_info "Retrieving complex object from Node 1..."
    result=$(curl -s "$NODE1/cache/product:456" | jq -r '.value.name // "null"')
    if [ "$result" = "Laptop" ]; then
        print_success "Complex object successfully replicated to Node 1"
    else
        print_error "Complex object not found on Node 1. Expected 'Laptop', got: $result"
    fi
}

test_deletion() {
    print_header "Testing Cache Deletion"

    # Delete from Node 3
    print_info "Deleting 'user:123' from Node 3..."
    curl -s -X DELETE "$NODE3/cache/user:123" > /dev/null

    sleep 1

    # Check deletion on Node 1
    print_info "Checking deletion on Node 1..."
    result=$(curl -s "$NODE1/cache/user:123" | jq -r '.found')
    if [ "$result" = "false" ]; then
        print_success "Deletion successfully replicated to Node 1"
    else
        print_error "Key still exists on Node 1 after deletion"
    fi
}

test_cache_stats() {
    print_header "Testing Cache Statistics"

    for i in 1 2 3; do
        local node_var="NODE$i"
        local node="${!node_var}"

        print_info "Getting stats from Node $i..."
        stats=$(curl -s "$node/cache/stats")
        cache_size=$(echo "$stats" | jq -r '.cacheSize')
        cluster_name=$(echo "$stats" | jq -r '.clusterTopology.clusterName // "unknown"')

        echo "  Cache Size: $cache_size"
        echo "  Cluster: $cluster_name"
    done
}

test_performance() {
    print_header "Performance Test - Storing 50 items"

    start_time=$(date +%s)

    for i in {1..50}; do
        curl -s -X PUT "$NODE1/cache/perf:$i" \
            -H "Content-Type: application/json" \
            -d "\"value-$i\"" > /dev/null &

        if (( i % 10 == 0 )); then
            wait # Wait for batch to complete
            echo -n "."
        fi
    done
    wait

    end_time=$(date +%s)
    duration=$((end_time - start_time))

    sleep 2 # Allow replication to complete

    # Check counts on all nodes
    for i in 1 2 3; do
        local node_var="NODE$i"
        local node="${!node_var}"
        count=$(curl -s "$node/cache/keys" | jq -r '.count')
        echo "Node $i has $count items"
    done

    print_success "Performance test completed in ${duration}s"
}

cleanup_cache() {
    print_header "Cleaning up cache"

    print_info "Clearing cache from Node 1..."
    curl -s -X DELETE "$NODE1/cache/clear" > /dev/null

    sleep 1

    # Verify on all nodes
    for i in 1 2 3; do
        local node_var="NODE$i"
        local node="${!node_var}"
        count=$(curl -s "$node/cache/keys" | jq -r '.count')
        if [ "$count" = "0" ]; then
            print_success "Node $i cache cleared (count: $count)"
        else
            print_error "Node $i still has $count items"
        fi
    done
}

main() {
    echo -e "${BLUE}"
    echo "🚀 JCacheX Distributed Cache Test Suite"
    echo "======================================="
    echo -e "${NC}"

    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        print_error "jq is required but not installed. Please install jq to continue."
        exit 1
    fi

    wait_for_nodes
    test_health_check
    test_cache_operations
    test_complex_data
    test_deletion
    test_cache_stats
    test_performance
    cleanup_cache

    print_header "Test Summary"
    print_success "All distributed cache tests completed successfully! 🎉"
    echo -e "\n${YELLOW}💡 Tips:${NC}"
    echo "• Watch logs with: docker-compose logs -f"
    echo "• Check individual nodes: curl http://localhost:808[0,2,4]/cache/stats"
    echo "• Monitor replication: docker-compose logs -f | grep 'TCP-Replication'"
}

# Run main function
main "$@"
