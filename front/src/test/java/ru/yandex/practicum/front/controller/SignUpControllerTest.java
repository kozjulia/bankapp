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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.TestSecurityConfiguration;
import ru.yandex.practicum.front.client.AccountsClient;
import ru.yandex.practicum.front.client.dto.CreateUserRequest;
import ru.yandex.practicum.front.dto.ErrorResponse;
import ru.yandex.practicum.front.client.dto.UserDto;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfiguration.class)
public class SignUpControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountsClient accountsClient;

    @Test
    void getSignUpFormThenReturnSignupViewTest() {
        webTestClient.get()
                .uri("/signup")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void registerUserWhenValidRequestThenRedirectToLoginTest() {
        CreateUserRequest request = CreateUserRequest.builder()
                .login("testUser")
                .password("password")
                .confirmPassword("password")
                .name("Test User")
                .birthdate("1990-01-01")
                .build();
        UserDto createdUser = UserDto.builder()
                .login(request.getLogin())
                .name(request.getName())
                .build();

        when(accountsClient.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.just(createdUser));

        webTestClient.post()
                .uri("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("login=" + request.getLogin()
                        + "&password=" + request.getPassword()
                        + "&confirmPassword=" + request.getConfirmPassword()
                        + "&name=" + request.getName()
                        + "&birthdate=" + request.getBirthdate())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/login");

        verify(accountsClient).createUser(any(CreateUserRequest.class));
    }

    @SneakyThrows
    @Test
    void registerUserWhenErrorThenReturnToSignupWithErrorTest() {
        CreateUserRequest request = CreateUserRequest.builder()
                .login("testUser")
                .password("password")
                .confirmPassword("password")
                .name("Test User")
                .birthdate("1990-01-01")
                .build();
        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Пользователь с таким логином уже существует",
                "api/path",
                OffsetDateTime.now().toInstant()
        );
        WebClientResponseException exception = WebClientResponseException.create(
                400,
                "Bad Request",
                HttpHeaders.EMPTY,
                "{\"error\":\"Пользователь с таким логином уже существует\"}".getBytes(),
                null
        );

        when(accountsClient.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.error(exception));
        when(objectMapper.readValue(anyString(), eq(ErrorResponse.class)))
                .thenReturn(errorResponse);

        webTestClient.post()
                .uri("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("login=" + request.getLogin()
                        + "&password=" + request.getPassword()
                        + "&confirmPassword=" + request.getConfirmPassword()
                        + "&name=" + request.getName()
                        + "&birthdate=" + request.getBirthdate())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);

        verify(accountsClient).createUser(any(CreateUserRequest.class));
        verify(objectMapper).readValue(anyString(), eq(ErrorResponse.class));
    }
}