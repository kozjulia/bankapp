package ru.yandex.practicum.accounts;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Import(TestSecurityConfiguration.class)
@EmbeddedKafka(topics = "notifications")
@ImportTestcontainers(PostreSqlTestcontainer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseIntegrationTest {

    @Autowired
    protected DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        databaseClient.sql("TRUNCATE TABLE accounts CASCADE; TRUNCATE TABLE users CASCADE")
                .then()
                .block();
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.r2dbc.url", PostreSqlTestcontainer::r2dbcUrl);
        registry.add("spring.r2dbc.username", PostreSqlTestcontainer.postgresqlContainer::getUsername);
        registry.add("spring.r2dbc.password", PostreSqlTestcontainer.postgresqlContainer::getPassword);

        registry.add("spring.datasource.url", PostreSqlTestcontainer.postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", PostreSqlTestcontainer.postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", PostreSqlTestcontainer.postgresqlContainer::getPassword);

        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration"); // 👈
        registry.add("spring.flyway.datasource.url", PostreSqlTestcontainer.postgresqlContainer::getJdbcUrl);
        registry.add("spring.flyway.datasource.username", PostreSqlTestcontainer.postgresqlContainer::getUsername);
        registry.add("spring.flyway.datasource.password", PostreSqlTestcontainer.postgresqlContainer::getPassword);
    }
}
