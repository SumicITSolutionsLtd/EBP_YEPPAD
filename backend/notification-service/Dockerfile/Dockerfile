# ═══════════════════════════════════════════════════════════════════════════
# MULTI-STAGE DOCKERFILE - Notification Service (Production-Ready)
# ═══════════════════════════════════════════════════════════════════════════

# ═══════════════════════════════════════════════════════════════════════════
# STAGE 1: BUILD
# ═══════════════════════════════════════════════════════════════════════════
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app

# Copy dependency files first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# ═══════════════════════════════════════════════════════════════════════════
# STAGE 2: RUNTIME
# ═══════════════════════════════════════════════════════════════════════════
FROM amazoncorretto:17-alpine

# Metadata
LABEL maintainer="Douglas Kings Kato <contact@youthconnect.ug>"
LABEL service="notification-service"
LABEL version="1.0.0"

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/notification-service-1.0.0.jar app.jar

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 7077

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:7077/api/notifications/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Xms512m", \
    "-Xmx1024m", \
    "-jar", \
    "app.jar"]