package ru.yandex.practicum.blocker.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.blocker.dto.OperationCheckResult;
import ru.yandex.practicum.blocker.dto.OperationContext;

public interface BlockerOperationService {

    Mono<OperationCheckResult> checkOperation(OperationContext context);
}
