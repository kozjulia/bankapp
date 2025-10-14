## Микросервисное приложение «Банк»

_______

Банк **умеет** делать следующее:

1.

-------

Приложение написано на **Java 21**, использует **Spring Boot 3**, **Gradle**, **Thymeleaf**, **WebFlux**, **Flyway**,
**Spring Security**,
**JUnit 5**, **Mockito**, **Testcontainers**, **Docker**, API соответствует **REST**, данные хранятся в БД **PostgreSQL
**,
кэш в **Redis**, тесты выполняются в **PostgreSQL**.  
Тестовое покрытие кода - % строк кода.

-------

Для запуска приложения:

```gradle
gradle clean build
```

```command
docker-compose up
```

В директории build/libs проекта появится jar-архив сервиса

3. Запустить приложения можно по адресу:  
   [адрес front](http://localhost:8000)
   [адрес accounts](http://localhost:8001)
   [адрес blocker](http://localhost:8002)
   [адрес cash](http://localhost:8003)
   [адрес exchange](http://localhost:8004)
   [адрес exchange-generator](http://localhost:8005)
   [адрес notifications](http://localhost:8006)
   [адрес transfer](http://localhost:8007)
   [адрес gateway](http://localhost:8010)
4. Успех!  