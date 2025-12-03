#!/bin/bash
# =============================================================
# Optimized JVM Startup Script for Spring Boot
# Target: 1GB RAM, 1 CPU Core Server
# Using Gradle build output
# =============================================================

# Check if native image exists (faster boot)
if [ -f "build/native/nativeCompile/spring-boot-optimization" ]; then
    echo "=========================================="
    echo "Starting Spring Boot optimization (Native Image)"
    echo "=========================================="
    ./build/native/nativeCompile/spring-boot-optimization --spring.profiles.active=prod
    exit 0
fi

# Application JAR (Gradle build output)
APP_JAR="build/libs/spring-boot-optimization-0.0.1-SNAPSHOT.jar"

# JVM Memory Settings for 1GB RAM server
# Reserve ~500MB for OS and other processes, use ~512MB for JVM
HEAP_SIZE="-Xms256m -Xmx384m"

# Metaspace settings
METASPACE="-XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m"

# Thread stack size (reduce from default 1MB to 512KB)
THREAD_STACK="-Xss512k"

# Garbage Collector - SerialGC is best for single-core, low-memory
GC_OPTIONS="-XX:+UseSerialGC"

# Alternative: G1GC with tuned settings for low memory
# GC_OPTIONS="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:G1HeapRegionSize=1m"

# Additional memory optimizations
MEMORY_OPTS="-XX:+UseStringDeduplication -XX:+OptimizeStringConcat"

# Disable JMX and other debugging features in production
PROD_OPTS="-XX:+DisableExplicitGC -XX:-OmitStackTraceInFastThrow"

# Container awareness (if running in Docker)
CONTAINER_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Class data sharing for faster startup
CDS_OPTS="-XX:+UseAppCDS -XX:SharedArchiveFile=app-cds.jsa"

# Compile all JVM arguments
JVM_OPTS="${HEAP_SIZE} ${METASPACE} ${THREAD_STACK} ${GC_OPTIONS} ${MEMORY_OPTS} ${PROD_OPTS}"

# Spring Boot specific options
SPRING_OPTS="--spring.profiles.active=prod"

echo "=========================================="
echo "Starting Spring Boot optimization"
echo "JVM Options: ${JVM_OPTS}"
echo "=========================================="

# Run the application
java ${JVM_OPTS} -jar ${APP_JAR} ${SPRING_OPTS}

