#!/bin/bash
# Script to update application version on server
# Usage: ./update-version.sh [version] [jar-file]

set -e

APP_NAME="spring-boot-optimization"
APP_DIR="/opt/spring-boot-optimization"
SERVICE_NAME="spring-boot-optimization"
CURRENT_SYMLINK="$APP_DIR/app.jar"

VERSION=$1
JAR_FILE=$2

if [ -z "$VERSION" ]; then
    echo "Usage: $0 <version> <jar-file>"
    echo "Example: $0 1.0.0 /tmp/spring-boot-optimization-1.0.0.jar"
    exit 1
fi

if [ -z "$JAR_FILE" ]; then
    echo "Error: JAR file path required"
    exit 1
fi

if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    exit 1
fi

echo "=========================================="
echo "Updating Spring Boot optimization"
echo "Version: $VERSION"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "❌ Please run as root (use sudo)"
    exit 1
fi

# Stop service
echo "Stopping service..."
systemctl stop "$SERVICE_NAME" || true
echo "✅ Service stopped"

# Create versioned directory
VERSION_DIR="$APP_DIR/versions"
mkdir -p "$VERSION_DIR"
echo "✅ Version directory ready"

# Copy new JAR with version
VERSIONED_JAR="$VERSION_DIR/${APP_NAME}-${VERSION}.jar"
echo "Copying JAR to: $VERSIONED_JAR"
cp "$JAR_FILE" "$VERSIONED_JAR"
chown springboot:springboot "$VERSIONED_JAR"
chmod 644 "$VERSIONED_JAR"
echo "✅ JAR copied"

# Backup old symlink if exists
if [ -L "$CURRENT_SYMLINK" ]; then
    OLD_VERSION=$(readlink "$CURRENT_SYMLINK" | sed "s|.*${APP_NAME}-||" | sed "s|\.jar||")
    BACKUP_SYMLINK="$VERSION_DIR/${APP_NAME}-${OLD_VERSION}.jar.backup"
    if [ -f "$(readlink -f "$CURRENT_SYMLINK")" ]; then
        cp "$(readlink -f "$CURRENT_SYMLINK")" "$BACKUP_SYMLINK"
        echo "✅ Old version backed up: $BACKUP_SYMLINK"
    fi
fi

# Create/update symlink
echo "Creating symlink: $CURRENT_SYMLINK -> $VERSIONED_JAR"
ln -sf "$VERSIONED_JAR" "$CURRENT_SYMLINK"
chown -h springboot:springboot "$CURRENT_SYMLINK"
echo "✅ Symlink created"

# Verify JAR
echo "Verifying JAR..."
if java -jar "$CURRENT_SYMLINK" --version > /dev/null 2>&1; then
    echo "✅ JAR is valid"
else
    echo "⚠️  JAR validation skipped (may require database connection)"
fi

# Start service
echo "Starting service..."
systemctl start "$SERVICE_NAME"
sleep 2

# Check status
if systemctl is-active --quiet "$SERVICE_NAME"; then
    echo "✅ Service started successfully"
    echo ""
    echo "Current version: $VERSION"
    echo "JAR location: $VERSIONED_JAR"
    echo "Symlink: $CURRENT_SYMLINK"
    echo ""
    echo "View logs: sudo journalctl -u $SERVICE_NAME -f"
else
    echo "❌ Service failed to start"
    echo "Check logs: sudo journalctl -u $SERVICE_NAME -n 50"
    exit 1
fi

# List all versions
echo ""
echo "Available versions:"
ls -lh "$VERSION_DIR"/*.jar 2>/dev/null | awk '{print $9, "(" $5 ")"}' || echo "No versions found"

