FROM eclipse-temurin:17-jdk-alpine AS backend-build
WORKDIR /app
COPY server/pom.xml ./
COPY server/.mvn .mvn
COPY server/mvnw ./
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B || true

COPY server/src ./src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine AS backend
WORKDIR /app
COPY --from=backend-build /app/target/vaccine-distribution-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
