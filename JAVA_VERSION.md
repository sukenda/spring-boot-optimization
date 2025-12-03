# Java Version Management dengan SDKMAN

Project ini memerlukan **Java 21** untuk build dan run.

## Menggunakan SDKMAN untuk Multiple Java Versions

SDKMAN memungkinkan Anda menginstall dan switch antara multiple Java versions tanpa mengubah default system Java.

### Setup Java 21 untuk Project Ini

**1. Install Java 21 (jika belum):**
```bash
sdk install java 21.0.1-tem
```

**2. Gunakan Java 21 untuk project ini saja:**

**Opsi A: Menggunakan `.sdkmanrc` (Recommended)**
```bash
# File .sdkmanrc sudah dibuat di project root
# Aktifkan Java 21 untuk project ini
cd /path/to/spring-boot-optimization
sdk env

# Verifikasi
java -version  # Should show Java 21
```

**Opsi B: Manual switch per session**
```bash
# Switch ke Java 21
sdk use java 21.0.1-tem

# Verifikasi
java -version
```

**Opsi C: Set JAVA_HOME di terminal**
```bash
export JAVA_HOME=$HOME/.sdkman/candidates/java/current
export PATH=$JAVA_HOME/bin:$PATH
java -version
```

### Switch ke Java Lain (Java 8 atau 11)

**Untuk project lain yang butuh Java 8:**
```bash
# Install Java 8 (jika belum)
sdk install java 8.0.392-tem

# Switch ke Java 8
sdk use java 8.0.392-tem
java -version  # Should show Java 8
```

**Untuk project lain yang butuh Java 11:**
```bash
# Install Java 11 (jika belum)
sdk install java 11.0.21-tem

# Switch ke Java 11
sdk use java 11.0.21-tem
java -version  # Should show Java 11
```

### Set Default Java Version

**Set Java 8 sebagai default (untuk project lain):**
```bash
sdk default java 8.0.392-tem
```

**Set Java 11 sebagai default:**
```bash
sdk default java 11.0.21-tem
```

**Set Java 21 sebagai default:**
```bash
sdk default java 21.0.1-tem
```

### List Installed Java Versions

```bash
sdk list java | grep installed
```

### Current Java Version

```bash
sdk current java
```

## Build Project Ini

**Dengan SDKMAN (Recommended):**
```bash
# Aktifkan Java 21 untuk project ini
cd /path/to/spring-boot-optimization
sdk env  # Auto-switch ke Java 21

# Build
make build
# atau
./gradlew clean build -x test -x processAot
```

**Manual:**
```bash
# Switch ke Java 21
sdk use java 21.0.1-tem

# Build
make build
```

## Tips

1. **`.sdkmanrc` file**: File ini sudah dibuat di project root. Ketika Anda `cd` ke project directory dan run `sdk env`, SDKMAN akan otomatis switch ke Java 21.

2. **Auto-switch dengan direnv (Optional)**: Install `direnv` untuk auto-switch saat masuk ke directory:
   ```bash
   # Install direnv
   # Ubuntu/Debian
   sudo apt install direnv
   
   # Add to shell config (.zshrc or .bashrc)
   eval "$(direnv hook zsh)"  # or bash
   
   # Allow direnv in project
   cd /path/to/spring-boot-optimization
   direnv allow
   ```

3. **Multiple Projects**: Setiap project bisa punya `.sdkmanrc` sendiri dengan Java version yang berbeda.

## Troubleshooting

**Build masih pakai Java 8/11:**
```bash
# Pastikan Java 21 aktif
sdk current java  # Should show 21.0.1-tem

# Jika tidak, switch manual
sdk use java 21.0.1-tem

# Set JAVA_HOME
export JAVA_HOME=$HOME/.sdkman/candidates/java/current
```

**Gradle masih pakai Java lama:**
```bash
# Clear Gradle cache
./gradlew --stop
rm -rf ~/.gradle/caches/

# Rebuild dengan Java 21
sdk use java 21.0.1-tem
./gradlew clean build
```

