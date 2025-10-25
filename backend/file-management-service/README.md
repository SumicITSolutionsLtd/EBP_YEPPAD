# File Management Service

## Overview
Microservice for handling all file operations in the Youth Entrepreneurship Platform.

## Features
- ✅ Profile picture uploads with image optimization
- ✅ Audio file management for multi-language learning modules
- ✅ Document storage (CV, certificates, application attachments)
- ✅ File validation and security
- ✅ Database integration with MySQL
- ✅ Async processing for large files
- ✅ Storage abstraction (local/S3/MinIO support planned)

## Tech Stack
- **Spring Boot 3.5.6** - Framework
- **Java 21** - Language
- **MySQL 8.0** - Database
- **Apache Tika** - File type detection
- **Eureka Client** - Service discovery
- **Spring Data JPA** - Database operations

## API Endpoints

### Upload Operations
```
POST /api/files/profile-picture/{userId}
POST /api/files/audio-module
POST /api/files/document/{userId}
```

### Retrieval Operations
```
GET /api/files/audio-module/{moduleKey}
GET /api/files/download/{category}/{fileName}
GET /api/files/metadata/{category}/{fileName}
```

### Management Operations
```
DELETE /api/files/{userId}/{fileName}
GET /api/files/health
```

## Configuration

### Database Setup
```bash
mysql -u root -p
CREATE DATABASE epb_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Application Properties
Configure in `application.yml`:
- Database connection
- File storage path
- Maximum file sizes
- Allowed file types

## Running the Service

### Prerequisites
- JDK 21
- Maven 3.9+
- MySQL 8.0
- Eureka Server running on port 8761

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Docker (Optional)
```bash
docker build -t file-management-service .
docker run -p 8089:8089 file-management-service
```

## Storage Structure
```
uploads/
├── users/
│   └── {userId}/
│       ├── profile-pictures/
│       │   ├── profile_{userId}_{timestamp}.jpg
│       │   ├── thumb_profile_{userId}_{timestamp}.jpg
│       │   └── med_profile_{userId}_{timestamp}.jpg
│       └── documents/
│           └── {documentType}_{userId}_{timestamp}.pdf
└── modules/
    └── {moduleKey}/
        ├── {moduleKey}_en_{timestamp}.mp3
        ├── {moduleKey}_lg_{timestamp}.mp3
        ├── {moduleKey}_lur_{timestamp}.mp3
        └── {moduleKey}_lgb_{timestamp}.mp3
```

## Security
- ✅ File type validation
- ✅ File size limits
- ✅ Filename sanitization (path traversal prevention)
- ✅ Content type verification
- 🔄 Virus scanning (planned with ClamAV)
- 🔄 Encryption at rest (planned)

## File Size Limits
- **Images**: 10 MB
- **Documents**: 50 MB
- **Audio**: 100 MB

## Supported File Types

### Images
- JPG, JPEG, PNG, GIF

### Documents
- PDF, DOC, DOCX, TXT

### Audio
- MP3, WAV, M4A, MP4

## Monitoring
- **Health Check**: `GET /api/files/health`
- **Actuator**: `/actuator/health`, `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

## Database Schema
Uses the `file_records` table from the production schema:
- `file_id` (Primary Key)
- `user_id` (Foreign Key)
- `file_name`, `original_name`
- `file_path`, `file_size`
- `content_type`, `file_category`
- `is_public`, `is_active`
- `upload_time`, `last_accessed`

## Error Handling
- `FileStorageException` - Storage operation failures
- `FileNotFoundException` - File not found
- `InvalidFileException` - Validation failures
- `MaxUploadSizeExceededException` - Size limit exceeded

## Future Enhancements
- [ ] AWS S3 integration
- [ ] MinIO support
- [ ] Image optimization (Thumbnailator)
- [ ] Audio transcoding (FFmpeg)
- [ ] Virus scanning (ClamAV)
- [ ] CDN integration (CloudFront)
- [ ] File compression
- [ ] Encryption at rest

## Testing
```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Author
Douglas Kings Kato

## Version
1.0.0 (Production Ready - 85% Complete)