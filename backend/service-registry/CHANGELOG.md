# Changelog

All notable changes to the Service Registry (Eureka Server) will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2025-01-29

### Added
- **Production-Ready Configuration**
    - Environment-specific profiles (dev, prod, docker)
    - Comprehensive security configuration with Spring Security
    - Custom health indicators for Eureka status monitoring
    - Prometheus metrics integration

- **Kubernetes Support**
    - Complete K8s manifests (Deployment, Service, ConfigMap, HPA)
    - Horizontal Pod Autoscaler with CPU/memory metrics
    - Pod Disruption Budget for high availability
    - RBAC configuration for service account

- **Docker Support**
    - Multi-stage Dockerfile for optimized images
    - Docker Compose configuration
    - Health checks and resource limits

- **Monitoring & Observability**
    - Custom Eureka health indicator
    - Actuator endpoints configuration
    - Structured logging with Logback
    - Prometheus metrics export

- **Operational Scripts**
    - Development startup script (`start-dev.sh`)
    - Production startup script (`start-prod.sh`)
    - Comprehensive health check script

- **UI Enhancements**
    - Custom branded dashboard CSS
    - Welcome page with system information
    - Responsive design improvements

### Changed
- Upgraded to Spring Boot 3.2.x
- Upgraded to Spring Cloud 2023.0.0
- Improved JVM configuration for production
- Enhanced logging patterns and rotation

### Security
- Implemented Basic Authentication for Eureka dashboard
- Added security context for Docker/K8s deployments
- Environment-based credential management
- Non-root user execution in containers

## [1.0.0] - 2024-09-01

### Added
- Initial Eureka Server implementation
- Basic Spring Boot configuration
- Simple Docker support
- Development environment setup

### Dependencies
- Spring Boot 3.2.0
- Spring Cloud Netflix Eureka Server
- Spring Boot Actuator
- Micrometer Prometheus Registry

---

## Upgrade Guide

### From 1.0.0 to 2.0.0

**Breaking Changes:**
1. Security is now enabled by default in production profile
    - Set `EUREKA_USERNAME` and `EUREKA_PASSWORD` environment variables
    - Update client configurations to include credentials in service URLs

2. Configuration structure changes
    - Profile-specific configurations now in separate files
    - Some property names updated for consistency

**Migration Steps:**
1. Update environment variables in deployment configurations
2. Review and update `application.yml` if customized
3. Update client applications to use authenticated service URLs
4. Test in development environment before production deployment

---

## Support

For issues, questions, or contributions:
- **Email:** douglaskings2@gmail.com
- **Team:** Platform Engineering Team
- **Repository:** Internal GitLab

---

## License

Proprietary - Kwetu-Hub Uganda Platform
© 2025 YouthConnect Uganda. All rights reserved.