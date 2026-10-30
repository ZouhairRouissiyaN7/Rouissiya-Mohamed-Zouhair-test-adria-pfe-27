FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked chmod +x mvnw && ./mvnw -B -q -pl transaction-service -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/transaction-service/target/transaction-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
