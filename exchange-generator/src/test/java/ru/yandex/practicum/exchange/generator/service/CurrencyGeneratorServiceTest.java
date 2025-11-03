package ru.yandex.practicum.exchange.generator.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(
        topics = {"currency-rates"}
)
public class CurrencyGeneratorServiceTest {

    @Autowired
    private CurrencyGeneratorService currencyGeneratorService;

    @Test
    void generateAndUpdateCurrencyRatesThenUpdateAllCurrencyTest() {
        currencyGeneratorService.generateAndUpdateCurrencyRates();
    }
}