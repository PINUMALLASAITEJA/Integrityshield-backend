# Use official Java image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the application
RUN chmod +x mvnw && ./mvnw clean package -Dmaven.test.skip=true

# 🔥 IMPORTANT: match Spring Boot port
EXPOSE 10000

# Run the application
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]