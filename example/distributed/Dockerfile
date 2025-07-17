# Use Eclipse Temurin which has better ARM64 support
FROM eclipse-temurin:11-jdk-jammy

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the entire project to build
COPY . .

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the project and create fat JAR
RUN ./gradlew :example:distributed:fatJar -x test

# Debug: List what was built
RUN ls -la example/distributed/build/libs/ || echo "No libs directory found"
RUN ls -la example/distributed/build/ || echo "No build directory found"

# Copy the built fat JAR - look for the "all" classifier first
RUN if [ -f example/distributed/build/libs/*-all.jar ]; then \
    cp example/distributed/build/libs/*-all.jar /app/jcachex-examples.jar; \
    else \
    JAR_FILE=$(find example/distributed/build/libs -name "*.jar" | head -1) && \
    if [ -n "$JAR_FILE" ]; then \
    cp "$JAR_FILE" /app/jcachex-examples.jar; \
    else \
    echo "No JAR file found in build output"; \
    exit 1; \
    fi; \
    fi

# Copy dependencies for classpath
RUN ./gradlew :example:distributed:copyDependencies || true
RUN if [ -d "example/distributed/build/dependencies" ]; then cp -r example/distributed/build/dependencies/* /app/; fi

# Set classpath
ENV CLASSPATH="/app/jcachex-examples.jar:/app/*"

# Expose port for JCacheX distributed cache
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Default command - use the JAR file directly since it contains all dependencies
CMD ["java", "-jar", "/app/jcachex-examples.jar"]
