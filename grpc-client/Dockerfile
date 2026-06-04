# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN --mount=type=cache,id=gradle-grpc-client,target=/root/.gradle \
    chmod +x gradlew && ./gradlew :grpc-client:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/grpc-client/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
