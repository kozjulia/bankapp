# Используем базовый образ с Java 21
FROM openjdk:21-jdk

# Указываем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY ./build/libs/accounts-*.jar app.jar

# Открываем порт 8080
EXPOSE 8001

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]