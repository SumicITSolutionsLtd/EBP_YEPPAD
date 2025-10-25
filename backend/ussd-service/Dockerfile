# =============================================
# Multi-Stage Dockerfile for USSD Service
# =============================================
# Stage 1: Build stage with Maven
# Stage 2: Runtime stage with optimized JRE

# =============================================
# STAGE 1: BUILD
# =============================================
FROM maven:3.9.5-eclipse-temurin-17-alpine AS builder

# Set working directory
WORKDIR /build

# Copy dependency configuration first (for layer caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests for faster builds, run tests in CI/CD)
RUN mvn clean package -DskipTests -B

# Verify JAR was created
RUN ls -lh /build/target/*.jar

# =============================================
# STAGE 2: RUNTIME
# =============================================
FROM eclipse-temurin:17-jre-alpine

# Metadata
LABEL maintainer="Entreprenuership Booster Platform Uganda <dev@entreprenuership-booster-platform.ug>"
LABEL description="USSD Service for Entreprenuership Booster Platform"
LABEL version="1.0.0"

# Install dependencies for monitoring and debugging
RUN apk add --no-cache \
    curl \
    bash \
    tzdata \
    && rm -rf /var/cache/apk/*

# Set timezone to Africa/Kampala
ENV TZ=Africa/Kampala
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create non-root user for security
RUN addgroup -g 1001 -S ussd && \
    adduser -u 1001 -S ussd -G ussd

# Create application directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/ussd-service.jar app.jar

# Create logs directory
RUN mkdir -p /var/log/youth-connect && \
    chown -R ussd:ussd /var/log/youth-connect

# Change ownership
RUN chown -R ussd:ussd /app

# Switch to non-root user
USER ussd

# Expose port
EXPOSE 8004

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8004/actuator/health || exit 1

# JVM options for container
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]