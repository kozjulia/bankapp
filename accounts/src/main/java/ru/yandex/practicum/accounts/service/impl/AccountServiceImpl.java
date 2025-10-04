package ru.yandex.practicum.accounts.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.model.AccountEntity;
import ru.yandex.practicum.accounts.repository.AccountRepository;
import ru.yandex.practicum.accounts.service.AccountService;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public Flux<AccountEntity> createAccounts(Flux<AccountEntity> accounts) {
        return accountRepository.saveAll(accounts);
    }

    public Flux<AccountEntity> findByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Flux<AccountEntity> updateAccounts(Long userId, Flux<AccountEntity> accounts) {
        return accounts
                .flatMap(account -> {
                    if (isNull(account.getId())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный список счетов"));
                    }
                    return accountRepository.findFirstByUserIdAndCurrency(userId, account.getCurrency())
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Аккаунт с id: %s не найден".formatted(account.getId()))))
                            .flatMap(existingAccount -> {
                                account.setUserId(userId);
                                return accountRepository.save(account);
                            });
                });
    }
}