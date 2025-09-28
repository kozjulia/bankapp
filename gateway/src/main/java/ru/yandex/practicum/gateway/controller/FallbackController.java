package ru.yandex.practicum.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/accounts-fallback")
    public Mono<ResponseEntity<Map<String, String>>> accountsFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Сервис счетов временно недоступен");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }

    @GetMapping("/blocker-fallback")
    public Mono<ResponseEntity<Map<String, String>>> blockerFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Сервис блокировок временно недоступен");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }

    @GetMapping("/cash-fallback")
    public Mono<ResponseEntity<Map<String, String>>> cashFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Сервис наличных операций временно недоступен");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }

    @GetMapping("/exchange-fallback")
    public Mono<ResponseEntity<Map<String, String>>> exchangeFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Сервис обмена валют временно недоступен");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }

    @GetMapping("/transfer-fallback")
    public Mono<ResponseEntity<Map<String, String>>> transferFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Сервис переводов временно недоступен");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }
}
