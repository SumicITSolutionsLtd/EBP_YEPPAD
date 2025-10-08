# Service Registry (Eureka Server)

## Overview

The Service Registry is the central service discovery component for the YouthConnect Uganda platform. Built on Netflix Eureka, it enables dynamic service discovery and load balancing across all microservices.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker (optional)

### Running Locally
```bash
# Start the service
mvn spring-boot:run

# Access dashboard
open http://localhost:8761

#Running with Docker
# Build image
docker build -t youthconnect/service-registry:2.0.0 .

# Run container
docker-compose up -d

#Health Checks
# Liveness
curl http://localhost:8761/actuator/health/liveness

# Readiness
curl http://localhost:8761/actuator/health/readiness

# Detailed health
curl http://localhost:8761/actuator/health

# Monitoring
# Prometheus Metrics
curl http://localhost:8761/actuator/prometheus

Troubleshooting
Services not registering

Check network connectivity
Verify eureka.client.service-url.defaultZone
Check authentication credentials

High memory usage

Adjust response cache settings
Reduce peer replication frequency
Increase JVM heap size

Security
Production Checklist:

 Change default credentials
 Enable HTTPS/TLS
 Restrict network