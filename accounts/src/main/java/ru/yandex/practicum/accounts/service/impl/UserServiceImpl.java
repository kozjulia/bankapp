package ru.yandex.practicum.accounts.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.yandex.practicum.accounts.mq.NotificationSender;
import ru.yandex.practicum.accounts.mq.dto.NotificationRequest;
import ru.yandex.practicum.accounts.controller.dto.AccountDto;
import ru.yandex.practicum.accounts.controller.dto.CurrencyEnum;
import ru.yandex.practicum.accounts.controller.dto.EditPasswordRequest;
import ru.yandex.practicum.accounts.controller.dto.UserCreateRequest;
import ru.yandex.practicum.accounts.controller.dto.UserDto;
import ru.yandex.practicum.accounts.mapper.UserMapper;
import ru.yandex.practicum.accounts.model.AccountEntity;
import ru.yandex.practicum.accounts.model.UserEntity;
import ru.yandex.practicum.accounts.repository.UserRepository;
import ru.yandex.practicum.accounts.service.AccountService;
import ru.yandex.practicum.accounts.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static ru.yandex.practicum.accounts.validator.UserValidator.validateAccounts;
import static ru.yandex.practicum.accounts.validator.UserValidator.validateBirthdate;
import static ru.yandex.practicum.accounts.validator.UserValidator.validatePasswordChange;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND_ERROR_MSG = "Пользователь с логином %s не найден";

    private final UserMapper userMapper;
    private final AccountService accountService;
    private final UserRepository userRepository;
    private final NotificationSender notificationSender;

    public Mono<UserDto> createUser(UserCreateRequest request) {
        return validateRequest(request)
                .then(validateExistingUser(request.getLogin()))
                .then(Mono.just(request))
                .flatMap(this::createUserWithAccounts);
    }

    public Mono<UserDto> getUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .flatMap(userEntity -> accountService.findByUserId(userEntity.getId())
                        .collectList()
                        .map(accountEntities -> userMapper.toUserDto(userEntity, accountEntities)))
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatusCode.valueOf(404),
                                USER_NOT_FOUND_ERROR_MSG.formatted(login)))
                );
    }

    public Flux<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<UserDto> updateUserPassword(String login, EditPasswordRequest request) {
        return validatePasswordChange(request.getPassword(), request.getConfirmPassword())
                .then(findAndUpdateUser(login, request))
                .flatMap(this::enrichWithAccounts)
                .map(tuple -> userMapper.toUserDto(tuple.getT1(), tuple.getT2()))
                .flatMap(user -> sendNotification(login, "Пароль успешно обновлен")
                        .thenReturn(user)
                )
                .onErrorResume(error -> sendNotification(
                        login,
                        String.format("Не удалось обновить пароль: %s", error.getMessage())
                ).then(Mono.error(error)));
    }

    public Mono<UserDto> updateUserAccounts(String login, UserDto user) {
        return validateUserData(user)
                .then(processUserUpdate(login, user))
                .flatMap(updatedUser -> sendAccountUpdateNotification(login, user.getAccounts().size())
                        .thenReturn(updatedUser));
    }

    private Mono<Void> validateRequest(UserCreateRequest request) {
        return Mono.just(request)
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Запрос не может быть пустым")))
                .flatMap(r -> validatePasswordChange(r.getPassword(), r.getConfirmPassword())
                        .then(Mono.just(r)))
                .flatMap(r -> validateBirthdate(r.getBirthdate())
                        .then(Mono.just(r)))
                .then();
    }

    private Mono<Void> validateExistingUser(String login) {
        return userRepository.findByLogin(login)
                .hasElement()
                .flatMap(exists -> exists
                        ? Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь с логином %s уже существует".formatted(login)))
                        : Mono.empty());
    }

    private Mono<UserDto> createUserWithAccounts(UserCreateRequest request) {
        return createUserEntity(request)
                .flatMap(userEntity -> createUserAccounts(userEntity.getId())
                        .map(accounts -> Tuples.of(userEntity, accounts)))
                .map(tuple -> userMapper.toUserDto(tuple.getT1(), tuple.getT2()));
    }

    private Mono<UserEntity> createUserEntity(UserCreateRequest request) {
        LocalDate birthdate = LocalDate.parse(request.getBirthdate());

        return Mono.just(UserEntity.builder()
                        .login(request.getLogin())
                        .name(request.getName())
                        .password(request.getPassword())
                        .birthdate(birthdate)
                        .build())
                .flatMap(userRepository::save);
    }

    private Mono<List<AccountEntity>> createUserAccounts(Long userId) {
        List<AccountEntity> accountEntities = Arrays.stream(CurrencyEnum.values())
                .map(currency -> AccountEntity.builder()
                        .currency(currency.name())
                        .userId(userId)
                        .value(BigDecimal.ZERO)
                        .exists(true)
                        .build())
                .toList();

        return accountService.createAccounts(Flux.fromIterable(accountEntities))
                .collectList();
    }

    private Mono<Void> sendNotification(String login, String message) {
        notificationSender.sendNotification(new NotificationRequest(login, message));
        return Mono.empty();
    }

    private Mono<UserEntity> findAndUpdateUser(String login, EditPasswordRequest request) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ERROR_MSG.formatted(login))))
                .flatMap(userEntity -> {
                    userEntity.setPassword(request.getPassword());
                    return userRepository.save(userEntity);
                });
    }

    private Mono<Tuple2<UserEntity, List<AccountEntity>>> enrichWithAccounts(UserEntity userEntity) {
        return accountService.findByUserId(userEntity.getId())
                .collectList()
                .map(accounts -> Tuples.of(userEntity, accounts));
    }

    private Mono<Void> validateUserData(UserDto user) {
        return Mono.just(user)
                .flatMap(r -> validateBirthdate(r.getBirthdate()))
                .then(validateAccounts(user));
    }

    private Mono<UserDto> processUserUpdate(String login, UserDto user) {
        return findAndUpdateUserData(login, user)
                .flatMap(userEntity -> updateUserAccountsData(userEntity, user.getAccounts()))
                .flatMap(this::enrichWithAccounts)
                .map(tuple -> userMapper.toUserDto(tuple.getT1(), tuple.getT2()));
    }

    private Mono<Void> sendAccountUpdateNotification(String login, int accountsCount) {
        String notificationMessage = String.format("Данные вашего аккаунта успешно обновлены. Изменено счетов: %d", accountsCount);

        notificationSender
                .sendNotification(new NotificationRequest(login, notificationMessage));

        return Mono.empty();
    }

    private Mono<UserEntity> findAndUpdateUserData(String login, UserDto user) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ERROR_MSG.formatted(login))))
                .flatMap(userEntity -> {
                    if (nonNull(user.getName())) {
                        userEntity.setName(user.getName());
                    }
                    if (nonNull(user.getBirthdate())) {
                        userEntity.setBirthdate(user.getBirthdate());
                    }
                    return userRepository.save(userEntity);
                });
    }

    private Mono<UserEntity> updateUserAccountsData(UserEntity userEntity, List<AccountDto> userAccounts) {
        return accountService.findByUserId(userEntity.getId())
                .collectList()
                .flatMap(accounts -> {
                    List<AccountEntity> accountEntities = convertToAccountEntities(userAccounts);
                    return accountService.updateAccounts(userEntity.getId(), Flux.fromIterable(accountEntities))
                            .collectList()
                            .thenReturn(userEntity);
                });
    }

    private List<AccountEntity> convertToAccountEntities(List<AccountDto> accounts) {
        return accounts.stream()
                .map(account -> AccountEntity.builder()
                        .id(account.getId())
                        .currency(account.getCurrency().getName())
                        .value(account.getValue())
                        .exists(account.isExists())
                        .build())
                .toList();
    }
}