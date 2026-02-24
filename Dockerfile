# ---- Stage 1: Build backend, generate OpenAPI, TS client, and frontend ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Build everything: backend, openapi.json, typescript client, frontend
RUN ./mvnw --batch-mode clean verify -DskipTests

# ---- Stage 2: Production runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Set Spring profile to prod
ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=build /app/target/app.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]