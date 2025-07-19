# JCacheX Distributed Cache Static Node Example

This example demonstrates JCacheX distributed cache capabilities using a 3-node cluster with static node discovery and TCP-based replication.

## Features

- 🚀 **TCP-Based Replication**: Efficient socket communication between nodes
- 🔍 **Static Node Discovery**: Pre-configured cluster topology
- 🌐 **REST API**: HTTP endpoints for cache operations
- 📊 **Real-time Monitoring**: Detailed logging of cache replication events
- 🐳 **Docker Support**: Complete containerized setup
- 🔄 **Automatic Replication**: Changes propagate across all cluster nodes

## Architecture

```
┌─────────────┐              ┌─────────────┐              ┌─────────────┐
│    Node 1   │◄────────────►│    Node 2   │◄────────────►│    Node 3   │
│  HTTP:8080  │   TCP:8081   │  HTTP:8082  │   TCP:8083   │  HTTP:8084  │
│hostname:    │  hostname:   │hostname:    │  hostname:   │hostname:    │
│  node1      │    node2     │  node2      │    node3     │  node3      │
│  Cache:8081 │              │  Cache:8083 │              │  Cache:8085 │
└─────────────┘              └─────────────┘              └─────────────┘
       ▲                              ▲                              ▲
       │                              │                              │
       └──────────────────────────────┼──────────────────────────────┘
                                      │
                              ┌─────────────┐
                              │   Docker    │
                              │   Network   │
                              │ jcachex-    │
                              │  cluster    │
                              └─────────────┘

Inter-node communication uses container hostnames (node1, node2, node3)
External access via localhost:808[0,2,4] from host machine
```

## Quick Start

### 1. Prerequisites

- Docker and Docker Compose
- Java 17+ (for local development)
- curl (for testing)

**Key Improvements:**
- ✅ **Simplified Docker Setup**: Uses standard Ubuntu + OpenJDK 17 (no complex multi-stage builds)
- ✅ **Local JAR Build**: Builds JAR locally first, avoiding Gradle issues inside Docker
- ✅ **Proper Docker Networking**: Inter-container communication via hostnames (node1, node2, node3)
- ✅ **Dynamic Port Configuration**: Each node uses different ports with proper health checks
- ✅ **Cross-Platform Compatible**: Works on Intel, Apple Silicon, and Linux systems

### 2. Build and Run

**Option A: All-in-One Script (Recommended)**
```bash
# Navigate to the example directory
cd example/distributed/staticnode

# Build JAR locally and start cluster
./build-and-run.sh

# Or with cleanup of previous containers
./build-and-run.sh --clean
```

**Option B: Manual Steps**
```bash
# 1. First build the JAR from project root
cd ../../..
./gradlew :example:distributed:staticnode:bootJar

# 2. Copy JAR to Docker context
mkdir -p example/distributed/staticnode/build/libs/
cp example/distributed/staticnode/build/libs/distributed-cache-example.jar \
   example/distributed/staticnode/build/libs/

# 3. Navigate back and start containers
cd example/distributed/staticnode
docker-compose up --build
```

### 3. Verify Cluster Status

```bash
# Check all nodes are healthy
curl http://localhost:8080/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8084/actuator/health

# Check cluster topology
curl http://localhost:8080/cache/stats
```

## Testing Distributed Cache

### Basic Cache Operations

```bash
# 1. Store data on Node 1
curl -X PUT http://localhost:8080/cache/user:123 \
  -H "Content-Type: application/json" \
  -d '"John Doe"'

# 2. Retrieve from Node 2 (should be replicated)
curl http://localhost:8082/cache/user:123

# 3. Retrieve from Node 3 (should be replicated)
curl http://localhost:8084/cache/user:123

# 4. Store complex data on Node 2
curl -X PUT http://localhost:8082/cache/product:456 \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":999.99,"category":"Electronics"}'

# 5. Retrieve from Node 1 (should be replicated)
curl http://localhost:8080/cache/product:456
```

### Advanced Operations

```bash
# Get all keys from any node
curl http://localhost:8080/cache/keys

# Delete a key from Node 3
curl -X DELETE http://localhost:8084/cache/user:123

# Verify deletion on Node 1
curl http://localhost:8080/cache/user:123

# Clear entire cache from Node 2
curl -X DELETE http://localhost:8082/cache/clear

# Check cache stats and cluster information
curl http://localhost:8080/cache/stats | jq
```

## Monitoring Replication Events

Watch the Docker logs to see real-time cache replication:

```bash
# Watch all nodes
docker-compose logs -f

# Watch specific node
docker-compose logs -f node1
docker-compose logs -f node2
docker-compose logs -f node3

# Filter replication events
docker-compose logs -f | grep "TCP-Replication\|Replication"
```

### Expected Log Output

When you store data on one node, you should see logs like:

**Node 1 (where PUT request was made):**
```
📝 [node1:8080] PUT request for key 'user:123' with value: John Doe
✅ [node1:8080] Successfully stored key 'user:123' -> 'John Doe'
🔄 [node1:8080] Cache put operation will replicate to cluster nodes
🔄 [TCP-Replication] Synchronous replication of key: user:123 to 2 nodes
📡 [TCP-Replication] PUT sent to node2:8083 (latency: 15ms)
📡 [TCP-Replication] PUT sent to node3:8085 (latency: 12ms)
```

**Node 2 & 3 (receiving replication):**
```
✅ [TCP-Server] Processed replication: PUT for key: user:123
📥 [Replication] Applied PUT: user:123
```

## REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/cache/{key}` | Retrieve value for key |
| `PUT` | `/cache/{key}` | Store value for key |
| `DELETE` | `/cache/{key}` | Delete key |
| `DELETE` | `/cache/clear` | Clear entire cache |
| `GET` | `/cache/keys` | Get all cache keys |
| `GET` | `/cache/stats` | Get cache statistics and cluster info |
| `GET` | `/actuator/health` | Health check endpoint |

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `NODE_NAME` | `node1` | Node identifier |
| `SERVER_PORT` | `8080` | HTTP API port |
| `CACHE_REPLICATION_PORT` | `8081` | TCP replication port |
| `CACHE_CLUSTER_NAME` | `static-cluster` | Cluster name |
| `CACHE_NODES` | `node1:8081,node2:8083,node3:8085` | All node addresses |
| `CACHE_REPLICATION_FACTOR` | `2` | Number of replicas |

### Custom Configuration

Edit `docker-compose.yml` to customize:

```yaml
environment:
  - CACHE_REPLICATION_FACTOR=3  # Replicate to all nodes
  - CACHE_CLUSTER_NAME=my-cluster
  - JAVA_OPTS=-Xmx1g -Xms512m
```

## Troubleshooting

### Common Issues

1. **JAR build fails:**
   ```bash
   # Clean and rebuild from project root
   cd ../../..
   ./gradlew clean
   ./gradlew :example:distributed:staticnode:bootJar

   # Or use the automated script with cleanup
   cd example/distributed/staticnode
   ./build-and-run.sh --clean
   ```

2. **Docker networking issues:**
   ```bash
   # Check if containers can communicate
   docker exec jcachex-node1 nc -zv node2 8083
   docker exec jcachex-node1 nc -zv node3 8085

   # Check container hostnames resolution
   docker exec jcachex-node1 nslookup node2
   docker exec jcachex-node1 nslookup node3
   ```

3. **Port conflicts:**
   ```bash
   # The build-and-run.sh script automatically checks ports
   # Or check manually:
   netstat -ln | grep :808
   lsof -i :8080-8085
   ```

4. **Container startup issues:**
   ```bash
   # Check container logs
   docker-compose logs node1
   docker-compose logs node2
   docker-compose logs node3

   # Check health status
   docker-compose ps
   ```

5. **JAR not found in Docker:**
   ```bash
   # Ensure JAR was built and copied correctly
   ls -la build/libs/

   # Rebuild everything
   ./build-and-run.sh --clean
   ```

### Logs and Debugging

```bash
# Detailed container logs
docker-compose logs --details node1

# Follow specific service logs
docker-compose logs -f --tail=100 node2

# Container resource usage
docker stats jcachex-node1 jcachex-node2 jcachex-node3
```

## Stopping the Cluster

```bash
# Stop all nodes
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Stop and remove images
docker-compose down --rmi all
```

## Development

### Running Locally

```bash
# Build the project
./gradlew :example:distributed:staticnode:bootJar

# Run with different profiles
java -jar build/libs/distributed-cache-example.jar --spring.profiles.active=node1
java -jar build/libs/distributed-cache-example.jar --spring.profiles.active=node2
java -jar build/libs/distributed-cache-example.jar --spring.profiles.active=node3
```

### Testing with Different Consistency Levels

The example uses `EVENTUAL` consistency by default. You can modify the configuration to test different consistency levels by editing `CacheConfiguration.java`.

## Performance Testing

```bash
# Simple load test with curl
for i in {1..100}; do
  curl -X PUT http://localhost:8080/cache/test:$i \
    -H "Content-Type: application/json" \
    -d "\"value-$i\"" &
done
wait

# Check replication across nodes
curl http://localhost:8080/cache/keys | jq '.count'
curl http://localhost:8082/cache/keys | jq '.count'
curl http://localhost:8084/cache/keys | jq '.count'
```

## Key Technical Details

### Docker Networking
- **Internal Communication**: Containers use hostnames (`node1`, `node2`, `node3`) for cache replication
- **External Access**: Host machine accesses via `localhost:808[0,2,4]` for HTTP APIs
- **Port Mapping**: Each node exposes both HTTP API and TCP replication ports

### Configuration
- **Node 1**: HTTP `8080` ↔ Cache TCP `node1:8081`
- **Node 2**: HTTP `8082` ↔ Cache TCP `node2:8083`
- **Node 3**: HTTP `8084` ↔ Cache TCP `node3:8085`

### Build Process
1. Local Gradle build generates optimized JAR
2. Simple Ubuntu + OpenJDK 17 Docker image
3. Pre-built JAR copied into lightweight container
4. Fast startup with proper health checks

## Next Steps

- Try different consistency levels (modify `CacheConfiguration.java`)
- Test with larger datasets and measure replication performance
- Explore different eviction strategies
- Monitor TCP replication latency and throughput
