# Youth Connect Uganda - User Service Dockerfile
# Multi-stage build for optimized production image

# Stage 1: Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application
COPY src ./src
RUN mvn clean package -DskipTests -Pprod

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create application user (security best practice)
RUN groupadd -r youthconnect && useradd -r -g youthconnect youthconnect

# Set working directory
WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/user-service-*.jar app.jar

# Create necessary directories and set permissions
RUN mkdir -p /app/logs /app/uploads && \
    chown -R youthconnect:youthconnect /app

# Switch to non-root user
USER youthconnect

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Expose application port
EXPOSE 8081

# JVM configuration for production
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

# Labels for better image management
LABEL maintainer="dev@youthconnect.ug"
LABEL version="1.0.0"
LABEL description="Youth Connect Uganda User Service - User management microservice"