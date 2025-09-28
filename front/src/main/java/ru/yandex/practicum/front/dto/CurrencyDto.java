package ru.yandex.practicum.front.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrencyDto {

    private String title;
    private String name;
    private Double value;
}
