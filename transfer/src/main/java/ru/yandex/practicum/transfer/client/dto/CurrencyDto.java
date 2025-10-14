package ru.yandex.practicum.transfer.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CurrencyDto {

    private String title;
    private String name;
    private BigDecimal value;
}
