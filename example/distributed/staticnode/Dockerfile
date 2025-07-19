# Simple Dockerfile for JCacheX Distributed Cache Example
# Build the JAR locally first, then copy into container

FROM ubuntu:22.04

LABEL maintainer="JCacheX Team"
LABEL description="JCacheX Distributed Cache Static Node Example"

# Install Java 17 and necessary packages
RUN apt-get update && apt-get install -y \
    openjdk-17-jre-headless \
    curl \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r jcachex && useradd -r -g jcachex jcachex

# Create app directory
RUN mkdir -p /app && chown jcachex:jcachex /app

WORKDIR /app

# Copy the pre-built jar file
COPY build/libs/distributed-cache-example.jar app.jar

# Change ownership
RUN chown jcachex:jcachex app.jar

# Switch to non-root user
USER jcachex

# Expose ports dynamically based on environment variables
# Default ports exposed, but actual ports will be set via environment
EXPOSE ${SERVER_PORT:-8080}
EXPOSE ${CACHE_REPLICATION_PORT:-8081}

# Dynamic health check that uses the actual server port
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
