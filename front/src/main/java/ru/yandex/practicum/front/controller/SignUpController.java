package ru.yandex.practicum.front.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.client.AccountsClient;
import ru.yandex.practicum.front.client.dto.CreateUserRequest;
import ru.yandex.practicum.front.dto.ErrorResponse;

import java.io.IOException;
import java.util.List;

import static ru.yandex.practicum.front.configuration.constants.ViewConstants.SIGNUP_VIEW;

@Controller
@RequiredArgsConstructor
public class SignUpController {

    private final ObjectMapper objectMapper;
    private final AccountsClient accountsClient;

    @GetMapping(value = "/signup")
    public Mono<String> getSignUpForm() {
        return Mono.just(SIGNUP_VIEW);
    }

    @PostMapping(value = "/signup")
    public Mono<String> registerUser(Model model, CreateUserRequest request) {
        return accountsClient.createUser(request)
                .map(user -> "redirect:/login")
                .onErrorResume(WebClientResponseException.class,
                        ex -> handleSignupError(ex, model, request));
    }

    private Mono<String> handleSignupError(WebClientResponseException ex, Model model, CreateUserRequest request) {
        return Mono.fromCallable(() -> {
            ErrorResponse error = objectMapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);

            model.addAttribute("errors", List.of(error.error()));
            model.addAttribute("login", request.getLogin());
            model.addAttribute("name", request.getName());
            model.addAttribute("birthdate", request.getBirthdate());
            return SIGNUP_VIEW;
        }).onErrorResume(IOException.class, e -> Mono.error(ex));
    }
}
