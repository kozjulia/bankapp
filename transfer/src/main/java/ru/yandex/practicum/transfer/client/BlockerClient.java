package ru.yandex.practicum.transfer.client;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.transfer.client.dto.OperationCheckResult;
import ru.yandex.practicum.transfer.client.dto.OperationRequest;

@Component
@RequiredArgsConstructor
public class BlockerClient {

    private final AuthorizedClient authorizedClient;
    private final @LoadBalanced WebClient.Builder blockerWebClient;

    public Mono<OperationCheckResult> performOperation(OperationRequest operationRequest) {
        return authorizedClient
                .retrieveToken()
                .flatMap(
                        accessToken -> blockerWebClient
                                .build()
                                .post()
                                .uri("/api/operations")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .body(Mono.just(operationRequest), OperationRequest.class)
                                .retrieve()
                                .bodyToMono(OperationCheckResult.class)
                );
    }
}
