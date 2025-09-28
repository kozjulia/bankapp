package ru.yandex.practicum.front.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.TestSecurityConfiguration;
import ru.yandex.practicum.front.client.TransferClient;
import ru.yandex.practicum.front.client.dto.TransferRequest;
import ru.yandex.practicum.front.security.CustomUserDetails;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfiguration.class)
class TransferControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private TransferClient transferClient;

    @Test
    void transferSuccessfulThenRedirectToMainTest() {

        String login = "testUser";
        TransferRequest request = new TransferRequest();
        request.setFromCurrency("USD");
        request.setToCurrency("RUB");
        request.setValue(100);
        request.setToLogin("recipient");

        CustomUserDetails userDetails = new CustomUserDetails(
                login,
                "password",
                true,
                true,
                true,
                true,
                List.of()
        );

        when(transferClient.transfer(eq(login), any(TransferRequest.class)))
                .thenReturn(Mono.empty());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .post()
                .uri("/user/{login}/transfer", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("fromCurrency=" + request.getFromCurrency()
                        + "&toCurrency=" + request.getToCurrency()
                        + "&value=" + request.getValue()
                        + "&toLogin=" + request.getToLogin())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");

        verify(transferClient).transfer(eq(login), any(TransferRequest.class));
    }

    @Test
    void transferWhenNotAuthenticatedThenRedirectToLoginTest() {

        TransferRequest request = new TransferRequest();
        request.setFromCurrency("USD");
        request.setToCurrency("RUB");
        request.setValue(100);
        request.setToLogin("recipient");
        String login = "testUser";

        webTestClient
                .post()
                .uri("/user/{login}/transfer", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("fromCurrency=" + request.getFromCurrency()
                        + "&toCurrency=" + request.getToCurrency()
                        + "&value=" + request.getValue()
                        + "&toLogin=" + request.getToLogin())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");

        verify(transferClient, never()).transfer(anyString(), any());
    }
}