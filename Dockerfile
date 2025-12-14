# Use Eclipse Temurin (AdoptOpenJDK) which is more reliable
FROM eclipse-temurin:17-jdk-alpine

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy pom.xml first for better Docker layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "target/freshChickenAPI-0.0.1-SNAPSHOT.jar"]