package ru.yandex.practicum.cash.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cash.client.dto.NotificationRequest;

@Component
@RequiredArgsConstructor
public class NotificationsClient {

    private final AuthorizedClient authorizedClient;
    private final WebClient.Builder notificationsWebClient;

    public Mono<Void> sendNotification(NotificationRequest request) {
        return authorizedClient
                .retrieveToken()
                .flatMap(
                        accessToken -> notificationsWebClient
                                .build()
                                .post()
                                .uri("/api/notifications")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .body(Mono.just(request), NotificationRequest.class)
                                .retrieve()
                                .bodyToMono(Void.class)
                );
    }
}
