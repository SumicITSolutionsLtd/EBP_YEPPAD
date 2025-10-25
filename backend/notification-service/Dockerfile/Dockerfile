# Multi-stage build for optimization
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/target/notification-service-1.0.0.jar app.jar
EXPOSE 7077
ENTRYPOINT ["java", "-jar", "app.jar"]