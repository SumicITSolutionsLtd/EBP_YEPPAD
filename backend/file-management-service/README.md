# File Management Service 🗂️

**Version:** 2.1.0 (Production-Ready)  
**Database:** PostgreSQL 15+  
**Java:** 21  
**Spring Boot:** 3.5.6

## ✅ All Issues Fixed

### 🔧 Fixed Issues

1. **✅ JJWT API Fixed** - Updated from deprecated `parserBuilder()` to modern `parser()` API (0.12.x)
2. **✅ PostgreSQL Migration** - Changed from MySQL to PostgreSQL throughout
3. **✅ UUID Support** - All userId fields now use UUID instead of Long
4. **✅ Pagination Added** - All list endpoints return paginated responses
5. **✅ Security Hardened** - JWT authentication, CORS, and validation
6. **✅ Well-Structured** - Clean separation of concerns, proper layering

---

## 📋 Overview

Microservice for handling all file operations in the Youth Entrepreneurship Platform.

### Key Features

- 📸 **Profile Pictures** - Upload with automatic optimization (thumbnail, medium, original)
- 🎵 **Audio Modules** - Multi-language learning content (English, Luganda, Alur, Lugbara)
- 📄 **Documents** - CVs, certificates, application attachments
- 🔒 **Security** - JWT authentication, file validation, virus scanning ready
- 📊 **Pagination** - All list endpoints return paginated responses
- 🗄️ **PostgreSQL** - Production-ready database with connection pooling
- 🔄 **Async Processing** - Background file processing for large uploads

---

## 🏗️ Architecture

```
file-management-service/
├── src/main/java/com/youthconnect/file/service/
│   ├── config/           # Configuration classes
│   │   ├── AsyncConfiguration.java
│   │   ├── FileStorageProperties.java
│   │   ├── JwtProperties.java
│   │   ├── OpenApiConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/       # REST endpoints
│   │   └── FileManagementController.java
│   ├── dto/              # Data Transfer Objects
│   │   ├── FileMetadata.java
│   │   ├── FileRecordDto.java
│   │   ├── FileUploadResult.java
│   │   └── PagedResponse.java
│   ├── entity/           # JPA entities
│   │   └── FileRecord.java
│   ├── exception/        # Custom exceptions
│   │   ├── FileNotFoundException.java
│   │   ├── FileStorageException.java
│   │   ├── InvalidFileException.java
│   │   └── GlobalExceptionHandler.java
│   ├── repository/       # Data access
│   │   └── FileRecordRepository.java
│   ├── security/         # JWT authentication
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtTokenProvider.java (✅ FIXED)
│   ├── service/          # Business logic
│   │   └── FileManagementService.java
│   ├── util/             # Utilities
│   │   └── FileValidationUtil.java
│   └── FileManagementServiceApplication.java
├── src/main/resources/
│   ├── application.yml           # Main configuration (✅ PostgreSQL)
│   ├── application-dev.yml       # Development profile
│   ├── application-prod.yml      # Production profile
│   └── db/migration/
│       ├── V1__Initial_File_Schema.sql
│       └── V2__File_Maintenance_Functions.sql
├── pom.xml                       # Maven dependencies (✅ PostgreSQL)
├── Dockerfile                    # Docker image
├── docker-compose.yml            # Docker Compose setup
└── README.md                     # This file
```

---

## 🚀 Quick Start

### Prerequisites

- ✅ JDK 21
- ✅ Maven 3.9+
- ✅ PostgreSQL 15+
- ✅ Eureka Server (running on port 8761)

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE epb_file
    WITH ENCODING 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- Create application user
CREATE USER epb_app_user WITH PASSWORD 'YourSecurePassword2025!';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE epb_file TO epb_app_user;

-- Connect to database and grant schema privileges
\c epb_file
GRANT ALL ON SCHEMA public TO epb_app_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO epb_app_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO epb_app_user;
```

### 2. Run Migrations

```bash
# Migrations are in src/main/resources/db/migration/
# Run them manually or use Flyway:

psql -U epb_app_user -d epb_file -f src/main/resources/db/migration/V1__Initial_File_Schema.sql
psql -U epb_app_user -d epb_file -f src/main/resources/db/migration/V2__File_Maintenance_Functions.sql
```

### 3. Configure Environment Variables

```bash
# Create .env file (development)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=epb_file
DB_USER=epb_app_user
DB_PASSWORD=YourSecurePassword2025!

# JWT (must match API Gateway!)
JWT_SECRET=youth-connect-secure-secret-key-2025-minimum-256-bits-required-for-production

# Storage
STORAGE_TYPE=LOCAL
STORAGE_PATH=uploads/
BASE_URL=http://localhost:8089

# Eureka
EUREKA_HOST=localhost
EUREKA_PORT=8761
EUREKA_USERNAME=admin
EUREKA_PASSWORD=changeme
```

### 4. Build & Run

```bash
# Build
mvn clean install

# Run (development)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run (production)
java -jar target/file-management-service-2.1.0.jar --spring.profiles.active=prod
```

---

## 🐳 Docker Deployment

### Build Image

```bash
docker build -t file-management-service:2.1.0 .
```

### Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f file-management-service

# Stop services
docker-compose down
```

---

## 📡 API Endpoints

### 🔓 Public Endpoints (No Authentication)

```
GET  /api/files/health                      # Health check
GET  /api/files/download/public/**          # Public file downloads
GET  /api/files/download/modules/**         # Learning module downloads
GET  /actuator/health                       # Actuator health
GET  /swagger-ui.html                       # API documentation
```

### 🔐 Protected Endpoints (JWT Required)

#### Upload Operations

```http
POST /api/files/profile-picture/{userId}
Content-Type: multipart/form-data
Authorization: Bearer {token}

Parameters:
  - file: MultipartFile (JPG, PNG, GIF - max 10MB)

Response:
{
  "success": true,
  "message": "Profile picture uploaded successfully",
  "data": {
    "fileName": "profile_123e4567_20250111.jpg",
    "fileUrl": "http://localhost:8089/api/files/download/...",
    "optimizedVersions": {
      "thumbnail": "...",
      "medium": "...",
      "original": "..."
    }
  }
}
```

```http
POST /api/files/audio-module
Content-Type: multipart/form-data
Authorization: Bearer {token}

Parameters:
  - moduleKey: string (e.g., "intro_entrepreneurship")
  - language: string (en, lg, lur, lgb)
  - file: MultipartFile (MP3, WAV, M4A - max 100MB)
```

```http
POST /api/files/document/{userId}
Content-Type: multipart/form-data
Authorization: Bearer {token}

Parameters:
  - documentType: string (CV, CERTIFICATE, LICENSE, etc.)
  - file: MultipartFile (PDF, DOC, DOCX - max 50MB)
```

#### Retrieval Operations

```http
GET /api/files/user/{userId}?page=0&size=20&category=DOCUMENT
Authorization: Bearer {token}

Response: (Paginated)
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 45,
  "totalPages": 3,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

```http
GET /api/files/audio-module/{moduleKey}
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": {
    "moduleKey": "intro_entrepreneurship",
    "audioFiles": {
      "en": "http://localhost:8089/api/files/download/modules/...",
      "lg": "http://localhost:8089/api/files/download/modules/...",
      "lur": "http://localhost:8089/api/files/download/modules/..."
    },
    "count": 3
  }
}
```

#### Management Operations

```http
DELETE /api/files/{userId}/{fileName}?category=DOCUMENT
Authorization: Bearer {token}
```

---

## 🗄️ Database Schema

### Main Tables

#### `file_records`
```sql
file_id             BIGSERIAL PRIMARY KEY
user_id             UUID                    -- ✅ UUID instead of BIGINT
file_name           VARCHAR(255) NOT NULL
original_name       VARCHAR(255)
file_path           VARCHAR(500) NOT NULL
file_size           BIGINT
content_type        VARCHAR(100)
file_category       file_category NOT NULL  -- ENUM
is_public           BOOLEAN DEFAULT FALSE
is_active           BOOLEAN DEFAULT TRUE
upload_time         TIMESTAMP NOT NULL
last_accessed       TIMESTAMP
created_at          TIMESTAMP DEFAULT NOW()
updated_at          TIMESTAMP DEFAULT NOW()
```

#### `file_metadata`
Extended metadata for different file types (images, audio, video, documents).

#### `file_versions`
Optimized file variants (thumbnails, compressed audio, etc.).

#### `file_access_logs`
Access tracking for analytics and security auditing.

### Indexes

- `idx_file_user_category` - User file lookups
- `idx_file_name` - Duplicate detection
- `idx_file_upload_time` - Chronological sorting
- `idx_file_public` - Public file access
- `idx_file_last_accessed` - Cold storage decisions

---

## 🔒 Security

### Authentication Flow

1. **Client** → Sends JWT token in `Authorization: Bearer {token}` header
2. **JwtAuthenticationFilter** → Extracts and validates token
3. **JwtTokenProvider** → Parses userId (UUID), username, roles
4. **Spring Security** → Sets authentication context
5. **Controller** → Access granted to protected endpoints

### Authorization Rules

- `@PreAuthorize("authentication.principal.toString() == #userId.toString()")` - User can only access their own files
- `@PreAuthorize("hasAnyRole('NGO', 'ADMIN')")` - Only NGO/ADMIN can upload learning modules

### File Validation

- ✅ File type checking (MIME type + extension)
- ✅ File size limits (10MB images, 50MB documents, 100MB audio)
- ✅ Filename sanitization (prevents path traversal)
- ✅ Content type verification with Apache Tika
- 🔄 Virus scanning (ClamAV integration planned)

---

## 📊 Monitoring

### Health Checks

```bash
# Service health
curl http://localhost:8089/api/files/health

# Actuator health (detailed)
curl http://localhost:8089/actuator/health
```

### Metrics

```bash
# Prometheus metrics
curl http://localhost:8089/actuator/prometheus

# Application metrics
curl http://localhost:8089/actuator/metrics
```

### Logs

```bash
# View logs
tail -f logs/file-service.log

# Docker logs
docker-compose logs -f file-management-service
```

---

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# With coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## 🔧 Troubleshooting

### Issue: "Cannot resolve method 'parserBuilder' in 'Jwts'"

**✅ FIXED:** Updated to JJWT 0.12.x API. Use `Jwts.parser()` instead of `Jwts.parserBuilder()`.

### Issue: Database connection refused

```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Check connection
psql -U epb_app_user -d epb_file -h localhost

# Verify credentials
echo $DB_PASSWORD
```

### Issue: File upload fails with 413 (Payload Too Large)

```yaml
# Increase limits in application.yml
server:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 200MB
```

### Issue: JWT validation fails

```bash
# Verify JWT secret matches API Gateway
echo $JWT_SECRET

# Check token in debugger: https://jwt.io
```

---

## 🚀 Future Enhancements

- [ ] AWS S3 integration
- [ ] MinIO support
- [ ] Image optimization with Thumbnailator
- [ ] Audio transcoding with FFmpeg
- [ ] Virus scanning with ClamAV
- [ ] CDN integration (CloudFront)
- [ ] File encryption at rest
- [ ] Duplicate file detection (hash-based)

---

## 📝 Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_HOST` | Yes | localhost | PostgreSQL host |
| `DB_PORT` | Yes | 5432 | PostgreSQL port |
| `DB_NAME` | Yes | epb_file | Database name |
| `DB_USER` | Yes | epb_app_user | Database user |
| `DB_PASSWORD` | Yes | - | Database password |
| `JWT_SECRET` | Yes | - | JWT signing secret (256+ bits) |
| `STORAGE_TYPE` | No | LOCAL | Storage backend (LOCAL/S3/MINIO) |
| `STORAGE_PATH` | No | uploads/ | File storage path |
| `BASE_URL` | No | http://localhost:8089 | Service base URL |
| `EUREKA_HOST` | Yes | localhost | Eureka server host |
| `EUREKA_PORT` | Yes | 8761 | Eureka server port |

---

## 👨‍💻 Author

**Douglas Kings Kato**  
File Management Service - Youth Entrepreneurship Platform

## 📄 License

MIT License - See LICENSE file for details

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [SpringDoc OpenAPI](https://springdoc.org/)

---

**Status:** ✅ Production-Ready  
**Version:** 2.1.0  
**Last Updated:** 2025-01-11