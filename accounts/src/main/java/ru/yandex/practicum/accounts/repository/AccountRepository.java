package ru.yandex.practicum.accounts.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.model.AccountEntity;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<AccountEntity, Long> {

    Flux<AccountEntity> findByUserId(Long userId);

    Mono<AccountEntity> findFirstByUserIdAndCurrency(Long userId, String currency);
}
