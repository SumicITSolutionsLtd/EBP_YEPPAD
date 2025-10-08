# =============================================
# Multi-Stage Dockerfile for Service Registry
# =============================================

# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy dependency files
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="YouthConnect Uganda <dev@youthconnect.ug>"
LABEL description="Service Registry (Eureka Server)"
LABEL version="2.0.0"

# Install utilities
RUN apk add --no-cache curl bash tzdata && \
    rm -rf /var/cache/apk/*

# Set timezone
ENV TZ=Africa/Kampala
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create non-root user
RUN addgroup -g 1001 -S eureka && \
    adduser -u 1001 -S eureka -G eureka

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/target/service-registry.jar app.jar

# Create logs directory
RUN mkdir -p /var/log/youth-connect && \
    chown -R eureka:eureka /var/log/youth-connect

# Change ownership
RUN chown -R eureka:eureka /app

USER eureka

EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

# JVM options
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]