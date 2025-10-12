package ru.yandex.practicum.blocker.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.blocker.dto.OperationContext;

public interface BlockerOperationDetector {

    String getDetectionAlgorithmName();

    Mono<Boolean> isOperationSuspicious(OperationContext context);
}
