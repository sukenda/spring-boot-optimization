#!/bin/bash
# =============================================================
# Development Mode Startup Script
# Using Gradle build output
# =============================================================

APP_JAR="build/libs/spring-boot-optimization-0.0.1-SNAPSHOT.jar"

# Development JVM settings (more memory for debugging)
JVM_OPTS="-Xms128m -Xmx256m -XX:+UseSerialGC -Xss512k"

# Spring Boot options
SPRING_OPTS="--spring.profiles.active=dev"

echo "=========================================="
echo "Starting Spring Boot optimization (DEV MODE)"
echo "=========================================="

java ${JVM_OPTS} -jar ${APP_JAR} ${SPRING_OPTS}

