# ------------------------------
# STEP 1: Build the application
# ------------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY src ./src

# Build the app (skip tests for speed)
RUN mvn clean package -DskipTests


# ------------------------------
# STEP 2: Run the application
# ------------------------------
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Render sets a PORT env automatically — expose it
EXPOSE 8080

# Copy the jar from the first stage
COPY --from=build /app/target/*.jar app.jar

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
