# ============================================================================
# FILE 3: Dockerfile (Multi-stage build for production)
# ============================================================================

# Stage 1: Build Application
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# Build arguments
ARG BUILD_DATE
ARG VCS_REF

# Set working directory
WORKDIR /app

# Copy Maven files first (for layer caching)
COPY pom.xml .
COPY .mvn .mvn

# Download dependencies (cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests for faster builds)
# Tests should run in CI/CD pipeline before building Docker image
RUN mvn clean package -DskipTests -B

# Verify JAR was created
RUN ls -lh target/ && \
    test -f target/*.jar || (echo "JAR file not found!" && exit 1)

# Stage 2: Runtime Image
FROM eclipse-temurin:17-jre-alpine

# Metadata labels
LABEL maintainer="Douglas Kings Kato <support@entrepreneurshipbooster.ug>"
LABEL description="Job Service for Youth Entrepreneurship Booster Platform"
LABEL version="3.0.0"
LABEL build-date="${BUILD_DATE}"
LABEL vcs-ref="${VCS_REF}"

# Install required utilities
RUN apk add --no-cache \
    curl \
    wget \
    bash \
    tzdata

# Set timezone
ENV TZ=Africa/Nairobi
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create non-root user for security
# Running as root in containers is a security risk
RUN addgroup -S spring && \
    adduser -S spring -G spring && \
    mkdir -p /app/logs /var/app/uploads && \
    chown -R spring:spring /app /var/app

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/job-services-*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8000

# Health check
# Docker will mark container as unhealthy if this fails
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8000/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]