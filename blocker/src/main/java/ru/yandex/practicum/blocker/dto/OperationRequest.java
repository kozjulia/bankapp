package ru.yandex.practicum.blocker.dto;

import java.math.BigDecimal;

public record OperationRequest(String userId, String operationType, BigDecimal amount) {

}