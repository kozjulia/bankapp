package ru.yandex.practicum.exchange.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.exchange.dto.CurrencyDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final List<CurrencyDto> currencies = new ArrayList<>();

    public CurrencyController() {
        currencies.add(new CurrencyDto("USD", "Dollars", BigDecimal.ONE));
        currencies.add(new CurrencyDto("CNY", "Yuan", BigDecimal.ONE));
        currencies.add(new CurrencyDto("RUB", "Rubles", BigDecimal.ONE));
    }

    @GetMapping
    public Flux<CurrencyDto> getAllCurrencies() {
        return Flux.fromIterable(currencies);
    }

    @GetMapping("/{name}")
    public Mono<CurrencyDto> getCurrency(@PathVariable String name) {
        return Mono.justOrEmpty(currencies.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(name)).findAny())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Валюта не найдена")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CurrencyDto> addCurrency(@RequestBody CurrencyDto currency) {
        return Mono.just(currency)
                .doOnNext(currencies::add);
    }

    @PutMapping("/{code}")
    public Mono<CurrencyDto> updateCurrency(@PathVariable String code, @RequestBody CurrencyDto updatedCurrency) {
        return getCurrency(code)
                .doOnNext(currency -> currency.setValue(updatedCurrency.getValue()))
                .thenReturn(updatedCurrency);
    }
}