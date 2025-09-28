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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.dto.CurrencyDto;

@Component
@RequiredArgsConstructor
public class ExchangeClient {

    private final ReactiveOAuth2AuthorizedClientManager manager;
    private final @LoadBalanced WebClient.Builder exchangeWebClient;

    public Flux<CurrencyDto> getCurrencyRates() {
        return retrieveToken()
                .flatMapMany(accessToken ->
                        exchangeWebClient.build()
                                .get()
                                .uri("/api/currencies")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .retrieve()
                                .bodyToFlux(CurrencyDto.class)
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
