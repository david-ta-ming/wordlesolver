# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the Maven configuration files first
COPY pom.xml .
COPY .mvn .mvn

# Download dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM amazoncorretto:17-alpine

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set the working directory
WORKDIR /app

# Copy the built artifact from builder stage
COPY --from=builder /app/target/wordlesolver.jar app.jar

# Change ownership of the application files
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose the application port
EXPOSE 8080

# Set Java options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]