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
import ru.yandex.practicum.front.client.TransferClient;
import ru.yandex.practicum.front.dto.ErrorResponse;
import ru.yandex.practicum.front.client.dto.TransferRequest;
import ru.yandex.practicum.front.security.CustomUserDetails;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TransferController {

    private final ObjectMapper objectMapper;
    private final TransferClient transferClient;
    private final AccountsController accountsController;

    @PostMapping(value = "/user/{login}/transfer")
    public Mono<String> transfer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            @PathVariable("login") String login,
            TransferRequest request
    ) {
        return transferClient.transfer(login, request)
                .then(Mono.fromCallable(() -> "redirect:/main"))
                .onErrorResume(WebClientResponseException.class,
                        ex -> handleTransferError(ex, userDetails, model, request));
    }

    private Mono<String> handleTransferError(WebClientResponseException ex,
                                             CustomUserDetails userDetails,
                                             Model model,
                                             TransferRequest request) {
        return Mono.fromCallable(() -> {
                    ErrorResponse error = objectMapper.readValue(ex.getResponseBodyAsString(),
                            ErrorResponse.class);
                    if (request.getLogin().equals(request.getToLogin())) {
                        model.addAttribute("transferErrors", List.of(error.error()));
                    } else {
                        model.addAttribute("transferOtherErrors", List.of(error.error()));
                    }
                    return null;
                })
                .onErrorResume(IOException.class, e -> Mono.error(ex))
                .then(accountsController.getAccountsInfo(userDetails, model));
    }
}
