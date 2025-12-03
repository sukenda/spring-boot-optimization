# Installation Guide

## System Requirements

### Minimum Requirements

- **OS**: Linux, macOS, or Windows
- **RAM**: 1GB (2GB recommended)
- **CPU**: 1 core (2+ cores recommended)
- **Disk**: 10GB free space

### Software Requirements

- **Java**: 21 or higher
- **Gradle**: 8.0+ (or use Gradle Wrapper)
- **MySQL**: 8.0+ (for production)
- **Make**: Optional (for Makefile commands)

## Installation Steps

### 1. Install Java 21

#### Ubuntu/Debian

```bash
sudo apt update
sudo apt install openjdk-21-jdk
java -version
```

#### macOS

```bash
brew install openjdk@21
```

#### Windows

Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)

### 2. Install Gradle (Optional)

Gradle Wrapper is included, but you can install Gradle globally:

```bash
# Ubuntu/Debian
sudo apt install gradle

# macOS
brew install gradle

# Or use SDKMAN
sdk install gradle 8.5
```

### 3. Install MySQL

#### Ubuntu/Debian

```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

#### macOS

```bash
brew install mysql
brew services start mysql
```

#### Windows

Download from [MySQL Downloads](https://dev.mysql.com/downloads/mysql/)

### 4. Install Make (Optional)

#### Ubuntu/Debian

```bash
sudo apt install make
```

#### macOS

Already included

#### Windows

Install via [Chocolatey](https://chocolatey.org/) or use WSL

### 5. Clone Repository

```bash
git clone <repository-url>
cd spring-boot-optimization
```

### 6. Verify Installation

```bash
# Check Java
java -version

# Check Gradle (if installed)
gradle --version

# Or use wrapper
./gradlew --version

# Check MySQL
mysql --version
```

## IDE Setup

### IntelliJ IDEA

1. **Open Project**
    - File → Open → Select project directory

2. **Import Gradle Project**
    - IntelliJ will detect Gradle automatically
    - Click "Import Gradle Project"

3. **Enable Lombok**
    - File → Settings → Plugins
    - Search for "Lombok" and install
    - Enable annotation processing:
        - Settings → Build → Compiler → Annotation Processors
        - Check "Enable annotation processing"

4. **Configure SDK**
    - File → Project Structure → Project
    - Set SDK to Java 21

### Eclipse

1. **Import Project**
    - File → Import → Gradle → Existing Gradle Project

2. **Install Lombok**
    - Download lombok.jar
    - Run: `java -jar lombok.jar`
    - Select Eclipse installation directory

3. **Configure Java**
    - Project → Properties → Java Build Path
    - Set Java 21 as JRE

### VS Code

1. **Install Extensions**
    - Java Extension Pack
    - Lombok Annotations Support

2. **Open Project**
    - File → Open Folder

3. **Configure Java**
    - Set JAVA_HOME to Java 21

## Database Setup

### Create Development Database

```bash
mysql -u root -p
```

```sql
CREATE
DATABASE devdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE
DATABASE proddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### Configure Connection

Set environment variables:

```bash
export DB_URL=r2dbc:mysql://localhost:3306/devdb?useSSL=false
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
```

Or update `application-dev.yml` directly.

## Verify Installation

### Build Project

```bash
make build
# or
./gradlew clean build
```

### Run Application

```bash
make run-dev
# or
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Test Installation

```bash
# Health check
curl http://localhost:8080/actuator/health

# Should return: {"status":"UP"}
```

## Troubleshooting

### Java Version Issues

```bash
# Check Java version
java -version

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

### Gradle Issues

```bash
# Use wrapper instead
./gradlew --version

# If wrapper missing, generate it
gradle wrapper --gradle-version 8.5
```

### MySQL Connection Issues

1. Check MySQL is running:
   ```bash
   sudo systemctl status mysql
   ```

2. Verify credentials
3. Check firewall rules
4. Verify connection string

### Port Already in Use

```bash
# Change port
export SERVER_PORT=8081
make run-dev
```

## Next Steps

After installation:

1. Read [Getting Started Guide](./GETTING_STARTED.md)
2. Review [Development Guide](./DEVELOPMENT.md)
3. Check [Configuration Guide](../CONFIG.md)
4. Explore [API Documentation](./API.md)

