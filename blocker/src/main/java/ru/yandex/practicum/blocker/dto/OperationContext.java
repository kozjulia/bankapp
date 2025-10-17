package ru.yandex.practicum.blocker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
public class OperationContext {

    private String userId;
    private String operationType;
    private BigDecimal amount;
    private OffsetDateTime timestamp;
    private Map<String, String> metadata;
}
