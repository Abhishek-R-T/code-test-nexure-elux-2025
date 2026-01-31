FROM gradle:8.7-jdk-21-and-22-alpine AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew gradlew.bat settings.gradle.kts gradle.properties ./
COPY gradle/libs.versions.toml gradle/
COPY app/build.gradle.kts app/
RUN gradle dependencies --no-daemon
COPY app/src app/src
RUN gradle :app:jar --no-daemon

FROM eclipse-temurin:22-jre
WORKDIR /app
COPY --from=build /app/app/build/libs/app.jar /app/app.jar
EXPOSE 8082
CMD ["java", "-jar", "/app/app.jar"]
