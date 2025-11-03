package ru.yandex.practicum.exchange.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CurrencyDto {

    private String name;
    private String title;
    private BigDecimal value;
}
