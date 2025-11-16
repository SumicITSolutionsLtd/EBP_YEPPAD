# 🗂️ PostgreSQL Quick Reference Commands

## 📁 **File Location**
Save this file as: `backend/scripts/POSTGRES_COMMANDS.md`

---

## 🚀 **One-Line Setup (Automated)**

```powershell
# Run the complete automated setup script
.\scripts\setup-postgres.ps1
```

---

## 🔧 **Manual Setup Commands**

### **Step 1: Navigate to Backend Directory**
```powershell
cd "F:\Douglas Kings\Hackthon\EBP_YEPPAD\backend"
```

### **Step 2: Set UTF-8 Encoding**
```powershell
$env:PGCLIENTENCODING = "UTF8"
```

### **Step 3: Clean Up Previous Installation (Optional)**
```powershell
# Connect to PostgreSQL
& "F:\Installations\PostgreSql\bin\psql.exe" -U postgres

# Inside psql, run:
DROP DATABASE IF EXISTS youthconnect_auth;
DROP DATABASE IF EXISTS youthconnect_user;
DROP DATABASE IF EXISTS youthconnect_job;
DROP DATABASE IF EXISTS youthconnect_opportunity;
DROP DATABASE IF EXISTS youthconnect_mentor;
DROP DATABASE IF EXISTS youthconnect_content;
DROP DATABASE IF EXISTS youthconnect_notification;
DROP DATABASE IF EXISTS youthconnect_file;
DROP DATABASE IF EXISTS youthconnect_ai;
DROP DATABASE IF EXISTS youthconnect_analytics;
DROP DATABASE IF EXISTS youthconnect_ussd;
DROP ROLE IF EXISTS youthconnect_user;

# Exit psql
\q
```

### **Step 4: Run Initialization Script**
```powershell
& "F:\Installations\PostgreSql\bin\psql.exe" -U postgres -f scripts\init-databases.sql
```

**Password:** `Douglas20!`

---

## ✅ **Verification Commands**

### **List All Databases**
```powershell
& "F:\Installations\PostgreSql\bin\psql.exe" -U postgres -c "SELECT datname FROM pg_database WHERE datname LIKE 'youthconnect_%' ORDER BY datname;"
```

### **Connect to Specific Database**
```powershell
# Auth Service
& "F:\Installations\PostgreSql\bin\psql.exe" -U youthconnect_user -d youthconnect_auth

# Job Service
& "F:\Installations\PostgreSql\bin\psql.exe" -U youthconnect_user -d youthconnect_job

# User Service
& "F:\Installations\PostgreSql\bin\psql.exe" -U youthconnect_user -d youthconnect_user
```

### **List Tables in Database**
```sql
-- After connecting to a database
\dt

-- List with details
\dt+

-- Exit
\q
```

### **Check Flyway Migration History**
```sql
-- After connecting to a database
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

---

## 🔑 **Database Credentials**

```
Username: youthconnect_user
Password: YouthConnect2024!
Host:     localhost
Port:     5432
```

---

## 🗄️ **Database List**

| Database Name | Purpose | Port |
|--------------|---------|------|
| `youthconnect_auth` | Authentication & Authorization | - |
| `youthconnect_user` | User Profiles & Management | - |
| `youthconnect_job` | Job Opportunities | - |
| `youthconnect_opportunity` | Business Opportunities | - |
| `youthconnect_mentor` | Mentorship Programs | - |
| `youthconnect_content` | Educational Content | - |
| `youthconnect_notification` | Notifications | - |
| `youthconnect_file` | File Management | - |
| `youthconnect_ai` | AI Recommendations | - |
| `youthconnect_analytics` | Analytics & BI | - |
| `youthconnect_ussd` | USSD Service | - |

---

## 🔗 **Connection Strings**

### **JDBC URLs**
```
jdbc:postgresql://localhost:5432/youthconnect_auth
jdbc:postgresql://localhost:5432/youthconnect_user
jdbc:postgresql://localhost:5432/youthconnect_job
jdbc:postgresql://localhost:5432/youthconnect_opportunity
jdbc:postgresql://localhost:5432/youthconnect_mentor
jdbc:postgresql://localhost:5432/youthconnect_content
jdbc:postgresql://localhost:5432/youthconnect_notification
jdbc:postgresql://localhost:5432/youthconnect_file
jdbc:postgresql://localhost:5432/youthconnect_ai
jdbc:postgresql://localhost:5432/youthconnect_analytics
jdbc:postgresql://localhost:5432/youthconnect_ussd
```

### **Environment Variables (.env format)**
```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=youthconnect_auth  # Change per service
DB_USERNAME=youthconnect_user
DB_PASSWORD=YouthConnect2024!
```

---

## 🚀 **Start Services Commands**

### **Service Registry (Eureka)**
```powershell
cd service-registry
mvn clean install -DskipTests
mvn spring-boot:run
```
**Verify:** http://localhost:8761

### **Auth Service**
```powershell
cd auth-service
mvn clean install -DskipTests
mvn spring-boot:run
```
**Port:** 8083

### **User Service**
```powershell
cd user-service
mvn clean install -DskipTests
mvn spring-boot:run
```
**Port:** 8084

### **Job Service**
```powershell
cd job-service
mvn clean install -DskipTests
mvn spring-boot:run
```
**Port:** 8000

### **API Gateway**
```powershell
cd api-gateway
mvn clean install -DskipTests
mvn spring-boot:run
```
**Port:** 8088

---

## 🛠️ **Troubleshooting Commands**

### **Check PostgreSQL Status**
```powershell
# Windows Service
Get-Service postgresql*

# Or check if listening on port
netstat -an | findstr :5432
```

### **Restart PostgreSQL**
```powershell
# Windows Services
Restart-Service postgresql-x64-16  # Adjust service name
```

### **View PostgreSQL Logs**
```
Location: C:\Program Files\PostgreSQL\16\data\log
```

### **Reset Password (if needed)**
```sql
-- Connect as postgres
psql -U postgres

-- Reset password
ALTER USER youthconnect_user WITH PASSWORD 'NewPassword123!';
```

### **Flyway Repair (if migration fails)**
```powershell
cd <service-directory>
mvn flyway:repair
```

### **Flyway Clean (DANGER: Deletes all data!)**
```powershell
cd <service-directory>
mvn flyway:clean
```

---

## 📊 **Useful SQL Queries**

### **List All Extensions**
```sql
\dx
-- or
SELECT * FROM pg_extension;
```

### **Database Size**
```sql
SELECT 
    datname AS database_name,
    pg_size_pretty(pg_database_size(datname)) AS size
FROM pg_database
WHERE datname LIKE 'youthconnect_%'
ORDER BY pg_database_size(datname) DESC;
```

### **Table Sizes**
```sql
SELECT 
    schemaname AS schema_name,
    tablename AS table_name,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### **Check Active Connections**
```sql
SELECT 
    datname AS database,
    usename AS user,
    application_name,
    client_addr,
    state,
    query
FROM pg_stat_activity
WHERE datname LIKE 'youthconnect_%'
ORDER BY datname;
```

---

## 🗑️ **Complete Cleanup (Nuclear Option)**

**⚠️ WARNING: This deletes EVERYTHING!**

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

-- Verify cleanup
\l
```

---

## 📝 **Notes**

1. **Always backup before cleanup:**
   ```powershell
   pg_dump -U youthconnect_user -d youthconnect_auth > backup_auth.sql
   ```

2. **Change default password in production!**

3. **Use separate users per service in production for better security.**

4. **Enable SSL/TLS for production databases.**

5. **Regular database maintenance:**
   ```sql
   VACUUM ANALYZE;
   REINDEX DATABASE youthconnect_auth;
   ```

---

## 🔗 **Useful Links**

- PostgreSQL Docs: https://www.postgresql.org/docs/
- Flyway Docs: https://flywaydb.org/documentation/
- Spring Boot + PostgreSQL: https://spring.io/guides/gs/accessing-data-postgresql/

---

**Last Updated:** November 15, 2025  
**Version:** 3.1.0