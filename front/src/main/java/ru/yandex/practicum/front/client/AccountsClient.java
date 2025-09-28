package ru.yandex.practicum.front.client;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.client.dto.CreateUserRequest;
import ru.yandex.practicum.front.client.dto.EditPasswordRequest;
import ru.yandex.practicum.front.client.dto.UserDto;

@Component
@RequiredArgsConstructor
public class AccountsClient {

    private final ReactiveOAuth2AuthorizedClientManager manager;
    private final @LoadBalanced WebClient.Builder accountsWebClient;

    public Mono<UserDto> createUser(CreateUserRequest request) {
        return retrieveToken()
                .flatMap(
                        accessToken -> accountsWebClient.build()
                                .post()
                                .uri("/api/v1/users")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(UserDto.class)
                );
    }

    public Mono<UserDto> getAccountDetails(String login) {
        return retrieveToken()
                .flatMap(
                        accessToken -> accountsWebClient.build()
                                .get()
                                .uri("/api/v1/users/" + login)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .retrieve()
                                .bodyToMono(UserDto.class)
                );
    }

    public Mono<UserDto> editPassword(String login, EditPasswordRequest request) {
        return retrieveToken()
                .flatMap(
                        accessToken -> accountsWebClient.build()
                                .post()
                                .uri("/api/v1/users/" + login + "/editPassword")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(UserDto.class)
                );
    }

    public Mono<UserDto> editUserAccounts(String login, UserDto request) {
        return retrieveToken()
                .flatMap(
                        accessToken -> accountsWebClient.build()
                                .post()
                                .uri("/api/v1/users/" + login + "/editUserAccounts")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(UserDto.class)
                );
    }

    public Flux<UserDto> getAllUsers() {
        return retrieveToken()
                .flatMapMany(
                        accessToken -> accountsWebClient.build()
                                .get()
                                .uri("/api/v1/users")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .retrieve()
                                .bodyToFlux(UserDto.class)
                );
    }

    private Mono<String> retrieveToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("front-client")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
