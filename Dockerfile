# Step 1: Use OpenJDK 21 as base image
FROM openjdk:21-jdk-slim

# Step 2: Set working directory inside container
WORKDIR /app

# Step 3: Copy the built JAR into container
COPY target/*.jar app.jar

# Step 4: Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]