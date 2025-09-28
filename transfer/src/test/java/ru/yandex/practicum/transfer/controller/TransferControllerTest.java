package ru.yandex.practicum.transfer.controller;

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
import ru.yandex.practicum.transfer.configuration.TestSecurityConfiguration;
import ru.yandex.practicum.transfer.controller.dto.TransferRequest;
import ru.yandex.practicum.transfer.service.TransferService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfiguration.class)
public class TransferControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private TransferService transferService;

    @Test
    void transferWhenValidRequestThenReturnOkTest() {
        TransferRequest request = new TransferRequest();
        request.setLogin("user1");
        request.setFromCurrency("RUB");
        request.setToCurrency("USD");
        request.setValue(BigDecimal.valueOf(1000));
        request.setToLogin("user2");

        when(transferService.transfer(any(TransferRequest.class)))
                .thenReturn(Mono.empty());

        webTestClient.mutateWith(getJwtMutator())
                .post()
                .uri("/users/user1/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        verify(transferService).transfer(any(TransferRequest.class));
    }

    private static SecurityMockServerConfigurers.JwtMutator getJwtMutator() {
        return mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_transfer.write"));
    }
}