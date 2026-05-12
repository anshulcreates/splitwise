FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY expense-tracker/pom.xml .
RUN mvn dependency:go-offline -q
COPY expense-tracker/src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
