package ru.yandex.practicum.transfer.client.dto;

import java.math.BigDecimal;

public record OperationRequest(String userId, String operationType, BigDecimal amount) {

}