package ru.yandex.practicum.front.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.client.CashClient;
import ru.yandex.practicum.front.client.dto.CashChangeRequest;
import ru.yandex.practicum.front.dto.ErrorResponse;
import ru.yandex.practicum.front.security.CustomUserDetails;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CashController {

    private final CashClient cashClient;
    private final ObjectMapper objectMapper;
    private final AccountsController accountController;

    @PostMapping(value = "/user/{login}/cash")
    public Mono<String> processAccountTransaction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            @PathVariable("login") String login,
            CashChangeRequest request
    ) {
        return cashClient.processAccountTransaction(login, request)
                .then(Mono.fromCallable(() -> "redirect:/main"))
                .onErrorResume(WebClientResponseException.class,
                        ex -> handleAccountTransactionError(ex, userDetails, model));
    }

    private Mono<String> handleAccountTransactionError(WebClientResponseException ex,
                                                       CustomUserDetails userDetails,
                                                       Model model) {
        return Mono.fromCallable(() -> {
                    ErrorResponse error = objectMapper.readValue(ex.getResponseBodyAsString(),
                            ErrorResponse.class);
                    model.addAttribute("cashErrors", List.of(error.error()));
                    return null;
                })
                .onErrorResume(IOException.class, e -> Mono.error(ex))
                .then(accountController.getAccountsInfo(userDetails, model));
    }
}
