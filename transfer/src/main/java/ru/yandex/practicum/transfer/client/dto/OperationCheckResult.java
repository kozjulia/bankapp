package ru.yandex.practicum.transfer.client.dto;

public record OperationCheckResult(boolean blocked, String message, String detectionAlgorithm) {

}
