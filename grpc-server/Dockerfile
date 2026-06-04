# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN --mount=type=cache,id=gradle-grpc-server,target=/root/.gradle \
    chmod +x gradlew && ./gradlew :grpc-server:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/grpc-server/build/libs/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
