# =============================================================================
# API Gateway Dockerfile
# Multi-stage build for optimized production image
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build Stage
# Uses Maven to compile and package the application
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
# Tests should be run in CI/CD pipeline before Docker build
RUN mvn clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime Stage
# Creates minimal runtime image with only the JAR
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8088

# Health check
# Checks if the gateway is responding to health requests
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8088/health || exit 1

# Environment variables (with defaults)
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms512m -Xmx1024m" \
    JWT_SECRET="" \
    EUREKA_HOST=service-registry \
    EUREKA_PORT=8761

# Run the application
# Uses shell form to allow environment variable substitution
ENTRYPOINT sh -c "java ${JAVA_OPTS} -jar app.jar"

# =============================================================================
# BUILD INSTRUCTIONS:
#
# 1. Build the image:
#    docker build -t youthconnect/api-gateway:latest .
#
# 2. Run standalone (for testing):
#    docker run -d \
#      -p 8088:8088 \
#      -e JWT_SECRET=your-secret-key \
#      -e EUREKA_HOST=localhost \
#      --name api-gateway \
#      youthconnect/api-gateway:latest
#
# 3. Check logs:
#    docker logs -f api-gateway
#
# 4. Stop and remove:
#    docker stop api-gateway && docker rm api-gateway
#
# =============================================================================