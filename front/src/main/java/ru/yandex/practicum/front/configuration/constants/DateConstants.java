package ru.yandex.practicum.front.configuration.constants;

import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateConstants {

    public static final DateTimeFormatter DATE_FORMATTER_WITH_DOTS = DateTimeFormatter.ofPattern("dd.MM.yyyy");
}
