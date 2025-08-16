# --- Build stage ---
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the built jar — adjust if needed later
COPY --from=build /app/target/*.jar /app/app.jar
ENV PORT=8080 JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70"
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]