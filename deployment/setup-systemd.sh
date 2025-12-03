#!/bin/bash
# Setup script for Spring Boot optimization systemd service
# Usage: sudo ./setup-systemd.sh

set -e

APP_NAME="spring-boot-optimization"
APP_USER="springboot"
APP_GROUP="springboot"
APP_DIR="/opt/spring-boot-optimization"
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"
JAR_FILE="${APP_DIR}/${APP_NAME}-0.0.1-SNAPSHOT.jar"

echo "=========================================="
echo "Spring Boot optimization - Systemd Setup"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "❌ Please run as root (use sudo)"
    exit 1
fi

# Check if Java 21 is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 21 first:"
    echo "   sudo apt update"
    echo "   sudo apt install openjdk-21-jdk"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java 21+ required. Found Java $JAVA_VERSION"
    echo "   Please install Java 21: sudo apt install openjdk-21-jdk"
    exit 1
fi

echo "✅ Java $(java -version 2>&1 | head -n 1) found"

# Create application user
if ! id "$APP_USER" &>/dev/null; then
    echo "Creating user: $APP_USER..."
    useradd -r -s /bin/false -d "$APP_DIR" "$APP_USER"
    echo "✅ User created"
else
    echo "✅ User $APP_USER already exists"
fi

# Create application directory
echo "Creating application directory: $APP_DIR..."
mkdir -p "$APP_DIR"/{logs,config}
chown -R "$APP_USER:$APP_GROUP" "$APP_DIR"
echo "✅ Directory created"

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "⚠️  JAR file not found at: $JAR_FILE"
    echo "   Please copy the JAR file to: $APP_DIR/"
    echo "   Example: cp build/libs/${APP_NAME}-0.0.1-SNAPSHOT.jar $APP_DIR/"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "✅ JAR file found"
    chown "$APP_USER:$APP_GROUP" "$JAR_FILE"
    chmod 644 "$JAR_FILE"
fi

# Copy service file
echo "Installing systemd service..."
cp "$(dirname "$0")/${APP_NAME}.service" "$SERVICE_FILE"
chmod 644 "$SERVICE_FILE"
echo "✅ Service file installed"

# Update JAVA_HOME in service file if needed
JAVA_HOME=$(readlink -f $(which java) | sed "s:bin/java::")
if [ -n "$JAVA_HOME" ]; then
    # Remove /jre if present
    JAVA_HOME=$(echo "$JAVA_HOME" | sed 's:/jre$::')
    sed -i "s|JAVA_HOME=.*|JAVA_HOME=$JAVA_HOME|" "$SERVICE_FILE"
    echo "✅ JAVA_HOME set to: $JAVA_HOME"
fi

# Create environment file for secrets (optional)
ENV_FILE="/etc/${APP_NAME}/application-prod.env"
if [ ! -f "$ENV_FILE" ]; then
    echo "Creating environment file template: $ENV_FILE"
    mkdir -p "$(dirname "$ENV_FILE")"
    cat > "$ENV_FILE" << EOF
# Spring Boot optimization - Production Environment Variables
# IMPORTANT: Change these values in production!

# Database
DB_URL=r2dbc:mysql://localhost:3306/proddb?useSSL=true
DB_USERNAME=prod_user
DB_PASSWORD=your_secure_password_here

# JWT (IMPORTANT: Use a secure secret key!)
JWT_SECRET=CHANGE-THIS-TO-A-SECURE-SECRET-KEY-MINIMUM-256-BITS-FOR-HMAC-SHA256
JWT_EXPIRATION=86400000
JWT_ISSUER=spring-boot-optimization
JWT_AUDIENCE=spring-boot-optimization-users

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
EOF
    chmod 600 "$ENV_FILE"
    echo "✅ Environment file created (edit: $ENV_FILE)"
    echo "⚠️  IMPORTANT: Edit $ENV_FILE and set secure passwords!"
fi

# Reload systemd
echo "Reloading systemd daemon..."
systemctl daemon-reload
echo "✅ Systemd reloaded"

echo ""
echo "=========================================="
echo "✅ Setup completed!"
echo "=========================================="
echo ""
echo "Next steps:"
echo ""
echo "1. Edit environment variables (if needed):"
echo "   sudo nano $ENV_FILE"
echo ""
echo "2. Copy your JAR file (if not already done):"
echo "   sudo cp build/libs/${APP_NAME}-0.0.1-SNAPSHOT.jar $APP_DIR/"
echo "   sudo chown $APP_USER:$APP_GROUP $APP_DIR/${APP_NAME}-0.0.1-SNAPSHOT.jar"
echo ""
echo "3. Start the service:"
echo "   sudo systemctl start $APP_NAME"
echo ""
echo "4. Enable auto-start on boot:"
echo "   sudo systemctl enable $APP_NAME"
echo ""
echo "5. Check status:"
echo "   sudo systemctl status $APP_NAME"
echo ""
echo "6. View logs:"
echo "   sudo journalctl -u $APP_NAME -f"
echo ""

