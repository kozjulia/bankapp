package ru.yandex.practicum.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cash.client.AccountsClient;
import ru.yandex.practicum.cash.client.BlockerClient;
import ru.yandex.practicum.cash.client.NotificationsClient;
import ru.yandex.practicum.cash.client.dto.AccountDto;
import ru.yandex.practicum.cash.client.dto.NotificationRequest;
import ru.yandex.practicum.cash.client.dto.OperationRequest;
import ru.yandex.practicum.cash.client.dto.UserDto;
import ru.yandex.practicum.cash.dto.CashChangeRequest;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService {

    private static final String ERROR_EMPTY_REQUEST = "Запрос или валюта не могут быть пустыми";
    private static final String ERROR_NEGATIVE_AMOUNT = "Сумма должна быть больше нуля";
    private static final String ERROR_ACCOUNT_NOT_FOUND = "Счет с указанной валютой не найден";
    private static final String ERROR_INSUFFICIENT_FUNDS = "На счету недостаточно средств";

    private final AccountsClient accountsClient;
    private final BlockerClient blockerClient;
    private final NotificationsClient notificationsClient;

    public Mono<Void> processAccountTransaction(String login, CashChangeRequest request) {
        return validateRequest(request)
                .flatMap(validRequest ->
                        blockerClient.performOperation(new OperationRequest(login, request.getAction().name(), request.getValue()))
                                .flatMap(checkResult -> {
                                    if (checkResult.blocked()) {
                                        return Mono.error(new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                checkResult.message()
                                        ));
                                    }
                                    return processAccountOperation(login, validRequest);
                                }))
                .flatMap(result -> sendSuccessNotification(login, "Операция прошла успешно"))
                .onErrorResume(error -> {
                    String errorMessage = error instanceof ResponseStatusException
                            ? ((ResponseStatusException) error).getReason()
                            : "Операция была отменена: " + error.getMessage();
                    return sendErrorNotification(login, errorMessage);
                })
                .then();
    }

    private Mono<Void> sendSuccessNotification(String login, String message) {
        return notificationsClient.sendNotification(new NotificationRequest(login, message))
                .doOnError(e -> log.error("Ошибка при отправке успешного уведомления", e))
                .then();
    }

    private Mono<Void> sendErrorNotification(String login, String message) {
        return notificationsClient.sendNotification(new NotificationRequest(login, message))
                .doOnError(e -> log.error("Ошибка при отправке неуспешного уведомления", e))
                .onErrorComplete();
    }

    private Mono<CashChangeRequest> validateRequest(CashChangeRequest request) {
        return Mono.just(request)
                .filter(this::isValidRequest)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, ERROR_EMPTY_REQUEST)))
                .filter(this::isPositiveAmount)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, ERROR_NEGATIVE_AMOUNT)));
    }

    private boolean isValidRequest(CashChangeRequest request) {
        return request != null && request.getCurrency() != null;
    }

    private boolean isPositiveAmount(CashChangeRequest request) {
        return request.getValue().compareTo(BigDecimal.ZERO) > 0;
    }

    private Mono<UserDto> processAccountOperation(String login, CashChangeRequest request) {
        return accountsClient.getAccountDetails(login)
                .flatMap(user -> {
                    AccountDto account = findAccountByCurrency(user, request.getCurrency());
                    validateWithdrawal(account, request);
                    updateAccountBalance(account, request);
                    return accountsClient.editUserAccounts(login, user);
                });
    }

    private AccountDto findAccountByCurrency(UserDto user, String currency) {
        return user.getAccounts().stream()
                .filter(acc -> acc.getCurrency() != null
                        && acc.getCurrency().getName().equals(currency)
                        && acc.isExists())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ERROR_ACCOUNT_NOT_FOUND));
    }

    private void validateWithdrawal(AccountDto account, CashChangeRequest request) {
        if (isWithdrawal(request) && hasInsufficientFunds(account, request)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ERROR_INSUFFICIENT_FUNDS);
        }
    }

    private boolean isWithdrawal(CashChangeRequest request) {
        return request.getAction().equals(CashChangeRequest.Action.GET);
    }

    private boolean hasInsufficientFunds(AccountDto account, CashChangeRequest request) {
        return account.getValue().compareTo(request.getValue()) < 0;
    }

    private void updateAccountBalance(AccountDto account, CashChangeRequest request) {
        BigDecimal newBalance = calculateNewBalance(account, request);
        account.setValue(newBalance);
    }

    private BigDecimal calculateNewBalance(AccountDto account, CashChangeRequest request) {
        return request.getAction().equals(CashChangeRequest.Action.PUT)
                ? account.getValue().add(request.getValue())
                : account.getValue().subtract(request.getValue());
    }
}