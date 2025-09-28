package ru.yandex.practicum.blocker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.blocker.dto.OperationCheckResult;
import ru.yandex.practicum.blocker.dto.OperationContext;
import ru.yandex.practicum.blocker.service.BlockerOperationDetector;
import ru.yandex.practicum.blocker.service.BlockerOperationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockerOperationServiceImpl implements BlockerOperationService {

    private final List<BlockerOperationDetector> detectors;

    public Mono<OperationCheckResult> checkOperation(OperationContext context) {
        return Flux.fromIterable(detectors)
                .flatMap(detector ->
                        detector.isOperationSuspicious(context)
                                .filter(Boolean.TRUE::equals)
                                .map(isSuspicious -> new OperationCheckResult(
                                        true,
                                        "Операция заблокирована " + detector.getDetectionAlgorithmName(),
                                        detector.getDetectionAlgorithmName()
                                ))
                )
                .next()
                .switchIfEmpty(Mono.just(OperationCheckResult.ok()));
    }
}
