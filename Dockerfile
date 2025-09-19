# ===============================
# Multi-stage Docker build for Spring Boot (Java 21)
# ===============================

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Copy gradle wrapper and build files first for better layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Ensure gradlew is executable
RUN chmod +x gradlew

# Download dependencies (will be cached unless build files change)
# This is a lightweight task to warm up the Gradle cache
RUN ./gradlew --no-daemon -g /workspace/.gradle dependencies || true

# Copy the rest of the project
COPY src src

# Build the application (skip tests for faster container builds)
RUN ./gradlew --no-daemon -g /workspace/.gradle clean bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre

# Create non-root user for security
RUN useradd -ms /bin/bash appuser
USER appuser

WORKDIR /app

# Copy the fat jar from the builder stage
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

# Expose default Spring Boot port
EXPOSE 8080

# JVM options can be overridden at runtime via JAVA_OPTS env var
ENV JAVA_OPTS=""

# Health and helpful defaults
ENV SPRING_MAIN_BANNER-MODE=off

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
