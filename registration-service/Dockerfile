FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY config-server config-server
COPY eureka-server eureka-server
COPY api-gateway api-gateway
COPY auth-service auth-service
COPY event-service event-service
COPY registration-service registration-service
COPY notification-service notification-service
RUN mvn -pl registration-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/registration-service/target/registration-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
