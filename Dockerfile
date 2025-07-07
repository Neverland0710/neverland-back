# 🔧 Build Stage
FROM gradle:8.5.0-jdk17 AS builder
WORKDIR /home/app
COPY . .
RUN gradle build --no-daemon

# 🚀 Runtime Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /home/app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
