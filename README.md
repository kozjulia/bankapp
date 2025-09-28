## Микросервисное приложение «Банк»

_______

Банк **умеет** делать следующее:

1.

-------

Приложение написано на **Java 21**, использует **Spring Boot 3**, **Gradle**, **Thymeleaf**, **WebMVC**, **Flyway**,
**Openapi Generator**, **Spring Security**,
**JUnit 5**, **Mockito**, **Testcontainers**, **Docker**, API соответствует **REST**, данные хранятся в БД **PostgreSQL
**,
кэш в **Redis**, тесты выполняются в **PostgreSQL**.  
Тестовое покрытие кода - % строк кода.

-------

Для запуска приложения:

1. Создайте БД с параметрами, как в файле: application.yaml.
2. Перейдите в папку с приложением и запустите (предварительно запустив Docker)

```gradle
gradle clean build
```

```command
docker-compose up
```

В директории build/libs проекта появится jar-архив сервиса

3. Запустить приложения можно по адресу:  
   [адрес front](http://localhost:8000)
   [](http://localhost:80)
4. Успех!  