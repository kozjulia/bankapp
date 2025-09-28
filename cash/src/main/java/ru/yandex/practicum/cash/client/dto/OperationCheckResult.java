package ru.yandex.practicum.cash.client.dto;

public record OperationCheckResult(boolean blocked, String message, String detectionAlgorithm) {

}
