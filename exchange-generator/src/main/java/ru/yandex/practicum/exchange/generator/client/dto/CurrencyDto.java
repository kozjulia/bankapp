package ru.yandex.practicum.exchange.generator.client.dto;

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
