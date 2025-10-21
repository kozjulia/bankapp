package ru.yandex.practicum.transfer.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.transfer.client.dto.CurrencyDto;

@Component
@RequiredArgsConstructor
public class ExchangeClient {

    private final AuthorizedClient authorizedClient;
    private final WebClient.Builder exchangeWebClient;

    public Flux<CurrencyDto> getCurrencyRates() {
        return authorizedClient
                .retrieveToken()
                .flatMapMany(
                        accessToken -> exchangeWebClient
                                .build()
                                .get()
                                .uri("/api/currencies")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .retrieve()
                                .bodyToFlux(CurrencyDto.class)
                );
    }
}
