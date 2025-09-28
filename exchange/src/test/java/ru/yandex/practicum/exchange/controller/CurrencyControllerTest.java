package ru.yandex.practicum.exchange.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.exchange.configuration.TestSecurityConfiguration;
import ru.yandex.practicum.exchange.dto.CurrencyDto;

import java.math.BigDecimal;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfiguration.class)
class CurrencyControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void getAllCurrenciesWhenCurrenciesArePresentThenReturnAllCurrenciesTest() {
        webTestClient.mutateWith(getJwtMutator())
                .get()
                .uri("/api/currencies")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CurrencyDto.class)
                .hasSize(3)
                .contains(
                        new CurrencyDto("USD", "Dollars", BigDecimal.ONE),
                        new CurrencyDto("CNY", "Yuan", BigDecimal.ONE),
                        new CurrencyDto("RUB", "Rubles", BigDecimal.ONE)
                );
    }

    @Test
    void getCurrencyWhenCurrencyExistsThenReturnCurrencyTest() {
        webTestClient.mutateWith(getJwtMutator())
                .get()
                .uri("/api/currencies/usd")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CurrencyDto.class)
                .isEqualTo(new CurrencyDto("USD", "Dollars", BigDecimal.ONE));
    }

    @Test
    void getCurrencyWhenCurrencyExistsIgnoreCaseThenReturnCurrencyTest() {
        webTestClient.mutateWith(getJwtMutator())
                .get()
                .uri("/api/currencies/USD")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CurrencyDto.class)
                .isEqualTo(new CurrencyDto("USD", "Dollars", BigDecimal.ONE));
    }

    @Test
    void getCurrencyWhenCurrencyNotExistsThenReturnBadRequestTest() {
        webTestClient.mutateWith(getJwtMutator())
                .get()
                .uri("/api/currencies/EURO")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateCurrencyWhenCurrencyExistsThenUpdateAndReturnCurrencyTest() {
        CurrencyDto updatedCurrency = new CurrencyDto("USD", "Dollars", BigDecimal.valueOf(1.5));

        webTestClient.mutateWith(getJwtMutator())
                .put()
                .uri("/api/currencies/usd")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCurrency)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrencyDto.class)
                .isEqualTo(updatedCurrency);

        updatedCurrency = new CurrencyDto("USD", "Dollars", BigDecimal.ONE);

        webTestClient.mutateWith(getJwtMutator())
                .put()
                .uri("/api/currencies/usd")
                .bodyValue(updatedCurrency)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrencyDto.class)
                .isEqualTo(updatedCurrency);
    }

    @Test
    void updateCurrencyWhenCurrencyNotExistsThenReturnBadRequestTest() {
        CurrencyDto updatedCurrency = new CurrencyDto("EURO", "Euro", BigDecimal.valueOf(0.85));

        webTestClient.mutateWith(getJwtMutator())
                .put()
                .uri("/api/currencies/EURO")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCurrency)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static SecurityMockServerConfigurers.JwtMutator getJwtMutator() {
        return mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_exchange.write"),
                new SimpleGrantedAuthority("SCOPE_exchange.read"));
    }
}