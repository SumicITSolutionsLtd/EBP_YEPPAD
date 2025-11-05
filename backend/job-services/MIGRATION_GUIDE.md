# Job Service Migration Guide
## API Gateway Authentication Integration

**Version:** 3.0.0  
**Date:** November 2025  
**Author:** Douglas Kings Kato

---

## 🎯 Migration Overview

This guide walks you through migrating the Job Service from **internal JWT authentication** to **API Gateway-based authentication**, ensuring compliance with the Backend Microservices Development Guidelines.

---

## 📋 Pre-Migration Checklist

- [ ] Backup current codebase
- [ ] Backup database
- [ ] Review current authentication flow
- [ ] Ensure API Gateway is running and configured
- [ ] Test current functionality (baseline)

---

## 🔄 Migration Steps

### **Step 1: Remove Old Security Files**

Delete the following files (no longer needed):

```bash
# Navigate to job-services directory
cd job-services/src/main/java/com/youthconnect/job_services/security/

# Delete old JWT files
rm JwtAuthenticationFilter.java
rm SecurityUtils.java  # If using old implementation
```

**Files to DELETE:**
- `security/JwtAuthenticationFilter.java`
- `security/SecurityUtils.java` (old version)

---

### **Step 2: Add New Security Files**

Create the following new files:

#### 2.1. GatewayUserContextUtil.java
```bash
# Location: src/main/java/com/youthconnect/job_services/security/
```

Copy the `GatewayUserContextUtil.java` from the artifacts above.

#### 2.2. SecurityConfig.java (Replace)
```bash
# Location: src/main/java/com/youthconnect/job_services/config/
```

**REPLACE** the existing `SecurityConfig.java` with the new version from artifacts.

---

### **Step 3: Update Controllers**

Update all controllers to use `GatewayUserContextUtil`:

#### 3.1. JobController.java

**FIND:**
```java
@PostMapping
public ApiResponse<JobDetailResponse> createJob(
        @Valid @RequestBody CreateJobRequest request,
        @RequestHeader("X-User-Id") UUID userId,
        @RequestHeader("X-User-Role") String userRole
) {
```

**REPLACE WITH:**
```java
@PostMapping
public ApiResponse<JobDetailResponse> createJob(
        @Valid @RequestBody CreateJobRequest request
) {
    UUID userId = GatewayUserContextUtil.getCurrentUserId();
    String userRole = GatewayUserContextUtil.getCurrentUserRole();
```

Repeat for all endpoints in:
- `JobController.java`
- `JobApplicationController.java`
- `FileUploadController.java`

---

### **Step 4: Update Feign Clients**

Replace Long with UUID in Feign client interfaces:

#### 4.1. NotificationServiceClient.java
Copy from artifacts above.

#### 4.2. AIRecommendationClient.java
Copy from artifacts above.

#### 4.3. Update AIRecommendationClientFallback.java

**FIND:**
```java
public List<RecommendedJobDto> getJobRecommendations(Long userId, int limit) {
```

**REPLACE WITH:**
```java
public List<RecommendedJobDto> getJobRecommendations(UUID userId, int limit) {
```

---

### **Step 5: Update Service Layer**

Fix any remaining Long references:

#### 5.1. JobServiceImpl.java

**FIND:**
```java
private void sendApplicationNotifications(Job job, Long applicantUserId) {
```

**REPLACE WITH:**
```java
private void sendApplicationNotifications(Job job, UUID applicantUserId) {
```

---

### **Step 6: Update API Gateway Configuration**

#### 6.1. Update gateway application.yml

In `api-gateway` project:

```bash
cd ../api-gateway/src/main/resources/
```

**REPLACE** the job service routes section with the configuration from artifacts.

**Key changes:**
- Service name: `lb://job-services` (consistent)
- Add all job service endpoints
- Configure public vs authenticated routes
- Add fallback routes

---

### **Step 7: Update Dependencies**

#### 7.1. Check pom.xml

Ensure you have:
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT (OPTIONAL - only for header validation) -->
<!-- Can be removed if not used -->
```

**REMOVE** if present and not needed:
```xml
<!-- JWT token generation dependencies -->
<!-- No longer needed as auth is handled by gateway -->
```

---

### **Step 8: Update Application Properties**

#### 8.1. application.yml

**REMOVE** (if present):
```yaml
app:
  security:
    jwt:
      secret: ...
      access-token-expiry: ...
```

**ADD** (if not present):
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

### **Step 9: Testing**

#### 9.1. Start Services in Order

```bash
# 1. Start Eureka Server
cd service-registry
mvn spring-boot:run

# 2. Start API Gateway
cd ../api-gateway
mvn spring-boot:run

# 3. Start Job Service
cd ../job-services
mvn spring-boot:run
```

#### 9.2. Test Endpoints

**Test Public Endpoints (No Auth):**
```bash
# Health check
curl http://localhost:8088/api/v1/jobs/search

# Featured jobs
curl http://localhost:8088/api/v1/jobs/featured

# Recent jobs
curl http://localhost:8088/api/v1/jobs/recent
```

**Test Authenticated Endpoints:**
```bash
# 1. Login to get JWT token
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# 2. Use token to access protected endpoint
curl http://localhost:8088/api/v1/jobs/my-jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 9.3. Verify Headers

Check that gateway adds headers:
```bash
# In job-service logs, you should see:
# "Extracted userId from header: <UUID>"
# "Extracted user role from header: <ROLE>"
```

---

## ✅ Validation Checklist

After migration, verify:

- [ ] Service registers with Eureka
- [ ] Public endpoints work without authentication
- [ ] Authenticated endpoints require JWT
- [ ] User context extracted correctly from headers
- [ ] No internal JWT validation (removed)
- [ ] API Gateway routes requests correctly
- [ ] Circuit breaker works (test by stopping job-service)
- [ ] Fallback responses returned on failure
- [ ] All UUID types consistent
- [ ] No Long ID references remain

---

## 🐛 Troubleshooting

### **Issue: "User context not found"**

**Cause:** Gateway not adding headers or service not extracting them.

**Solution:**
1. Check gateway logs for JWT validation
2. Verify gateway adds `X-User-Id` header
3. Check `GatewayUserContextUtil.getCurrentUserId()` returns UUID

---

### **Issue: "Service not found" (503 error)**

**Cause:** Service not registered with Eureka.

**Solution:**
1. Check Eureka dashboard: http://localhost:8761
2. Verify service name in application.yml matches gateway config
3. Restart services in order (Eureka → Gateway → Job Service)

---

### **Issue: "Cannot convert Long to UUID"**

**Cause:** Remaining Long references in code.

**Solution:**
1. Search project for: `Long userId`, `Long jobId`
2. Replace with `UUID userId`, `UUID jobId`
3. Update method signatures in services and repositories

---

### **Issue: "Unauthorized" on public endpoints**

**Cause:** SecurityConfig blocking public endpoints.

**Solution:**
1. Check `SecurityConfig.java` has `.permitAll()` for public paths
2. Verify `/health`, `/swagger-ui/**`, `/v3/api-docs/**` are public
3. Check gateway routes don't require auth for public endpoints

---

## 📊 Rollback Plan

If migration fails:

```bash
# 1. Stop all services
pkill -f "spring-boot"

# 2. Restore from backup
cd job-services
git checkout main  # or your stable branch

# 3. Restore old SecurityConfig
cp backups/SecurityConfig.java src/main/java/.../config/

# 4. Restart services
mvn spring-boot:run
```

---

## 📝 Post-Migration Tasks

- [ ] Update API documentation
- [ ] Update deployment scripts
- [ ] Update Docker Compose files
- [ ] Update CI/CD pipelines
- [ ] Notify frontend team of changes
- [ ] Update monitoring dashboards
- [ ] Archive old authentication code

---

## 🔗 Related Documentation

- [Backend Microservices Development Guidelines](./BACKEND_GUIDELINES.md)
- [API Gateway Configuration](../api-gateway/README.md)
- [User Service Authentication](../user-service/AUTHENTICATION.md)

---

## 📞 Support

If you encounter issues during migration:

**Contact:** Douglas Kings Kato  
**Email:** support@youthconnect.ug  
**Slack:** #job-service-migration

---

**Last Updated:** November 2025  
**Migration Version:** 3.0.0