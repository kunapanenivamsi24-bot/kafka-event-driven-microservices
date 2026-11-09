# syntax=docker/dockerfile:1
# Producer service: multi-stage build (Maven -> slim JRE image)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY common ./common
COPY producer-service ./producer-service
RUN mvn -q -pl common,producer-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/producer-service/target/producer-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
