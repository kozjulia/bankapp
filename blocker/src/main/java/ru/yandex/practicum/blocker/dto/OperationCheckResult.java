package ru.yandex.practicum.blocker.dto;

public record OperationCheckResult(boolean blocked, String message, String detectionAlgorithm) {

    public static OperationCheckResult ok() {
        return new OperationCheckResult(false, "Operation allowed", null);
    }
}
