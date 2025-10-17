package ru.yandex.practicum.blocker.service.impl;

import ru.yandex.practicum.blocker.configuration.TestSecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.blocker.dto.OperationCheckResult;
import ru.yandex.practicum.blocker.dto.OperationContext;
import ru.yandex.practicum.blocker.service.BlockerOperationDetector;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestSecurityConfiguration.class)
public class BlockerOperationServiceImplTest {

    @Mock
    private BlockerOperationDetector detector1;

    @Mock
    private BlockerOperationDetector detector2;

    private BlockerOperationServiceImpl service;
    private OperationContext context;

    @BeforeEach
    void setUp() {
        service = new BlockerOperationServiceImpl(List.of(detector1, detector2));
        context = OperationContext.builder()
                .userId("user1")
                .operationType("PAYMENT")
                .amount(BigDecimal.valueOf(1000.0))
                .timestamp(OffsetDateTime.now())
                .metadata(Map.of("key", "value"))
                .build();
    }

    @Test
    void checkOperationWhenNoDetectorsFlagThenReturnOkTest() {
        when(detector1.isOperationSuspicious(context)).thenReturn(Mono.just(false));
        when(detector2.isOperationSuspicious(context)).thenReturn(Mono.just(false));

        StepVerifier.create(service.checkOperation(context))
                .expectNext(OperationCheckResult.ok())
                .verifyComplete();
    }

    @Test
    void checkOperationWhenFirstDetectorFlagsThenReturnBlockedTest() {
        when(detector1.isOperationSuspicious(context)).thenReturn(Mono.just(true));
        when(detector1.getDetectionAlgorithmName()).thenReturn("DETECTOR_1");

        StepVerifier.create(service.checkOperation(context))
                .expectNext(new OperationCheckResult(
                        true,
                        "Операция заблокирована DETECTOR_1",
                        "DETECTOR_1"))
                .verifyComplete();
    }

    @Test
    void checkOperationWhenSecondDetectorFlagsThenReturnBlockedTest() {
        when(detector1.isOperationSuspicious(context)).thenReturn(Mono.just(false));
        when(detector2.isOperationSuspicious(context)).thenReturn(Mono.just(true));
        when(detector2.getDetectionAlgorithmName()).thenReturn("DETECTOR_2");

        StepVerifier.create(service.checkOperation(context))
                .expectNext(new OperationCheckResult(
                        true,
                        "Операция заблокирована DETECTOR_2",
                        "DETECTOR_2"))
                .verifyComplete();
    }

    @Test
    void checkOperationWhenNoDetectorsThenReturnOkTest() {
        service = new BlockerOperationServiceImpl(Collections.emptyList());

        StepVerifier.create(service.checkOperation(context))
                .expectNext(OperationCheckResult.ok())
                .verifyComplete();
    }

    @Test
    void checkOperationWhenDetectorReturnsErrorThenPropagateErrorTest() {
        when(detector1.isOperationSuspicious(context))
                .thenReturn(Mono.error(new RuntimeException("Test error")));

        StepVerifier.create(service.checkOperation(context))
                .expectError(RuntimeException.class)
                .verify();
    }
}