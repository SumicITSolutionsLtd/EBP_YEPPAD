# Youth Connect Uganda - User Service

A comprehensive microservice for user management in the Youth Connect Uganda platform, supporting multi-role authentication, USSD integration, and AI-powered recommendations.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- MySQL 8.0+ (running on port 3307)
- Maven 3.8+
- Redis (optional, for caching)

### 1. Clone and Setup
```bash
git clone <your-repo-url>
cd user-service
```

### 2. Database Setup
```sql
-- Connect to MySQL on port 3307
mysql -u root -p -P 3307

-- Create database
CREATE DATABASE youth_connect_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Run the complete schema from your documents
SOURCE /path/to/your/schema.sql;
```

### 3. Environment Configuration
Create `.env` file in the project root:
```bash
# Copy the provided .env configuration
cp .env.example .env
```

### 4. Run the Application
```bash
# Development mode
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=development
```

## 🏗️ Architecture Overview

### Core Components
- **AuthService**: User registration, authentication, JWT management
- **UserService**: User profile management across all roles
- **NotificationService**: SMS/Email notifications
- **AIRecommendationService**: Personalized recommendations
- **FileManagementService**: File upload and storage

### Supported User Roles
- **YOUTH**: Young entrepreneurs seeking opportunities
- **MENTOR**: Experienced professionals providing guidance
- **NGO**: Organizations offering programs
- **FUNDER**: Entities providing financial support
- **SERVICE_PROVIDER**: Professional service providers
- **ADMIN**: Platform administrators

## 🔧 Configuration

### Database Connection
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/youth_connect_db
    username: root
    password: Douglas20!
```

### JWT Security
```yaml
jwt:
  secret: aVeryLongAndSecureSecretKey...
  expiration: 36000000  # 10 hours
  refresh:
    expiration: 604800000  # 7 days
```

### CORS Configuration
```yaml
app:
  security:
    cors:
      allowed-origins:
        - http://localhost:3000
        - https://youthconnect.ug
```

## 📡 API Endpoints

### Authentication
```http
POST /api/v1/api/auth/register
POST /api/v1/api/auth/register-ussd
POST /api/v1/api/auth/login
POST /api/v1/api/auth/login-ussd
POST /api/v1/api/auth/refresh-token
POST /api/v1/api/auth/logout
GET  /api/v1/api/auth/health
```

### User Management
```http
GET  /api/v1/api/users/me
PUT  /api/v1/api/users/me/profile
GET  /api/v1/api/users/profile/phone/{phoneNumber}
PUT  /api/v1/api/users/profile/phone/{phoneNumber}
GET  /api/v1/api/users/mentors
```

### USSD Integration
```http
POST /api/v1/api/ussd/at-ussd
```

## 🔒 Security Features

### JWT Authentication
- Secure token generation with HS512
- Refresh token support
- Token blacklisting on logout
- Role-based access control

### Input Validation
- Phone number validation for Uganda (+256)
- Email format validation
- Name validation supporting local conventions
- Password strength requirements

### Rate Limiting
- Request rate limiting per IP
- Separate limits for auth endpoints
- Configurable burst capacity

## 📱 USSD Integration

### Registration Flow
```
*256# -> Welcome to Youth Connect Uganda!
      -> Enter First Name
      -> Enter Last Name  
      -> Select Gender
      -> Select Age Group
      -> Enter District
      -> Select Business Stage
      -> Registration Complete!
```

### Features
- Mobile-first registration
- Multi-language support
- SMS confirmations
- Progress tracking

## 🤖 AI Recommendations

### Capabilities
- Personalized opportunity matching
- Content recommendations
- Mentor compatibility scoring
- Success probability prediction
- User behavior analysis

### Data Sources
- User profiles and interests
- Activity logs
- Application history
- Geographic data
- Role-based preferences

## 🔄 Microservices Integration

### Service Discovery
- Eureka client registration
- Health checks
- Load balancing

### Inter-Service Communication
- RESTful APIs
- Circuit breakers
- Retry mechanisms
- Timeout handling

## 📊 Monitoring & Observability

### Health Checks
```http
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

### Logging
- Structured logging with correlation IDs
- Security event logging
- Performance metrics
- Error tracking

### Metrics
- Custom business metrics
- JVM metrics
- Database connection pool metrics
- Cache statistics

## 🧪 Testing

### Run Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# With coverage
mvn clean test jacoco:report
```

### Test Profiles
```bash
# Test with H2 database
mvn test -Dspring.profiles.active=test
```

## 🚀 Deployment

### Docker
```bash
# Build image
docker build -t youthconnect-user-service .

# Run container
docker run -p 8081:8081 \
  -e DB_HOST=mysql \
  -e DB_PORT=3307 \
  youthconnect-user-service
```

### Production Checklist
- [ ] Update JWT secret
- [ ] Configure SSL/TLS
- [ ] Set up monitoring
- [ ] Configure backup strategy
- [ ] Enable security auditing
- [ ] Set up log aggregation

## 🔧 Troubleshooting

### Common Issues

#### Database Connection Failed
```bash
# Check MySQL is running on port 3307
netstat -an | grep 3307

# Test connection
mysql -h localhost -P 3307 -u root -p
```

#### JWT Token Issues
```bash
# Check token format
curl -H "Authorization: Bearer <token>" http://localhost:8081/api/v1/api/users/me
```

#### USSD Registration Failed
```bash
# Check logs
tail -f logs/user-service.log | grep USSD
```

### Debug Mode
```bash
# Enable debug logging
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dlogging.level.com.youthconnect.user_service=DEBUG"
```

## 📁 Project Structure
```
user-service/
├── src/main/java/com/youthconnect/user_service/
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   ├── service/         # Business logic
│   ├── repository/      # Data access
│   ├── entity/         # JPA entities
│   ├── dto/           # Data transfer objects
│   ├── util/         # Utilities
│   ├── validation/   # Custom validators
│   ├── security/     # Security components
│   └── exception/    # Custom exceptions
├── src/main/resources/
│   ├── application.yml
│   └── static/
├── src/test/
└── target/
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- Email: dev@youthconnect.ug
- Documentation: [API Docs](http://localhost:8081/swagger-ui.html)
- Issues: GitHub Issues

## 🔄 Version History

- **1.0.0** - Initial release with core functionality
    - Multi-role authentication
    - USSD integration
    - AI recommendations
    - Comprehensive security