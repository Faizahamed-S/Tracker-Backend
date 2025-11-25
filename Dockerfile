# Step 1: Use Eclipse Temurin JDK 21 (official OpenJDK replacement)
FROM eclipse-temurin:21-jdk-jammy

# Step 2: Set working directory inside container
WORKDIR /app

# Step 3: Copy the built JAR into container
COPY target/*.jar app.jar

# Step 4: Run the application with production profile explicitly activated
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]