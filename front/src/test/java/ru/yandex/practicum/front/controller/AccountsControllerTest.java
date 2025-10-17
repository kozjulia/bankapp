package ru.yandex.practicum.front.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.TestSecurityConfiguration;
import ru.yandex.practicum.front.client.AccountsClient;
import ru.yandex.practicum.front.dto.AccountDto;
import ru.yandex.practicum.front.dto.CurrencyDto;
import ru.yandex.practicum.front.client.dto.EditPasswordRequest;
import ru.yandex.practicum.front.dto.ErrorResponse;
import ru.yandex.practicum.front.client.dto.UserDto;
import ru.yandex.practicum.front.dto.UserUpdateRequest;
import ru.yandex.practicum.front.security.CustomUserDetails;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfiguration.class)
public class AccountsControllerTest {

    @Autowired
    WebTestClient webTestClient;
    @MockitoBean
    private ObjectMapper objectMapper;
    @MockitoBean
    private AccountsClient accountsClient;

    @Test
    void getAccountsInfoWhenUserNotAuthenticatedThenReturnMainViewTest() {
        webTestClient.get()
                .uri("/main")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void getAccountsInfoWhenUserAuthenticatedThenReturnMainViewWithUserDataTest() {
        String login = "testUser";
        CustomUserDetails userDetails = new CustomUserDetails(login, "password", true, true, true, true, List.of());
        UserDto user = UserDto.builder()
                .login(login)
                .name("Test User")
                .birthdate(LocalDate.of(2000, 5, 9))
                .build();
        List<UserDto> allUsers = List.of(
                user,
                UserDto.builder()
                        .login("user2")
                        .name("User 2")
                        .build()
        );

        when(accountsClient.getAccountDetails(login))
                .thenReturn(Mono.just(user));
        when(accountsClient.getAllUsers())
                .thenReturn(Flux.fromIterable(allUsers));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .get()
                .uri("/main")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);

        verify(accountsClient).getAccountDetails(login);
        verify(accountsClient).getAllUsers();
    }

    @Test
    void editPasswordWhenAllParamsAreValidThenRedirectToMainTest() {
        String login = "testUser";
        EditPasswordRequest request = EditPasswordRequest.builder()
                .login(login)
                .password("newPassword")
                .confirmPassword("newPassword")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(login, "password", true, true, true, true, List.of());
        UserDto updatedUser = UserDto.builder()
                .login(login)
                .password("newPassword")
                .build();

        when(accountsClient.editPassword(eq(login), any(EditPasswordRequest.class)))
                .thenReturn(Mono.just(updatedUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .post()
                .uri("/user/{login}/editPassword", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("password=" + request.getPassword() + "&confirmPassword=" + request.getConfirmPassword())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");

        verify(accountsClient).editPassword(login, request);
    }

    @Test
    @SneakyThrows
    void editPasswordWhenErrorThenHandleErrorAndReturnToMainViewTest() {
        String login = "testUser";
        EditPasswordRequest request = EditPasswordRequest.builder()
                .login(login)
                .password("newPassword")
                .confirmPassword("newPassword")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(login, "password", true, true, true, true, List.of());

        ErrorResponse errorResponse = new ErrorResponse(200, "Ошибка изменения пароля", "api/path", OffsetDateTime.now().toInstant());
        WebClientResponseException exception = WebClientResponseException
                .create(400, "Bad Request", HttpHeaders.EMPTY, "{\"error\":\"Ошибка изменения пароля\"}".getBytes(), null);

        when(accountsClient.editPassword(login, request))
                .thenReturn(Mono.error(exception));
        when(objectMapper.readValue(anyString(), eq(ErrorResponse.class)))
                .thenReturn(errorResponse);
        when(accountsClient.getAccountDetails(login))
                .thenReturn(Mono.just(UserDto.builder().login(login).build()));
        when(accountsClient.getAllUsers())
                .thenReturn(Flux.just(UserDto.builder().login(login).build()));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .post()
                .uri("/user/{login}/editPassword", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("password=" + request.getPassword() + "&confirmPassword=" + request.getConfirmPassword())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);

        verify(accountsClient).editPassword(login, request);
        verify(objectMapper).readValue(anyString(), eq(ErrorResponse.class));
    }

    @Test
    void editPasswordWhenNotAuthenticatedThenReturnUnauthorizedTest() {
        String login = "testUser";
        EditPasswordRequest request = EditPasswordRequest.builder()
                .login(login)
                .password("newPassword")
                .confirmPassword("newPassword")
                .build();

        webTestClient
                .post()
                .uri("/user/{login}/editPassword", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("password=" + request.getPassword() + "&confirmPassword=" + request.getConfirmPassword())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");

        verify(accountsClient, never()).editPassword(anyString(), any());
    }

    @Test
    void editUserAccountsWhenAllParamsAreValidThenRedirectToMainTest() {

        String login = "testUser";
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Новое Имя");
        request.setBirthdate("1990-01-01");
        request.setAccount(List.of("RUB", "USD"));
        CustomUserDetails userDetails = new CustomUserDetails(login, "password", true, true, true, true, List.of());

        UserDto existingUser = UserDto.builder()
                .id(UUID.randomUUID())
                .login(login)
                .name("Старое Имя")
                .password("password")
                .accounts(List.of(
                        AccountDto.builder().currency(new CurrencyDto("RUB", "RUB", 1d)).exists(true).build(),
                        AccountDto.builder().currency(new CurrencyDto("USD", "USD", 1d)).exists(false).build()
                ))
                .build();
        UserDto updatedUser = UserDto.builder()
                .id(existingUser.getId())
                .login(login)
                .name(request.getName())
                .password(existingUser.getPassword())
                .accounts(List.of(
                        AccountDto.builder().currency(new CurrencyDto("RUB", "RUB", 1d)).exists(true).build(),
                        AccountDto.builder().currency(new CurrencyDto("USD", "USD", 1d)).exists(true).build()
                ))
                .build();

        when(accountsClient.getAccountDetails(login))
                .thenReturn(Mono.just(existingUser));
        when(accountsClient.editUserAccounts(eq(login), any(UserDto.class)))
                .thenReturn(Mono.just(updatedUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .post()
                .uri("/user/{login}/editUserAccounts", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("name=" + request.getName()
                        + "&birthdate=" + request.getBirthdate()
                        + "&account=RUB&account=USD")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");

        verify(accountsClient).getAccountDetails(login);
        verify(accountsClient).editUserAccounts(eq(login), any(UserDto.class));
    }

    @Test
    @SneakyThrows
    void editUserAccountsWhenErrorThenHandleErrorAndReturnToMainViewTest() {

        String login = "testUser";
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Новое Имя");
        request.setBirthdate("1990-01-01");
        request.setAccount(List.of("RUB"));
        CustomUserDetails userDetails = new CustomUserDetails(login, "password", true, true, true, true, List.of());

        UserDto existingUser = UserDto.builder()
                .id(UUID.randomUUID())
                .login(login)
                .name("Старое Имя")
                .accounts(List.of())
                .build();
        ErrorResponse errorResponse = new ErrorResponse(400, "Ошибка обновления аккаунта", "api/path", OffsetDateTime.now().toInstant());
        WebClientResponseException exception = WebClientResponseException.create(
                400,
                "Bad Request",
                HttpHeaders.EMPTY,
                "{\"error\":\"Ошибка обновления аккаунта\"}".getBytes(),
                null
        );

        when(accountsClient.getAccountDetails(login))
                .thenReturn(Mono.just(existingUser));
        when(accountsClient.editUserAccounts(eq(login), any(UserDto.class)))
                .thenReturn(Mono.error(exception));
        when(objectMapper.readValue(anyString(), eq(ErrorResponse.class)))
                .thenReturn(errorResponse);
        when(accountsClient.getAllUsers())
                .thenReturn(Flux.just(existingUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .post()
                .uri("/user/{login}/editUserAccounts", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("name=" + request.getName()
                        + "&birthdate=" + request.getBirthdate()
                        + "&account=RUB")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void editUserAccountsWhenNotAuthenticatedThenRedirectToLoginTest() {

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Новое Имя");
        request.setBirthdate("1990-01-01");
        request.setAccount(List.of("RUB"));
        String login = "testUser";

        webTestClient
                .post()
                .uri("/user/{login}/editUserAccounts", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("name=" + request.getName()
                        + "&birthdate=" + request.getBirthdate()
                        + "&account=RUB")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");

        verify(accountsClient, never()).getAccountDetails(anyString());
        verify(accountsClient, never()).editUserAccounts(anyString(), any());
    }
}
