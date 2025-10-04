package ru.yandex.practicum.accounts.service;

import reactor.core.publisher.Flux;
import ru.yandex.practicum.accounts.model.AccountEntity;

public interface AccountService {

    Flux<AccountEntity> createAccounts(Flux<AccountEntity> accounts);

    Flux<AccountEntity> findByUserId(Long userId);

    Flux<AccountEntity> updateAccounts(Long userId, Flux<AccountEntity> accounts);
}
