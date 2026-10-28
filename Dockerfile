FROM eclipse-temurin:21-jdk-alpine AS dependencies
RUN apk add --no-cache maven
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline

FROM dependencies AS builder
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S spring && adduser -S -G spring spring
COPY --from=builder --chown=spring:spring /build/target/*.jar app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]