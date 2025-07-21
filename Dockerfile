# Using a JDK 17 image
FROM openjdk:17-jdk-slim

# Setting the working directory inside the container
WORKDIR /app

# Copy the jar file into the container
COPY target/redisapi-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8081
EXPOSE 8081

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
