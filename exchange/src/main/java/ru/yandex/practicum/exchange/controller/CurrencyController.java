package ru.yandex.practicum.exchange.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
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
import ru.yandex.practicum.exchange.controller.dto.CurrencyDto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final Map<String, CurrencyDto> currencies = new ConcurrentHashMap<>();

    public CurrencyController() {
        currencies.put("USD", new CurrencyDto("USD", "Dollars", BigDecimal.ONE));
        currencies.put("CNY", new CurrencyDto("CNY", "Yuan", BigDecimal.ONE));
        currencies.put("RUB", new CurrencyDto("RUB", "Rubles", BigDecimal.ONE));
    }

    @GetMapping
    public Flux<CurrencyDto> getAllCurrencies() {
        return Flux.fromIterable(currencies.values());
    }

    @GetMapping("/{name}")
    public Mono<CurrencyDto> getCurrency(@PathVariable String name) {
        return Mono.justOrEmpty(currencies.get(name))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Валюта не найдена")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CurrencyDto> addCurrency(@RequestBody CurrencyDto currency) {
        return Mono.just(currency)
                .doOnNext(newCurrency -> currencies.put(currency.getName(), currency));
    }

    @PutMapping("/{code}")
    public Mono<CurrencyDto> updateCurrency(@PathVariable String code, @RequestBody CurrencyDto updatedCurrency) {
        return getCurrency(code)
                .doOnNext(currency -> currency.setValue(updatedCurrency.getValue()))
                .thenReturn(updatedCurrency);
    }

    @KafkaListener(topics = "currency-rates", groupId = "exchange-service")
    public void listen(@Payload CurrencyDto currencyDto) {
        log.info("Получена валюта: {}", currencyDto);
        currencies.put(currencyDto.getName(), currencyDto);
    }
}