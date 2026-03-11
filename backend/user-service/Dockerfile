# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent pom
COPY pom.xml ./pom.xml
COPY common-lib/pom.xml ./common-lib/pom.xml
COPY user-service/pom.xml ./user-service/pom.xml

# Download dependencies
RUN mvn dependency:go-offline -pl user-service -am

# Copy source code
COPY common-lib/src ./common-lib/src
COPY user-service/src ./user-service/src

# Build
RUN mvn package -pl user-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar
COPY --from=build /app/user-service/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

EXPOSE 8081

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
