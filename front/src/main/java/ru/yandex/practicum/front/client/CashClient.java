package ru.yandex.practicum.front.client;

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
import ru.yandex.practicum.front.client.dto.CashChangeRequest;

@Component
@RequiredArgsConstructor
public class CashClient {

    private final ReactiveOAuth2AuthorizedClientManager manager;
    private final @LoadBalanced WebClient.Builder cashWebClient;

    public Mono<Void> processAccountTransaction(String login, CashChangeRequest request) {
        return retrieveToken()
                .flatMap(
                        accessToken -> cashWebClient.build()
                                .post()
                                .uri("/api/v1/users/" + login + "/cash")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(Void.class)
                );
    }

    private Mono<String> retrieveToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("front-client")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
