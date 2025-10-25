# ========================================
# Content Service - Multi-Stage Dockerfile
# ========================================
# This Dockerfile uses multi-stage builds to:
# 1. Build the application in a Maven container
# 2. Run the application in a lightweight JRE container
# 3. Optimize image size and security
#
# Author: Douglas Kings Kato
# Version: 1.0
# Date: 2025-01-01
# ========================================

# ========================================
# STAGE 1: BUILD
# ========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Set working directory
WORKDIR /app

# Copy Maven files first (for better caching)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
# Skip tests during Docker build (run separately in CI/CD)
RUN mvn clean package -DskipTests -B

# Verify JAR was created
RUN ls -la /app/target/

# ========================================
# STAGE 2: RUNTIME
# ========================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Add labels for container metadata
LABEL maintainer="damienpapers3@gmail.com"
LABEL service="content-service"
LABEL version="1.0.0"
LABEL description="Content Management Service - Learning Modules & Community"

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Create logs directory
RUN mkdir -p /var/log/content-service && \
    chown -R spring:spring /var/log/content-service

# Copy JAR from build stage
COPY --from=build /app/target/content-service-*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose service port
EXPOSE 8084

# Health check (checks actuator health endpoint)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# JVM optimization flags
ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseStringDeduplication \
    -XX:+PrintGCDetails \
    -XX:+PrintGCDateStamps \
    -Xloggc:/var/log/content-service/gc.log \
    -Duser.timezone=Africa/Nairobi \
    -Djava.security.egd=file:/dev/./urandom"

# Entry point with environment variable support
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

# ========================================
# BUILD INSTRUCTIONS
# ========================================
# Build image:
#   docker build -t content-service:1.0.0 .
#
# Run container (development):
#   docker run -p 8084:8084 \
#     -e SPRING_PROFILES_ACTIVE=dev \
#     -e DB_URL=jdbc:mysql://host.docker.internal:3307/epb_db \
#     content-service:1.0.0
#
# Run container (production):
#   docker run -p 8084:8084 \
#     -e SPRING_PROFILES_ACTIVE=prod \
#     -e DB_URL=jdbc:mysql://prod-db:3306/epb_db \
#     -e DB_USERNAME=epb_user \
#     -e DB_PASSWORD=secure_password \
#     -e EUREKA_SERVER_URL=http://eureka-server:8761/eureka/ \
#     --restart unless-stopped \
#     content-service:1.0.0
# ========================================