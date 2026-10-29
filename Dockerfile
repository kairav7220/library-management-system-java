# syntax=docker/dockerfile:1

# -----------------------------------------------------------------------------
# Stage 1: Build the Spring Boot application using Maven 3.9 and Java 21
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and package the jar
COPY src ./src
RUN mvn clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime image with Eclipse Temurin JRE 21 (Debian/glibc based)
# Note: glibc is required for com.lambdaworks:scrypt native bindings.
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as non-privileged user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy built artifact
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Render automatically sets PORT (defaults to 10000 or custom)
ENV PORT=5000
EXPOSE 5000

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
