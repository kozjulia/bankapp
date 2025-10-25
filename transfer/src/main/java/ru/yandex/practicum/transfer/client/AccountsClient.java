package ru.yandex.practicum.transfer.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.transfer.client.dto.UserDto;

@Component
@RequiredArgsConstructor
public class AccountsClient {

    private final AuthorizedClient authorizedClient;
    private final WebClient.Builder accountsWebClient;

    public Mono<UserDto> getAccountDetails(String login) {
        return authorizedClient
                .retrieveToken()
                .flatMap(
                        accessToken -> accountsWebClient
                                .build()
                                .get()
                                .uri("/api/v1/users/" + login)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .retrieve()
                                .bodyToMono(UserDto.class)
                );
    }

    public Mono<UserDto> editUserAccounts(String login, UserDto request) {
        return authorizedClient
                .retrieveToken()
                .flatMap(
                        accessToken -> accountsWebClient
                                .build()
                                .post()
                                .uri("/api/v1/users/" + login + "/editUserAccounts")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(UserDto.class)
                );
    }
}
