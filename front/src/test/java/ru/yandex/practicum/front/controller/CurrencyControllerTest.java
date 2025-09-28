package ru.yandex.practicum.front.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.front.TestSecurityConfiguration;
import ru.yandex.practicum.front.client.ExchangeClient;
import ru.yandex.practicum.front.dto.CurrencyDto;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfiguration.class)
class CurrencyControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ExchangeClient exchangeClient;

    @Test
    void getCurrencyRatesThenReturnCurrenciesTest() {

        CurrencyDto usd = new CurrencyDto("USD", "US Dollar", 1.0);
        CurrencyDto eur = new CurrencyDto("EUR", "Euro", 1.1);
        List<CurrencyDto> currencies = Arrays.asList(usd, eur);

        when(exchangeClient.getCurrencyRates())
                .thenReturn(Flux.fromIterable(currencies));

        webTestClient.get()
                .uri("/api/rates")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CurrencyDto.class)
                .hasSize(2)
                .contains(usd, eur);
    }

    @Test
    void getCurrencyRatesWhenEmptyThenReturnEmptyListTest() {
        when(exchangeClient.getCurrencyRates())
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/rates")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CurrencyDto.class)
                .hasSize(0);
    }

    @Test
    void getCurrencyRates_WhenError_ShouldReturn5xx() {
        when(exchangeClient.getCurrencyRates())
                .thenReturn(Flux.error(new RuntimeException("Service unavailable")));

        webTestClient.get()
                .uri("/api/rates")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}