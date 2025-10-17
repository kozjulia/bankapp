package ru.yandex.practicum.front.dto;

import java.time.Instant;

public record ErrorResponse(int status, String error, String path, Instant timestamp) {

}
