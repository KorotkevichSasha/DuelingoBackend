# Build stage
FROM gradle:8.4.0-jdk17 AS builder
WORKDIR /app
COPY build.gradle .
COPY settings.gradle .
COPY src ./src
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]