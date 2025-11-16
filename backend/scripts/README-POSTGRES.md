# 🗄️ PostgreSQL Database Setup - Youth Connect Platform

**Complete guide for setting up and managing PostgreSQL databases for all microservices**

---

## 📁 **File Structure**

Save these files in your `backend/scripts/` directory:

```
backend/
├── scripts/
│   ├── init-databases.sql          # Main initialization script (already created ✅)
│   ├── setup-postgres.ps1          # PowerShell automated setup
│   ├── quick-setup.bat             # Windows batch file (double-click)
│   ├── POSTGRES_COMMANDS.md        # Quick reference commands
│   └── README-POSTGRES.md          # This file
```

---

## 🚀 **Quick Start (3 Options)**

### **Option 1: Batch File (Easiest - Windows)**

1. Save `quick-setup.bat` to `backend/scripts/`
2. **Double-click** `quick-setup.bat`
3. Enter postgres password when prompted: `Douglas20!`
4. Done! ✅

### **Option 2: PowerShell Script (Recommended)**

```powershell
# Navigate to backend
cd "F:\Douglas Kings\Hackthon\EBP_YEPPAD\backend"

# Run setup script
.\scripts\setup-postgres.ps1

# Enter password: Douglas20!
```

### **Option 3: Manual Commands**

```powershell
# 1. Set encoding
$env:PGCLIENTENCODING = "UTF8"

# 2. Navigate to backend
cd "F:\Douglas Kings\Hackthon\EBP_YEPPAD\backend"

# 3. Run initialization script
& "F:\Installations\PostgreSql\bin\psql.exe" -U postgres -f scripts\init-databases.sql

# Enter password: Douglas20!
```

---

## ✅ **What Gets Created**

### **11 Databases:**

| # | Database | Purpose | Service |
|---|----------|---------|---------|
| 1 | `youthconnect_auth` | Authentication & Authorization | auth-service |
| 2 | `youthconnect_user` | User Profiles & Management | user-service |
| 3 | `youthconnect_job` | Job Opportunities | job-service |
| 4 | `youthconnect_opportunity` | Business Opportunities | opportunity-service |
| 5 | `youthconnect_mentor` | Mentorship Programs | mentor-service |
| 6 | `youthconnect_content` | Educational Content | content-service |
| 7 | `youthconnect_notification` | Multi-channel Notifications | notification-service |
| 8 | `youthconnect_file` | File Management | file-management-service |
| 9 | `youthconnect_ai` | AI Recommendations | ai-recommendation-service |
| 10 | `youthconnect_analytics` | Analytics & BI | analytics-service |
| 11 | `youthconnect_ussd` | USSD Service | ussd-service |

### **Database User:**
- **Username:** `youthconnect_user`
- **Password:** `YouthConnect2024!` ⚠️ *Change in production!*
- **Privileges:** Full access to all 11 databases

### **Extensions Installed:**
- `uuid-ossp` - UUID generation
- `pgcrypto` - Password hashing (auth-service)
- `pg_trgm` - Full-text search
- `btree_gin` - JSONB indexing
- `tablefunc` - Crosstab queries (analytics)

---

## 📊 **Verification**

### **Check Databases Were Created:**

```powershell
& "F:\Installations\PostgreSql\bin\psql.exe" -U postgres -c "SELECT datname FROM pg_database WHERE datname LIKE 'youthconnect_%' ORDER BY datname;"
```

**Expected Output:**
```
       datname        
---------------------
 youthconnect_ai
 youthconnect_analytics
 youthconnect_auth
 youthconnect_content
 youthconnect_file
 youthconnect_job
 youthconnect_mentor
 youthconnect_notification
 youthconnect_opportunity
 youthconnect_user
 youthconnect_ussd
(11 rows)
```

### **Test Connection:**

```powershell
# Connect to auth database
& "F:\Installations\PostgreSql\bin\psql.exe" -U youthconnect_user -d youthconnect_auth

# Inside psql:
\dt                          # List tables (should be empty before Flyway)
\dx                          # List extensions (should see uuid-ossp, etc.)
SELECT current_database();   # Verify connected database
\q                           # Exit
```

---

## 🔧 **Configuration Files**

### **For Each Service, Create `.env` File:**

**Example for Job Service** (`job-service/.env`):

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=youthconnect_job
DB_USERNAME=youthconnect_user
DB_PASSWORD=YouthConnect2024!

# Eureka
EUREKA_HOST=localhost
EUREKA_PORT=8761
EUREKA_USERNAME=admin
EUREKA_PASSWORD=changeme

# JWT (MUST MATCH ACROSS ALL SERVICES!)
JWT_SECRET=youth-connect-secure-secret-key-2025-minimum-256-bits-required-for-production

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Spring Profile
SPRING_PROFILES_ACTIVE=dev
```

**Copy this pattern for all services**, changing only:
- `DB_NAME` (e.g., `youthconnect_auth`, `youthconnect_user`, etc.)

---

## 📝 **Migration Files**

After database setup, create Flyway migration files for each service:

### **Directory Structure:**

```
<service-name>/
└── src/
    └── main/
        └── resources/
            └── db/
                └── migration/
                    ├── V1__Initial_Schema.sql
                    ├── V2__Add_Indexes.sql
                    └── V3__Seed_Data.sql
```

### **Naming Convention:**

- `V1__Initial_Schema.sql` - Initial table creation
- `V2__Add_Indexes.sql` - Performance indexes
- `V3__Seed_Data.sql` - Reference data
- `V4__Add_Column.sql` - Schema changes

**Rules:**
- Version numbers must be sequential (V1, V2, V3...)
- Use double underscore `__` after version
- Once executed, NEVER modify migration files
- Always create new migration for changes

---

## 🚀 **Starting Services**

### **Start Order (CRITICAL!):**

```
1. PostgreSQL Database ✅ (Already running)
2. Service Registry (Eureka)
3. Auth Service
4. User Service
5. API Gateway
6. Job Service (and other services)
```

### **Commands:**

**1. Service Registry:**
```powershell
cd service-registry
mvn clean install -DskipTests
mvn spring-boot:run

# Verify: http://localhost:8761
```

**2. Auth Service (NEW TERMINAL):**
```powershell
cd auth-service
mvn clean install -DskipTests
mvn spring-boot:run

# Watch for Flyway migrations:
# "Flyway: Migrating schema to version 1 - Initial Schema"
# Port: 8083
```

**3. User Service (NEW TERMINAL):**
```powershell
cd user-service
mvn clean install -DskipTests
mvn spring-boot:run

# Port: 8084
```

**4. API Gateway (NEW TERMINAL):**
```powershell
cd api-gateway
mvn clean install -DskipTests
mvn spring-boot:run

# Port: 8088
```

**5. Job Service (NEW TERMINAL):**
```powershell
cd job-service
mvn clean install -DskipTests
mvn spring-boot:run

# Port: 8000
```

---

## 🔍 **Health Checks**

### **Check Service Registration:**
```
URL: http://localhost:8761
```

**Expected services registered:**
- ✅ SERVICE-REGISTRY
- ✅ AUTH-SERVICE
- ✅ USER-SERVICE
- ✅ API-GATEWAY
- ✅ JOB-SERVICE

### **Check Service Health:**
```powershell
# Direct service health
curl http://localhost:8083/actuator/health  # Auth Service
curl http://localhost:8084/actuator/health  # User Service
curl http://localhost:8000/actuator/health  # Job Service

# Via API Gateway
curl http://localhost:8088/api/auth/actuator/health
curl http://localhost:8088/api/users/actuator/health
curl http://localhost:8088/api/jobs/actuator/health
```

**Expected response:**
```json
{
  "status": "UP"
}
```

---

## 🛠️ **Troubleshooting**

### **Issue 1: "Connection refused" to PostgreSQL**

**Solution:**
```powershell
# Check if PostgreSQL is running
Get-Service postgresql*

# Start if stopped
Start-Service postgresql-x64-16  # Adjust service name
```

### **Issue 2: "Password authentication failed"**

**Solution:**
```sql
-- Connect as postgres
psql -U postgres

-- Reset password
ALTER USER youthconnect_user WITH PASSWORD 'YouthConnect2024!';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE youthconnect_auth TO youthconnect_user;
```

### **Issue 3: Flyway migration fails**

**Solution:**
```sql
-- Check what failed
psql -U youthconnect_user -d youthconnect_job

SELECT * FROM flyway_schema_history WHERE success = false;

-- Fix SQL file, then repair:
```

```powershell
cd job-service
mvn flyway:repair
mvn spring-boot:run
```

### **Issue 4: Port already in use**

**Solution:**
```powershell
# Find what's using port 8000
netstat -ano | findstr :8000

# Kill the process (replace <PID>)
taskkill /PID <PID> /F
```

---

## 🗑️ **Clean Reinstall**

If you need to start over completely:

### **Option 1: Use Cleanup Script**
```powershell
.\scripts\setup-postgres.ps1  # Select 'yes' for cleanup
```

### **Option 2: Manual Cleanup**
```sql
-- Connect as postgres
psql -U postgres

-- Drop all databases
DROP DATABASE IF EXISTS youthconnect_auth CASCADE;
DROP DATABASE IF EXISTS youthconnect_user CASCADE;
DROP DATABASE IF EXISTS youthconnect_job CASCADE;
DROP DATABASE IF EXISTS youthconnect_opportunity CASCADE;
DROP DATABASE IF EXISTS youthconnect_mentor CASCADE;
DROP DATABASE IF EXISTS youthconnect_content CASCADE;
DROP DATABASE IF EXISTS youthconnect_notification CASCADE;
DROP DATABASE IF EXISTS youthconnect_file CASCADE;
DROP DATABASE IF EXISTS youthconnect_ai CASCADE;
DROP DATABASE IF EXISTS youthconnect_analytics CASCADE;
DROP DATABASE IF EXISTS youthconnect_ussd CASCADE;

-- Drop user
DROP ROLE IF EXISTS youthconnect_user;

-- Exit and re-run setup
\q
```

Then run the setup script again.

---

## 📚 **Useful SQL Commands**

### **List All Tables:**
```sql
-- Connect to database first
\c youthconnect_auth

-- List tables
\dt

-- List with details
\dt+
```

### **Check Flyway History:**
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### **Database Sizes:**
```sql
SELECT 
    datname AS database_name,
    pg_size_pretty(pg_database_size(datname)) AS size
FROM pg_database
WHERE datname LIKE 'youthconnect_%'
ORDER BY pg_database_size(datname) DESC;
```

### **Active Connections:**
```sql
SELECT 
    datname AS database,
    usename AS user,
    count(*) AS connections
FROM pg_stat_activity
WHERE datname LIKE 'youthconnect_%'
GROUP BY datname, usename
ORDER BY datname;
```

---

## 🔐 **Security Notes**

### **Development:**
- ✅ Default password (`YouthConnect2024!`) is acceptable
- ✅ Single user for all databases simplifies development

### **Production:**
- ❌ **NEVER** use default passwords!
- ✅ Use strong, randomly generated passwords
- ✅ Store passwords in secrets manager (AWS Secrets Manager, Vault)
- ✅ Consider separate users per service for better isolation
- ✅ Enable SSL/TLS for database connections
- ✅ Use connection pooling (HikariCP - already configured)
- ✅ Regular password rotation (quarterly)
- ✅ Network isolation (private subnets)

---

## 📞 **Support & Resources**

### **Documentation:**
- PostgreSQL Docs: https://www.postgresql.org/docs/
- Flyway Docs: https://flywaydb.org/documentation/
- Spring Boot + PostgreSQL: https://spring.io/guides/gs/accessing-data-postgresql/

### **Quick Reference:**
- See `POSTGRES_COMMANDS.md` for command cheat sheet

### **Contact:**
- Developer: Douglas Kings Kato
- Email: douglaskings2@gmail.com

---

## 📝 **Changelog**

### **Version 3.1.0** (2025-11-15)
- ✅ Complete automated setup scripts
- ✅ Windows batch file for one-click setup
- ✅ PowerShell script with error handling
- ✅ Comprehensive documentation
- ✅ Verification and troubleshooting guides

### **Version 3.0.0** (2025-11-12)
- ✅ Initial PostgreSQL migration from MySQL
- ✅ Database-per-service architecture
- ✅ Flyway migration support
- ✅ Extension installation

---

**Last Updated:** November 15, 2025  
**Version:** 3.1.0  
**Database:** PostgreSQL 16.2