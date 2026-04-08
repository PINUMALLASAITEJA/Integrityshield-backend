# Use official Java image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the application
RUN chmod +x mvnw && ./mvnw clean package -Dmaven.test.skip=true

# Expose port
EXPOSE 10000

# Run using Render PORT (CRITICAL FIX)
CMD java -Dserver.port=${PORT} -Dserver.address=0.0.0.0 -jar target/backend-0.0.1-SNAPSHOT.jar