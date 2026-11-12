# ============================================
# STAGE 1: BUILD
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy pom.xml first (for better caching)
COPY . .
# Download dependencies (cached if pom.xml doesn't change)
RUN mvn dependency:go-offline
# Build the application
RUN mvn clean package -DskipTests
# ============================================
# STAGE 2: RUN
# ============================================
# Run the JAR in a slim runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]