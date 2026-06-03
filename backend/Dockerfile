# Use Maven to build the project
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only pom and source first for better cache behavior
COPY pom.xml .
RUN mvn dependency:go-offline

# Build jar
COPY src ./src
RUN mvn -B -DskipTests package

# Runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
