# =============================================================================
# Multi-stage Dockerfile for Mentor Service
# =============================================================================
# Stage 1: Build stage
# Uses Maven to compile and package the application
# =============================================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Package application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# =============================================================================
# Stage 2: Runtime stage
# Minimal JRE image for running the application
# =============================================================================
FROM eclipse-temurin:17-jre-alpine

# Metadata
LABEL maintainer="Douglas Kings Kato <dev@youthconnect.ug>"
LABEL service="mentor-service"
LABEL version="2.0.0"

# Create application user (security best practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to application user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8090/actuator/health || exit 1

# JVM optimization flags
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+OptimizeStringConcat -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]