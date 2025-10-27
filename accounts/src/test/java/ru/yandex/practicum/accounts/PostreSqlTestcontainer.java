package ru.yandex.practicum.accounts;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class PostreSqlTestcontainer {

    @Container
    @ServiceConnection
    public static final PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:15");

    static {
        postgresqlContainer.start();
    }

    public static String r2dbcUrl() {
        return String.format("r2dbc:postgres://%s:%s/%s",
                postgresqlContainer.getHost(),
                postgresqlContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgresqlContainer.getDatabaseName());
    }
}
