# Etapa de compilación
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Se copian primero los archivos de Gradle para aprovechar la caché de capas
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

# Código fuente y empaquetado
COPY src src
RUN ./gradlew --no-daemon bootJar

# Etapa de ejecución
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --uid 1001 appuser
COPY --from=build /app/build/libs/*SNAPSHOT.jar app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
