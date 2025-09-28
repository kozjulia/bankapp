package ru.yandex.practicum.cash.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cash.configuration.TestSecurityConfiguration;
import ru.yandex.practicum.cash.dto.CashChangeRequest;
import ru.yandex.practicum.cash.service.CashService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfiguration.class)
public class CashControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    CashService cashService;

    @Test
    void processAccountTransactionWhenPutCashThenSuccessfulProcessTransactionTest() {
        String login = "testUser";
        CashChangeRequest request = new CashChangeRequest(
                "USD",
                BigDecimal.valueOf(100),
                CashChangeRequest.Action.PUT
        );

        when(cashService.processAccountTransaction(eq(login), any(CashChangeRequest.class)))
                .thenReturn(Mono.empty());

        webTestClient.mutateWith(getJwtMutator())
                .post()
                .uri("/api/v1/users/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void processAccountTransactionWhenEmptyRequestThenReturnBadRequestTest() {
        String login = "testUser";

        webTestClient.mutateWith(getJwtMutator())
                .post()
                .uri("/api/v1/users/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void processAccountTransactionWhenWithdrawMoneyThenSuccessfulProcessTransactionTest() {
        String login = "testUser";
        CashChangeRequest request = new CashChangeRequest(
                "USD",
                BigDecimal.valueOf(50),
                CashChangeRequest.Action.GET
        );

        when(cashService.processAccountTransaction(eq(login), any(CashChangeRequest.class)))
                .thenReturn(Mono.empty());

        webTestClient.mutateWith(getJwtMutator())
                .post()
                .uri("/api/v1/users/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    private static SecurityMockServerConfigurers.JwtMutator getJwtMutator() {
        return mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_cash.write"));
    }
}