package ru.yandex.practicum.front.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.client.dto.TransferRequest;

@Component
@RequiredArgsConstructor
public class TransferClient {

    private final ReactiveOAuth2AuthorizedClientManager manager;
    private final WebClient.Builder transferWebClient;

    public Mono<Void> transfer(String login, TransferRequest request) {
        request.setLogin(login);
        return retrieveToken()
                .flatMap(
                        accessToken -> transferWebClient.build()
                                .post()
                                .uri("/users/" + login + "/transfer")
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
