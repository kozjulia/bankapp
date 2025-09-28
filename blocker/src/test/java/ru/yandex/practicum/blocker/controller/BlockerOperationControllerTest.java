package ru.yandex.practicum.blocker.controller;

import ru.yandex.practicum.blocker.configuration.TestSecurityConfiguration;
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
import ru.yandex.practicum.blocker.dto.OperationCheckResult;
import ru.yandex.practicum.blocker.dto.OperationRequest;
import ru.yandex.practicum.blocker.service.BlockerOperationService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfiguration.class)
public class BlockerOperationControllerTest {

    @Autowired
    WebTestClient webTestClient;
    @MockitoBean
    private BlockerOperationService blockerOperationService;

    @Test
    void performOperationWhenOperationAllowedThenReturnOkResultTest() {
        OperationRequest request = new OperationRequest("user123", "PAYMENT", BigDecimal.valueOf(100.0));
        OperationCheckResult expectedResult = OperationCheckResult.ok();

        when(blockerOperationService.checkOperation(any()))
                .thenReturn(Mono.just(expectedResult));

        webTestClient.mutateWith(getJwtMutator())
                .post()
                .uri("/api/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OperationCheckResult.class)
                .isEqualTo(expectedResult);
    }

    @Test
    void performOperationWhenOperationBlockedThenReturnBlockedResultTest() {
        OperationRequest request = new OperationRequest("user123", "PAYMENT", BigDecimal.valueOf(1000000.0));
        OperationCheckResult blockedResult = new OperationCheckResult(
                true,
                "Операция заблокирована Large Amount Detector",
                "LARGE_AMOUNT_DETECTOR"
        );

        when(blockerOperationService.checkOperation(any()))
                .thenReturn(Mono.just(blockedResult));

        webTestClient.mutateWith(getJwtMutator())
                .post()
                .uri("/api/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OperationCheckResult.class)
                .isEqualTo(blockedResult);
    }

    @Test
    void performOperationThenInvalidRequestThenReturnBadRequestTest() {
        String invalidJson = "{\"userId\": \"user123\", invalid}";

        webTestClient.mutateWith(getJwtMutator())
                .post()
                .uri("/api/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void performOperationWhenEmptyRequestThenReturnBadRequestTest() {
        webTestClient.mutateWith(getJwtMutator())
                .post()
                .uri("/api/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static SecurityMockServerConfigurers.JwtMutator getJwtMutator() {
        return mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_blocker.read"));
    }
}