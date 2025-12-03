# Database Setup Guide

## Current Configuration

### Development (Default)
- **Database:** H2 in-memory (ready to use, no setup required!)
- **Driver:** R2DBC H2
- **Auto Migration:** Enabled
- **Status:** ✅ Ready to run immediately

### Production
- **Database:** MySQL
- **Driver:** R2DBC MySQL
- **Auto Migration:** Enabled
- **Status:** Requires MySQL setup

---

## Quick Start (H2 - Development)

**No setup required!** Aplikasi sudah dikonfigurasi untuk menggunakan H2 in-memory database.

```bash
# Build dan run
make run-dev

# Atau langsung run JAR
java -jar build/libs/spring-boot-optimization-0.0.1.jar --spring.profiles.active=dev
```

**Apa yang terjadi:**
1. ✅ H2 database akan dibuat otomatis di memory
2. ✅ Migrations akan dijalankan otomatis (V1, V2, V3, V4)
3. ✅ Default roles akan di-insert (ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR)
4. ✅ Aplikasi siap digunakan!

**Catatan:** Data di H2 in-memory akan hilang saat aplikasi di-restart.

---

## MySQL Setup (Optional - untuk Production)

### 1. Install MySQL

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install mysql-server

# Start MySQL service
sudo systemctl start mysql
sudo systemctl enable mysql
```

### 2. Buat Database

```bash
mysql -u root -p
```

```sql
-- Development database
CREATE DATABASE devdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Production database
CREATE DATABASE proddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (optional)
CREATE USER 'app_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON devdb.* TO 'app_user'@'localhost';
GRANT ALL PRIVILEGES ON proddb.* TO 'app_user'@'localhost';
FLUSH PRIVILEGES;

EXIT;
```

### 3. Update Configuration

**Untuk Development:**
Edit `src/main/resources/application-dev.yml`:

```yaml
r2dbc:
  # Comment H2, uncomment MySQL
  # url: ${DB_URL:r2dbc:h2:mem:///devdb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL}
  url: ${DB_URL:r2dbc:mysql://localhost:3306/devdb?useSSL=false&allowPublicKeyRetrieval=true}
  username: ${DB_USERNAME:root}
  password: ${DB_PASSWORD:yourpassword}
```

**Untuk Production:**
Edit `src/main/resources/application-prod.yml` atau set environment variables:

```bash
export DB_URL=r2dbc:mysql://localhost:3306/proddb?useSSL=true
export DB_USERNAME=app_user
export DB_PASSWORD=your_password
```

### 4. Run Application

```bash
# Development dengan MySQL
make run-dev

# Production dengan MySQL
make run-prod
```

---

## Migration Files

Migrations ada di `src/main/resources/db/migration/`:

- **V1__initial_schema.sql** - Create tables (users, roles, user_roles)
- **V2__insert_default_roles.sql** - Insert default roles
- **V3__create_audit_tables.sql** - Create audit_logs table
- **V4__add_deleted_at_for_soft_delete.sql** - Add soft delete support

**Migrations akan dijalankan otomatis saat aplikasi start** (`sql.init.mode: always`).

---

## Database Compatibility

### H2 (Current Default)
- ✅ Fully compatible dengan migration files
- ✅ Syntax sudah disesuaikan untuk H2
- ✅ Auto-increment support
- ✅ Foreign keys support
- ⚠️ Data hilang saat restart (in-memory)

### MySQL
- ✅ Fully compatible
- ✅ Production ready
- ✅ Persistent data
- ✅ Better performance untuk production

---

## Troubleshooting

### Error: "Can't connect to MySQL server"
**Solution:** 
- Pastikan MySQL service running: `sudo systemctl status mysql`
- Cek username/password
- Gunakan H2 untuk development (tidak perlu setup)

### Error: "Table already exists"
**Solution:**
- H2: Normal, karena in-memory reset setiap restart
- MySQL: Hapus database dan buat ulang, atau set `continue-on-error: true`

### Error: "Migration failed"
**Solution:**
- Cek syntax SQL compatibility dengan database yang digunakan
- Pastikan database sudah dibuat
- Cek logs untuk detail error

---

## Summary

**Untuk Development (Recommended):**
- ✅ Gunakan H2 in-memory (default)
- ✅ Tidak perlu setup apapun
- ✅ Langsung bisa running

**Untuk Production:**
- ✅ Setup MySQL
- ✅ Update configuration
- ✅ Set environment variables

**Status Saat Ini:**
- ✅ H2 configured sebagai default
- ✅ Migrations ready
- ✅ Aplikasi bisa langsung running!
