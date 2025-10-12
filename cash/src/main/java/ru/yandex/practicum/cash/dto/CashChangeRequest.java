package ru.yandex.practicum.cash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CashChangeRequest {

    private String currency;
    private BigDecimal value;
    private Action action;

    public enum Action {
        PUT, GET
    }
}
