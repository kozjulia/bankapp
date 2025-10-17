package ru.yandex.practicum.cash.client.dto;

import java.math.BigDecimal;

public record OperationRequest(String userId, String operationType, BigDecimal amount) {

}