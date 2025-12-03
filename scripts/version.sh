#!/bin/bash
# Version management script for Spring Boot optimization
# Usage: ./scripts/version.sh [command] [args]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
VERSION_FILE="$PROJECT_ROOT/version.properties"
BUILD_GRADLE="$PROJECT_ROOT/build.gradle"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get current version from git tag or version.properties
get_current_version() {
    cd "$PROJECT_ROOT"
    
    # Try to get version from git tag
    if git rev-parse --git-dir > /dev/null 2>&1; then
        GIT_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
        if [ -n "$GIT_TAG" ]; then
            # Remove 'v' prefix if present
            VERSION=$(echo "$GIT_TAG" | sed 's/^v//')
            echo "$VERSION"
            return
        fi
    fi
    
    # Fallback to version.properties
    if [ -f "$VERSION_FILE" ]; then
        VERSION=$(grep "^version=" "$VERSION_FILE" | cut -d'=' -f2)
        echo "$VERSION"
        return
    fi
    
    # Fallback to build.gradle
    if [ -f "$BUILD_GRADLE" ]; then
        VERSION=$(grep "^version = " "$BUILD_GRADLE" | sed "s/version = ['\"]//" | sed "s/['\"].*//" | sed "s/-SNAPSHOT//")
        echo "$VERSION"
        return
    fi
    
    echo "0.0.1"
}

# Get next version based on type (major, minor, patch)
get_next_version() {
    local CURRENT_VERSION=$1
    local TYPE=$2
    
    IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"
    MAJOR=${VERSION_PARTS[0]}
    MINOR=${VERSION_PARTS[1]}
    PATCH=${VERSION_PARTS[2]}
    
    case "$TYPE" in
        major)
            MAJOR=$((MAJOR + 1))
            MINOR=0
            PATCH=0
            ;;
        minor)
            MINOR=$((MINOR + 1))
            PATCH=0
            ;;
        patch)
            PATCH=$((PATCH + 1))
            ;;
        *)
            echo "Invalid version type: $TYPE. Use: major, minor, or patch" >&2
            exit 1
            ;;
    esac
    
    echo "$MAJOR.$MINOR.$PATCH"
}

# Update version in all files
update_version() {
    local NEW_VERSION=$1
    local SNAPSHOT=${2:-false}
    
    cd "$PROJECT_ROOT"
    
    # Update version.properties
    if [ -f "$VERSION_FILE" ]; then
        sed -i "s/^version=.*/version=$NEW_VERSION/" "$VERSION_FILE"
        sed -i "s/^build.date=.*/build.date=$(date +%Y-%m-%d)/" "$VERSION_FILE"
        BUILD_NUM=$(grep "^build.number=" "$VERSION_FILE" | cut -d'=' -f2)
        BUILD_NUM=$((BUILD_NUM + 1))
        sed -i "s/^build.number=.*/build.number=$BUILD_NUM/" "$VERSION_FILE"
        echo "✅ Updated version.properties"
    fi
    
    # Update build.gradle
    if [ -f "$BUILD_GRADLE" ]; then
        if [ "$SNAPSHOT" = "true" ]; then
            sed -i "s/^version = .*/version = '$NEW_VERSION-SNAPSHOT'/" "$BUILD_GRADLE"
        else
            sed -i "s/^version = .*/version = '$NEW_VERSION'/" "$BUILD_GRADLE"
        fi
        echo "✅ Updated build.gradle"
    fi
    
    # Update Makefile
    if [ -f "$PROJECT_ROOT/Makefile" ]; then
        sed -i "s/^APP_VERSION := .*/APP_VERSION := $NEW_VERSION/" "$PROJECT_ROOT/Makefile"
        echo "✅ Updated Makefile"
    fi
}

# Show current version
show_version() {
    local CURRENT_VERSION=$(get_current_version)
    echo "Current version: $CURRENT_VERSION"
    
    if git rev-parse --git-dir > /dev/null 2>&1; then
        local GIT_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "none")
        if [ "$GIT_TAG" != "none" ]; then
            echo "Git tag: $GIT_TAG"
        else
            echo "Git tag: none (not tagged yet)"
        fi
    fi
}

# Bump version
bump_version() {
    local TYPE=$1
    local CURRENT_VERSION=$(get_current_version)
    local NEXT_VERSION=$(get_next_version "$CURRENT_VERSION" "$TYPE")
    
    echo "Current version: $CURRENT_VERSION"
    echo "Next version: $NEXT_VERSION"
    read -p "Continue? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 0
    fi
    
    update_version "$NEXT_VERSION" "true"
    
    echo ""
    echo "✅ Version bumped to: $NEXT_VERSION-SNAPSHOT"
    echo ""
    echo "Next steps:"
    echo "1. Commit changes: git add . && git commit -m 'Bump version to $NEXT_VERSION-SNAPSHOT'"
    echo "2. Build: make build-prod"
    echo "3. Test the build"
    echo "4. Tag release: git tag -a v$NEXT_VERSION -m 'Release v$NEXT_VERSION'"
    echo "5. Update to release version: ./scripts/version.sh release $NEXT_VERSION"
}

# Release version (remove SNAPSHOT)
release_version() {
    local VERSION=${1:-$(get_current_version | sed 's/-SNAPSHOT//')}
    
    echo "Releasing version: $VERSION"
    read -p "Continue? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 0
    fi
    
    update_version "$VERSION" "false"
    
    echo ""
    echo "✅ Released version: $VERSION"
    echo ""
    echo "Next steps:"
    echo "1. Commit changes: git add . && git commit -m 'Release v$VERSION'"
    echo "2. Tag: git tag -a v$VERSION -m 'Release v$VERSION'"
    echo "3. Build: make build-prod"
    echo "4. Push: git push && git push --tags"
}

# Main
case "$1" in
    current|show)
        show_version
        ;;
    bump)
        TYPE=${2:-patch}
        if [[ ! "$TYPE" =~ ^(major|minor|patch)$ ]]; then
            echo "Usage: $0 bump [major|minor|patch]"
            exit 1
        fi
        bump_version "$TYPE"
        ;;
    release)
        VERSION=$2
        release_version "$VERSION"
        ;;
    *)
        echo "Version Management Script"
        echo ""
        echo "Usage: $0 [command] [args]"
        echo ""
        echo "Commands:"
        echo "  current, show    - Show current version"
        echo "  bump [type]       - Bump version (major|minor|patch, default: patch)"
        echo "  release [version] - Release version (remove SNAPSHOT)"
        echo ""
        echo "Examples:"
        echo "  $0 current                    # Show current version"
        echo "  $0 bump patch                 # Bump patch version (0.0.1 -> 0.0.2-SNAPSHOT)"
        echo "  $0 bump minor                 # Bump minor version (0.0.1 -> 0.1.0-SNAPSHOT)"
        echo "  $0 bump major                 # Bump major version (0.0.1 -> 1.0.0-SNAPSHOT)"
        echo "  $0 release                    # Release current version (remove SNAPSHOT)"
        echo "  $0 release 1.0.0              # Release specific version"
        exit 1
        ;;
esac

