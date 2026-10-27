FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies

COPY src src
RUN ./gradlew --no-daemon bootJar -x test

RUN set -e; jar="$(ls -1 build/libs/*.jar | grep -v -- '-plain\.jar$')"; test -f "$jar"; cp "$jar" /app/app.jar

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring
RUN mkdir -p /app/var/resumes && chown -R spring:spring /app

COPY --from=build /app/app.jar app.jar
RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
