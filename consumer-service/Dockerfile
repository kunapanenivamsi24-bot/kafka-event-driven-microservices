# syntax=docker/dockerfile:1
# Consumer service: multi-stage build (Maven -> slim JRE image)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY common ./common
COPY consumer-service ./consumer-service
RUN mvn -q -pl common,consumer-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/consumer-service/target/consumer-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
