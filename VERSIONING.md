# Version Management Guide

Sistem versioning untuk Spring Boot optimization menggunakan Semantic Versioning (SemVer).

## Version Format

Format: `MAJOR.MINOR.PATCH` (contoh: `1.0.0`)

- **MAJOR**: Breaking changes (tidak backward compatible)
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Version Types

- **Release Version**: `1.0.0` (tanpa SNAPSHOT)
- **Development Version**: `1.0.1-SNAPSHOT` (dengan SNAPSHOT)

## Version Sources (Priority Order)

1. **Git Tag** (highest priority)
   - Format: `v1.0.0` atau `1.0.0`
   - Diambil dari: `git describe --tags --abbrev=0`
   - Untuk production releases

2. **version.properties**
   - File: `version.properties`
   - Format: `version=1.0.0`
   - Fallback jika tidak ada git tag

3. **build.gradle**
   - Default: `0.0.1-SNAPSHOT`
   - Hanya jika tidak ada git tag dan version.properties

## Quick Start

### Show Current Version
```bash
make version
# atau
./scripts/version.sh current
```

### Bump Version

**Patch version (bug fixes):**
```bash
make version-bump
# atau
./scripts/version.sh bump patch
# 0.0.1 -> 0.0.2-SNAPSHOT
```

**Minor version (new features):**
```bash
make version-minor
# atau
./scripts/version.sh bump minor
# 0.0.1 -> 0.1.0-SNAPSHOT
```

**Major version (breaking changes):**
```bash
make version-major
# atau
./scripts/version.sh bump major
# 0.0.1 -> 1.0.0-SNAPSHOT
```

### Release Version

**Remove SNAPSHOT untuk release:**
```bash
make version-release
# atau
./scripts/version.sh release
# 0.0.1-SNAPSHOT -> 0.0.1
```

**Release specific version:**
```bash
./scripts/version.sh release 1.0.0
```

## Workflow

### Development Workflow

1. **Start development:**
   ```bash
   # Current: 1.0.0 (release)
   make version-bump
   # Now: 1.0.1-SNAPSHOT
   ```

2. **Make changes and commit:**
   ```bash
   git add .
   git commit -m "Add new feature"
   ```

3. **Build and test:**
   ```bash
   make build-prod
   # JAR: spring-boot-optimization-1.0.1-SNAPSHOT.jar
   ```

4. **Release:**
   ```bash
   make version-release
   # Now: 1.0.1 (release)
   
   git add .
   git commit -m "Release v1.0.1"
   git tag -a v1.0.1 -m "Release v1.0.1"
   git push && git push --tags
   ```

5. **Build release:**
   ```bash
   make build-prod
   # JAR: spring-boot-optimization-1.0.1.jar
   ```

### Production Deployment Workflow

1. **Build dengan version:**
   ```bash
   make version
   # Shows: Current version: 1.0.1
   
   make build-prod
   # Creates: build/libs/spring-boot-optimization-1.0.1.jar
   ```

2. **Transfer ke server:**
   ```bash
   scp build/libs/spring-boot-optimization-1.0.1.jar user@server:/tmp/
   ```

3. **Update di server:**
   ```bash
   # Di server
   sudo ./deployment/update-version.sh 1.0.1 /tmp/spring-boot-optimization-1.0.1.jar
   ```

4. **Verify:**
   ```bash
   sudo systemctl status spring-boot-optimization
   curl http://localhost:8080/actuator/info
   ```

## Version Management di Server

### Directory Structure

```
/opt/spring-boot-optimization/
├── app.jar -> versions/spring-boot-optimization-1.0.1.jar (symlink)
├── versions/
│   ├── spring-boot-optimization-1.0.0.jar
│   ├── spring-boot-optimization-1.0.0.jar.backup
│   ├── spring-boot-optimization-1.0.1.jar (current)
│   └── spring-boot-optimization-1.0.2-SNAPSHOT.jar
└── logs/
```

### Update Version

**Automatic (recommended):**
```bash
sudo ./deployment/update-version.sh 1.0.1 /tmp/spring-boot-optimization-1.0.1.jar
```

**Manual:**
```bash
# Stop service
sudo systemctl stop spring-boot-optimization

# Copy new JAR
sudo cp /tmp/spring-boot-optimization-1.0.1.jar /opt/spring-boot-optimization/versions/
sudo chown springboot:springboot /opt/spring-boot-optimization/versions/*.jar

# Update symlink
sudo ln -sf /opt/spring-boot-optimization/versions/spring-boot-optimization-1.0.1.jar \
            /opt/spring-boot-optimization/app.jar

# Start service
sudo systemctl start spring-boot-optimization
```

### Rollback ke Version Sebelumnya

```bash
# List available versions
ls -lh /opt/spring-boot-optimization/versions/

# Rollback to specific version
sudo systemctl stop spring-boot-optimization
sudo ln -sf /opt/spring-boot-optimization/versions/spring-boot-optimization-1.0.0.jar \
            /opt/spring-boot-optimization/app.jar
sudo systemctl start spring-boot-optimization
```

## Git Integration

### Tagging Releases

**Create tag:**
```bash
git tag -a v1.0.1 -m "Release v1.0.1"
git push origin v1.0.1
```

**List tags:**
```bash
git tag -l
```

**Delete tag (if needed):**
```bash
git tag -d v1.0.1
git push origin :refs/tags/v1.0.1
```

### Version dari Git Tag

Build system akan otomatis menggunakan git tag sebagai version:

```bash
# Tag exists
git tag v1.0.1
make build-prod
# JAR: spring-boot-optimization-1.0.1.jar

# No tag, use version.properties
make build-prod
# JAR: spring-boot-optimization-0.0.1.jar (from version.properties)
```

## Best Practices

1. **Always tag releases:**
   ```bash
   git tag -a v1.0.1 -m "Release v1.0.1"
   git push --tags
   ```

2. **Use SNAPSHOT for development:**
   - Development: `1.0.1-SNAPSHOT`
   - Release: `1.0.1`

3. **Keep version.properties updated:**
   - Update saat bump version
   - Commit ke git

4. **Backup old versions:**
   - Script `update-version.sh` otomatis backup
   - Keep last 3-5 versions

5. **Document changes:**
   - CHANGELOG.md untuk track changes
   - Git commit messages yang jelas

## Version dalam Manifest

Version juga disertakan dalam JAR manifest:

```bash
jar xf spring-boot-optimization-1.0.1.jar META-INF/MANIFEST.MF
cat META-INF/MANIFEST.MF | grep Implementation-Version
```

## Troubleshooting

**Version tidak terdeteksi:**
```bash
# Check git tag
git tag -l

# Check version.properties
cat version.properties

# Check build.gradle
grep "^version = " build.gradle
```

**Build dengan version manual:**
```bash
# Override version
./gradlew clean build -Pversion=1.0.1
```

**Version di server tidak update:**
```bash
# Check symlink
ls -la /opt/spring-boot-optimization/app.jar

# Check service file
sudo systemctl cat spring-boot-optimization | grep app.jar
```

## Examples

### Example 1: Patch Release

```bash
# Current: 1.0.0
make version-bump
# Now: 1.0.1-SNAPSHOT

# Develop and test...
make build-prod

# Release
make version-release
# Now: 1.0.1

git add .
git commit -m "Release v1.0.1"
git tag -a v1.0.1 -m "Release v1.0.1"
git push && git push --tags

make build-prod
# JAR: spring-boot-optimization-1.0.1.jar
```

### Example 2: Minor Release

```bash
# Current: 1.0.0
make version-minor
# Now: 1.1.0-SNAPSHOT

# Add new features...
make build-prod

# Release
make version-release
git tag -a v1.1.0 -m "Release v1.1.0"
```

### Example 3: Major Release

```bash
# Current: 1.0.0
make version-major
# Now: 2.0.0-SNAPSHOT

# Breaking changes...
make build-prod

# Release
make version-release
git tag -a v2.0.0 -m "Release v2.0.0"
```

