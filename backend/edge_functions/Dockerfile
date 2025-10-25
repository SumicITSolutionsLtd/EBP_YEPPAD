# ==============================================================================
# Edge Functions Service - Multi-Stage Production Dockerfile
# Optimized for minimal image size and fast startup
# ==============================================================================

# ==============================================================================
# STAGE 1: Build Stage with Maven
# ==============================================================================
FROM maven:3.9.5-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /build

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (cached layer if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests for faster builds - run tests in CI/CD)
RUN mvn clean package -DskipTests -B && \
    # Verify JAR was created
    ls -lh target/*.jar

# ==============================================================================
# STAGE 2: Runtime Stage with JRE
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine

# ==============================================================================
# METADATA
# ==============================================================================
LABEL maintainer="Douglas Kings Kato <douglaskings@youthconnect.ug>" \
      service="edge-functions" \
      version="1.0.0" \
      description="Edge Functions Service - USSD, AI, Multi-Service Orchestration"

# ==============================================================================
# SYSTEM DEPENDENCIES
# ==============================================================================
# Install required packages for health checks and debugging
RUN apk add --no-cache \
    curl \
    bash \
    tzdata \
    && rm -rf /var/cache/apk/*

# ==============================================================================
# TIMEZONE CONFIGURATION
# ==============================================================================
# Set timezone to East Africa Time (Uganda)
ENV TZ=Africa/Kampala
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# ==============================================================================
# APPLICATION USER (Security Best Practice)
# ==============================================================================
# Create non-root user for running the application
RUN addgroup -g 1001 appgroup && \
    adduser -D -u 1001 -G appgroup appuser

# ==============================================================================
# APPLICATION SETUP
# ==============================================================================
# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /var/log/edge-functions && \
    chown -R appuser:appgroup /app /var/log/edge-functions

# Switch to non-root user
USER appuser

# ==============================================================================
# PORTS
# ==============================================================================
# Expose application port
EXPOSE 8091

# ==============================================================================
# HEALTH CHECK
# ==============================================================================
# Spring Boot Actuator health endpoint
HEALTHCHECK --interval=30s \
            --timeout=5s \
            --start-period=90s \
            --retries=3 \
    CMD curl -f http://localhost:8091/actuator/health || exit 1

# ==============================================================================
# JVM OPTIMIZATION
# ==============================================================================
# JVM flags optimized for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+ParallelRefProcEnabled \
               -XX:+UnlockExperimentalVMOptions \
               -XX:+UseCGroupMemoryLimitForHeap \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} \
               -Duser.timezone=Africa/Kampala"

# ==============================================================================
# ENTRYPOINT
# ==============================================================================
# Run application with JVM optimizations
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ==============================================================================
# USAGE EXAMPLES
# ==============================================================================
# Build:
#   docker build -t youthconnect/edge-functions:latest .
#
# Run (Development):
#   docker run -p 8091:8091 \
#              -e SPRING_PROFILES_ACTIVE=dev \
#              -e OPENAI_API_KEY=your-key-here \
#              youthconnect/edge-functions:latest
#
# Run (Production):
#   docker run -p 8091:8091 \
#              -e SPRING_PROFILES_ACTIVE=prod \
#              -e OPENAI_API_KEY=your-key-here \
#              -e EUREKA_URL=http://service-registry:8761/eureka/ \
#              --memory=1g \
#              --cpus=1.0 \
#              youthconnect/edge-functions:latest
# ==============================================================================