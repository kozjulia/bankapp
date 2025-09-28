package ru.yandex.practicum.blocker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.blocker.dto.OperationCheckResult;
import ru.yandex.practicum.blocker.dto.OperationContext;
import ru.yandex.practicum.blocker.dto.OperationRequest;
import ru.yandex.practicum.blocker.service.BlockerOperationService;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
public class BlockerOperationController {

    private final BlockerOperationService blockerOperationService;

    @PostMapping("/api/operations")
    public Mono<OperationCheckResult> performOperation(@RequestBody Mono<OperationRequest> requestMono) {
        return requestMono
                .flatMap(request -> {
                    OperationContext context = OperationContext.builder()
                            .userId(request.userId())
                            .operationType(request.operationType())
                            .amount(request.amount())
                            .timestamp(OffsetDateTime.now())
                            .build();

                    return blockerOperationService.checkOperation(context);
                });
    }
}
