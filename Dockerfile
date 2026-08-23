# Build stage
FROM gradle:8.11.1-jdk17 AS builder
WORKDIR /app
COPY build.gradle .
COPY settings.gradle .
COPY src ./src
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --home-dir /app app \
    && mkdir -p /app/uploads/avatars \
    && chown -R app:app /app
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8082 10000
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl --fail --silent "http://127.0.0.1:${SERVER_PORT:-10000}/actuator/health/liveness" > /dev/null || exit 1
# Keep heap, class metadata, thread stacks, and native buffers inside Render
# Free's 512 MB limit. Spring + Hibernate need more than 128 MB of metaspace
# after all production paths have been exercised, so trade a little heap for a
# stable class-metadata budget instead of restarting under normal traffic.
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx224m -XX:MaxMetaspaceSize=192m -XX:MaxDirectMemorySize=24m -Xss512k -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError -Djava.net.preferIPv4Stack=true"
ENTRYPOINT ["java", "-jar", "app.jar"]
