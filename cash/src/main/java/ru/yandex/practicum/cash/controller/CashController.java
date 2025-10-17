package ru.yandex.practicum.cash.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cash.dto.CashChangeRequest;
import ru.yandex.practicum.cash.service.CashService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class CashController {

    private final CashService cashService;

    @PostMapping("/{login}/cash")
    public Mono<Void> processAccountTransaction(@PathVariable String login, @RequestBody CashChangeRequest request) {
        return cashService.processAccountTransaction(login, request);
    }
}
