package ru.yandex.practicum.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.yandex.practicum.transfer.client.AccountsClient;
import ru.yandex.practicum.transfer.client.BlockerClient;
import ru.yandex.practicum.transfer.client.ExchangeClient;
import ru.yandex.practicum.transfer.client.NotificationsClient;
import ru.yandex.practicum.transfer.client.dto.AccountDto;
import ru.yandex.practicum.transfer.client.dto.CurrencyDto;
import ru.yandex.practicum.transfer.client.dto.NotificationRequest;
import ru.yandex.practicum.transfer.client.dto.OperationCheckResult;
import ru.yandex.practicum.transfer.client.dto.OperationRequest;
import ru.yandex.practicum.transfer.client.dto.UserDto;
import ru.yandex.practicum.transfer.controller.dto.TransferRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private static final String TRANSFER_OPERATION = "TRANSFER";
    private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Аккаунт с валютой %s не найден";
    private static final String INSUFFICIENT_FUNDS_MESSAGE = "На счету %s недостаточно средств";

    private final AccountsClient accountsClient;
    private final ExchangeClient exchangeClient;
    private final BlockerClient blockerClient;
    private final NotificationsClient notificationsClient;

    public Mono<Void> transfer(TransferRequest request) {
        return validateTransfer(request)
                .flatMap(this::processTransfer);
    }

    private Mono<Void> processTransfer(TransferRequest request) {
        return checkAccounts(request)
                .flatMap(usersTuple -> processBlockerCheck(request, usersTuple.getT2()))
                .onErrorResume(error -> handleTransferError(error, request));
    }

    private Mono<Void> processBlockerCheck(TransferRequest request, UserDto fromUser) {
        return blockerClient.performOperation(createOperationRequest(request))
                .flatMap(result -> handleBlockerResult(result, request, fromUser));
    }

    private OperationRequest createOperationRequest(TransferRequest request) {
        return new OperationRequest(request.getLogin(), TRANSFER_OPERATION, request.getValue());
    }

    private Mono<Void> handleBlockerResult(OperationCheckResult result, TransferRequest request, UserDto fromUser) {
        if (result.blocked()) {
            sendTransferNotification(request.getLogin(), "Перевод заблокирован: " + result.message());
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Операция заблокирована: " + result.message()));
        }
        return getConvertedAmount(request)
                .flatMap(amount -> executeTransfer(request, fromUser, amount)
                        .doOnSuccess(__ -> sendSuccessNotification(request)));
    }

    private Mono<Void> sendSuccessNotification(TransferRequest request) {
        return sendTransferNotification(
                request.getLogin(),
                "Перевод успешно выполнен на сумму " + request.getValue()
        );
    }

    private Mono<Void> sendTransferNotification(String login, String message) {
        return notificationsClient.sendNotification(new NotificationRequest(login, message))
                .doOnError(e -> log.error("Ошибка при отправке увндомления", e))
                .onErrorComplete();
    }

    private AccountDto findAccountByCurrency(UserDto user, String currencyName, String errorMessage) {
        return user.getAccounts().stream()
                .filter(acc -> acc.getCurrency().getName().equals(currencyName))
                .filter(AccountDto::isExists)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format(errorMessage, currencyName)));
    }

    private Mono<Tuple2<TransferRequest, UserDto>> checkAccounts(TransferRequest request) {
        return Mono.zip(
                        accountsClient.getAccountDetails(request.getLogin()),
                        accountsClient.getAccountDetails(request.getToLogin())
                )
                .flatMap(tuple -> validateAccounts(request, tuple.getT1(), tuple.getT2()));
    }

    private Mono<Tuple2<TransferRequest, UserDto>> validateAccounts(
            TransferRequest request,
            UserDto fromUser,
            UserDto toUser
    ) {
        AccountDto fromAccount = findAccountByCurrency(fromUser, request.getFromCurrency(),
                ACCOUNT_NOT_FOUND_MESSAGE);
        findAccountByCurrency(toUser, request.getToCurrency(), ACCOUNT_NOT_FOUND_MESSAGE);

        if (fromAccount.getValue().subtract(request.getValue()).compareTo(BigDecimal.ZERO) < 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(INSUFFICIENT_FUNDS_MESSAGE, request.getFromCurrency())));
        }
        return Mono.just(Tuples.of(request, fromUser));
    }

    private Mono<Void> handleTransferError(Throwable error, TransferRequest request) {
        String errorMessage = error instanceof ResponseStatusException
                ? error.getMessage()
                : "Ошибка при переводе: " + error.getMessage();
        sendTransferNotification(request.getLogin(), errorMessage);
        return Mono.error(error);
    }

    private Mono<BigDecimal> getConvertedAmount(TransferRequest request) {
        return exchangeClient.getCurrencyRates()
                .collectList()
                .flatMap(rates -> {
                    CurrencyDto fromCurrency = findCurrency(rates, request.getFromCurrency());
                    CurrencyDto toCurrency = findCurrency(rates, request.getToCurrency());
                    BigDecimal convertedAmount = request.getValue()
                            .multiply(fromCurrency.getValue())
                            .divide(toCurrency.getValue(), RoundingMode.HALF_UP);
                    return Mono.just(convertedAmount);
                });
    }

    private CurrencyDto findCurrency(List<CurrencyDto> rates, String currencyName) {
        return rates.stream()
                .filter(c -> c.getName().equals(currencyName))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Валюта " + currencyName + " не найдена"
                ));
    }

    private Mono<Void> executeTransfer(TransferRequest request, UserDto fromUser, BigDecimal convertedAmount) {
        AccountDto fromAccount = fromUser.getAccounts().stream()
                .filter(acc -> acc.getCurrency().getName().equals(request.getFromCurrency()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Счет отправителя не найден"
                ));

        fromAccount.setValue(fromAccount.getValue().subtract(request.getValue()));

        return accountsClient.editUserAccounts(request.getLogin(), fromUser)
                .then(updateRecipientBalance(request, convertedAmount));
    }

    private Mono<Void> updateRecipientBalance(TransferRequest request, BigDecimal convertedAmount) {
        return accountsClient.getAccountDetails(request.getToLogin())
                .flatMap(toUser -> {
                    AccountDto toAccount = toUser.getAccounts().stream()
                            .filter(acc -> acc.getCurrency().getName().equals(request.getToCurrency()))
                            .findFirst()
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Счет получателя не найден"
                            ));

                    toAccount.setValue(toAccount.getValue().add(convertedAmount));
                    return accountsClient.editUserAccounts(request.getToLogin(), toUser);
                })
                .then();
    }

    private Mono<TransferRequest> validateTransfer(TransferRequest request) {
        if (request.getValue().compareTo(BigDecimal.ZERO) < 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Значение перевода должно быть положительным"));
        }
        return Mono.just(request);
    }
}