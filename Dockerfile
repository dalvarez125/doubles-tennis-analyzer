# Etapa 1: compilar el proyecto con Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: usar solo el JAR en una imagen ligera
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/doubles-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
