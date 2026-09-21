# syntax=docker/dockerfile:1

# Runtime-only image: expects the Spring Boot fat JAR to be built beforehand with
#   ./mvnw -DskipTests clean package
# Building the JAR on the host (instead of inside the container) avoids running Maven
# under QEMU emulation, which fails on Apple Silicon when producing linux/amd64 images.
#
# Build for the target platform, e.g.:
#   docker build --platform linux/amd64 -t booking-service:latest .
#
# The JAR is pure Java bytecode (architecture-independent); only the JRE base image
# is architecture-specific, and it is selected automatically by --platform.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user for a smaller attack surface.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Copy the pre-built artifact (single Spring Boot fat JAR) from the host target/ dir.
COPY target/*.jar app.jar

# booking-service listens on 8084 (see src/main/resources/application.yaml).
# SERVER_PORT can override this at runtime (e.g. set to 8080 for AWS App Runner defaults).
EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
