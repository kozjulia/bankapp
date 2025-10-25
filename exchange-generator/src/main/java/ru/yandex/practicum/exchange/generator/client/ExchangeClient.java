package ru.yandex.practicum.exchange.generator.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.exchange.generator.client.dto.CurrencyDto;

@Component
@RequiredArgsConstructor
public class ExchangeClient {

    private final ReactiveOAuth2AuthorizedClientManager manager;
    private final WebClient.Builder exchangeWebClient;

    public Mono<CurrencyDto> updateCurrencyRate(String code, Mono<CurrencyDto> updatedCurrency) {
        return retrieveToken()
                .flatMap(accessToken ->
                        exchangeWebClient
                                .build()
                                .put()
                                .uri("/api/currencies/" + code)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .body(updatedCurrency, CurrencyDto.class)
                                .retrieve()
                                .bodyToMono(CurrencyDto.class)
                );
    }

    private Mono<String> retrieveToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("exchange-generator-client")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
