package ru.yandex.practicum.accounts.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.controller.dto.EditPasswordRequest;
import ru.yandex.practicum.accounts.controller.dto.UserDto;
import ru.yandex.practicum.accounts.controller.dto.UserCreateRequest;
import ru.yandex.practicum.accounts.model.UserEntity;
import ru.yandex.practicum.accounts.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public Mono<UserDto> createUser(@RequestBody @Valid UserCreateRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/{login}")
    public Mono<UserDto> getUser(@PathVariable String login) {
        return userService.getUserByLogin(login);
    }

    @GetMapping
    public Flux<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/{login}/editPassword")
    public Mono<UserDto> editPassword(
            @PathVariable String login,
            @RequestBody @Valid EditPasswordRequest editPasswordRequest
    ) {
        return userService.updateUserPassword(login, editPasswordRequest);
    }

    @PostMapping("/{login}/editUserAccounts")
    public Mono<UserDto> editUserAccounts(
            @PathVariable String login,
            @RequestBody @Valid UserDto user
    ) {
        return userService.updateUserAccounts(login, user);
    }
}