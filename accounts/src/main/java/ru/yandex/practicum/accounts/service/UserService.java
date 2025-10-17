package ru.yandex.practicum.accounts.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.controller.dto.EditPasswordRequest;
import ru.yandex.practicum.accounts.controller.dto.UserCreateRequest;
import ru.yandex.practicum.accounts.controller.dto.UserDto;
import ru.yandex.practicum.accounts.model.UserEntity;

public interface UserService {

    Mono<UserDto> createUser(UserCreateRequest request);

    Mono<UserDto> getUserByLogin(String login);

    Flux<UserEntity> getAllUsers();

    Mono<UserDto> updateUserPassword(String login, EditPasswordRequest request);

    Mono<UserDto> updateUserAccounts(String login, UserDto user);
}
