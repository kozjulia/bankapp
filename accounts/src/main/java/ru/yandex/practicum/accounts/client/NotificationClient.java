package ru.yandex.practicum.accounts.client;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.client.dto.NotificationRequest;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final ReactiveOAuth2AuthorizedClientManager manager;
    private final @LoadBalanced WebClient.Builder notificationsWebClient;

    public Mono<Void> sendNotification(NotificationRequest request) {
        return retrieveToken()
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

    private Mono<String> retrieveToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("accounts-client")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
