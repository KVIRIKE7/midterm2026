FROM maven:3.9.6-eclipse-temurin-11 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ---- runtime image ----
FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=build /app/target/uno-1.0.0.jar uno.jar

ENTRYPOINT ["java", "-jar", "uno.jar"]
