package ru.yandex.practicum.front.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.client.AccountsClient;
import ru.yandex.practicum.front.dto.AccountDto;
import ru.yandex.practicum.front.client.dto.EditPasswordRequest;
import ru.yandex.practicum.front.dto.ErrorResponse;
import ru.yandex.practicum.front.client.dto.UserDto;
import ru.yandex.practicum.front.dto.UserUpdateRequest;
import ru.yandex.practicum.front.configuration.constants.DateConstants;
import ru.yandex.practicum.front.configuration.constants.ViewConstants;
import ru.yandex.practicum.front.security.CustomUserDetails;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Controller
@RequiredArgsConstructor
public class AccountsController {

    private final ObjectMapper objectMapper;
    private final AccountsClient accountsClient;

    @GetMapping(value = {"/", "/main"})
    public Mono<String> getAccountsInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (isNull(userDetails)) {
            return Mono.just(ViewConstants.MAIN_VIEW);
        }

        return accountsClient.getAccountDetails(userDetails.getUsername())
                .zipWith(accountsClient.getAllUsers().collectList())
                .map(tuple -> {
                    UserDto user = tuple.getT1();
                    model.addAttribute("login", user.getLogin());
                    model.addAttribute("name", user.getName());
                    model.addAttribute("birthdate",
                            nonNull(user.getBirthdate())
                                    ? user.getBirthdate().format(DateConstants.DATE_FORMATTER_WITH_DOTS)
                                    : null);
                    model.addAttribute("accounts", user.getAccounts());
                    model.addAttribute("currency",
                            nonNull(user.getAccounts())
                                    ? user.getAccounts().stream().map(AccountDto::getCurrency).toList()
                                    : null);

                    model.addAttribute("users", tuple.getT2());
                    return ViewConstants.MAIN_VIEW;
                });
    }

    @PostMapping(value = "/user/{login}/editPassword")
    public Mono<String> editPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            @PathVariable("login") String login,
            EditPasswordRequest request
    ) {
        return accountsClient.editPassword(login, request)
                .map(user -> "redirect:/main")
                .onErrorResume(WebClientResponseException.class,
                        exception -> handleEditPasswordError(exception, userDetails, model));
    }

    @PostMapping(value = "/user/{login}/editUserAccounts")
    public Mono<String> editUserAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            @PathVariable("login") String login,
            UserUpdateRequest request
    ) {
        return accountsClient.getAccountDetails(login)
                .map(account -> updateUserFromRequest(account, request, login))
                .flatMap(user -> accountsClient.editUserAccounts(login, user))
                .map(user -> "redirect:/main")
                .onErrorResume(WebClientResponseException.class,
                        ex -> handleEditAccountsError(ex, userDetails, model));
    }

    private Mono<String> handleEditPasswordError(WebClientResponseException ex, CustomUserDetails userDetails, Model model) {
        return Mono.fromCallable(() -> {
                    ErrorResponse error = objectMapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);
                    model.addAttribute("passwordErrors", List.of(error.error()));
                    return null;
                })
                .onErrorResume(IOException.class, e -> Mono.error(ex))
                .then(getAccountsInfo(userDetails, model));
    }

    private UserDto updateUserFromRequest(UserDto account, UserUpdateRequest request, String login) {
        List<AccountDto> updatedAccounts = updateAccounts(account.getAccounts(), request.getAccount());
        LocalDate newBirthday = parseBirthdate(request.getBirthdate(), account.getBirthdate());

        return UserDto.builder()
                .id(account.getId())
                .login(login)
                .name(request.getName())
                .password(account.getPassword())
                .birthdate(newBirthday)
                .accounts(updatedAccounts)
                .build();
    }

    private List<AccountDto> updateAccounts(List<AccountDto> accounts, List<String> selectedAccounts) {
        return accounts.stream()
                .peek(acc -> acc.setExists(selectedAccounts.contains(acc.getCurrency().getName())))
                .toList();
    }

    private LocalDate parseBirthdate(String birthdate, LocalDate defaultBirthdate) {
        return nonNull(birthdate) && !birthdate.isEmpty()
                ? LocalDate.parse(birthdate)
                : defaultBirthdate;
    }

    private Mono<String> handleEditAccountsError(WebClientResponseException ex,
                                                 CustomUserDetails userDetails,
                                                 Model model) {
        return Mono.fromCallable(() -> {
                    ErrorResponse error = objectMapper.readValue(ex.getResponseBodyAsString(),
                            ErrorResponse.class);
                    model.addAttribute("userAccountsErrors", List.of(error.error()));
                    return null;
                })
                .onErrorResume(IOException.class, e -> Mono.error(ex))
                .then(getAccountsInfo(userDetails, model));
    }
}