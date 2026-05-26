# =============================================================================
# Stage 1: Build the Maven application
# =============================================================================
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application skipping unit tests for speed (can be verified outside docker)
RUN mvn clean package -DskipTests

# =============================================================================
# Stage 2: Run the Spring Boot JAR inside JRE
# =============================================================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Expose server port
EXPOSE 8080

# Copy the generated JAR from Stage 1
COPY --from=build /app/target/project-management-system-1.0.0.jar app.jar

# Define storage volumes for file uploads
VOLUME /app/uploads

# Configure container entrypoint
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
