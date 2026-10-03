# ---- build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B -DskipTests package

# ---- run stage: small JRE image, non-root user ----
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -r -u 1001 clinic
COPY --from=build /app/target/*.jar app.jar
USER clinic
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
