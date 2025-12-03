# =============================================================
# Makefile for Spring Boot optimization
# =============================================================

# Use bash as shell (required for 'source' command)
SHELL := /bin/bash

.PHONY: help build build-dev build-prod build-native clean test run run-dev run-prod run-native stop version version-bump version-minor version-major version-release info version version-bump version-minor version-major version-release

# Variables
APP_NAME := spring-boot-optimization

# Get version from git tag, version.properties, or build.gradle
GET_VERSION := $(shell \
	if git rev-parse --git-dir > /dev/null 2>&1 && git describe --tags --abbrev=0 > /dev/null 2>&1; then \
		git describe --tags --abbrev=0 | sed 's/^v//'; \
	elif [ -f version.properties ]; then \
		grep "^version=" version.properties | cut -d'=' -f2; \
	else \
		grep "^version = " build.gradle | sed "s/version = ['\"]//" | sed "s/['\"].*//"; \
	fi)

APP_VERSION := $(if $(GET_VERSION),$(GET_VERSION),0.0.1-SNAPSHOT)
JAR_FILE := build/libs/$(APP_NAME)-$(APP_VERSION).jar
NATIVE_IMAGE := build/native/nativeCompile/$(APP_NAME)
GRADLE := ./gradlew

# JVM Options for Production
PROD_HEAP_SIZE := -Xms256m -Xmx384m
PROD_METASPACE := -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m
PROD_THREAD_STACK := -Xss512k
PROD_GC := -XX:+UseSerialGC
PROD_MEMORY_OPTS := -XX:+UseStringDeduplication -XX:+OptimizeStringConcat
PROD_OPTS := -XX:+DisableExplicitGC -XX:-OmitStackTraceInFastThrow
PROD_JVM_OPTS := $(PROD_HEAP_SIZE) $(PROD_METASPACE) $(PROD_THREAD_STACK) $(PROD_GC) $(PROD_MEMORY_OPTS) $(PROD_OPTS)

# JVM Options for Development
DEV_JVM_OPTS := -Xms128m -Xmx256m -XX:+UseSerialGC -Xss512k

# Default target
.DEFAULT_GOAL := help

# =============================================================
# Help
# =============================================================
help: ## Show this help message
	@echo "=========================================="
	@echo "Spring Boot optimization - Makefile Commands"
	@echo "=========================================="
	@echo ""
	@echo "Build Commands:"
	@echo "  make build          - Build JAR (development)"
	@echo "  make build-dev      - Build JAR with dev profile"
	@echo "  make build-prod     - Build JAR with prod profile (optimized)"
	@echo "  make build-native   - Build GraalVM native image (ultra-fast boot)"
	@echo ""
	@echo "Run Commands:"
	@echo "  make run            - Run application (production mode, uses native if available)"
	@echo "  make run-dev        - Run application (development mode)"
	@echo "  make run-prod       - Run JAR (production mode)"
	@echo "  make run-native     - Run native image (production mode)"
	@echo ""
	@echo "Other Commands:"
	@echo "  make test           - Run tests"
	@echo "  make clean          - Clean build artifacts"
	@echo "  make help           - Show this help message"
	@echo ""

# =============================================================
# Build Commands
# =============================================================
build: ## Build JAR (development)
	@echo "=========================================="
	@echo "Building JAR (Development)"
	@echo "=========================================="
	@if [ -f ~/.sdkman/bin/sdkman-init.sh ]; then \
		bash -c "source ~/.sdkman/bin/sdkman-init.sh && export JAVA_HOME=$$HOME/.sdkman/candidates/java/current && $(GRADLE) clean build -x test -x processAot"; \
	else \
		$(GRADLE) clean build -x test -x processAot; \
	fi

build-dev: ## Build JAR with dev profile
	@echo "=========================================="
	@echo "Building JAR (Development Profile)"
	@echo "=========================================="
	@if [ -f ~/.sdkman/bin/sdkman-init.sh ]; then \
		bash -c "source ~/.sdkman/bin/sdkman-init.sh && export JAVA_HOME=$$HOME/.sdkman/candidates/java/current && $(GRADLE) clean build -x test -x processAot -Pprofile=dev"; \
	else \
		$(GRADLE) clean build -x test -x processAot -Pprofile=dev; \
	fi

build-prod: ## Build JAR with prod profile (optimized)
	@echo "=========================================="
	@echo "Building JAR (Production Profile)"
	@echo "=========================================="
	@if [ -f ~/.sdkman/bin/sdkman-init.sh ]; then \
		bash -c "source ~/.sdkman/bin/sdkman-init.sh && export JAVA_HOME=$$HOME/.sdkman/candidates/java/current && $(GRADLE) clean build -x test -x processAot -Pprofile=prod"; \
	else \
		$(GRADLE) clean build -x test -x processAot -Pprofile=prod; \
	fi

build-native: ## Build GraalVM native image (ultra-fast boot)
	@echo "=========================================="
	@echo "Building Native Image (GraalVM)"
	@echo "This may take several minutes..."
	@echo "=========================================="
	$(GRADLE) nativeCompile

# =============================================================
# Run Commands
# =============================================================
run: ## Run application (production mode, uses native if available)
	@echo "=========================================="
	@echo "Starting Spring Boot optimization"
	@echo "=========================================="
	@if [ -f "$(NATIVE_IMAGE)" ]; then \
		echo "Using Native Image (fastest boot)"; \
		$(NATIVE_IMAGE) --spring.profiles.active=prod; \
	else \
		echo "Using JAR file"; \
		java $(PROD_JVM_OPTS) -jar $(JAR_FILE) --spring.profiles.active=prod; \
	fi

run-dev: build ## Run application (development mode)
	@echo "=========================================="
	@echo "Starting Spring Boot optimization (DEV MODE)"
	@echo "=========================================="
	@if [ ! -f "$(JAR_FILE)" ]; then \
		echo "JAR not found. Building..."; \
		$(MAKE) build; \
	fi
	@echo "Using H2 in-memory database (no MySQL setup required)"
	@echo "Database will be created automatically on startup"
	java $(DEV_JVM_OPTS) -jar $(JAR_FILE) --spring.profiles.active=dev

run-prod: build-prod ## Run JAR (production mode)
	@echo "=========================================="
	@echo "Starting Spring Boot optimization (PRODUCTION)"
	@echo "JVM Options: $(PROD_JVM_OPTS)"
	@echo "=========================================="
	@if [ ! -f "$(JAR_FILE)" ]; then \
		echo "JAR not found. Building..."; \
		$(MAKE) build-prod; \
	fi
	java $(PROD_JVM_OPTS) -jar $(JAR_FILE) --spring.profiles.active=prod

run-native: build-native ## Run native image (production mode)
	@echo "=========================================="
	@echo "Starting Spring Boot optimization (NATIVE IMAGE)"
	@echo "Ultra-fast boot time!"
	@echo "=========================================="
	@if [ ! -f "$(NATIVE_IMAGE)" ]; then \
		echo "Native image not found. Building..."; \
		$(MAKE) build-native; \
	fi
	$(NATIVE_IMAGE) --spring.profiles.active=prod

# =============================================================
# Gradle Wrapper Commands
# =============================================================
run-gradle: ## Run using Gradle bootRun (development)
	@echo "=========================================="
	@echo "Running with Gradle bootRun (DEV MODE)"
	@echo "=========================================="
	$(GRADLE) bootRun --args='--spring.profiles.active=dev'

# =============================================================
# Test Commands
# =============================================================
test: ## Run tests
	@echo "=========================================="
	@echo "Running Tests"
	@echo "=========================================="
	$(GRADLE) test

test-verbose: ## Run tests with verbose output
	@echo "=========================================="
	@echo "Running Tests (Verbose)"
	@echo "=========================================="
	$(GRADLE) test --info

# =============================================================
# Clean Commands
# =============================================================
clean: ## Clean build artifacts
	@echo "=========================================="
	@echo "Cleaning Build Artifacts"
	@echo "=========================================="
	$(GRADLE) clean
	@rm -rf build/
	@rm -rf .gradle/
	@echo "Clean completed!"

clean-all: clean ## Clean all including Gradle cache
	@echo "=========================================="
	@echo "Cleaning All (including Gradle cache)"
	@echo "=========================================="
	$(GRADLE) clean --no-daemon
	@rm -rf ~/.gradle/caches/

# =============================================================
# Utility Commands
# =============================================================
dependencies: ## Show dependencies
	@echo "=========================================="
	@echo "Dependencies"
	@echo "=========================================="
	$(GRADLE) dependencies

check: test ## Run tests and check code quality
	@echo "=========================================="
	@echo "Running Checks"
	@echo "=========================================="
	$(GRADLE) check

# =============================================================
# Version Management Commands
# =============================================================
version: ## Show current version
	@echo "Current version: $(APP_VERSION)"
	@echo "JAR file: $(JAR_FILE)"
	@if [ -f "$(JAR_FILE)" ]; then \
		echo "JAR exists: ✅"; \
		ls -lh "$(JAR_FILE)"; \
	else \
		echo "JAR exists: ❌ (not built yet)"; \
	fi

version-bump: ## Bump patch version (0.0.1 -> 0.0.2-SNAPSHOT)
	@./scripts/version.sh bump patch

version-minor: ## Bump minor version (0.0.1 -> 0.1.0-SNAPSHOT)
	@./scripts/version.sh bump minor

version-major: ## Bump major version (0.0.1 -> 1.0.0-SNAPSHOT)
	@./scripts/version.sh bump major

version-release: ## Release current version (remove SNAPSHOT)
	@./scripts/version.sh release

info: ## Show project information
	@echo "=========================================="
	@echo "Project Information"
	@echo "=========================================="
	@echo "Application Name: $(APP_NAME)"
	@echo "Version: $(APP_VERSION)"
	@echo "JAR File: $(JAR_FILE)"
	@echo "Native Image: $(NATIVE_IMAGE)"
	@echo ""
	@if [ -f "$(JAR_FILE)" ]; then \
		echo "✓ JAR file exists"; \
		ls -lh $(JAR_FILE); \
	else \
		echo "✗ JAR file not found"; \
	fi
	@echo ""
	@if [ -f "$(NATIVE_IMAGE)" ]; then \
		echo "✓ Native image exists"; \
		ls -lh $(NATIVE_IMAGE); \
	else \
		echo "✗ Native image not found"; \
	fi

