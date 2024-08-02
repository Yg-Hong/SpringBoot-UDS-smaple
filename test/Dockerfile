# Stage 1: Build stage
FROM gradle:8.9.0-jdk21 AS build

WORKDIR /app

COPY gradle ./gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

COPY src ./src

RUN ./gradlew build -x test

# Stage 2: Copy testing python file
FROM python:3.11-alpine

RUN python3 /app/src/main/resources/static/uds.py

# Stage 3: Run stage
FROM alpine/java:21-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]