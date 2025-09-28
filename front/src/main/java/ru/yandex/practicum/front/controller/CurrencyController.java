package ru.yandex.practicum.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.front.client.ExchangeClient;
import ru.yandex.practicum.front.dto.CurrencyDto;

@RestController
@RequiredArgsConstructor
public class CurrencyController {

    private final ExchangeClient exchangeClient;

    @GetMapping(value = "/api/rates")
    public Flux<CurrencyDto> getCurrencyRates() {
        return exchangeClient.getCurrencyRates();
    }
}
