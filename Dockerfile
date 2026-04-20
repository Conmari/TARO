# Этап 1: Сборка jar
FROM gradle:9.2-jdk25 AS build
WORKDIR /app

# Копируем файлы конфигурации
COPY build.gradle settings.gradle gradle.properties ./

# Кешируем зависимости через mount.
# Это НЕ создает лишних слоев, но ускоряет повторные сборки.
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle dependencies --no-daemon

# Копируем исходники
COPY src ./src

# Собираем jar, используя тот же кеш для Gradle
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle bootJar -x test --no-daemon

# Этап 2: Подготовка слоёв
FROM eclipse-temurin:25-jre AS layers
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Этап 3: Финальный образ
FROM eclipse-temurin:25-jre
WORKDIR /app

# Слои Spring Boot (от редко меняющихся к часто меняющимся)
COPY --from=layers /app/dependencies/ ./
COPY --from=layers /app/spring-boot-loader/ ./
COPY --from=layers /app/snapshot-dependencies/ ./
COPY --from=layers /app/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
