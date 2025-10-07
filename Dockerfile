# Step 1: Use OpenJDK 24
FROM openjdk:21-jdk-slim AS build

WORKDIR /app

# Copy Maven wrapper & pom.xml
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the Spring Boot jar
RUN ./mvnw clean package -DskipTests

# Step 2: Create runtime image
FROM openjdk:21-jdk-slim 

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
