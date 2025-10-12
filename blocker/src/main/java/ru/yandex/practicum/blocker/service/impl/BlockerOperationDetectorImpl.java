package ru.yandex.practicum.blocker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.blocker.dto.OperationContext;
import ru.yandex.practicum.blocker.service.BlockerOperationDetector;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class BlockerOperationDetectorImpl implements BlockerOperationDetector {

    private final Random random = new Random();

    @Override
    public String getDetectionAlgorithmName() {
        return "RANDOM_DETECTOR";
    }

    @Override
    public Mono<Boolean> isOperationSuspicious(OperationContext context) {
        return Mono.just(random.nextDouble() < 0.2);
    }
}
