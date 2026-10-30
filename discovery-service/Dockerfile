FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked chmod +x mvnw && ./mvnw -B -q -pl discovery-service -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/discovery-service/target/discovery-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
